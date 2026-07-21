package com.hbm.ntm.compat;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.explosion.RemoteDetonatable;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;

/** Teaches moving Sable bombs that the ground is a detonator: a hard enough impact arms them. */
public final class SableBombImpactFuse implements BlockSubLevelCollisionCallback {
    public static final SableBombImpactFuse INSTANCE = new SableBombImpactFuse();

    private SableBombImpactFuse() { }

    // Impact fusing is purely speed-based now, so the assembly-move hooks the mixin still calls have
    // no release altitude to record. Kept as no-ops so the Sable listener contract stays satisfied.
    public static void beforeMove(ServerLevel sourceLevel, BlockPos sourcePosition,
                                  BlockPos targetPosition) { }

    public static void afterMove(ServerLevel targetLevel, BlockPos targetPosition) { }

    @Override
    public CollisionResult sable$onCollision(BlockPos movingPosition, BlockPos otherPosition,
                                             Vector3d contactPoint, double impactSpeed) {
        try {
            return handleCollision(movingPosition, impactSpeed);
        } catch (RuntimeException exception) {
            // Do not throw through Rapier unless a poisoned mutex and JVM funeral sound enjoyable.
            HbmNtm.LOGGER.error("Failed to process Sable bomb impact at {}", movingPosition, exception);
            return CollisionResult.NONE;
        }
    }

    private CollisionResult handleCollision(BlockPos movingPosition, double impactSpeed) {
        if (!BombImpactFusePhysics.shouldDetonate(impactSpeed)) return CollisionResult.NONE;

        SubLevelPhysicsSystem physics = SubLevelPhysicsSystem.getCurrentlySteppingSystem();
        if (physics == null) return CollisionResult.NONE;
        ServerLevel level = physics.getLevel();

        BlockState state = level.getBlockState(movingPosition);
        if (!(state.getBlock() instanceof RemoteDetonatable)) return CollisionResult.NONE;
        // Rapier is locked here. Detonate later unless mutex poisoning is the intended payload.
        BombImpactFuseEvents.queue(level, movingPosition, () -> {
            BlockState currentState = level.getBlockState(movingPosition);
            if (currentState.getBlock() instanceof RemoteDetonatable currentBomb) {
                currentBomb.detonateRemotely(level, movingPosition);
            }
        });
        return new CollisionResult(new Vector3d(), true);
    }
}
