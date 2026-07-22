package com.hbm.ntm.mixin.compat.sable;

import com.hbm.ntm.blockentity.TurretFriendlyBlockEntity;
import com.hbm.ntm.compat.TurretTargetingFrame;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

/** Projects Mister Friendly's targeting and projectiles out of a moving Sable sublevel. */
@Mixin(TurretFriendlyBlockEntity.class)
public abstract class TurretFriendlySableMixin extends BlockEntity implements TurretTargetingFrame {
    protected TurretFriendlySableMixin(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    private SubLevel hbm$subLevel() {
        return level == null ? null : Sable.HELPER.getContaining(level, worldPosition);
    }

    @Override
    public Vec3 hbm$localPositionToWorld(Vec3 position) {
        return level == null ? position : Sable.HELPER.projectOutOfSubLevel(level, position);
    }

    @Override
    public Vec3 hbm$worldPositionToLocal(Vec3 position) {
        SubLevel subLevel = hbm$subLevel();
        return subLevel == null ? position : subLevel.logicalPose().transformPositionInverse(position);
    }

    @Override
    public Vec3 hbm$localVectorToWorld(Vec3 vector) {
        SubLevel subLevel = hbm$subLevel();
        return subLevel == null ? vector : subLevel.logicalPose().transformNormal(vector);
    }

    @Override
    public Vec3 hbm$worldVectorToLocal(Vec3 vector) {
        SubLevel subLevel = hbm$subLevel();
        return subLevel == null ? vector : subLevel.logicalPose().transformNormalInverse(vector);
    }

    @Override
    public Vec3 hbm$entityPosition(Entity entity) {
        return level == null ? entity.position()
                : Sable.HELPER.projectOutOfSubLevel(level, entity.position());
    }

    @Override
    public Vec3 hbm$entityEyePosition(Entity entity) {
        return level == null ? entity.getEyePosition()
                : Sable.HELPER.projectOutOfSubLevel(level, entity.getEyePosition());
    }

    @Override
    public Vec3 hbm$velocityAt(Vec3 localPosition) {
        SubLevel subLevel = hbm$subLevel();
        return level == null || subLevel == null
                ? Vec3.ZERO : Sable.HELPER.getVelocity(level, subLevel, localPosition).scale(1D / 20D);
    }
}
