package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.screen.AnvilScreen;
import com.hbm.ntm.client.screen.AssemblyMachineScreen;
import com.hbm.ntm.client.screen.CentrifugeScreen;
import com.hbm.ntm.client.screen.ChemicalPlantScreen;
import com.hbm.ntm.client.screen.MachinePressScreen;
import com.hbm.ntm.recipe.AssemblyClientRecipes;
import com.hbm.ntm.recipe.AssemblyRecipe;
import com.hbm.ntm.recipe.CentrifugeRecipes;
import com.hbm.ntm.recipe.CentrifugeRecipes.CentrifugeRecipe;
import com.hbm.ntm.recipe.ChemicalPlantRecipes;
import com.hbm.ntm.recipe.ChemicalPlantRecipes.ChemicalRecipe;
import com.hbm.ntm.recipe.PressRecipes;
import com.hbm.ntm.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@JeiPlugin
public final class HbmJeiPlugin implements IModPlugin {
    public static final RecipeType<AnvilJeiRecipe> ANVIL =
            RecipeType.create(HbmNtm.MOD_ID, "anvil", AnvilJeiRecipe.class);
    public static final RecipeType<AssemblyRecipe> ASSEMBLY =
            RecipeType.create(HbmNtm.MOD_ID, "assembly", AssemblyRecipe.class);
    public static final RecipeType<PressRecipes.PressRecipe> PRESS =
            RecipeType.create(HbmNtm.MOD_ID, "press", PressRecipes.PressRecipe.class);
    public static final RecipeType<ChemicalRecipe> CHEMICAL_PLANT =
            RecipeType.create(HbmNtm.MOD_ID, "chemical_plant", ChemicalRecipe.class);
    public static final RecipeType<CentrifugeRecipe> CENTRIFUGE =
            RecipeType.create(HbmNtm.MOD_ID, "centrifuge", CentrifugeRecipe.class);

    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "jei");

    private IJeiRuntime runtime;
    private List<AssemblyRecipe> registeredAssemblyRecipes = List.of();

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new AnvilRecipeCategory(gui),
                new AssemblyRecipeCategory(gui),
                new PressRecipeCategory(gui),
                new ChemicalPlantRecipeCategory(gui),
                new CentrifugeRecipeCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(ANVIL, AnvilJeiRecipe.all());
        registration.addRecipes(PRESS, PressRecipes.all());
        registration.addRecipes(CHEMICAL_PLANT, ChemicalPlantRecipes.all());
        registration.addRecipes(CENTRIFUGE, CentrifugeRecipes.all());
        registeredAssemblyRecipes = AssemblyClientRecipes.all();
        registration.addRecipes(ASSEMBLY, registeredAssemblyRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalysts(ANVIL,
                ModItems.ANVIL_IRON_ITEM.get(), ModItems.ANVIL_LEAD_ITEM.get(),
                ModItems.ANVIL_STEEL_ITEM.get(), ModItems.ANVIL_DESH_ITEM.get(),
                ModItems.ANVIL_FERROURANIUM_ITEM.get(), ModItems.ANVIL_SATURNITE_ITEM.get(),
                ModItems.ANVIL_BISMUTH_BRONZE_ITEM.get(), ModItems.ANVIL_ARSENIC_BRONZE_ITEM.get(),
                ModItems.ANVIL_SCHRABIDATE_ITEM.get(), ModItems.ANVIL_DNT_ITEM.get(),
                ModItems.ANVIL_OSMIRIDIUM_ITEM.get(), ModItems.ANVIL_MURKY_ITEM.get());
        registration.addRecipeCatalysts(ASSEMBLY, ModItems.MACHINE_ASSEMBLY_MACHINE_ITEM.get());
        registration.addRecipeCatalysts(PRESS, ModItems.MACHINE_PRESS_ITEM.get());
        registration.addRecipeCatalysts(CHEMICAL_PLANT, ModItems.MACHINE_CHEMICAL_PLANT_ITEM.get());
        registration.addRecipeCatalysts(CENTRIFUGE, ModItems.MACHINE_CENTRIFUGE_ITEM.get());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AnvilScreen.class, 52, 53, 18, 18, ANVIL);
        registration.addRecipeClickArea(AssemblyMachineScreen.class, 7, 125, 18, 18, ASSEMBLY);
        registration.addRecipeClickArea(MachinePressScreen.class, 79, 35, 18, 16, PRESS);
        registration.addRecipeClickArea(ChemicalPlantScreen.class, 62, 126, 70, 16, CHEMICAL_PLANT);
        registration.addRecipeClickArea(CentrifugeScreen.class, 44, 18, 90, 20, CENTRIFUGE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        AssemblyClientRecipes.setUpdateListener(this::replaceAssemblyRecipes);
        replaceAssemblyRecipes(AssemblyClientRecipes.all());
    }

    @Override
    public void onRuntimeUnavailable() {
        AssemblyClientRecipes.setUpdateListener(null);
        runtime = null;
        registeredAssemblyRecipes = List.of();
    }

    private void replaceAssemblyRecipes(List<AssemblyRecipe> recipes) {
        if (runtime == null || registeredAssemblyRecipes.equals(recipes)) return;
        if (!registeredAssemblyRecipes.isEmpty()) {
            runtime.getRecipeManager().hideRecipes(ASSEMBLY, registeredAssemblyRecipes);
        }
        registeredAssemblyRecipes = List.copyOf(recipes);
        if (!registeredAssemblyRecipes.isEmpty()) {
            runtime.getRecipeManager().addRecipes(ASSEMBLY, registeredAssemblyRecipes);
        }
    }
}
