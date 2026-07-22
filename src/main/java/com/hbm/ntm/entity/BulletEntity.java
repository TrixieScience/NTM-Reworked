package com.hbm.ntm.entity;

import com.hbm.ntm.network.ChargeBlastPayload;
import com.hbm.ntm.radiation.ModDamageTypes;
import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.SednaAmmoType;
import com.hbm.ntm.weapon.StandardAmmoTypes;
import com.hbm.ntm.weapon.WeaponStatusEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

/** Swept Sedna projectile used by black-powder weapons. */
public final class BulletEntity extends Projectile {
    private static final EntityDataAccessor<Integer> AMMO =
            SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE =
            SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> INCENDIARY =
            SynchedEntityData.defineId(BulletEntity.class, EntityDataSerializers.BOOLEAN);

    private int ricochets;
    private double tracerVelocity;
    private double previousTracerVelocity;
    private int turnProgress;
    private double syncPosX;
    private double syncPosY;
    private double syncPosZ;
    private double syncYaw;
    private double syncPitch;

    public BulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public BulletEntity(ServerLevel level, LivingEntity owner, SednaAmmoType ammo, float damage,
                        float spread, Vec3 origin, Vec3 heading) {
        this(level, owner, ammo, damage, spread, origin, heading, false);
    }

    public BulletEntity(ServerLevel level, LivingEntity owner, SednaAmmoType ammo, float damage,
                        float spread, Vec3 origin, Vec3 heading, boolean incendiary) {
        this(ModEntities.BULLET.get(), level);
        setOwner(owner);
        // Secret .50 ammo overlaps ordinary metadata, so sync its config ID instead.
        int projectileAmmo = ammo instanceof com.hbm.ntm.weapon.FiftyCalAmmoType fifty && fifty.secret()
                || ammo instanceof com.hbm.ntm.weapon.Equestrian44AmmoType
                ? ammo.legacyBulletConfig() : ammo.legacyMetadata();
        entityData.set(AMMO, projectileAmmo);
        entityData.set(DAMAGE, damage);
        entityData.set(INCENDIARY, incendiary);
        setPos(origin);

        double inaccuracy = 0.0075D * spread;
        Vec3 movement = new Vec3(
                heading.x + random.nextGaussian() * inaccuracy,
                heading.y + random.nextGaussian() * inaccuracy,
                heading.z + random.nextGaussian() * inaccuracy
        ).scale(ammo.projectileSpeed());
        setDeltaMovement(movement);
        setYRot((float) (Mth.atan2(movement.x, movement.z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(movement.y, Math.sqrt(movement.x * movement.x + movement.z * movement.z))
                * Mth.RAD_TO_DEG));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AMMO, 0);
        builder.define(DAMAGE, 0.0F);
        builder.define(INCENDIARY, false);
    }

    public SednaAmmoType ammoType() {
        int config = entityData.get(AMMO);
        if (config == 104 || config == 106) {
            return com.hbm.ntm.weapon.FiftyCalAmmoType.fromLegacyBulletConfig(config);
        }
        if (config == 102 || config == 103) {
            return com.hbm.ntm.weapon.Equestrian44AmmoType.fromLegacyBulletConfig(config);
        }
        return StandardAmmoTypes.fromLegacyMetadata(config);
    }

    public float damage() {
        return entityData.get(DAMAGE);
    }

    public boolean incendiary() {
        return entityData.get(INCENDIARY);
    }

    public int ricochets() {
        return ricochets;
    }

    public double tracerLength(float partialTick) {
        return Mth.lerp(partialTick, previousTracerVelocity, tracerVelocity);
    }

    public int tracerDarkColor() {
        return incendiary() ? 0xFFFF6A00 : ammoType().tracerDarkColor();
    }

    public int tracerLightColor() {
        return incendiary() ? 0xFFFFE28D : ammoType().tracerLightColor();
    }

    public boolean tracerFullbright() {
        return !incendiary() && ammoType().tracerFullbright();
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        if (!level().isClientSide) {
            super.lerpTo(x, y, z, yRot, xRot, steps);
            return;
        }
        if (steps <= 0) {
            setPos(x, y, z);
            setRot(yRot, xRot);
            turnProgress = 0;
            return;
        }
        syncPosX = x;
        syncPosY = y;
        syncPosZ = z;
        syncYaw = yRot;
        syncPitch = xRot;
        turnProgress = steps;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            Vec3 start = position();
            interpolateClientPosition();
            Vec3 displacement = position().subtract(start);
            previousTracerVelocity = tracerVelocity;
            tracerVelocity = displacement.length();
            updateRotation(displacement);
            return;
        }
        if (tickCount > ammoType().projectileLifetime()) {
            discardWithFinalTeleport();
            return;
        }

