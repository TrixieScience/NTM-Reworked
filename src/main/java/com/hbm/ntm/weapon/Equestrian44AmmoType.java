package com.hbm.ntm.weapon;

/** The same Head-Exploder cartridge summons a different payload in each legendary revolver. */
public enum Equestrian44AmmoType implements SednaAmmoType {
    BOXCAR(102, 0xFFFFD464, 0xFFFFFFFF),
    TORPEDO(103, 0xFF9E082E, 0xFFFF8A79);

    private final int bulletConfig;
    private final int darkColor;
    private final int lightColor;

    Equestrian44AmmoType(int bulletConfig, int darkColor, int lightColor) {
        this.bulletConfig = bulletConfig;
        this.darkColor = darkColor;
        this.lightColor = lightColor;
    }

    @Override public String serializedName() { return "m44_equestrian"; }
    @Override public int legacyMetadata() { return 2; }
    @Override public int legacyBulletConfig() { return bulletConfig; }
    @Override public float damageMultiplier() { return 0.0F; }
    @Override public float spread() { return 0.0F; }
    @Override public int projectiles() { return 1; }
    @Override public float ricochetAngle() { return 5.0F; }
    @Override public int maxRicochets() { return 0; }
    @Override public boolean penetrates() { return false; }
    @Override public boolean penetrationDamageFalloff() { return true; }
    @Override public int fallingPayload() { return this == BOXCAR ? 1 : 2; }
    @Override public int tracerDarkColor() { return darkColor; }
    @Override public int tracerLightColor() { return lightColor; }

    public static Equestrian44AmmoType fromLegacyBulletConfig(int config) {
        return config == TORPEDO.bulletConfig ? TORPEDO : BOXCAR;
    }
}
