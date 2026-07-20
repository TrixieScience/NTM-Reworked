package com.hbm.ntm.blockentity;

import com.hbm.ntm.inventory.NukePrototypeMenu;
import com.hbm.ntm.item.BreedingRodItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Fourteen slots of prototype-quality arming logic. */
public final class NukePrototypeBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int SLOTS = 14;
    private static final int[] NO_AUTOMATION = new int[0];

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private Component customName;
    private boolean detonating;

    public NukePrototypeBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.NUKE_PROTOTYPE.get(), position, state);
    }

    /**
     * Fourteen-slot arming puzzle:
     * <ul>
     *   <li>slots 0, 1, 12, 13 = {@code cell_sas3} (matched by {@code getItem()})</li>
     *   <li>slots 2, 3, 10, 11 = {@code rod_quad} of {@code BreedingRodType.URANIUM}</li>
     *   <li>slots 4, 5, 8, 9   = {@code rod_quad} of {@code BreedingRodType.LEAD}</li>
     *   <li>slots 6, 7         = {@code rod_quad} of {@code BreedingRodType.NP237}</li>
     * </ul>
     */
    public boolean isReady() {
        for (ItemStack stack : items) {
            if (stack.isEmpty()) return false;
        }
        return items.get(0).is(ModItems.CELL_SAS3.get()) && items.get(1).is(ModItems.CELL_SAS3.get())
                && items.get(12).is(ModItems.CELL_SAS3.get()) && items.get(13).is(ModItems.CELL_SAS3.get())
                && hasCorrectRodLayout();
    }

    public boolean hasCorrectRodLayout() {
        Item rod = ModItems.ROD_QUAD.get();
        return isRod(2, rod, BreedingRodItem.Type.URANIUM)
                && isRod(3, rod, BreedingRodItem.Type.URANIUM)
                && isRod(4, rod, BreedingRodItem.Type.LEAD)
                && isRod(5, rod, BreedingRodItem.Type.LEAD)
                && isRod(6, rod, BreedingRodItem.Type.NP237)
                && isRod(7, rod, BreedingRodItem.Type.NP237)
                && isRod(8, rod, BreedingRodItem.Type.LEAD)
                && isRod(9, rod, BreedingRodItem.Type.LEAD)
                && isRod(10, rod, BreedingRodItem.Type.URANIUM)
                && isRod(11, rod, BreedingRodItem.Type.URANIUM);
    }

    private boolean isRod(int slot, Item rod, BreedingRodItem.Type type) {
        ItemStack stack = items.get(slot);
        return stack.is(rod) && BreedingRodItem.type(stack) == type;
    }

    public void clearForDetonation() {
        detonating = true;
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
        setChanged();
    }

    public boolean detonating() { return detonating; }

    public void dropContents() {
        if (level == null) return;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level,
                        worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D, stack);
            }
        }
        for (int i = 0; i < items.size(); i++) items.set(i, ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        if (customName != null) tag.putString("name", Component.Serializer.toJson(customName, registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        customName = tag.contains("name") ? Component.Serializer.fromJson(tag.getString("name"), registries) : null;
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }

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
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override public int getMaxStackSize() { return 64; }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public int[] getSlotsForFace(net.minecraft.core.Direction side) { return NO_AUTOMATION; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable net.minecraft.core.Direction side) { return false; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, net.minecraft.core.Direction side) { return false; }

    public void setCustomName(Component name) { customName = name; setChanged(); }
    @Override public Component getDisplayName() { return customName != null ? customName : Component.translatable("container.nukePrototype"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new NukePrototypeMenu(id, inventory, this);
    }
}
