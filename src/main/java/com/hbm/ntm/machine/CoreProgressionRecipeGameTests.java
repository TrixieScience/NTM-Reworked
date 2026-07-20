package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.hazard.HazardCarrier;
import com.hbm.ntm.item.BreedingRodItem;
import com.hbm.ntm.nuclear.CustomNukeExplosion;
import com.hbm.ntm.recipe.BreederRecipes;
import com.hbm.ntm.recipe.ShredderRecipes;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
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
                new RodMaterial(BreedingRodItem.Type.U235, "billet_u235"),
                new RodMaterial(BreedingRodItem.Type.NP237, "billet_neptunium"),
                new RodMaterial(BreedingRodItem.Type.U238, "billet_u238"),
                new RodMaterial(BreedingRodItem.Type.PU238, "billet_pu238"),
                new RodMaterial(BreedingRodItem.Type.PU239, "billet_pu239"),
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
        check(helper, ModItems.CELL_TRITIUM.get()
                        .hbm$getHazards(new ItemStack(ModItems.CELL_TRITIUM.get())).radiation() == 0.001F,
                "Tritium Cells must retain the source 0.001 RAD/s hazard");

        CustomNukeExplosion.Yields yields = CustomNukeExplosion.computeYields(List.of(
                new ItemStack(ModItems.CUSTOM_TNT.get(), 2),
                new ItemStack(ModItems.get("ingot_u235").get(), 7),
                new ItemStack(ModItems.CELL_TRITIUM.get())));
        check(helper, yields.hydro() == 30F,
                "One Tritium Cell must contribute the source 30 hydrogen-stage points");
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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }

    private record RodMaterial(BreedingRodItem.Type type, String itemId) { }
}
