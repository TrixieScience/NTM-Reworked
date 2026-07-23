package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FluidStorageTankBlockEntity;
import com.hbm.ntm.blockentity.FluidStorageTankProxyBlockEntity;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/** Source machine_fluidtank: a five-wide, three-deep and three-high persistent tank. */
public final class FluidStorageTankBlock extends BaseEntityBlock {
    public static final MapCodec<FluidStorageTankBlock> CODEC = simpleCodec(FluidStorageTankBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty PART_X = IntegerProperty.create("part_x", 0, 4);
    public static final IntegerProperty PART_Z = IntegerProperty.create("part_z", 0, 2);
    public static final IntegerProperty PART_Y = IntegerProperty.create("part_y", 0, 2);
    private static final ThreadLocal<Boolean> REMOVING = ThreadLocal.withInitial(() -> false);
    private static final VoxelShape FULL = Shapes.block();

    public FluidStorageTankBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(PART_X, 2).setValue(PART_Z, 1).setValue(PART_Y, 0));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos clicked = context.getClickedPos();
        BlockPos core = clicked.relative(facing.getOpposite());
        for (BlockPos part : partPositions(core, facing)) {
            if (!part.equals(clicked) && !context.getLevel().getBlockState(part).canBeReplaced(context)) return null;
        }
        return stateForPart(clicked, core, facing);
    }

    @Override public void setPlacedBy(Level level, BlockPos position, BlockState state,
                                      LivingEntity placer, ItemStack stack) {
        BlockPos core = corePosition(position, state);
        Direction facing = state.getValue(FACING);
        for (BlockPos part : partPositions(core, facing)) {
            level.setBlock(part, stateForPart(part, core, facing), Block.UPDATE_ALL);
        }
        if (level.getBlockEntity(core) instanceof FluidStorageTankBlockEntity tank) {
            if (stack.has(DataComponents.CUSTOM_NAME)) tank.setCustomName(stack.getHoverName());
            tank.restoreFromItem(stack);
        }
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos position,
                                             CollisionContext context) { return FULL; }

    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                                         BlockPos position, Player player, InteractionHand hand,
                                                         BlockHitResult hit) {
        if (!(stack.getItem() instanceof FluidIdentifierItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        BlockPos core = corePosition(position, state);
        if (!(level.getBlockEntity(core) instanceof FluidStorageTankBlockEntity tank)) {
            return ItemInteractionResult.FAIL;
        }
        if (tank.damaged()) return ItemInteractionResult.FAIL;
        FluidIdentifierItem.Selection selection = FluidIdentifierItem.primary(stack);
        if (!level.isClientSide) {
            tank.selectFluid(selection);
            player.displayClientMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable(selection.translationKey())).append("!"), false);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos position,
                                                          Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.sidedSuccess(level.isClientSide);
        BlockPos core = corePosition(position, state);
        if (level.getBlockEntity(core) instanceof FluidStorageTankBlockEntity tank && tank.damaged()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(core) instanceof FluidStorageTankBlockEntity tank) {
            serverPlayer.openMenu(tank, buffer -> buffer.writeBlockPos(core));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos position,
                                      BlockState newState, boolean moved) {
        if (state.is(newState.getBlock()) || REMOVING.get()) {
            super.onRemove(state, level, position, newState, moved);
            return;
        }
        BlockPos core = corePosition(position, state);
        REMOVING.set(true);
        try {
            if (!level.isClientSide && level.getBlockEntity(core) instanceof FluidStorageTankBlockEntity tank) {
                Containers.dropContents(level, core, tank);
                tank.clearContent();
            }
            Direction facing = state.getValue(FACING);
            for (BlockPos part : partPositions(core, facing)) {
                if (!part.equals(position) && level.getBlockState(part).is(this)) {
                    level.setBlock(part, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                            Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                }
            }
        } finally {
            REMOVING.set(false);
        }
        super.onRemove(state, level, position, newState, moved);
    }

    @Override protected void onExplosionHit(BlockState state, Level level, BlockPos position,
                                             Explosion explosion, BiConsumer<ItemStack, BlockPos> drops) {
        BlockPos core = corePosition(position, state);
        if (level.getBlockEntity(core) instanceof FluidStorageTankBlockEntity tank
                && tank.damageFrom(explosion)) return;
        super.onExplosionHit(state, level, position, explosion, drops);
    }

    @Override public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (params.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS) != null) return List.of();
        FluidStorageTankBlockEntity tank = findTank(state, params);
        return List.of(tank == null ? new ItemStack(ModItems.MACHINE_FLUIDTANK_ITEM.get()) : tank.machineDrop());
    }

    @Nullable private static FluidStorageTankBlockEntity findTank(BlockState state, LootParams.Builder params) {
        BlockEntity supplied = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (supplied instanceof FluidStorageTankBlockEntity tank) return tank;
        Vec3 origin = params.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin == null) return null;
        BlockPos core = corePosition(BlockPos.containing(origin), state);
        return params.getLevel().getBlockEntity(core) instanceof FluidStorageTankBlockEntity tank ? tank : null;
    }

    @Override public boolean dropFromExplosion(Explosion explosion) { return false; }
    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos position) {
        if (!isCore(state) && !isProxy(state)) return 0;
        BlockPos core = corePosition(position, state);
        return level.getBlockEntity(core) instanceof FluidStorageTankBlockEntity tank
                ? tank.comparatorSignal() : 0;
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        if (isCore(state)) return new FluidStorageTankBlockEntity(position, state);
        return isProxy(state) ? new FluidStorageTankProxyBlockEntity(position, state) : null;
    }

    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return isCore(state) ? createTickerHelper(type, ModBlockEntities.MACHINE_FLUIDTANK.get(),
                FluidStorageTankBlockEntity::tick) : null;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART_X, PART_Z, PART_Y);
    }

    public static boolean isCore(BlockState state) {
        return state.getValue(PART_X) == 2 && state.getValue(PART_Z) == 1 && state.getValue(PART_Y) == 0;
    }

    public static boolean isProxy(BlockState state) {
        int x = state.getValue(PART_X);
        int z = state.getValue(PART_Z);
        return state.getValue(PART_Y) == 0 && (x == 1 || x == 3) && (z == 0 || z == 2);
    }

    public static boolean canConnectAt(BlockState state, @Nullable Direction direction) {
        if (!isProxy(state) || direction == null || !direction.getAxis().isHorizontal()) return false;
        Direction facing = state.getValue(FACING);
        Direction side = facing.getClockWise();
        int x = state.getValue(PART_X);
        int z = state.getValue(PART_Z);
        return x == 1 && direction == side.getOpposite()
                || x == 3 && direction == side
                || z == 0 && direction == facing.getOpposite()
                || z == 2 && direction == facing;
    }

    public static BlockPos corePosition(BlockPos position, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction side = facing.getClockWise();
        return position.relative(side, 2 - state.getValue(PART_X))
                .relative(facing, 1 - state.getValue(PART_Z)).below(state.getValue(PART_Y));
    }

    /** Faux-ladder strip: the core shifted by dir*0.5 - clockwise(dir)*2.25. */
    public static AABB ladderBounds(BlockPos core, Direction facing) {
        Direction clockwise = facing.getClockWise();
        double xOffset = facing.getStepX() * .5D - clockwise.getStepX() * 2.25D;
        double zOffset = facing.getStepZ() * .5D - clockwise.getStepZ() * 2.25D;
        return new AABB(core.getX(), core.getY(), core.getZ(),
                core.getX() + 1D, core.getY() + 2.875D, core.getZ() + 1D).move(xOffset, 0D, zOffset);
    }

    public static List<BlockPos> partPositions(BlockPos core, Direction facing) {
        Direction side = facing.getClockWise();
        List<BlockPos> result = new ArrayList<>(45);
        for (int y = 0; y <= 2; y++) for (int x = -2; x <= 2; x++) for (int z = -1; z <= 1; z++) {
            result.add(core.relative(side, x).relative(facing, z).above(y));
        }
        return result;
    }

    private BlockState stateForPart(BlockPos part, BlockPos core, Direction facing) {
        Direction side = facing.getClockWise();
        BlockPos delta = part.subtract(core);
        int x = delta.getX() * side.getStepX() + delta.getZ() * side.getStepZ() + 2;
        int z = delta.getX() * facing.getStepX() + delta.getZ() * facing.getStepZ() + 1;
        return defaultBlockState().setValue(FACING, facing).setValue(PART_X, x)
                .setValue(PART_Z, z).setValue(PART_Y, delta.getY());
    }
}
