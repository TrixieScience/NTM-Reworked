package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.AssemblyMachineBlock;
import com.hbm.ntm.anvil.AnvilRecipes;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.blockentity.AssemblyMachineProxyBlockEntity;
import com.hbm.ntm.item.MachineUpgradeItem;
import com.hbm.ntm.recipe.AssemblyRecipe;
import com.hbm.ntm.recipe.AssemblyRecipes;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AssemblyMachineGameTests {
    private static final ResourceLocation SHREDDER = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "ass.shredder");
    private AssemblyMachineGameTests() { }

    @GameTest(template = "empty")
    public static void structureUsesElevenPartsAndEightCapabilityProxies(GameTestHelper helper) {
        AssemblyMachineBlockEntity machine = placeMachine(helper, new BlockPos(3, 1, 3));
        int proxies = 0;
        for (BlockPos part : AssemblyMachineBlock.partPositions(machine.getBlockPos())) {
            helper.assertTrue(helper.getLevel().getBlockState(part).is(ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()),
                    "Every Assembly Machine structure position must use the shared block identity");
            if (helper.getLevel().getBlockEntity(part) instanceof AssemblyMachineProxyBlockEntity) proxies++;
        }
        helper.assertTrue(AssemblyMachineBlock.partPositions(machine.getBlockPos()).size() == 11,
                "Assembly Machine must occupy a 3x3 base plus two-block center column");
        helper.assertTrue(proxies == 8, "All eight bottom perimeter blocks must be capability proxies");
        BlockPos broken = machine.getBlockPos().north().west();
        helper.getLevel().destroyBlock(broken, false);
        for (BlockPos part : AssemblyMachineBlock.partPositions(machine.getBlockPos())) {
            helper.assertTrue(helper.getLevel().getBlockState(part).isAir(),
                    "Breaking any Assembly Machine part must tear down the full structure");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shredderRecipeLoadsExactSourceDefinition(GameTestHelper helper) {
        AssemblyRecipe recipe = AssemblyRecipes.get(SHREDDER);
        helper.assertTrue(recipe != null, "Datapack Assembly recipe hbm:ass.shredder must load");
        helper.assertTrue(recipe.duration() == 100 && recipe.power() == 100L,
                "ass.shredder must preserve 100 ticks and 100 HE/t");
        helper.assertTrue(recipe.inputs().size() == 3
                        && recipe.inputs().get(0).count() == 8
                        && recipe.inputs().get(1).count() == 4
                        && recipe.inputs().get(2).count() == 2,
                "ass.shredder must preserve 8 Steel Plates, 4 Copper Plates, and 2 Motors");
        helper.assertTrue(recipe.output().is(ModItems.MACHINE_SHREDDER_ITEM.get()) && recipe.output().getCount() == 1,
                "ass.shredder must output exactly one Shredder");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breedingReactorConstructionKeepsBothSourceAssemblySteps(GameTestHelper helper) {
        AssemblyRecipe core = AssemblyRecipes.byName("ass.reactorcore");
        helper.assertTrue(core != null && core.duration() == 100 && core.power() == 100L
                        && core.inputs().size() == 4
                        && core.inputs().get(0).count() == 4
                        && core.inputs().get(0).ingredient().test(CastPlateItem.create(ModItems.PLATE_CAST.get(),
                        CastPlateItem.CastPlateMaterial.LEAD, 1))
                        && core.inputs().get(1).count() == 8
                        && core.inputs().get(1).ingredient().test(new ItemStack(ModItems.get("ingot_beryllium").get()))
                        && core.inputs().get(2).count() == 8
                        && core.inputs().get(2).ingredient().test(new ItemStack(ModItems.get("plate_dura_steel").get()))
                        && core.inputs().get(3).count() == 4
                        && core.inputs().get(3).ingredient().test(new ItemStack(ModItems.get("ingot_asbestos").get()))
                        && core.output().is(ModItems.REACTOR_CORE.get()) && core.output().getCount() == 1,
                "ass.reactorcore must preserve Lead Cast Plate, Beryllium, Dura Steel, and Asbestos");

        AssemblyRecipe breeder = AssemblyRecipes.byName("ass.breedingreactor");
        helper.assertTrue(breeder != null && breeder.duration() == 200 && breeder.power() == 100L
                        && breeder.inputs().size() == 7
                        && breeder.inputs().get(0).count() == 1
                        && breeder.inputs().get(0).ingredient().test(new ItemStack(ModItems.REACTOR_CORE.get()))
                        && breeder.inputs().get(1).count() == 12
                        && breeder.inputs().get(1).ingredient().test(new ItemStack(ModItems.get("ingot_steel").get()))
                        && breeder.inputs().get(2).count() == 16
                        && breeder.inputs().get(2).ingredient().test(new ItemStack(ModItems.get("plate_lead").get()))
                        && breeder.inputs().get(3).count() == 4
                        && breeder.inputs().get(3).ingredient().test(new ItemStack(ModItems.REINFORCED_GLASS_ITEM.get()))
                        && breeder.inputs().get(4).count() == 4
                        && breeder.inputs().get(4).ingredient().test(new ItemStack(ModItems.get("ingot_asbestos").get()))
                        && breeder.inputs().get(5).count() == 4
                        && breeder.inputs().get(5).ingredient().test(new ItemStack(ModItems.get("ingot_tcalloy").get()))
                        && breeder.inputs().get(5).ingredient().test(new ItemStack(ModItems.get("ingot_cdalloy").get()))
                        && breeder.inputs().get(6).count() == 1
                        && breeder.inputs().get(6).ingredient().test(new ItemStack(ModItems.CRT_DISPLAY.get()))
                        && breeder.output().is(ModItems.MACHINE_REACTOR_BREEDING_ITEM.get())
                        && breeder.output().getCount() == 1,
                "ass.breedingreactor must preserve its exact seven source inputs and output");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pooledPlateRecipesPreserveSourceTableAndAutoswitchGroup(GameTestHelper helper) {
        String[] names = {"ass.plateiron", "ass.plategold", "ass.platetitanium", "ass.platealu",
                "ass.platesteel", "ass.platelead", "ass.platecopper", "ass.plategunmetal",
                "ass.plateweaponsteel", "ass.platedura"};
        String[] outputs = {"plate_iron", "plate_gold", "plate_titanium", "plate_aluminium",
                "plate_steel", "plate_lead", "plate_copper", "plate_gunmetal",
                "plate_weaponsteel", "plate_dura_steel"};
        ItemStack[] inputs = {new ItemStack(net.minecraft.world.item.Items.IRON_INGOT),
                new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT),
                new ItemStack(ModItems.get("ingot_titanium").get()),
                new ItemStack(ModItems.get("ingot_aluminium").get()),
                new ItemStack(ModItems.get("ingot_steel").get()),
                new ItemStack(ModItems.get("ingot_lead").get()),
                new ItemStack(ModItems.get("ingot_copper").get()),
                new ItemStack(ModItems.get("ingot_gunmetal").get()),
                new ItemStack(ModItems.get("ingot_weaponsteel").get()),
                new ItemStack(ModItems.get("ingot_dura_steel").get())};
        for (int index = 0; index < names.length; index++) {
            AssemblyRecipe recipe = AssemblyRecipes.byName(names[index]);
            helper.assertTrue(recipe != null && recipe.duration() == 60 && recipe.power() == 100L
                            && recipe.inputs().size() == 1 && recipe.inputs().getFirst().count() == 1
                            && recipe.inputs().getFirst().matches(inputs[index])
                            && recipe.output().is(ModItems.get(outputs[index]).get())
                            && recipe.output().getCount() == 1
                            && recipe.pools().equals(List.of("alt.plates"))
                            && recipe.autoswitch().equals(Optional.of("autoswitch.plates")),
                    names[index] + " must preserve its exact one-ingot plate recipe and pooled autoswitch metadata");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void anvilBootstrapUsesFourVacuumTubesAtTierTwo(GameTestHelper helper) {
        AnvilRecipes.Construction recipe = AnvilRecipes.byId(ResourceLocation.fromNamespaceAndPath(
                HbmNtm.MOD_ID, "anvil/machine_assembly_machine"));
        helper.assertTrue(recipe != null && recipe.validForTier(2) && !recipe.validForTier(1),
                "First Assembly Machine must be a Tier-2 NTM Anvil construction");
        helper.assertTrue(recipe.inputs().size() == 4
                        && recipe.inputs().get(0).count() == 8
                        && recipe.inputs().get(1).count() == 4
                        && recipe.inputs().get(2).count() == 2
                        && recipe.inputs().get(3).count() == 4,
                "Bootstrap must cost 8 Steel Ingots, 4 Copper Plates, 2 Motors, and 4 Vacuum Tubes");
        ItemStack tube = CircuitItem.create(ModItems.CIRCUIT.get(), CircuitItem.CircuitType.VACUUM_TUBE, 1);
        helper.assertTrue(recipe.inputs().get(3).matches(tube)
                        && !recipe.inputs().get(3).matches(CircuitItem.create(ModItems.CIRCUIT.get(),
                        CircuitItem.CircuitType.CAPACITOR, 1)),
                "Bootstrap circuit input must be exact Vacuum Tube identity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void selfRecipeActivatesOnlyWithExactBasicCircuit(GameTestHelper helper) {
        AssemblyRecipe recipe = AssemblyRecipes.get(ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "ass.assembler"));
        helper.assertTrue(recipe != null && recipe.duration() == 200 && recipe.power() == 100L
                        && recipe.inputs().size() == 4
                        && recipe.inputs().get(3).ingredient().test(CircuitItem.create(ModItems.CIRCUIT.get(),
                        CircuitItem.CircuitType.BASIC, 1))
                        && !recipe.inputs().get(3).ingredient().test(CircuitItem.create(ModItems.CIRCUIT.get(),
                        CircuitItem.CircuitType.ANALOG, 1))
                        && recipe.output().is(ModItems.MACHINE_ASSEMBLY_MACHINE_ITEM.get()),
                "ass.assembler must require exact Basic Circuit and preserve 200 ticks at 100 HE/t");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dynamicCapacityUsesOneHundredTicksOfSelectedPower(GameTestHelper helper) {
        AssemblyRecipe highPower = new AssemblyRecipe(ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "test"),
                "test", List.of(), Optional.empty(), new ItemStack(ModItems.MOTOR.get()), Optional.empty(),
                1, 20_000L, List.of(), Optional.empty());
        helper.assertTrue(AssemblyMachineBlockEntity.capacityFor(highPower, 0L, 100_000L) == 2_000_000L,
                "Selected recipe capacity must hold exactly 100 ticks of 20,000 HE/t");
        helper.assertTrue(AssemblyMachineBlockEntity.capacityFor(highPower, 3_000_000L, 100_000L) == 3_000_000L,
                "Dynamic capacity must never shrink below currently stored HE");
        helper.assertTrue(AssemblyMachineBlockEntity.capacityFor(null, 0L, 250_000L) == 250_000L,
                "No-recipe state must retain prior capacity while respecting the 100,000 HE floor");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shredderOperationConsumesExactlyTenThousandHe(GameTestHelper helper) {
        AssemblyMachineBlockEntity machine = bareMachine(helper, new BlockPos(3, 1, 3));
        loadShredderInputs(machine);
        helper.assertTrue(machine.selectRecipe(SHREDDER, false), "Server must accept loaded ass.shredder recipe");
        machine.setPower(10_000L);
        for (int tick = 0; tick < 99; tick++) tick(helper, machine);
        helper.assertTrue(machine.getItem(AssemblyMachineBlockEntity.OUTPUT).isEmpty(),
                "Assembly output must not appear before tick 100");
        helper.assertTrue(machine.getItem(AssemblyMachineBlockEntity.INPUT_START).getCount() == 8,
                "Inputs must not be consumed before completion");
        tick(helper, machine);
        helper.assertTrue(machine.getItem(AssemblyMachineBlockEntity.OUTPUT).is(ModItems.MACHINE_SHREDDER_ITEM.get()),
                "Tick 100 must produce one Shredder");
        helper.assertTrue(machine.getPower() == 0L, "100 ticks at 100 HE/t must consume exactly 10,000 HE");
        helper.assertTrue(machine.getItem(4).isEmpty() && machine.getItem(5).isEmpty() && machine.getItem(6).isEmpty(),
                "Completion must consume the three fixed input lanes atomically");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void batteryDischargesBeforeFirstProcessingTick(GameTestHelper helper) {
        AssemblyMachineBlockEntity machine = bareMachine(helper, new BlockPos(3, 1, 3));
        loadShredderInputs(machine);
        machine.selectRecipe(SHREDDER, false);
        machine.setItem(AssemblyMachineBlockEntity.BATTERY, new ItemStack(ModItems.BATTERY_CREATIVE.get()));
        tick(helper, machine);
        helper.assertTrue(machine.progress() > 0D && machine.getPower() == 99_900L,
                "Battery discharge must fill the empty buffer before same-tick 100 HE processing");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void runtimePacketSynchronizesActiveAnimationState(GameTestHelper helper) {
        AssemblyMachineBlockEntity serverMachine = bareMachine(helper, new BlockPos(2, 1, 2));
        loadShredderInputs(serverMachine);
        serverMachine.selectRecipe(SHREDDER, false);
        serverMachine.setPower(100_000L);
        tick(helper, serverMachine);
        helper.assertTrue(serverMachine.active(), "Powered server machine must be processing");

        AssemblyMachineBlockEntity clientMachine = bareMachine(helper, new BlockPos(5, 1, 2));
        clientMachine.onDataPacket(null, ClientboundBlockEntityDataPacket.create(serverMachine),
                helper.getLevel().registryAccess());
        helper.assertTrue(clientMachine.active(),
                "Runtime block-entity packets must activate client animation and sound");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void blockedOutputResetsProgressWithoutConsumingInputs(GameTestHelper helper) {
        AssemblyMachineBlockEntity machine = bareMachine(helper, new BlockPos(3, 1, 3));
        loadShredderInputs(machine);
        machine.selectRecipe(SHREDDER, false);
        machine.setPower(100_000L);
        machine.setItem(AssemblyMachineBlockEntity.OUTPUT,
                new ItemStack(ModItems.MACHINE_SHREDDER_ITEM.get(), 64));
        tick(helper, machine);
        helper.assertTrue(machine.progress() == 0D && machine.getPower() == 100_000L,
                "Full output must reject processing and reset progress before spending HE");
        helper.assertTrue(machine.getItem(4).getCount() == 8 && machine.getItem(5).getCount() == 4
                        && machine.getItem(6).getCount() == 2,
                "Blocked output must not partially consume Assembly inputs");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void automationIsFixedLaneAwareAndExtractsClogs(GameTestHelper helper) {
        AssemblyMachineBlockEntity machine = bareMachine(helper, new BlockPos(3, 1, 3));
        machine.selectRecipe(SHREDDER, false);
        ItemStack steel = new ItemStack(ModItems.get("plate_steel").get());
        ItemStack copper = new ItemStack(ModItems.get("plate_copper").get());
        helper.assertTrue(machine.canPlaceItemThroughFace(4, steel, Direction.NORTH)
                        && !machine.canPlaceItemThroughFace(4, copper, Direction.NORTH)
                        && machine.canPlaceItemThroughFace(5, copper, Direction.NORTH),
                "Automation insertion must respect each recipe's fixed input lane");
        machine.setItem(4, copper);
        helper.assertTrue(machine.canTakeItemThroughFace(4, copper, Direction.NORTH),
                "A mismatched occupied input lane must become externally extractable as a clog");
        helper.assertTrue(Arrays.equals(machine.getSlotsForFace(Direction.NORTH),
                        new int[]{4,5,6,7,8,9,10,11,12,13,14,15,16}),
                "Automation must expose exactly slots 4 through 16");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void upgradeLevelsSumClampAndPreserveSourceFormula(GameTestHelper helper) {
        AssemblyMachineBlockEntity machine = bareMachine(helper, new BlockPos(3, 1, 3));
        machine.setItem(2, new ItemStack(ModItems.MACHINE_UPGRADES.get("upgrade_speed_3").get()));
        machine.setItem(3, new ItemStack(ModItems.MACHINE_UPGRADES.get("upgrade_power_3").get()));
        helper.assertTrue(Math.abs(machine.speedMultiplier() - 2D) < 0.00001D,
                "Speed III must double Assembly progress speed");
        helper.assertTrue(Math.abs(machine.powerMultiplier() - 3.25D) < 0.00001D,
                "Source code must preserve Speed III +300% and Power III -75% power formula");
        machine.setItem(2, new ItemStack(ModItems.MACHINE_UPGRADES.get("upgrade_overdrive_3").get()));
        machine.setItem(3, ItemStack.EMPTY);
        helper.assertTrue(Math.abs(machine.speedMultiplier() - 4D) < 0.00001D
                        && Math.abs(machine.powerMultiplier() - 11D) < 0.00001D,
                "Overdrive III must preserve +300% speed and +1000% power multiplier contribution");
        helper.succeed();
    }

    private static AssemblyMachineBlockEntity placeMachine(GameTestHelper helper, BlockPos relativeCore) {
        AssemblyMachineBlock block = ModBlocks.MACHINE_ASSEMBLY_MACHINE.get();
        var state = block.defaultBlockState().setValue(AssemblyMachineBlock.FACING, Direction.NORTH);
        helper.setBlock(relativeCore, state);
        BlockPos absoluteCore = helper.absolutePos(relativeCore);
        block.setPlacedBy(helper.getLevel(), absoluteCore, state,
                helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(ModItems.MACHINE_ASSEMBLY_MACHINE_ITEM.get()));
        return (AssemblyMachineBlockEntity) helper.getLevel().getBlockEntity(absoluteCore);
    }

    private static AssemblyMachineBlockEntity bareMachine(GameTestHelper helper, BlockPos relativeCore) {
        helper.setBlock(relativeCore, ModBlocks.MACHINE_ASSEMBLY_MACHINE.get().defaultBlockState());
        return helper.getBlockEntity(relativeCore);
    }

    private static void loadShredderInputs(AssemblyMachineBlockEntity machine) {
        machine.setItem(4, new ItemStack(ModItems.get("plate_steel").get(), 8));
        machine.setItem(5, new ItemStack(ModItems.get("plate_copper").get(), 4));
        machine.setItem(6, new ItemStack(ModItems.MOTOR.get(), 2));
    }

    private static void tick(GameTestHelper helper, AssemblyMachineBlockEntity machine) {
        AssemblyMachineBlockEntity.tick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }
}
