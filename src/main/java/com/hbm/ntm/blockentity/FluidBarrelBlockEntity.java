package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.FluidBarrelBlock;
import com.hbm.ntm.fluid.FluidTankProperties;
import com.hbm.ntm.inventory.FluidBarrelMenu;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.InfiniteFluidBarrelItem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public final class FluidBarrelBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int IDENTIFIER_INPUT = 0;
    public static final int IDENTIFIER_OUTPUT = 1;
    public static final int FILLED_INPUT = 2;
    public static final int EMPTY_OUTPUT = 3;
    public static final int EMPTY_INPUT = 4;
    public static final int FILLED_OUTPUT = 5;
    public static final int SLOT_COUNT = 6;

    public static final int MODE_INPUT = 0;
    public static final int MODE_BUFFER = 1;
    public static final int MODE_OUTPUT = 2;
    public static final int MODE_LOCKED = 3;

    public static final String ITEM_FLUID = "barrelFluid";
    public static final String ITEM_AMOUNT = "barrelAmount";
    public static final String ITEM_MODE = "barrelMode";

    private static final int[] AUTOMATION_SLOTS = {FILLED_INPUT, EMPTY_OUTPUT, EMPTY_INPUT, FILLED_OUTPUT};

    private final FluidBarrelBlock.Type type;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private FluidIdentifierItem.Selection selection = FluidIdentifierItem.Selection.NONE;
    private final FluidTank tank;
    private int mode;
    private boolean pushing;
    private int lastAmount = Integer.MIN_VALUE;
    private int lastMode = Integer.MIN_VALUE;
    private FluidIdentifierItem.Selection lastSelection;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> tank.getFluidAmount();
                case 1 -> selection.ordinal();
                case 2 -> mode;
                case 3 -> type.capacity();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            FluidIdentifierItem.Selection[] values = FluidIdentifierItem.Selection.values();
            if (index == 1) {
                selection = value >= 0 && value < values.length
                        ? values[value] : FluidIdentifierItem.Selection.NONE;
            } else if (index == 2) {
                mode = Math.clamp(value, MODE_INPUT, MODE_LOCKED);
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            return index == 0 ? tank.getFluid().copy() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int index) {
            return index == 0 ? type.capacity() : 0;
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return index == 0 && !pushing && (mode == MODE_INPUT || mode == MODE_BUFFER)
                    && selection.accepts(stack.getFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return isFluidValid(0, resource) ? tank.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!canDrain() || resource.isEmpty() || tank.isEmpty()
                    || !resource.getFluid().isSame(tank.getFluid().getFluid())) return FluidStack.EMPTY;
            return tank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return canDrain() ? tank.drain(maxDrain, action) : FluidStack.EMPTY;
        }

        private boolean canDrain() {
            return !pushing && (mode == MODE_BUFFER || mode == MODE_OUTPUT);
        }
    };

    public FluidBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_BARREL.get(), pos, state);
        type = ((FluidBarrelBlock) state.getBlock()).type();
        tank = new FluidTank(type.capacity(), stack -> selection.accepts(stack.getFluid())) {
            @Override
            protected void onContentsChanged() {
                FluidBarrelBlockEntity.this.setChanged();
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluidBarrelBlockEntity barrel) {
        if (!level.isClientSide) barrel.serverTick((ServerLevel) level, pos, state);
    }

    private void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        processIdentifier();
        loadFromContainer();
        unloadToContainer();
        if (mode == MODE_BUFFER || mode == MODE_OUTPUT) pushFluid(level, pos);
        if (checkUnsafeFluid(level, pos)) return;
        syncIfChanged(level, pos, state);
    }

    private void processIdentifier() {
        ItemStack input = items.get(IDENTIFIER_INPUT);
        if (!(input.getItem() instanceof FluidIdentifierItem) || input.isEmpty()) return;
        ItemStack moved = input.copyWithCount(1);
        if (!canMerge(items.get(IDENTIFIER_OUTPUT), moved)) return;
        selectFluid(FluidIdentifierItem.primary(input));
        input.shrink(1);
        mergeInto(IDENTIFIER_OUTPUT, moved);
    }

    private void loadFromContainer() {
        if (selection == FluidIdentifierItem.Selection.NONE || tank.getSpace() <= 0) return;
        ItemStack input = items.get(FILLED_INPUT);
        if (input.isEmpty()) return;
        if (InfiniteFluidBarrelItem.is(input)) {
            InfiniteFluidBarrelItem.fillTank(tank, selection.fluid());
            return;
        }
        IFluidHandlerItem handler = input.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return;
        FluidStack simulated = handler.drain(tank.getSpace(), IFluidHandler.FluidAction.SIMULATE);
        if (simulated.isEmpty() || !selection.accepts(simulated.getFluid())) return;
        int accepted = tank.fill(simulated, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return;
        FluidStack drained = handler.drain(simulated.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) return;
        ItemStack result = handler.getContainer().copy();
        if (!canMerge(items.get(EMPTY_OUTPUT), result)) return;
        tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        input.shrink(1);
        mergeInto(EMPTY_OUTPUT, result);
    }

    private void unloadToContainer() {
        if (tank.isEmpty()) return;
        ItemStack input = items.get(EMPTY_INPUT);
        if (input.isEmpty()) return;
        if (InfiniteFluidBarrelItem.is(input)) {
            InfiniteFluidBarrelItem.discardTank(tank);
            return;
        }
        IFluidHandlerItem handler = input.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return;
        FluidStack available = tank.getFluid().copy();
        int accepted = handler.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return;
        int filled = handler.fill(available.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
        if (filled <= 0) return;
        ItemStack result = handler.getContainer().copy();
        if (!canMerge(items.get(FILLED_OUTPUT), result)) return;
        tank.drain(filled, IFluidHandler.FluidAction.EXECUTE);
        input.shrink(1);
        mergeInto(FILLED_OUTPUT, result);
    }

    private void pushFluid(ServerLevel level, BlockPos pos) {
        if (tank.isEmpty()) return;
        pushing = true;
        try {
            for (Direction direction : Direction.values()) {
                if (tank.isEmpty()) break;
                BlockPos targetPos = pos.relative(direction);
                IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK,
                        targetPos, direction.getOpposite());
                if (target == null) continue;
                FluidStack offered = tank.getFluid().copy();
                int accepted = target.fill(offered, IFluidHandler.FluidAction.EXECUTE);
                if (accepted > 0) tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            }
        } finally {
            pushing = false;
        }
    }

    private boolean checkUnsafeFluid(ServerLevel level, BlockPos pos) {
        if (tank.isEmpty()) return false;
        FluidTankProperties.Profile profile = FluidTankProperties.get(selection);
        boolean hot = tank.getFluid().getFluidType().getTemperature(tank.getFluid()) >= 373;
        boolean corrosive = profile.symbol() == FluidTankProperties.Symbol.ACID;
        boolean antimatter = profile.symbol() == FluidTankProperties.Symbol.ANTIMATTER;

        if (type != FluidBarrelBlock.Type.ANTIMATTER && antimatter) {
            level.destroyBlock(pos, false);
            level.explode(null, pos.getX() + .5D, pos.getY() + .5D, pos.getZ() + .5D,
                    5F, true, Level.ExplosionInteraction.TNT);
            return true;
        }
        if (type == FluidBarrelBlock.Type.PLASTIC && (hot || corrosive)) {
            level.destroyBlock(pos, false);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1F, 1F);
            return true;
        }
        return false;
    }

    private void syncIfChanged(ServerLevel level, BlockPos pos, BlockState state) {
        if (tank.getFluidAmount() != lastAmount || mode != lastMode || selection != lastSelection
                || level.getGameTime() % 20L == 0L) {
            lastAmount = tank.getFluidAmount();
            lastMode = mode;
            lastSelection = selection;
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
        setChanged();
    }

    public void selectFluid(@Nullable FluidIdentifierItem.Selection newSelection) {
        if (newSelection == null) newSelection = FluidIdentifierItem.Selection.NONE;
        if (selection == newSelection) return;
        selection = newSelection;
        tank.setFluid(FluidStack.EMPTY);
        setChanged();
    }

    public void cycleMode() {
        mode = (mode + 1) & 3;
        setChanged();
    }

    public FluidIdentifierItem.Selection selection() {
        return selection;
    }

    public FluidTank tank() {
        return tank;
    }

    public int mode() {
        return mode;
    }

    public int capacity() {
        return type.capacity();
    }

    public IFluidHandler fluidHandler() {
        return fluidHandler;
    }

    public ContainerData dataAccess() {
        return data;
    }

    public int comparatorSignal() {
        return tank.isEmpty() ? 0
                : Math.clamp((int) ((long) tank.getFluidAmount() * 15L / type.capacity()) + 1, 1, 15);
    }

    public ItemStack machineDrop() {
        ItemStack stack = new ItemStack(getBlockState().getBlock());
        if (tank.isEmpty()) return stack;
        CompoundTag data = new CompoundTag();
        data.putString(ITEM_FLUID, selection.id());
        data.putInt(ITEM_AMOUNT, tank.getFluidAmount());
        data.putInt(ITEM_MODE, mode);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    public void restoreFromItem(ItemStack stack) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!data.contains(ITEM_FLUID)) return;
        selection = FluidIdentifierItem.Selection.byId(data.getString(ITEM_FLUID));
        int amount = Math.clamp(data.getInt(ITEM_AMOUNT), 0, type.capacity());
        tank.setFluid(selection == FluidIdentifierItem.Selection.NONE || amount == 0
                ? FluidStack.EMPTY : new FluidStack(selection.fluid(), amount));
        mode = Math.clamp(data.getInt(ITEM_MODE), MODE_INPUT, MODE_LOCKED);
        conformTank();
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.barrel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FluidBarrelMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putString("selectedFluid", selection.id());
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("mode", mode);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        selection = FluidIdentifierItem.Selection.byId(tag.getString("selectedFluid"));
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
        mode = Math.clamp(tag.getInt("mode"), MODE_INPUT, MODE_LOCKED);
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
        tag.putInt("mode", mode);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        selection = FluidIdentifierItem.Selection.byId(tag.getString("selectedFluid"));
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
        mode = Math.clamp(tag.getInt("mode"), MODE_INPUT, MODE_LOCKED);
        conformTank();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static boolean canMerge(ItemStack existing, ItemStack addition) {
        return !addition.isEmpty() && (existing.isEmpty()
                || ItemStack.isSameItemSameComponents(existing, addition)
                && existing.getCount() + addition.getCount() <= existing.getMaxStackSize());
    }

    private void mergeInto(int slot, ItemStack addition) {
        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) items.set(slot, addition.copy());
        else existing.grow(addition.getCount());
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 128D;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == IDENTIFIER_INPUT) return stack.getItem() instanceof FluidIdentifierItem;
        IFluidHandlerItem handler = stack.copyWithCount(1).getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return false;
        if (slot == FILLED_INPUT) {
            if (InfiniteFluidBarrelItem.is(stack)) return selection != FluidIdentifierItem.Selection.NONE;
            FluidStack fluid = handler.drain(type.capacity(), IFluidHandler.FluidAction.SIMULATE);
            return !fluid.isEmpty() && selection.accepts(fluid.getFluid());
        }
        return slot == EMPTY_INPUT && !tank.isEmpty()
                && handler.fill(tank.getFluid().copy(), IFluidHandler.FluidAction.SIMULATE) > 0;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return AUTOMATION_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return (slot == FILLED_INPUT || slot == EMPTY_INPUT) && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == EMPTY_OUTPUT || slot == FILLED_OUTPUT;
    }
}
