package com.hbm.ntm.weapon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

/** Projectile and inventory properties shared by registered Sedna ammunition. */
public interface SednaAmmoType {
    String TYPE_KEY = "hbm_ammo_type";

    String serializedName();
    int legacyMetadata();
    int legacyBulletConfig();
    float damageMultiplier();
    float spread();
    int projectiles();
    float ricochetAngle();
    int maxRicochets();
    boolean penetrates();
    boolean penetrationDamageFalloff();

    default float headshotMultiplier() { return 1.25F; }
    default float armorThresholdNegation() { return 0.0F; }
    default float armorPiercing() { return 0.0F; }
    default float wear() { return 1.0F; }
    default boolean blackPowder() { return false; }
    default float impactExplosionRadius() { return 0.0F; }
    default int phosphorusTicks() { return 0; }
    default boolean spectral() { return false; }
    default double projectileSpeed() { return 10.0D; }
    default int projectileLifetime() { return 30; }
    default float blockBreakHardness() { return -1.0F; }
    default boolean spawnsBuildingOnImpact() { return false; }
    default int tracerDarkColor() { return 0xFFFFBF00; }
    default int tracerLightColor() { return 0xFFFFFFFF; }
    default boolean tracerFullbright() { return false; }

    default ItemStack createStack(Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = new CompoundTag();
        tag.putString(TYPE_KEY, serializedName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(legacyMetadata()));
        return stack;
    }
}
