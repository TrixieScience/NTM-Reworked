package com.hbm.ntm.inventory;

import com.hbm.ntm.blockentity.FluidPumpBlockEntity;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class FluidPumpMenu extends AbstractContainerMenu {
    private final FluidPumpBlockEntity pump;
    private final ContainerData data;

    public FluidPumpMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, find(inventory, buffer.readBlockPos()), new SimpleContainerData(6));
    }

    public FluidPumpMenu(int id, Inventory inventory, FluidPumpBlockEntity pump, ContainerData data) {
        super(ModMenus.FLUID_PUMP.get(), id);
        checkContainerDataCount(data, 6);
        this.pump = pump;
        this.data = data;
        addDataSlots(data);
    }

    private static FluidPumpBlockEntity find(Inventory inventory, BlockPos pos) {
        return inventory.player.level().getBlockEntity(pos) instanceof FluidPumpBlockEntity pump ? pump : null;
    }

    public FluidPumpBlockEntity blockEntity() { return pump; }
    public int amount() { return data.get(0); }
    public int throughput() { return data.get(2); }
    public int pressure() { return data.get(3); }
    public int priority() { return data.get(4); }
    public boolean redstone() { return data.get(5) != 0; }
    public FluidIdentifierItem.Selection selection() {
        FluidIdentifierItem.Selection[] values = FluidIdentifierItem.Selection.values();
        int ordinal = data.get(1);
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FluidIdentifierItem.Selection.NONE;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) {
        return pump != null && pump.getLevel() != null
                && pump.getLevel().getBlockEntity(pump.getBlockPos()) == pump
                && player.distanceToSqr(pump.getBlockPos().getCenter()) <= 128D;
    }
}
