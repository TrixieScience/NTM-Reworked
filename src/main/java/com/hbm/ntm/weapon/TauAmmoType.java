package com.hbm.ntm.weapon;

public enum TauAmmoType implements SednaAmmoType {
    DEPLETED_URANIUM("tau_uranium", 70);

    private final String serializedName;
    private final int legacyMetadata;

    TauAmmoType(String serializedName, int legacyMetadata) {
        this.serializedName = serializedName;
        this.legacyMetadata = legacyMetadata;
    }

    @Override public String serializedName() { return serializedName; }
    @Override public int legacyMetadata() { return legacyMetadata; }
    @Override public int legacyBulletConfig() { return legacyMetadata; }
    @Override public float damageMultiplier() { return 1.0F; }
    @Override public float spread() { return 0.0F; }
    @Override public int projectiles() { return 1; }
    @Override public float ricochetAngle() { return 0.0F; }
    @Override public int maxRicochets() { return 0; }
    @Override public boolean penetrates() { return true; }
    @Override public boolean penetrationDamageFalloff() { return false; }
    @Override public float wear() { return 1.0F; }

    public static TauAmmoType fromLegacyMetadata(int metadata) {
        return DEPLETED_URANIUM;
    }

    public static TauAmmoType fromLegacyBulletConfig(int config) {
        return DEPLETED_URANIUM;
    }
}
