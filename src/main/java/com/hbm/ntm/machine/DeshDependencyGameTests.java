package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.anvil.AnvilRecipes;
import com.hbm.ntm.foundry.FoundryMaterial;
import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.item.DenseWireItem;
import com.hbm.ntm.item.WeldedPlateItem;
import com.hbm.ntm.recipe.ArcWelderRecipes;
import com.hbm.ntm.recipe.AssemblyRecipes;
import com.hbm.ntm.recipe.ChemicalPlantRecipes;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DeshDependencyGameTests {
    private DeshDependencyGameTests() { }

    @GameTest(template = "empty")
    public static void polymerAndDeshAnvilRecipesMatchSource(GameTestHelper helper) {
        var polymer = ChemicalPlantRecipes.get(ChemicalPlantRecipes.POLYMER);
        check(helper, polymer != null && polymer.duration() == 100 && polymer.power() == 100
                        && polymer.itemInputs().size() == 2
                        && polymer.itemInputs().get(0).count() == 2
                        && polymer.itemInputs().get(1).count() == 1
                        && polymer.fluidInputs().size() == 1
                        && polymer.fluidInputs().getFirst().amount() == 1_000
                        && polymer.fluidInputs().getFirst().fluid().get().isSame(ModFluids.PETROLEUM.get())
                        && polymer.itemOutputs().getFirst().is(ModItems.get("ingot_polymer").get())
                        && polymer.itemOutputs().getFirst().getCount() == 4,
                "Polymer must remain 2 Coal Dust + Fluorite + 1,000mB Petroleum -> 4 bars at 100x100");

        var motor = AnvilRecipes.byId(id("anvil/motor_desh"));
        check(helper, motor != null && motor.tierLower() == 3 && motor.inputs().size() == 4
                        && motor.inputs().get(0).count() == 1
                        && motor.inputs().get(0).matches(new ItemStack(ModItems.MOTOR.get()))
                        && motor.inputs().get(1).count() == 2
                        && motor.inputs().get(1).matches(new ItemStack(ModItems.get("ingot_polymer").get()))
                        && motor.inputs().get(2).count() == 2
                        && motor.inputs().get(2).matches(new ItemStack(ModItems.get("ingot_desh").get()))
                        && motor.inputs().get(3).count() == 1
                        && motor.inputs().get(3).matches(DenseWireItem.create(
                        ModItems.WIRE_DENSE.get(), FoundryMaterial.GOLD, 1))
                        && motor.outputs().getFirst().stack().get().is(ModItems.MOTOR_DESH.get()),
                "Tier-3 Desh Motor must keep its Motor, Polymer, Desh and Dense Gold Wire recipe");

        var plate = AnvilRecipes.byId(id("anvil/plate_desh"));
        check(helper, plate != null && plate.tierLower() == 3 && plate.inputs().size() == 3
                        && plate.inputs().get(0).count() == 4
                        && plate.inputs().get(1).count() == 2
                        && plate.inputs().get(2).count() == 1
                        && plate.outputs().getFirst().stack().get().is(ModItems.get("plate_desh").get())
                        && plate.outputs().getFirst().stack().get().getCount() == 4,
                "Tier-3 Desh Plate must keep the source 4 Desh, 2 Polymer Powder and 1 Dura Steel recipe");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void resistantAlloyWeldsKeepTheirOwnIdentity(GameTestHelper helper) {
        checkWeld(helper, CastPlateItem.CastPlateMaterial.TECHNETIUM_STEEL,
                WeldedPlateItem.WeldedPlateMaterial.TECHNETIUM_STEEL, FoundryMaterial.TECHNETIUM_STEEL);
        checkWeld(helper, CastPlateItem.CastPlateMaterial.CADMIUM_STEEL,
                WeldedPlateItem.WeldedPlateMaterial.CADMIUM_STEEL, FoundryMaterial.CADMIUM_STEEL);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void newlyUngatedAssemblyRecipesLoadExactly(GameTestHelper helper) {
        var plate = AssemblyRecipes.byName("ass.platedesh");
        check(helper, plate != null && plate.duration() == 200 && plate.power() == 100
                        && plate.inputs().size() == 3
                        && plate.inputs().get(0).count() == 4
                        && plate.inputs().get(1).count() == 2
                        && plate.inputs().get(2).count() == 1
                        && plate.output().is(ModItems.get("plate_desh").get())
                        && plate.output().getCount() == 4,
                "Assembly recipe ass.platedesh must load with its exact source costs");

        var condenser = AssemblyRecipes.byName("ass.hpcondenser");
        ItemStack tcWeld = WeldedPlateItem.create(ModItems.PLATE_WELDED.get(),
                WeldedPlateItem.WeldedPlateMaterial.TECHNETIUM_STEEL, 4);
        ItemStack cdWeld = WeldedPlateItem.create(ModItems.PLATE_WELDED.get(),
                WeldedPlateItem.WeldedPlateMaterial.CADMIUM_STEEL, 4);
        ItemStack steelWeld = WeldedPlateItem.steel(ModItems.PLATE_WELDED.get(), 4);
        check(helper, condenser != null && condenser.duration() == 600 && condenser.power() == 100
                        && condenser.inputs().size() == 5
                        && condenser.inputs().get(0).count() == 8
                        && condenser.inputs().get(1).count() == 4
                        && condenser.inputs().get(1).matches(tcWeld)
                        && condenser.inputs().get(1).matches(cdWeld)
                        && !condenser.inputs().get(1).matches(steelWeld)
                        && condenser.inputs().get(3).count() == 3
                        && condenser.inputs().get(3).matches(new ItemStack(ModItems.MOTOR_DESH.get(), 3))
                        && condenser.fluidInput().isPresent()
                        && condenser.fluidInput().orElseThrow().fluid().equals(id("lubricant"))
                        && condenser.fluidInput().orElseThrow().amount() == 4_000
                        && condenser.output().is(ModItems.MACHINE_CONDENSER_POWERED_ITEM.get()),
                "Powered Condenser must accept either resistant weld, three Desh Motors and 4,000mB Lubricant");

        var reactor = AssemblyRecipes.byName("ass.researchreactor");
        check(helper, reactor != null && reactor.duration() == 200 && reactor.power() == 100
                        && reactor.inputs().size() == 7
                        && reactor.inputs().get(1).count() == 4
                        && reactor.inputs().get(1).matches(new ItemStack(ModItems.get("ingot_tcalloy").get(), 4))
                        && reactor.inputs().get(1).matches(new ItemStack(ModItems.get("ingot_cdalloy").get(), 4))
                        && reactor.inputs().get(2).count() == 2
                        && reactor.inputs().get(2).matches(new ItemStack(ModItems.MOTOR_DESH.get(), 2))
                        && reactor.output().is(ModItems.get("machine_reactor_small").get()),
                "Research Reactor must load its seven-input source construction recipe");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void deshDependentCraftingRecipesAreActuallyPresent(GameTestHelper helper) {
        String[] recipes = {
                "motor_desh", "part_stock_polymer", "part_grip_polymer", "blades_desh",
                "gun_uzi", "gun_uzi_akimbo", "gun_spas12", "gun_panzerschreck", "gun_star_f",
                "gun_autoshotgun", "gun_quadro", "gun_lag", "gun_minigun", "gun_missile_launcher",
                "cladding_rubber", "cladding_lead", "cladding_desh"
        };
        for (String name : recipes) {
            var recipe = helper.getLevel().getRecipeManager().byKey(id(name));
            check(helper, recipe.isPresent() && !recipe.orElseThrow().value()
                            .getResultItem(helper.getLevel().registryAccess()).isEmpty(),
                    "Generated source recipe hbm:" + name + " must load");
        }
        helper.succeed();
    }

    private static void checkWeld(GameTestHelper helper, CastPlateItem.CastPlateMaterial cast,
                                  WeldedPlateItem.WeldedPlateMaterial welded,
                                  FoundryMaterial foundryMaterial) {
        var recipe = ArcWelderRecipes.find(CastPlateItem.create(ModItems.PLATE_CAST.get(), cast, 2));
        check(helper, recipe != null && recipe.duration() == 1_200 && recipe.consumption() == 1_000_000L
                        && recipe.fluid() != null && recipe.fluid().is(ModFluids.OXYGEN.get())
                        && recipe.fluid().getAmount() == 1_000
                        && WeldedPlateItem.material(recipe.output()) == welded,
                welded.id() + " must keep its 1,200-tick oxygen weld");
        FoundryMaterial.MaterialAmount remelted = FoundryMaterial.fromItem(recipe.output());
        check(helper, remelted != null && remelted.material() == foundryMaterial
                        && remelted.amount() == FoundryMaterial.WELDED_PLATE,
                welded.id() + " must remelt as six ingots of itself");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
