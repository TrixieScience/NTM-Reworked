package com.hbm.ntm.inventory;

import com.hbm.ntm.blockentity.AmmoPressBlockEntity;
import com.hbm.ntm.recipe.AmmoPressRecipes;
import com.hbm.ntm.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AmmoPressMenu extends AbstractContainerMenu {
    private final Container press;
    private final ContainerData data;

    public AmmoPressMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, findPress(inventory, buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public AmmoPressMenu(int id, Inventory inventory, Container press, ContainerData data) {
        super(ModMenus.AMMO_PRESS.get(), id);
        checkContainerSize(press, AmmoPressBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, 2);
        this.press = press;
        this.data = data;
        for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++)
            addSlot(new Slot(press, column + row * 3, 116 + column * 18, 18 + row * 18));
        addSlot(new Slot(press, AmmoPressBlockEntity.OUTPUT_SLOT, 134, 72) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 118 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 176));
        addDataSlots(data);
    }

    private static Container findPress(Inventory inventory, BlockPos position) {
        return inventory.player.level().getBlockEntity(position) instanceof AmmoPressBlockEntity press
                ? press : new SimpleContainer(AmmoPressBlockEntity.SLOT_COUNT);
    }

    @Override public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= AmmoPressRecipes.all().size()) return false;
        if (press instanceof AmmoPressBlockEntity blockEntity) blockEntity.selectRecipe(id);
        return true;
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < AmmoPressBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, AmmoPressBlockEntity.SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            AmmoPressRecipes.Recipe recipe = AmmoPressRecipes.get(selectedRecipe());
            boolean moved = false;
            if (recipe != null) for (int i = 0; i < AmmoPressBlockEntity.INPUT_COUNT && !moved; i++) {
                AmmoPressRecipes.SlotIngredient ingredient = recipe.input(i);
                if (ingredient != null && ingredient.matches(stack, true)) moved = moveItemStackTo(stack, i, i + 1, false);
            }
            if (!moved) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }

    @Override public boolean stillValid(Player player) { return press.stillValid(player); }
    public int selectedRecipe() { return data.get(0); }
}
