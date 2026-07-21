package com.hbm.ntm.compat;

/** Sable-free impact-fuse policy shared by the optional Aeronautics integration and GameTests. */
public final class BombImpactFusePhysics {
    /** Matches Sable's own impact-prime threshold for vanilla TNT. */
    public static final double MIN_IMPACT_SPEED = 5.0D;

    private BombImpactFusePhysics() { }

    /**
     * A hard enough impact arms the bomb, no matter how far it fell -- so a launched or staff-thrown
     * contraption detonates on a fast slam. Gentle taxiing and deck bumps stay under the threshold.
     */
    public static boolean shouldDetonate(double impactSpeed) {
        return Double.isFinite(impactSpeed) && Math.abs(impactSpeed) >= MIN_IMPACT_SPEED;
    }
}
