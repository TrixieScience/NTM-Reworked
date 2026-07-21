package com.hbm.ntm.weapon;

/** The three capacitor loads shared by the source Tesla Cannon and laser weapons. */
public enum EnergyAmmoType implements SednaAmmoType {
    STANDARD("capacitor", 67, 67, 1.0F, true, false),
    OVERCHARGE("capacitor_overcharge", 68, 68, 1.5F, true, false),
    LOW_WAVELENGTH("capacitor_ir", 69, 69, 0.8F, false, true);

    private final String serializedName;
    private final int legacyMetadata;
    private final int bulletConfig;
    private final float damageMultiplier;
    private final boolean penetrates;
    private final boolean chainLightning;

    EnergyAmmoType(String serializedName, int legacyMetadata, int bulletConfig,
                   float damageMultiplier, boolean penetrates, boolean chainLightning) {
        this.serializedName = serializedName;
        this.legacyMetadata = legacyMetadata;
        this.bulletConfig = bulletConfig;
        this.damageMultiplier = damageMultiplier;
        this.penetrates = penetrates;
        this.chainLightning = chainLightning;
    }

    @Override public String serializedName() { return serializedName; }
    @Override public int legacyMetadata() { return legacyMetadata; }
    @Override public int legacyBulletConfig() { return bulletConfig; }
    @Override public float damageMultiplier() { return damageMultiplier; }
    @Override public float spread() { return 0.0F; }
    @Override public int projectiles() { return 1; }
    @Override public float ricochetAngle() { return 0.0F; }
    @Override public int maxRicochets() { return 0; }
    @Override public boolean penetrates() { return penetrates; }
    @Override public boolean penetrationDamageFalloff() { return false; }
    @Override public float headshotMultiplier() { return 1.0F; }
    @Override public float armorThresholdNegation() { return 0.0F; }
    @Override public float armorPiercing() { return 0.0F; }
    @Override public float wear() { return 1.0F; }

    public boolean chainLightning() { return chainLightning; }
    public boolean laserPenetrates() { return this == OVERCHARGE; }
    public boolean laserFire() { return this == LOW_WAVELENGTH; }

    public static EnergyAmmoType fromLegacyMetadata(int metadata) {
        for (EnergyAmmoType type : values()) if (type.legacyMetadata == metadata) return type;
        return STANDARD;
    }

    public static EnergyAmmoType fromLegacyBulletConfig(int config) {
        for (EnergyAmmoType type : values()) if (type.bulletConfig == config) return type;
        return STANDARD;
    }
}
