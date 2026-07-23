package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.weapon.FiftyCalAmmoType;
import com.hbm.ntm.weapon.FiveFiveSixAmmoType;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.TauAmmoType;
import com.hbm.ntm.weapon.TurretShellAmmoType;
import net.minecraft.world.item.ItemStack;

public enum TurretVariant {
    CHEKHOV("turret_chekhov", "container.turretChekhov", 10_000L, 100L,
            32D, 3D, -45F, 30F, 4.5F, 3F, 3.5D),
    FRIENDLY("turret_friendly", "container.turretFriendly", 10_000L, 100L,
            32D, 3D, -45F, 30F, 4.5F, 3F, 3.5D),
    JEREMY("turret_jeremy", "container.turretJeremy", 10_000L, 100L,
            80D, 16D, -45F, 30F, 4.5F, 3F, 4.25D),
    TAUON("turret_tauon", "container.turretTauon", 100_000L, 1_000L,
            128D, 3D, -35F, 35F, 9F, 6F, 1.9375D);

    private final String id;
    private final String titleKey;
    private final long maxPower;
    private final long consumption;
    private final double range;
    private final double grace;
    private final float minPitch;
    private final float maxPitch;
    private final float yawSpeed;
    private final float pitchSpeed;
    private final double barrelLength;

    TurretVariant(String id, String titleKey, long maxPower, long consumption,
                  double range, double grace, float minPitch, float maxPitch,
                  float yawSpeed, float pitchSpeed, double barrelLength) {
        this.id = id;
        this.titleKey = titleKey;
        this.maxPower = maxPower;
        this.consumption = consumption;
        this.range = range;
        this.grace = grace;
        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
        this.yawSpeed = yawSpeed;
        this.pitchSpeed = pitchSpeed;
        this.barrelLength = barrelLength;
    }

    public String id() { return id; }
    public String titleKey() { return titleKey; }
    public long maxPower() { return maxPower; }
    public long consumption() { return consumption; }
    public double range() { return range; }
    public double grace() { return grace; }
    public float minPitch() { return minPitch; }
    public float maxPitch() { return maxPitch; }
    public float yawSpeed() { return yawSpeed; }
    public float pitchSpeed() { return pitchSpeed; }
    public double barrelLength() { return barrelLength; }

    public boolean accepts(ItemStack stack) {
        if (this == JEREMY) {
            return stack.is(ModItems.AMMO_SHELL.get()) && TurretShellAmmoType.fromStack(stack) != null;
        }
        if (!stack.is(ModItems.AMMO_STANDARD.get())) return false;
        var ammo = StandardAmmoTypes.fromStack(stack);
        return switch (this) {
            case CHEKHOV -> ammo instanceof FiftyCalAmmoType type && !type.secret();
            case FRIENDLY -> ammo instanceof FiveFiveSixAmmoType;
            case TAUON -> ammo instanceof TauAmmoType;
            case JEREMY -> false;
        };
    }
}
