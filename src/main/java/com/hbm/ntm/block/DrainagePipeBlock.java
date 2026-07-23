package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DrainagePipeBlockEntity;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DrainagePipeBlock extends BaseEntityBlock {
    public static final MapCodec<DrainagePipeBlock> CODEC = simpleCodec(DrainagePipeBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2);
    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);
    private static final VoxelShape FULL = box(0, 0, 0, 16, 16, 16);

    public DrainagePipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, 0));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos core = context.getClickedPos();
        for (BlockPos part : partPositions(core, facing)) {
            if (!part.equals(core) && !context.getLevel().getBlockState(part).canBeReplaced(context)) return null;
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos position, BlockState state,
                            LivingEntity placer, ItemStack stack) {
        BlockPos core = corePosition(position, state);
        Direction facing = state.getValue(FACING);
        for (int part = 0; part < 3; part++) {
            level.setBlock(core.relative(facing.getOpposite(), part),
                    defaultBlockState().setValue(FACING, facing).setValue(PART, part), Block.UPDATE_ALL);
        }
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position,
                                             CollisionContext context) { return FULL; }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                               BlockPos position, Player player, InteractionHand hand,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown() || !(stack.getItem() instanceof FluidIdentifierItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        BlockPos core = corePosition(position, state);
        if (!(level.getBlockEntity(core) instanceof DrainagePipeBlockEntity drain)) {
            return ItemInteractionResult.FAIL;
        }
        FluidIdentifierItem.Selection selection = FluidIdentifierItem.primary(stack);
        if (!level.isClientSide) {
            drain.selectFluid(selection);
            player.displayClientMessage(Component.literal("Changed type to ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable(selection.translationKey()))
                    .append("!"), false);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos position,
                                                          Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos position,
                            BlockState next, boolean moved) {
        if (state.is(next.getBlock())) {
            super.onRemove(state, level, position, next, moved);
            return;
        }
        BlockPos core = corePosition(position, state);
        if (!REMOVING.get()) {
            REMOVING.set(true);
            try {
                for (BlockPos part : partPositions(core, state.getValue(FACING))) {
                    if (!part.equals(position) && level.getBlockState(part).is(this)) {
                        level.setBlock(part, Blocks.AIR.defaultBlockState(),
                                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                    }
                }
            } finally {
                REMOVING.set(false);
            }
        }
        super.onRemove(state, level, position, next, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return isCore(state) ? new DrainagePipeBlockEntity(position, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return isCore(state) ? createTickerHelper(type, ModBlockEntities.MACHINE_DRAIN.get(),
                DrainagePipeBlockEntity::tick) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    public static boolean isCore(BlockState state) { return state.getValue(PART) == 0; }

    public static BlockPos corePosition(BlockPos position, BlockState state) {
        return position.relative(state.getValue(FACING), state.getValue(PART));
    }

    public static List<BlockPos> partPositions(BlockPos core, Direction facing) {
        return List.of(core, core.relative(facing.getOpposite()),
                core.relative(facing.getOpposite(), 2));
    }

    public static boolean canConnectAt(BlockState state, @Nullable Direction side) {
        if (!isCore(state) || side == null) return false;
        Direction facing = state.getValue(FACING);
        return side == facing || side == facing.getClockWise() || side == facing.getCounterClockWise();
    }
}
