package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FluidBarrelBlockEntity;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class FluidBarrelBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 16, 14);

    public enum Type {
        PLASTIC(12_000, true),
        CORRODED(6_000, false),
        STEEL(16_000, true),
        TCALLOY(24_000, true),
        ANTIMATTER(16_000, true);

        private final int capacity;
        private final boolean storesFluid;

        Type(int capacity, boolean storesFluid) {
            this.capacity = capacity;
            this.storesFluid = storesFluid;
        }

        public int capacity() {
            return capacity;
        }

        public boolean storesFluid() {
            return storesFluid;
        }
    }

    private final MapCodec<FluidBarrelBlock> codec = MapCodec.unit(this);
    private final Type type;

    public FluidBarrelBlock(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public Type type() {
        return type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                               BlockPos pos, Player player, InteractionHand hand,
                                               BlockHitResult hit) {
        if (!player.isShiftKeyDown() || !type.storesFluid()
                || !(stack.getItem() instanceof FluidIdentifierItem)
                || !(level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        FluidIdentifierItem.Selection selection = FluidIdentifierItem.primary(stack);
        if (!level.isClientSide) {
            barrel.selectFluid(selection);
            player.displayClientMessage(Component.literal("Changed type to ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.translatable(selection.translationKey())).append("!"), false);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!type.storesFluid()) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) return InteractionResult.sidedSuccess(level.isClientSide);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel) {
            serverPlayer.openMenu(barrel, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel) {
            barrel.restoreFromItem(stack);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                            boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel) {
            Containers.dropContents(level, pos, barrel);
            barrel.clearContent();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity supplied = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (supplied instanceof FluidBarrelBlockEntity barrel) return List.of(barrel.machineDrop());
        return List.of(new ItemStack(this));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return type.storesFluid();
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FluidBarrelBlockEntity barrel
                ? barrel.comparatorSignal() : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return type.storesFluid() ? new FluidBarrelBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        return type.storesFluid()
                ? createTickerHelper(blockEntityType, ModBlockEntities.FLUID_BARREL.get(),
                FluidBarrelBlockEntity::tick)
                : null;
    }
}
