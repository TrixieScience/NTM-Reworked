package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.foundry.FoundryMaterial;
import com.hbm.ntm.recipe.CrucibleRecipes.Recipe;
import com.hbm.ntm.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class CrucibleRecipeCategory implements IRecipeCategory<Recipe> {
    private static final int[] INPUT_X = {6, 24, 6, 24};
    private static final int[] INPUT_Y = {4, 4, 34, 34};
    private static final int[] OUTPUT_X = {96, 96};
    private static final int[] OUTPUT_Y = {4, 34};

    private final IDrawable icon;
    private final IDrawable arrow;

    public CrucibleRecipeCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemLike(ModItems.MACHINE_CRUCIBLE_ITEM.get());
        arrow = gui.getRecipeArrow();
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return HbmJeiPlugin.CRUCIBLE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm.crucible");
    }

    @Override
    public int getWidth() {
        return 172;
    }

    @Override
    public int getHeight() {
        return 78;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Recipe recipe, IFocusGroup focuses) {
        List<FoundryMaterial.MaterialAmount> inputs = recipe.inputs();
        for (int i = 0; i < inputs.size() && i < INPUT_X.length; i++) {
            ItemStack stack = JeiMaterials.crucibleStack(inputs.get(i));
            if (stack.isEmpty()) continue;
            builder.addInputSlot(INPUT_X[i], INPUT_Y[i]).setStandardSlotBackground().addItemStack(stack);
        }
        List<FoundryMaterial.MaterialAmount> outputs = recipe.outputs();
        for (int i = 0; i < outputs.size() && i < OUTPUT_X.length; i++) {
            ItemStack stack = JeiMaterials.crucibleStack(outputs.get(i));
            if (stack.isEmpty()) continue;
            builder.addOutputSlot(OUTPUT_X[i], OUTPUT_Y[i]).setOutputSlotBackground().addItemStack(stack);
        }
    }

    @Override
    public void draw(Recipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrow.draw(graphics, 50, 18);
        var font = Minecraft.getInstance().font;
        List<FoundryMaterial.MaterialAmount> inputs = recipe.inputs();
        for (int i = 0; i < inputs.size() && i < INPUT_X.length; i++) {
            graphics.drawString(font, JeiMaterials.compactAmount(inputs.get(i).amount()),
                    INPUT_X[i], INPUT_Y[i] + 18, 0x606060, false);
        }
        List<FoundryMaterial.MaterialAmount> outputs = recipe.outputs();
        for (int i = 0; i < outputs.size() && i < OUTPUT_X.length; i++) {
            graphics.drawString(font, JeiMaterials.compactAmount(outputs.get(i).amount()),
                    OUTPUT_X[i], OUTPUT_Y[i] + 18, 0x606060, false);
        }
        Component name = Component.translatable(recipe.translationKey());
        graphics.drawString(font, name, 6, 68, 0x404040, false);
    }

    @Override
    public ResourceLocation getRegistryName(Recipe recipe) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID,
                recipe.translationKey().replace('.', '/'));
    }
}
