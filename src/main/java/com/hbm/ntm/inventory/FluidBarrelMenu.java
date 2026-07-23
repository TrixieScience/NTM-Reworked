package com.hbm.ntm.inventory;

import com.hbm.ntm.blockentity.FluidBarrelBlockEntity;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class FluidBarrelMenu extends AbstractContainerMenu {
    private final Container barrel;
    private final ContainerData data;

    public FluidBarrelMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(4));
    }

    public FluidBarrelMenu(int id, Inventory inventory, Container barrel, ContainerData data) {
        super(ModMenus.FLUID_BARREL.get(), id);
        checkContainerSize(barrel, FluidBarrelBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, 4);
        this.barrel = barrel;
        this.data = data;

        addSlot(new RestrictedSlot(barrel, FluidBarrelBlockEntity.IDENTIFIER_INPUT, 8, 17));
        addSlot(new OutputSlot(barrel, FluidBarrelBlockEntity.IDENTIFIER_OUTPUT, 8, 53));
        addSlot(new RestrictedSlot(barrel, FluidBarrelBlockEntity.FILLED_INPUT, 35, 17));
        addSlot(new OutputSlot(barrel, FluidBarrelBlockEntity.EMPTY_OUTPUT, 35, 53));
        addSlot(new RestrictedSlot(barrel, FluidBarrelBlockEntity.EMPTY_INPUT, 125, 17));
        addSlot(new OutputSlot(barrel, FluidBarrelBlockEntity.FILLED_OUTPUT, 125, 53));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    private static Container find(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel
                ? barrel : new SimpleContainer(FluidBarrelBlockEntity.SLOT_COUNT);
    }

    public FluidBarrelBlockEntity blockEntity() {
        return barrel instanceof FluidBarrelBlockEntity entity ? entity : null;
    }

    public int amount() {
        return data.get(0);
    }

    public FluidIdentifierItem.Selection selection() {
        int ordinal = data.get(1);
        FluidIdentifierItem.Selection[] values = FluidIdentifierItem.Selection.values();
        return ordinal >= 0 && ordinal < values.length
                ? values[ordinal] : FluidIdentifierItem.Selection.NONE;
    }

    public int mode() {
        return Math.clamp(data.get(2), 0, 3);
    }

    public int capacity() {
        return Math.max(data.get(3), 1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < FluidBarrelBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, FluidBarrelBlockEntity.SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof FluidIdentifierItem) {
            if (!moveItemStackTo(stack, FluidBarrelBlockEntity.IDENTIFIER_INPUT,
                    FluidBarrelBlockEntity.IDENTIFIER_INPUT + 1, false)) return ItemStack.EMPTY;
        } else if (barrel.canPlaceItem(FluidBarrelBlockEntity.FILLED_INPUT, stack)) {
            if (!moveItemStackTo(stack, FluidBarrelBlockEntity.FILLED_INPUT,
                    FluidBarrelBlockEntity.FILLED_INPUT + 1, false)) return ItemStack.EMPTY;
        } else if (barrel.canPlaceItem(FluidBarrelBlockEntity.EMPTY_INPUT, stack)) {
            if (!moveItemStackTo(stack, FluidBarrelBlockEntity.EMPTY_INPUT,
                    FluidBarrelBlockEntity.EMPTY_INPUT + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return barrel.stillValid(player);
    }

    private final class RestrictedSlot extends Slot {
        private RestrictedSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return barrel.canPlaceItem(getContainerSlot(), stack);
        }
    }

    private static final class OutputSlot extends Slot {
        private OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
