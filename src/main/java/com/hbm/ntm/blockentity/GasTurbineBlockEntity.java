package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.GasTurbineBlock;
import com.hbm.ntm.energy.HeProvider;
import com.hbm.ntm.inventory.GasTurbineMenu;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.HeBatteryItem;
import com.hbm.ntm.pollution.PollutionData;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModSounds;
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
import net.minecraft.sounds.SoundSource;
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

/** The old gas turbine's 580-tick startup ritual, four tanks and stubborn flywheel. */
public final class GasTurbineBlockEntity extends BlockEntity
        implements WorldlyContainer, MenuProvider, HeProvider, RorValueProvider, RorInteractive {
    public static final int BATTERY = 0;
    public static final int FLUID_IDENTIFIER = 1;
    public static final int SLOT_COUNT = 2;
    public static final int FUEL_CAPACITY = 100_000;
    public static final int LUBRICANT_CAPACITY = 16_000;
    public static final int WATER_CAPACITY = 16_000;
    public static final int STEAM_CAPACITY = 160_000;
    public static final long MAX_POWER = 1_000_000L;
    public static final int MAX_SLIDER = 60;
    public static final int MAX_RPM = 100;
    public static final int MAX_TEMPERATURE = 800;

    private static final int RPM_IDLE = 10;
    private static final int TEMP_IDLE = 300;
    private static final double NATURAL_GAS_MAX_CONSUMPTION = 50D;
    private static final long NATURAL_GAS_COMBUSTION_ENERGY = 75_000L;
    private static final int[] AUTOMATION_SLOTS = {BATTERY, FLUID_IDENTIFIER};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private FluidIdentifierItem.Selection selectedFuel = FluidIdentifierItem.Selection.GAS;
    private final FluidTank fuel = tank(FUEL_CAPACITY, ModFluids.GAS.get());
    private final FluidTank lubricant = tank(LUBRICANT_CAPACITY, ModFluids.LUBRICANT.get());
    private final FluidTank water = tank(WATER_CAPACITY, net.minecraft.world.level.material.Fluids.WATER);
    private final FluidTank steam = tank(STEAM_CAPACITY, ModFluids.HOTSTEAM.get());
    private final IFluidHandler fuelLubeHandler = new PortFluidHandler(GasTurbineBlock.Port.FUEL_LUBE);
    private final IFluidHandler waterHandler = new PortFluidHandler(GasTurbineBlock.Port.WATER);
    private final IFluidHandler steamHandler = new PortFluidHandler(GasTurbineBlock.Port.STEAM);

    private long power;
    private long displayPower;
    private int rpm;
    private int temperature;
    private int slider;
    private int instantPowerOutput;
    private int counter;
    private int state;
    private boolean autoMode;
    private double fuelToConsume;
    private double waterToBoil;
    private int rpmLast;
    private int temperatureLast;
    private Component customName;

    private long lastSnapshot = Long.MIN_VALUE;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> (int) displayPower;
                case 1 -> (int) (displayPower >>> 32);
                case 2 -> fuel.getFluidAmount();
                case 3 -> lubricant.getFluidAmount();
                case 4 -> water.getFluidAmount();
                case 5 -> steam.getFluidAmount();
                case 6 -> selectedFuel.ordinal();
                case 7 -> rpm;
                case 8 -> temperature;
                case 9 -> state;
                case 10 -> autoMode ? 1 : 0;
                case 11 -> slider;
                case 12 -> instantPowerOutput;
                case 13 -> counter;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> displayPower = displayPower & 0xFFFFFFFF00000000L | value & 0xFFFFFFFFL;
                case 1 -> displayPower = displayPower & 0xFFFFFFFFL | (long) value << 32;
                case 2 -> clientTank(fuel, ModFluids.GAS.get(), value);
                case 3 -> clientTank(lubricant, ModFluids.LUBRICANT.get(), value);
                case 4 -> clientTank(water, net.minecraft.world.level.material.Fluids.WATER, value);
                case 5 -> clientTank(steam, ModFluids.HOTSTEAM.get(), value);
                case 6 -> selectedFuel = selection(value);
                case 7 -> rpm = value;
                case 8 -> temperature = value;
                case 9 -> state = value;
                case 10 -> autoMode = value != 0;
                case 11 -> slider = value;
                case 12 -> instantPowerOutput = value;
                case 13 -> counter = value;
                default -> { }
            }
        }

        @Override public int getCount() { return 14; }
    };

    public GasTurbineBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.MACHINE_TURBINE_GAS.get(), position, state);
    }

    private FluidTank tank(int capacity, Fluid accepted) {
        return new FluidTank(capacity, stack -> stack.is(accepted)) {
            @Override protected void onContentsChanged() { GasTurbineBlockEntity.this.setChanged(); }
        };
    }

    public static void tick(Level level, BlockPos position, BlockState state,
                            GasTurbineBlockEntity turbine) {
        if (!level.isClientSide) turbine.serverTick((ServerLevel) level, position, state);
    }

    private void serverTick(ServerLevel level, BlockPos position, BlockState blockState) {
        waterToBoil = 0D;
        refreshFuelSelection();
        int throttle = throttle();
        if (autoMode) {
            int target = fuel.getFluidAmount() * 10 > fuel.getCapacity()
                    ? 60 - (int) (60L * power / MAX_POWER)
                    : (int) (fuel.getFluidAmount() * 0.0001D
                    * (60 - (int) (60L * power / MAX_POWER)));
            if (target > slider) slider++;
            else if (target < slider) slider--;
            throttle = throttle();
        }

        switch (state) {
            case 0 -> shutdown(level);
            case -1 -> {
                stopIfNotReady();
                startup(level);
            }
            case 1 -> {
                stopIfNotReady();
                run(level, throttle);
            }
            default -> state = 0;
        }

        displayPower = Math.min(power, MAX_POWER);
        chargeBattery();

        Direction facing = blockState.getValue(GasTurbineBlock.FACING);
        GasTurbineBlock.Connection powerConnection = GasTurbineBlock.powerConnection(position, facing);
        tryProvide(level, powerConnection.target(), powerConnection.outward());
        power = Math.min(power, MAX_POWER);

        for (GasTurbineBlock.Connection connection : GasTurbineBlock.fluidConnections(position, facing)) {
            exchangeFluids(level, connection);
        }

        sync(level, position, blockState);
        setChanged();
    }

    private void refreshFuelSelection() {
        ItemStack identifier = items.get(FLUID_IDENTIFIER);
        if (!(identifier.getItem() instanceof FluidIdentifierItem)) return;
        FluidIdentifierItem.Selection selection = FluidIdentifierItem.primary(identifier);
        if (selection == FluidIdentifierItem.Selection.GAS) selectedFuel = selection;
    }

    private void stopIfNotReady() {
        if (fuel.isEmpty() || lubricant.isEmpty() || selectedFuel != FluidIdentifierItem.Selection.GAS) state = 0;
    }

    private void startup(ServerLevel level) {
        counter++;
        if (counter <= 20) rpm = 5 * counter;
        else if (counter <= 40) rpm = 100 - 5 * (counter - 20);
        else if (counter > 50) {
            rpm = RPM_IDLE * (counter - 50) / 530;
            temperature = TEMP_IDLE * (counter - 50) / 530;
        }
        if (counter == 50) level.playSound(null, worldPosition.above(2), ModSounds.TURBINE_GAS_STARTUP.get(),
                SoundSource.BLOCKS, 1F, 1F);
        if (counter >= 580) {
            counter = 225;
            state = 1;
        }
    }

    private void shutdown(ServerLevel level) {
        autoMode = false;
        instantPowerOutput = 0;
        if (slider > 0) slider--;

        if (rpm <= RPM_IDLE && counter > 0) {
            if (counter == 225) {
                level.playSound(null, worldPosition.above(2), ModSounds.TURBINE_GAS_SHUTDOWN.get(),
                        SoundSource.BLOCKS, 1F, 1F);
                rpmLast = rpm;
                temperatureLast = temperature;
            }
            counter--;
            rpm = rpmLast * counter / 225;
            temperature = temperatureLast * counter / 225;
        } else if (rpm > 11) {
            counter = 42_069;
            rpm--;
        } else if (rpm == 11) {
            counter = 225;
            rpm--;
        }
    }

    private void run(ServerLevel level, int throttle) {
        int rpmTarget = (int) (throttle * 0.9D);
        if (rpmTarget > rpm - RPM_IDLE && level.getGameTime() % 5L == 0L) rpm++;
        else if (rpmTarget < rpm - RPM_IDLE && level.getGameTime() % 2L == 0L) rpm--;
        rpm = Math.clamp(rpm, 0, MAX_RPM);

        int maxTemp = burnTemperature();
        int temperatureTarget = throttle * 5 * (maxTemp - TEMP_IDLE) / 500;
        if (level.getGameTime() % 2L == 0L) {
            if (temperatureTarget > temperature - TEMP_IDLE) temperature++;
            else if (temperatureTarget < temperature - TEMP_IDLE) temperature--;
        }
        temperature = Math.clamp(temperature, 0, MAX_TEMPERATURE);

        if (level.getGameTime() % 20L == 0L) {
            PollutionData.get(level).increment(worldPosition, PollutionData.Type.SOOT, 0.12F);
        }
        makePower(level, throttle);
    }

    private int burnTemperature() {
        return (int) Math.floor(800D - Math.exp(-NATURAL_GAS_COMBUSTION_ENERGY / 100_000D) * 300D);
    }

    private void makePower(ServerLevel level, int throttle) {
        double consumption = NATURAL_GAS_MAX_CONSUMPTION * 0.05D
                + NATURAL_GAS_MAX_CONSUMPTION * throttle / 100D;
        fuelToConsume += consumption;
        int wholeFuel = (int) Math.floor(fuelToConsume);
        if (wholeFuel > 0) {
            int drained = fuel.drain(wholeFuel, IFluidHandler.FluidAction.EXECUTE).getAmount();
            fuelToConsume -= wholeFuel;
            if (drained < wholeFuel) state = 0;
        }
        if (level.getGameTime() % 10L == 0L) lubricant.drain(1, IFluidHandler.FluidAction.EXECUTE);

        int energyPerMb = (int) (NATURAL_GAS_COMBUSTION_ENERGY / 1_000L);
        int rpmEfficiency = rpm - RPM_IDLE;
        int target = (int) (NATURAL_GAS_MAX_CONSUMPTION * energyPerMb * rpmEfficiency / 90D);
        if (instantPowerOutput < target) {
            instantPowerOutput += (int) (level.random.nextDouble() * 0.005D
                    * NATURAL_GAS_MAX_CONSUMPTION * energyPerMb);
            if (instantPowerOutput > target) instantPowerOutput = target;
        } else if (instantPowerOutput > target) {
            instantPowerOutput -= (int) (level.random.nextDouble() * 0.011D
                    * NATURAL_GAS_MAX_CONSUMPTION * energyPerMb);
            if (instantPowerOutput < target) instantPowerOutput = target;
        }
        power += Math.max(instantPowerOutput, 0);

        waterToBoil = NATURAL_GAS_MAX_CONSUMPTION * energyPerMb
                * (temperature - TEMP_IDLE) / 220_000D;
        int cycles = Math.min((int) Math.floor(Math.max(waterToBoil, 0D)),
                Math.min(water.getFluidAmount(), steam.getSpace() / 10));
        if (cycles > 0) {
            water.drain(cycles, IFluidHandler.FluidAction.EXECUTE);
            steam.fill(new FluidStack(ModFluids.HOTSTEAM.get(), cycles * 10), IFluidHandler.FluidAction.EXECUTE);
        }
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

    private void exchangeFluids(ServerLevel level, GasTurbineBlock.Connection connection) {
        IFluidHandler neighbor = level.getCapability(Capabilities.FluidHandler.BLOCK,
                connection.target(), connection.outward().getOpposite());
        if (neighbor == null) return;
        switch (connection.portType()) {
            case FUEL_LUBE -> {
                pull(neighbor, fuel, ModFluids.GAS.get());
                pull(neighbor, lubricant, ModFluids.LUBRICANT.get());
            }
            case WATER -> pull(neighbor, water, net.minecraft.world.level.material.Fluids.WATER);
            case STEAM -> push(neighbor, steam);
            default -> { }
        }
    }

    private static void pull(IFluidHandler source, FluidTank target, Fluid fluid) {
        if (target.getSpace() <= 0) return;
        FluidStack available = source.drain(new FluidStack(fluid, target.getSpace()),
                IFluidHandler.FluidAction.SIMULATE);
        int accepted = target.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return;
        FluidStack drained = source.drain(new FluidStack(fluid, accepted), IFluidHandler.FluidAction.EXECUTE);
        target.fill(drained, IFluidHandler.FluidAction.EXECUTE);
    }

    private static void push(IFluidHandler target, FluidTank source) {
        if (source.isEmpty()) return;
        int accepted = target.fill(source.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
        if (accepted > 0) source.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
    }

    private void sync(ServerLevel level, BlockPos position, BlockState blockState) {
        long snapshot = power ^ ((long) fuel.getFluidAmount() << 1) ^ ((long) lubricant.getFluidAmount() << 18)
                ^ ((long) water.getFluidAmount() << 35) ^ ((long) steam.getFluidAmount() << 47)
                ^ ((long) rpm << 8) ^ ((long) temperature << 16) ^ ((long) state << 28)
                ^ ((long) slider << 32) ^ ((long) instantPowerOutput << 40) ^ (autoMode ? 1L : 0L);
        if (snapshot != lastSnapshot || level.getGameTime() % 20L == 0L) {
            lastSnapshot = snapshot;
            level.sendBlockUpdated(position, blockState, blockState, Block.UPDATE_CLIENTS);
        }
    }

    public void setControl(Control control, int value) {
        switch (control) {
            case STATE -> {
                if (value == 1 && state == 0 && counter == 0) state = -1;
                else if (value == 0 && state == 1) state = 0;
            }
            case AUTO -> {
                if (state == 1) autoMode = value != 0;
            }
            case THROTTLE -> {
                if (state == 1) {
                    slider = Math.clamp(value, 0, MAX_SLIDER);
                    autoMode = false;
                }
            }
        }
        setChanged();
    }

    public IFluidHandler fluidHandler(GasTurbineBlock.Port port) {
        return switch (port) {
            case FUEL_LUBE -> fuelLubeHandler;
            case WATER -> waterHandler;
            case STEAM -> steamHandler;
            default -> null;
        };
    }

    public int throttle() { return slider * 100 / MAX_SLIDER; }
    public int rpm() { return rpm; }
    public int temperature() { return temperature; }
    public int state() { return state; }
    public boolean autoMode() { return autoMode; }
    public int slider() { return slider; }
    public int instantPowerOutput() { return instantPowerOutput; }
    public int counter() { return counter; }
    public int fuelAmount() { return fuel.getFluidAmount(); }
    public int lubricantAmount() { return lubricant.getFluidAmount(); }
    public int waterAmount() { return water.getFluidAmount(); }
    public int steamAmount() { return steam.getFluidAmount(); }
    public double waterToBoil() { return waterToBoil; }
    public ContainerData dataAccess() { return data; }

    public void setCustomName(Component name) { customName = name; setChanged(); }
    @Override public Component getDisplayName() {
        return customName != null ? customName : Component.translatable("container.turbinegas");
    }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GasTurbineMenu(id, inventory, this, data);
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.put("gas", fuel.writeToNBT(registries, new CompoundTag()));
        tag.put("lube", lubricant.writeToNBT(registries, new CompoundTag()));
        tag.put("water", water.writeToNBT(registries, new CompoundTag()));
        tag.put("densesteam", steam.writeToNBT(registries, new CompoundTag()));
        tag.putLong("power", power);
        tag.putBoolean("automode", autoMode);
        tag.putDouble("fuelRemainder", fuelToConsume);
        if (state == 1) {
            tag.putInt("state", state);
            tag.putInt("rpm", rpm);
            tag.putInt("temperature", temperature);
            tag.putInt("slidPos", slider);
            tag.putInt("instPwr", instantPowerOutput);
            tag.putInt("counter", 225);
        } else {
            tag.putInt("state", 0);
            tag.putInt("rpm", 0);
            tag.putInt("temperature", 20);
            tag.putInt("slidPos", 0);
            tag.putInt("instPwr", 0);
            tag.putInt("counter", 0);
        }
        if (customName != null) tag.putString("name", Component.Serializer.toJson(customName, registries));
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        if (tag.contains("gas")) fuel.readFromNBT(registries, tag.getCompound("gas"));
        if (tag.contains("lube")) lubricant.readFromNBT(registries, tag.getCompound("lube"));
        if (tag.contains("water")) water.readFromNBT(registries, tag.getCompound("water"));
        if (tag.contains("densesteam")) steam.readFromNBT(registries, tag.getCompound("densesteam"));
        power = Math.clamp(tag.getLong("power"), 0L, MAX_POWER);
        displayPower = power;
        autoMode = tag.getBoolean("automode");
        fuelToConsume = Math.max(tag.getDouble("fuelRemainder"), 0D);
        state = Math.clamp(tag.getInt("state"), -1, 1);
        rpm = Math.clamp(tag.getInt("rpm"), 0, MAX_RPM);
        temperature = Math.clamp(tag.getInt("temperature"), 0, MAX_TEMPERATURE);
        slider = Math.clamp(tag.getInt("slidPos"), 0, MAX_SLIDER);
        instantPowerOutput = Math.max(tag.getInt("instPwr"), 0);
        counter = Math.max(tag.getInt("counter"), 0);
        customName = tag.contains("name") ? Component.Serializer.fromJson(tag.getString("name"), registries) : null;
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("power", displayPower);
        tag.putInt("fuel", fuel.getFluidAmount());
        tag.putInt("lube", lubricant.getFluidAmount());
        tag.putInt("water", water.getFluidAmount());
        tag.putInt("steam", steam.getFluidAmount());
        tag.putInt("rpm", rpm);
        tag.putInt("temperature", temperature);
        tag.putInt("state", state);
        tag.putBoolean("automode", autoMode);
        tag.putInt("slider", slider);
        tag.putInt("output", instantPowerOutput);
        tag.putInt("counter", counter);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        displayPower = tag.getLong("power");
        clientTank(fuel, ModFluids.GAS.get(), tag.getInt("fuel"));
        clientTank(lubricant, ModFluids.LUBRICANT.get(), tag.getInt("lube"));
        clientTank(water, net.minecraft.world.level.material.Fluids.WATER, tag.getInt("water"));
        clientTank(steam, ModFluids.HOTSTEAM.get(), tag.getInt("steam"));
        rpm = tag.getInt("rpm");
        temperature = tag.getInt("temperature");
        state = tag.getInt("state");
        autoMode = tag.getBoolean("automode");
        slider = tag.getInt("slider");
        instantPowerOutput = tag.getInt("output");
        counter = tag.getInt("counter");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 625D;
    }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public int[] getSlotsForFace(Direction side) { return AUTOMATION_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == BATTERY && stack.getItem() instanceof HeBatteryItem battery
                && battery.getCharge(stack) >= battery.getMaxCharge(stack);
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == BATTERY) return stack.getItem() instanceof HeBatteryItem;
        return slot == FLUID_IDENTIFIER && stack.getItem() instanceof FluidIdentifierItem
                && FluidIdentifierItem.primary(stack) == FluidIdentifierItem.Selection.GAS;
    }

    @Override public boolean canConnect(Direction side) { return false; }
    @Override public long getPower() { return power; }
    @Override public void setPower(long value) { power = Math.clamp(value, 0L, MAX_POWER); setChanged(); }
    @Override public long getMaxPower() { return MAX_POWER; }
    @Override public boolean isHeLoaded() { return hasLevel() && !isRemoved(); }

    @Override public String[] rorInfo() {
        return new String[]{VALUE_PREFIX + "turbinepercent", VALUE_PREFIX + "turbinespeed",
                VALUE_PREFIX + "output", VALUE_PREFIX + "state", VALUE_PREFIX + "automode",
                VALUE_PREFIX + "temp", VALUE_PREFIX + "power", VALUE_PREFIX + "fuel",
                VALUE_PREFIX + "lubricant", VALUE_PREFIX + "water", VALUE_PREFIX + "steam",
                FUNCTION_PREFIX + "setauto!auto", FUNCTION_PREFIX + "setthrottle!percent",
                FUNCTION_PREFIX + "setstate!state"};
    }

    @Override public String provideRorValue(String name) {
        if ((VALUE_PREFIX + "turbinepercent").equals(name)) return Integer.toString(throttle());
        if ((VALUE_PREFIX + "turbinespeed").equals(name)) return Integer.toString(rpm);
        if ((VALUE_PREFIX + "output").equals(name)) return Integer.toString(instantPowerOutput * 20);
        if ((VALUE_PREFIX + "state").equals(name)) return Integer.toString(state);
        if ((VALUE_PREFIX + "automode").equals(name)) return autoMode ? "1" : "0";
        if ((VALUE_PREFIX + "temp").equals(name)) return Integer.toString(temperature);
        if ((VALUE_PREFIX + "power").equals(name)) return Long.toString(power);
        if ((VALUE_PREFIX + "fuel").equals(name)) return Integer.toString(fuel.getFluidAmount());
        if ((VALUE_PREFIX + "lubricant").equals(name)) return Integer.toString(lubricant.getFluidAmount());
        if ((VALUE_PREFIX + "water").equals(name)) return Integer.toString(water.getFluidAmount());
        if ((VALUE_PREFIX + "steam").equals(name)) return Integer.toString(steam.getFluidAmount());
        return null;
    }

    @Override public void runRorFunction(String name, String[] parameters) throws RorFunctionException {
        if ((FUNCTION_PREFIX + "setauto").equals(name) && parameters.length > 0) {
            setControl(Control.AUTO, RorInteractive.integer(parameters[0], 0, 1));
        } else if ((FUNCTION_PREFIX + "setthrottle").equals(name) && parameters.length > 0) {
            int percent = RorInteractive.integer(parameters[0], 0, 100);
            setControl(Control.THROTTLE, percent * MAX_SLIDER / 100);
        } else if ((FUNCTION_PREFIX + "setstate").equals(name) && parameters.length > 0) {
            setControl(Control.STATE, RorInteractive.integer(parameters[0], 0, 1));
        }
    }

    private static FluidIdentifierItem.Selection selection(int ordinal) {
        FluidIdentifierItem.Selection[] values = FluidIdentifierItem.Selection.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FluidIdentifierItem.Selection.GAS;
    }

    private static void clientTank(FluidTank tank, Fluid fluid, int amount) {
        tank.setFluid(amount <= 0 ? FluidStack.EMPTY
                : new FluidStack(fluid, Math.min(amount, tank.getCapacity())));
    }

    public enum Control { STATE, AUTO, THROTTLE }

    private final class PortFluidHandler implements IFluidHandler {
        private final GasTurbineBlock.Port port;
        private PortFluidHandler(GasTurbineBlock.Port port) { this.port = port; }

        @Override public int getTanks() { return port == GasTurbineBlock.Port.FUEL_LUBE ? 2 : 1; }
        @Override public FluidStack getFluidInTank(int tank) {
            return tank(tank).getFluid().copy();
        }
        @Override public int getTankCapacity(int tank) { return tank(tank).getCapacity(); }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return port != GasTurbineBlock.Port.STEAM && tank(tank).isFluidValid(stack);
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (port == GasTurbineBlock.Port.STEAM) return 0;
            if (port == GasTurbineBlock.Port.FUEL_LUBE) {
                if (resource.is(ModFluids.GAS.get())) return fuel.fill(resource, action);
                if (resource.is(ModFluids.LUBRICANT.get())) return lubricant.fill(resource, action);
                return 0;
            }
            return water.fill(resource, action);
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            if (port != GasTurbineBlock.Port.STEAM || resource.isEmpty()
                    || !resource.is(ModFluids.HOTSTEAM.get())) return FluidStack.EMPTY;
            return steam.drain(resource, action);
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return port == GasTurbineBlock.Port.STEAM ? steam.drain(maxDrain, action) : FluidStack.EMPTY;
        }

        private FluidTank tank(int index) {
            if (port == GasTurbineBlock.Port.FUEL_LUBE) return index == 1 ? lubricant : fuel;
            return port == GasTurbineBlock.Port.WATER ? water : steam;
        }
    }
}
