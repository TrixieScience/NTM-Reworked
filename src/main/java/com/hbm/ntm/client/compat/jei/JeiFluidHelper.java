package com.hbm.ntm.client.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** Shared JEI fluid-slot placement for the oil/chemistry categories. */
final class JeiFluidHelper {
    private JeiFluidHelper() {
    }

    static void fluidSlot(IRecipeLayoutBuilder builder, Fluid fluid, long amount,
                          boolean input, int x, int y) {
        if (fluid == null || fluid == Fluids.EMPTY) return;
        var slot = input ? builder.addInputSlot(x, y) : builder.addOutputSlot(x, y);
        slot.setStandardSlotBackground()
                .setCustomRenderer(NeoForgeTypes.FLUID_STACK, HbmFluidIngredientRenderer.INSTANCE)
                .addFluidStack(fluid, amount);
    }
}
