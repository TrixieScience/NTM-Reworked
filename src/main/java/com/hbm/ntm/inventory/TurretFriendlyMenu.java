package com.hbm.ntm.inventory;

import com.hbm.ntm.blockentity.TurretFriendlyBlockEntity;
import com.hbm.ntm.item.HeBatteryItem;
import com.hbm.ntm.item.TurretChipItem;
import com.hbm.ntm.registry.ModMenus;
import com.hbm.ntm.weapon.FiveFiveSixAmmoType;
import com.hbm.ntm.weapon.StandardAmmoTypes;
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

public final class TurretFriendlyMenu extends AbstractContainerMenu {
    private final Container turret;
    private final ContainerData data;

    public TurretFriendlyMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(7));
    }

    public TurretFriendlyMenu(int id, Inventory inventory, Container turret, ContainerData data) {
        super(ModMenus.TURRET_FRIENDLY.get(), id);
        checkContainerSize(turret, TurretFriendlyBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, 7);
        this.turret = turret;
        this.data = data;
        addSlot(new FilterSlot(turret, 0, 98, 27, stack -> stack.getItem() instanceof TurretChipItem));
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++) {
            addSlot(new FilterSlot(turret, 1 + row * 3 + column, 80 + column * 18, 63 + row * 18,
                    stack -> StandardAmmoTypes.fromStack(stack) instanceof FiveFiveSixAmmoType));
        }
        addSlot(new FilterSlot(turret, 10, 152, 99, stack -> stack.getItem() instanceof HeBatteryItem));
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, 9 + row * 9 + column, 8 + column * 18, 140 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 198));
        addDataSlots(data);
    }

    private static Container find(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos) instanceof TurretFriendlyBlockEntity turret
                ? turret : new SimpleContainer(TurretFriendlyBlockEntity.SLOT_COUNT);
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        return turret instanceof TurretFriendlyBlockEntity friendly && friendly.clickButton(id);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < TurretFriendlyBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, TurretFriendlyBlockEntity.SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            int start = stack.getItem() instanceof TurretChipItem ? 0
                    : stack.getItem() instanceof HeBatteryItem ? 10 : 1;
            int end = start == 1 ? 10 : start + 1;
            if (!moveItemStackTo(stack, start, end, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public boolean stillValid(Player player) { return turret.stillValid(player); }
    public int power() { return data.get(0); }
    public boolean active() { return data.get(1) != 0; }
    public boolean mode(int id) { return data.get(id + 1) != 0; }
    public int stattrak() { return data.get(6); }
    public java.util.List<String> whitelist() { return TurretChipItem.names(turret.getItem(0)); }
    public void editWhitelist(int action, String name, int index) {
        if (!(turret instanceof TurretFriendlyBlockEntity friendly)) return;
        if (action == 0) friendly.addWhitelistName(name);
        else if (action == 1) friendly.removeWhitelistName(index);
    }

    private static final class FilterSlot extends Slot {
        private final java.util.function.Predicate<ItemStack> filter;
        private FilterSlot(Container container, int slot, int x, int y,
                           java.util.function.Predicate<ItemStack> filter) {
            super(container, slot, x, y); this.filter = filter;
        }
        @Override public boolean mayPlace(ItemStack stack) { return filter.test(stack); }
    }
}
