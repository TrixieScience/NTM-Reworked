package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.weapon.FiftyCalAmmoType;
import com.hbm.ntm.weapon.FiveFiveSixAmmoType;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.TauAmmoType;
import com.hbm.ntm.weapon.TurretShellAmmoType;
import com.hbm.ntm.weapon.FlamerFuelType;
import com.hbm.ntm.weapon.RocketAmmoType;
import com.hbm.ntm.weapon.NineMillimeterAmmoType;
import com.hbm.ntm.item.MachineUpgradeItem;
import net.minecraft.world.item.ItemStack;

public enum TurretVariant {
    CHEKHOV("turret_chekhov", "container.turretChekhov", 10_000L, 100L,
            32D, 3D, -45F, 30F, 4.5F, 3F, 3.5D),
    FRIENDLY("turret_friendly", "container.turretFriendly", 10_000L, 100L,
            32D, 3D, -45F, 30F, 4.5F, 3F, 3.5D),
    JEREMY("turret_jeremy", "container.turretJeremy", 10_000L, 100L,
            80D, 16D, -45F, 30F, 4.5F, 3F, 4.25D),
    TAUON("turret_tauon", "container.turretTauon", 100_000L, 1_000L,
            128D, 3D, -35F, 35F, 9F, 6F, 1.9375D),
    RICHARD("turret_richard", "container.turretRichard", 10_000L, 100L,
            64D, 8D, -25F, 25F, 4.5F, 3F, 1.25D),
    HOWARD("turret_howard", "container.turretHoward", 50_000L, 500L,
            250D, 3D, -50F, 90F, 12F, 8F, 3.25D),
    FRITZ("turret_fritz", "container.turretFritz", 10_000L, 100L,
            48D, 2D, -45F, 45F, 4.5F, 3F, 2.25D),
    MAXWELL("turret_maxwell", "container.turretMaxwell", 10_000_000L, 10_000L,
            64D, 5D, -35F, 40F, 9F, 6F, 2.125D),
    ARTY("turret_arty", "container.turretArty", 100_000L, 100L,
            3_000D, 250D, -90F, 30F, 1F, 0.5F, 9D),
    HIMARS("turret_himars", "container.turretHIMARS", 1_000_000L, 100L,
            5_000D, 250D, -90F, 0F, 1F, 0.5F, 0.5D),
    SENTRY("turret_sentry", "container.turretSentry", 1_000L, 5L,
            24D, 2D, -20F, 20F, 4.5F, 3F, 1.25D);

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
        if (this == HOWARD) return stack.is(ModItems.AMMO_DGK.get());
        if (this == ARTY) return stack.is(ModItems.AMMO_ARTY.get());
        if (this == HIMARS) return stack.is(ModItems.AMMO_HIMARS.get());
        if (this == MAXWELL) {
            return stack.is(ModItems.UPGRADE_5G.get()) || stack.is(ModItems.UPGRADE_SCREM.get())
                    || stack.getItem() instanceof MachineUpgradeItem upgrade
                    && switch (upgrade.type()) {
                        case SPEED, POWER, EFFECT, AFTERBURN, OVERDRIVE -> true;
                        default -> false;
                    };
        }
        if (!stack.is(ModItems.AMMO_STANDARD.get())) return false;
        var ammo = StandardAmmoTypes.fromStack(stack);
        return switch (this) {
            case CHEKHOV -> ammo instanceof FiftyCalAmmoType type && !type.secret();
            case FRIENDLY -> ammo instanceof FiveFiveSixAmmoType;
            case TAUON -> ammo instanceof TauAmmoType;
            case RICHARD -> ammo instanceof RocketAmmoType;
            case FRITZ -> ammo instanceof FlamerFuelType;
            case SENTRY -> ammo instanceof NineMillimeterAmmoType;
            case JEREMY, HOWARD, MAXWELL, ARTY, HIMARS -> false;
        };
    }
}
