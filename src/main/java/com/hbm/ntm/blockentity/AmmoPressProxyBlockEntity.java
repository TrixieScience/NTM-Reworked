package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.AmmoPressBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class AmmoPressProxyBlockEntity extends InventoryProxyBlockEntity<AmmoPressBlockEntity> {
    public AmmoPressProxyBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.AMMO_PRESS_PROXY.get(), position, state);
    }

    @Override @Nullable
    protected AmmoPressBlockEntity target() {
        if (level == null || !(getBlockState().getBlock() instanceof AmmoPressBlock)) return null;
        BlockPos core = AmmoPressBlock.corePosition(worldPosition, getBlockState());
        return level.getBlockEntity(core) instanceof AmmoPressBlockEntity press ? press : null;
    }
}
