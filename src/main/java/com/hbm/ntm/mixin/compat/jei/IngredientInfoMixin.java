package com.hbm.ntm.mixin.compat.jei;

import com.hbm.ntm.client.compat.jei.HbmFluidIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "mezz.jei.library.ingredients.IngredientInfo", remap = false)
public abstract class IngredientInfoMixin<T> {
    @Shadow @Final private IIngredientType<T> ingredientType;
    @Shadow @Final @Mutable private IIngredientRenderer<T> ingredientRenderer;

    @Inject(method = "<init>", at = @At("RETURN"))
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void hbm$useFluidIcons(CallbackInfo callback) {
        if (ingredientType == NeoForgeTypes.FLUID_STACK) {
            ingredientRenderer = (IIngredientRenderer) HbmFluidIngredientRenderer.withFallback(
                    (IIngredientRenderer<FluidStack>) ingredientRenderer);
        }
    }
}
