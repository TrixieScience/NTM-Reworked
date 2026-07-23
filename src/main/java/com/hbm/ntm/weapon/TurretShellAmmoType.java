package com.hbm.ntm.weapon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

public enum TurretShellAmmoType implements SednaAmmoType {
    STOCK("ammo_shell", 200, 1F, false, true, 10F, false),
    HIGH_EXPLOSIVE("ammo_shell_explosive", 201, 1.5F, false, true, 10F, false),
    APFSDS_T("ammo_shell_apfsds_t", 202, 2F, true, true, 0F, false),
    APFSDS_DU("ammo_shell_apfsds_du", 203, 2.5F, true, false, 0F, false),
    W9("ammo_shell_w9", 204, 2.5F, false, true, 0F, true);

    private final String name;
    private final int config;
    private final float damage;
    private final boolean penetrates;
    private final boolean falloff;
    private final float explosion;
    private final boolean nuclear;

    TurretShellAmmoType(String name, int config, float damage, boolean penetrates,
                        boolean falloff, float explosion, boolean nuclear) {
        this.name = name;
        this.config = config;
        this.damage = damage;
        this.penetrates = penetrates;
        this.falloff = falloff;
        this.explosion = explosion;
        this.nuclear = nuclear;
    }

    @Override public String serializedName() { return name; }
    @Override public int legacyMetadata() { return ordinal(); }
    @Override public int legacyBulletConfig() { return config; }
    @Override public float damageMultiplier() { return damage; }
    @Override public float spread() { return 0F; }
    @Override public int projectiles() { return 1; }
    @Override public float ricochetAngle() { return 0F; }
    @Override public int maxRicochets() { return 0; }
    @Override public boolean penetrates() { return penetrates; }
    @Override public boolean penetrationDamageFalloff() { return falloff; }
    @Override public float impactExplosionRadius() { return explosion; }
    @Override public double impactExplosionRange() { return explosion; }
    @Override public boolean impactBreaksBlocks() { return explosion > 0F; }
    @Override public boolean nuclearImpact() { return nuclear; }

    @Override public ItemStack createStack(Item item, int count) {
        ItemStack stack = SednaAmmoType.super.createStack(item, count);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(ordinal()));
        return stack;
    }

    public static TurretShellAmmoType fromStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(TYPE_KEY)) {
            String value = tag.getString(TYPE_KEY);
            for (TurretShellAmmoType type : values()) if (type.name.equals(value)) return type;
        }
        CustomModelData model = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        int value = model == null ? 0 : model.value();
        return value >= 0 && value < values().length ? values()[value] : STOCK;
    }

    public static TurretShellAmmoType fromConfig(int config) {
        for (TurretShellAmmoType type : values()) if (type.config == config) return type;
        return STOCK;
    }
}
