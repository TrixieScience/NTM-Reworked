package com.hbm.ntm.entity;

import com.hbm.ntm.explosion.MineExplosion;
import com.hbm.ntm.network.MineBlastPayload;
import com.hbm.ntm.network.MiniNukeBlastPayload;
import com.hbm.ntm.nuclear.NuclearExplosionEntity;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.weapon.MiniNukeAmmoType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public final class MiniNukeProjectileEntity extends Projectile {
    private static final EntityDataAccessor<Integer> AMMO = SynchedEntityData.defineId(
            MiniNukeProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(
            MiniNukeProjectileEntity.class, EntityDataSerializers.FLOAT);

    public MiniNukeProjectileEntity(EntityType<? extends MiniNukeProjectileEntity> type, Level level) {
        super(type, level);
    }

    public MiniNukeProjectileEntity(ServerLevel level, LivingEntity owner, MiniNukeAmmoType ammo,
                                    float damage, float spread, Vec3 origin, Vec3 heading) {
        this(ModEntities.MINI_NUKE_PROJECTILE.get(), level);
        setOwner(owner);
        entityData.set(AMMO, ammo.legacyMetadata());
        entityData.set(DAMAGE, damage);
        setPos(origin);
        double inaccuracy = spread * 0.0075D;
        Vec3 velocity = new Vec3(
                heading.x + random.nextGaussian() * inaccuracy,
                heading.y + random.nextGaussian() * inaccuracy,
                heading.z + random.nextGaussian() * inaccuracy).normalize();
        setDeltaMovement(velocity);
        updateRotation(velocity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AMMO, MiniNukeAmmoType.STANDARD.legacyMetadata());
        builder.define(DAMAGE, 0.0F);
    }

    public MiniNukeAmmoType ammoType() {
        return MiniNukeAmmoType.fromLegacyMetadata(entityData.get(AMMO));
    }

    public float damage() { return entityData.get(DAMAGE); }

    public Vec3 direction() {
        Vec3 movement = getDeltaMovement();
        return movement.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, 0.0D, 1.0D) : movement.normalize();
    }

    @Override
    public void tick() {
        super.tick();
        MiniNukeAmmoType ammo = ammoType();
        if (tickCount > ammo.projectileLifetime()) {
            discard();
            return;
        }

        Vec3 movement = getDeltaMovement();
        Vec3 step = movement.scale(ammo.projectileSpeed());
        Vec3 start = position();
        Vec3 end = start.add(step);

        if (!level().isClientSide) {
            BlockHitResult blockHit = level().clip(new ClipContext(
                    start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            Vec3 collisionEnd = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
            AABB sweep = getBoundingBox().expandTowards(step).inflate(1.0D);
            Entity nearest = null;
            Vec3 nearestHit = null;
            double nearestDistance = Double.MAX_VALUE;
            for (Entity candidate : level().getEntities(this, sweep, this::isImpactCandidate)) {
                Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.3D).clip(start, collisionEnd);
                if (hit.isEmpty()) continue;
                double distance = start.distanceToSqr(hit.get());
                if (distance < nearestDistance) {
                    nearest = candidate;
                    nearestHit = hit.get();
                    nearestDistance = distance;
                }
            }
            if (nearest != null) impact(nearestHit);
            else if (blockHit.getType() != HitResult.Type.MISS) impact(blockHit.getLocation());
        }

        if (isAlive()) {
            setPos(end);
            Vec3 next = movement.add(0.0D, -ammo.gravity(), 0.0D);
            setDeltaMovement(next);
            updateRotation(next);
        }
    }

    private boolean isImpactCandidate(Entity entity) {
        if (!entity.isAlive() || entity.isSpectator() || !entity.isPickable()) return false;
        return entity != getOwner() || tickCount >= 3;
    }

    private void impact(Vec3 hit) {
        if (!(level() instanceof ServerLevel level)) return;
        MiniNukeAmmoType ammo = ammoType();
        switch (ammo.kind()) {
            case STANDARD -> {
                blastEntities(level, hit, 10.0F, 2.0D, 1.5F);
                MineExplosion.incrementRad(level, (int) Math.floor(hit.x), (int) Math.floor(hit.y),
                        (int) Math.floor(hit.z), 1.0F);
                muke(level, hit, false, random.nextInt(100) == 0);
            }
            case DEMOLITION -> {
                MineExplosion.blastBlocksAndIgnite(level, hit.x, hit.y, hit.z, 15.0F, 64,
                        getOwner(), Blocks.FIRE.defaultBlockState());
                blastEntities(level, hit, 15.0F, 2.0D, 1.5F);
                MineExplosion.incrementRad(level, (int) Math.floor(hit.x), (int) Math.floor(hit.y),
                        (int) Math.floor(hit.z), 1.5F);
                muke(level, hit, false, random.nextInt(100) == 0);
            }
            case HIGH_YIELD -> {
                level.addFreshEntity(NuclearExplosionEntity.create(level, 35, hit.x, hit.y, hit.z));
                muke(level, hit, false, random.nextInt(100) == 0);
            }
            case TINY_TOTS -> {
                blastEntities(level, hit, 5.0F, 2.0D, 1.5F);
                MineExplosion.incrementRad(level, (int) Math.floor(hit.x), (int) Math.floor(hit.y),
                        (int) Math.floor(hit.z), 0.25F);
                muke(level, hit, true, random.nextInt(100) == 0);
            }
            case HIVE -> {
                blastEntities(level, hit, 5.0F, 1.0D, 1.5F);
                PacketDistributor.sendToPlayersNear(level, null, hit.x, hit.y, hit.z, 200.0D,
                        new MineBlastPayload(hit.x, hit.y, hit.z, 10, 2.5F));
            }
            case BALEFIRE -> {
                MineExplosion.blastBlocksAndIgnite(level, hit.x, hit.y, hit.z, 10.0F, 64,
                        getOwner(), ModBlocks.BALEFIRE.get().defaultBlockState());
                blastEntities(level, hit, 10.0F, 2.0D, 1.5F);
                MineExplosion.incrementRad(level, (int) Math.floor(hit.x), (int) Math.floor(hit.y),
                        (int) Math.floor(hit.z), 1.5F);
                muke(level, hit, false, true);
            }
        }
        setPos(hit);
        discard();
    }

    private void blastEntities(ServerLevel level, Vec3 hit, float size, double nodeDist,
                               float rangeMod) {
        MineExplosion.blastEntities(level, hit.x, hit.y, hit.z, size, damage(), nodeDist,
                0.0F, 0.0F, rangeMod, this, getOwner());
    }

    private static void muke(ServerLevel level, Vec3 hit, boolean tiny, boolean balefire) {
        level.playSound(null, BlockPos.containing(hit.x, hit.y + 0.5D, hit.z),
                ModSounds.MUKE_EXPLOSION.get(), SoundSource.BLOCKS, 15.0F, 1.0F);
        PacketDistributor.sendToPlayersNear(level, null, hit.x, hit.y, hit.z, 250.0D,
                new MiniNukeBlastPayload(hit.x, hit.y + 0.5D, hit.z, tiny, balefire));
    }

    private void updateRotation(Vec3 direction) {
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        setYRot((float) (Math.atan2(direction.x, direction.z) * 180.0D / Math.PI));
        setXRot((float) (Math.atan2(direction.y, horizontal) * 180.0D / Math.PI));
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(AMMO, tag.getInt("ammo"));
        entityData.set(DAMAGE, tag.getFloat("damage"));
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ammo", entityData.get(AMMO));
        tag.putFloat("damage", damage());
    }
}
