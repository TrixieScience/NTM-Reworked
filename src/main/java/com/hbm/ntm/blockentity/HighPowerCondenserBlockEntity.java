package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HighPowerCondenserBlock;
import com.hbm.ntm.config.HbmConfig;
import com.hbm.ntm.energy.HeReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/** A very large radiator which refuses to do partial invoices. */
public final class HighPowerCondenserBlockEntity extends BlockEntity implements HeReceiver {
    public static final int SOURCE_POWER_CHECK_PER_MB = 10;

    private final FluidTank spentSteam = fixedTank(
            HbmConfig.POWERED_CONDENSER_INPUT_CAPACITY.get(), ModFluids.SPENTSTEAM.get());
    private final FluidTank water = fixedTank(
            HbmConfig.POWERED_CONDENSER_OUTPUT_CAPACITY.get(), Fluids.WATER);
    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }
        @Override public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? spentSteam.getFluid() : tank == 1 ? water.getFluid() : FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int tank) {
            return tank == 0 ? spentSteam.getCapacity() : tank == 1 ? water.getCapacity() : 0;
        }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && stack.getFluid().isSame(ModFluids.SPENTSTEAM.get());
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            return resource.getFluid().isSame(ModFluids.SPENTSTEAM.get())
                    ? spentSteam.fill(resource, action) : 0;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return resource.getFluid().isSame(Fluids.WATER) ? water.drain(resource, action) : FluidStack.EMPTY;
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return water.drain(maxDrain, action); }
    };

    private long power;
    private int age;
    private int waterTimer;
    private int throughput;
    private float spin;
    private float lastSpin;
    private long lastSnapshot = Long.MIN_VALUE;

    public HighPowerCondenserBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.MACHINE_CONDENSER_POWERED.get(), position, state);
    }

    private FluidTank fixedTank(int capacity, net.minecraft.world.level.material.Fluid fluid) {
        return new FluidTank(capacity, stack -> stack.getFluid().isSame(fluid)) {
            @Override protected void onContentsChanged() { HighPowerCondenserBlockEntity.this.setChanged(); }
        };
    }

    public static void tick(Level level, BlockPos position, BlockState state,
                            HighPowerCondenserBlockEntity condenser) {
        if (level.isClientSide) condenser.clientTick(level, state);
        else condenser.serverTick((ServerLevel) level, position, state);
    }

    private void clientTick(Level level, BlockState state) {
        lastSpin = spin;
        if (waterTimer <= 0) return;
        spin += 30F;
        if (spin >= 360F) {
            spin -= 360F;
            lastSpin -= 360F;
        }
        if (level.getGameTime() % 4L != 0L) return;
        Direction facing = state.getValue(HighPowerCondenserBlock.FACING);
        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 1.5D;
        double z = worldPosition.getZ() + 0.5D;
        level.addParticle(ParticleTypes.CLOUD, x + facing.getStepX() * 1.5D, y,
                z + facing.getStepZ() * 1.5D, facing.getStepX() * 0.1D, 0D, facing.getStepZ() * 0.1D);
        level.addParticle(ParticleTypes.CLOUD, x - facing.getStepX() * 1.5D, y,
                z - facing.getStepZ() * 1.5D, -facing.getStepX() * 0.1D, 0D, -facing.getStepZ() * 0.1D);
    }

    private void serverTick(ServerLevel level, BlockPos position, BlockState state) {
        conformTanks();
        age = age == 0 ? 1 : 0;
        if (waterTimer > 0) waterTimer--;

        int convert = Math.min(spentSteam.getFluidAmount(), water.getSpace());
        throughput = convert;
        if (power >= (long) convert * SOURCE_POWER_CHECK_PER_MB) {
            spentSteam.drain(convert, IFluidHandler.FluidAction.EXECUTE);
            if (convert > 0) waterTimer = 20;
            water.fill(new FluidStack(Fluids.WATER, convert), IFluidHandler.FluidAction.EXECUTE);
            power = Math.max(0L, power - (long) convert * HbmConfig.POWERED_CONDENSER_POWER_CONSUMPTION.get());
        }

        Direction facing = state.getValue(HighPowerCondenserBlock.FACING);
        for (HighPowerCondenserBlock.Connection connection
                : HighPowerCondenserBlock.connections(position, facing)) {
            pullSpentSteam(level, connection.target(), connection.outward());
            pushWater(level, connection.target(), connection.outward());
            if (level.getGameTime() % 20L == 0L) {
                trySubscribe(level, connection.target(), connection.outward());
            }
        }
        sync(level, position, state);
        setChanged();
    }

    private void pullSpentSteam(ServerLevel level, BlockPos target, Direction outward) {
        if (spentSteam.getSpace() <= 0) return;
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK,
                target, outward.getOpposite());
        if (handler == null) return;
        FluidStack request = new FluidStack(ModFluids.SPENTSTEAM.get(), spentSteam.getSpace());
        FluidStack available = handler.drain(request, IFluidHandler.FluidAction.SIMULATE);
        if (available.isEmpty() || !available.getFluid().isSame(ModFluids.SPENTSTEAM.get())) return;
        int accepted = spentSteam.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return;
        FluidStack drained = handler.drain(new FluidStack(ModFluids.SPENTSTEAM.get(), accepted),
                IFluidHandler.FluidAction.EXECUTE);
        spentSteam.fill(drained, IFluidHandler.FluidAction.EXECUTE);
    }

    private void pushWater(ServerLevel level, BlockPos target, Direction outward) {
        if (water.isEmpty()) return;
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK,
                target, outward.getOpposite());
        if (handler == null) return;
        int accepted = handler.fill(water.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
        if (accepted > 0) water.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
    }

    private void conformTanks() {
        if (!spentSteam.isEmpty() && !spentSteam.getFluid().getFluid().isSame(ModFluids.SPENTSTEAM.get())) {
            spentSteam.setFluid(FluidStack.EMPTY);
        }
        if (!water.isEmpty() && !water.getFluid().getFluid().isSame(Fluids.WATER)) {
            water.setFluid(FluidStack.EMPTY);
        }
        power = Mth.clamp(power, 0L, getMaxPower());
    }

    private void sync(ServerLevel level, BlockPos position, BlockState state) {
        long snapshot = power ^ ((long) spentSteam.getFluidAmount() << 7)
                ^ ((long) water.getFluidAmount() << 29) ^ ((long) waterTimer << 57);
        if (snapshot == lastSnapshot && level.getGameTime() % 20L != 0L) return;
        lastSnapshot = snapshot;
        level.sendBlockUpdated(position, state, state, Block.UPDATE_CLIENTS);
    }

    public IFluidHandler fluidHandler() { return fluidHandler; }
    public FluidTank spentSteamTank() { return spentSteam; }
    public FluidTank waterTank() { return water; }
    public int waterTimer() { return waterTimer; }
    public int throughput() { return throughput; }
    public float spin(float partialTick) { return Mth.lerp(partialTick, lastSpin, spin); }

    @Override public long getPower() { return power; }
    @Override public void setPower(long power) { this.power = Mth.clamp(power, 0L, getMaxPower()); }
    @Override public long getMaxPower() { return HbmConfig.POWERED_CONDENSER_MAX_POWER.get(); }
    @Override public boolean isHeLoaded() { return hasLevel() && !isRemoved(); }

    public AABB renderBounds() {
        BlockPos position = getBlockPos();
        return new AABB(position.getX() - 3D, position.getY(), position.getZ() - 3D,
                position.getX() + 4D, position.getY() + 3D, position.getZ() + 4D);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", power);
        // Yes, these names are backwards. The 1.7.10 saves insist.
        tag.put("water", spentSteam.writeToNBT(registries, new CompoundTag()));
        tag.put("steam", water.writeToNBT(registries, new CompoundTag()));
        tag.putInt("waterTimer", waterTimer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        power = tag.getLong("power");
        if (tag.contains("water")) spentSteam.readFromNBT(registries, tag.getCompound("water"));
        if (tag.contains("steam")) water.readFromNBT(registries, tag.getCompound("steam"));
        waterTimer = tag.getInt("waterTimer");
        conformTanks();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("power", power);
        tag.putInt("spentSteam", spentSteam.getFluidAmount());
        tag.putInt("water", water.getFluidAmount());
        tag.putInt("waterTimer", waterTimer);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        power = Mth.clamp(tag.getLong("power"), 0L, getMaxPower());
        int spentAmount = Mth.clamp(tag.getInt("spentSteam"), 0, spentSteam.getCapacity());
        int waterAmount = Mth.clamp(tag.getInt("water"), 0, water.getCapacity());
        spentSteam.setFluid(spentAmount == 0 ? FluidStack.EMPTY
                : new FluidStack(ModFluids.SPENTSTEAM.get(), spentAmount));
        water.setFluid(waterAmount == 0 ? FluidStack.EMPTY : new FluidStack(Fluids.WATER, waterAmount));
        waterTimer = Mth.clamp(tag.getInt("waterTimer"), 0, 20);
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
