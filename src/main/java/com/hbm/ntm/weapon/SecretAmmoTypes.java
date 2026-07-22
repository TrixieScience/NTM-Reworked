package com.hbm.ntm.weapon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

public final class SecretAmmoTypes {
    private SecretAmmoTypes() { }

    public static SednaAmmoType fromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(SednaAmmoType.TYPE_KEY)) {
            String name = tag.getString(SednaAmmoType.TYPE_KEY);
            if (Equestrian44AmmoType.BOXCAR.serializedName().equals(name)) {
                return Equestrian44AmmoType.BOXCAR;
            }
            for (FollyAmmoType type : FollyAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (FiftyCalAmmoType type : FiftyCalAmmoType.values()) {
                if (type.secret() && type.serializedName().equals(name)) return type;
            }
            for (AberratorAmmoType type : AberratorAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
        }
        CustomModelData modelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        int metadata = modelData == null ? 0 : modelData.value();
        if (metadata == 0 || metadata == 1) return FollyAmmoType.fromLegacyMetadata(metadata);
        if (metadata == 2) return Equestrian44AmmoType.BOXCAR;
        if (metadata == 5 || metadata == 7) return AberratorAmmoType.fromLegacyMetadata(metadata);
        return FiftyCalAmmoType.fromLegacyMetadata(metadata);
    }
}
