package com.hbm.ntm.entity;

import com.hbm.ntm.radiation.ModDamageTypes;
import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.EnergyAmmoType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

/** Instant Tesla beam with the old penetrating and low-wavelength split paths. */
public final class TeslaBeamEntity extends Projectile {
    public static final double RANGE = 250.0D;
    private static final EntityDataAccessor<Integer> AMMO =
            SynchedEntityData.defineId(TeslaBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(TeslaBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_LENGTH =
            SynchedEntityData.defineId(TeslaBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SUB =
            SynchedEntityData.defineId(TeslaBeamEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vector3f> DIRECTION =
            SynchedEntityData.defineId(TeslaBeamEntity.class, EntityDataSerializers.VECTOR3);

    public TeslaBeamEntity(EntityType<? extends TeslaBeamEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public TeslaBeamEntity(ServerLevel level, LivingEntity shooter, EnergyAmmoType ammo,
                           float damage, float spreadDegrees, Vec3 localOffset) {
        this(ModEntities.TESLA_BEAM.get(), level);
        setOwner(shooter);
        entityData.set(AMMO, ammo.legacyMetadata());
        entityData.set(DAMAGE, damage);
        float yaw = shooter.getYRot() + (float) random.nextGaussian() * spreadDegrees;
        float pitch = shooter.getXRot() + (float) random.nextGaussian() * spreadDegrees;
        point(yaw, pitch);
        setPos(shooter.getEyePosition().add(localOffset.xRot(-pitch * Mth.DEG_TO_RAD)
                .yRot(-yaw * Mth.DEG_TO_RAD)));
    }

    private TeslaBeamEntity(ServerLevel level, LivingEntity shooter, Vec3 origin, Vec3 target, float damage) {
        this(ModEntities.TESLA_BEAM.get(), level);
        setOwner(shooter);
        entityData.set(AMMO, EnergyAmmoType.LOW_WAVELENGTH.legacyMetadata());
        entityData.set(DAMAGE, damage);
        entityData.set(SUB, true);
        setPos(origin);
        Vec3 delta = target.subtract(origin);
        point((float) (Mth.atan2(-delta.x, delta.z) * Mth.RAD_TO_DEG),
                (float) (Mth.atan2(-delta.y, Math.sqrt(delta.x * delta.x + delta.z * delta.z)) * Mth.RAD_TO_DEG));
        entityData.set(BEAM_LENGTH, (float) delta.length());
    }

    private void point(float yaw, float pitch) {
        setYRot(yaw); setXRot(pitch); yRotO = yaw; xRotO = pitch;
        Vec3 direction = Vec3.directionFromRotation(pitch, yaw);
        entityData.set(DIRECTION, new Vector3f((float) direction.x, (float) direction.y, (float) direction.z));
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AMMO, EnergyAmmoType.STANDARD.legacyMetadata());
        builder.define(DAMAGE, 0.0F);
        builder.define(BEAM_LENGTH, (float) RANGE);
        builder.define(SUB, false);
        builder.define(DIRECTION, new Vector3f(0.0F, 0.0F, 1.0F));
    }

    public EnergyAmmoType ammoType() { return EnergyAmmoType.fromLegacyMetadata(entityData.get(AMMO)); }
    public float beamDamage() { return entityData.get(DAMAGE); }
    public float beamLength() { return entityData.get(BEAM_LENGTH); }
    public boolean subBeam() { return entityData.get(SUB); }

    public void performHitscan() {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 start = position();
        Vec3 direction = beamDirection().scale(subBeam() ? beamLength() : RANGE);
        Vec3 end = start.add(direction);
        BlockHitResult blockHit = server.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        Vec3 clippedEnd = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        List<BeamHit> hits = entityHits(server, start, clippedEnd);

        if (subBeam()) {
            for (BeamHit hit : hits) directDamage(server, hit.entity());
            return;
        }

        EnergyAmmoType ammo = ammoType();
        if (ammo.penetrates()) {
            for (BeamHit hit : hits) impact(server, hit.entity(), hit.location());
            if (blockHit.getType() != HitResult.Type.MISS) impact(server, null, blockImpact(blockHit));
            entityData.set(BEAM_LENGTH, (float) start.distanceTo(clippedEnd));
        } else {
            BeamHit nearest = hits.isEmpty() ? null : hits.getFirst();
            if (nearest != null) {
                impact(server, nearest.entity(), nearest.location());
                entityData.set(BEAM_LENGTH, (float) start.distanceTo(nearest.location()));
            } else if (blockHit.getType() != HitResult.Type.MISS) {
                Vec3 hit = blockImpact(blockHit);
                impact(server, null, hit);
                entityData.set(BEAM_LENGTH, (float) start.distanceTo(blockHit.getLocation()));
            }
        }
    }

    private List<BeamHit> entityHits(ServerLevel server, Vec3 start, Vec3 end) {
        List<BeamHit> hits = new ArrayList<>();
        AABB sweep = new AABB(start, end).inflate(1.0D);
        for (Entity candidate : server.getEntities(this, sweep, this::canHit)) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.3D).clip(start, end);
            hit.ifPresent(vec -> hits.add(new BeamHit(candidate, vec, start.distanceToSqr(vec))));
        }
        hits.sort(Comparator.comparingDouble(BeamHit::distance));
        return hits;
    }

    private boolean canHit(Entity entity) {
        return entity != getOwner() && entity.isAlive() && !entity.isSpectator() && entity.isPickable();
    }

    private static Vec3 blockImpact(BlockHitResult hit) {
        Direction direction = hit.getDirection();
        return hit.getLocation().add(Vec3.atLowerCornerOf(direction.getNormal()).scale(0.5D));
    }

    private void impact(ServerLevel server, Entity direct, Vec3 hit) {
        TeslaImpactEntity pulse = new TeslaImpactEntity(ModEntities.TESLA_IMPACT.get(), server);
        pulse.setPos(hit); pulse.setYRot(random.nextFloat() * 360.0F); server.addFreshEntity(pulse);
        server.playSound(null, hit.x, hit.y, hit.z, ModSounds.TESLA_BLAST.get(), SoundSource.HOSTILE,
                5.0F, 0.9F + random.nextFloat() * 0.2F);
        server.playSound(null, hit.x, hit.y, hit.z, SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.HOSTILE, 5.0F, 0.5F);

        electricBurst(server, hit);
        if (direct instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9));
            living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 9));
        }
        if (ammoType().chainLightning() && direct != null) split(server, direct, hit);
    }

    private void electricBurst(ServerLevel server, Vec3 hit) {
        Entity owner = getOwner();
        AABB area = new AABB(hit, hit).inflate(4.0D);
        for (Entity target : server.getEntities(this, area, Entity::isAlive)) {
            double distance = target.getBoundingBox().getCenter().distanceTo(hit);
            if (distance > 4.0D || exposure(server, hit, target) < 0.125F) continue;
            float density = (float) (1.0D - distance / 4.0D);
            float dealt = beamDamage() * density * (target == owner ? 0.5F : 1.0F);
            target.invulnerableTime = 0;
            target.hurt(server.damageSources().source(ModDamageTypes.ELECTRIC, this, owner), dealt);
            Vec3 push = target.getBoundingBox().getCenter().subtract(hit).normalize().scale(density);
            target.push(push.x, push.y, push.z);
        }
    }

    private float exposure(ServerLevel server, Vec3 hit, Entity target) {
        Vec3 center = target.getBoundingBox().getCenter();
        int clear = rayClear(server, hit, center) ? 1 : 0;
        for (Direction direction : Direction.values()) {
            Vec3 sample = center.add(Vec3.atLowerCornerOf(direction.getNormal()));
            if (rayClear(server, hit, sample)) clear++;
        }
        return clear / 7.0F;
    }

    private boolean rayClear(ServerLevel server, Vec3 from, Vec3 to) {
        return server.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private void split(ServerLevel server, Entity direct, Vec3 hit) {
        List<LivingEntity> targets = new ArrayList<>(server.getEntitiesOfClass(LivingEntity.class,
                new AABB(hit, hit).inflate(20.0D), target -> target != getOwner() && target != direct
                        && target.isAlive() && target.distanceToSqr(hit) <= 400.0D));
        java.util.Collections.shuffle(targets, new java.util.Random(random.nextLong()));
        LivingEntity shooter = getOwner() instanceof LivingEntity living ? living : null;
        if (shooter == null) return;
        for (LivingEntity target : targets) {
            Vec3 endpoint = target.getBoundingBox().getCenter();
            TeslaBeamEntity branch = new TeslaBeamEntity(server, shooter, hit, endpoint, beamDamage() * 0.5F);
            branch.performHitscan();
            server.addFreshEntity(branch);
        }
    }

    private void directDamage(ServerLevel server, Entity target) {
        target.invulnerableTime = 0;
        target.hurt(server.damageSources().source(ModDamageTypes.ELECTRIC, this, getOwner()), beamDamage());
    }

    public Vec3 beamDirection() {
        Vector3f direction = entityData.get(DIRECTION);
        Vec3 beam = new Vec3(direction.x, direction.y, direction.z);
        return beam.lengthSqr() > 1.0E-8D ? beam.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
    }

    @Override public void tick() {
        super.tick();
        if (tickCount >= (subBeam() ? 3 : 5)) discard();
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }

    private record BeamHit(Entity entity, Vec3 location, double distance) { }
}
