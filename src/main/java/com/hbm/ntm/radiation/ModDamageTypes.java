package com.hbm.ntm.radiation;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> RADIATION = key("radiation");
    public static final ResourceKey<DamageType> ASBESTOS = key("asbestos");
    public static final ResourceKey<DamageType> BLACK_LUNG = key("black_lung");
    public static final ResourceKey<DamageType> DIGAMMA = key("digamma");
    public static final ResourceKey<DamageType> RUBBLE = key("rubble");
    public static final ResourceKey<DamageType> BLENDER = key("blender");
    public static final ResourceKey<DamageType> NUCLEAR_BLAST = key("nuclear_blast");
    public static final ResourceKey<DamageType> BULLET = key("bullet");
    public static final ResourceKey<DamageType> SHRAPNEL = key("shrapnel");
    public static final ResourceKey<DamageType> LASER = key("laser");
    public static final ResourceKey<DamageType> PLASMA = key("plasma");
    public static final ResourceKey<DamageType> BANG = key("bang");
    public static final ResourceKey<DamageType> BLACK_HOLE = key("blackhole");
    public static final ResourceKey<DamageType> MONOXIDE = key("monoxide");
    public static final ResourceKey<DamageType> MKU = key("mku");
    public static final ResourceKey<DamageType> ELECTRIC = key("electric");
    public static final ResourceKey<DamageType> AMS = key("ams");
    public static final ResourceKey<DamageType> AMS_CORE = key("ams_core");
    public static final ResourceKey<DamageType> HARD_LANDING = key("hard_landing");
    public static final ResourceKey<DamageType> FLAMETHROWER = key("flamethrower");
    public static final ResourceKey<DamageType> BUILDING = key("building");
    public static final ResourceKey<DamageType> SUBATOMIC = key("subatomic");
    public static final ResourceKey<DamageType> TAU_BLAST = key("tau_blast");

    private ModDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(
                Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path)
        );
    }
}
