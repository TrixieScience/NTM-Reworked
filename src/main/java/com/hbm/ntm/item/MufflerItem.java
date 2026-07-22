package com.hbm.ntm.item;

import com.hbm.ntm.block.ThermalMultiblockBlock;
import com.hbm.ntm.blockentity.MufflableMachine;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class MufflerItem extends Item {
    public MufflerItem() {
        super(new Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockState state = level.getBlockState(clicked);
        BlockPos target = state.getBlock() instanceof ThermalMultiblockBlock
                ? ThermalMultiblockBlock.corePosition(clicked, state)
                : clicked;
        if (!(level.getBlockEntity(target) instanceof MufflableMachine machine) || machine.isMuffled()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && machine.installMuffler()) {
            context.getItemInHand().shrink(1);
            level.playSound(null, clicked, ModSounds.UPGRADE_PLUG.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
