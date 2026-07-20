package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.CombustionEngineBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/** A useful hole in the combustion engine's otherwise decorative shell. */
public final class CombustionEngineProxyBlockEntity extends BlockEntity implements HeProviderProxy {
    public CombustionEngineProxyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_COMBUSTION_ENGINE_PROXY.get(), pos, state);
    }

    @Override @Nullable public CombustionEngineBlockEntity target() {
        if (level == null || !(getBlockState().getBlock() instanceof CombustionEngineBlock)) return null;
        BlockPos core = CombustionEngineBlock.corePosition(worldPosition, getBlockState());
        return level.getBlockEntity(core) instanceof CombustionEngineBlockEntity engine ? engine : null;
    }

    @Nullable public IFluidHandler fluidHandler(@Nullable Direction side) {
        CombustionEngineBlockEntity target = target();
        return target != null && CombustionEngineBlock.canConnectAt(getBlockState(), side)
                ? target.fluidHandler() : null;
    }

    @Override public boolean canConnect(Direction side) {
        return CombustionEngineBlock.canConnectAt(getBlockState(), side);
    }
}
