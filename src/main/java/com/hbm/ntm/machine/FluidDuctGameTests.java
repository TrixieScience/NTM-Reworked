package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.FluidDuctBlock;
import com.hbm.ntm.block.HeatBoilerBlock;
import com.hbm.ntm.block.FluidUtilityBlock;
import com.hbm.ntm.block.FluidPumpBlock;
import com.hbm.ntm.block.DrainagePipeBlock;
import com.hbm.ntm.blockentity.DrainagePipeBlockEntity;
import com.hbm.ntm.blockentity.FluidPumpBlockEntity;
import com.hbm.ntm.blockentity.FluidUtilityBlockEntity;
import com.hbm.ntm.blockentity.HeatBoilerBlockEntity;
import com.hbm.ntm.blockentity.ArcWelderBlockEntity;
import com.hbm.ntm.item.FluidDuctItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.recipe.FluidDuctTypingRecipe;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FluidDuctGameTests {
    private FluidDuctGameTests() { }

    @GameTest(template = "empty")
    public static void exactBaseAndDynamicTypingRecipes(GameTestHelper helper) {
        ItemStack base = helper.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(
                HbmNtm.MOD_ID, "fluid_duct_neo")).orElseThrow().value()
                .getResultItem(helper.getLevel().registryAccess());
        check(helper, base.is(ModItems.FLUID_DUCT_NEO_ITEM.get()) && base.getCount() == 8,
                "Four Steel Plates and two Aluminium Plates must produce eight base ducts");

        ItemStack identifier = new ItemStack(ModItems.FLUID_IDENTIFIER_MULTI.get());
        FluidIdentifierItem.set(identifier, FluidIdentifierItem.Selection.STEAM, true);
        CraftingInput input = CraftingInput.of(2, 1,
                List.of(new ItemStack(ModItems.FLUID_DUCT_NEO_ITEM.get()), identifier));
        FluidDuctTypingRecipe typing = new FluidDuctTypingRecipe(CraftingBookCategory.MISC);
        ItemStack typed = typing.assemble(input, helper.getLevel().registryAccess());
        check(helper, typing.matches(input, helper.getLevel()) && typed.is(ModItems.FLUID_DUCT.get())
                        && FluidDuctItem.selection(typed) == FluidIdentifierItem.Selection.STEAM,
                "The source identifier recipe must copy Steam onto the typed duct item");
        check(helper, typing.getRemainingItems(input).get(1).is(ModItems.FLUID_IDENTIFIER_MULTI.get()),
                "The Multi Fluid Identifier must remain after typing a duct");
        FluidIdentifierItem.set(identifier, FluidIdentifierItem.Selection.HOTOIL, true);
        ItemStack hotOilDuct = typing.assemble(CraftingInput.of(2, 1,
                List.of(new ItemStack(ModItems.FLUID_DUCT_NEO_ITEM.get()), identifier)),
                helper.getLevel().registryAccess());
        check(helper, FluidDuctItem.selection(hotOilDuct) == FluidIdentifierItem.Selection.HOTOIL
                        && FluidIdentifierItem.Selection.HOTOIL.accepts(ModFluids.HOTOIL.get()),
                "The exact HOTOIL identity must be available to source typed ducts");
        FluidIdentifierItem.set(identifier, FluidIdentifierItem.Selection.ULTRAHOTSTEAM, true);
        ItemStack ultraSteamDuct = typing.assemble(CraftingInput.of(2, 1,
                List.of(new ItemStack(ModItems.FLUID_DUCT_NEO_ITEM.get()), identifier)),
                helper.getLevel().registryAccess());
        check(helper, FluidDuctItem.selection(ultraSteamDuct)
                        == FluidIdentifierItem.Selection.ULTRAHOTSTEAM
                        && FluidIdentifierItem.Selection.ULTRAHOTSTEAM.accepts(ModFluids.ULTRAHOTSTEAM.get()),
                "Typed ducts must carry Ultra Dense Steam as its own source fluid identity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sourceGeometryAndStrictTypeSeparation(GameTestHelper helper) {
        BlockPos first = new BlockPos(3, 2, 3);
        BlockPos second = first.east();
        helper.setBlock(first, ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.STEAM));
        helper.setBlock(second, ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.STEAM));
        helper.getLevel().updateNeighborsAt(helper.absolutePos(first), ModBlocks.FLUID_DUCT_NEO.get());
        helper.getLevel().updateNeighborsAt(helper.absolutePos(second), ModBlocks.FLUID_DUCT_NEO.get());
        var state = helper.getBlockState(first);
        check(helper, state.getValue(FluidDuctBlock.EAST) && state.getValue(FluidDuctBlock.WEST)
                        && state.getValue(FluidDuctBlock.RENDER_SHAPE) == FluidDuctBlock.DuctRenderShape.X,
                "A source duct with one connection must render and collide as a straight through-pipe");

        helper.setBlock(second, ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.WATER));
        helper.getLevel().updateNeighborsAt(helper.absolutePos(first), ModBlocks.FLUID_DUCT_NEO.get());
        state = helper.getBlockState(first);
        check(helper, state.getValue(FluidDuctBlock.NORTH) && state.getValue(FluidDuctBlock.SOUTH)
                        && state.getValue(FluidDuctBlock.UP) && state.getValue(FluidDuctBlock.DOWN)
                        && state.getValue(FluidDuctBlock.EAST) && state.getValue(FluidDuctBlock.WEST)
                        && state.getValue(FluidDuctBlock.RENDER_SHAPE) == FluidDuctBlock.DuctRenderShape.ISOLATED,
                "An isolated source duct must expose all six arms");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void zeroStorageNetworkRoutesFluidToBoilerPort(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 1, 3);
        HeatBoilerBlock boilerBlock = ModBlocks.MACHINE_BOILER.get();
        var boilerState = boilerBlock.defaultBlockState().setValue(HeatBoilerBlock.FACING, Direction.NORTH);
        helper.setBlock(boilerPos, boilerState);
        BlockPos absoluteBoiler = helper.absolutePos(boilerPos);
        boilerBlock.setPlacedBy(helper.getLevel(), absoluteBoiler, boilerState,
                helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(ModItems.MACHINE_BOILER_ITEM.get()));
        HeatBoilerBlockEntity boiler = (HeatBoilerBlockEntity) helper.getLevel().getBlockEntity(absoluteBoiler);

        BlockPos near = boilerPos.east(2);
        BlockPos far = boilerPos.east(3);
        var ductState = ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.WATER);
        helper.setBlock(near, ductState);
        helper.setBlock(far, ductState);
        IFluidHandler duct = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(far), Direction.EAST);
        check(helper, duct != null && duct.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1_000),
                        IFluidHandler.FluidAction.EXECUTE) == 1_000,
                "A typed zero-storage duct network must route Water across connected nodes");
        check(helper, boiler != null && boiler.inputTank().getFluidAmount() == 1_000,
                "The routed Water must arrive through the Boiler's outward-only source port");
        check(helper, duct.fill(new FluidStack(ModFluids.STEAM.get(), 100),
                        IFluidHandler.FluidAction.EXECUTE) == 0,
                "Water ducts must reject Steam by strict source fluid identity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void oilDuctRoutesIntoConfiguredBoiler(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 1, 3);
        HeatBoilerBlock boilerBlock = ModBlocks.MACHINE_BOILER.get();
        var boilerState = boilerBlock.defaultBlockState().setValue(HeatBoilerBlock.FACING, Direction.NORTH);
        helper.setBlock(boilerPos, boilerState);
        BlockPos absoluteBoiler = helper.absolutePos(boilerPos);
        boilerBlock.setPlacedBy(helper.getLevel(), absoluteBoiler, boilerState,
                helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(ModItems.MACHINE_BOILER_ITEM.get()));
        HeatBoilerBlockEntity boiler = (HeatBoilerBlockEntity) helper.getLevel().getBlockEntity(absoluteBoiler);
        check(helper, boiler != null && boiler.configureInput(FluidIdentifierItem.Selection.OIL),
                "Oil must be a valid source Boiler input mode");

        BlockPos near = boilerPos.east(2);
        BlockPos far = boilerPos.east(3);
        var ductState = ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.OIL);
        helper.setBlock(near, ductState);
        helper.setBlock(far, ductState);
        IFluidHandler duct = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(far), Direction.EAST);
        check(helper, duct != null && duct.fill(new FluidStack(ModFluids.OIL.get(), 1_000),
                        IFluidHandler.FluidAction.EXECUTE) == 1_000,
                "An Oil-typed zero-storage duct must route Crude Oil into a configured Boiler");
        check(helper, boiler.inputTank().getFluidAmount() == 1_000
                        && boiler.inputTank().getFluid().is(ModFluids.OIL.get()),
                "Routed Oil must arrive through the Boiler's source outward-only port");
        check(helper, duct.fill(new FluidStack(ModFluids.HOTOIL.get(), 1),
                        IFluidHandler.FluidAction.EXECUTE) == 0,
                "Oil ducts must reject Hot Oil by strict source fluid identity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void boilerPushesHotOilAcrossTypedDuctNetwork(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 2, 3);
        helper.setBlock(boilerPos, ModBlocks.MACHINE_BOILER.get().defaultBlockState()
                .setValue(HeatBoilerBlock.FACING, Direction.NORTH));
        HeatBoilerBlockEntity boiler = helper.getBlockEntity(boilerPos);
        check(helper, boiler.configureInput(FluidIdentifierItem.Selection.OIL),
                "Oil must be a valid source Boiler input mode");
        boiler.outputTank().fill(new FluidStack(ModFluids.HOTOIL.get(), 750),
                IFluidHandler.FluidAction.EXECUTE);

        var ductState = ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.HOTOIL);
        helper.setBlock(boilerPos.east(2), ductState);
        helper.setBlock(boilerPos.east(3), ductState);
        BlockPos receiverPos = boilerPos.east(4);
        helper.setBlock(receiverPos, ModBlocks.MACHINE_ARC_WELDER.get().defaultBlockState());
        ArcWelderBlockEntity receiver = helper.getBlockEntity(receiverPos);
        ItemStack identifier = new ItemStack(ModItems.FLUID_IDENTIFIER_MULTI.get());
        FluidIdentifierItem.set(identifier, FluidIdentifierItem.Selection.HOTOIL, true);
        receiver.setItem(ArcWelderBlockEntity.FLUID_IDENTIFIER, identifier);
        ArcWelderBlockEntity.tick(helper.getLevel(), receiver.getBlockPos(), receiver.getBlockState(), receiver);

        HeatBoilerBlockEntity.tick(helper.getLevel(), boiler.getBlockPos(), boiler.getBlockState(), boiler);
        check(helper, boiler.outputTank().isEmpty()
                        && receiver.tank().getFluidAmount() == 750
                        && receiver.tank().getFluid().is(ModFluids.HOTOIL.get()),
                "Boiler output pushing must deliver Hot Oil through HOTOIL ducts to a typed receiver");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void valveSplitsAndRejoinsTypedNetwork(GameTestHelper helper) {
        BlockPos boilerPos = new BlockPos(3, 1, 3);
        HeatBoilerBlock boilerBlock = ModBlocks.MACHINE_BOILER.get();
        BlockState boilerState = boilerBlock.defaultBlockState().setValue(HeatBoilerBlock.FACING, Direction.NORTH);
        helper.setBlock(boilerPos, boilerState);
        boilerBlock.setPlacedBy(helper.getLevel(), helper.absolutePos(boilerPos), boilerState,
                helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(ModItems.MACHINE_BOILER_ITEM.get()));

        BlockPos valvePos = boilerPos.east(2);
        helper.setBlock(valvePos, ModBlocks.FLUID_VALVE.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.WATER));
        BlockPos ductPos = boilerPos.east(3);
        helper.setBlock(ductPos, ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.WATER));
        IFluidHandler duct = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(ductPos), Direction.EAST);
        check(helper, duct != null && duct.fill(new FluidStack(
                net.minecraft.world.level.material.Fluids.WATER, 250),
                IFluidHandler.FluidAction.EXECUTE) == 0,
                "A closed source Fluid Valve must split its typed network");

        helper.setBlock(valvePos, helper.getBlockState(valvePos).setValue(FluidUtilityBlock.OPEN, true));
        check(helper, duct.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 250),
                IFluidHandler.FluidAction.EXECUTE) == 250,
                "An open source Fluid Valve must rejoin its typed network");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void counterRecordsAcceptedFluidAndRorReset(GameTestHelper helper) throws Exception {
        BlockPos boilerPos = new BlockPos(3, 1, 3);
        HeatBoilerBlock boilerBlock = ModBlocks.MACHINE_BOILER.get();
        BlockState boilerState = boilerBlock.defaultBlockState().setValue(HeatBoilerBlock.FACING, Direction.NORTH);
        helper.setBlock(boilerPos, boilerState);
        boilerBlock.setPlacedBy(helper.getLevel(), helper.absolutePos(boilerPos), boilerState,
                helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(ModItems.MACHINE_BOILER_ITEM.get()));

        BlockPos counterPos = boilerPos.east(2);
        helper.setBlock(counterPos, ModBlocks.FLUID_COUNTER_VALVE.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.WATER)
                .setValue(FluidUtilityBlock.OPEN, true));
        BlockPos ductPos = boilerPos.east(3);
        helper.setBlock(ductPos, ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.WATER));
        IFluidHandler duct = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(ductPos), Direction.EAST);
        check(helper, duct != null && duct.fill(new FluidStack(
                net.minecraft.world.level.material.Fluids.WATER, 400),
                IFluidHandler.FluidAction.EXECUTE) == 400,
                "The counter valve must pass accepted fluid while open");
        FluidUtilityBlockEntity counter = helper.getBlockEntity(counterPos);
        check(helper, counter.counter() == 400
                        && "400".equals(counter.provideRorValue("VAL:value")),
                "The counter and ROR value must record actual accepted transfer");
        counter.runRorFunction("FUN:reset", new String[0]);
        check(helper, counter.counter() == 0, "The source ROR reset function must clear the counter");
        counter.runRorFunction("FUN:setstate", new String[]{"0"});
        check(helper, !counter.routesFluid(), "The source ROR state function must close the counter valve");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void flowControlPumpIsDirectionalRateLimitedAndRedstoneLocked(GameTestHelper helper) {
        BlockPos sourcePos = new BlockPos(3, 2, 3);
        BlockPos sinkPos = sourcePos.east();
        BlockState pumpState = ModBlocks.FLUID_PUMP.get().defaultBlockState()
                .setValue(FluidPumpBlock.FACING, Direction.SOUTH);
        helper.setBlock(sourcePos, pumpState);
        helper.setBlock(sinkPos, pumpState);
        FluidPumpBlockEntity source = helper.getBlockEntity(sourcePos);
        FluidPumpBlockEntity sink = helper.getBlockEntity(sinkPos);
        source.selectFluid(FluidIdentifierItem.Selection.WATER);
        sink.selectFluid(FluidIdentifierItem.Selection.WATER);
        source.configure(100, 0, 2);
        sink.configure(500, 0, 2);

        BlockPos absoluteSource = helper.absolutePos(sourcePos);
        IFluidHandler wrongSide = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                absoluteSource, Direction.NORTH);
        IFluidHandler input = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                absoluteSource, Direction.WEST);
        check(helper, wrongSide == null && input != null
                        && input.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 250),
                        IFluidHandler.FluidAction.EXECUTE) == 100,
                "The pump must accept only from its source-rotated input and cap its buffer at throughput");

        FluidPumpBlockEntity.serverTick(helper.getLevel(), absoluteSource, source.getBlockState(), source);
        check(helper, source.amount() == 0 && sink.amount() == 100,
                "The pump must send one throughput-limited batch through its opposite output");

        check(helper, input.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 100),
                IFluidHandler.FluidAction.EXECUTE) == 100, "The drained source buffer must refill");
        helper.setBlock(sourcePos.north(), Blocks.REDSTONE_BLOCK);
        FluidPumpBlockEntity.serverTick(helper.getLevel(), absoluteSource, source.getBlockState(), source);
        check(helper, source.amount() == 100 && sink.amount() == 100 && source.redstone(),
                "Redstone must stop pump output without refusing or destroying buffered fluid");

        source.configure(100, 1, 2);
        check(helper, source.amount() == 0 && source.pressure() == 1,
                "Changing source pressure must clear the pump tank");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void flowControlPumpPriorityOrdersDuctReceivers(GameTestHelper helper) {
        BlockPos ductPos = new BlockPos(4, 2, 4);
        BlockPos lowPos = ductPos.west();
        BlockPos highPos = ductPos.east();
        helper.setBlock(ductPos, ModBlocks.FLUID_DUCT_NEO.get().defaultBlockState()
                .setValue(FluidDuctBlock.TYPE, FluidIdentifierItem.Selection.WATER));
        helper.setBlock(lowPos, ModBlocks.FLUID_PUMP.get().defaultBlockState()
                .setValue(FluidPumpBlock.FACING, Direction.NORTH));
        helper.setBlock(highPos, ModBlocks.FLUID_PUMP.get().defaultBlockState()
                .setValue(FluidPumpBlock.FACING, Direction.SOUTH));
        FluidPumpBlockEntity low = helper.getBlockEntity(lowPos);
        FluidPumpBlockEntity high = helper.getBlockEntity(highPos);
        low.selectFluid(FluidIdentifierItem.Selection.WATER);
        high.selectFluid(FluidIdentifierItem.Selection.WATER);
        low.configure(100, 0, 0);
        high.configure(100, 0, 4);

        IFluidHandler duct = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(ductPos), Direction.UP);
        check(helper, duct != null && duct.fill(new FluidStack(
                net.minecraft.world.level.material.Fluids.WATER, 100),
                IFluidHandler.FluidAction.EXECUTE) == 100,
                "The typed duct must accept the pump-priority test fluid");
        check(helper, high.amount() == 100 && low.amount() == 0,
                "Highest-priority pump receivers must fill before lowest-priority receivers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void drainagePipeMatchesSourceFootprintInputsAndHalfDrain(GameTestHelper helper) {
        BlockPos core = new BlockPos(4, 2, 5);
        Direction facing = Direction.SOUTH;
        BlockState base = ModBlocks.MACHINE_DRAIN.get().defaultBlockState()
                .setValue(DrainagePipeBlock.FACING, facing);
        for (int part = 0; part < 3; part++) {
            helper.setBlock(core.relative(facing.getOpposite(), part),
                    base.setValue(DrainagePipeBlock.PART, part));
        }

        check(helper, DrainagePipeBlock.corePosition(core.north(2),
                        helper.getBlockState(core.north(2))).equals(core),
                "Every drainage-pipe dummy must resolve back to the core");
        BlockPos absoluteCore = helper.absolutePos(core);
        IFluidHandler front = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                absoluteCore, Direction.SOUTH);
        IFluidHandler side = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                absoluteCore, Direction.EAST);
        IFluidHandler back = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                absoluteCore, Direction.NORTH);
        IFluidHandler top = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                absoluteCore, Direction.UP);
        check(helper, front != null && side != null && back == null && top == null,
                "The source drain must accept fluid at its front and sides only");

        DrainagePipeBlockEntity drain = helper.getBlockEntity(core);
        drain.selectFluid(FluidIdentifierItem.Selection.WATER);
        check(helper, front.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 2_000),
                        IFluidHandler.FluidAction.EXECUTE) == 2_000,
                "The source drain must hold a 2,000 mB selected-fluid buffer");
        DrainagePipeBlockEntity.tick(helper.getLevel(), absoluteCore, drain.getBlockState(), drain);
        check(helper, drain.amount() == 1_000,
                "Each source drain tick must discard half of its stored fluid");
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
