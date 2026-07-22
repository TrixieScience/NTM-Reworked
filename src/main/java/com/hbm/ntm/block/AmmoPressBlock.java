package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.AmmoPressBlockEntity;
import com.hbm.ntm.blockentity.AmmoPressProxyBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class AmmoPressBlock extends BaseEntityBlock {
    public static final MapCodec<AmmoPressBlock> CODEC = simpleCodec(AmmoPressBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty DEPTH = IntegerProperty.create("part_depth", 0, 2);
    public static final IntegerProperty Y = IntegerProperty.create("part_y", 0, 1);
    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);
    private static final VoxelShape FULL = Shapes.block();

    public AmmoPressBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DEPTH, 1).setValue(Y, 0));
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
        return stateForPart(core, core, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos position, BlockState state, LivingEntity placer, ItemStack stack) {
        BlockPos core = corePosition(position, state);
        Direction facing = state.getValue(FACING);
        for (BlockPos part : partPositions(core, facing)) level.setBlock(part, stateForPart(part, core, facing), Block.UPDATE_ALL);
        if (stack.has(DataComponents.CUSTOM_NAME) && level.getBlockEntity(core) instanceof AmmoPressBlockEntity press) {
            press.setCustomName(stack.getHoverName());
        }
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return FULL; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos position, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.sidedSuccess(level.isClientSide);
        BlockPos core = corePosition(position, state);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(core) instanceof AmmoPressBlockEntity press) {
            serverPlayer.openMenu(press, buffer -> buffer.writeBlockPos(core));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos position, BlockState newState, boolean moved) {
        if (state.is(newState.getBlock())) { super.onRemove(state, level, position, newState, moved); return; }
        BlockPos core = corePosition(position, state);
        Direction facing = state.getValue(FACING);
        if (!REMOVING.get()) {
            REMOVING.set(true);
            try {
                if (!level.isClientSide && level.getBlockEntity(core) instanceof AmmoPressBlockEntity press) {
                    Containers.dropContents(level, core, press);
                    press.clearContent();
                }
                for (BlockPos part : partPositions(core, facing)) {
                    if (!part.equals(position) && level.getBlockState(part).is(this)) {
                        level.setBlock(part, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                    }
                }
            } finally { REMOVING.set(false); }
        }
        super.onRemove(state, level, position, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCore(state) ? new AmmoPressBlockEntity(pos, state) : new AmmoPressProxyBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return isCore(state) ? createTickerHelper(type, ModBlockEntities.AMMO_PRESS.get(), AmmoPressBlockEntity::tick) : null;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DEPTH, Y);
    }

    public static boolean isCore(BlockState state) { return state.getValue(DEPTH) == 1 && state.getValue(Y) == 0; }

    public static BlockPos corePosition(BlockPos position, BlockState state) {
        return position.relative(state.getValue(FACING), 1 - state.getValue(DEPTH)).below(state.getValue(Y));
    }

    public static BlockPos[] partPositions(BlockPos core, Direction facing) {
        return new BlockPos[]{core.relative(facing.getOpposite()), core, core.relative(facing),
                core.relative(facing.getOpposite()).above(), core.above(), core.relative(facing).above()};
    }

    private BlockState stateForPart(BlockPos part, BlockPos core, Direction facing) {
        int depth = part.equals(core.relative(facing.getOpposite())) || part.below().equals(core.relative(facing.getOpposite())) ? 0
                : part.equals(core.relative(facing)) || part.below().equals(core.relative(facing)) ? 2 : 1;
        return defaultBlockState().setValue(FACING, facing).setValue(DEPTH, depth).setValue(Y, part.getY() - core.getY());
    }
}
