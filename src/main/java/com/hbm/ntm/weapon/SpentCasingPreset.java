package com.hbm.ntm.weapon;

public enum SpentCasingPreset {
    PISTOL_BRASS(Shape.STRAIGHT, 0xEBC35E, 0, 1.0F, 1.0F, 0.75F, Sound.SMALL),
    PISTOL_STEEL(Shape.STRAIGHT, 0x3E3E3E, 0, 1.0F, 1.0F, 0.75F, Sound.SMALL),
    RIFLE_BRASS(Shape.BOTTLENECK, 0xEBC35E, 0, 1.0F, 1.0F, 1.0F, Sound.SMALL),
    RIFLE_STEEL(Shape.BOTTLENECK, 0x3E3E3E, 0, 1.0F, 1.0F, 1.0F, Sound.SMALL),
    SHOTGUN_12_BLACK_POWDER(Shape.SHOTGUN, 0xEBC35E, 0xEBC35E, 0.75F, 0.75F, 0.75F, Sound.SHELL),
    SHOTGUN_12_BUCKSHOT(Shape.SHOTGUN, 0xB52B2B, 0xEBC35E, 0.75F, 0.75F, 0.75F, Sound.SHELL),
    SHOTGUN_12_SLUG(Shape.SHOTGUN, 0x393939, 0xEBC35E, 0.75F, 0.75F, 0.75F, Sound.SHELL),
    SHOTGUN_12_FLECHETTE(Shape.SHOTGUN, 0x3C80F0, 0xEBC35E, 0.75F, 0.75F, 0.75F, Sound.SHELL),
    SHOTGUN_12_MAGNUM(Shape.SHOTGUN, 0x278400, 0x757575, 0.75F, 0.75F, 0.75F, Sound.SHELL),
    SHOTGUN_12_EXPLOSIVE(Shape.SHOTGUN, 0xDA4127, 0x757575, 0.75F, 0.75F, 0.75F, Sound.SHELL),
    SHOTGUN_12_PHOSPHORUS(Shape.SHOTGUN, 0x910001, 0x757575, 0.75F, 0.75F, 0.75F, Sound.SHELL),
    SHOTGUN_10_BUCKSHOT(Shape.SHOTGUN, 0xB52B2B, 0x757575, 1.0F, 1.0F, 1.0F, Sound.SHELL),
    SHOTGUN_10_SHRAPNEL(Shape.SHOTGUN, 0xE5DD00, 0x757575, 1.0F, 1.0F, 1.0F, Sound.SHELL),
    SHOTGUN_10_DU(Shape.SHOTGUN, 0x538D53, 0x757575, 1.0F, 1.0F, 1.0F, Sound.SHELL),
    SHOTGUN_10_SLUG(Shape.SHOTGUN, 0x808080, 0x757575, 1.0F, 1.0F, 1.0F, Sound.SHELL),
    SHOTGUN_10_EXPLOSIVE(Shape.SHOTGUN, 0xFAC943, 0x757575, 1.0F, 1.0F, 1.0F, Sound.SHELL),
    SHELL_240MM(Shape.BOTTLENECK, 0xEBC35E, 0, 7.5F, 7.5F, 7.5F, Sound.LARGE);

    private final Shape shape;
    private final int bodyColor;
    private final int baseColor;
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final Sound sound;

    SpentCasingPreset(Shape shape, int bodyColor, int baseColor,
                      float scaleX, float scaleY, float scaleZ, Sound sound) {
        this.shape = shape;
        this.bodyColor = bodyColor;
        this.baseColor = baseColor;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.sound = sound;
    }

    public Shape shape() { return shape; }
    public int bodyColor() { return bodyColor; }
    public int baseColor() { return baseColor; }
    public float scaleX() { return scaleX; }
    public float scaleY() { return scaleY; }
    public float scaleZ() { return scaleZ; }
    public Sound sound() { return sound; }

    public static SpentCasingPreset forNineMillimeter(NineMillimeterAmmoType ammo) {
        return ammo == NineMillimeterAmmoType.ARMOR_PIERCING ? PISTOL_STEEL : PISTOL_BRASS;
    }

    public static SpentCasingPreset forSevenSixTwo(SevenSixTwoAmmoType ammo) {
        return switch (ammo) {
            case ARMOR_PIERCING, DEPLETED_URANIUM, HIGH_EXPLOSIVE -> RIFLE_STEEL;
            default -> RIFLE_BRASS;
        };
    }

    public static SpentCasingPreset forTwelveGauge(Shotgun12GaugeAmmoType ammo) {
        return switch (ammo) {
            case BLACK_POWDER_BUCKSHOT, BLACK_POWDER_MAGNUM, BLACK_POWDER_SLUG -> SHOTGUN_12_BLACK_POWDER;
            case BUCKSHOT -> SHOTGUN_12_BUCKSHOT;
            case SLUG -> SHOTGUN_12_SLUG;
            case FLECHETTE -> SHOTGUN_12_FLECHETTE;
            case MAGNUM -> SHOTGUN_12_MAGNUM;
            case EXPLOSIVE -> SHOTGUN_12_EXPLOSIVE;
            case PHOSPHORUS -> SHOTGUN_12_PHOSPHORUS;
        };
    }

    public static SpentCasingPreset forTenGauge(Shotgun10GaugeAmmoType ammo) {
        return switch (ammo) {
            case BUCKSHOT -> SHOTGUN_10_BUCKSHOT;
            case SHRAPNEL -> SHOTGUN_10_SHRAPNEL;
            case DEPLETED_URANIUM -> SHOTGUN_10_DU;
            case SLUG -> SHOTGUN_10_SLUG;
            case EXPLOSIVE -> SHOTGUN_10_EXPLOSIVE;
        };
    }

    public enum Shape { STRAIGHT, BOTTLENECK, SHOTGUN }
    public enum Sound { SMALL, MEDIUM, LARGE, SHELL }
}
