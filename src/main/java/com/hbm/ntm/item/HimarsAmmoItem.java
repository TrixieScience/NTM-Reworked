package com.hbm.ntm.item;

import com.hbm.ntm.weapon.HimarsAmmoType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class HimarsAmmoItem extends Item {
    public HimarsAmmoItem() { super(new Properties().stacksTo(1)); }

    @Override public String getDescriptionId(ItemStack stack) {
        return "item.hbm." + HimarsAmmoType.fromStack(stack).serializedName();
    }
}
