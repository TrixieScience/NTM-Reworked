package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.ShredderRecipes.ShredderRecipe;
import com.hbm.ntm.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
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

import java.util.List;

public final class ShredderRecipeCategory implements IRecipeCategory<ShredderRecipe> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/nei/gui_nei_shredder.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated power;
    private final IDrawableAnimated progress;

    public ShredderRecipeCategory(IGuiHelper gui) {
        background = gui.createDrawable(TEXTURE, 5, 11, 166, 65);
        icon = gui.createDrawableItemLike(ModItems.MACHINE_SHREDDER_ITEM.get());
        power = gui.createAnimatedDrawable(gui.createDrawable(TEXTURE, 36, 86, 16, 52),
                480, IDrawableAnimated.StartDirection.TOP, true);
        progress = gui.createAnimatedDrawable(gui.createDrawable(TEXTURE, 100, 118, 24, 16),
                48, IDrawableAnimated.StartDirection.LEFT, false);
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
    public void setRecipe(IRecipeLayoutBuilder builder, ShredderRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(39, 24).addItemStacks(recipe.display().get());
        builder.addOutputSlot(129, 24).addItemStack(recipe.output().get().copy());
        List<ItemStack> blades = List.of(
                new ItemStack(ModItems.BLADES_STEEL.get()),
                new ItemStack(ModItems.BLADES_TITANIUM.get()),
                new ItemStack(ModItems.BLADES_DESH.get()));
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 84, 6).addItemStacks(blades);
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 84, 42).addItemStacks(blades);
    }

    @Override
    public void draw(ShredderRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        power.draw(graphics, 3, 6);
        progress.draw(graphics, 80, 23);
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }
}
