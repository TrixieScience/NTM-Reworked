package com.hbm.ntm.item;

import com.hbm.ntm.hazard.HazardProfile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/** A one-bucket legacy cell that leaves its empty shell behind in crafting. */
public final class FluidCellItem extends HazardousItem {
    private final Supplier<Item> emptyCell;

    public FluidCellItem(Properties properties, HazardProfile hazards, Supplier<Item> emptyCell) {
        super(properties, hazards);
        this.emptyCell = emptyCell;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return new ItemStack(emptyCell.get());
    }
}
