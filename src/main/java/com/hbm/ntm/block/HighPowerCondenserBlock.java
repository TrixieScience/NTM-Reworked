package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.HighPowerCondenserBlockEntity;
import com.hbm.ntm.blockentity.HighPowerCondenserProxyBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Sixty-three blocks of condenser, six holes you're actually allowed to use. */
public final class HighPowerCondenserBlock extends BaseEntityBlock {
    public static final MapCodec<HighPowerCondenserBlock> CODEC = simpleCodec(HighPowerCondenserBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART_LENGTH = IntegerProperty.create("part_length", 0, 2);
    public static final IntegerProperty PART_SIDE = IntegerProperty.create("part_side", 0, 6);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, 2);

    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);
    private static final VoxelShape FULL = Shapes.block();

    public HighPowerCondenserBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(PART_LENGTH, 1).setValue(PART_SIDE, 3).setValue(PART_Y, 0));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos clicked = context.getClickedPos();
        // Source getOffset() == 1: the clicked end cap sits one block ahead of the core.
        BlockPos core = clicked.relative(facing.getOpposite());
        for (BlockPos part : partPositions(core, facing)) {
            if (!part.equals(clicked) && !context.getLevel().getBlockState(part).canBeReplaced(context)) return null;
        }
        return stateForPart(clicked, core, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos position, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        BlockPos core = corePosition(position, state);
        Direction facing = state.getValue(FACING);
        for (BlockPos part : partPositions(core, facing)) {
            level.setBlock(part, stateForPart(part, core, facing), Block.UPDATE_ALL);
        }
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position,
                                             CollisionContext context) { return FULL; }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos position,
                            BlockState newState, boolean movedByPiston) {
        if (state.is(newState.getBlock()) || REMOVING.get()) {
            super.onRemove(state, level, position, newState, movedByPiston);
            return;
        }
        BlockPos core = corePosition(position, state);
        Direction facing = state.getValue(FACING);
        REMOVING.set(true);
        try {
            for (BlockPos part : partPositions(core, facing)) {
                if (!part.equals(position) && level.getBlockState(part).is(this)) {
                    level.setBlock(part, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                }
            }
        } finally {
            REMOVING.set(false);
        }
        super.onRemove(state, level, position, newState, movedByPiston);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS) != null) return List.of();
        return List.of(new ItemStack(ModItems.MACHINE_CONDENSER_POWERED_ITEM.get()));
    }

    @Override public boolean dropFromExplosion(Explosion explosion) { return false; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        if (isCore(state)) return new HighPowerCondenserBlockEntity(position, state);
        return isPort(state) ? new HighPowerCondenserProxyBlockEntity(position, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return isCore(state) ? createTickerHelper(type, ModBlockEntities.MACHINE_CONDENSER_POWERED.get(),
                HighPowerCondenserBlockEntity::tick) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART_LENGTH, PART_SIDE, PART_Y);
    }

    public static boolean isCore(BlockState state) {
        return state.getValue(PART_LENGTH) == 1 && state.getValue(PART_SIDE) == 3
                && state.getValue(PART_Y) == 0;
    }

    public static boolean isPort(BlockState state) {
        if (state.getValue(PART_Y) != 1) return false;
        int length = state.getValue(PART_LENGTH) - 1;
        int side = state.getValue(PART_SIDE) - 3;
        return length == 0 && Math.abs(side) == 3 || Math.abs(length) == 1 && Math.abs(side) == 1;
    }

    @Nullable
    public static Direction outwardDirection(BlockState state) {
        if (!isPort(state)) return null;
        Direction facing = state.getValue(FACING);
        int length = state.getValue(PART_LENGTH) - 1;
        int side = state.getValue(PART_SIDE) - 3;
        if (length > 0) return facing;
        if (length < 0) return facing.getOpposite();
        return side > 0 ? facing.getClockWise() : facing.getCounterClockWise();
    }

    public static boolean canConnectAt(BlockState state, @Nullable Direction side) {
        return side != null && side == outwardDirection(state);
    }

    public static BlockPos corePosition(BlockPos position, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction lateral = facing.getClockWise();
        return position.relative(facing, 1 - state.getValue(PART_LENGTH))
                .relative(lateral, 3 - state.getValue(PART_SIDE)).below(state.getValue(PART_Y));
    }

    public static List<BlockPos> partPositions(BlockPos core, Direction facing) {
        Direction lateral = facing.getClockWise();
        List<BlockPos> positions = new ArrayList<>(63);
        for (int y = 0; y <= 2; y++) {
            for (int length = -1; length <= 1; length++) {
                for (int side = -3; side <= 3; side++) {
                    positions.add(core.relative(facing, length).relative(lateral, side).above(y));
                }
            }
        }
        return positions;
    }

    public static List<Connection> connections(BlockPos core, Direction facing) {
        Direction lateral = facing.getClockWise();
        List<Connection> connections = new ArrayList<>(6);
        addConnection(connections, core.relative(lateral, 3).above(), lateral);
        addConnection(connections, core.relative(lateral, -3).above(), lateral.getOpposite());
        for (int length : new int[]{-1, 1}) {
            Direction outward = length > 0 ? facing : facing.getOpposite();
            addConnection(connections, core.relative(facing, length).relative(lateral).above(), outward);
            addConnection(connections, core.relative(facing, length).relative(lateral, -1).above(), outward);
        }
        return connections;
    }

    private static void addConnection(List<Connection> connections, BlockPos port, Direction outward) {
        connections.add(new Connection(port, port.relative(outward), outward));
    }

    public BlockState stateForPart(BlockPos part, BlockPos core, Direction facing) {
        Direction lateral = facing.getClockWise();
        BlockPos delta = part.subtract(core);
        int length = delta.getX() * facing.getStepX() + delta.getZ() * facing.getStepZ();
        int side = delta.getX() * lateral.getStepX() + delta.getZ() * lateral.getStepZ();
        return defaultBlockState().setValue(FACING, facing)
                .setValue(PART_LENGTH, length + 1).setValue(PART_SIDE, side + 3)
                .setValue(PART_Y, delta.getY());
    }

    public record Connection(BlockPos port, BlockPos target, Direction outward) { }
}
