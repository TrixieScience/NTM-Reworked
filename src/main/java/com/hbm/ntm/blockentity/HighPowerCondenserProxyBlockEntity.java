package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HighPowerCondenserBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/** One of the six combination fluid/power sockets hidden in the condenser shell. */
public final class HighPowerCondenserProxyBlockEntity extends BlockEntity implements HeReceiverProxy {
    public HighPowerCondenserProxyBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.MACHINE_CONDENSER_POWERED_PROXY.get(), position, state);
    }

    @Override
    @Nullable
    public HighPowerCondenserBlockEntity target() {
        if (level == null || !(getBlockState().getBlock() instanceof HighPowerCondenserBlock)) return null;
        BlockPos core = HighPowerCondenserBlock.corePosition(worldPosition, getBlockState());
        return level.getBlockEntity(core) instanceof HighPowerCondenserBlockEntity condenser ? condenser : null;
    }

    @Nullable
    public IFluidHandler fluidHandler(@Nullable Direction side) {
        HighPowerCondenserBlockEntity condenser = target();
        return condenser != null && HighPowerCondenserBlock.canConnectAt(getBlockState(), side)
                ? condenser.fluidHandler() : null;
    }

    @Override public boolean canConnect(Direction side) {
        return HighPowerCondenserBlock.canConnectAt(getBlockState(), side);
    }
}
