package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RadioactiveBlock;
import com.hbm.ntm.hazard.HazardCarrier;
import com.hbm.ntm.item.BreedingRodItem;
import com.hbm.ntm.item.ZirnoxRodItem;
import com.hbm.ntm.nuclear.CustomNukeExplosion;
import com.hbm.ntm.recipe.BreederRecipes;
import com.hbm.ntm.recipe.ShredderRecipes;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CoreProgressionRecipeGameTests {
    private CoreProgressionRecipeGameTests() { }

    @GameTest(template = "empty")
    public static void denseStoneMatchesTheSourceRecipe(GameTestHelper helper) {
        craft(helper, "reinforced_stone", ModItems.REINFORCED_STONE_ITEM.get(), 4, Map.of(
                'F', new ItemStack(Items.COBBLESTONE),
                'B', new ItemStack(Items.STONE)), "FBF", "BFB", "FBF");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reinforcedGlassFamilyMatchesTheSourceRecipes(GameTestHelper helper) {
        ItemStack glass = craft(helper, "reinforced_glass", ModItems.REINFORCED_GLASS_ITEM.get(), 4, Map.of(
                'F', new ItemStack(Items.IRON_BARS),
                'B', new ItemStack(Items.GLASS)), "FBF", "BFB", "FBF");
        craft(helper, "reinforced_glass_pane", ModItems.REINFORCED_GLASS_PANE_ITEM.get(), 16,
                Map.of('G', glass.copyWithCount(1)), "GGG", "GGG");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void emptyBreedingRodsKeepEverySourceConversion(GameTestHelper helper) {
        ItemStack single = craft(helper, "rod_empty", ModItems.ROD_EMPTY.get(), 16, Map.of(
                'S', new ItemStack(ModItems.get("plate_steel").get()),
                'L', new ItemStack(ModItems.get("plate_lead").get())), "SSS", "L L", "SSS");
        ItemStack dual = craft(helper, "rod_dual_empty_from_rods", ModItems.ROD_DUAL_EMPTY.get(), 1,
                Map.of('R', single.copyWithCount(1)), "RR");
        craft(helper, "rod_empty_from_dual", ModItems.ROD_EMPTY.get(), 2,
                Map.of('D', dual.copy()), "D");

        ItemStack quadFromSingles = craft(helper, "rod_quad_empty_from_rods", ModItems.ROD_QUAD_EMPTY.get(), 1,
                Map.of('R', single.copyWithCount(1)), "RR", "RR");
        craft(helper, "rod_empty_from_quad", ModItems.ROD_EMPTY.get(), 4,
                Map.of('Q', quadFromSingles.copy()), "Q");
        craft(helper, "rod_quad_empty_from_duals", ModItems.ROD_QUAD_EMPTY.get(), 1,
                Map.of('D', dual.copy()), "DD");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wasteDrumUsesTheSourceRodAndLeadShell(GameTestHelper helper) {
        craft(helper, "machine_waste_drum", ModItems.MACHINE_WASTE_DRUM_ITEM.get(), 1, Map.of(
                'L', new ItemStack(ModItems.get("ingot_lead").get()),
                'B', new ItemStack(Items.IRON_BARS),
                'R', new ItemStack(ModItems.ROD_QUAD_EMPTY.get())), "LRL", "BRB", "LRL");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void activeBreedingRodsKeepSourceLoadingAndUnloading(GameTestHelper helper) {
        List<RodMaterial> materials = List.of(
                new RodMaterial(BreedingRodItem.Type.LITHIUM, "lithium"),
                new RodMaterial(BreedingRodItem.Type.CO, "billet_cobalt"),
                new RodMaterial(BreedingRodItem.Type.CO60, "billet_co60"),
                new RodMaterial(BreedingRodItem.Type.TH232, "billet_th232"),
                new RodMaterial(BreedingRodItem.Type.THF, "billet_thorium_fuel"),
                new RodMaterial(BreedingRodItem.Type.U235, "billet_u235"),
                new RodMaterial(BreedingRodItem.Type.NP237, "billet_neptunium"),
                new RodMaterial(BreedingRodItem.Type.U238, "billet_u238"),
                new RodMaterial(BreedingRodItem.Type.PU238, "billet_pu238"),
                new RodMaterial(BreedingRodItem.Type.PU239, "billet_pu239"),
                new RodMaterial(BreedingRodItem.Type.RGP, "billet_pu_mix"),
                new RodMaterial(BreedingRodItem.Type.WASTE, "billet_nuclear_waste"),
                new RodMaterial(BreedingRodItem.Type.URANIUM, "billet_uranium")
        );

        for (RodMaterial material : materials) {
            for (BreedingRodItem.Form form : BreedingRodItem.Form.values()) {
                int amount = switch (form) {
                    case SINGLE -> 1;
                    case DUAL -> 2;
                    case QUAD -> 4;
                };
                Item emptyRod = emptyRod(form);
                Item loadedRod = loadedRod(form);
                Item filling = ModItems.get(material.itemId()).get();

                List<ItemStack> loadingInputs = new ArrayList<>();
                loadingInputs.add(new ItemStack(emptyRod));
                for (int i = 0; i < amount; i++) loadingInputs.add(new ItemStack(filling));
                CraftingInput loading = craftingInput(loadingInputs);
                var loadingRecipe = helper.getLevel().getRecipeManager().getRecipeFor(
                        RecipeType.CRAFTING, loading, helper.getLevel()).orElseThrow();
                check(helper, loadingRecipe.id().equals(id(form.id() + "_" + material.type().id())),
                        "Loading recipe must keep the source rod/form identity");
                ItemStack filled = loadingRecipe.value().assemble(loading, helper.getLevel().registryAccess());
                check(helper, filled.is(loadedRod) && BreedingRodItem.type(filled) == material.type(),
                        "Loading must preserve " + material.type().id() + " in the " + form.id());
                check(helper, loadingRecipe.value().getRemainingItems(loading).stream().allMatch(ItemStack::isEmpty),
                        "Loading an empty rod must not duplicate its shell");

                CraftingInput unloading = craftingInput(List.of(filled.copy()));
                var unloadingRecipe = helper.getLevel().getRecipeManager().getRecipeFor(
                        RecipeType.CRAFTING, unloading, helper.getLevel()).orElseThrow();
                String formSuffix = switch (form) {
                    case SINGLE -> "";
                    case DUAL -> "_dual";
                    case QUAD -> "_quad";
                };
                check(helper, unloadingRecipe.id().equals(id(material.itemId() + "_from_rod" + formSuffix)),
                        "Unloading recipe must keep the source material/form identity");
                ItemStack returned = unloadingRecipe.value().assemble(
                        unloading, helper.getLevel().registryAccess());
                check(helper, returned.is(filling) && returned.getCount() == amount,
                        "Unloading must return exactly " + amount + " source material item(s)");
                List<ItemStack> remainders = unloadingRecipe.value().getRemainingItems(unloading);
                check(helper, remainders.size() == 1 && remainders.getFirst().is(emptyRod),
                        "Unloading must return the matching empty " + form.id());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void leadRodsKeepTheirOddSourceNuggetCounts(GameTestHelper helper) {
        Item leadIngot = ModItems.get("ingot_lead").get();
        Item leadNugget = ModItems.get("nugget_lead").get();
        craft(helper, "ingot_lead_from_nugget_lead", leadIngot, 1,
                Map.of('N', new ItemStack(leadNugget)), "NNN", "NNN", "NNN");
        craft(helper, "nugget_lead_from_ingot_lead", leadNugget, 9,
                Map.of('I', new ItemStack(leadIngot)), "I");

        for (BreedingRodItem.Form form : BreedingRodItem.Form.values()) {
            int ingots = switch (form) {
                case SINGLE -> 0;
                case DUAL -> 1;
                case QUAD -> 2;
            };
            int nuggets = switch (form) {
                case SINGLE, QUAD -> 6;
                case DUAL -> 3;
            };
            int returnedNuggets = switch (form) {
                case SINGLE -> 6;
                case DUAL -> 12;
                case QUAD -> 24;
            };

            List<ItemStack> loadingInputs = new ArrayList<>();
            loadingInputs.add(new ItemStack(emptyRod(form)));
            for (int i = 0; i < ingots; i++) loadingInputs.add(new ItemStack(leadIngot));
            for (int i = 0; i < nuggets; i++) loadingInputs.add(new ItemStack(leadNugget));
            CraftingInput loading = craftingInput(loadingInputs);
            var loadingRecipe = helper.getLevel().getRecipeManager().getRecipeFor(
                    RecipeType.CRAFTING, loading, helper.getLevel()).orElseThrow();
            check(helper, loadingRecipe.id().equals(id(form.id() + "_lead")),
                    "Lead loading must use the source " + form.id() + " recipe");
            ItemStack filled = loadingRecipe.value().assemble(loading, helper.getLevel().registryAccess());
            check(helper, filled.is(loadedRod(form)) && BreedingRodItem.type(filled) == BreedingRodItem.Type.LEAD,
                    "Lead loading must preserve its rod type");

            CraftingInput unloading = craftingInput(List.of(filled.copy()));
            var unloadingRecipe = helper.getLevel().getRecipeManager().getRecipeFor(
                    RecipeType.CRAFTING, unloading, helper.getLevel()).orElseThrow();
            String suffix = switch (form) {
                case SINGLE -> "";
                case DUAL -> "_dual";
                case QUAD -> "_quad";
            };
            check(helper, unloadingRecipe.id().equals(id("nugget_lead_from_rod" + suffix)),
                    "Lead unloading must keep the source " + form.id() + " recipe");
            ItemStack returned = unloadingRecipe.value().assemble(unloading, helper.getLevel().registryAccess());
            check(helper, returned.is(leadNugget) && returned.getCount() == returnedNuggets,
                    "Lead unloading must return exactly " + returnedNuggets + " nuggets");
            List<ItemStack> remainders = unloadingRecipe.value().getRemainingItems(unloading);
            check(helper, remainders.size() == 1 && remainders.getFirst().is(emptyRod(form)),
                    "Lead unloading must return the matching empty " + form.id());
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void thoriumFuelKeepsItsSourceBlendAndHazards(GameTestHelper helper) {
        Item th232Billet = ModItems.get("billet_th232").get();
        Item u233Billet = ModItems.get("billet_u233").get();
        Item thoriumFuelBillet = ModItems.get("billet_thorium_fuel").get();
        craftShapeless(helper, "billet_thorium_fuel_from_billets", thoriumFuelBillet, 6,
                new ItemStack(th232Billet), new ItemStack(th232Billet), new ItemStack(th232Billet),
                new ItemStack(th232Billet), new ItemStack(th232Billet), new ItemStack(u233Billet));

        Item th232Nugget = ModItems.get("nugget_th232").get();
        Item u233Nugget = ModItems.get("nugget_u233").get();
        craftShapeless(helper, "billet_thorium_fuel_from_nuggets", thoriumFuelBillet, 1,
                new ItemStack(th232Nugget), new ItemStack(th232Nugget), new ItemStack(th232Nugget),
                new ItemStack(th232Nugget), new ItemStack(th232Nugget), new ItemStack(u233Nugget));

        assertRadiation(helper, "ingot_thorium_fuel", 1.75F);
        assertRadiation(helper, "billet_thorium_fuel", 0.875F);
        assertRadiation(helper, "nugget_thorium_fuel", 0.175F);
        assertRadiation(helper, "block_thorium_fuel", 17.5F);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reachableZirnoxRodsUseTheirOldLoadingRecipes(GameTestHelper helper) {
        craft(helper, "rod_zirnox_empty", ModItems.ROD_ZIRNOX_EMPTY.get(), 4, Map.of(
                'Z', new ItemStack(ModItems.get("nugget_zirconium").get()),
                'B', new ItemStack(ModItems.get("ingot_beryllium").get())), "Z Z", "ZBZ", "Z Z");

        List<ZirnoxLoad> loads = List.of(
                new ZirnoxLoad("natural_uranium_fuel", "billet_uranium",
                        ModItems.ROD_ZIRNOX_NATURAL_URANIUM_FUEL.get(), ZirnoxRodItem.Type.NATURAL_URANIUM_FUEL),
                new ZirnoxLoad("uranium_fuel", "billet_uranium_fuel",
                        ModItems.ROD_ZIRNOX_URANIUM_FUEL.get(), ZirnoxRodItem.Type.URANIUM_FUEL),
                new ZirnoxLoad("th232", "billet_th232",
                        ModItems.ROD_ZIRNOX_TH232.get(), ZirnoxRodItem.Type.TH232),
                new ZirnoxLoad("thorium_fuel", "billet_thorium_fuel",
                        ModItems.ROD_ZIRNOX_THORIUM_FUEL.get(), ZirnoxRodItem.Type.THORIUM_FUEL),
                new ZirnoxLoad("mox_fuel", "billet_mox_fuel",
                        ModItems.ROD_ZIRNOX_MOX_FUEL.get(), ZirnoxRodItem.Type.MOX_FUEL),
                new ZirnoxLoad("plutonium_fuel", "billet_plutonium_fuel",
                        ModItems.ROD_ZIRNOX_PLUTONIUM_FUEL.get(), ZirnoxRodItem.Type.PLUTONIUM_FUEL),
                new ZirnoxLoad("u233_fuel", "billet_u233",
                        ModItems.ROD_ZIRNOX_U233_FUEL.get(), ZirnoxRodItem.Type.U233_FUEL),
                new ZirnoxLoad("u235_fuel", "billet_u235",
                        ModItems.ROD_ZIRNOX_U235_FUEL.get(), ZirnoxRodItem.Type.U235_FUEL),
                new ZirnoxLoad("lithium", "lithium",
                        ModItems.ROD_ZIRNOX_LITHIUM.get(), ZirnoxRodItem.Type.LITHIUM)
        );
        for (ZirnoxLoad load : loads) {
            ItemStack result = craftShapeless(helper, "rod_zirnox_" + load.recipeSuffix(),
                    load.output(), 1, new ItemStack(ModItems.ROD_ZIRNOX_EMPTY.get()),
                    new ItemStack(ModItems.get(load.fuel()).get()), new ItemStack(ModItems.get(load.fuel()).get()));
            check(helper, result.getItem() instanceof ZirnoxRodItem rod && rod.type() == load.type(),
                    "ZIRNOX " + load.recipeSuffix() + " must keep its source fuel identity");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ordinaryReactorFuelBlendsKeepTheirSourceRatios(GameTestHelper helper) {
        Item u238Billet = ModItems.get("billet_u238").get();
        Item u235Billet = ModItems.get("billet_u235").get();
        Item uraniumFuelBillet = ModItems.get("billet_uranium_fuel").get();
        craftShapeless(helper, "billet_uranium_fuel_from_billets", uraniumFuelBillet, 6,
                new ItemStack(u238Billet), new ItemStack(u238Billet), new ItemStack(u238Billet),
                new ItemStack(u238Billet), new ItemStack(u238Billet), new ItemStack(u235Billet));
        craftShapeless(helper, "billet_uranium_fuel_from_nuggets", uraniumFuelBillet, 1,
                item("nugget_u238"), item("nugget_u238"), item("nugget_u238"),
                item("nugget_u238"), item("nugget_u238"), item("nugget_u235"));

        Item plutoniumFuelBillet = ModItems.get("billet_plutonium_fuel").get();
        craftShapeless(helper, "billet_plutonium_fuel_from_billets", plutoniumFuelBillet, 3,
                new ItemStack(u238Billet), new ItemStack(u238Billet), item("billet_pu_mix"));
        craftShapeless(helper, "billet_plutonium_fuel_from_nuggets", plutoniumFuelBillet, 1,
                item("nugget_pu_mix"), item("nugget_pu_mix"), item("nugget_u238"),
                item("nugget_u238"), item("nugget_u238"), item("nugget_u238"));

        Item moxBillet = ModItems.get("billet_mox_fuel").get();
        craftShapeless(helper, "billet_mox_fuel_from_billets", moxBillet, 3,
                new ItemStack(uraniumFuelBillet), new ItemStack(uraniumFuelBillet), item("billet_pu239"));
        craftShapeless(helper, "billet_mox_fuel_from_nuggets", moxBillet, 1,
                item("nugget_pu239"), item("nugget_pu239"), item("nugget_uranium_fuel"),
                item("nugget_uranium_fuel"), item("nugget_uranium_fuel"), item("nugget_uranium_fuel"));

        assertFuelHazards(helper, "uranium_fuel", 0.5F);
        assertFuelHazards(helper, "plutonium_fuel", 4.25F);
        assertFuelHazards(helper, "mox_fuel", 2.5F);
        assertFuelEconomy(helper, "uranium_fuel");
        assertFuelEconomy(helper, "plutonium_fuel");
        assertFuelEconomy(helper, "mox_fuel");
        check(helper, !((RadioactiveBlock) ModBlocks.get("block_uranium_fuel").get()).radiationFog()
                        && ((RadioactiveBlock) ModBlocks.get("block_plutonium_fuel").get()).radiationFog()
                        && ((RadioactiveBlock) ModBlocks.get("block_mox_fuel").get()).radiationFog(),
                "Only Plutonium and MOX fuel blocks must keep the source RADFOG display effect");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cobaltBreedingClosesTheSourceMaterialLoop(GameTestHelper helper) {
        Item cobaltIngot = ModItems.get("ingot_cobalt").get();
        Item cobaltBillet = ModItems.get("billet_cobalt").get();
        Item cobaltNugget = ModItems.get("nugget_cobalt").get();
        craft(helper, "billet_cobalt_from_ingot_cobalt", cobaltBillet, 3,
                Map.of('I', new ItemStack(cobaltIngot)), "II");
        craft(helper, "ingot_cobalt_from_billet_cobalt", cobaltIngot, 2,
                Map.of('B', new ItemStack(cobaltBillet)), "BBB");
        craft(helper, "billet_cobalt_from_nugget_cobalt", cobaltBillet, 1,
                Map.of('N', new ItemStack(cobaltNugget)), "NNN", "NNN");
        craft(helper, "nugget_cobalt_from_billet_cobalt", cobaltNugget, 6,
                Map.of('B', new ItemStack(cobaltBillet)), "B");
        craft(helper, "ingot_cobalt_from_nugget_cobalt", cobaltIngot, 1,
                Map.of('N', new ItemStack(cobaltNugget)), "NNN", "NNN", "NNN");
        craft(helper, "nugget_cobalt_from_ingot_cobalt", cobaltNugget, 9,
                Map.of('I', new ItemStack(cobaltIngot)), "I");

        BreederRecipes.Recipe bred = BreederRecipes.get(
                BreedingRodItem.stack(ModItems.ROD.get(), BreedingRodItem.Type.CO, 1));
        check(helper, bred != null && bred.flux() == 100
                        && BreedingRodItem.type(bred.output()) == BreedingRodItem.Type.CO60,
                "A Cobalt Rod must breed into Cobalt-60 at the source 100-flux threshold");

        Item co60Ingot = ModItems.get("ingot_co60").get();
        Item co60Billet = ModItems.get("billet_co60").get();
        Item co60Nugget = ModItems.get("nugget_co60").get();
        craft(helper, "billet_co60_from_ingot_co60", co60Billet, 3,
                Map.of('I', new ItemStack(co60Ingot)), "II");
        craft(helper, "ingot_co60_from_billet_co60", co60Ingot, 2,
                Map.of('B', new ItemStack(co60Billet)), "BBB");
        craft(helper, "ingot_co60_from_nugget_co60", co60Ingot, 1,
                Map.of('N', new ItemStack(co60Nugget)), "NNN", "NNN", "NNN");

        ItemStack powder = ShredderRecipes.getResult(new ItemStack(co60Ingot));
        check(helper, powder.is(ModItems.get("powder_co60").get()) && powder.getCount() == 1,
                "The Shredder must retain the source Cobalt-60 dust conversion");
        var smeltingInput = new net.minecraft.world.item.crafting.SingleRecipeInput(powder);
        var smelting = helper.getLevel().getRecipeManager().getRecipeFor(
                RecipeType.SMELTING, smeltingInput, helper.getLevel()).orElseThrow();
        ItemStack remelted = smelting.value().assemble(smeltingInput, helper.getLevel().registryAccess());
        check(helper, smelting.id().equals(id("ingot_co60_from_powder")) && remelted.is(co60Ingot),
                "Cobalt-60 Powder must smelt back into its ingot");
        check(helper, co60Ingot instanceof HazardCarrier ingotHazard
                        && ingotHazard.hbm$getHazards(new ItemStack(co60Ingot)).radiation() == 30F
                        && ingotHazard.hbm$getHazards(new ItemStack(co60Ingot)).heat() == 1F
                        && powder.getItem() instanceof HazardCarrier powderHazard
                        && powderHazard.hbm$getHazards(powder).radiation() == 90F
                        && powderHazard.hbm$getHazards(powder).heat() == 3F,
                "Cobalt-60 forms must keep their source radiation, heat, and dust multipliers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tritiumCellsCloseTheSourceBreederLoop(GameTestHelper helper) {
        craft(helper, "cell_empty", ModItems.CELL_EMPTY.get(), 6, Map.of(
                'S', new ItemStack(ModItems.get("plate_steel").get()),
                'G', new ItemStack(Items.GLASS_PANE)), " S ", "G G", " S ");

        for (BreedingRodItem.Form form : BreedingRodItem.Form.values()) {
            int amount = switch (form) {
                case SINGLE -> 1;
                case DUAL -> 2;
                case QUAD -> 4;
            };
            String suffix = switch (form) {
                case SINGLE -> "";
                case DUAL -> "_dual";
                case QUAD -> "_quad";
            };
            Item emptyRod = emptyRod(form);
            ItemStack bredRod = BreedingRodItem.stack(loadedRod(form), BreedingRodItem.Type.TRITIUM, 1);
            List<ItemStack> inputs = new ArrayList<>();
            inputs.add(bredRod);
            for (int i = 0; i < amount; i++) inputs.add(new ItemStack(ModItems.CELL_EMPTY.get()));
            CraftingInput input = craftingInput(inputs);
            var recipe = helper.getLevel().getRecipeManager().getRecipeFor(
                    RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
            check(helper, recipe.id().equals(id("cell_tritium_from_rod" + suffix)),
                    "Tritium extraction must keep the source rod/form identity");
            ItemStack output = recipe.value().assemble(input, helper.getLevel().registryAccess());
            check(helper, output.is(ModItems.CELL_TRITIUM.get()) && output.getCount() == amount,
                    "A " + form.id() + " must yield exactly " + amount + " Tritium Cell(s)");
            List<ItemStack> remainders = recipe.value().getRemainingItems(input);
            List<ItemStack> shells = remainders.stream().filter(stack -> !stack.isEmpty()).toList();
            check(helper, shells.size() == 1 && shells.getFirst().is(emptyRod),
                    "Extracting Tritium must return the matching empty " + form.id());
        }

        ItemStack emptyCell = new ItemStack(ModItems.CELL_EMPTY.get());
        IFluidHandlerItem handler = emptyCell.getCapability(Capabilities.FluidHandler.ITEM);
        check(helper, handler != null, "Empty Cells must expose a fluid capability");
        check(helper, handler.fill(new FluidStack(ModFluids.TRITIUM.get(), 999),
                        IFluidHandler.FluidAction.EXECUTE) == 0,
                "A legacy cell must only accept a complete 1,000 mB fill");
        check(helper, handler.fill(new FluidStack(ModFluids.TRITIUM.get(), 1_000),
                        IFluidHandler.FluidAction.EXECUTE) == 1_000
                        && handler.getContainer().is(ModItems.CELL_TRITIUM.get()),
                "Filling 1,000 mB of Tritium must swap the shell to a Tritium Cell");
        FluidStack drained = handler.drain(1_000, IFluidHandler.FluidAction.EXECUTE);
        check(helper, drained.is(ModFluids.TRITIUM.get()) && drained.getAmount() == 1_000
                        && handler.getContainer().is(ModItems.CELL_EMPTY.get()),
                "Draining a Tritium Cell must return 1,000 mB and its empty shell");
        check(helper, handler.fill(new FluidStack(ModFluids.SAS3.get(), 1_000),
                        IFluidHandler.FluidAction.EXECUTE) == 1_000
                        && handler.getContainer().is(ModItems.CELL_SAS3.get()),
                "The same Empty Cell must also accept a full bucket of SAS3");
        drained = handler.drain(1_000, IFluidHandler.FluidAction.EXECUTE);
        check(helper, drained.is(ModFluids.SAS3.get()) && drained.getAmount() == 1_000
                        && handler.getContainer().is(ModItems.CELL_EMPTY.get()),
                "Draining an SAS3 Cell must return 1,000 mB and its empty shell");
        check(helper, ModItems.CELL_TRITIUM.get()
                        .hbm$getHazards(new ItemStack(ModItems.CELL_TRITIUM.get())).radiation() == 0.001F,
                "Tritium Cells must retain the source 0.001 RAD/s hazard");
        check(helper, ModItems.CELL_SAS3.get()
                        .hbm$getHazards(new ItemStack(ModItems.CELL_SAS3.get())).radiation() == 5.0F
                        && ModItems.CELL_SAS3.get()
                        .hbm$getHazards(new ItemStack(ModItems.CELL_SAS3.get())).blinding() == 60.0F,
                "SAS3 Cells must retain the source 5 RAD/s and 60-tick blinding hazards");

        CustomNukeExplosion.Yields yields = CustomNukeExplosion.computeYields(List.of(
                new ItemStack(ModItems.CUSTOM_TNT.get(), 2),
                new ItemStack(ModItems.get("ingot_u235").get(), 7),
                new ItemStack(ModItems.CELL_TRITIUM.get())));
        check(helper, yields.hydro() == 30F,
                "One Tritium Cell must contribute the source 30 hydrogen-stage points");
        yields = CustomNukeExplosion.computeYields(List.of(
                new ItemStack(ModItems.CUSTOM_TNT.get(), 2),
                new ItemStack(ModItems.get("ingot_u235").get(), 4),
                new ItemStack(ModItems.CELL_SAS3.get())));
        check(helper, yields.schrab() == 7.5F,
                "One SAS3 Cell must contribute the source 7.5 schrabidium-stage points");
        helper.succeed();
    }

    private static CraftingInput craftingInput(List<ItemStack> ingredients) {
        int width = Math.min(3, ingredients.size());
        int height = (ingredients.size() + width - 1) / width;
        List<ItemStack> slots = new ArrayList<>(ingredients);
        while (slots.size() < width * height) slots.add(ItemStack.EMPTY);
        return CraftingInput.of(width, height, slots);
    }

    private static Item emptyRod(BreedingRodItem.Form form) {
        return switch (form) {
            case SINGLE -> ModItems.ROD_EMPTY.get();
            case DUAL -> ModItems.ROD_DUAL_EMPTY.get();
            case QUAD -> ModItems.ROD_QUAD_EMPTY.get();
        };
    }

    private static Item loadedRod(BreedingRodItem.Form form) {
        return switch (form) {
            case SINGLE -> ModItems.ROD.get();
            case DUAL -> ModItems.ROD_DUAL.get();
            case QUAD -> ModItems.ROD_QUAD.get();
        };
    }

    private static ItemStack craft(GameTestHelper helper, String recipeName, Item expected, int count,
                                   Map<Character, ItemStack> key, String... pattern) {
        int width = pattern[0].length();
        var slots = new ArrayList<ItemStack>(width * pattern.length);
        for (String row : pattern) {
            check(helper, row.length() == width, "Test pattern " + recipeName + " must be rectangular");
            for (int column = 0; column < width; column++) {
                char symbol = row.charAt(column);
                slots.add(symbol == ' ' ? ItemStack.EMPTY : key.get(symbol).copy());
            }
        }
        CraftingInput input = CraftingInput.of(width, pattern.length, slots);
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(
                RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
        check(helper, recipe.id().equals(id(recipeName)), "Crafting grid must resolve to hbm:" + recipeName);
        ItemStack output = recipe.value().assemble(input, helper.getLevel().registryAccess());
        check(helper, output.is(expected) && output.getCount() == count,
                "hbm:" + recipeName + " must produce " + count + " source item(s)");
        return output;
    }

    private static ItemStack craftShapeless(
            GameTestHelper helper,
            String recipeName,
            Item expected,
            int count,
            ItemStack... ingredients
    ) {
        CraftingInput input = craftingInput(List.of(ingredients));
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(
                RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
        check(helper, recipe.id().equals(id(recipeName)), "Crafting grid must resolve to hbm:" + recipeName);
        ItemStack output = recipe.value().assemble(input, helper.getLevel().registryAccess());
        check(helper, output.is(expected) && output.getCount() == count,
                "hbm:" + recipeName + " must produce " + count + " source item(s)");
        return output;
    }

    private static void assertRadiation(GameTestHelper helper, String itemId, float radiation) {
        Item item = BuiltInRegistries.ITEM.get(id(itemId));
        check(helper, item instanceof HazardCarrier carrier
                        && Math.abs(carrier.hbm$getHazards(new ItemStack(item)).radiation() - radiation) < 0.0001F,
                "hbm:" + itemId + " must retain source radiation " + radiation);
    }

    private static void assertFuelHazards(GameTestHelper helper, String fuel, float radiation) {
        assertRadiation(helper, "ingot_" + fuel, radiation);
        assertRadiation(helper, "billet_" + fuel, radiation * 0.5F);
        assertRadiation(helper, "nugget_" + fuel, radiation * 0.1F);
        assertRadiation(helper, "block_" + fuel, radiation * 10F);
    }

    private static void assertFuelEconomy(GameTestHelper helper, String fuel) {
        Item ingot = ModItems.get("ingot_" + fuel).get();
        Item billet = ModItems.get("billet_" + fuel).get();
        Item nugget = ModItems.get("nugget_" + fuel).get();
        Item block = ModItems.getBlockItem("block_" + fuel).get();
        craft(helper, "ingot_" + fuel + "_from_nugget_" + fuel, ingot, 1,
                Map.of('N', new ItemStack(nugget)), "NNN", "NNN", "NNN");
        craft(helper, "nugget_" + fuel + "_from_ingot_" + fuel, nugget, 9,
                Map.of('I', new ItemStack(ingot)), "I");
        craft(helper, "billet_" + fuel + "_from_nugget_" + fuel, billet, 1,
                Map.of('N', new ItemStack(nugget)), "NNN", "NNN");
        craft(helper, "nugget_" + fuel + "_from_billet_" + fuel, nugget, 6,
                Map.of('B', new ItemStack(billet)), "B");
        craft(helper, "ingot_" + fuel + "_from_billet_" + fuel, ingot, 2,
                Map.of('B', new ItemStack(billet)), "BBB");
        craft(helper, "billet_" + fuel + "_from_ingot_" + fuel, billet, 3,
                Map.of('I', new ItemStack(ingot)), "II");
        craft(helper, fuel + "_block", block, 1,
                Map.of('I', new ItemStack(ingot)), "III", "III", "III");
        craft(helper, "ingot_" + fuel + "_from_block_" + fuel, ingot, 9,
                Map.of('B', new ItemStack(block)), "B");
    }

    private static ItemStack item(String id) {
        return new ItemStack(ModItems.get(id).get());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }

    private record RodMaterial(BreedingRodItem.Type type, String itemId) { }
    private record ZirnoxLoad(String recipeSuffix, String fuel, Item output, ZirnoxRodItem.Type type) { }
}
