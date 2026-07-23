package com.hbm.ntm.inventory;

import com.hbm.ntm.blockentity.CraneGrabberBlockEntity;
import com.hbm.ntm.item.MachineUpgradeItem;
import com.hbm.ntm.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class CraneGrabberMenu extends AbstractContainerMenu {
    private final Container grabber;
    private final ContainerData data;

    public CraneGrabberMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()),
                new SimpleContainerData(CraneGrabberBlockEntity.FILTER_END + 1));
    }

    public CraneGrabberMenu(int id, Inventory inventory, Container grabber, ContainerData data) {
        super(ModMenus.CRANE_GRABBER.get(), id);
        checkContainerSize(grabber, CraneGrabberBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, CraneGrabberBlockEntity.FILTER_END + 1);
        this.grabber = grabber;
        this.data = data;
        grabber.startOpen(inventory.player);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new FilterSlot(grabber, column + row * 3,
                        40 + column * 18, 17 + row * 18));
            }
        }
        addSlot(new UpgradeSlot(grabber, CraneGrabberBlockEntity.STACK_UPGRADE,
                121, 23, MachineUpgradeItem.Type.STACK));
        addSlot(new UpgradeSlot(grabber, CraneGrabberBlockEntity.EJECTOR_UPGRADE,
                121, 47, MachineUpgradeItem.Type.EJECTOR));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 103 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 161));
        }
        addDataSlots(data);
    }

    private static Container find(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos) instanceof CraneGrabberBlockEntity grabber
                ? grabber : new SimpleContainer(CraneGrabberBlockEntity.SLOT_COUNT);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= CraneGrabberBlockEntity.FILTER_START
                && slotId < CraneGrabberBlockEntity.FILTER_END
                && grabber instanceof CraneGrabberBlockEntity blockEntity) {
            if (button == 1 && blockEntity.getItem(slotId).isEmpty()) return;
            if (button == 1) blockEntity.nextFilterMode(slotId);
            else blockEntity.setFilter(slotId, getCarried());
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || index < CraneGrabberBlockEntity.FILTER_END) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int machineSlots = CraneGrabberBlockEntity.SLOT_COUNT;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof MachineUpgradeItem upgrade
                && upgrade.type() == MachineUpgradeItem.Type.STACK) {
            if (!moveItemStackTo(stack, CraneGrabberBlockEntity.STACK_UPGRADE,
                    CraneGrabberBlockEntity.STACK_UPGRADE + 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof MachineUpgradeItem upgrade
                && upgrade.type() == MachineUpgradeItem.Type.EJECTOR) {
            if (!moveItemStackTo(stack, CraneGrabberBlockEntity.EJECTOR_UPGRADE,
                    CraneGrabberBlockEntity.EJECTOR_UPGRADE + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != 0 || !(grabber instanceof CraneGrabberBlockEntity blockEntity)) return false;
        blockEntity.toggleWhitelist();
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return grabber.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        grabber.stopOpen(player);
    }

    public boolean whitelist() {
        return data.get(0) != 0;
    }

    public String filterModeLabel(int slot) {
        int code = data.get(slot + 1);
        if (code == 0) return "Item and meta match";
        if (code == 1) return "Item matches";
        List<String> tags = getSlot(slot).getItem().getItem().builtInRegistryHolder().tags()
                .map(tag -> tag.location().toString()).toList();
        int tag = code - 2;
        return tag >= 0 && tag < tags.size() ? "Item tag matches: #" + tags.get(tag) : "Item and meta match";
    }

    private static final class FilterSlot extends Slot {
        private FilterSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class UpgradeSlot extends Slot {
        private final MachineUpgradeItem.Type type;

        private UpgradeSlot(Container container, int slot, int x, int y, MachineUpgradeItem.Type type) {
            super(container, slot, x, y);
            this.type = type;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof MachineUpgradeItem upgrade && upgrade.type() == type;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
