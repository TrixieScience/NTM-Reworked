package com.hbm.ntm.item;

import com.hbm.ntm.config.HbmConfig;
import com.hbm.ntm.weapon.GunInput;
import com.hbm.ntm.weapon.SednaCrosshair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Common gun questions with thirty different answers. */
public abstract class SednaGunItem extends Item {
    protected SednaGunItem() {
        super(new Properties().stacksTo(1));
    }

    public static void handleInput(Player player, GunInput input) {
        if (!HbmConfig.ENABLE_GUNS.get()) return;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof SednaGunItem gun) gun.handleGunInput(player, stack, input);
    }

    protected abstract void handleGunInput(Player player, ItemStack stack, GunInput input);
    public abstract boolean gunAiming(ItemStack stack);
    public boolean gunAutomatic() { return false; }
    public boolean gunSecondaryAutomatic() { return false; }
    public SednaCrosshair gunCrosshair() { return SednaCrosshair.CIRCLE; }
    public boolean gunHideCrosshairWhenAimed() { return true; }
    public boolean gunHideCrosshairWhenAimed(ItemStack stack) { return gunHideCrosshairWhenAimed(); }
    /** Stinger hides the reticle until it finishes raising its eyebrow. */
    public boolean gunCrosshairOnlyWhenAimed() { return false; }
    /** Full-aim FOV multiplier. */
    public float gunAimFovMultiplier() { return 0.67F; }
    public float gunAimFovMultiplier(ItemStack stack) { return gunAimFovMultiplier(); }
    /** Scope overlay, if this gun believes in optics. */
    public ResourceLocation gunScopeTexture() { return null; }
    public ResourceLocation gunScopeTexture(ItemStack stack) { return gunScopeTexture(); }
    public abstract int gunRounds(ItemStack stack);
    public abstract int gunCapacity();
    public abstract float gunWear(ItemStack stack);
    public abstract float gunDurability();
    public float gunDurability(ItemStack stack) { return gunDurability(); }
    /** Broken Maresleg has no condition because zero durability means forever. */
    public boolean gunShowDurability() { return true; }
    /** Belt guns count boxes, not individual regrets. */
    public boolean gunBeltFed() { return false; }
    /** Ammo icon without the scary arithmetic. */
    public boolean gunShowAmmoCounter() { return true; }
    public abstract ItemStack gunAmmoIcon(ItemStack stack);
    public boolean gunHasMirroredHud() { return false; }
    public int gunMirroredRounds(ItemStack stack) { return 0; }
    public int gunMirroredCapacity() { return 0; }
    public float gunMirroredWear(ItemStack stack) { return 0.0F; }
    public float gunMirroredDurability() { return 1.0F; }
    public boolean gunShowMirroredDurability() { return true; }
    public ItemStack gunMirroredAmmoIcon(ItemStack stack) { return ItemStack.EMPTY; }
    public abstract float recoilVertical();
    /** Auto Shotgun adds randomized vertical suffering. */
    public float recoilVerticalSigma() { return 0.0F; }
    public abstract float recoilHorizontalSigma();
}
