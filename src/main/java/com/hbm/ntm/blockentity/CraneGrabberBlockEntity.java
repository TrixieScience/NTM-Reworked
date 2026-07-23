package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.AbstractCraneBlock;
import com.hbm.ntm.block.ConveyorBlock;
import com.hbm.ntm.conveyor.ConveyorBelt;
import com.hbm.ntm.conveyor.ConveyorType;
import com.hbm.ntm.entity.MovingConveyorItemEntity;
import com.hbm.ntm.inventory.CraneGrabberMenu;
import com.hbm.ntm.item.MachineUpgradeItem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CraneGrabberBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int FILTER_START = 0;
    public static final int FILTER_END = 9;
    public static final int STACK_UPGRADE = 9;
    public static final int EJECTOR_UPGRADE = 10;
    public static final int SLOT_COUNT = 11;
    public static final String MODE_EXACT = "exact";
    public static final String MODE_WILDCARD = "wildcard";
    private static final int[] NO_SLOTS = {};

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final String[] filterModes = new String[FILTER_END];
    private boolean whitelist;
    private long lastGrabbedTick;
    private Component customName;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index == 0) return whitelist ? 1 : 0;
            return index > 0 && index <= FILTER_END ? modeCode(index - 1) : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                whitelist = value != 0;
            } else if (index > 0 && index <= FILTER_END) {
                setModeCode(index - 1, value);
            }
        }

        @Override
        public int getCount() {
            return FILTER_END + 1;
        }
    };

    public CraneGrabberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRANE_GRABBER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CraneGrabberBlockEntity grabber) {
        grabber.serverTick(level, pos, state);
    }

    private void serverTick(Level level, BlockPos pos, BlockState state) {
        int delay = switch (upgradeLevel(EJECTOR_UPGRADE, MachineUpgradeItem.Type.EJECTOR)) {
            case 1 -> 10;
            case 2 -> 5;
            case 3 -> 2;
            default -> 20;
        };
        if (level.getGameTime() < lastGrabbedTick + delay || level.hasNeighborSignal(pos)) return;

        Direction input = state.getValue(AbstractCraneBlock.INPUT);
        Direction output = state.getValue(AbstractCraneBlock.OUTPUT);
        BlockPos outputPos = pos.relative(output);
        BlockState outputState = level.getBlockState(outputPos);
        AABB search = searchBox(level, pos, input);
        List<MovingConveyorItemEntity> passing = level.getEntitiesOfClass(
                MovingConveyorItemEntity.class, search, entity -> entity.isAlive() && !entity.getItemStack().isEmpty());

        if (outputState.getBlock() instanceof ConveyorBelt belt) {
            moveOntoBelt(level, pos, output, outputPos, outputState, belt, passing);
            return;
        }

        IItemHandler target = targetHandler(level, outputPos, output.getOpposite());
        if (target == null) return;
        int amount = switch (upgradeLevel(STACK_UPGRADE, MachineUpgradeItem.Type.STACK)) {
            case 1 -> 4;
            case 2 -> 16;
            case 3 -> 64;
            default -> 1;
        };
        moveIntoInventory(level, passing, target, amount);
    }

    private void moveOntoBelt(Level level, BlockPos pos, Direction output, BlockPos beltPos,
                              BlockState beltState, ConveyorBelt belt,
                              List<MovingConveyorItemEntity> passing) {
        for (MovingConveyorItemEntity item : passing) {
            ItemStack stack = item.getItemStack();
            if (!allowed(stack)) continue;
            lastGrabbedTick = level.getGameTime();
            Vec3 start = pos.getCenter().add(output.getStepX() * 0.55D,
                    output.getStepY() * 0.55D, output.getStepZ() * 0.55D);
            Vec3 snap = belt.closestSnappingPosition(level, beltPos, beltState, start);
            MovingConveyorItemEntity moved = MovingConveyorItemEntity.create(level, stack.copy());
            moved.setPos(snap.x, snap.y, snap.z);
            item.discard();
            level.addFreshEntity(moved);
            setChanged();
            return;
        }
    }

    private void moveIntoInventory(Level level, List<MovingConveyorItemEntity> passing,
                                   IItemHandler target, int amount) {
        for (MovingConveyorItemEntity item : passing) {
            ItemStack stack = item.getItemStack().copy();
            if (!allowed(stack)) continue;
            lastGrabbedTick = level.getGameTime();

            int toAdd = Math.min(stack.getCount(), amount);
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(target, stack.copyWithCount(toAdd), false);
            int accepted = toAdd - remainder.getCount();
            if (accepted > 0) {
                stack.shrink(accepted);
                if (stack.isEmpty()) item.discard();
                else item.setItemStack(stack);
                amount -= accepted;
            }
            setChanged();
            if (amount <= 0) return;
        }
    }

    private static AABB searchBox(Level level, BlockPos pos, Direction input) {
        double reach = 1.0D;
        if (input.getAxis().isHorizontal()) {
            BlockState inputState = level.getBlockState(pos.relative(input));
            if (inputState.getBlock() instanceof ConveyorBlock conveyor) {
                if (conveyor.type() == ConveyorType.DOUBLE) reach = 0.5D;
                if (conveyor.type() == ConveyorType.TRIPLE) reach = 0.33D;
            }
        }
        double x = pos.getX() + input.getStepX() * reach;
        double y = pos.getY() + input.getStepY() * reach;
        double z = pos.getZ() + input.getStepZ() * reach;
        return new AABB(x + 0.1875D, y + 0.1875D, z + 0.1875D,
                x + 0.8125D, y + 0.8125D, z + 0.8125D);
    }

    @Nullable
    private static IItemHandler targetHandler(Level level, BlockPos pos, Direction face) {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, face);
        if (handler != null) return handler;
        if (level.getBlockEntity(pos) instanceof WorldlyContainer sided) {
            return new SidedInvWrapper(sided, face);
        }
        if (level.getBlockEntity(pos) instanceof net.minecraft.world.Container container) {
            return new InvWrapper(container);
        }
        return null;
    }

    private boolean allowed(ItemStack stack) {
        boolean match = matchesFilter(stack);
        return whitelist ? match : !match;
    }

    public boolean matchesFilter(ItemStack stack) {
        for (int slot = FILTER_START; slot < FILTER_END; slot++) {
            ItemStack filter = items.get(slot);
            if (filter.isEmpty()) continue;
            String mode = normalizedMode(slot);
            if (MODE_EXACT.equals(mode)) {
                if (ItemStack.isSameItemSameComponents(filter, stack)) return true;
                continue;
            }
            if (MODE_WILDCARD.equals(mode)) {
                if (filter.is(stack.getItem())) return true;
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(mode);
            if (id != null && stack.is(TagKey.create(Registries.ITEM, id))) return true;
        }
        return false;
    }

    public void setFilter(int slot, ItemStack stack) {
        if (slot < FILTER_START || slot >= FILTER_END) return;
        items.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        filterModes[slot] = stack.isEmpty() ? null
                : ItemStack.isSameItemSameComponents(stack, stack.getItem().getDefaultInstance())
                ? MODE_WILDCARD : MODE_EXACT;
        setChanged();
    }

    public void nextFilterMode(int slot) {
        if (slot < FILTER_START || slot >= FILTER_END || items.get(slot).isEmpty()) return;
        List<String> tags = filterTags(slot);
        String mode = normalizedMode(slot);
        if (MODE_EXACT.equals(mode)) {
            filterModes[slot] = MODE_WILDCARD;
        } else if (MODE_WILDCARD.equals(mode)) {
            filterModes[slot] = tags.isEmpty() ? MODE_EXACT : tags.getFirst();
        } else {
            int index = tags.indexOf(mode);
            filterModes[slot] = index >= 0 && index + 1 < tags.size() ? tags.get(index + 1) : MODE_EXACT;
        }
        setChanged();
    }

    public String filterMode(int slot) {
        return slot >= FILTER_START && slot < FILTER_END ? normalizedMode(slot) : MODE_EXACT;
    }

    public boolean whitelist() {
        return whitelist;
    }

    public void toggleWhitelist() {
        whitelist = !whitelist;
        setChanged();
    }

    public ContainerData dataAccess() {
        return data;
    }

    private String normalizedMode(int slot) {
        String mode = filterModes[slot];
        if (mode == null || mode.isEmpty()) {
            filterModes[slot] = mode = MODE_EXACT;
        }
        return mode;
    }

    private List<String> filterTags(int slot) {
        ItemStack filter = items.get(slot);
        if (filter.isEmpty()) return List.of();
        return filter.getItem().builtInRegistryHolder().tags()
                .map(tag -> tag.location().toString()).toList();
    }

    private int modeCode(int slot) {
        String mode = normalizedMode(slot);
        if (MODE_EXACT.equals(mode)) return 0;
        if (MODE_WILDCARD.equals(mode)) return 1;
        int tag = filterTags(slot).indexOf(mode);
        return tag < 0 ? 0 : tag + 2;
    }

    private void setModeCode(int slot, int code) {
        if (code <= 0) {
            filterModes[slot] = MODE_EXACT;
        } else if (code == 1) {
            filterModes[slot] = MODE_WILDCARD;
        } else {
            List<String> tags = filterTags(slot);
            int index = code - 2;
            filterModes[slot] = index >= 0 && index < tags.size() ? tags.get(index) : MODE_EXACT;
        }
    }

    private int upgradeLevel(int slot, MachineUpgradeItem.Type type) {
        return items.get(slot).getItem() instanceof MachineUpgradeItem upgrade && upgrade.type() == type
                ? upgrade.level() : 0;
    }

    public void dropRealContents() {
        if (level == null) return;
        for (int slot = STACK_UPGRADE; slot < SLOT_COUNT; slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack.copy());
                items.set(slot, ItemStack.EMPTY);
            }
        }
        setChanged();
    }

    public void setCustomName(Component name) {
        customName = name;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return customName != null ? customName : Component.translatable("container.craneGrabber");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new CraneGrabberMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putBoolean("isWhitelist", whitelist);
        tag.putLong("lastGrabbedTick", lastGrabbedTick);
        for (int i = 0; i < filterModes.length; i++) {
            if (filterModes[i] != null) tag.putString("mode" + i, filterModes[i]);
        }
        if (customName != null) tag.putString("name", Component.Serializer.toJson(customName, registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        whitelist = tag.getBoolean("isWhitelist");
        lastGrabbedTick = tag.getLong("lastGrabbedTick");
        for (int i = 0; i < filterModes.length; i++) {
            filterModes[i] = tag.contains("mode" + i) ? tag.getString("mode" + i) : null;
        }
        customName = tag.contains("name") ? Component.Serializer.fromJson(tag.getString("name"), registries) : null;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        int limit = slot < FILTER_END ? 1 : Math.min(64, stack.getMaxStackSize());
        items.set(slot, stack.copyWithCount(Math.min(stack.getCount(), limit)));
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 400.0D;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (!(stack.getItem() instanceof MachineUpgradeItem upgrade)) return false;
        return slot == STACK_UPGRADE && upgrade.type() == MachineUpgradeItem.Type.STACK
                || slot == EJECTOR_UPGRADE && upgrade.type() == MachineUpgradeItem.Type.EJECTOR;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
