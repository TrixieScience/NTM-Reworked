package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.recipe.CentrifugeRecipes.CentrifugeRecipe;
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
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public final class CentrifugeRecipeCategory implements IRecipeCategory<CentrifugeRecipe> {
    private static final int[] OUTPUT_X = {70, 88, 70, 88};
    private static final int[] OUTPUT_Y = {9, 9, 27, 27};

    private final IDrawable icon;
    private final IDrawable arrow;

    public CentrifugeRecipeCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemLike(ModItems.MACHINE_CENTRIFUGE_ITEM.get());
        arrow = gui.getRecipeArrow();
    }

    @Override
    public RecipeType<CentrifugeRecipe> getRecipeType() {
        return HbmJeiPlugin.CENTRIFUGE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm.centrifuge");
    }

    @Override
    public int getWidth() {
        return 116;
    }

    @Override
    public int getHeight() {
        return 54;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CentrifugeRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(8, 19).setStandardSlotBackground().addIngredients(recipe.input());
        List<Supplier<ItemStack>> outputs = recipe.outputs();
        for (int i = 0; i < outputs.size() && i < OUTPUT_X.length; i++) {
            builder.addOutputSlot(OUTPUT_X[i], OUTPUT_Y[i])
                    .setOutputSlotBackground()
                    .addItemStack(outputs.get(i).get().copy());
        }
    }

    @Override
    public void draw(CentrifugeRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrow.draw(graphics, 34, 19);
    }
}
