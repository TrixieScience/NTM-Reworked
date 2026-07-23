package com.hbm.ntm.item;

import com.hbm.ntm.weapon.TurretShellAmmoType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AmmoShellItem extends Item {
    public AmmoShellItem() {
        super(new Properties());
    }

    @Override public String getDescriptionId(ItemStack stack) {
        return "item.hbm.ammo_shell." + TurretShellAmmoType.fromStack(stack).serializedName();
    }
}
