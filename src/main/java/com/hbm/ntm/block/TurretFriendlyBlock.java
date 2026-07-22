package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.TurretFriendlyBlockEntity;
import com.hbm.ntm.blockentity.TurretFriendlyProxyBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** The source 2x2 half-height mount shared by the first NT turret. */
public final class TurretFriendlyBlock extends BaseEntityBlock {
    public static final MapCodec<TurretFriendlyBlock> CODEC = simpleCodec(TurretFriendlyBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty X = IntegerProperty.create("part_x", 0, 1);
    public static final IntegerProperty Z = IntegerProperty.create("part_z", 0, 1);
    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.5, 1);
    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);

    public TurretFriendlyBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(X, 0).setValue(Z, 0));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos core = context.getClickedPos();
        for (BlockPos part : parts(core, facing)) {
            if (!part.equals(core) && !context.getLevel().getBlockState(part).canBeReplaced(context)) return null;
        }
        return stateForPart(core, core, facing);
    }

    @Override public void setPlacedBy(Level level, BlockPos position, BlockState state,
                                      LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        BlockPos core = corePosition(position, state);
        for (BlockPos part : parts(core, facing)) {
            level.setBlock(part, stateForPart(part, core, facing), Block.UPDATE_ALL);
        }
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                             CollisionContext context) { return SHAPE; }
    @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                                      CollisionContext context) { return SHAPE; }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                          Player player, BlockHitResult hit) {
        BlockPos core = corePosition(pos, state);
        if (!level.isClientSide && player instanceof ServerPlayer server
                && level.getBlockEntity(core) instanceof TurretFriendlyBlockEntity turret) {
            server.openMenu(turret, buffer -> buffer.writeBlockPos(core));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos,
                                      BlockState replacement, boolean moved) {
        if (state.is(replacement.getBlock()) || REMOVING.get()) {
            super.onRemove(state, level, pos, replacement, moved);
            return;
        }
        BlockPos core = corePosition(pos, state);
        Direction facing = state.getValue(FACING);
        REMOVING.set(true);
        try {
            if (!level.isClientSide && level.getBlockEntity(core) instanceof TurretFriendlyBlockEntity turret) {
                Containers.dropContents(level, core, turret);
                turret.clearContent();
            }
            for (BlockPos part : parts(core, facing)) {
                if (!part.equals(pos) && level.getBlockState(part).is(this)) {
                    level.setBlock(part, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                }
            }
        } finally {
            REMOVING.set(false);
        }
        super.onRemove(state, level, pos, replacement, moved);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return isCore(state) ? new TurretFriendlyBlockEntity(pos, state)
                : new TurretFriendlyProxyBlockEntity(pos, state);
    }

    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return isCore(state) ? createTickerHelper(type, ModBlockEntities.TURRET_FRIENDLY.get(),
                TurretFriendlyBlockEntity::tick) : null;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, X, Z);
    }

    public static boolean isCore(BlockState state) { return state.getValue(X) == 0 && state.getValue(Z) == 0; }

    public static BlockPos corePosition(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction side = facing.getClockWise();
        return pos.relative(side, -state.getValue(X)).relative(facing, -state.getValue(Z));
    }

    public static BlockPos[] parts(BlockPos core, Direction facing) {
        Direction side = facing.getClockWise();
        return new BlockPos[]{core, core.relative(side), core.relative(facing), core.relative(side).relative(facing)};
    }

    public static Vec3 horizontalOffset(Direction facing) {
        Direction side = facing.getClockWise();
        return new Vec3(0.5D + 0.5D * (facing.getStepX() + side.getStepX()), 0D,
                0.5D + 0.5D * (facing.getStepZ() + side.getStepZ()));
    }

    private BlockState stateForPart(BlockPos part, BlockPos core, Direction facing) {
        BlockPos delta = part.subtract(core);
        Direction side = facing.getClockWise();
        int x = delta.getX() * side.getStepX() + delta.getZ() * side.getStepZ();
        int z = delta.getX() * facing.getStepX() + delta.getZ() * facing.getStepZ();
        return defaultBlockState().setValue(FACING, facing).setValue(X, x).setValue(Z, z);
    }
}
