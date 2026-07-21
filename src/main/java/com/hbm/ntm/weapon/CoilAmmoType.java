package com.hbm.ntm.weapon;

public enum CoilAmmoType implements SednaAmmoType {
    TUNGSTEN("coil_tungsten", 71, 1.25F),
    FERROURANIUM("coil_ferrouranium", 72, 2.5F);

    private final String serializedName;
    private final int legacyMetadata;
    private final float blockBreakHardness;

    CoilAmmoType(String serializedName, int legacyMetadata, float blockBreakHardness) {
        this.serializedName = serializedName;
        this.legacyMetadata = legacyMetadata;
        this.blockBreakHardness = blockBreakHardness;
    }

    @Override public String serializedName() { return serializedName; }
    @Override public int legacyMetadata() { return legacyMetadata; }
    @Override public int legacyBulletConfig() { return legacyMetadata; }
    @Override public float damageMultiplier() { return 1.0F; }
    @Override public float spread() { return 0.0F; }
    @Override public int projectiles() { return 1; }
    @Override public float ricochetAngle() { return 5.0F; }
    @Override public int maxRicochets() { return 2; }
    @Override public boolean penetrates() { return true; }
    @Override public boolean penetrationDamageFalloff() { return false; }
    @Override public boolean spectral() { return true; }
    @Override public double projectileSpeed() { return 7.5D; }
    @Override public int projectileLifetime() { return 50; }
    @Override public float blockBreakHardness() { return blockBreakHardness; }

    public static CoilAmmoType fromLegacyMetadata(int metadata) {
        return metadata == FERROURANIUM.legacyMetadata ? FERROURANIUM : TUNGSTEN;
    }

    public static CoilAmmoType fromLegacyBulletConfig(int config) {
        return fromLegacyMetadata(config);
    }
}
