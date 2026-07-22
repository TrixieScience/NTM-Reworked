package com.hbm.ntm.blockentity;

import com.hbm.ntm.inventory.AmmoPressMenu;
import com.hbm.ntm.recipe.AmmoPressRecipes;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class AmmoPressBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int INPUT_COUNT = 9;
    public static final int OUTPUT_SLOT = 9;
    public static final int SLOT_COUNT = 10;
    private static final int[] ALL_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? selectedRecipe : animationSerial; }
        @Override public void set(int index, int value) {
            if (index == 0) selectedRecipe = value;
            else if (animationSerial != value) { animationSerial = value; animationTicks = 120; }
        }
        @Override public int getCount() { return 2; }
    };
    private Component customName;
    private int selectedRecipe = -1;
    private int animationSerial;
    private int animationTicks;

    public AmmoPressBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.AMMO_PRESS.get(), position, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AmmoPressBlockEntity press) {
        if (level.isClientSide) {
            if (press.animationTicks > 0) press.animationTicks--;
        } else {
            press.process((ServerLevel) level, pos, state);
        }
    }

    private void process(ServerLevel level, BlockPos pos, BlockState state) {
        AmmoPressRecipes.Recipe recipe = AmmoPressRecipes.get(selectedRecipe);
        if (recipe == null || !matches(recipe)) return;
        ItemStack output = items.get(OUTPUT_SLOT);
        ItemStack result = recipe.output();
        int operations = maxOperations(recipe, output, result);
        if (operations <= 0) return;
        for (int slot = 0; slot < INPUT_COUNT; slot++) {
            AmmoPressRecipes.SlotIngredient ingredient = recipe.input(slot);
            if (ingredient != null) items.get(slot).shrink(ingredient.count() * operations);
        }
        if (output.isEmpty()) items.set(OUTPUT_SLOT, result.copyWithCount(result.getCount() * operations));
        else output.grow(result.getCount() * operations);
        animationSerial++;
        animationTicks = 120;
        setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    private boolean matches(AmmoPressRecipes.Recipe recipe) {
        for (int slot = 0; slot < INPUT_COUNT; slot++) {
            AmmoPressRecipes.SlotIngredient ingredient = recipe.input(slot);
            ItemStack stack = items.get(slot);
            if (ingredient == null ? !stack.isEmpty() : !ingredient.matches(stack, false)) return false;
        }
        ItemStack output = items.get(OUTPUT_SLOT);
        ItemStack result = recipe.output();
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, result);
    }

    private int maxOperations(AmmoPressRecipes.Recipe recipe, ItemStack output, ItemStack result) {
        int operations = Integer.MAX_VALUE;
        for (int slot = 0; slot < INPUT_COUNT; slot++) {
            AmmoPressRecipes.SlotIngredient ingredient = recipe.input(slot);
            if (ingredient != null) operations = Math.min(operations, items.get(slot).getCount() / ingredient.count());
        }
        int room = (output.isEmpty() ? result.getMaxStackSize() : output.getMaxStackSize() - output.getCount()) / result.getCount();
        return Math.min(operations, room);
    }

    public void selectRecipe(int recipe) {
        if (recipe < -1 || recipe >= AmmoPressRecipes.all().size()) return;
        selectedRecipe = selectedRecipe == recipe ? -1 : recipe;
        setChanged();
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("recipe", selectedRecipe);
        tag.putInt("animation", animationSerial);
        if (customName != null) tag.putString("name", Component.Serializer.toJson(customName, registries));
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        selectedRecipe = tag.contains("recipe") ? tag.getInt("recipe") : -1;
        animationSerial = tag.getInt("animation");
        customName = tag.contains("name") ? Component.Serializer.fromJson(tag.getString("name"), registries) : null;
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("recipe", selectedRecipe);
        tag.putInt("animation", animationSerial);
        return tag;
    }
    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        selectedRecipe = tag.getInt("recipe");
        int serial = tag.getInt("animation");
        if (serial != animationSerial) animationTicks = 120;
        animationSerial = serial;
    }
    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override public Component getDisplayName() { return customName != null ? customName : Component.translatable("container.ammo_press"); }
    public void setCustomName(Component name) { customName = name; }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AmmoPressMenu(id, inventory, this, data);
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) setChanged(); return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack); if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize()); setChanged();
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 128.0D;
    }
    @Override public void clearContent() { items.clear(); }
    @Override public int[] getSlotsForFace(Direction side) { return ALL_SLOTS; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot >= INPUT_COUNT) return false;
        AmmoPressRecipes.Recipe recipe = AmmoPressRecipes.get(selectedRecipe);
        AmmoPressRecipes.SlotIngredient ingredient = recipe == null ? null : recipe.input(slot);
        return ingredient != null && ingredient.matches(stack, true);
    }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) { return canPlaceItem(slot, stack); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return slot == OUTPUT_SLOT; }

    public int selectedRecipe() { return selectedRecipe; }
    public int animationTicks() { return animationTicks; }
}
