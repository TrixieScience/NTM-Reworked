package com.hbm.ntm.entity;

import com.hbm.ntm.explosion.MineExplosion;
import com.hbm.ntm.network.MineBlastPayload;
import com.hbm.ntm.nuclear.NuclearExplosionEntity;
import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.weapon.ArtilleryAmmoType;
import com.hbm.ntm.weapon.HimarsAmmoType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public final class TurretOrdnanceEntity extends Projectile implements ItemSupplier {
    private static final EntityDataAccessor<Boolean> HIMARS = SynchedEntityData.defineId(
            TurretOrdnanceEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> AMMO = SynchedEntityData.defineId(
            TurretOrdnanceEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(
            TurretOrdnanceEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TARGET_X = SynchedEntityData.defineId(
            TurretOrdnanceEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Y = SynchedEntityData.defineId(
            TurretOrdnanceEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Z = SynchedEntityData.defineId(
            TurretOrdnanceEntity.class, EntityDataSerializers.FLOAT);

    public TurretOrdnanceEntity(EntityType<? extends TurretOrdnanceEntity> type, Level level) {
        super(type, level);
    }

    public static TurretOrdnanceEntity artillery(ServerLevel level, ArtilleryAmmoType ammo,
                                                  Vec3 origin, Vec3 velocity, Vec3 target) {
        TurretOrdnanceEntity shell = new TurretOrdnanceEntity(ModEntities.TURRET_ORDNANCE.get(), level);
        shell.entityData.set(AMMO, ammo.ordinal());
        shell.setPos(origin);
        shell.setDeltaMovement(velocity);
        shell.setTarget(target, -1);
        return shell;
    }

    public static TurretOrdnanceEntity himars(ServerLevel level, HimarsAmmoType ammo,
                                               Vec3 origin, Vec3 velocity, Entity target) {
        TurretOrdnanceEntity rocket = new TurretOrdnanceEntity(ModEntities.TURRET_ORDNANCE.get(), level);
        rocket.entityData.set(HIMARS, true);
        rocket.entityData.set(AMMO, ammo.ordinal());
        rocket.setPos(origin);
        rocket.setDeltaMovement(velocity);
        rocket.setTarget(target.position().add(0, target.getBbHeight() * 0.5D, 0), target.getId());
        return rocket;
    }

    private void setTarget(Vec3 target, int id) {
        entityData.set(TARGET, id);
        entityData.set(TARGET_X, (float) target.x);
        entityData.set(TARGET_Y, (float) target.y);
        entityData.set(TARGET_Z, (float) target.z);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HIMARS, false);
        builder.define(AMMO, 0);
        builder.define(TARGET, -1);
        builder.define(TARGET_X, 0F);
        builder.define(TARGET_Y, 0F);
        builder.define(TARGET_Z, 0F);
    }

    @Override public void tick() {
        super.tick();
        if (tickCount > (entityData.get(HIMARS) ? 500 : 800)) {
            discard();
            return;
        }

        Vec3 start = position();
        Vec3 velocity = getDeltaMovement();
        Vec3 end = start.add(velocity);
        if (!level().isClientSide) {
            BlockHitResult blockHit = level().clip(new ClipContext(start, end,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            Vec3 collisionEnd = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
            Entity hitEntity = null;
            Vec3 entityHit = null;
            double nearest = Double.MAX_VALUE;
            for (Entity candidate : level().getEntities(this,
                    getBoundingBox().expandTowards(velocity).inflate(0.5D), this::canHit)) {
                Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.35D).clip(start, collisionEnd);
                if (hit.isPresent() && start.distanceToSqr(hit.get()) < nearest) {
                    nearest = start.distanceToSqr(hit.get());
                    hitEntity = candidate;
                    entityHit = hit.get();
                }
            }
            if (hitEntity != null) impact(entityHit);
            else if (blockHit.getType() != HitResult.Type.MISS) impact(blockHit.getLocation());
        } else if (entityData.get(HIMARS)) {
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), 0, 0, 0);
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
        }

        if (!isAlive()) return;
        setPos(end);
        if (entityData.get(HIMARS)) {
            Vec3 target = targetPosition();
            Vec3 desired = target.subtract(end);
            if (desired.lengthSqr() > 1D) {
                double speed = Math.max(velocity.length(), 2.5D);
                setDeltaMovement(velocity.normalize().lerp(desired.normalize(), 0.08D).normalize().scale(speed));
            }
        } else {
            setDeltaMovement(velocity.add(0, -0.4905D, 0));
        }
    }

    private boolean canHit(Entity entity) {
        return entity.isAlive() && entity.isPickable() && entity != getOwner() && tickCount > 2;
    }

    private Vec3 targetPosition() {
        Entity target = level().getEntity(entityData.get(TARGET));
        if (target != null && target.isAlive()) {
            Vec3 position = target.position().add(0, target.getBbHeight() * 0.5D, 0);
            setTarget(position, target.getId());
            return position;
        }
        return new Vec3(entityData.get(TARGET_X), entityData.get(TARGET_Y), entityData.get(TARGET_Z));
    }

    private void impact(Vec3 hit) {
        if (!(level() instanceof ServerLevel level)) return;
        if (entityData.get(HIMARS)) impactHimars(level, hit);
        else impactArtillery(level, hit);
        setPos(hit);
        discard();
    }

    private void impactArtillery(ServerLevel level, Vec3 hit) {
        ArtilleryAmmoType type = ArtilleryAmmoType.values()[Math.floorMod(
                entityData.get(AMMO), ArtilleryAmmoType.values().length)];
        switch (type) {
            case NORMAL -> explode(level, hit, 10F, 3F, false);
            case CLASSIC -> explode(level, hit, 15F, 5F, false);
            case HIGH_EXPLOSIVE -> explode(level, hit, 15F, 3F, true);
            case MINI_NUKE -> level.addFreshEntity(NuclearExplosionEntity.create(level, 35, hit.x, hit.y, hit.z));
            case NUKE -> NuclearExplosionEntity.spawnLargeNuke(level, hit.x, hit.y, hit.z, 100, false);
            case PHOSPHORUS -> phosphorus(level, hit, 10F);
            case MINI_NUKE_MULTI -> {
                for (int i = 0; i < 5; i++) level.addFreshEntity(NuclearExplosionEntity.create(level, 18,
                        hit.x + random.nextGaussian() * 8D, hit.y, hit.z + random.nextGaussian() * 8D));
            }
            case PHOSPHORUS_MULTI -> {
                for (int i = 0; i < 10; i++) phosphorus(level,
                        hit.add(random.nextGaussian() * 8D, 0, random.nextGaussian() * 8D), 5F);
            }
            case CARGO -> level.addFreshEntity(new ItemEntity(level, hit.x, hit.y, hit.z,
                    type.createStack(ModItems.AMMO_ARTY.get(), 1)));
            case CHLORINE -> gas(level, hit, 18);
            case PHOSGENE -> gas(level, hit, 36);
            case MUSTARD -> gas(level, hit, 60);
        }
    }

    private void impactHimars(ServerLevel level, Vec3 hit) {
        HimarsAmmoType type = HimarsAmmoType.values()[Math.floorMod(
                entityData.get(AMMO), HimarsAmmoType.values().length)];
        switch (type) {
            case SMALL -> explode(level, hit, 20F, 3F, false);
            case LARGE -> explode(level, hit, 50F, 5F, true);
            case SMALL_HE -> explode(level, hit, 20F, 3F, true);
            case SMALL_WP -> phosphorus(level, hit, 20F);
            case SMALL_TB -> explode(level, hit, 20F, 10F, true);
            case LARGE_TB -> explode(level, hit, 50F, 12F, true);
            case SMALL_MINI_NUKE -> level.addFreshEntity(
                    NuclearExplosionEntity.create(level, 35, hit.x, hit.y, hit.z));
            case SMALL_LAVA -> {
                explode(level, hit, 20F, 3F, true);
                BlockPos center = BlockPos.containing(hit);
                for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -2), center.offset(2, 1, 2))) {
                    if (level.getBlockState(pos).isAir() && level.random.nextInt(3) == 0) {
                        level.setBlock(pos, Blocks.LAVA.defaultBlockState(), 11);
                    }
                }
            }
        }
    }

    private void explode(ServerLevel level, Vec3 hit, float size, float range, boolean blocks) {
        MineExplosion.blastEntities(level, hit.x, hit.y, hit.z, size, size * 4F,
                1D, 0F, 0F, range, this, getOwner());
        if (blocks) MineExplosion.blastBlocks(level, hit.x, hit.y, hit.z, size, 48, false, getOwner());
        PacketDistributor.sendToPlayersNear(level, null, hit.x, hit.y, hit.z, 250,
                new MineBlastPayload(hit.x, hit.y, hit.z, Math.max(10, (int) size), 1F));
    }

    private void phosphorus(ServerLevel level, Vec3 hit, float size) {
        explode(level, hit, size, 3F, false);
        level.addFreshEntity(new LingeringFireEntity(ModEntities.LINGERING_FIRE.get(), level,
                hit.x, hit.y, hit.z, 600, size * 2D, 6D, LingeringFireEntity.Kind.PHOSPHORUS));
    }

    private void gas(ServerLevel level, Vec3 hit, int clouds) {
        explode(level, hit, 5F, 2F, false);
        for (int i = 0; i < clouds; i++) {
            level.addFreshEntity(ChlorineCloudEntity.create(level,
                    hit.x + random.nextGaussian() * 5D, hit.y + random.nextDouble() * 3D,
                    hit.z + random.nextGaussian() * 5D, 0, 0.02D, 0));
        }
    }

    @Override public ItemStack getItem() {
        if (entityData.get(HIMARS)) {
            HimarsAmmoType type = HimarsAmmoType.values()[Math.floorMod(
                    entityData.get(AMMO), HimarsAmmoType.values().length)];
            return type.createStack(ModItems.AMMO_HIMARS.get(), 1);
        }
        ArtilleryAmmoType type = ArtilleryAmmoType.values()[Math.floorMod(
                entityData.get(AMMO), ArtilleryAmmoType.values().length)];
        return type.createStack(ModItems.AMMO_ARTY.get(), 1);
    }

    public boolean isHimars() { return entityData.get(HIMARS); }
    public int ammoIndex() { return entityData.get(AMMO); }
    public Vec3 direction() {
        Vec3 motion = getDeltaMovement();
        return motion.lengthSqr() < 1.0E-8D ? new Vec3(0, 0, 1) : motion.normalize();
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(HIMARS, tag.getBoolean("Himars"));
        entityData.set(AMMO, tag.getInt("Ammo"));
        entityData.set(TARGET, tag.getInt("Target"));
        entityData.set(TARGET_X, tag.getFloat("TargetX"));
        entityData.set(TARGET_Y, tag.getFloat("TargetY"));
        entityData.set(TARGET_Z, tag.getFloat("TargetZ"));
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Himars", entityData.get(HIMARS));
        tag.putInt("Ammo", entityData.get(AMMO));
        tag.putInt("Target", entityData.get(TARGET));
        tag.putFloat("TargetX", entityData.get(TARGET_X));
        tag.putFloat("TargetY", entityData.get(TARGET_Y));
        tag.putFloat("TargetZ", entityData.get(TARGET_Z));
    }
}
