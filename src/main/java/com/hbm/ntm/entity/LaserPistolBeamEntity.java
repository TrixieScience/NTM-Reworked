package com.hbm.ntm.entity;

import com.hbm.ntm.radiation.ModDamageTypes;
import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.weapon.EnergyAmmoType;
import com.hbm.ntm.weapon.WeaponStatusEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Five-tick beam shared by the Laser Pistol family's capacitor profiles. */
public final class LaserPistolBeamEntity extends Projectile {
    public static final double RANGE = 250.0D;
    public static final int LIFETIME = 5;

    private static final EntityDataAccessor<Integer> AMMO =
            SynchedEntityData.defineId(LaserPistolBeamEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(LaserPistolBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_LENGTH =
            SynchedEntityData.defineId(LaserPistolBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Vector3f> DIRECTION =
            SynchedEntityData.defineId(LaserPistolBeamEntity.class, EntityDataSerializers.VECTOR3);
    private static final EntityDataAccessor<Boolean> EMERALD =
            SynchedEntityData.defineId(LaserPistolBeamEntity.class, EntityDataSerializers.BOOLEAN);

    public LaserPistolBeamEntity(EntityType<? extends LaserPistolBeamEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public LaserPistolBeamEntity(ServerLevel level, LivingEntity shooter, EnergyAmmoType ammo,
                                 float damage, float spreadDegrees, Vec3 localOffset) {
        this(level, shooter, ammo, damage, spreadDegrees, localOffset, false);
    }

    public LaserPistolBeamEntity(ServerLevel level, LivingEntity shooter, EnergyAmmoType ammo,
                                 float damage, float spreadDegrees, Vec3 localOffset, boolean emerald) {
        this(ModEntities.LASER_PISTOL_BEAM.get(), level);
        setOwner(shooter);
        entityData.set(AMMO, ammo.legacyMetadata());
        entityData.set(DAMAGE, damage);
        entityData.set(EMERALD, emerald);
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
        builder.define(AMMO, EnergyAmmoType.STANDARD.legacyMetadata());
        builder.define(DAMAGE, 0.0F);
        builder.define(BEAM_LENGTH, (float) RANGE);
        builder.define(DIRECTION, new Vector3f(0.0F, 0.0F, 1.0F));
        builder.define(EMERALD, false);
    }

    public EnergyAmmoType ammoType() { return EnergyAmmoType.fromLegacyMetadata(entityData.get(AMMO)); }
    public float beamDamage() { return entityData.get(DAMAGE); }
    public float beamLength() { return entityData.get(BEAM_LENGTH); }
    public boolean emerald() { return entityData.get(EMERALD); }
    public float armorPiercing() { return emerald() ? 0.5F : 0.0F; }
    public float armorThresholdNegation() {
        if (!emerald()) return 0.0F;
        return ammoType() == EnergyAmmoType.OVERCHARGE ? 15.0F : 10.0F;
    }

    public void performHitscan() {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 start = position();
        Vec3 end = start.add(beamDirection().scale(RANGE));
        BlockHitResult blockHit = server.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        Vec3 clippedEnd = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        List<BeamHit> hits = entityHits(server, start, clippedEnd);

        if (ammoType().laserPenetrates()) {
            for (BeamHit hit : hits) impactEntity(server, hit.entity());
            if (blockHit.getType() != HitResult.Type.MISS) impactBlock(server, blockHit);
            entityData.set(BEAM_LENGTH, (float) start.distanceTo(clippedEnd));
            return;
        }

        BeamHit nearest = hits.isEmpty() ? null : hits.getFirst();
        if (nearest != null) {
            impactEntity(server, nearest.entity());
            entityData.set(BEAM_LENGTH, (float) start.distanceTo(nearest.location()));
        } else if (blockHit.getType() != HitResult.Type.MISS) {
            impactBlock(server, blockHit);
            entityData.set(BEAM_LENGTH, (float) start.distanceTo(blockHit.getLocation()));
        } else {
            entityData.set(BEAM_LENGTH, (float) RANGE);
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

    private void impactEntity(ServerLevel server, Entity target) {
        target.invulnerableTime = 0;
        var damageSource = ammoType().laserFire()
                ? server.damageSources().source(ModDamageTypes.FLAMETHROWER, this, getOwner())
                : server.damageSources().source(ModDamageTypes.LASER, this, getOwner());
        float appliedDamage = target instanceof LivingEntity living
                ? compensateForArmorPiercing(living, damageSource, beamDamage(),
                        armorThresholdNegation(), armorPiercing())
                : beamDamage();
        target.hurt(damageSource, appliedDamage);
        if (ammoType().laserFire() && target instanceof LivingEntity living) {
            WeaponStatusEvents.applyFire(living, 100);
        }
    }

    private void impactBlock(ServerLevel server, BlockHitResult hit) {
        if (!ammoType().laserFire()) return;
        BlockPos blockPos = hit.getBlockPos();
        Direction direction = hit.getDirection();
        BlockState state = server.getBlockState(blockPos);
        BlockPos firePos = blockPos.relative(direction);
        if (state.isFlammable(server, blockPos, direction.getOpposite())
                && server.getBlockState(firePos).isAir()) {
            server.setBlock(firePos, BaseFireBlock.getState(server, firePos), 11);
            return;
        }
        Vec3 position = hit.getLocation();
        server.addFreshEntity(new LingeringFireEntity(ModEntities.LINGERING_FIRE.get(), server,
                position.x, position.y, position.z, 100, 2.0D, 1.0D, LingeringFireEntity.Kind.FIRE));
    }

    public Vec3 beamDirection() {
        Vector3f direction = entityData.get(DIRECTION);
        Vec3 beam = new Vec3(direction.x, direction.y, direction.z);
        return beam.lengthSqr() > 1.0E-8D ? beam.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
    }

    private static float compensateForArmorPiercing(LivingEntity living, DamageSource source,
                                                     float intendedDamage, float thresholdNegation,
                                                     float armorPiercing) {
        if ((armorPiercing == 0.0F && thresholdNegation == 0.0F) || living.getArmorValue() <= 0) {
            return intendedDamage;
        }
        float armor = living.getArmorValue();
        float toughness = (float) living.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float effectiveArmor = Math.max(0.0F, armor - thresholdNegation)
                * Mth.clamp(1.0F - armorPiercing, 0.0F, 2.0F);
        float targetAfterArmor = CombatRules.getDamageAfterAbsorb(
                living, intendedDamage, source, effectiveArmor, toughness);
        float low = 0.0F;
        float high = Math.max(intendedDamage * 4.0F, intendedDamage + armor + 1.0F);
        while (CombatRules.getDamageAfterAbsorb(living, high, source, armor, toughness) < targetAfterArmor
                && high < 4096.0F) {
            high *= 2.0F;
        }
        for (int i = 0; i < 24; i++) {
            float mid = (low + high) * 0.5F;
            float result = CombatRules.getDamageAfterAbsorb(living, mid, source, armor, toughness);
            if (result < targetAfterArmor) low = mid;
            else high = mid;
        }
        return (low + high) * 0.5F;
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount >= LIFETIME) discard();
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }

    private record BeamHit(Entity entity, Vec3 location, double distance) { }
}
