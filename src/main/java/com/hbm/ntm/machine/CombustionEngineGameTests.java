package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.CombustionEngineBlock;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.blockentity.CombustionEngineProxyBlockEntity;
import com.hbm.ntm.foundry.FoundryMaterial;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.item.DenseWireItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.recipe.AssemblyRecipe;
import com.hbm.ntm.recipe.AssemblyRecipes;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CombustionEngineGameTests {
    private CombustionEngineGameTests() { }

    @GameTest(template = "empty")
    public static void footprintAndFourComboPortsMatchSource(GameTestHelper helper) {
        BlockPos core = new BlockPos(8, 2, 8);
        var parts = CombustionEngineBlock.partPositions(core, Direction.SOUTH);
        check(helper, parts.size() == 28 && new HashSet<>(parts).size() == 28,
                "Combustion engine needs its 6x2x2 body plus four connection dummies");
        var connections = CombustionEngineBlock.connections(core, Direction.SOUTH);
        check(helper, connections.size() == 4
                        && connections.stream().filter(c -> c.outward() == Direction.SOUTH).count() == 2
                        && connections.stream().filter(c -> c.outward() == Direction.NORTH).count() == 2,
                "Two power/fluid ports must remain at each end of the engine");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void proxyOnlyConnectsThroughItsOutsideFace(GameTestHelper helper) {
        BlockPos core = new BlockPos(8, 2, 8);
        helper.setBlock(core, coreState(Direction.SOUTH));
        BlockPos port = core.south().west();
        helper.setBlock(port, coreState(Direction.SOUTH)
                .setValue(CombustionEngineBlock.PART_DEPTH, 3)
                .setValue(CombustionEngineBlock.PART_SIDE, 4));
        CombustionEngineProxyBlockEntity proxy = helper.getBlockEntity(port);
        IFluidHandler handler = proxy.fluidHandler(Direction.SOUTH);
        check(helper, handler != null && proxy.fluidHandler(Direction.NORTH) == null,
                "ProxyCombo must keep its source outward-only connection");
        check(helper, handler.fill(new FluidStack(ModFluids.DIESEL.get(), 250),
                        IFluidHandler.FluidAction.EXECUTE) == 250,
                "Selected diesel must enter through any combo port");
        CombustionEngineBlockEntity engine = helper.getBlockEntity(core);
        check(helper, engine.fuelAmount() == 250, "Proxy fuel must land in the core tank");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tenthsThrottleAndPistonGradeKeepSourceMath(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 2, 4);
        helper.setBlock(pos, coreState(Direction.SOUTH));
        CombustionEngineBlockEntity engine = helper.getBlockEntity(pos);
        engine.setItem(CombustionEngineBlockEntity.PISTON_SET,
                new ItemStack(ModItems.PISTON_SET_STEEL.get()));
        engine.setFuelForTest(FluidIdentifierItem.Selection.DIESEL, 10, 0);
        engine.setControl(CombustionEngineBlockEntity.Control.THROTTLE, 30);
        engine.setControl(CombustionEngineBlockEntity.Control.TOGGLE, 0);
        engine.runForTest(helper.getLevel());
        check(helper, engine.fuelAmount() == 4 && engine.getPower() == 750L && engine.active(),
                "Steel pistons at throttle 30 must burn 6mB Diesel and make exactly 750 HE");

        engine.setFuelForTest(FluidIdentifierItem.Selection.KEROSENE, 10, 0);
        engine.setPower(0L);
        engine.runForTest(helper.getLevel());
        check(helper, engine.fuelAmount() == 10 && engine.getPower() == 0L && !engine.active(),
                "Steel pistons must retain their source zero-percent Aviation efficiency");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void constructionRecipeKeepsTheFiveSourceInputs(GameTestHelper helper) {
        AssemblyRecipe recipe = AssemblyRecipes.byName("ass.combustiongen");
        check(helper, recipe != null && recipe.duration() == 300 && recipe.power() == 100L
                        && recipe.inputs().size() == 5
                        && recipe.output().is(ModItems.MACHINE_COMBUSTION_ENGINE_ITEM.get()),
                "ass.combustiongen must stay a five-input 300-tick recipe at 100 HE/t");
        if (recipe == null) return;
        check(helper, recipe.inputs().get(0).matches(new ItemStack(ModItems.get("plate_steel").get(), 16))
                        && recipe.inputs().get(1).matches(new ItemStack(ModItems.get("ingot_copper").get(), 12))
                        && recipe.inputs().get(2).matches(DenseWireItem.create(ModItems.WIRE_DENSE.get(),
                        FoundryMaterial.GOLD, 8))
                        && recipe.inputs().get(3).matches(new ItemStack(ModItems.CANISTER_EMPTY.get(), 4))
                        && recipe.inputs().get(4).matches(CircuitItem.create(ModItems.CIRCUIT.get(),
                        CircuitItem.CircuitType.BASIC, 1)),
                "Construction must keep Steel Plates, Copper, Dense Gold Wire, canisters and a Basic Circuit");
        helper.succeed();
    }

    private static BlockState coreState(Direction facing) {
        return ModBlocks.MACHINE_COMBUSTION_ENGINE.get().defaultBlockState()
                .setValue(CombustionEngineBlock.FACING, facing)
                .setValue(CombustionEngineBlock.PART_DEPTH, 2)
                .setValue(CombustionEngineBlock.PART_SIDE, 3)
                .setValue(CombustionEngineBlock.PART_Y, 0);
    }
    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
