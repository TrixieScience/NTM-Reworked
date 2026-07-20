package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.CombustionEngineBlock;
import com.hbm.ntm.energy.HeProvider;
import com.hbm.ntm.inventory.CombustionEngineMenu;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.HeBatteryItem;
import com.hbm.ntm.item.InfiniteFluidBarrelItem;
import com.hbm.ntm.item.PistonSetItem;
import com.hbm.ntm.item.SourceFluidContainerItem;
import com.hbm.ntm.item.UniversalFluidTankItem;
import com.hbm.ntm.pollution.PollutionData;
import com.hbm.ntm.recipe.CombustionEngineFuels;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.ror.RorFunctionException;
import com.hbm.ntm.ror.RorInteractive;
import com.hbm.ntm.ror.RorValueProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/** Industrial Combustion Engine, including the source's tenths-of-a-millibucket bookkeeping. */
public final class CombustionEngineBlockEntity extends BlockEntity
        implements WorldlyContainer, MenuProvider, HeProvider, RorValueProvider, RorInteractive {
    public static final int FUEL_INPUT = 0;
    public static final int CONTAINER_OUTPUT = 1;
    public static final int PISTON_SET = 2;
    public static final int BATTERY = 3;
    public static final int FLUID_IDENTIFIER = 4;
    public static final int SLOT_COUNT = 5;
    public static final int FUEL_CAPACITY = 24_000;
    public static final int SMOKE_CAPACITY = 50;
    public static final long MAX_POWER = 2_500_000L;
    public static final int MAX_THROTTLE = 30;
    private static final int[] AUTOMATION_SLOTS = {FUEL_INPUT, CONTAINER_OUTPUT, BATTERY};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private FluidIdentifierItem.Selection selectedFluid = FluidIdentifierItem.Selection.DIESEL;
    private final FluidTank fuel = new FluidTank(FUEL_CAPACITY,
            stack -> selectedFluid.accepts(stack.getFluid())) {
        @Override protected void onContentsChanged() { CombustionEngineBlockEntity.this.setChanged(); }
    };
    private int smoke;
    private int tenth;
    private long power;
    private int throttle;
    private boolean on;
    private boolean active;
    private int playersUsing;
    private float doorAngle;
    private float previousDoorAngle;
    private Component customName;
    private long lastSnapshot = Long.MIN_VALUE;

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 2; }
        @Override public FluidStack getFluidInTank(int tank) {
            if (tank == 0) return fuel.getFluid().copy();
            return tank == 1 && smoke > 0 ? new FluidStack(ModFluids.SMOKE.get(), smoke) : FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int tank) { return tank == 0 ? FUEL_CAPACITY : tank == 1 ? SMOKE_CAPACITY : 0; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && selectedFluid.accepts(stack.getFluid());
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            return selectedFluid.accepts(resource.getFluid()) ? fuel.fill(resource, action) : 0;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            return resource.is(ModFluids.SMOKE.get()) ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }
        @Override public FluidStack drain(int amount, FluidAction action) {
            int drained = Math.min(Math.max(amount, 0), smoke);
            if (drained <= 0) return FluidStack.EMPTY;
            FluidStack result = new FluidStack(ModFluids.SMOKE.get(), drained);
            if (action.execute()) { smoke -= drained; setChanged(); }
            return result;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> (int) power;
                case 1 -> (int) (power >>> 32);
                case 2 -> fuel.getFluidAmount();
                case 3 -> selectedFluid.ordinal();
                case 4 -> throttle;
                case 5 -> on ? 1 : 0;
                case 6 -> active ? 1 : 0;
                case 7 -> pistonType() == null ? -1 : pistonType().ordinal();
                case 8 -> playersUsing;
                case 9 -> smoke;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> power = power & 0xFFFFFFFF00000000L | value & 0xFFFFFFFFL;
                case 1 -> power = power & 0xFFFFFFFFL | (long) value << 32;
                case 3 -> selectedFluid = selection(value);
                case 4 -> throttle = Math.clamp(value, 0, MAX_THROTTLE);
                case 5 -> on = value != 0;
                case 6 -> active = value != 0;
                case 8 -> playersUsing = Math.max(value, 0);
                case 9 -> smoke = Math.clamp(value, 0, SMOKE_CAPACITY);
                default -> { }
            }
        }
        @Override public int getCount() { return 10; }
    };

    public CombustionEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_COMBUSTION_ENGINE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CombustionEngineBlockEntity engine) {
        if (level.isClientSide) engine.clientTick();
        else engine.serverTick((ServerLevel) level, pos, state);
    }

    private void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        refreshSelectedFluid();
        loadFuelContainer();
        active = false;
        CombustionEngineFuels.Fuel profile = CombustionEngineFuels.fuel(selectedFluid);
        PistonSetItem.Type piston = pistonType();
        int fill = fuel.getFluidAmount() * 10 + tenth;
        if (on && throttle > 0 && piston != null && fill > 0 && profile.accepted()) {
            double efficiency = piston.efficiency(profile.grade());
            if (efficiency > 0D) {
                int burned = Math.min(fill, throttle * 2);
                power += (long) (burned * (profile.combustionEnergyPerBucket() / 10_000D) * efficiency);
                fill -= burned;
                active = burned > 0;
                if (active && profile.polluting() && level.getGameTime() % 5L == 0L) emitSmoke(level, pos, burned);
                fuel.setFluid(fill / 10 <= 0 ? FluidStack.EMPTY
                        : new FluidStack(selectedFluid.fluid(), fill / 10));
                tenth = fill % 10;
            }
        }
        chargeBattery();
        Direction facing = state.getValue(CombustionEngineBlock.FACING);
        for (CombustionEngineBlock.Connection connection : CombustionEngineBlock.connections(pos, facing)) {
            tryProvide(level, connection.target(), connection.outward());
            exchangeFluids(level, connection);
        }
        power = Math.min(power, MAX_POWER);
        sync(level, pos, state);
        setChanged();
    }

    private void clientTick() {
        previousDoorAngle = doorAngle;
        float speed = doorAngle / 10F + 3F;
        doorAngle = Math.clamp(doorAngle + (playersUsing > 0 ? speed : -speed), 0F, 135F);
    }

    private void refreshSelectedFluid() {
        ItemStack identifier = items.get(FLUID_IDENTIFIER);
        if (!(identifier.getItem() instanceof FluidIdentifierItem)) return;
        FluidIdentifierItem.Selection selection = FluidIdentifierItem.primary(identifier);
        if (selection == FluidIdentifierItem.Selection.NONE || selection == selectedFluid) return;
        selectedFluid = selection;
        fuel.setFluid(FluidStack.EMPTY);
        tenth = 0;
        setChanged();
    }

    private void loadFuelContainer() {
        ItemStack input = items.get(FUEL_INPUT);
        if (input.isEmpty()) return;
        if (InfiniteFluidBarrelItem.is(input)) {
            if (InfiniteFluidBarrelItem.fillTank(fuel, selectedFluid.fluid()) > 0) setChanged();
            return;
        }
        Fluid fluid;
        ItemStack remainder;
        if (input.getItem() instanceof UniversalFluidTankItem) {
            fluid = UniversalFluidTankItem.fluid(input).fluid();
            remainder = new ItemStack(ModItems.FLUID_TANK_EMPTY.get());
        } else if (input.is(ModItems.CANISTER_FULL.get())) {
            fluid = SourceFluidContainerItem.fluid(input).fluid();
            remainder = new ItemStack(ModItems.CANISTER_EMPTY.get());
        } else if (input.is(ModItems.GAS_FULL.get())) {
            fluid = SourceFluidContainerItem.fluid(input).fluid();
            remainder = new ItemStack(ModItems.GAS_EMPTY.get());
        } else return;
        if (!selectedFluid.accepts(fluid) || !canMerge(items.get(CONTAINER_OUTPUT), remainder)) return;
        FluidStack load = new FluidStack(fluid, 1_000);
        if (fuel.fill(load, IFluidHandler.FluidAction.SIMULATE) != 1_000) return;
        fuel.fill(load, IFluidHandler.FluidAction.EXECUTE);
        input.shrink(1);
        if (input.isEmpty()) items.set(FUEL_INPUT, ItemStack.EMPTY);
        mergeOutput(CONTAINER_OUTPUT, remainder);
        setChanged();
    }

    private void chargeBattery() {
        ItemStack stack = items.get(BATTERY);
        if (!(stack.getItem() instanceof HeBatteryItem battery)) return;
        long amount = Math.min(Math.min(power, battery.getChargeRate(stack)),
                Math.max(battery.getMaxCharge(stack) - battery.getCharge(stack), 0L));
        if (amount <= 0L) return;
        battery.charge(stack, amount);
        power -= amount;
    }

    private void exchangeFluids(ServerLevel level, CombustionEngineBlock.Connection connection) {
        IFluidHandler neighbor = level.getCapability(Capabilities.FluidHandler.BLOCK,
                connection.target(), connection.outward().getOpposite());
        if (neighbor == null) return;
        pullFuel(neighbor);
        if (smoke <= 0) return;
        int accepted = neighbor.fill(new FluidStack(ModFluids.SMOKE.get(), smoke),
                IFluidHandler.FluidAction.EXECUTE);
        smoke -= Math.min(Math.max(accepted, 0), smoke);
    }

    private void pullFuel(IFluidHandler source) {
        if (selectedFluid == FluidIdentifierItem.Selection.NONE || fuel.getSpace() <= 0) return;
        FluidStack wanted = new FluidStack(selectedFluid.fluid(), fuel.getSpace());
        FluidStack available = source.drain(wanted, IFluidHandler.FluidAction.SIMULATE);
        int accepted = fuel.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return;
        FluidStack drained = source.drain(new FluidStack(selectedFluid.fluid(), accepted),
                IFluidHandler.FluidAction.EXECUTE);
        fuel.fill(drained, IFluidHandler.FluidAction.EXECUTE);
    }

    private void emitSmoke(ServerLevel level, BlockPos pos, int burnedTenths) {
        smoke += Math.max(1, burnedTenths / 10);
        if (smoke <= SMOKE_CAPACITY) return;
        int overflow = smoke - SMOKE_CAPACITY;
        smoke = SMOKE_CAPACITY;
        PollutionData.get(level).increment(pos, PollutionData.Type.SOOT, overflow * 0.5F);
    }

    private void sync(ServerLevel level, BlockPos pos, BlockState state) {
        long snapshot = power ^ ((long) fuel.getFluidAmount() << 8) ^ ((long) tenth << 24)
                ^ ((long) throttle << 28) ^ ((long) selectedFluid.ordinal() << 36)
                ^ ((long) playersUsing << 48) ^ (on ? 1L : 0L) ^ (active ? 2L : 0L);
        if (snapshot != lastSnapshot || level.getGameTime() % 20L == 0L) {
            lastSnapshot = snapshot;
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public void setControl(Control control, int value) {
        if (control == Control.TOGGLE) on = !on;
        else throttle = Math.clamp(value, 0, MAX_THROTTLE);
        setChanged();
    }

    @Nullable public PistonSetItem.Type pistonType() {
        return items.get(PISTON_SET).getItem() instanceof PistonSetItem piston ? piston.type() : null;
    }
    public FluidIdentifierItem.Selection selectedFluid() { return selectedFluid; }
    public int fuelAmount() { return fuel.getFluidAmount(); }
    public int throttle() { return throttle; }
    public boolean on() { return on; }
    public boolean active() { return active; }
    public float doorAngle(float partialTick) { return previousDoorAngle + (doorAngle - previousDoorAngle) * partialTick; }
    public int smokeAmount() { return smoke; }
    public ContainerData dataAccess() { return data; }
    public IFluidHandler fluidHandler() { return fluidHandler; }

    public void setCustomName(Component name) { customName = name; setChanged(); }
    @Override public Component getDisplayName() {
        return customName != null ? customName : Component.translatable("container.combustionEngine");
    }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CombustionEngineMenu(id, inventory, this, data);
    }

    @Override public void startOpen(Player player) { if (!player.isSpectator()) { playersUsing++; setChanged(); } }
    @Override public void stopOpen(Player player) { if (!player.isSpectator()) { playersUsing = Math.max(0, playersUsing - 1); setChanged(); } }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putLong("power", power);
        tag.putString("selectedFluid", selectedFluid.id());
        tag.put("fuel", fuel.writeToNBT(registries, new CompoundTag()));
        tag.putInt("smoke", smoke);
        tag.putInt("tenth", tenth);
        tag.putInt("setting", throttle);
        tag.putBoolean("isOn", on);
        if (customName != null) tag.putString("name", Component.Serializer.toJson(customName, registries));
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        power = Math.clamp(tag.getLong("power"), 0L, MAX_POWER);
        selectedFluid = tag.contains("selectedFluid") ? FluidIdentifierItem.Selection.byId(tag.getString("selectedFluid"))
                : FluidIdentifierItem.Selection.DIESEL;
        if (tag.contains("fuel")) fuel.readFromNBT(registries, tag.getCompound("fuel"));
        if (!fuel.isEmpty() && !selectedFluid.accepts(fuel.getFluid().getFluid())) fuel.setFluid(FluidStack.EMPTY);
        smoke = Math.clamp(tag.getInt("smoke"), 0, SMOKE_CAPACITY);
        tenth = Math.clamp(tag.getInt("tenth"), 0, 9);
        throttle = Math.clamp(tag.getInt("setting"), 0, MAX_THROTTLE);
        on = tag.getBoolean("isOn");
        customName = tag.contains("name") ? Component.Serializer.fromJson(tag.getString("name"), registries) : null;
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("power", power);
        tag.putString("selectedFluid", selectedFluid.id());
        tag.put("fuel", fuel.writeToNBT(registries, new CompoundTag()));
        tag.putInt("smoke", smoke);
        tag.putInt("setting", throttle);
        tag.putBoolean("isOn", on);
        tag.putBoolean("active", active);
        tag.putInt("playersUsing", playersUsing);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        power = Math.clamp(tag.getLong("power"), 0L, MAX_POWER);
        selectedFluid = FluidIdentifierItem.Selection.byId(tag.getString("selectedFluid"));
        if (tag.contains("fuel")) fuel.readFromNBT(registries, tag.getCompound("fuel"));
        smoke = Math.clamp(tag.getInt("smoke"), 0, SMOKE_CAPACITY);
        throttle = Math.clamp(tag.getInt("setting"), 0, MAX_THROTTLE);
        on = tag.getBoolean("isOn");
        active = tag.getBoolean("active");
        playersUsing = Math.max(tag.getInt("playersUsing"), 0);
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) < 625D;
    }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public int[] getSlotsForFace(Direction side) { return AUTOMATION_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) { return canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (slot == CONTAINER_OUTPUT) return true;
        return slot == BATTERY && stack.getItem() instanceof HeBatteryItem battery
                && battery.getCharge(stack) >= battery.getMaxCharge(stack);
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == FUEL_INPUT) return containerMatchesSelection(stack);
        if (slot == PISTON_SET) return stack.getItem() instanceof PistonSetItem;
        if (slot == BATTERY) return stack.getItem() instanceof HeBatteryItem;
        return slot == FLUID_IDENTIFIER && stack.getItem() instanceof FluidIdentifierItem;
    }

    private boolean containerMatchesSelection(ItemStack stack) {
        if (InfiniteFluidBarrelItem.is(stack)) return selectedFluid != FluidIdentifierItem.Selection.NONE;
        Fluid fluid;
        if (stack.getItem() instanceof UniversalFluidTankItem) fluid = UniversalFluidTankItem.fluid(stack).fluid();
        else if (stack.is(ModItems.CANISTER_FULL.get()) || stack.is(ModItems.GAS_FULL.get())) fluid = SourceFluidContainerItem.fluid(stack).fluid();
        else return false;
        return selectedFluid.accepts(fluid);
    }

    private static boolean canMerge(ItemStack target, ItemStack addition) {
        return !addition.isEmpty() && (target.isEmpty() || ItemStack.isSameItemSameComponents(target, addition)
                && target.getCount() + addition.getCount() <= target.getMaxStackSize());
    }
    private void mergeOutput(int slot, ItemStack addition) {
        if (items.get(slot).isEmpty()) items.set(slot, addition.copy()); else items.get(slot).grow(addition.getCount());
    }
    private static FluidIdentifierItem.Selection selection(int ordinal) {
        FluidIdentifierItem.Selection[] values = FluidIdentifierItem.Selection.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FluidIdentifierItem.Selection.DIESEL;
    }

    @Override public long getPower() { return power; }
    @Override public void setPower(long value) { power = Math.clamp(value, 0L, MAX_POWER); setChanged(); }
    @Override public long getMaxPower() { return MAX_POWER; }
    @Override public boolean isHeLoaded() { return hasLevel() && !isRemoved(); }
    @Override public boolean canConnect(Direction side) { return false; }

    @Override public String[] rorInfo() {
        return new String[]{VALUE_PREFIX + "state", VALUE_PREFIX + "throttle", VALUE_PREFIX + "power",
                VALUE_PREFIX + "fuel", VALUE_PREFIX + "efficiency", FUNCTION_PREFIX + "setstate!state",
                FUNCTION_PREFIX + "setthrottle!throttle"};
    }
    @Override public String provideRorValue(String name) {
        if ((VALUE_PREFIX + "state").equals(name)) return on ? "1" : "0";
        if ((VALUE_PREFIX + "throttle").equals(name)) return Integer.toString(throttle);
        if ((VALUE_PREFIX + "power").equals(name)) return Long.toString(power);
        if ((VALUE_PREFIX + "fuel").equals(name)) return Integer.toString(fuel.getFluidAmount());
        if ((VALUE_PREFIX + "efficiency").equals(name)) return Integer.toString((int) Math.round(efficiency() * 100D));
        return null;
    }
    @Override public void runRorFunction(String name, String[] parameters) throws RorFunctionException {
        if ((FUNCTION_PREFIX + "setstate").equals(name) && parameters.length > 0) {
            on = RorInteractive.integer(parameters[0], 0, 1) == 1;
            setChanged();
        } else if ((FUNCTION_PREFIX + "setthrottle").equals(name) && parameters.length > 0) {
            throttle = RorInteractive.integer(parameters[0], 0, MAX_THROTTLE);
            setChanged();
        }
    }

    public double efficiency() {
        PistonSetItem.Type piston = pistonType();
        return piston == null ? 0D : piston.efficiency(CombustionEngineFuels.fuel(selectedFluid).grade());
    }

    public enum Control { TOGGLE, THROTTLE }

    public void setFuelForTest(FluidIdentifierItem.Selection selection, int amount, int tenths) {
        selectedFluid = selection;
        fuel.setFluid(amount <= 0 ? FluidStack.EMPTY : new FluidStack(selection.fluid(), Math.min(amount, FUEL_CAPACITY)));
        tenth = Math.clamp(tenths, 0, 9);
    }
    public void runForTest(ServerLevel level) { serverTick(level, worldPosition, getBlockState()); }
}
