package com.hbm.ntm.weapon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

public enum ArtilleryAmmoType {
    NORMAL("ammo_arty", 0),
    CLASSIC("ammo_arty_classic", 1),
    HIGH_EXPLOSIVE("ammo_arty_he", 2),
    MINI_NUKE("ammo_arty_mini_nuke", 3),
    NUKE("ammo_arty_nuke", 4),
    PHOSPHORUS("ammo_arty_phosphorus", 5),
    MINI_NUKE_MULTI("ammo_arty_mini_nuke_multi", 6),
    PHOSPHORUS_MULTI("ammo_arty_phosphorus_multi", 7),
    CARGO("ammo_arty_cargo", 8),
    CHLORINE("ammo_arty_chlorine", 9),
    PHOSGENE("ammo_arty_phosgene", 10),
    MUSTARD("ammo_arty_mustard_gas", 11);

    private static final String TYPE_KEY = "hbm_arty_ammo";
    private final String serializedName;
    private final int legacyMetadata;

    ArtilleryAmmoType(String serializedName, int legacyMetadata) {
        this.serializedName = serializedName;
        this.legacyMetadata = legacyMetadata;
    }

    public String serializedName() { return serializedName; }
    public int legacyMetadata() { return legacyMetadata; }

    public ItemStack createStack(Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = new CompoundTag();
        tag.putString(TYPE_KEY, serializedName);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(legacyMetadata));
        return stack;
    }

    public static ArtilleryAmmoType fromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(TYPE_KEY)) {
            String name = tag.getString(TYPE_KEY);
            for (ArtilleryAmmoType type : values()) if (type.serializedName.equals(name)) return type;
        }
        CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        int metadata = data == null ? 0 : data.value();
        for (ArtilleryAmmoType type : values()) if (type.legacyMetadata == metadata) return type;
        return NORMAL;
    }
}
