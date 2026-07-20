package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.anvil.AnvilRecipes;
import com.hbm.ntm.block.ChemicalPlantBlock;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.blockentity.ChemicalPlantProxyBlockEntity;
import com.hbm.ntm.blockentity.SolderingStationBlockEntity;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.item.ArcElectrodeItem;
import com.hbm.ntm.item.BatteryPackItem;
import com.hbm.ntm.item.BlueprintItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.PipeItem;
import com.hbm.ntm.item.UniversalFluidTankItem;
import com.hbm.ntm.fluid.FluidTankProperties;
import com.hbm.ntm.recipe.AssemblyRecipes;
import com.hbm.ntm.recipe.ChemicalPlantRecipes;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ChemicalPlantGameTests {
    private ChemicalPlantGameTests() { }

    @GameTest(template = "empty")
    public static void chemicalPlantUsesSourceStructureAndConstructionDependencies(GameTestHelper helper) {
        ChemicalPlantBlockEntity plant = placePlant(helper, new BlockPos(4, 1, 4));
        int proxies = 0;
        for (BlockPos part : ChemicalPlantBlock.partPositions(plant.getBlockPos())) {
            check(helper, helper.getLevel().getBlockState(part).is(ModBlocks.MACHINE_CHEMICAL_PLANT.get()),
                    "Every Chemical Plant footprint cell must use the shared block identity");
            if (helper.getLevel().getBlockEntity(part) instanceof ChemicalPlantProxyBlockEntity) proxies++;
        }
        check(helper, proxies == 8, "Chemical Plant must expose eight bottom-layer inventory/HE/fluid proxies");
        var pipe = AnvilRecipes.byId(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                HbmNtm.MOD_ID, "anvil/pipe_copper"));
        check(helper, pipe != null && pipe.tierLower() == 1 && pipe.inputs().getFirst().count() == 3
                        && PipeItem.isCopper(pipe.outputs().getFirst().stack().get()),
                "Tier-1 Anvil must form one Copper Pipe from three Copper Plates");
        var construction = AssemblyRecipes.byName("ass.chemplant");
        check(helper, construction != null && construction.duration() == 200 && construction.power() == 100
                        && construction.inputs().size() == 6
                        && construction.inputs().get(1).ingredient().test(PipeItem.copper(ModItems.PIPE.get(), 1))
                        && !construction.inputs().get(1).ingredient().test(PipeItem.steel(ModItems.PIPE.get(), 1))
                        && CircuitItem.type(construction.inputs().get(5).display()) == CircuitItem.CircuitType.ANALOG,
                "Chemical Plant construction must preserve exact Copper Pipe subtype, six source inputs and 200x100 cost");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void waterBecomesPeroxideWithExactContainerAndEnergyRules(GameTestHelper helper) {
        ChemicalPlantBlockEntity plant = barePlant(helper, new BlockPos(3, 1, 3));
        check(helper, plant.selectRecipe(ChemicalPlantRecipes.PEROXIDE), "Peroxide recipe must be selectable");
        plant.setPower(5_000L);
        plant.setItem(10, new ItemStack(Items.WATER_BUCKET));
        plant.setItem(16, new ItemStack(ModItems.FLUID_TANK_EMPTY.get()));
        for (int i = 0; i < 50; i++) tick(helper, plant);
        check(helper, plant.getPower() == 0L && plant.outputTank(0).getFluidAmount() == 1_000
                        && plant.outputTank(0).getFluid().is(ModFluids.PEROXIDE.get()),
                "One water bucket must become 1,000 mB Peroxide in 50 ticks for exactly 5,000 HE");
        check(helper, plant.getItem(10).isEmpty() && plant.getItem(13).is(Items.BUCKET),
                "Input container handling must return the source empty bucket");
        tick(helper, plant);
        check(helper, plant.outputTank(0).isEmpty()
                        && UniversalFluidTankItem.fluid(plant.getItem(19))
                        == UniversalFluidTankItem.ContainedFluid.PEROXIDE,
                "An Empty Universal Fluid Tank must extract exactly 1,000 mB Peroxide");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void peroxideWaterAndSulfurBecomeSulfuricAcid(GameTestHelper helper) {
        ChemicalPlantBlockEntity plant = barePlant(helper, new BlockPos(3, 1, 3));
        check(helper, plant.selectRecipe(ChemicalPlantRecipes.SULFURIC_ACID),
                "Sulfuric Acid recipe must be selectable");
        plant.setPower(5_000L);
        plant.setItem(10, UniversalFluidTankItem.create(ModItems.FLUID_TANK_FULL.get(),
                UniversalFluidTankItem.ContainedFluid.PEROXIDE, 1));
        plant.setItem(11, new ItemStack(Items.WATER_BUCKET));
        plant.setItem(4, new ItemStack(ModItems.get("sulfur").get()));
        for (int i = 0; i < 50; i++) tick(helper, plant);
        check(helper, plant.getPower() == 0L && plant.outputTank(0).getFluidAmount() == 2_000
                        && plant.outputTank(0).getFluid().is(ModFluids.SULFURIC_ACID.get()),
                "Peroxide, water and Sulfur must make 2,000 mB Sulfuric Acid in 50 ticks for 5,000 HE");
        check(helper, plant.getItem(4).isEmpty() && plant.getItem(13).is(ModItems.FLUID_TANK_EMPTY.get())
                        && plant.getItem(14).is(Items.BUCKET),
                "Sulfur and both fluid containers must be consumed with exact remainders");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sulfuricOutputPushesOnlyIntoIdentifiedSolderingTank(GameTestHelper helper) {
        ChemicalPlantBlockEntity plant = barePlant(helper, new BlockPos(3, 1, 3));
        SolderingStationBlockEntity station = bareSoldering(helper, new BlockPos(5, 1, 3));
        plant.outputTank(0).fill(new FluidStack(ModFluids.SULFURIC_ACID.get(), 2_000),
                IFluidHandler.FluidAction.EXECUTE);
        tick(helper, plant);
        check(helper, plant.outputTank(0).getFluidAmount() == 2_000 && station.tank().isEmpty(),
                "Unidentified Soldering tanks must reject adjacent Chemical Plant output");
        ItemStack identifier = new ItemStack(ModItems.FLUID_IDENTIFIER_MULTI.get());
        FluidIdentifierItem.set(identifier, FluidIdentifierItem.Selection.SULFURIC_ACID, true);
        station.setItem(8, identifier);
        tick(helper, plant);
        check(helper, plant.outputTank(0).isEmpty() && station.tank().getFluidAmount() == 2_000
                        && station.tank().getFluid().is(ModFluids.SULFURIC_ACID.get()),
                "Chemical Plant output must transfer through the exact identified Soldering capability");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void recipeInputsRejectWrongLanesAndAutomationCanExtractClogs(GameTestHelper helper) {
        ChemicalPlantBlockEntity plant = barePlant(helper, new BlockPos(3, 1, 3));
        ItemStack sulfur = new ItemStack(ModItems.get("sulfur").get());
        check(helper, !plant.canPlaceItem(4, sulfur),
                "Chemical Plant solid inputs must reject items until a recipe is selected");
        plant.selectRecipe(ChemicalPlantRecipes.PEROXIDE);
        check(helper, !plant.canPlaceItem(4, sulfur),
                "Peroxide has no solid input and must reject Sulfur");
        plant.selectRecipe(ChemicalPlantRecipes.SULFURIC_ACID);
        check(helper, plant.canPlaceItem(4, sulfur) && !plant.canPlaceItem(5, sulfur)
                        && !plant.canPlaceItem(4, new ItemStack(Items.DIRT)),
                "Sulfuric Acid must accept Sulfur only in its exact first input lane");
        ItemStack clog = new ItemStack(Items.DIRT);
        plant.setItem(4, clog);
        check(helper, plant.canTakeItemThroughFace(4, clog, Direction.NORTH),
                "Automation must be able to extract a source-style clogged input");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void expandedDependencyCompleteRecipeTableMatchesSource(GameTestHelper helper) {
        var heavyLube = ChemicalPlantRecipes.get(ChemicalPlantRecipes.HEAVY_LUBE);
        var coalLube = ChemicalPlantRecipes.get(ChemicalPlantRecipes.COAL_LUBE);
        var cobble = ChemicalPlantRecipes.get(ChemicalPlantRecipes.COBBLE);
        var aggregate = ChemicalPlantRecipes.get(ChemicalPlantRecipes.AGGREGATE);
        var battery = ChemicalPlantRecipes.get(ChemicalPlantRecipes.BATTERY_LEAD);
        var oilElectrodes = ChemicalPlantRecipes.get(ChemicalPlantRecipes.OIL_ELECTRODES);
        var lubeElectrodes = ChemicalPlantRecipes.get(ChemicalPlantRecipes.LUBE_ELECTRODES);
        var cordite = ChemicalPlantRecipes.get(ChemicalPlantRecipes.CORDITE);
        var dynamite = ChemicalPlantRecipes.get(ChemicalPlantRecipes.DYNAMITE);
        var sas3 = ChemicalPlantRecipes.get(ChemicalPlantRecipes.SAS3);
        check(helper, ChemicalPlantRecipes.all().size() == 22,
                "The Chemical Plant table must contain exactly twenty-two registered source recipes");
        check(helper, coalLube != null && coalLube.duration() == 40 && coalLube.power() == 100
                        && coalLube.pools().equals(java.util.List.of("alt..lube"))
                        && coalLube.fluidInputs().getFirst().amount() == 1_000
                        && coalLube.fluidInputs().getFirst().fluid().get().isSame(ModFluids.COALCREOSOTE.get())
                        && coalLube.fluidOutputs().getFirst().amount() == 1_000
                        && coalLube.fluidOutputs().getFirst().fluid().get().isSame(ModFluids.LUBRICANT.get()),
                "Coal Creosote lubricant must preserve 1,000 -> 1,000 mB, 40x100 and its source pool");
        check(helper, heavyLube != null && heavyLube.duration() == 40 && heavyLube.power() == 100
                        && heavyLube.pools().equals(java.util.List.of("alt..lube"))
                        && heavyLube.fluidInputs().getFirst().amount() == 2_000
                        && heavyLube.fluidInputs().getFirst().fluid().get().isSame(ModFluids.HEAVYOIL.get())
                        && heavyLube.fluidOutputs().getFirst().amount() == 1_000
                        && heavyLube.fluidOutputs().getFirst().fluid().get().isSame(ModFluids.LUBRICANT.get()),
                "Heavy Oil lubricant must preserve 2,000 -> 1,000 mB, 40x100 and its source pool");
        check(helper, cobble != null && cobble.duration() == 20 && cobble.power() == 100
                        && cobble.fluidInputs().get(0).amount() == 1_000
                        && cobble.fluidInputs().get(1).amount() == 25
                        && cobble.itemOutputs().getFirst().is(Items.COBBLESTONE),
                "Cobblestone must preserve 1,000 mB Water plus 25 mB Lava and 20x100");
        check(helper, aggregate != null && aggregate.duration() == 320 && aggregate.power() == 500
                        && aggregate.pools().equals(java.util.List.of("discover..stone"))
                        && aggregate.itemInputs().getFirst().count() == 16
                        && aggregate.itemOutputs().get(0).is(Items.GRAVEL)
                        && aggregate.itemOutputs().get(0).getCount() == 8
                        && aggregate.itemOutputs().get(1).is(Items.SAND)
                        && aggregate.itemOutputs().get(1).getCount() == 8,
                "Instant Aggregate must preserve 16 Cobblestone -> 8 Gravel + 8 Sand and 320x500");
        check(helper, battery != null && battery.duration() == 100 && battery.power() == 100
                        && battery.itemInputs().get(0).count() == 4 && battery.itemInputs().get(1).count() == 4
                        && battery.fluidInputs().getFirst().amount() == 8_000
                        && BatteryPackItem.type(battery.itemOutputs().getFirst())
                        == BatteryPackItem.BatteryType.BATTERY_LEAD,
                "Lead battery must preserve four Steel Plates, four Lead Ingots and 8,000 mB acid");
        check(helper, oilElectrodes != null && lubeElectrodes != null
                        && oilElectrodes.duration() == 600 && lubeElectrodes.duration() == 600
                        && oilElectrodes.fluidInputs().getFirst().amount() == 4_000
                        && lubeElectrodes.fluidInputs().getFirst().amount() == 8_000
                        && oilElectrodes.pools().equals(java.util.List.of("alt..electrodes"))
                        && lubeElectrodes.pools().equals(java.util.List.of("alt..electrodes"))
                        && ArcElectrodeItem.type(oilElectrodes.itemOutputs().getFirst())
                        == ArcElectrodeItem.ElectrodeType.GRAPHITE,
                "Both instant Graphite Electrode alternatives must preserve source fluids, costs and pool");
        check(helper, cordite != null && cordite.duration() == 40 && cordite.power() == 100
                        && cordite.itemInputs().get(0).count() == 2
                        && cordite.itemInputs().get(1).count() == 2
                        && cordite.fluidInputs().getFirst().amount() == 200
                        && cordite.fluidInputs().getFirst().fluid().get().isSame(ModFluids.GAS.get())
                        && cordite.itemOutputs().getFirst().is(ModItems.get("cordite").get())
                        && cordite.itemOutputs().getFirst().getCount() == 4,
                "Cordite must preserve 2 Niter, 2 Sawdust, 200 mB Gas and four-output count");
        check(helper, dynamite != null && dynamite.duration() == 50 && dynamite.power() == 100
                        && dynamite.itemInputs().size() == 3
                        && dynamite.itemOutputs().getFirst().is(ModItems.BALL_DYNAMITE.get())
                        && dynamite.itemOutputs().getFirst().getCount() == 2,
                "Dynamite must preserve Sugar, Niter, Sand, two-output count and 50x100");
        check(helper, sas3 != null && sas3.duration() == 200 && sas3.power() == 5_000
                        && sas3.itemInputs().size() == 2
                        && sas3.itemInputs().get(0).count() == 1
                        && sas3.itemInputs().get(0).matches(
                                new ItemStack(ModItems.get("powder_schrabidium").get()))
                        && sas3.itemInputs().get(1).count() == 2
                        && sas3.itemInputs().get(1).matches(new ItemStack(ModItems.get("sulfur").get(), 2))
                        && sas3.fluidInputs().getFirst().amount() == 2_000
                        && sas3.fluidInputs().getFirst().fluid().get().isSame(ModFluids.PEROXIDE.get())
                        && sas3.fluidOutputs().getFirst().amount() == 1_000
                        && sas3.fluidOutputs().getFirst().fluid().get().isSame(ModFluids.SAS3.get()),
                "SAS3 must preserve one Schrabidium dust, two Sulfur, 2,000 mB Peroxide and 200x5,000");
        FluidTankProperties.Profile sas3Profile = FluidTankProperties.get(FluidIdentifierItem.Selection.SAS3);
        check(helper, sas3Profile.health() == 5 && sas3Profile.flammability() == 0
                        && sas3Profile.reactivity() == 4
                        && sas3Profile.symbol() == FluidTankProperties.Symbol.RADIATION
                        && sas3Profile.phase() == FluidTankProperties.Phase.LIQUID,
                "SAS3 tanks must show the source 5/0/4 radioactive-liquid warning diamond");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sas3RunsAndFillsItsLegacyCell(GameTestHelper helper) {
        ChemicalPlantBlockEntity plant = barePlant(helper, new BlockPos(3, 1, 3));
        check(helper, plant.selectRecipe(ChemicalPlantRecipes.SAS3), "SAS3 recipe must be selectable");
        tick(helper, plant);
        plant.inputTank(0).fill(new FluidStack(ModFluids.PEROXIDE.get(), 2_000),
                IFluidHandler.FluidAction.EXECUTE);
        plant.setItem(4, new ItemStack(ModItems.get("powder_schrabidium").get()));
        plant.setItem(5, new ItemStack(ModItems.get("sulfur").get(), 2));
        for (int i = 0; i < 200; i++) {
            plant.setPower(plant.getMaxPower());
            tick(helper, plant);
        }
        check(helper, plant.outputTank(0).getFluidAmount() == 1_000
                        && plant.outputTank(0).getFluid().is(ModFluids.SAS3.get())
                        && plant.inputTank(0).isEmpty() && plant.getItem(4).isEmpty()
                        && plant.getItem(5).isEmpty(),
                "The full source batch must consume its solids and Peroxide into 1,000 mB SAS3");

        plant.setItem(ChemicalPlantBlockEntity.FLUID_OUTPUT_CONTAINER_START,
                new ItemStack(ModItems.CELL_EMPTY.get()));
        tick(helper, plant);
        check(helper, plant.outputTank(0).isEmpty()
                        && plant.getItem(ChemicalPlantBlockEntity.FLUID_OUTPUT_RESULT_START)
                        .is(ModItems.CELL_SAS3.get()),
                "An Empty Cell must bottle the finished SAS3 exactly like the legacy container registry");

        ItemStack cell = new ItemStack(ModItems.CELL_SAS3.get());
        IFluidHandlerItem handler = cell.getCapability(Capabilities.FluidHandler.ITEM);
        check(helper, handler != null && handler.getFluidInTank(0).is(ModFluids.SAS3.get())
                        && handler.getFluidInTank(0).getAmount() == 1_000,
                "The bottled SAS3 Cell must expose its full 1,000 mB fluid capability");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pooledChemicalRecipesRequireTheirExactBlueprint(GameTestHelper helper) {
        ChemicalPlantBlockEntity plant = barePlant(helper, new BlockPos(3, 1, 3));
        check(helper, !plant.selectRecipe(ChemicalPlantRecipes.HEAVY_LUBE),
                "Pooled lubricant recipe must reject selection without a blueprint");
        plant.setItem(ChemicalPlantBlockEntity.BLUEPRINT,
                BlueprintItem.forPool(ModItems.BLUEPRINTS.get(), "alt.plates"));
        check(helper, !plant.selectRecipe(ChemicalPlantRecipes.HEAVY_LUBE),
                "Pooled lubricant recipe must reject the wrong blueprint pool");
        plant.setItem(ChemicalPlantBlockEntity.BLUEPRINT,
                BlueprintItem.forPool(ModItems.BLUEPRINTS.get(), "alt..lube"));
        check(helper, plant.selectRecipe(ChemicalPlantRecipes.HEAVY_LUBE),
                "Pooled lubricant recipe must accept its exact source pool");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void malformedFullTankDefaultsToNoFluid(GameTestHelper helper) {
        ItemStack malformed = new ItemStack(ModItems.FLUID_TANK_FULL.get());
        check(helper, UniversalFluidTankItem.fluid(malformed) == UniversalFluidTankItem.ContainedFluid.NONE,
                "A full tank without valid subtype data must preserve source fluid NONE, not become water");
        ChemicalPlantBlockEntity plant = barePlant(helper, new BlockPos(3, 1, 3));
        plant.selectRecipe(ChemicalPlantRecipes.PEROXIDE);
        plant.setItem(10, malformed);
        tick(helper, plant);
        check(helper, plant.inputTank(0).isEmpty() && plant.getItem(10).is(ModItems.FLUID_TANK_FULL.get()),
                "A malformed full tank must not inject water or be consumed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chemicalRecipeTanksAndSelectionPersistAndSynchronize(GameTestHelper helper) {
        ChemicalPlantBlockEntity plant = barePlant(helper, new BlockPos(2, 1, 2));
        plant.selectRecipe(ChemicalPlantRecipes.SULFURIC_ACID);
        plant.setPower(4_000L);
        plant.inputTank(0).fill(new FluidStack(ModFluids.PEROXIDE.get(), 1_000), IFluidHandler.FluidAction.EXECUTE);
        plant.inputTank(1).fill(new FluidStack(Fluids.WATER, 1_000), IFluidHandler.FluidAction.EXECUTE);
        plant.setItem(4, new ItemStack(ModItems.get("sulfur").get()));
        plant.outputTank(0).fill(new FluidStack(ModFluids.SULFURIC_ACID.get(), 1_250), IFluidHandler.FluidAction.EXECUTE);
        tick(helper, plant);
        check(helper, plant.progress() > 0D && plant.progress() < 1D,
                "A powered Chemical Plant must retain partial source-style progress");
        double savedProgress = plant.progress();
        var saved = plant.saveWithoutMetadata(helper.getLevel().registryAccess());
        ChemicalPlantBlockEntity loaded = barePlant(helper, new BlockPos(5, 1, 2));
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        check(helper, ChemicalPlantRecipes.SULFURIC_ACID.equals(loaded.recipeId())
                        && loaded.getPower() == 3_900L
                        && loaded.inputTank(0).getFluidAmount() == 1_000
                        && loaded.outputTank(0).getFluidAmount() == 1_250
                        && Math.abs(loaded.progress() - savedProgress) < 0.000001D,
                "Chemical recipe, partial progress, HE and all tank contents must persist");
        ChemicalPlantBlockEntity clientCopy = barePlant(helper, new BlockPos(7, 1, 2));
        clientCopy.handleUpdateTag(loaded.getUpdateTag(helper.getLevel().registryAccess()),
                helper.getLevel().registryAccess());
        check(helper, ChemicalPlantRecipes.SULFURIC_ACID.equals(clientCopy.recipeId())
                        && clientCopy.inputTank(0).getFluidAmount() == 1_000
                        && clientCopy.outputTank(0).getFluidAmount() == 1_250
                        && Math.abs(clientCopy.progress() - savedProgress) < 0.000001D,
                "Chemical recipe, partial progress and six-tank state must synchronize to clients");
        helper.succeed();
    }

    private static ChemicalPlantBlockEntity placePlant(GameTestHelper helper, BlockPos relativeCore) {
        ChemicalPlantBlock block = ModBlocks.MACHINE_CHEMICAL_PLANT.get();
        var state = block.defaultBlockState().setValue(ChemicalPlantBlock.FACING, Direction.NORTH);
        helper.setBlock(relativeCore, state);
        BlockPos absolute = helper.absolutePos(relativeCore);
        block.setPlacedBy(helper.getLevel(), absolute, state, helper.makeMockPlayer(GameType.SURVIVAL),
                new ItemStack(ModItems.MACHINE_CHEMICAL_PLANT_ITEM.get()));
        return (ChemicalPlantBlockEntity) helper.getLevel().getBlockEntity(absolute);
    }
    private static ChemicalPlantBlockEntity barePlant(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, ModBlocks.MACHINE_CHEMICAL_PLANT.get().defaultBlockState());
        return helper.getBlockEntity(position);
    }
    private static SolderingStationBlockEntity bareSoldering(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, ModBlocks.MACHINE_SOLDERING_STATION.get().defaultBlockState());
        return helper.getBlockEntity(position);
    }
    private static void tick(GameTestHelper helper, ChemicalPlantBlockEntity plant) {
        ChemicalPlantBlockEntity.tick(helper.getLevel(), plant.getBlockPos(), plant.getBlockState(), plant);
    }
    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