        Vec3 start = position();
        Vec3 movement = getDeltaMovement();
        Vec3 end = start.add(movement);
        breakSoftBlocks(start, end);

        BlockHitResult blockHit = ammoType().spectral()
                ? BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end))
                : level().clip(new ClipContext(
                        start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 collisionEnd = blockHit.getType() == net.minecraft.world.phys.HitResult.Type.MISS
                ? end : blockHit.getLocation();

        AABB sweep = getBoundingBox().expandTowards(movement).inflate(1.0D);
        List<EntityIntersection> intersections = level().getEntities(this, sweep, this::canDamage)
                .stream()
                .map(entity -> intersection(entity, start, collisionEnd))
                .filter(java.util.Objects::nonNull)
                .toList();

        if (ammoType().penetrates()) {
            for (EntityIntersection intersection : intersections) {
                hitEntity(intersection.entity(), intersection.location());
            }
        } else {
            EntityIntersection nearest = null;
            for (EntityIntersection intersection : intersections) {
                if (nearest == null || intersection.distanceSqr() < nearest.distanceSqr()) nearest = intersection;
            }
            if (nearest != null) {
                if (spawnFallingPayload(nearest.location())) return;
                if (ammoType().spawnsBuildingOnImpact()) {
                    com.hbm.ntm.entity.BuildingEntity.spawn((ServerLevel) level(), nearest.location());
                    setPos(nearest.location());
                    discardWithFinalTeleport();
                    return;
                }
                if (ammoType().impactExplosionRadius() > 0.0F
                        && ammoType().explodesBeforeDirectHit()
                        && !(nearest.entity() == getOwner()
                        && ammoType().tinyImpactExplosion() && tickCount < 3)) {
                    explodeAt(nearest.location(), null);
                    return;
                }
                hitEntity(nearest.entity(), nearest.location());
                if (ammoType().impactExplosionRadius() > 0.0F
                        && !ammoType().explodesBeforeDirectHit()) {
                    explodeAt(nearest.location(), null);
                    return;
                }
                if (nearest.entity() instanceof LivingEntity) {
                    setPos(nearest.location());
                    discardWithFinalTeleport();
                    return;
                }
            }
        }

        if (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
            if (spawnFallingPayload(blockHit.getLocation())) return;
            if (ammoType().spawnsBuildingOnImpact()) {
                com.hbm.ntm.entity.BuildingEntity.spawn((ServerLevel) level(), blockHit.getLocation());
                setPos(blockHit.getLocation());
                discardWithFinalTeleport();
                return;
            }
            if (ammoType().impactExplosionRadius() > 0.0F) {
                explodeAt(blockHit.getLocation(), blockHit.getDirection());
                return;
            }
            hitBlock(blockHit, movement);
        } else {
            setPos(end);
        }
        updateRotation(getDeltaMovement());
    }

    private boolean spawnFallingPayload(Vec3 impact) {
        if (!(level() instanceof ServerLevel server)) return false;
        if (ammoType().fallingPayload() == 1) BoxcarEntity.spawn(server, impact);
        else if (ammoType().fallingPayload() == 2) TorpedoEntity.spawn(server, impact);
        else return false;
        setPos(impact);
        discardWithFinalTeleport();
        return true;
    }

    private void breakSoftBlocks(Vec3 start, Vec3 end) {
        float threshold = ammoType().blockBreakHardness();
        if (threshold <= 0.0F || !(level() instanceof ServerLevel server)) return;
        Vec3 travel = end.subtract(start);
        double length = travel.length();
        if (length < 1.0E-6D) return;
        Vec3 direction = travel.scale(1.0D / length);
        for (double distance = 0.0D; distance < length; distance += 0.5D) {
            Vec3 sample = start.add(direction.scale(distance));
            BlockPos position = BlockPos.containing(sample);
            BlockState state = server.getBlockState(position);
            float hardness = state.getDestroySpeed(server, position);
            if (!state.isAir() && hardness >= 0.0F && hardness < threshold) {
                server.destroyBlock(position, false, getOwner());
            }
            server.sendParticles(ParticleTypes.FIREWORK, sample.x, sample.y, sample.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void interpolateClientPosition() {
        if (turnProgress <= 0) {
            setPos(getX(), getY(), getZ());
            return;
        }
        double step = 1.0D / turnProgress;
        double x = Mth.lerp(step, getX(), syncPosX);
        double y = Mth.lerp(step, getY(), syncPosY);
        double z = Mth.lerp(step, getZ(), syncPosZ);
        float yaw = (float) (getYRot() + Mth.wrapDegrees(syncYaw - getYRot()) * step);
        float pitch = (float) Mth.lerp(step, getXRot(), syncPitch);
        turnProgress--;
        setPos(x, y, z);
        setRot(yaw, pitch);
    }

    private boolean canDamage(Entity entity) {
        if (!entity.isAlive() || entity.isSpectator() || !entity.isPickable()) return false;
        Entity owner = getOwner();
        return entity != owner || tickCount >= 2;
    }

    private static EntityIntersection intersection(Entity entity, Vec3 start, Vec3 end) {
        Optional<Vec3> hit = entity.getBoundingBox().inflate(0.3D).clip(start, end);
        return hit.map(vec3 -> new EntityIntersection(entity, vec3, start.distanceToSqr(vec3))).orElse(null);
    }

    private void hitEntity(Entity target, Vec3 hitLocation) {
        Entity owner = getOwner();
        var source = level().damageSources().source(ModDamageTypes.BULLET, this, owner);
        if (!(target instanceof LivingEntity living)) {
            target.hurt(source, damage());
            return;
        }
        if (!living.isAlive()) return;

        float intendedDamage = damage();
        if (ammoType().headshotMultiplier() > 1.0F) {
            double head = living.getBbHeight() - living.getEyeHeight();
            if (hitLocation.y > living.getY() + living.getBbHeight() - head * 2.0D) {
                intendedDamage *= ammoType().headshotMultiplier();
            }
        }
        float appliedDamage = compensateForArmorPiercing(living, source, intendedDamage,
                ammoType().armorThresholdNegation(), ammoType().armorPiercing());

        float healthBefore = living.getHealth();
        Vec3 previousMotion = living.getDeltaMovement();
        int phosphorusTicks = incendiary() ? 300 : ammoType().phosphorusTicks();
        if (phosphorusTicks > 0 && ammoType().phosphorusOnImpact()) {
            WeaponStatusEvents.applyPhosphorus(living, phosphorusTicks);
        }
        living.invulnerableTime = 0;
        boolean hurt = living.hurt(source, appliedDamage);
        living.setDeltaMovement(previousMotion);
        if (hurt && phosphorusTicks > 0 && !ammoType().phosphorusOnImpact()) {
            WeaponStatusEvents.applyPhosphorus(living, phosphorusTicks);
        }
        if (hurt && owner != null) {
            double dx = living.getX() - owner.getX();
            double dz = living.getZ() - owner.getZ();
            if (dx * dx + dz * dz < 1.0E-4D) {
                dx = (random.nextDouble() - random.nextDouble()) * 0.01D;
                dz = (random.nextDouble() - random.nextDouble()) * 0.01D;
            }
            living.knockback(0.1D, -dx, -dz);
        }

        if (ammoType().penetrationDamageFalloff()) {
            float dealt = Math.max(healthBefore - living.getHealth(), 0.0F);
            entityData.set(DAMAGE, damage() - dealt * 0.5F);
        }
        if (!ammoType().penetrates() || damage() < 0.0F) discardWithFinalTeleport();
    }

    private static float compensateForArmorPiercing(LivingEntity living,
                                                     net.minecraft.world.damagesource.DamageSource source,
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

        // Work backward through vanilla armor until it produces the damage we actually wanted.
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

    private void explodeAt(Vec3 center, Direction impactFace) {
        if (!(level() instanceof ServerLevel level)) {
            discardWithFinalTeleport();
            return;
        }
        SednaAmmoType ammo = ammoType();
        if (ammo.tinyImpactExplosion() && impactFace != null) {
            center = center.add(Vec3.atLowerCornerOf(impactFace.getNormal()).scale(0.25D));
        }
        double range = ammo.impactExplosionRange();
        double nodeDistance = ammo.tinyImpactExplosion() ? 0.5D : 1.0D;
        double knockback = ammo.tinyImpactExplosion() ? 0.25D : 1.0D;
        Entity owner = getOwner();
        var source = level.damageSources().explosion(this, owner);
        AABB area = new AABB(center, center).inflate(range + 1.0D);
        for (Entity target : level.getEntities(this, area, Entity::isAlive)) {
            AABB bounds = target.getBoundingBox();
            double closestX = Mth.clamp(center.x, bounds.minX, bounds.maxX);
            double closestY = Mth.clamp(center.y, bounds.minY, bounds.maxY);
            double closestZ = Mth.clamp(center.z, bounds.minZ, bounds.maxZ);
            double distance = center.distanceTo(new Vec3(closestX, closestY, closestZ));
            double scaled = distance / range;
            float density = exposure(center, target, nodeDistance);
            if (scaled > 1.0D || density < 0.125F) continue;

            float amount = (float) (damage() * (1.0D - scaled));
            if (target == owner) amount *= 0.5F;
            if (amount <= 0.0F) continue;
            if (target instanceof LivingEntity living) {
                amount = compensateForArmorPiercing(living, source, amount,
                        ammo.armorThresholdNegation(), ammo.armorPiercing());
                living.invulnerableTime = 0;
            }
            if (target.hurt(source, amount)) {
                Vec3 push = target.getEyePosition().subtract(center);
                if (push.lengthSqr() > 1.0E-6D) {
                    target.setDeltaMovement(target.getDeltaMovement().add(
                            push.normalize().scale((1.0D - scaled) * density * knockback)));
                }
            }
        }
        if (ammo.tinyImpactExplosion()) {
            level.playSound(null, center.x, center.y, center.z, ModSounds.GUN_EXPLOSION_TINY.get(),
                    SoundSource.PLAYERS, 15.0F, 1.0F);
            level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        } else {
            PacketDistributor.sendToPlayersNear(level, null, center.x, center.y, center.z, 200.0D,
                    new ChargeBlastPayload(center.x, center.y, center.z, false));
        }
        setPos(center);
        discardWithFinalTeleport();
    }

    private static float exposure(Vec3 center, Entity target) {
        return exposure(center, target, 1.0D);
    }

    private static float exposure(Vec3 center, Entity target, double distance) {
        Vec3[] nodes = {
                center,
                center.add(distance, 0.0D, 0.0D), center.add(-distance, 0.0D, 0.0D),
                center.add(0.0D, distance, 0.0D), center.add(0.0D, -distance, 0.0D),
                center.add(0.0D, 0.0D, distance), center.add(0.0D, 0.0D, -distance)
        };
        float density = 0.0F;
        for (Vec3 node : nodes) {
            density = Math.max(density, Explosion.getSeenPercent(node, target));
        }
        return density;
    }

    private void hitBlock(BlockHitResult hit, Vec3 incoming) {
        BlockPos position = hit.getBlockPos();
        BlockState state = level().getBlockState(position);
        if (isGlass(state)) {
            level().destroyBlock(position, false);
            setPos(hit.getLocation().add(incoming.normalize().scale(0.01D)));
            return;
        }

        Vec3 face = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
        Vec3 velocity = incoming.normalize();
        double angleToNormal = Math.toDegrees(Math.acos(Mth.clamp(velocity.dot(face), -1.0D, 1.0D)));
        double grazingAngle = Math.abs(angleToNormal - 90.0D);
        if (grazingAngle > ammoType().ricochetAngle()) {
            setPos(hit.getLocation());
            discardWithFinalTeleport();
            return;
        }

        ricochets++;
        if (ricochets > ammoType().maxRicochets()) {
            setPos(hit.getLocation());
            discardWithFinalTeleport();
            return;
        }

        Direction side = hit.getDirection();
        Vec3 reflected = switch (side.getAxis()) {
            case X -> new Vec3(-incoming.x, incoming.y, incoming.z);
            case Y -> new Vec3(incoming.x, -incoming.y, incoming.z);
            case Z -> new Vec3(incoming.x, incoming.y, -incoming.z);
        };
        setDeltaMovement(reflected);
        setPos(hit.getLocation().add(reflected.normalize().scale(0.01D)));
        level().playSound(null, position, ModSounds.GUN_RICOCHET.get(), SoundSource.PLAYERS, 0.25F, 1.0F);
    }

    private static boolean isGlass(BlockState state) {
        return state.getSoundType() == SoundType.GLASS;
    }

    private void updateRotation(Vec3 movement) {
        if (movement.lengthSqr() <= 0.0D) return;
        float yaw = (float) (Mth.atan2(movement.x, movement.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(movement.y,
                Math.sqrt(movement.x * movement.x + movement.z * movement.z)) * Mth.RAD_TO_DEG);
        while (pitch - xRotO < -180.0F) xRotO -= 360.0F;
        while (pitch - xRotO >= 180.0F) xRotO += 360.0F;
        while (yaw - yRotO < -180.0F) yRotO -= 360.0F;
        while (yaw - yRotO >= 180.0F) yRotO += 360.0F;
        setRot(yaw, pitch);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ammo", entityData.get(AMMO));
        tag.putFloat("damage", entityData.get(DAMAGE));
        tag.putBoolean("incendiary", entityData.get(INCENDIARY));
        tag.putInt("ricochets", ricochets);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(AMMO, tag.getInt("ammo"));
        entityData.set(DAMAGE, tag.getFloat("damage"));
        entityData.set(INCENDIARY, tag.getBoolean("incendiary"));
        ricochets = tag.getInt("ricochets");
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0D * 128.0D;
    }

    private void discardWithFinalTeleport() {
        discard();
        // Final teleport lets first-tick collisions draw more than one embarrassed pixel.
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().broadcast(this, new ClientboundTeleportEntityPacket(this));
        }
    }

    private record EntityIntersection(Entity entity, Vec3 location, double distanceSqr) { }
}
