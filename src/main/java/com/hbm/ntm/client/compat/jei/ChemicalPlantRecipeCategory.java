package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.BatteryPackItem;
import com.hbm.ntm.item.BlueprintItem;
import com.hbm.ntm.recipe.ChemicalPlantRecipes;
import com.hbm.ntm.recipe.ChemicalPlantRecipes.ChemicalRecipe;
import com.hbm.ntm.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
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
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/nei/gui_nei.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final IDrawable machineFrame;
    private final IDrawable templateFrame;

    public ChemicalPlantRecipeCategory(IGuiHelper gui) {
        background = gui.createDrawable(TEXTURE, 5, 11, 166, 65);
        icon = gui.createDrawableItemLike(ModItems.MACHINE_CHEMICAL_PLANT_ITEM.get());
        slot = gui.createDrawable(TEXTURE, 5, 87, 18, 18);
        machineFrame = gui.createDrawable(TEXTURE, 59, 87, 18, 36);
        templateFrame = gui.createDrawable(TEXTURE, 77, 87, 18, 50);
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
    public void setRecipe(IRecipeLayoutBuilder builder, ChemicalRecipe recipe, IFocusGroup focuses) {
        int[][] inputs = inputPositions(recipe.itemInputs().size() + recipe.fluidInputs().size());
        int index = 0;
        for (ChemicalPlantRecipes.ItemInput input : recipe.itemInputs()) {
            List<ItemStack> stacks = Arrays.stream(input.ingredient().getItems())
                    .map(stack -> stack.copyWithCount(input.count()))
                    .toList();
            builder.addInputSlot(inputs[index][0], inputs[index][1])
                    .setBackground(slot, -1, -1)
                    .addItemStacks(stacks);
            index++;
        }
        for (ChemicalPlantRecipes.FluidInput input : recipe.fluidInputs()) {
            addFluid(builder, input.fluid().get(), input.amount(), true,
                    inputs[index][0], inputs[index][1]);
            index++;
        }

        int[][] outputs = outputPositions(recipe.itemOutputs().size() + recipe.fluidOutputs().size());
        index = 0;
        for (ItemStack output : recipe.itemOutputs()) {
            builder.addOutputSlot(outputs[index][0], outputs[index][1])
                    .setBackground(slot, -1, -1)
                    .addItemStack(output.copy());
            index++;
        }
        for (ChemicalPlantRecipes.FluidOutput output : recipe.fluidOutputs()) {
            addFluid(builder, output.fluid().get(), output.amount(), false,
                    outputs[index][0], outputs[index][1]);
            index++;
        }

        if (recipe.pools().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 75, 31)
                    .addItemStack(new ItemStack(ModItems.MACHINE_CHEMICAL_PLANT_ITEM.get()));
        } else {
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 75, 10)
                    .addItemStacks(recipe.pools().stream()
                            .map(pool -> BlueprintItem.forPool(ModItems.BLUEPRINTS.get(), pool))
                            .toList());
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 75, 38)
                    .addItemStack(new ItemStack(ModItems.MACHINE_CHEMICAL_PLANT_ITEM.get()));
        }
    }

    private void addFluid(IRecipeLayoutBuilder builder, Fluid fluid, int amount,
                          boolean input, int x, int y) {
        if (fluid == null || fluid == Fluids.EMPTY) return;
        var fluidSlot = input ? builder.addInputSlot(x, y) : builder.addOutputSlot(x, y);
        fluidSlot.setBackground(slot, -1, -1)
                .setFluidRenderer(amount, false, 16, 16)
                .addFluidStack(fluid, amount);
    }

    @Override
    public void draw(ChemicalRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        background.draw(graphics, 0, 0);
        if (recipe.pools().isEmpty()) machineFrame.draw(graphics, 74, 14);
        else templateFrame.draw(graphics, 74, 7);

        var font = Minecraft.getInstance().font;
        String duration = BatteryPackItem.shortNumber(recipe.duration()) + " ticks";
        String consumption = BatteryPackItem.shortNumber(recipe.power()) + "HE/t";
        graphics.drawString(font, duration, 164 - font.width(duration), 45, 0x404040, false);
        graphics.drawString(font, consumption, 164 - font.width(consumption), 57, 0x404040, false);
    }

    @Override
    public boolean needsRecipeBorder() {
        return false;
    }

    @Override
    public ResourceLocation getRegistryName(ChemicalRecipe recipe) {
        return recipe.id();
    }

    private static int[][] inputPositions(int count) {
        return switch (count) {
            case 1 -> new int[][]{{48, 24}};
            case 2 -> new int[][]{{30, 24}, {48, 24}};
            case 3 -> new int[][]{{12, 24}, {30, 24}, {48, 24}};
            case 4 -> new int[][]{{30, 15}, {48, 15}, {30, 33}, {48, 33}};
            case 5 -> new int[][]{{12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33}};
            case 6 -> new int[][]{{12, 15}, {30, 15}, {48, 15}, {12, 33}, {30, 33}, {48, 33}};
            default -> inputGrid(count);
        };
    }

    private static int[][] inputGrid(int count) {
        int[][] positions = new int[count][2];
        int columns = (count + 2) / 3;
        for (int index = 0; index < count; index++) {
            positions[index][0] = 12 + index % columns * 18 - (columns == 4 ? 18 : 0);
            positions[index][1] = 6 + index / columns * 18;
        }
        return positions;
    }

    private static int[][] outputPositions(int count) {
        return switch (count) {
            case 1 -> new int[][]{{102, 24}};
            case 2 -> new int[][]{{102, 24}, {120, 24}};
            case 3 -> new int[][]{{102, 24}, {120, 24}, {138, 24}};
            case 4 -> new int[][]{{102, 15}, {120, 15}, {102, 33}, {120, 33}};
            case 5 -> new int[][]{{102, 15}, {120, 15}, {102, 33}, {120, 33}, {138, 24}};
            case 6 -> new int[][]{{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42}};
            case 7 -> new int[][]{{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42},
                    {138, 24}};
            case 8 -> new int[][]{{102, 6}, {120, 6}, {102, 24}, {120, 24}, {102, 42}, {120, 42},
                    {138, 24}, {138, 42}};
            default -> new int[count][2];
        };
    }
}
