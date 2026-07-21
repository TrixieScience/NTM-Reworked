package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.anvil.AnvilRecipes;
import com.hbm.ntm.foundry.FoundryMaterial;
import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.item.DenseWireItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.WeldedPlateItem;
import com.hbm.ntm.recipe.ArcWelderRecipes;
import com.hbm.ntm.recipe.AssemblyRecipes;
import com.hbm.ntm.recipe.ChemicalPlantRecipes;
import com.hbm.ntm.recipe.ShredderRecipes;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
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

    @GameTest(template = "empty")
    public static void deshStorageBlockMatchesSource(GameTestHelper helper) {
        Block block = BuiltInRegistries.BLOCK.get(id("block_desh"));
        check(helper, block != Blocks.AIR, "block_desh must be registered");
        BlockState state = block.defaultBlockState();
        check(helper, block.defaultDestroyTime() == 5.0F, "block_desh hardness must be the source 5.0");
        check(helper, Math.abs(block.getExplosionResistance() - 180.0F) < 0.01F,
                "block_desh must keep the modern 180 resistance (legacy 300)");
        check(helper, state.getSoundType() == SoundType.METAL, "block_desh must keep the metal step sound");
        check(helper, state.is(BlockTags.BEACON_BASE_BLOCKS),
                "block_desh must remain a beacon base, matching the source BlockBeaconable");

        var compress = helper.getLevel().getRecipeManager().byKey(id("desh_block"));
        ItemStack compressed = compress.map(r -> r.value()
                .getResultItem(helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
        check(helper, compressed.is(block.asItem()) && compressed.getCount() == 1,
                "Nine Desh Ingots must craft one Reinforced Block of Desh");

        var decompress = helper.getLevel().getRecipeManager().byKey(id("ingot_desh_from_block_desh"));
        ItemStack loose = decompress.map(r -> r.value()
                .getResultItem(helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
        check(helper, loose.is(ModItems.get("ingot_desh").get()) && loose.getCount() == 9,
                "One Reinforced Block of Desh must uncraft into nine Desh Ingots");

        FoundryMaterial.MaterialAmount melt = FoundryMaterial.fromItem(new ItemStack(block.asItem()));
        check(helper, melt != null && melt.material() == FoundryMaterial.DESH
                        && melt.amount() == FoundryMaterial.BLOCK && FoundryMaterial.BLOCK == 648,
                "block_desh must melt as 648mB of Desh");

        ItemStack cast = FoundryMaterial.DESH.output(FoundryMoldItem.Mold.BLOCK);
        check(helper, cast.is(block.asItem()) && cast.getCount() == 1,
                "The Foundry must cast 648mB of Desh back into a Reinforced Block of Desh");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void deshPowderMatchesSource(GameTestHelper helper) {
        var powder = ModItems.get("powder_desh").get();
        check(helper, powder != null, "powder_desh must be registered");
        check(helper, new ItemStack(powder).is(ItemTags.create(
                        ResourceLocation.fromNamespaceAndPath("c", "dusts/desh"))),
                "powder_desh must carry the c:dusts/desh common tag");

        ItemStack fromIngot = ShredderRecipes.getResult(new ItemStack(ModItems.get("ingot_desh").get()));
        check(helper, fromIngot.is(powder) && fromIngot.getCount() == 1,
                "Shredding a Desh Ingot must yield one Desh Powder");

        ItemStack fromBlock = ShredderRecipes.getResult(new ItemStack(ModItems.getBlockItem("block_desh").get()));
        check(helper, fromBlock.is(powder) && fromBlock.getCount() == 9,
                "Shredding a Reinforced Block of Desh must yield nine Desh Powder");

        ItemStack fromPowder = ShredderRecipes.getResult(new ItemStack(powder));
        check(helper, fromPowder.is(ModItems.get("dust").get()),
                "Desh Powder must itself shred to scrap dust like every dust");

        var smelt = helper.getLevel().getRecipeManager().byKey(id("ingot_desh_from_powder"));
        ItemStack smelted = smelt.map(r -> r.value()
                .getResultItem(helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
        check(helper, smelted.is(ModItems.get("ingot_desh").get()),
                "Desh Powder must smelt back into a Desh Ingot");

        FoundryMaterial.MaterialAmount melt = FoundryMaterial.fromItem(new ItemStack(powder));
        check(helper, melt != null && melt.material() == FoundryMaterial.DESH
                        && melt.amount() == FoundryMaterial.INGOT,
                "Desh Powder must melt in the Foundry as one Desh ingot quantum");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void duraSteelPowderAndBlockMatchSource(GameTestHelper helper) {
        Block block = BuiltInRegistries.BLOCK.get(id("block_dura_steel"));
        check(helper, block != Blocks.AIR, "block_dura_steel must be registered");
        BlockState state = block.defaultBlockState();
        check(helper, block.defaultDestroyTime() == 5.0F, "block_dura_steel hardness must be the source 5.0");
        check(helper, Math.abs(block.getExplosionResistance() - 120.0F) < 0.01F,
                "block_dura_steel must keep the modern 120 resistance (legacy 200)");
        check(helper, state.getSoundType() == SoundType.METAL, "block_dura_steel must keep the metal step sound");
        check(helper, state.is(BlockTags.BEACON_BASE_BLOCKS),
                "block_dura_steel must remain a beacon base, matching the source BlockBeaconable");

        var compress = helper.getLevel().getRecipeManager().byKey(id("dura_steel_block"));
        ItemStack compressed = compress.map(r -> r.value()
                .getResultItem(helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
        check(helper, compressed.is(block.asItem()) && compressed.getCount() == 1,
                "Nine High-Speed Steel Ingots must craft one Reinforced Block of High-Speed Steel");
        var decompress = helper.getLevel().getRecipeManager().byKey(id("ingot_dura_steel_from_block_dura_steel"));
        ItemStack loose = decompress.map(r -> r.value()
                .getResultItem(helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
        check(helper, loose.is(ModItems.get("ingot_dura_steel").get()) && loose.getCount() == 9,
                "One Reinforced Block of High-Speed Steel must uncraft into nine ingots");

        var powder = ModItems.get("powder_dura_steel").get();
        check(helper, powder != null && new ItemStack(powder).is(ItemTags.create(
                        ResourceLocation.fromNamespaceAndPath("c", "dusts/dura_steel"))),
                "powder_dura_steel must be registered with the c:dusts/dura_steel tag");
        ItemStack fromIngot = ShredderRecipes.getResult(new ItemStack(ModItems.get("ingot_dura_steel").get()));
        check(helper, fromIngot.is(powder) && fromIngot.getCount() == 1,
                "Shredding a High-Speed Steel Ingot must yield one powder");
        ItemStack fromBlock = ShredderRecipes.getResult(new ItemStack(block.asItem()));
        check(helper, fromBlock.is(powder) && fromBlock.getCount() == 9,
                "Shredding a Reinforced Block of High-Speed Steel must yield nine powder");
        check(helper, ShredderRecipes.getResult(new ItemStack(powder)).is(ModItems.get("dust").get()),
                "High-Speed Steel Powder must itself shred to scrap dust");

        var smelt = helper.getLevel().getRecipeManager().byKey(id("ingot_dura_steel_from_powder"));
        ItemStack smelted = smelt.map(r -> r.value()
                .getResultItem(helper.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
        check(helper, smelted.is(ModItems.get("ingot_dura_steel").get()),
                "High-Speed Steel Powder must smelt back into an ingot");

        FoundryMaterial.MaterialAmount blockMelt = FoundryMaterial.fromItem(new ItemStack(block.asItem()));
        check(helper, blockMelt != null && blockMelt.material() == FoundryMaterial.DURA_STEEL
                        && blockMelt.amount() == FoundryMaterial.BLOCK,
                "block_dura_steel must melt as 648mB of High-Speed Steel");
        FoundryMaterial.MaterialAmount powderMelt = FoundryMaterial.fromItem(new ItemStack(powder));
        check(helper, powderMelt != null && powderMelt.material() == FoundryMaterial.DURA_STEEL
                        && powderMelt.amount() == FoundryMaterial.INGOT,
                "powder_dura_steel must melt as one High-Speed Steel ingot quantum");
        ItemStack cast = FoundryMaterial.DURA_STEEL.output(FoundryMoldItem.Mold.BLOCK);
        check(helper, cast.is(block.asItem()) && cast.getCount() == 1,
                "The Foundry must cast High-Speed Steel back into its reinforced block");
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
