package com.hbm.ntm.weapon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

public final class StandardAmmoTypes {
    private StandardAmmoTypes() { }

    public static SednaAmmoType fromStack(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(SednaAmmoType.TYPE_KEY)) {
            String name = tag.getString(SednaAmmoType.TYPE_KEY);
            for (PepperboxAmmoType type : PepperboxAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (Magnum357AmmoType type : Magnum357AmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (Magnum44AmmoType type : Magnum44AmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (NineMillimeterAmmoType type : NineMillimeterAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (TwentyTwoAmmoType type : TwentyTwoAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (FlamerFuelType type : FlamerFuelType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (RocketAmmoType type : RocketAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (Shotgun12GaugeAmmoType type : Shotgun12GaugeAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (FortyMillimeterAmmoType type : FortyMillimeterAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (SevenSixTwoAmmoType type : SevenSixTwoAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (FiveFiveSixAmmoType type : FiveFiveSixAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (FiftyCalAmmoType type : FiftyCalAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (EnergyAmmoType type : EnergyAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (TauAmmoType type : TauAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
            for (CoilAmmoType type : CoilAmmoType.values()) {
                if (type.serializedName().equals(name)) return type;
            }
        }
        CustomModelData modelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return fromLegacyMetadata(modelData == null ? 0 : modelData.value());
    }

    public static SednaAmmoType fromLegacyMetadata(int metadata) {
        if (metadata >= 4 && metadata <= 9) return Magnum357AmmoType.fromLegacyMetadata(metadata);
        if (metadata >= 10 && metadata <= 15) return Magnum44AmmoType.fromLegacyMetadata(metadata);
        if (metadata >= 16 && metadata <= 19) return TwentyTwoAmmoType.fromLegacyMetadata(metadata);
        if (metadata >= 20 && metadata <= 23) return NineMillimeterAmmoType.fromLegacyMetadata(metadata);
        if (metadata >= 24 && metadata <= 27) return FiveFiveSixAmmoType.fromLegacyMetadata(metadata);
        if ((metadata >= 33 && metadata <= 37) || metadata == 83 || metadata == 94) {
            return FiftyCalAmmoType.fromLegacyMetadata(metadata);
        }
        if (metadata == 104 || metadata == 106) return FiftyCalAmmoType.fromLegacyBulletConfig(metadata);
        if (metadata >= 63 && metadata <= 66) return FlamerFuelType.fromLegacyMetadata(metadata);
        if (metadata >= 67 && metadata <= 69) return EnergyAmmoType.fromLegacyMetadata(metadata);
        if (metadata == 70) return TauAmmoType.fromLegacyMetadata(metadata);
        if (metadata == 71 || metadata == 72) return CoilAmmoType.fromLegacyMetadata(metadata);
        if (metadata >= 58 && metadata <= 62) return RocketAmmoType.fromLegacyMetadata(metadata);
        if (metadata >= 41 && metadata <= 49) return Shotgun12GaugeAmmoType.fromLegacyMetadata(metadata);
        if (metadata == 50 || metadata >= 53 && metadata <= 57) {
            return FortyMillimeterAmmoType.fromLegacyMetadata(metadata);
        }
        if ((metadata >= 28 && metadata <= 32) || metadata == 82) {
            return SevenSixTwoAmmoType.fromLegacyMetadata(metadata);
        }
        return PepperboxAmmoType.fromLegacyMetadata(metadata);
    }
}
