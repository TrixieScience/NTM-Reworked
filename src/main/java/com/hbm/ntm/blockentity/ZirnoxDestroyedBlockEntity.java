package com.hbm.ntm.blockentity;

import com.hbm.ntm.radiation.ChunkRadiationData;
import com.hbm.ntm.radiation.RadiationSystem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ZirnoxDestroyedBlockEntity extends BlockEntity {
    private boolean onFire = true;

    public ZirnoxDestroyedBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ZIRNOX_DESTROYED.get(), pos, state);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, ZirnoxDestroyedBlockEntity wreck) {
        if (!(level instanceof ServerLevel server)) return;
        if (wreck.onFire && server.random.nextInt(5_000) == 0) {
            wreck.onFire = false;
            wreck.setChanged();
        }
        if (wreck.onFire && server.getGameTime() % 50 == 0) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + .5, pos.getY() + 1.2,
                    pos.getZ() + .5, 4, .35, .3, .35, .02);
            server.sendParticles(ParticleTypes.FLAME, pos.getX() + .5, pos.getY() + 1.75,
                    pos.getZ() + .5, 3, .25, .15, .25, .01);
            server.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                    1.0F + server.random.nextFloat(), server.random.nextFloat() * .7F + .3F);
        }
        if (server.getGameTime() % 20 == 0) {
            ChunkRadiationData.get(server).increment(pos, wreck.onFire ? 500F : 75F);
        }
        Vec3 origin = pos.getCenter();
        float source = wreck.onFire ? 500_000F : 75_000F;
        for (LivingEntity living : server.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(100))) {
            Vec3 ray = new Vec3(living.getX() - origin.x, living.getEyeY() - origin.y,
                    living.getZ() - origin.z);
            double distance = ray.length();
            if (distance <= 0D || distance > 100D) continue;
            Vec3 direction = ray.normalize();
            float resistance = 0F;
            for (int step = 1; step < distance; step++) {
                BlockPos sample = BlockPos.containing(origin.add(direction.scale(step)));
                resistance += server.getBlockState(sample).getBlock().getExplosionResistance();
            }
            float dose = source / Math.max(resistance, 1F) / (float) (distance * distance);
            RadiationSystem.contaminate(living, dose, false);
            if (wreck.onFire && distance < 5D) living.hurt(server.damageSources().onFire(), 2F);
        }
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("onFire", onFire);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("onFire")) onFire = tag.getBoolean("onFire");
    }

    public AABB renderBounds() { return new AABB(worldPosition).inflate(3, 1, 3).expandTowards(0, 3, 0); }
}
