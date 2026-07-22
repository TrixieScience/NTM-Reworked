package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ThermalMultiblockBlock;
import com.hbm.ntm.inventory.FireboxMenu;
import com.hbm.ntm.pollution.PollutionData;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.thermal.FireboxFuel;
import com.hbm.ntm.thermal.HeatSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public final class FireboxBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, HeatSource,
        MufflableMachine {
    public static final int SLOT_COUNT = 2;
    public static final int MAX_HEAT = 100_000;
    public static final int BASE_HEAT = 100;
    public static final int OVEN_MAX_HEAT = 500_000;
    public static final int OVEN_BASE_HEAT = 500;
    public static final double OVEN_TIME_MULTIPLIER = 0.125D;
    public static final double OVEN_HEAT_EFFICIENCY = 0.5D;
    public static final int SMOKE_CAPACITY = 50;
    private static final int[] SLOTS = {0, 1};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> maxBurnTime;
                case 1 -> burnTime;
                case 2 -> burnHeat;
                case 3 -> heatEnergy;
                case 4 -> wasOn ? 1 : 0;
                case 5 -> isHeatingOven() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> maxBurnTime = value;
                case 1 -> burnTime = value;
                case 2 -> burnHeat = value;
                case 3 -> heatEnergy = value;
                case 4 -> wasOn = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };
    private final IFluidHandler smokeHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 && smoke > 0 ? new FluidStack(ModFluids.SMOKE.get(), smoke) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? SMOKE_CAPACITY : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !resource.is(ModFluids.SMOKE.get())) {
                return FluidStack.EMPTY;
            }
            return drain(resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int drained = Math.min(Math.max(maxDrain, 0), smoke);
            if (drained <= 0) {
                return FluidStack.EMPTY;
            }
            FluidStack result = new FluidStack(ModFluids.SMOKE.get(), drained);
            if (action.execute()) {
                smoke -= drained;
                setChanged();
            }
            return result;
        }
    };

    private Component customName;
    private int maxBurnTime;
    private int burnTime;
    private int burnHeat;
    private int heatEnergy;
    private int smoke;
    private boolean wasOn;
    private boolean muffled;
    private int playersUsing;
    private float doorAngle;
    private float previousDoorAngle;

    public FireboxBlockEntity(BlockPos position, BlockState state) {
        super(state.is(com.hbm.ntm.registry.ModBlocks.HEATER_OVEN.get())
                ? ModBlockEntities.HEATER_OVEN.get() : ModBlockEntities.HEATER_FIREBOX.get(), position, state);
    }

    public static void tick(Level level, BlockPos position, BlockState state, FireboxBlockEntity firebox) {
        if (level.isClientSide) {
            firebox.clientTick(level, position, state);
        } else {
            firebox.serverTick((ServerLevel) level, position, state);
        }
    }

    private void serverTick(ServerLevel level, BlockPos position, BlockState state) {
        if (isHeatingOven()) {
            pullHeat(level, position);
        }
        sendSmoke(level, position);
        wasOn = false;

        if (burnTime <= 0) {
            for (int slot = 0; slot < SLOT_COUNT; slot++) {
                ItemStack fuelStack = items.get(slot);
                int baseTime = FireboxFuel.burnTime(fuelStack);
                if (baseTime <= 0) {
                    continue;
                }

                if (level.getBlockEntity(position.below()) instanceof AshpitBlockEntity ashpit) {
                    ashpit.addAsh(FireboxFuel.ashType(fuelStack), baseTime);
                }

                maxBurnTime = burnTime = (int) (baseTime * burnTimeMultiplier());
                burnHeat = FireboxFuel.burnHeat(fuelStack, baseHeat());
                consumeFuel(slot, fuelStack);
                wasOn = true;
                break;
            }
        } else {
            if (heatEnergy < maxHeat()) {
                burnTime--;
                if (level.getGameTime() % 20L == 0L) {
                    polluteSoot(level, position, 3.0F / 25.0F);
                }
            }
            wasOn = true;
            if (level.random.nextInt(15) == 0 && !muffled) {
                level.playSound(null, position, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                        1.0F, 0.5F + level.random.nextFloat() * 0.5F);
            }
        }

        if (wasOn) {
            heatEnergy = Math.min(heatEnergy + burnHeat, maxHeat());
        } else {
            heatEnergy = Math.max(heatEnergy - Math.max(heatEnergy / 1000, 1), 0);
            burnHeat = 0;
        }

        setChanged();
        level.sendBlockUpdated(position, state, state, Block.UPDATE_CLIENTS);
    }

    private void pullHeat(ServerLevel level, BlockPos position) {
        if (!(level.getBlockEntity(position.below()) instanceof HeatSource source)) {
            return;
        }
        int toPull = Math.max(Math.min(source.getHeatStored(), maxHeat() - heatEnergy), 0);
        heatEnergy += (int) (toPull * OVEN_HEAT_EFFICIENCY);
        source.useUpHeat(toPull);
    }

    private void sendSmoke(ServerLevel level, BlockPos position) {
        if (smoke <= 0) {
            return;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction perpendicular = direction.getClockWise();
            for (int offset = -1; offset <= 1 && smoke > 0; offset++) {
                BlockPos target = position.relative(direction, 2).relative(perpendicular, offset);
                IFluidHandler handler = level.getCapability(
                        Capabilities.FluidHandler.BLOCK, target, direction.getOpposite());
                if (handler == null) {
                    continue;
                }
                FluidStack available = new FluidStack(ModFluids.SMOKE.get(), smoke);
                int accepted = handler.fill(available, IFluidHandler.FluidAction.EXECUTE);
                smoke -= Math.min(Math.max(accepted, 0), smoke);
            }
        }
    }

    private void polluteSoot(ServerLevel level, BlockPos position, float amount) {
        int fluidAmount = (int) Math.ceil(amount * 100.0F);
        smoke += fluidAmount;
        if (smoke > SMOKE_CAPACITY) {
            int overflow = smoke - SMOKE_CAPACITY;
            smoke = SMOKE_CAPACITY;
            PollutionData.get(level).increment(position, PollutionData.Type.SOOT, overflow / 100.0F);
            if (level.random.nextInt(3) == 0) {
                level.playSound(null, position, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 1.5F);
            }
        }
    }

    private void consumeFuel(int slot, ItemStack fuelStack) {
        ItemStack remainder = fuelStack.getCraftingRemainingItem();
        fuelStack.shrink(1);
        if (fuelStack.isEmpty()) {
            items.set(slot, remainder);
        }
    }

    private void clientTick(Level level, BlockPos position, BlockState state) {
        previousDoorAngle = doorAngle;
        float swingSpeed = doorAngle / 10.0F + 3.0F;
        doorAngle += playersUsing > 0 ? swingSpeed : -swingSpeed;
        doorAngle = Mth.clamp(doorAngle, 0.0F, 135.0F);

        if (wasOn && level.getGameTime() % 5L == 0L) {
            Direction direction = state.getValue(ThermalMultiblockBlock.FACING);
            double x = position.getX() + 0.5D + direction.getStepX();
            double y = position.getY() + 0.25D;
            double z = position.getZ() + 0.5D + direction.getStepZ();
            level.addParticle(ParticleTypes.FLAME,
                    x + level.random.nextDouble() * 0.5D - 0.25D,
                    y + level.random.nextDouble() * 0.25D,
                    z + level.random.nextDouble() * 0.5D - 0.25D,
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Nullable
    public IFluidHandler smokeHandler(@Nullable Direction side) {
        return side != null && side != Direction.DOWN ? smokeHandler : null;
    }

    @Override
    public int getHeatStored() {
        return heatEnergy;
    }

    @Override
    public void useUpHeat(int heat) {
        heatEnergy = Math.max(0, heatEnergy - heat);
        setChanged();
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return customName != null ? customName : Component.translatable(
                isHeatingOven() ? "container.heaterOven" : "container.heaterFirebox");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FireboxMenu(containerId, inventory, this, data);
    }

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            playersUsing++;
            syncNow();
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            playersUsing--;
            syncNow();
        }
    }

    private void syncNow() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("maxBurnTime", maxBurnTime);
        tag.putInt("burnTime", burnTime);
        tag.putInt("burnHeat", burnHeat);
        tag.putInt("heatEnergy", heatEnergy);
        tag.putInt("smoke0", smoke);
        tag.putBoolean("muffled", muffled);
        if (customName != null) tag.putString("name", Component.Serializer.toJson(customName, registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        maxBurnTime = tag.getInt("maxBurnTime");
        burnTime = tag.getInt("burnTime");
        burnHeat = tag.getInt("burnHeat");
        heatEnergy = tag.getInt("heatEnergy");
        smoke = tag.getInt("smoke0");
        muffled = tag.getBoolean("muffled");
        customName = tag.contains("name") ? Component.Serializer.fromJson(tag.getString("name"), registries) : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("maxBurnTime", maxBurnTime);
        tag.putInt("burnTime", burnTime);
        tag.putInt("burnHeat", burnHeat);
        tag.putInt("heatEnergy", heatEnergy);
        tag.putInt("playersUsing", playersUsing);
        tag.putBoolean("wasOn", wasOn);
        tag.putBoolean("muffled", muffled);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        maxBurnTime = tag.getInt("maxBurnTime");
        burnTime = tag.getInt("burnTime");
        burnHeat = tag.getInt("burnHeat");
        heatEnergy = tag.getInt("heatEnergy");
        playersUsing = tag.getInt("playersUsing");
        wasOn = tag.getBoolean("wasOn");
        muffled = tag.getBoolean("muffled");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) setChanged(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize()); setChanged(); }
    @Override public boolean stillValid(Player player) { return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getCenter()) <= 128.0D; }
    @Override public void clearContent() { items.clear(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return FireboxFuel.burnTime(stack) > 0; }
    @Override public int[] getSlotsForFace(Direction direction) { return SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) { return canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }

    public int maxBurnTime() { return maxBurnTime; }
    public int burnTime() { return burnTime; }
    public int burnHeat() { return burnHeat; }
    public int heatEnergy() { return heatEnergy; }
    public int smokeStored() { return smoke; }
    public boolean wasOn() { return wasOn; }

    @Override
    public boolean installMuffler() {
        if (muffled) return false;
        muffled = true;
        setChanged();
        syncNow();
        return true;
    }

    @Override
    public boolean isMuffled() {
        return muffled;
    }

    public boolean isHeatingOven() {
        return getBlockState().is(com.hbm.ntm.registry.ModBlocks.HEATER_OVEN.get());
    }
    public int maxHeat() { return isHeatingOven() ? OVEN_MAX_HEAT : MAX_HEAT; }
    private int baseHeat() { return isHeatingOven() ? OVEN_BASE_HEAT : BASE_HEAT; }
    private double burnTimeMultiplier() { return isHeatingOven() ? OVEN_TIME_MULTIPLIER : 1.0D; }
    public float doorAngle() { return doorAngle; }
    public float previousDoorAngle() { return previousDoorAngle; }
    public AABB renderBounds() { return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 1, worldPosition.getZ() + 2); }
}
