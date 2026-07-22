package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.recipe.AmmoPressRecipes;
import com.hbm.ntm.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

public final class AmmoPressRecipeCategory implements IRecipeCategory<AmmoPressRecipes.Recipe> {
    private final IDrawable icon;

    public AmmoPressRecipeCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemLike(ModItems.AMMO_PRESS_ITEM.get());
    }

    @Override public RecipeType<AmmoPressRecipes.Recipe> getRecipeType() { return HbmJeiPlugin.AMMO_PRESS; }
    @Override public Component getTitle() { return Component.translatable("jei.hbm.ammo_press"); }
    @Override public int getWidth() { return 92; }
    @Override public int getHeight() { return 58; }
    @Override public IDrawable getIcon() { return icon; }

    @Override public void setRecipe(IRecipeLayoutBuilder builder, AmmoPressRecipes.Recipe recipe, IFocusGroup focuses) {
        for (int slot = 0; slot < 9; slot++) {
            AmmoPressRecipes.SlotIngredient ingredient = recipe.input(slot);
            if (ingredient != null) builder.addInputSlot(1 + 18 * (slot % 3), 1 + 18 * (slot / 3))
                    .setStandardSlotBackground().addItemStack(ingredient.displayStack());
        }
        builder.addOutputSlot(73, 19).setOutputSlotBackground().addItemStack(recipe.output());
    }
}
