package com.hbm.ntm.inventory;

import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.HeBatteryItem;
import com.hbm.ntm.item.PistonSetItem;
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

public final class CombustionEngineMenu extends AbstractContainerMenu {
    private final Container engine;
    private final ContainerData data;

    public CombustionEngineMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(10));
    }

    public CombustionEngineMenu(int id, Inventory inventory, Container engine, ContainerData data) {
        super(ModMenus.MACHINE_COMBUSTION_ENGINE.get(), id);
        checkContainerSize(engine, CombustionEngineBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, 10);
        this.engine = engine;
        this.data = data;
        engine.startOpen(inventory.player);

        addSlot(new RestrictedSlot(engine, CombustionEngineBlockEntity.FUEL_INPUT, 17, 17));
        addSlot(new OutputSlot(engine, CombustionEngineBlockEntity.CONTAINER_OUTPUT, 17, 53));
        addSlot(new RestrictedSlot(engine, CombustionEngineBlockEntity.PISTON_SET, 88, 71));
        addSlot(new RestrictedSlot(engine, CombustionEngineBlockEntity.BATTERY, 143, 71));
        addSlot(new RestrictedSlot(engine, CombustionEngineBlockEntity.FLUID_IDENTIFIER, 35, 71));
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 121 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 179));
        addDataSlots(data);
    }

    private static Container find(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos) instanceof CombustionEngineBlockEntity engine
                ? engine : new SimpleContainer(CombustionEngineBlockEntity.SLOT_COUNT);
    }

    public CombustionEngineBlockEntity blockEntity() {
        return engine instanceof CombustionEngineBlockEntity entity ? entity : null;
    }
    public long power() { return (data.get(0) & 0xFFFFFFFFL) | (long) data.get(1) << 32; }
    public int fuel() { return data.get(2); }
    public FluidIdentifierItem.Selection selectedFluid() {
        int ordinal = data.get(3);
        FluidIdentifierItem.Selection[] values = FluidIdentifierItem.Selection.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FluidIdentifierItem.Selection.DIESEL;
    }
    public int throttle() { return data.get(4); }
    public boolean on() { return data.get(5) != 0; }
    public boolean active() { return data.get(6) != 0; }
    public PistonSetItem.Type pistonType() {
        int ordinal = data.get(7);
        PistonSetItem.Type[] values = PistonSetItem.Type.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
    }
    public double efficiency() {
        PistonSetItem.Type piston = pistonType();
        return piston == null ? 0D : piston.efficiency(
                com.hbm.ntm.recipe.CombustionEngineFuels.fuel(selectedFluid()).grade());
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < CombustionEngineBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, CombustionEngineBlockEntity.SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof HeBatteryItem) {
            if (!moveItemStackTo(stack, CombustionEngineBlockEntity.BATTERY,
                    CombustionEngineBlockEntity.BATTERY + 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof FluidIdentifierItem) {
            if (!moveItemStackTo(stack, CombustionEngineBlockEntity.FLUID_IDENTIFIER,
                    CombustionEngineBlockEntity.FLUID_IDENTIFIER + 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof PistonSetItem) {
            if (!moveItemStackTo(stack, CombustionEngineBlockEntity.PISTON_SET,
                    CombustionEngineBlockEntity.PISTON_SET + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, CombustionEngineBlockEntity.FUEL_INPUT,
                CombustionEngineBlockEntity.FUEL_INPUT + 1, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        engine.stopOpen(player);
    }
    @Override public boolean stillValid(Player player) { return engine.stillValid(player); }

    private final class RestrictedSlot extends Slot {
        private RestrictedSlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return engine.canPlaceItem(getContainerSlot(), stack); }
    }
    private static final class OutputSlot extends Slot {
        private OutputSlot(Container container, int slot, int x, int y) { super(container, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }
}
