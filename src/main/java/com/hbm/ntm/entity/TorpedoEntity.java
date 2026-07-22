package com.hbm.ntm.entity;

import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class TorpedoEntity extends Projectile {
    public TorpedoEntity(EntityType<? extends TorpedoEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static TorpedoEntity spawn(ServerLevel level, Vec3 impact) {
        TorpedoEntity torpedo = new TorpedoEntity(ModEntities.TORPEDO.get(), level);
        torpedo.setPos(impact.x, impact.y + 50.0D, impact.z);
        level.addFreshEntity(torpedo);
        return torpedo;
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        ServerLevel server = (ServerLevel) level();
        if (tickCount == 1) {
            for (int i = 0; i < 15; i++) server.sendParticles(ModParticles.FLAMETHROWER_BALEFIRE.get(),
                    getX() + (random.nextDouble() - 0.5D) * 2.0D,
                    getY() + (random.nextDouble() - 0.5D),
                    getZ() + (random.nextDouble() - 0.5D) * 2.0D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        Vec3 motion = getDeltaMovement();
        setPos(position().add(motion));
        setDeltaMovement(motion.x, Math.max(-2.5D, motion.y - 0.04D), motion.z);
        if (!server.getBlockState(BlockPos.containing(position())).isAir()) {
            server.explode(this, getX(), getY(), getZ(), 20.0F, false, Level.ExplosionInteraction.TNT);
            discard();
        }
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 25_000.0D; }
}
