package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.ChemicalPlantBlock;
import com.hbm.ntm.energy.HeReceiver;
import com.hbm.ntm.inventory.ChemicalPlantMenu;
import com.hbm.ntm.item.BlueprintItem;
import com.hbm.ntm.item.HeBatteryItem;
import com.hbm.ntm.item.MachineUpgradeItem;
import com.hbm.ntm.item.SourceFluidContainerItem;
import com.hbm.ntm.item.InfiniteFluidBarrelItem;
import com.hbm.ntm.item.UniversalFluidTankItem;
import com.hbm.ntm.recipe.ChemicalPlantRecipes;
import com.hbm.ntm.recipe.ChemicalPlantRecipes.ChemicalRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class ChemicalPlantBlockEntity extends BlockEntity
        implements WorldlyContainer, MenuProvider, HeReceiver {
    public static final int SLOT_COUNT = 22;
    public static final int BATTERY = 0;
    public static final int BLUEPRINT = 1;
    public static final int UPGRADE_START = 2;
    public static final int UPGRADE_END = 4;
    public static final int ITEM_INPUT_START = 4;
    public static final int ITEM_OUTPUT_START = 7;
    public static final int FLUID_INPUT_CONTAINER_START = 10;
    public static final int FLUID_INPUT_REMAINDER_START = 13;
    public static final int FLUID_OUTPUT_CONTAINER_START = 16;
    public static final int FLUID_OUTPUT_RESULT_START = 19;
    public static final int TANK_CAPACITY = 24_000;
    public static final long MIN_POWER = 100_000L;
    private static final int[] AUTOMATION_SLOTS = {4, 5, 6, 7, 8, 9};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final FluidTank[] inputTanks = new FluidTank[3];
    private final FluidTank[] outputTanks = new FluidTank[3];
    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return 6; }
        @Override public FluidStack getFluidInTank(int index) { return tank(index).getFluid(); }
        @Override public int getTankCapacity(int index) { return tank(index).getCapacity(); }
        @Override public boolean isFluidValid(int index, FluidStack stack) {
            return index >= 0 && index < 3 && inputTanks[index].isFluidValid(stack);
        }
        @Override public int fill(FluidStack stack, FluidAction action) {
            for (FluidTank tank : inputTanks) {
                int accepted = tank.fill(stack, action);
                if (accepted > 0) return accepted;
            }
            return 0;
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            for (FluidTank tank : outputTanks) {
                FluidStack drained = tank.drain(resource, action);
                if (!drained.isEmpty()) return drained;
            }
            return FluidStack.EMPTY;
        }
        @Override public FluidStack drain(int amount, FluidAction action) {
            for (FluidTank tank : outputTanks) {
                FluidStack drained = tank.drain(amount, action);
                if (!drained.isEmpty()) return drained;
            }
            return FluidStack.EMPTY;
        }
    };
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> (int) Math.round(progress * 1_000_000D);
                case 1 -> (int) power;
                case 2 -> (int) (power >>> 32);
                case 3 -> (int) maxPower;
                case 4 -> (int) (maxPower >>> 32);
                case 5 -> active ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value / 1_000_000D;
                case 1 -> power = power & 0xFFFFFFFF00000000L | value & 0xFFFFFFFFL;
                case 2 -> power = power & 0xFFFFFFFFL | (long) value << 32;
                case 3 -> maxPower = maxPower & 0xFFFFFFFF00000000L | value & 0xFFFFFFFFL;
                case 4 -> maxPower = maxPower & 0xFFFFFFFFL | (long) value << 32;
                case 5 -> active = value != 0;
                default -> { }
            }
        }
        @Override public int getCount() { return 6; }
    };

    private Component customName;
    private long power;
    private long maxPower = MIN_POWER;
    private double progress;
    private ResourceLocation recipeId;
    private boolean active;
    private int animation;
    private int previousAnimation;
    private long lastPower = Long.MIN_VALUE;
    private double lastProgress = Double.NaN;
    private boolean lastActive;

    public ChemicalPlantBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_CHEMICAL_PLANT.get(), pos, state);
        for (int i = 0; i < 3; i++) {
            inputTanks[i] = changedTank();
            outputTanks[i] = changedTank();
        }
    }

    private FluidTank changedTank() {
        return new FluidTank(TANK_CAPACITY) {
            @Override protected void onContentsChanged() { ChemicalPlantBlockEntity.this.setChanged(); }
        };
    }

    private FluidTank tank(int index) { return index < 3 ? inputTanks[index] : outputTanks[index - 3]; }

    public static void tick(Level level, BlockPos pos, BlockState state, ChemicalPlantBlockEntity plant) {
        if (level.isClientSide) plant.clientTick(); else plant.serverTick((ServerLevel) level, pos, state);
    }

    private void clientTick() {
        previousAnimation = animation;
        if (active) animation++;
    }

    private void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        ChemicalRecipe recipe = selectedRecipe();
        if (recipe != null && !recipe.pools().isEmpty()
                && !recipe.pools().contains(BlueprintItem.pool(items.get(BLUEPRINT)))) {
            recipeId = null;
            recipe = null;
            progress = 0D;
        }
        if (recipe != null) maxPower = Math.max(Math.max(power, recipe.power() * 100L), MIN_POWER);
        else maxPower = Math.max(Math.max(power, maxPower), MIN_POWER);
        dischargeBattery();
        loadInputContainers();
        unloadOutputContainers();
        pushOutputs(level, pos);
        if (level.getGameTime() % 20L == 0L) subscribeOutputs(level, pos);

        double speed = speedMultiplier();
        double powerMultiplier = powerMultiplier();
        active = recipe != null && canProcess(recipe, powerMultiplier);
        if (active) {
            long consumption = powerMultiplier == 1D ? recipe.power() : (long) (recipe.power() * powerMultiplier);
            power -= consumption;
            progress += Math.min(speed / recipe.duration(), 1D);
            if (progress >= 1D) {
                consume(recipe);
                produce(recipe);
                if (canProcess(recipe, powerMultiplier)) progress -= 1D; else progress = 0D;
            }
        } else progress = 0D;

        if (power != lastPower || progress != lastProgress || active != lastActive || level.getGameTime() % 20L == 0L) {
            lastPower = power; lastProgress = progress; lastActive = active;
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        setChanged();
    }

    private void dischargeBattery() {
        ItemStack stack = items.get(BATTERY);
        if (!(stack.getItem() instanceof HeBatteryItem battery)) return;
        long amount = Math.min(Math.min(Math.max(maxPower - power, 0L), battery.getDischargeRate(stack)),
                battery.getCharge(stack));
        if (amount > 0L) { battery.discharge(stack, amount); power += amount; }
    }

    private void subscribeOutputs(ServerLevel level, BlockPos core) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction cross = direction.getClockWise();
            for (int offset = -1; offset <= 1; offset++) {
                trySubscribe(level, core.relative(direction, 2).relative(cross, offset), direction);
            }
        }
    }

    private void pushOutputs(ServerLevel level, BlockPos core) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction cross = direction.getClockWise();
            for (int offset = -1; offset <= 1; offset++) {
                BlockPos target = core.relative(direction, 2).relative(cross, offset);
                IFluidHandler handler = level.getCapability(
                        net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                        target, direction.getOpposite());
                if (handler == null) continue;
                for (FluidTank tank : outputTanks) if (!tank.isEmpty()) {
                    int accepted = handler.fill(tank.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
                    if (accepted > 0) tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }

    private void loadInputContainers() {
        for (int lane = 0; lane < 3; lane++) {
            int inputSlot = FLUID_INPUT_CONTAINER_START + lane;
            int remainderSlot = FLUID_INPUT_REMAINDER_START + lane;
            ItemStack stack = items.get(inputSlot);
            if (InfiniteFluidBarrelItem.is(stack)) {
                ChemicalRecipe recipe = selectedRecipe();
                if (recipe != null && lane < recipe.fluidInputs().size()
                        && InfiniteFluidBarrelItem.fillTank(inputTanks[lane],
                        recipe.fluidInputs().get(lane).fluid().get()) > 0) setChanged();
                continue;
            }
            Fluid fluid = null;
            int amount = 1_000;
            ItemStack remainder = ItemStack.EMPTY;
            if (stack.is(Items.WATER_BUCKET)) { fluid = Fluids.WATER; remainder = new ItemStack(Items.BUCKET); }
            else if (stack.is(Items.LAVA_BUCKET)) { fluid = Fluids.LAVA; remainder = new ItemStack(Items.BUCKET); }
            else if (stack.getItem() instanceof UniversalFluidTankItem) {
                fluid = UniversalFluidTankItem.fluid(stack).fluid();
                remainder = new ItemStack(ModItems.FLUID_TANK_EMPTY.get());
            } else if (stack.is(ModItems.GAS_FULL.get())) {
                SourceFluidContainerItem.ContainedFluid contained = SourceFluidContainerItem.fluid(stack);
                if (contained != SourceFluidContainerItem.ContainedFluid.NONE) {
                    fluid = contained.fluid();
                    remainder = new ItemStack(ModItems.GAS_EMPTY.get());
                }
            } else if (stack.is(ModItems.CELL_TRITIUM.get())) {
                fluid = ModFluids.TRITIUM.get();
                remainder = new ItemStack(ModItems.CELL_EMPTY.get());
            } else if (stack.is(ModItems.CELL_SAS3.get())) {
                fluid = ModFluids.SAS3.get();
                remainder = new ItemStack(ModItems.CELL_EMPTY.get());
            } else if (stack.is(ModItems.get("nugget_mercury").get())) {
                fluid = com.hbm.ntm.registry.ModFluids.MERCURY.get();
                amount = 125;
            }
            if (fluid == null || fluid.isSame(Fluids.EMPTY)
                    || (!remainder.isEmpty() && !canMerge(items.get(remainderSlot), remainder))) continue;
            FluidStack load = new FluidStack(fluid, amount);
            if (inputTanks[lane].fill(load, IFluidHandler.FluidAction.SIMULATE) != amount) continue;
            inputTanks[lane].fill(load, IFluidHandler.FluidAction.EXECUTE);
            stack.shrink(1);
            if (!remainder.isEmpty()) mergeInto(remainderSlot, remainder);
        }
    }

    private void unloadOutputContainers() {
        for (int lane = 0; lane < 3; lane++) {
            int emptySlot = FLUID_OUTPUT_CONTAINER_START + lane;
            int resultSlot = FLUID_OUTPUT_RESULT_START + lane;
            ItemStack empty = items.get(emptySlot);
            FluidTank tank = outputTanks[lane];
            if (InfiniteFluidBarrelItem.is(empty)) {
                if (InfiniteFluidBarrelItem.discardTank(tank) > 0) setChanged();
                continue;
            }
            if (tank.getFluidAmount() < 1_000) continue;
            ItemStack full;
            if (empty.is(ModItems.FLUID_TANK_EMPTY.get())) {
                UniversalFluidTankItem.ContainedFluid type = UniversalFluidTankItem.ContainedFluid.fromFluid(
                        tank.getFluid().getFluid());
                if (type == null) continue;
                full = UniversalFluidTankItem.create(ModItems.FLUID_TANK_FULL.get(), type, 1);
            } else if (empty.is(ModItems.GAS_EMPTY.get())) {
                SourceFluidContainerItem.ContainedFluid type = SourceFluidContainerItem.ContainedFluid.fromFluid(
                        tank.getFluid().getFluid());
                if (type != SourceFluidContainerItem.ContainedFluid.GAS
                        && type != SourceFluidContainerItem.ContainedFluid.HYDROGEN
                        && type != SourceFluidContainerItem.ContainedFluid.OXYGEN) continue;
                full = SourceFluidContainerItem.create(ModItems.GAS_FULL.get(), type, 1);
            } else if (empty.is(ModItems.CELL_EMPTY.get())) {
                Fluid fluid = tank.getFluid().getFluid();
                if (fluid.isSame(ModFluids.TRITIUM.get())) {
                    full = new ItemStack(ModItems.CELL_TRITIUM.get());
                } else if (fluid.isSame(ModFluids.SAS3.get())) {
                    full = new ItemStack(ModItems.CELL_SAS3.get());
                } else continue;
            } else continue;
            if (!canMerge(items.get(resultSlot), full)) continue;
            empty.shrink(1);
            tank.drain(1_000, IFluidHandler.FluidAction.EXECUTE);
            mergeInto(resultSlot, full);
        }
    }

    private boolean canMerge(ItemStack existing, ItemStack added) {
        return existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, added)
                && existing.getCount() < existing.getMaxStackSize();
    }
    private void mergeInto(int slot, ItemStack added) {
        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) items.set(slot, added);
        else existing.grow(added.getCount());
    }

    public boolean canProcess(ChemicalRecipe recipe, double powerMultiplier) {
        long consumption = powerMultiplier == 1D ? recipe.power() : (long) (recipe.power() * powerMultiplier);
        if (power < consumption) return false;
        for (int lane = 0; lane < recipe.itemInputs().size(); lane++) {
            if (!recipe.itemInputs().get(lane).matches(items.get(ITEM_INPUT_START + lane))) return false;
        }
        for (int lane = 0; lane < recipe.fluidInputs().size(); lane++) {
            var required = recipe.fluidInputs().get(lane);
            FluidStack stored = inputTanks[lane].getFluid();
            if (stored.isEmpty() || !stored.getFluid().isSame(required.fluid().get())
                    || stored.getAmount() < required.amount()) return false;
        }
        for (int lane = 0; lane < recipe.itemOutputs().size(); lane++) {
            ItemStack output = items.get(ITEM_OUTPUT_START + lane);
            ItemStack produced = recipe.itemOutputs().get(lane);
            if (!output.isEmpty() && (!ItemStack.isSameItemSameComponents(output, produced)
                    || output.getCount() + produced.getCount() > output.getMaxStackSize())) return false;
        }
        for (int lane = 0; lane < recipe.fluidOutputs().size(); lane++) {
            var produced = recipe.fluidOutputs().get(lane);
            FluidTank tank = outputTanks[lane];
            if (!tank.isEmpty() && !tank.getFluid().getFluid().isSame(produced.fluid().get())) return false;
            if (tank.getFluidAmount() + produced.amount() > tank.getCapacity()) return false;
        }
        return true;
    }

    private void consume(ChemicalRecipe recipe) {
        for (int lane = 0; lane < recipe.itemInputs().size(); lane++) {
            removeItem(ITEM_INPUT_START + lane, recipe.itemInputs().get(lane).count());
        }
        for (int lane = 0; lane < recipe.fluidInputs().size(); lane++) {
            inputTanks[lane].drain(recipe.fluidInputs().get(lane).amount(), IFluidHandler.FluidAction.EXECUTE);
        }
    }
    private void produce(ChemicalRecipe recipe) {
        for (int lane = 0; lane < recipe.itemOutputs().size(); lane++) {
            ItemStack output = items.get(ITEM_OUTPUT_START + lane);
            if (output.isEmpty()) items.set(ITEM_OUTPUT_START + lane, recipe.itemOutputs().get(lane).copy());
            else output.grow(recipe.itemOutputs().get(lane).getCount());
        }
        for (int lane = 0; lane < recipe.fluidOutputs().size(); lane++) {
            var output = recipe.fluidOutputs().get(lane);
            outputTanks[lane].fill(new FluidStack(output.fluid().get(), output.amount()),
                    IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public boolean selectRecipe(ResourceLocation id) {
        ChemicalRecipe recipe = ChemicalPlantRecipes.get(id);
        if (recipe == null || !recipe.pools().isEmpty()
                && !recipe.pools().contains(BlueprintItem.pool(items.get(BLUEPRINT)))) return false;
        recipeId = id;
        configureRecipeTanks(recipe, true);
        setChanged();
        return true;
    }

    public void clearRecipe() {
        recipeId = null;
        progress = 0D;
        active = false;
        setChanged();
    }

    private void configureRecipeTanks(ChemicalRecipe recipe, boolean resetProgress) {
        if (resetProgress) progress = 0D;
        for (int lane = 0; lane < 3; lane++) {
            Fluid expected = lane < recipe.fluidInputs().size() ? recipe.fluidInputs().get(lane).fluid().get() : Fluids.EMPTY;
            inputTanks[lane].setValidator(stack -> expected != Fluids.EMPTY && stack.getFluid().isSame(expected));
            if (!inputTanks[lane].isEmpty() && (expected == Fluids.EMPTY
                    || !inputTanks[lane].getFluid().getFluid().isSame(expected))) inputTanks[lane].setFluid(FluidStack.EMPTY);
            Fluid output = lane < recipe.fluidOutputs().size() ? recipe.fluidOutputs().get(lane).fluid().get() : Fluids.EMPTY;
            if (!outputTanks[lane].isEmpty() && (output == Fluids.EMPTY
                    || !outputTanks[lane].getFluid().getFluid().isSame(output))) outputTanks[lane].setFluid(FluidStack.EMPTY);
        }
    }

    private int upgradeLevel(MachineUpgradeItem.Type type) {
        int value = 0;
        for (int slot = UPGRADE_START; slot < UPGRADE_END; slot++) {
            if (items.get(slot).getItem() instanceof MachineUpgradeItem upgrade && upgrade.type() == type) value += upgrade.level();
        }
        return Math.min(value, 3);
    }
    public double speedMultiplier() { return 1D + upgradeLevel(MachineUpgradeItem.Type.SPEED) / 3D
            + upgradeLevel(MachineUpgradeItem.Type.OVERDRIVE); }
    public double powerMultiplier() { return 1D - upgradeLevel(MachineUpgradeItem.Type.POWER) * 0.25D
            + upgradeLevel(MachineUpgradeItem.Type.SPEED)
            + upgradeLevel(MachineUpgradeItem.Type.OVERDRIVE) * 10D / 3D; }

    @Nullable public ChemicalRecipe selectedRecipe() { return recipeId == null ? null : ChemicalPlantRecipes.get(recipeId); }
    @Nullable public ResourceLocation recipeId() { return recipeId; }
    public FluidTank inputTank(int lane) { return inputTanks[lane]; }
    public FluidTank outputTank(int lane) { return outputTanks[lane]; }
    public IFluidHandler fluidHandler() { return fluidHandler; }
    public double progress() { return progress; }
    public boolean active() { return active; }
    public float animation(float partialTick) { return previousAnimation + (animation - previousAnimation) * partialTick; }
    public boolean hasFrame() { return level != null && !level.getBlockState(worldPosition.above(3)).isAir(); }
    public ContainerData data() { return data; }

    @Override public long getPower() { return Math.max(0L, Math.min(power, maxPower)); }
    @Override public void setPower(long value) { power = Math.max(0L, Math.min(value, maxPower)); }
    @Override public long getMaxPower() { return maxPower; }
    @Override public boolean isHeLoaded() { return hasLevel() && !isRemoved(); }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putLong("power", power); tag.putLong("maxPower", maxPower);
        tag.putDouble("progress", progress); tag.putInt("animation", animation);
        if (recipeId != null) tag.putString("recipe", recipeId.toString());
        for (int i = 0; i < 3; i++) {
            tag.put("inputTank" + i, inputTanks[i].writeToNBT(registries, new CompoundTag()));
            tag.put("outputTank" + i, outputTanks[i].writeToNBT(registries, new CompoundTag()));
        }
        if (customName != null) tag.putString("name", Component.Serializer.toJson(customName, registries));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        power = tag.getLong("power"); maxPower = Math.max(MIN_POWER, tag.getLong("maxPower"));
        progress = tag.getDouble("progress"); animation = previousAnimation = tag.getInt("animation");
        recipeId = tag.contains("recipe") ? ResourceLocation.tryParse(tag.getString("recipe")) : null;
        for (int i = 0; i < 3; i++) {
            if (tag.contains("inputTank" + i)) inputTanks[i].readFromNBT(registries, tag.getCompound("inputTank" + i));
            if (tag.contains("outputTank" + i)) outputTanks[i].readFromNBT(registries, tag.getCompound("outputTank" + i));
        }
        ChemicalRecipe recipe = selectedRecipe();
        if (recipe != null) configureRecipeTanks(recipe, false);
        customName = tag.contains("name") ? Component.Serializer.fromJson(tag.getString("name"), registries) : null;
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("power", power); tag.putLong("maxPower", maxPower); tag.putDouble("progress", progress);
        // Animation is client-local and happily unsaved.
        // didProcess-driven animation. Sending the server's unused zero value here
        // reset the animation on every machine update.
        tag.putBoolean("active", active);
        if (recipeId != null) tag.putString("recipe", recipeId.toString());
        for (int i = 0; i < 3; i++) {
            tag.put("inputTank" + i, inputTanks[i].writeToNBT(registries, new CompoundTag()));
            tag.put("outputTank" + i, outputTanks[i].writeToNBT(registries, new CompoundTag()));
        }
        return tag;
    }
    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        power = tag.getLong("power"); maxPower = tag.getLong("maxPower"); progress = tag.getDouble("progress");
        active = tag.getBoolean("active");
        recipeId = tag.contains("recipe") ? ResourceLocation.tryParse(tag.getString("recipe")) : null;
        for (int i = 0; i < 3; i++) {
            if (tag.contains("inputTank" + i)) inputTanks[i].readFromNBT(registries, tag.getCompound("inputTank" + i));
            if (tag.contains("outputTank" + i)) outputTanks[i].readFromNBT(registries, tag.getCompound("outputTank" + i));
        }
    }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public Component getDisplayName() { return customName != null ? customName : Component.translatable("container.machineChemicalPlant"); }
    public void setCustomName(Component name) { customName = name; setChanged(); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ChemicalPlantMenu(id, inventory, this, data);
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count); if (!removed.isEmpty()) setChanged(); return removed;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack); if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize()); setChanged();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getCenter()) <= 128D;
    }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == BATTERY) return stack.getItem() instanceof HeBatteryItem;
        if (slot == BLUEPRINT) return stack.is(ModItems.BLUEPRINTS.get());
        if (slot >= UPGRADE_START && slot < UPGRADE_END) return stack.getItem() instanceof MachineUpgradeItem;
        if (slot >= ITEM_INPUT_START && slot < ITEM_OUTPUT_START) return itemInputValid(slot, stack);
        if (slot >= FLUID_INPUT_CONTAINER_START && slot < FLUID_INPUT_REMAINDER_START)
            return InfiniteFluidBarrelItem.is(stack) || stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET)
                    || stack.getItem() instanceof UniversalFluidTankItem
                    || stack.is(ModItems.GAS_FULL.get()) || stack.is(ModItems.get("nugget_mercury").get());
        return slot >= FLUID_OUTPUT_CONTAINER_START && slot < FLUID_OUTPUT_RESULT_START
                && (InfiniteFluidBarrelItem.is(stack) || stack.is(ModItems.FLUID_TANK_EMPTY.get())
                || stack.is(ModItems.GAS_EMPTY.get()));
    }
    private boolean itemInputValid(int slot, ItemStack stack) {
        ChemicalRecipe recipe = selectedRecipe();
        int lane = slot - ITEM_INPUT_START;
        return recipe != null && lane >= 0 && lane < recipe.itemInputs().size()
                && recipe.itemInputs().get(lane).ingredient().test(stack);
    }

    @Override public int[] getSlotsForFace(Direction side) { return Arrays.copyOf(AUTOMATION_SLOTS, AUTOMATION_SLOTS.length); }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot >= ITEM_INPUT_START && slot < ITEM_OUTPUT_START && canPlaceItem(slot, stack);
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot >= ITEM_OUTPUT_START && slot < FLUID_INPUT_CONTAINER_START
                || slot >= ITEM_INPUT_START && slot < ITEM_OUTPUT_START && !itemInputValid(slot, stack);
    }
}
