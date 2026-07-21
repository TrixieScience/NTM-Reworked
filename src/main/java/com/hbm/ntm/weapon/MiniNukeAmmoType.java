package com.hbm.ntm.weapon;

public enum MiniNukeAmmoType implements SednaAmmoType {
    STANDARD("nuke_standard", 73, Kind.STANDARD, 1.0F, 3.0D, 0.0F, 1),
    DEMOLITION("nuke_demo", 74, Kind.DEMOLITION, 1.0F, 3.0D, 0.0F, 1),
    HIGH_YIELD("nuke_high", 75, Kind.HIGH_YIELD, 1.0F, 3.0D, 0.0F, 1),
    TINY_TOTS("nuke_tots", 76, Kind.TINY_TOTS, 0.35F, 3.0D, 0.1F, 8),
    HIVE("nuke_hive", 77, Kind.HIVE, 0.25F, 1.0D, 0.15F, 12),
    BALEFIRE("nuke_balefire", 93, Kind.BALEFIRE, 2.5F, 3.0D, 0.0F, 1);

    public static final int LIFE_TICKS = 300;
    public static final float GRAVITY = 0.025F;

    private final String serializedName;
    private final int legacyMetadata;
    private final Kind kind;
    private final float damageMultiplier;
    private final double speed;
    private final float spread;
    private final int projectiles;

    MiniNukeAmmoType(String serializedName, int legacyMetadata, Kind kind,
                     float damageMultiplier, double speed, float spread, int projectiles) {
        this.serializedName = serializedName;
        this.legacyMetadata = legacyMetadata;
        this.kind = kind;
        this.damageMultiplier = damageMultiplier;
        this.speed = speed;
        this.spread = spread;
        this.projectiles = projectiles;
    }

    @Override public String serializedName() { return serializedName; }
    @Override public int legacyMetadata() { return legacyMetadata; }
    @Override public int legacyBulletConfig() { return legacyMetadata; }
    @Override public float damageMultiplier() { return damageMultiplier; }
    @Override public float spread() { return spread; }
    @Override public int projectiles() { return projectiles; }
    @Override public float ricochetAngle() { return 5.0F; }
    @Override public int maxRicochets() { return 0; }
    @Override public boolean penetrates() { return false; }
    @Override public boolean penetrationDamageFalloff() { return true; }
    @Override public double projectileSpeed() { return speed; }
    @Override public int projectileLifetime() { return LIFE_TICKS; }

    public Kind kind() { return kind; }
    public float gravity() { return GRAVITY; }

    public static MiniNukeAmmoType fromLegacyMetadata(int metadata) {
        for (MiniNukeAmmoType type : values()) if (type.legacyMetadata == metadata) return type;
        return STANDARD;
    }

    public enum Kind { STANDARD, DEMOLITION, HIGH_YIELD, TINY_TOTS, HIVE, BALEFIRE }
}
