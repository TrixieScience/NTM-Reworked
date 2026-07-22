package com.hbm.ntm.weapon;

/** The Debug Gun feeds the same cartridge into a bullet or six-shot receiver. */
public enum DebugAmmoType implements SednaAmmoType {
    BULLET("debug", 0, 110, 1, 0.01F),
    SHOT("debug_shot", 0, 111, 6, 0.05F);

    private final String name;
    private final int metadata;
    private final int config;
    private final int projectiles;
    private final float spread;

    DebugAmmoType(String name, int metadata, int config, int projectiles, float spread) {
        this.name = name;
        this.metadata = metadata;
        this.config = config;
        this.projectiles = projectiles;
        this.spread = spread;
    }

    @Override public String serializedName() { return name; }
    @Override public int legacyMetadata() { return metadata; }
    @Override public int legacyBulletConfig() { return config; }
    @Override public float damageMultiplier() { return 1.0F; }
    @Override public float spread() { return spread; }
    @Override public int projectiles() { return projectiles; }
    @Override public float ricochetAngle() { return 45.0F; }
    @Override public int maxRicochets() { return 1; }
    @Override public boolean penetrates() { return false; }
    @Override public boolean penetrationDamageFalloff() { return true; }
    @Override public float headshotMultiplier() { return 1.0F; }
    @Override public float armorThresholdNegation() { return 0.0F; }
    @Override public float armorPiercing() { return 0.0F; }
    @Override public float wear() { return 1.0F; }

    public static DebugAmmoType fromConfig(int config) {
        return config == SHOT.config ? SHOT : BULLET;
    }
}
