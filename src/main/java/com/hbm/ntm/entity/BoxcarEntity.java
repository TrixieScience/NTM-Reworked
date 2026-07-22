package com.hbm.ntm.entity;

import com.hbm.ntm.radiation.ModDamageTypes;
import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModParticles;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BoxcarEntity extends Projectile {
    public static final double SPAWN_HEIGHT = 50.0D;
    private boolean landed;

    public BoxcarEntity(EntityType<? extends BoxcarEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static BoxcarEntity spawn(ServerLevel level, Vec3 impact) {
        BoxcarEntity boxcar = new BoxcarEntity(ModEntities.BOXCAR.get(), level);
        boxcar.setPos(impact.x, impact.y + SPAWN_HEIGHT, impact.z);
        level.addFreshEntity(boxcar);
        level.playSound(null, boxcar.getX(), boxcar.getY() + 50.0D, boxcar.getZ(),
                ModSounds.TRAIN_HORN.get(), SoundSource.PLAYERS, 100.0F, 1.0F);
        return boxcar;
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || landed) return;
        ServerLevel server = (ServerLevel) level();
        if (tickCount == 1) {
            for (int i = 0; i < 50; i++) server.sendParticles(ModParticles.FLAMETHROWER_BALEFIRE.get(),
                    getX() + (random.nextDouble() - 0.5D) * 3.0D,
                    getY() + (random.nextDouble() - 0.5D) * 15.0D,
                    getZ() + (random.nextDouble() - 0.5D) * 3.0D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        Vec3 motion = getDeltaMovement();
        setPos(position().add(motion));
        setDeltaMovement(motion.x, Math.max(-1.5D, motion.y - 0.03D), motion.z);
        if (!server.getBlockState(BlockPos.containing(position())).isAir()) land(server);
    }

    private void land(ServerLevel level) {
        landed = true;
        setDeltaMovement(Vec3.ZERO);
        level.playSound(null, getX(), getY(), getZ(), ModSounds.TRAIN_IMPACT.get(),
                SoundSource.PLAYERS, 100.0F, 1.0F);
        AABB area = new AABB(getX() - 2.0D, getY() - 2.0D, getZ() - 2.0D,
                getX() + 2.0D, getY() + 2.0D, getZ() + 2.0D);
        for (Entity entity : level.getEntities(this, area, Entity::isAlive)) {
            entity.hurt(level.damageSources().source(ModDamageTypes.BUILDING, this), 1_000.0F);
        }
        shock(level, 3.0D);
        shock(level, 2.5D);
        shock(level, 2.0D);
    }

    private void shock(ServerLevel level, double strength) {
        double start = random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < 24; i++) {
            double angle = start + Math.PI * 2.0D * i / 24.0D;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                    getX(), getY() + 1.0D, getZ(), 0,
                    Math.cos(angle) * strength, 0.0D, -Math.sin(angle) * strength, 1.0D);
        }
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) { tag.putBoolean("landed", landed); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { landed = tag.getBoolean("landed"); }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 25_000.0D; }
}
