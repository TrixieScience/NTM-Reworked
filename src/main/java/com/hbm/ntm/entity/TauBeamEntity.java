package com.hbm.ntm.entity;

import com.hbm.ntm.radiation.ModDamageTypes;
import com.hbm.ntm.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class TauBeamEntity extends Projectile {
    public static final double RANGE = 250.0D;
    public static final int LIFETIME = 5;

    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(TauBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_LENGTH =
            SynchedEntityData.defineId(TauBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> DIRECTION =
            SynchedEntityData.defineId(TauBeamEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> CHARGED =
            SynchedEntityData.defineId(TauBeamEntity.class, EntityDataSerializers.BOOLEAN);

    public TauBeamEntity(EntityType<? extends TauBeamEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public TauBeamEntity(ServerLevel level, LivingEntity shooter, float damage,
                         float spreadDegrees, Vec3 localOffset, boolean charged) {
        this(ModEntities.TAU_BEAM.get(), level);
        setOwner(shooter);
        entityData.set(DAMAGE, damage);
        entityData.set(CHARGED, charged);
        float yaw = shooter.getYRot() + (float) random.nextGaussian() * spreadDegrees;
        float pitch = shooter.getXRot() + (float) random.nextGaussian() * spreadDegrees;
        point(yaw, pitch);
        setPos(shooter.getEyePosition().add(localOffset.xRot(-pitch * Mth.DEG_TO_RAD)
                .yRot(-yaw * Mth.DEG_TO_RAD)));
    }

    private void point(float yaw, float pitch) {
        setYRot(yaw);
        setXRot(pitch);
        yRotO = yaw;
        xRotO = pitch;
        Vec3 direction = Vec3.directionFromRotation(pitch, yaw);
        entityData.set(DIRECTION, new Vector3f((float) direction.x, (float) direction.y, (float) direction.z));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DAMAGE, 0.0F);
        builder.define(BEAM_LENGTH, (float) RANGE);
        builder.define(DIRECTION, new Vector3f(0.0F, 0.0F, 1.0F));
        builder.define(CHARGED, false);
    }

    public float beamDamage() { return entityData.get(DAMAGE); }
    public float beamLength() { return entityData.get(BEAM_LENGTH); }
    public boolean charged() { return entityData.get(CHARGED); }

    public void performHitscan() {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 start = position();
        Vec3 end = start.add(beamDirection().scale(RANGE));
        BlockHitResult blockHit = charged()
                ? BlockHitResult.miss(end, net.minecraft.core.Direction.UP, net.minecraft.core.BlockPos.containing(end))
                : server.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, this));
        Vec3 clippedEnd = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();

        for (BeamHit hit : entityHits(server, start, clippedEnd)) {
            hit.entity().invulnerableTime = 0;
            hit.entity().hurt(server.damageSources().source(ModDamageTypes.SUBATOMIC, this, getOwner()),
                    beamDamage());
        }
        entityData.set(BEAM_LENGTH, (float) start.distanceTo(clippedEnd));
    }

    private List<BeamHit> entityHits(ServerLevel server, Vec3 start, Vec3 end) {
        List<BeamHit> hits = new ArrayList<>();
        AABB sweep = new AABB(start, end).inflate(1.0D);
        for (Entity candidate : server.getEntities(this, sweep, this::canHit)) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.3D).clip(start, end);
            hit.ifPresent(location -> hits.add(new BeamHit(candidate, start.distanceToSqr(location))));
        }
        hits.sort(Comparator.comparingDouble(BeamHit::distance));
        return hits;
    }

    private boolean canHit(Entity entity) {
        return entity != getOwner() && entity.isAlive() && !entity.isSpectator() && entity.isPickable();
    }

    public Vec3 beamDirection() {
        Vector3f direction = entityData.get(DIRECTION);
        Vec3 beam = new Vec3(direction.x, direction.y, direction.z);
        return beam.lengthSqr() > 1.0E-8D ? beam.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
    }

    @Override public void tick() {
        super.tick();
        if (tickCount >= LIFETIME) discard();
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }

    private record BeamHit(Entity entity, double distance) { }
}
