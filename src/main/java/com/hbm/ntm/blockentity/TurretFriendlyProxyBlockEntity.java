package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.TurretFriendlyBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class TurretFriendlyProxyBlockEntity extends InventoryProxyBlockEntity<TurretFriendlyBlockEntity> {
    public TurretFriendlyProxyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET_PROXY.get(), pos, state);
    }

    @Nullable @Override public TurretFriendlyBlockEntity target() {
        if (level == null || !(getBlockState().getBlock() instanceof TurretFriendlyBlock)) return null;
        BlockPos core = TurretFriendlyBlock.corePosition(worldPosition, getBlockState());
        return level.getBlockEntity(core) instanceof TurretFriendlyBlockEntity turret ? turret : null;
    }
}
