package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.recipe.ChemicalPlantRecipes;
import com.hbm.ntm.recipe.ChemicalPlantRecipes.ChemicalRecipe;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Arrays;
import java.util.List;

public final class ChemicalPlantRecipeCategory implements IRecipeCategory<ChemicalRecipe> {
    private final IDrawable icon;
    private final IDrawable arrow;

    public ChemicalPlantRecipeCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemLike(ModItems.MACHINE_CHEMICAL_PLANT_ITEM.get());
        arrow = gui.getRecipeArrow();
    }

    @Override
    public RecipeType<ChemicalRecipe> getRecipeType() {
        return HbmJeiPlugin.CHEMICAL_PLANT;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.hbm.chemplant");
    }

    @Override
    public int getWidth() {
        return 176;
    }

    @Override
    public int getHeight() {
        return 92;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ChemicalRecipe recipe, IFocusGroup focuses) {
        List<ChemicalPlantRecipes.ItemInput> items = recipe.itemInputs();
        for (int i = 0; i < items.size(); i++) {
            ChemicalPlantRecipes.ItemInput input = items.get(i);
            List<ItemStack> stacks = Arrays.stream(input.ingredient().getItems())
                    .map(stack -> stack.copyWithCount(input.count()))
                    .toList();
            builder.addInputSlot(3, 3 + i * 18).setStandardSlotBackground().addItemStacks(stacks);
        }

        List<ChemicalPlantRecipes.FluidInput> fluidInputs = recipe.fluidInputs();
        for (int i = 0; i < fluidInputs.size(); i++) {
            addFluid(builder, fluidInputs.get(i).fluid().get(), fluidInputs.get(i).amount(),
                    true, 25, 3 + i * 18);
        }

        List<ItemStack> outputs = recipe.itemOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            builder.addOutputSlot(135, 3 + i * 18).setOutputSlotBackground()
                    .addItemStack(outputs.get(i).copy());
        }

        List<ChemicalPlantRecipes.FluidOutput> fluidOutputs = recipe.fluidOutputs();
        for (int i = 0; i < fluidOutputs.size(); i++) {
            addFluid(builder, fluidOutputs.get(i).fluid().get(), fluidOutputs.get(i).amount(),
                    false, 157, 3 + i * 18);
        }
    }

    private static void addFluid(IRecipeLayoutBuilder builder, Fluid fluid, int amount,
                                 boolean input, int x, int y) {
        if (fluid == null || fluid == Fluids.EMPTY) return;
        var slot = input ? builder.addInputSlot(x, y) : builder.addOutputSlot(x, y);
        slot.setStandardSlotBackground()
                .setFluidRenderer(Math.max(1_000L, amount), false, 16, 16)
                .addFluidStack(fluid, amount);
    }

    @Override
    public void draw(ChemicalRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        arrow.draw(graphics, 88, 21);
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("jei.hbm.power", recipe.power()), 3, 73,
                0x404040, false);
        Component time = Component.translatable("jei.hbm.time", recipe.duration());
        graphics.drawString(font, time, 173 - font.width(time), 73, 0x404040, false);
        graphics.drawString(font, Component.translatable("recipe.hbm." + recipe.name()), 3, 84,
                0x606060, false);
    }

    @Override
    public ResourceLocation getRegistryName(ChemicalRecipe recipe) {
        return recipe.id();
    }
}
