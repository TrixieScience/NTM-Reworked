package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.recipe.CentrifugeRecipes.CentrifugeRecipe;
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

public final class CentrifugeRecipeCategory implements IRecipeCategory<CentrifugeRecipe> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/nei/gui_nei.png");
    private static final int[][] OUTPUTS = {{102, 15}, {120, 15}, {102, 33}, {120, 33}};

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable machineFrame;

    public CentrifugeRecipeCategory(IGuiHelper gui) {
        background = gui.createDrawable(TEXTURE, 5, 11, 166, 65);
        icon = gui.createDrawableItemLike(ModItems.MACHINE_CENTRIFUGE_ITEM.get());
        slot = gui.createDrawable(TEXTURE, 5, 87, 18, 18);
        machineFrame = gui.createDrawable(TEXTURE, 59, 87, 18, 36);
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
    public void setRecipe(IRecipeLayoutBuilder builder, CentrifugeRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(48, 24).setBackground(slot, -1, -1).addIngredients(recipe.input());
        for (int index = 0; index < recipe.outputs().size(); index++) {
            builder.addOutputSlot(OUTPUTS[index][0], OUTPUTS[index][1])
                    .setBackground(slot, -1, -1)
                    .addItemStack(recipe.outputs().get(index).get().copy());
        }
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 75, 31)
                .addItemStack(new ItemStack(ModItems.MACHINE_CENTRIFUGE_ITEM.get()));
    }

    @Override
    public void draw(CentrifugeRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        machineFrame.draw(graphics, 74, 14);
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }
}
