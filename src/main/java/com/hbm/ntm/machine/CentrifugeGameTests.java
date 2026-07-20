package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.CentrifugeBlock;
import com.hbm.ntm.blockentity.CentrifugeBlockEntity;
import com.hbm.ntm.blockentity.CentrifugeProxyBlockEntity;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.recipe.AssemblyRecipe;
import com.hbm.ntm.recipe.AssemblyRecipes;
import com.hbm.ntm.recipe.CentrifugeRecipes;
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
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CentrifugeGameTests {
    private CentrifugeGameTests() { }

    @GameTest(template = "empty")
    public static void sourceTowerProxiesAutomationAndConstructionRecipes(GameTestHelper helper) {
        CentrifugeBlockEntity centrifuge = placeCentrifuge(helper, new BlockPos(3, 1, 3));
        int cores = 0;
        int proxies = 0;
        for (int partY = 0; partY < 4; partY++) {
            BlockPos part = centrifuge.getBlockPos().above(partY);
            var state = helper.getLevel().getBlockState(part);
            check(helper, state.is(ModBlocks.MACHINE_CENTRIFUGE.get())
                            && state.getValue(CentrifugeBlock.PART_Y) == partY,
                    "The Centrifuge must retain the source four-cell vertical tower");
            if (helper.getLevel().getBlockEntity(part) instanceof CentrifugeBlockEntity) cores++;
            if (helper.getLevel().getBlockEntity(part) instanceof CentrifugeProxyBlockEntity proxy) {
                proxies++;
                check(helper, proxy.target() == centrifuge
                                && Arrays.equals(proxy.getSlotsForFace(Direction.NORTH), new int[]{0, 2, 3, 4, 5}),
                        "Every upper tower cell must delegate the exact source automation slots");
                check(helper, helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, part,
                                Direction.NORTH) != null,
                        "Every upper source ProxyCombo cell must expose inventory automation");
            }
        }
        check(helper, cores == 1 && proxies == 3,
                "The source Centrifuge must contain one core and three HE/inventory proxies");

        AssemblyRecipe tower = AssemblyRecipes.byName("ass.centrifugetower");
        check(helper, tower != null && tower.inputs().size() == 3 && tower.duration() == 100
                        && tower.power() == 100 && tower.output().is(ModItems.CENTRIFUGE_ELEMENT.get())
                        && tower.inputs().get(0).matches(new ItemStack(ModItems.get("plate_dura_steel").get(), 4))
                        && tower.inputs().get(1).matches(new ItemStack(ModItems.get("plate_titanium").get(), 4))
                        && tower.inputs().get(2).matches(new ItemStack(ModItems.MOTOR.get())),
                "ass.centrifugetower must preserve four HSS Plates, four Titanium Plates and one Motor");

        AssemblyRecipe machine = AssemblyRecipes.byName("ass.centrifuge");
        check(helper, machine != null && machine.inputs().size() == 5 && machine.duration() == 200
                        && machine.power() == 100 && machine.output().is(ModItems.MACHINE_CENTRIFUGE_ITEM.get())
                        && machine.inputs().get(0).matches(new ItemStack(ModItems.CENTRIFUGE_ELEMENT.get()))
                        && machine.inputs().get(1).matches(new ItemStack(ModItems.PLATE_POLYMER.get(), 4))
                        && machine.inputs().get(2).matches(new ItemStack(ModItems.get("plate_steel").get(), 8))
                        && machine.inputs().get(3).matches(new ItemStack(ModItems.get("plate_copper").get(), 4))
                        && CircuitItem.type(machine.inputs().get(4).display()) == CircuitItem.CircuitType.ANALOG,
                "ass.centrifuge must preserve the exact source element, plate and Analog Circuit inputs");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void redstoneUsesExactOutputsAndSourceFreeFirstTick(GameTestHelper helper) {
        CentrifugeBlockEntity centrifuge = bareCentrifuge(helper, new BlockPos(3, 1, 3));
        check(helper, CentrifugeRecipes.recipeCount() == 13,
                "The Centrifuge table must expose its thirteen registered source recipes");
        centrifuge.setItem(CentrifugeBlockEntity.INPUT, new ItemStack(Items.REDSTONE_ORE));
        centrifuge.setPower(40_000L);

        tick(helper, centrifuge);
        check(helper, centrifuge.progress() == 1 && centrifuge.getPower() == 40_000L,
                "The source first processing tick must advance once without drawing HE");
        tick(helper, centrifuge);
        check(helper, centrifuge.progress() == 2 && centrifuge.getPower() == 39_800L,
                "Every later default processing tick must draw exactly 200 HE");
        for (int i = 2; i < CentrifugeBlockEntity.PROCESSING_SPEED; i++) tick(helper, centrifuge);

        check(helper, centrifuge.getItem(CentrifugeBlockEntity.INPUT).isEmpty()
                        && centrifuge.getPower() == 200L
                        && centrifuge.getItem(2).is(Items.REDSTONE)
                        && centrifuge.getItem(2).getCount() == 3
                        && centrifuge.getItem(3).is(Items.REDSTONE)
                        && centrifuge.getItem(3).getCount() == 3
                        && centrifuge.getItem(4).is(ModItems.get("nugget_mercury").get())
                        && centrifuge.getItem(4).getCount() == 1
                        && centrifuge.getItem(5).is(Items.GRAVEL),
                "Redstone Ore must become 3+3 Redstone, one Mercury Drop and one Gravel atomically");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void uraniumAndThoriumOresKeepTheirSourceOutputs(GameTestHelper helper) {
        ItemStack[] uranium = CentrifugeRecipes.getOutput(
                new ItemStack(ModItems.legacyOreBlockItem("ore_uranium").get()));
        check(helper, uranium != null && uranium.length == 4
                        && uranium[0].is(ModItems.get("powder_uranium").get()) && uranium[0].getCount() == 1
                        && uranium[1].is(ModItems.get("powder_uranium").get()) && uranium[1].getCount() == 1
                        && uranium[2].is(ModItems.get("nugget_ra226").get()) && uranium[2].getCount() == 1
                        && uranium[3].is(Items.GRAVEL) && uranium[3].getCount() == 1,
                "Uranium Ore must centrifuge into 1+1 Uranium Powder, one Ra-226 Nugget and Gravel");
        for (String variant : new String[]{"ore_gneiss_uranium", "ore_nether_uranium"}) {
            ItemStack[] variantOutput = CentrifugeRecipes.getOutput(
                    new ItemStack(ModItems.legacyOreBlockItem(variant).get()));
            check(helper, variantOutput != null && variantOutput.length == 4
                            && variantOutput[2].is(ModItems.get("nugget_ra226").get()),
                    variant + " must retain the shared source Uranium ore-dictionary recipe");
        }

        ItemStack[] thorium = CentrifugeRecipes.getOutput(
                new ItemStack(ModItems.legacyOreBlockItem("ore_thorium").get()));
        check(helper, thorium != null && thorium.length == 4
                        && thorium[0].is(ModItems.get("powder_thorium").get()) && thorium[0].getCount() == 1
                        && thorium[1].is(ModItems.get("powder_thorium").get()) && thorium[1].getCount() == 1
                        && thorium[2].is(ModItems.get("powder_uranium").get()) && thorium[2].getCount() == 1
                        && thorium[3].is(Items.GRAVEL) && thorium[3].getCount() == 1,
                "Thorium Ore must centrifuge into 1+1 Thorium Powder, one Uranium Powder and Gravel");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blockedOutputStallsAtomicallyAfterSourcePowerSequencing(GameTestHelper helper) {
        CentrifugeBlockEntity centrifuge = bareCentrifuge(helper, new BlockPos(3, 1, 3));
        centrifuge.setItem(CentrifugeBlockEntity.INPUT, new ItemStack(Items.REDSTONE_ORE));
        centrifuge.setPower(1_000L);
        tick(helper, centrifuge);
        centrifuge.setItem(4, new ItemStack(Blocks.DIRT));
        tick(helper, centrifuge);

        check(helper, centrifuge.getPower() == 800L && centrifuge.progress() == 0
                        && centrifuge.getItem(CentrifugeBlockEntity.INPUT).is(Items.REDSTONE_ORE)
                        && centrifuge.getItem(2).isEmpty() && centrifuge.getItem(3).isEmpty()
                        && centrifuge.getItem(4).is(Blocks.DIRT.asItem()) && centrifuge.getItem(5).isEmpty(),
                "A newly blocked output must preserve all items while retaining source pre-check HE draw order");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void upgradesClampAndPreserveExactIntegerFormula(GameTestHelper helper) {
        CentrifugeBlockEntity efficient = bareCentrifuge(helper, new BlockPos(2, 1, 2));
        efficient.setItem(CentrifugeBlockEntity.INPUT, new ItemStack(Items.REDSTONE_ORE));
        efficient.setItem(6, new ItemStack(ModItems.MACHINE_UPGRADES.get("upgrade_speed_3").get()));
        efficient.setItem(7, new ItemStack(ModItems.MACHINE_UPGRADES.get("upgrade_power_3").get()));
        efficient.setPower(1_000L);
        tick(helper, efficient);
        tick(helper, efficient);
        check(helper, efficient.consumption() == 200 && efficient.progress() == 8
                        && efficient.getPower() == 800L,
                "Speed III plus Power III must advance four per tick at the exact integer 200 HE/t cost");

        CentrifugeBlockEntity overdrive = bareCentrifuge(helper, new BlockPos(6, 1, 2));
        overdrive.setItem(CentrifugeBlockEntity.INPUT, new ItemStack(Items.REDSTONE_ORE));
        overdrive.setItem(6, new ItemStack(ModItems.MACHINE_UPGRADES.get("upgrade_overdrive_3").get()));
        overdrive.setPower(CentrifugeBlockEntity.MAX_POWER);
        tick(helper, overdrive);
        tick(helper, overdrive);
        check(helper, overdrive.consumption() == 30_200 && overdrive.progress() == 32
                        && overdrive.getPower() == 69_800L,
                "Overdrive III must advance sixteen per tick and draw the exact source 30,200 HE/t");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mercuryDropCapabilityFeedsExactDeshOperation(GameTestHelper helper) {
        ItemStack drop = new ItemStack(ModItems.get("nugget_mercury").get());
        IFluidHandlerItem handler = drop.getCapability(Capabilities.FluidHandler.ITEM);
        check(helper, handler != null && handler.getTankCapacity(0) == 125
                        && handler.getFluidInTank(0).getAmount() == 125
                        && handler.getFluidInTank(0).is(ModFluids.MERCURY.get()),
                "One source Drop of Mercury must expose exactly 125 mB Mercury");

        ChemicalPlantBlockEntity plant = bareChemicalPlant(helper, new BlockPos(3, 1, 3));
        check(helper, plant.selectRecipe(ChemicalPlantRecipes.DESH),
                "The exact chem.desh operation must be selectable");
        plant.setPower(10_000L);
        plant.setItem(ChemicalPlantBlockEntity.ITEM_INPUT_START,
                new ItemStack(ModItems.get("powder_desh_mix").get()));
        plant.inputTank(0).fill(new FluidStack(ModFluids.LIGHTOIL.get(), 200),
                IFluidHandler.FluidAction.EXECUTE);
        plant.setItem(ChemicalPlantBlockEntity.FLUID_INPUT_CONTAINER_START + 1,
                new ItemStack(ModItems.get("nugget_mercury").get(), 2));
        plant.setItem(ChemicalPlantBlockEntity.FLUID_INPUT_REMAINDER_START + 1, new ItemStack(Blocks.DIRT));

        tickChemicalPlant(helper, plant);
        check(helper, plant.inputTank(1).getFluidAmount() == 125
                        && plant.getItem(ChemicalPlantBlockEntity.FLUID_INPUT_CONTAINER_START + 1).getCount() == 1,
                "Mercury consumables must load even when the unused remainder lane is occupied");
        tickChemicalPlant(helper, plant);
        for (int i = 1; i < 100; i++) tickChemicalPlant(helper, plant);

        check(helper, plant.getPower() == 0L
                        && plant.getItem(ChemicalPlantBlockEntity.ITEM_INPUT_START).isEmpty()
                        && plant.inputTank(0).isEmpty()
                        && plant.inputTank(1).getFluidAmount() == 50
                        && plant.getItem(ChemicalPlantBlockEntity.ITEM_OUTPUT_START)
                        .is(ModItems.get("ingot_desh").get()),
                "One Desh Blend plus 200 mB Light Oil and Mercury must make one Desh Ingot in 100x100");
        helper.succeed();
    }

    private static CentrifugeBlockEntity placeCentrifuge(GameTestHelper helper, BlockPos relativeCore) {
        CentrifugeBlock block = ModBlocks.MACHINE_CENTRIFUGE.get();
        var state = block.defaultBlockState().setValue(CentrifugeBlock.FACING, Direction.NORTH);
        helper.setBlock(relativeCore, state);
        BlockPos absoluteCore = helper.absolutePos(relativeCore);
        block.setPlacedBy(helper.getLevel(), absoluteCore, state, helper.makeMockPlayer(GameType.SURVIVAL),
                new ItemStack(ModItems.MACHINE_CENTRIFUGE_ITEM.get()));
        return (CentrifugeBlockEntity) helper.getLevel().getBlockEntity(absoluteCore);
    }

    private static CentrifugeBlockEntity bareCentrifuge(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, ModBlocks.MACHINE_CENTRIFUGE.get().defaultBlockState());
        return helper.getBlockEntity(position);
    }

    private static ChemicalPlantBlockEntity bareChemicalPlant(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, ModBlocks.MACHINE_CHEMICAL_PLANT.get().defaultBlockState());
        return helper.getBlockEntity(position);
    }

    private static void tick(GameTestHelper helper, CentrifugeBlockEntity centrifuge) {
        CentrifugeBlockEntity.tick(helper.getLevel(), centrifuge.getBlockPos(), centrifuge.getBlockState(), centrifuge);
    }

    private static void tickChemicalPlant(GameTestHelper helper, ChemicalPlantBlockEntity plant) {
        ChemicalPlantBlockEntity.tick(helper.getLevel(), plant.getBlockPos(), plant.getBlockState(), plant);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
