package com.hbm.ntm.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/** Twenty-tick carrier for the source Tesla plasmablast triplet. */
public final class TeslaImpactEntity extends Entity {
    public static final int LIFETIME = 20;

    public TeslaImpactEntity(EntityType<? extends TeslaImpactEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    @Override public void tick() {
        super.tick();
        if (tickCount >= LIFETIME) discard();
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
}
