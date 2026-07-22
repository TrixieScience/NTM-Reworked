package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.compat.jei.FoundryJeiRecipes.SmeltingRecipe;
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
import net.minecraft.world.item.ItemStack;

public final class CrucibleSmeltingRecipeCategory implements IRecipeCategory<SmeltingRecipe> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_crucible_smelting.png");

    private final IDrawable background;
    private final IDrawable icon;

    public CrucibleSmeltingRecipeCategory(IGuiHelper gui) {
        background = gui.createDrawable(TEXTURE, 5, 11, 166, 65);
        icon = gui.createDrawableItemLike(ModItems.MACHINE_CRUCIBLE_ITEM.get());
    }

    @Override
    public RecipeType<SmeltingRecipe> getRecipeType() {
        return HbmJeiPlugin.CRUCIBLE_SMELTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm.crucible_smelting");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SmeltingRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(48, 24).addItemStacks(recipe.inputs());
        builder.addOutputSlot(102, 6).addItemStack(recipe.output());
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 75, 42)
                .addItemStack(new ItemStack(ModItems.MACHINE_CRUCIBLE_ITEM.get()));
    }

    @Override
    public void draw(SmeltingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }

    @Override
    public ResourceLocation getRegistryName(SmeltingRecipe recipe) {
        return recipe.id();
    }
}
