package com.hbm.ntm.weapon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

public enum HimarsAmmoType {
    SMALL("ammo_himars_standard", 0, 6),
    LARGE("ammo_himars_single", 1, 1),
    SMALL_HE("ammo_himars_standard_he", 2, 6),
    SMALL_WP("ammo_himars_standard_wp", 3, 6),
    SMALL_TB("ammo_himars_standard_tb", 4, 6),
    LARGE_TB("ammo_himars_single_tb", 5, 1),
    SMALL_MINI_NUKE("ammo_himars_standard_mini_nuke", 6, 6),
    SMALL_LAVA("ammo_himars_standard_lava", 7, 6);

    private static final String TYPE_KEY = "hbm_himars_ammo";
    private final String serializedName;
    private final int legacyMetadata;
    private final int rockets;

    HimarsAmmoType(String serializedName, int legacyMetadata, int rockets) {
        this.serializedName = serializedName;
        this.legacyMetadata = legacyMetadata;
        this.rockets = rockets;
    }

    public String serializedName() { return serializedName; }
    public int legacyMetadata() { return legacyMetadata; }
    public int rockets() { return rockets; }

    public ItemStack createStack(Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = new CompoundTag();
        tag.putString(TYPE_KEY, serializedName);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(legacyMetadata));
        return stack;
    }

    public static HimarsAmmoType fromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(TYPE_KEY)) {
            String name = tag.getString(TYPE_KEY);
            for (HimarsAmmoType type : values()) if (type.serializedName.equals(name)) return type;
        }
        CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        int metadata = data == null ? 0 : data.value();
        for (HimarsAmmoType type : values()) if (type.legacyMetadata == metadata) return type;
        return SMALL;
    }
}
