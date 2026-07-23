package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.DrainagePipeBlock;
import com.hbm.ntm.fluid.FluidTankProperties;
import com.hbm.ntm.fluid.PrioritizedFluidHandler;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.pollution.PollutionData;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.Set;

public final class DrainagePipeBlockEntity extends BlockEntity {
    public static final int CAPACITY = 2_000;
    private static final Set<FluidIdentifierItem.Selection> OIL_SPILL_FLUIDS = EnumSet.of(
            FluidIdentifierItem.Selection.OIL,
            FluidIdentifierItem.Selection.HEAVYOIL,
            FluidIdentifierItem.Selection.NAPHTHA,
            FluidIdentifierItem.Selection.SMEAR,
            FluidIdentifierItem.Selection.HEATINGOIL);

    private FluidIdentifierItem.Selection selection = FluidIdentifierItem.Selection.NONE;
    private final FluidTank tank = new FluidTank(CAPACITY, stack -> selection.accepts(stack.getFluid())) {
        @Override protected void onContentsChanged() { DrainagePipeBlockEntity.this.setChanged(); }
    };
    private int lastAmount = Integer.MIN_VALUE;
    private FluidIdentifierItem.Selection lastSelection;

    private final PrioritizedFluidHandler handler = new PrioritizedFluidHandler() {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int index) {
            return index == 0 ? tank.getFluid().copy() : FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int index) { return index == 0 ? CAPACITY : 0; }
        @Override public boolean isFluidValid(int index, FluidStack stack) {
            return index == 0 && selection.accepts(stack.getFluid());
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            return isFluidValid(0, resource) ? tank.fill(resource, action) : 0;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
        @Override public int fluidPriority() { return 1; }
    };

    public DrainagePipeBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.MACHINE_DRAIN.get(), position, state);
    }

    public static void tick(Level level, BlockPos position, BlockState state, DrainagePipeBlockEntity drain) {
        if (level instanceof ServerLevel server) drain.serverTick(server, position, state);
    }

    private void serverTick(ServerLevel level, BlockPos position, BlockState state) {
        if (!tank.isEmpty()) {
            FluidTankProperties.Profile profile = FluidTankProperties.get(selection);
            if (profile.symbol() == FluidTankProperties.Symbol.ANTIMATTER) {
                level.explode(null, position.getX() + .5D, position.getY() + .5D,
                        position.getZ() + .5D, 10F, true, Level.ExplosionInteraction.TNT);
                return;
            }

            int spilled = Math.max(tank.getFluidAmount() / 2, 1);
            tank.drain(spilled, IFluidHandler.FluidAction.EXECUTE);
            pollute(level, position, spilled, profile);
            spawnOutletParticles(level, position, state.getValue(DrainagePipeBlock.FACING), profile);
            if (spilled >= 100 && OIL_SPILL_FLUIDS.contains(selection)
                    && level.random.nextInt(20) == 0) {
                placeOilSpill(level, position, state.getValue(DrainagePipeBlock.FACING));
            }
        }
        syncIfChanged(level, position, state);
    }

    private void pollute(ServerLevel level, BlockPos position, int amount,
                         FluidTankProperties.Profile profile) {
        PollutionData pollution = PollutionData.get(level);
        if (selection == FluidIdentifierItem.Selection.FLUE) {
            pollution.increment(position, PollutionData.Type.SOOT, .005F * amount);
            return;
        }
        float poison = selection == FluidIdentifierItem.Selection.CARBONDIOXIDE
                ? .00002F : profile.spillPoisonPerMb();
        if (poison > 0F) pollution.increment(position, PollutionData.Type.POISON, poison * amount);
        if (selection == FluidIdentifierItem.Selection.GASOLINE_LEADED
                || selection == FluidIdentifierItem.Selection.COALGAS_LEADED) {
            pollution.increment(position, PollutionData.Type.HEAVYMETAL, .00005F * amount);
        }
    }

    private void spawnOutletParticles(ServerLevel level, BlockPos core, Direction facing,
                                      FluidTankProperties.Profile profile) {
        Vec3 outlet = Vec3.atCenterOf(core).add(
                facing.getStepX() * -2.5D, 0D, facing.getStepZ() * -2.5D);
        int color = selection.color();
        Vector3f rgb = new Vector3f((color >> 16 & 255) / 255F,
                (color >> 8 & 255) / 255F, (color & 255) / 255F);
        if (profile.gaseous()) {
            level.sendParticles(new DustParticleOptions(rgb, 2F), outlet.x, outlet.y, outlet.z,
                    6, .35D, .75D, .35D, .03D);
        } else {
            level.sendParticles(new DustParticleOptions(rgb, 1F), outlet.x, outlet.y, outlet.z,
                    4, .2D, .08D, .2D, .02D);
        }
    }

    private void placeOilSpill(ServerLevel level, BlockPos core, Direction facing) {
        Vec3 start = Vec3.atCenterOf(core).add(
                facing.getStepX() * -3D, 0D, facing.getStepZ() * -3D);
        Vec3 end = start.add(level.random.nextGaussian() * 5D, -25D,
                level.random.nextGaussian() * 5D);
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, (Entity) null));
        if (hit.getType() != HitResult.Type.BLOCK || hit.getDirection() != Direction.UP) return;
        BlockPos spill = hit.getBlockPos().above();
        BlockState current = level.getBlockState(spill);
        BlockState oil = ModBlocks.OIL_SPILL.get().defaultBlockState();
        if (current.getFluidState().isEmpty() && current.canBeReplaced()
                && oil.canSurvive(level, spill)) {
            level.setBlock(spill, oil, Block.UPDATE_ALL);
        }
    }

    private void syncIfChanged(ServerLevel level, BlockPos position, BlockState state) {
        if (tank.getFluidAmount() == lastAmount && selection == lastSelection) return;
        lastAmount = tank.getFluidAmount();
        lastSelection = selection;
        setChanged();
        level.sendBlockUpdated(position, state, state, Block.UPDATE_CLIENTS);
    }

    public void selectFluid(@Nullable FluidIdentifierItem.Selection next) {
        if (next == null) next = FluidIdentifierItem.Selection.NONE;
        if (next == selection) return;
        selection = next;
        tank.setFluid(FluidStack.EMPTY);
        changed();
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    public IFluidHandler fluidHandler(@Nullable Direction side) {
        return DrainagePipeBlock.canConnectAt(getBlockState(), side) ? handler : null;
    }

    public FluidIdentifierItem.Selection selection() { return selection; }
    public int amount() { return tank.getFluidAmount(); }
    public FluidTank tank() { return tank; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("selectedFluid", selection.id());
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        selection = FluidIdentifierItem.Selection.byId(tag.getString("selectedFluid"));
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
        conformTank();
    }

    private void conformTank() {
        if (!tank.isEmpty() && !selection.accepts(tank.getFluid().getFluid())) {
            tank.setFluid(FluidStack.EMPTY);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("selectedFluid", selection.id());
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        selection = FluidIdentifierItem.Selection.byId(tag.getString("selectedFluid"));
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
        conformTank();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
