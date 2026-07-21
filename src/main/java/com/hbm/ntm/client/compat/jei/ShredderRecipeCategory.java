package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.recipe.ShredderRecipes.ShredderRecipe;
import com.hbm.ntm.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class ShredderRecipeCategory implements IRecipeCategory<ShredderRecipe> {
    private final IDrawable icon;
    private final IDrawable arrow;

    public ShredderRecipeCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemLike(ModItems.MACHINE_SHREDDER_ITEM.get());
        arrow = gui.getRecipeArrow();
    }

    @Override
    public RecipeType<ShredderRecipe> getRecipeType() {
        return HbmJeiPlugin.SHREDDER;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm.shredder");
    }

    @Override
    public int getWidth() {
        return 100;
    }

    @Override
    public int getHeight() {
        return 26;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ShredderRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(8, 5).setStandardSlotBackground().addItemStacks(recipe.display().get());
        builder.addOutputSlot(74, 5).setOutputSlotBackground().addItemStack(recipe.output().get().copy());
    }

    @Override
    public void draw(ShredderRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrow.draw(graphics, 34, 5);
    }
}
