package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.compat.jei.FoundryJeiRecipes.CastingRecipe;
import com.hbm.ntm.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FoundryCastingRecipeCategory implements IRecipeCategory<CastingRecipe> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_foundry.png");

    private final IDrawable background;
    private final IDrawable icon;

    public FoundryCastingRecipeCategory(IGuiHelper gui) {
        background = gui.createDrawable(TEXTURE, 5, 11, 166, 65);
        icon = gui.createDrawableItemLike(ModItems.FOUNDRY_MOLD_ITEM.get());
    }

    @Override
    public RecipeType<CastingRecipe> getRecipeType() {
        return HbmJeiPlugin.FOUNDRY_CASTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm.foundry_casting");
    }

    @Override
    public int getWidth() {
        return 166;
    }

    @Override
    public int getHeight() {
        return 65;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CastingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(48, 24).addItemStack(recipe.input());
        builder.addInputSlot(75, 6).addItemStack(recipe.mold());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 75, 42).addItemStack(recipe.machine());
        builder.addOutputSlot(102, 24).addItemStack(recipe.output());
    }

    @Override
    public void draw(CastingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }

    @Override
    public ResourceLocation getRegistryName(CastingRecipe recipe) {
        return recipe.id();
    }
}
