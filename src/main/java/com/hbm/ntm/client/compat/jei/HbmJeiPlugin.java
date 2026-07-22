package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.screen.AnvilScreen;
import com.hbm.ntm.client.screen.AmmoPressScreen;
import com.hbm.ntm.client.screen.AssemblyMachineScreen;
import com.hbm.ntm.client.screen.BreedingReactorScreen;
import com.hbm.ntm.client.screen.CentrifugeScreen;
import com.hbm.ntm.client.screen.ChemicalPlantScreen;
import com.hbm.ntm.client.screen.CombinationOvenScreen;
import com.hbm.ntm.client.screen.CrucibleScreen;
import com.hbm.ntm.client.screen.MachinePressScreen;
import com.hbm.ntm.client.screen.MachineShredderScreen;
import com.hbm.ntm.client.screen.ZirnoxScreen;
import com.hbm.ntm.client.compat.jei.FoundryJeiRecipes.CastingRecipe;
import com.hbm.ntm.client.compat.jei.FoundryJeiRecipes.SmeltingRecipe;
import com.hbm.ntm.recipe.AssemblyClientRecipes;
import com.hbm.ntm.recipe.AmmoPressRecipes;
import com.hbm.ntm.recipe.AssemblyRecipe;
import com.hbm.ntm.recipe.BreederRecipes;
import com.hbm.ntm.recipe.BreederRecipes.DisplayRecipe;
import com.hbm.ntm.recipe.CentrifugeRecipes;
import com.hbm.ntm.recipe.CentrifugeRecipes.CentrifugeRecipe;
import com.hbm.ntm.recipe.ChemicalPlantRecipes;
import com.hbm.ntm.recipe.ChemicalPlantRecipes.ChemicalRecipe;
import com.hbm.ntm.recipe.CombinationOvenRecipes;
import com.hbm.ntm.recipe.CrackingRecipes;
import com.hbm.ntm.recipe.CrackingRecipes.CrackingRecipe;
import com.hbm.ntm.recipe.CrucibleRecipes;
import com.hbm.ntm.recipe.FractionRecipes;
import com.hbm.ntm.recipe.FractionRecipes.FractionRecipe;
import com.hbm.ntm.recipe.PressRecipes;
import com.hbm.ntm.recipe.ShredderRecipes;
import com.hbm.ntm.recipe.ShredderRecipes.ShredderRecipe;
import com.hbm.ntm.recipe.ZirnoxRecipes;
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
    public static final RecipeType<AmmoPressRecipes.Recipe> AMMO_PRESS =
            RecipeType.create(HbmNtm.MOD_ID, "ammo_press", AmmoPressRecipes.Recipe.class);
    public static final RecipeType<ChemicalRecipe> CHEMICAL_PLANT =
            RecipeType.create(HbmNtm.MOD_ID, "chemical_plant", ChemicalRecipe.class);
    public static final RecipeType<CentrifugeRecipe> CENTRIFUGE =
            RecipeType.create(HbmNtm.MOD_ID, "centrifuge", CentrifugeRecipe.class);
    public static final RecipeType<ShredderRecipe> SHREDDER =
            RecipeType.create(HbmNtm.MOD_ID, "shredder", ShredderRecipe.class);
    public static final RecipeType<CrackingRecipe> CRACKING =
            RecipeType.create(HbmNtm.MOD_ID, "cracking", CrackingRecipe.class);
    public static final RecipeType<FractionRecipe> FRACTION =
            RecipeType.create(HbmNtm.MOD_ID, "fraction", FractionRecipe.class);
    public static final RecipeType<RefineryJeiRecipe> REFINERY =
            RecipeType.create(HbmNtm.MOD_ID, "refinery", RefineryJeiRecipe.class);
    public static final RecipeType<CrucibleRecipes.Recipe> CRUCIBLE =
            RecipeType.create(HbmNtm.MOD_ID, "crucible", CrucibleRecipes.Recipe.class);
    public static final RecipeType<SmeltingRecipe> CRUCIBLE_SMELTING =
            RecipeType.create(HbmNtm.MOD_ID, "crucible_smelting", SmeltingRecipe.class);
    public static final RecipeType<CastingRecipe> FOUNDRY_CASTING =
            RecipeType.create(HbmNtm.MOD_ID, "foundry_casting", CastingRecipe.class);
    public static final RecipeType<DisplayRecipe> BREEDING_REACTOR =
            RecipeType.create(HbmNtm.MOD_ID, "breeding", DisplayRecipe.class);
    public static final RecipeType<ZirnoxRecipes.Recipe> ZIRNOX =
            RecipeType.create(HbmNtm.MOD_ID, "zirnox", ZirnoxRecipes.Recipe.class);
    public static final RecipeType<CombinationOvenRecipes.Recipe> COMBINATION_OVEN =
            RecipeType.create(HbmNtm.MOD_ID, "combination_oven", CombinationOvenRecipes.Recipe.class);

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
                new AmmoPressRecipeCategory(gui),
                new ChemicalPlantRecipeCategory(gui),
                new CentrifugeRecipeCategory(gui),
                new ShredderRecipeCategory(gui),
                new CrackingRecipeCategory(gui),
                new FractionRecipeCategory(gui),
                new RefineryRecipeCategory(gui),
                new CrucibleSmeltingRecipeCategory(gui),
                new FoundryCastingRecipeCategory(gui),
                new CrucibleRecipeCategory(gui),
                new BreedingReactorRecipeCategory(gui),
                new ZirnoxRecipeCategory(gui),
                new CombinationOvenRecipeCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(ANVIL, AnvilJeiRecipe.all());
        registration.addRecipes(PRESS, PressRecipes.all());
        registration.addRecipes(AMMO_PRESS, AmmoPressRecipes.all());
        registration.addRecipes(CHEMICAL_PLANT, ChemicalPlantRecipes.all());
        registration.addRecipes(CENTRIFUGE, CentrifugeRecipes.all());
        registration.addRecipes(SHREDDER, ShredderRecipes.all());
        registration.addRecipes(CRACKING, CrackingRecipes.all());
        registration.addRecipes(FRACTION, FractionRecipes.all());
        registration.addRecipes(REFINERY, RefineryJeiRecipe.all());
        registration.addRecipes(CRUCIBLE_SMELTING, FoundryJeiRecipes.smelting());
        registration.addRecipes(FOUNDRY_CASTING, FoundryJeiRecipes.casting());
        registration.addRecipes(CRUCIBLE, CrucibleRecipes.all());
        registration.addRecipes(BREEDING_REACTOR, BreederRecipes.all());
        registration.addRecipes(ZIRNOX, ZirnoxRecipes.all());
        registration.addRecipes(COMBINATION_OVEN, CombinationOvenRecipes.all());
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
        registration.addRecipeCatalysts(AMMO_PRESS, ModItems.AMMO_PRESS_ITEM.get());
        registration.addRecipeCatalysts(CHEMICAL_PLANT, ModItems.MACHINE_CHEMICAL_PLANT_ITEM.get());
        registration.addRecipeCatalysts(CENTRIFUGE, ModItems.MACHINE_CENTRIFUGE_ITEM.get());
        registration.addRecipeCatalysts(SHREDDER, ModItems.MACHINE_SHREDDER_ITEM.get());
        registration.addRecipeCatalysts(CRACKING, ModItems.MACHINE_CATALYTIC_CRACKER_ITEM.get());
        registration.addRecipeCatalysts(FRACTION, ModItems.MACHINE_FRACTION_TOWER_ITEM.get());
        registration.addRecipeCatalysts(REFINERY, ModItems.MACHINE_REFINERY_ITEM.get());
        registration.addRecipeCatalysts(CRUCIBLE, ModItems.MACHINE_CRUCIBLE_ITEM.get());
        registration.addRecipeCatalysts(CRUCIBLE_SMELTING, ModItems.MACHINE_CRUCIBLE_ITEM.get());
        registration.addRecipeCatalysts(FOUNDRY_CASTING,
                ModItems.FOUNDRY_MOLD_ITEM.get(), ModItems.FOUNDRY_BASIN_ITEM.get());
        registration.addRecipeCatalysts(BREEDING_REACTOR,
                ModItems.MACHINE_REACTOR_BREEDING_ITEM.get());
        registration.addRecipeCatalysts(ZIRNOX, ModItems.REACTOR_ZIRNOX_ITEM.get());
        registration.addRecipeCatalysts(COMBINATION_OVEN, ModItems.FURNACE_COMBINATION_ITEM.get());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(AnvilScreen.class, 52, 53, 18, 18, ANVIL);
        registration.addRecipeClickArea(AssemblyMachineScreen.class, 7, 125, 18, 18, ASSEMBLY);
        registration.addRecipeClickArea(MachinePressScreen.class, 79, 35, 18, 16, PRESS);
        registration.addRecipeClickArea(AmmoPressScreen.class, 107, 9, 68, 82, AMMO_PRESS);
        registration.addRecipeClickArea(ChemicalPlantScreen.class, 62, 126, 70, 16, CHEMICAL_PLANT);
        registration.addRecipeClickArea(CentrifugeScreen.class, 44, 18, 90, 20, CENTRIFUGE);
        registration.addRecipeClickArea(MachineShredderScreen.class, 63, 89, 34, 18, SHREDDER);
        registration.addRecipeClickArea(CrucibleScreen.class, 151, 71, 18, 18,
                CRUCIBLE, CRUCIBLE_SMELTING);
        registration.addRecipeClickArea(BreedingReactorScreen.class, 68, 9, 30, 37,
                BREEDING_REACTOR);
        registration.addRecipeClickArea(ZirnoxScreen.class, 147, 1, 18, 18, ZIRNOX);
        registration.addRecipeClickArea(CombinationOvenScreen.class, 49, 44, 18, 18,
                COMBINATION_OVEN);
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
