package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.blockentity.CombustionEngineProxyBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Six blocks wide, two deep, and still somehow needs four more connection blocks. */
public final class CombustionEngineBlock extends BaseEntityBlock {
    public static final MapCodec<CombustionEngineBlock> CODEC = simpleCodec(CombustionEngineBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART_DEPTH = IntegerProperty.create("part_depth", 0, 3);
    public static final IntegerProperty PART_SIDE = IntegerProperty.create("part_side", 0, 5);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, 1);
    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);
    private static final VoxelShape FULL = Shapes.block();

    public CombustionEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(PART_DEPTH, 2).setValue(PART_SIDE, 3).setValue(PART_Y, 0));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos core = context.getClickedPos();
        for (BlockPos part : partPositions(core, facing)) {
            if (!part.equals(core) && !context.getLevel().getBlockState(part).canBeReplaced(context)) return null;
        }
        return stateForPart(core, core, facing);
    }

    @Override public void setPlacedBy(Level level, BlockPos position, BlockState state,
                                      @Nullable LivingEntity placer, ItemStack stack) {
        BlockPos core = corePosition(position, state);
        Direction facing = state.getValue(FACING);
        for (BlockPos part : partPositions(core, facing)) level.setBlock(part, stateForPart(part, core, facing), Block.UPDATE_ALL);
        if (level.getBlockEntity(core) instanceof CombustionEngineBlockEntity engine
                && stack.has(DataComponents.CUSTOM_NAME)) engine.setCustomName(stack.getHoverName());
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                             CollisionContext context) { return FULL; }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                          Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        BlockPos core = corePosition(pos, state);
        if (!level.isClientSide && player instanceof ServerPlayer server
                && level.getBlockEntity(core) instanceof CombustionEngineBlockEntity engine) {
            server.openMenu(engine, buffer -> buffer.writeBlockPos(core));
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
            if (!level.isClientSide && level.getBlockEntity(core) instanceof CombustionEngineBlockEntity engine) {
                Containers.dropContents(level, core, engine);
                engine.clearContent();
            }
            for (BlockPos part : partPositions(core, facing)) if (!part.equals(pos) && level.getBlockState(part).is(this)) {
                level.setBlock(part, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        } finally {
            REMOVING.set(false);
        }
        super.onRemove(state, level, pos, replacement, moved);
    }

    @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS) != null) return List.of();
        return List.of(new ItemStack(ModItems.MACHINE_COMBUSTION_ENGINE_ITEM.get()));
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (isCore(state)) return new CombustionEngineBlockEntity(pos, state);
        return isPort(state) ? new CombustionEngineProxyBlockEntity(pos, state) : null;
    }

    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return isCore(state) ? createTickerHelper(type, ModBlockEntities.MACHINE_COMBUSTION_ENGINE.get(),
                CombustionEngineBlockEntity::tick) : null;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART_DEPTH, PART_SIDE, PART_Y);
    }

    public static boolean isCore(BlockState state) {
        return state.getValue(PART_DEPTH) == 2 && state.getValue(PART_SIDE) == 3 && state.getValue(PART_Y) == 0;
    }

    public static boolean isPort(BlockState state) {
        int depth = state.getValue(PART_DEPTH) - 2;
        int side = state.getValue(PART_SIDE) - 3;
        return state.getValue(PART_Y) == 0 && (depth == 1 || depth == -2) && Math.abs(side) == 1;
    }

    public static boolean canConnectAt(BlockState state, @Nullable Direction side) {
        if (!isPort(state)) return false;
        int depth = state.getValue(PART_DEPTH) - 2;
        Direction facing = state.getValue(FACING);
        return side == (depth == 1 ? facing : facing.getOpposite());
    }

    public static BlockPos corePosition(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction lateral = facing.getClockWise();
        return pos.relative(facing, 2 - state.getValue(PART_DEPTH))
                .relative(lateral, 3 - state.getValue(PART_SIDE)).below(state.getValue(PART_Y));
    }

    public static List<BlockPos> partPositions(BlockPos core, Direction facing) {
        Direction lateral = facing.getClockWise();
        List<BlockPos> positions = new ArrayList<>(28);
        for (int y = 0; y <= 1; y++) for (int depth = -1; depth <= 0; depth++) {
            for (int side = -3; side <= 2; side++) positions.add(core.relative(facing, depth).relative(lateral, side).above(y));
        }
        for (int depth : new int[]{1, -2}) for (int side : new int[]{-1, 1}) {
            positions.add(core.relative(facing, depth).relative(lateral, side));
        }
        return positions;
    }

    public static List<Connection> connections(BlockPos core, Direction facing) {
        Direction lateral = facing.getClockWise();
        List<Connection> result = new ArrayList<>(4);
        for (int depth : new int[]{1, -2}) {
            Direction outward = depth == 1 ? facing : facing.getOpposite();
            for (int side : new int[]{-1, 1}) {
                BlockPos port = core.relative(facing, depth).relative(lateral, side);
                result.add(new Connection(port, port.relative(outward), outward));
            }
        }
        return result;
    }

    private BlockState stateForPart(BlockPos part, BlockPos core, Direction facing) {
        BlockPos delta = part.subtract(core);
        Direction lateral = facing.getClockWise();
        int depth = delta.getX() * facing.getStepX() + delta.getZ() * facing.getStepZ();
        int side = delta.getX() * lateral.getStepX() + delta.getZ() * lateral.getStepZ();
        return defaultBlockState().setValue(FACING, facing).setValue(PART_DEPTH, depth + 2)
                .setValue(PART_SIDE, side + 3).setValue(PART_Y, delta.getY());
    }

    public record Connection(BlockPos port, BlockPos target, Direction outward) { }
}
