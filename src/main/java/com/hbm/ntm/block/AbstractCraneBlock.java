package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CraneExtractorBlockEntity;
import com.hbm.ntm.blockentity.CraneGrabberBlockEntity;
import com.hbm.ntm.blockentity.CraneInserterBlockEntity;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Six-faced crane rules shared by every flavor of cargo meddling. */
public abstract class AbstractCraneBlock extends BaseEntityBlock {
    public static final DirectionProperty INPUT = ConveyorBoxerBlock.INPUT;
    public static final DirectionProperty OUTPUT = ConveyorBoxerBlock.OUTPUT;

    protected AbstractCraneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(INPUT, Direction.NORTH)
                .setValue(OUTPUT, Direction.SOUTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction input = context.getNearestLookingDirection().getOpposite();
        return defaultBlockState().setValue(INPUT, input).setValue(OUTPUT, input.getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!stack.has(DataComponents.CUSTOM_NAME)) return;
        if (level.getBlockEntity(pos) instanceof CraneExtractorBlockEntity extractor) {
            extractor.setCustomName(stack.getHoverName());
        } else if (level.getBlockEntity(pos) instanceof CraneInserterBlockEntity inserter) {
            inserter.setCustomName(stack.getHoverName());
        } else if (level.getBlockEntity(pos) instanceof CraneGrabberBlockEntity grabber) {
            grabber.setCustomName(stack.getHoverName());
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof ConveyorWandItem) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (!stack.is(ModItems.SCREWDRIVER.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            BlockState changed = player.isShiftKeyDown()
                    ? ConveyorBoxerBlock.setOutput(state, hit.getDirection())
                    : ConveyorBoxerBlock.setInput(state, hit.getDirection());
            level.setBlock(pos, changed, Block.UPDATE_ALL);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MenuProvider provider) {
            serverPlayer.openMenu(provider, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                            boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof CraneExtractorBlockEntity extractor) {
                extractor.dropRealContents();
            } else if (level.getBlockEntity(pos) instanceof CraneGrabberBlockEntity grabber) {
                grabber.dropRealContents();
            } else if (level.getBlockEntity(pos) instanceof net.minecraft.world.Container container) {
                Containers.dropContents(level, pos, container);
                container.clearContent();
            }
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INPUT, OUTPUT);
    }
}
