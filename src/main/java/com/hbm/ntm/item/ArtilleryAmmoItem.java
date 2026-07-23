package com.hbm.ntm.item;

import com.hbm.ntm.weapon.ArtilleryAmmoType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ArtilleryAmmoItem extends Item {
    public ArtilleryAmmoItem() { super(new Properties()); }

    @Override public String getDescriptionId(ItemStack stack) {
        return "item.hbm." + ArtilleryAmmoType.fromStack(stack).serializedName();
    }
}
