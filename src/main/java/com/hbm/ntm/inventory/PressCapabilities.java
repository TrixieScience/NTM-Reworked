package com.hbm.ntm.inventory;

import com.hbm.ntm.item.SourceFluidContainerHandler;
import com.hbm.ntm.item.SourceFluidContainerItem;
import com.hbm.ntm.item.BlowtorchFluidHandler;
import com.hbm.ntm.item.InfiniteFluidBarrelHandler;
import com.hbm.ntm.item.UniversalFluidContainerHandler;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public final class PressCapabilities {
    private PressCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_PRESS.get(),
                (press, side) -> new SidedInvWrapper(press, side == null ? Direction.DOWN : side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_SHREDDER.get(),
                (shredder, side) -> new SidedInvWrapper(shredder, side == null ? Direction.DOWN : side)
        );
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.HEATER_FIREBOX.get(),
                (firebox, side) -> new SidedInvWrapper(firebox, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.HEATER_OVEN.get(),
                (oven, side) -> new SidedInvWrapper(oven, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_ASHPIT.get(),
                (ashpit, side) -> new SidedInvWrapper(ashpit, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.THERMAL_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_SAWMILL.get(),
                (sawmill, side) -> new SidedInvWrapper(sawmill, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.HEATER_FIREBOX.get(),
                (firebox, side) -> firebox.smokeHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.HEATER_OVEN.get(),
                (oven, side) -> oven.smokeHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.HEATER_OILBURNER_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.HEATER_HEATEX_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_BLAST_FURNACE.get(),
                (furnace, side) -> new SidedInvWrapper(furnace, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_BLAST_FURNACE.get(),
                (furnace, side) -> furnace.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_BLAST_FURNACE_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_BLAST_FURNACE_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FURNACE_COMBINATION.get(),
                (oven, side) -> new SidedInvWrapper(oven, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FURNACE_COMBINATION.get(),
                (oven, side) -> oven.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FURNACE_COMBINATION_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FURNACE_COMBINATION_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FURNACE_STEEL.get(),
                (furnace, side) -> new SidedInvWrapper(furnace, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FURNACE_STEEL_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_ELECTRIC_FURNACE.get(),
                (furnace, side) -> new SidedInvWrapper(furnace, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_FURNACE_BRICK.get(),
                (furnace, side) -> new SidedInvWrapper(furnace, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_WOOD_BURNER.get(),
                (burner, side) -> new SidedInvWrapper(burner, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_WOOD_BURNER_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_MICROWAVE.get(),
                (microwave, side) -> new SidedInvWrapper(microwave, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_WOOD_BURNER_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.THERMAL_PROXY.get(),
                (proxy, side) -> proxy.smokeHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_BOILER_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_STEAM_ENGINE_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_INDUSTRIAL_TURBINE_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_TURBINE_GAS.get(),
                (turbine, side) -> new SidedInvWrapper(turbine, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_TURBINE_GAS_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_COMBUSTION_ENGINE.get(),
                (engine, side) -> new SidedInvWrapper(engine, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_COMBUSTION_ENGINE_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_TURBOFAN.get(),
                (turbofan, side) -> new SidedInvWrapper(turbofan, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_TURBOFAN_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_TURBINE.get(),
                (turbine, side) -> new SidedInvWrapper(turbine, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_TURBINE.get(),
                (turbine, side) -> turbine.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.REACTOR_ZIRNOX.get(),
                (reactor, side) -> reactor.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.REACTOR_ZIRNOX.get(),
                (reactor, side) -> new SidedInvWrapper(reactor, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.REACTOR_ZIRNOX_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_REACTOR_BREEDING.get(),
                (reactor, side) -> new SidedInvWrapper(reactor, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_REACTOR_BREEDING_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.REACTOR_RESEARCH.get(),
                (reactor, side) -> new SidedInvWrapper(reactor, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.REACTOR_RESEARCH_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_RADGEN.get(),
                (radGen, side) -> new SidedInvWrapper(radGen, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_RADGEN_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_WASTE_DRUM.get(),
                (drum, side) -> new SidedInvWrapper(drum, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.PUMP_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_INTAKE.get(),
                (intake, side) -> side != null && side.getAxis().isHorizontal()
                        ? intake.outputHandler() : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_INTAKE_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_CONDENSER.get(),
                (condenser, side) -> condenser.fluidHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_CONDENSER_POWERED_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.FLUID_DUCT.get(),
                (duct, side) -> duct.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_FLUIDTANK_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_WELL.get(),
                (derrick, side) -> derrick.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_DIESEL.get(),
                (generator, side) -> new SidedInvWrapper(generator, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.MACHINE_DIESEL.get(),
                (generator, side) -> generator.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DFC_CORE.get(),
                (core, side) -> new SidedInvWrapper(core, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.DFC_EMITTER.get(),
                (emitter, side) -> emitter.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DFC_INJECTOR.get(),
                (injector, side) -> new SidedInvWrapper(injector, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.DFC_INJECTOR.get(),
                (injector, side) -> injector.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ModBlockEntities.DFC_RECEIVER.get(),
                (receiver, side) -> receiver.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DFC_STABILIZER.get(),
                (stabilizer, side) -> new SidedInvWrapper(stabilizer, side == null ? Direction.DOWN : side));
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> SourceFluidContainerHandler.empty(stack,
                        new SourceFluidContainerItem.ContainedFluid[]{
                                SourceFluidContainerItem.ContainedFluid.OIL,
                                SourceFluidContainerItem.ContainedFluid.HEAVYOIL,
                                SourceFluidContainerItem.ContainedFluid.NAPHTHA,
                                SourceFluidContainerItem.ContainedFluid.LIGHTOIL,
                                SourceFluidContainerItem.ContainedFluid.BITUMEN,
                                SourceFluidContainerItem.ContainedFluid.SMEAR,
                                SourceFluidContainerItem.ContainedFluid.HEATINGOIL,
                                SourceFluidContainerItem.ContainedFluid.WOODOIL,
                                SourceFluidContainerItem.ContainedFluid.COALCREOSOTE,
                                SourceFluidContainerItem.ContainedFluid.LUBRICANT,
                                SourceFluidContainerItem.ContainedFluid.DIESEL,
                                SourceFluidContainerItem.ContainedFluid.KEROSENE},
                        ModItems.CANISTER_EMPTY::get, ModItems.CANISTER_FULL::get, 1_000),
                ModItems.CANISTER_EMPTY.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> SourceFluidContainerHandler.full(stack,
                        ModItems.CANISTER_EMPTY::get, ModItems.CANISTER_FULL::get, 1_000),
                ModItems.CANISTER_FULL.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> SourceFluidContainerHandler.empty(stack,
                        new SourceFluidContainerItem.ContainedFluid[]{
                                SourceFluidContainerItem.ContainedFluid.GAS,
                                SourceFluidContainerItem.ContainedFluid.PETROLEUM,
                                SourceFluidContainerItem.ContainedFluid.HYDROGEN,
                                SourceFluidContainerItem.ContainedFluid.OXYGEN,
                                SourceFluidContainerItem.ContainedFluid.DEUTERIUM,
                                SourceFluidContainerItem.ContainedFluid.TRITIUM,
                                SourceFluidContainerItem.ContainedFluid.UNSATURATEDS},
                        ModItems.GAS_EMPTY::get, ModItems.GAS_FULL::get, 1_000),
                ModItems.GAS_EMPTY.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> SourceFluidContainerHandler.full(stack,
                        ModItems.GAS_EMPTY::get, ModItems.GAS_FULL::get, 1_000),
                ModItems.GAS_FULL.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> SourceFluidContainerHandler.fixedEmpty(stack,
                        SourceFluidContainerItem.ContainedFluid.TRITIUM,
                        ModItems.CELL_EMPTY::get, ModItems.CELL_TRITIUM::get, 1_000),
                ModItems.CELL_EMPTY.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> SourceFluidContainerHandler.fixedFull(stack,
                        SourceFluidContainerItem.ContainedFluid.TRITIUM,
                        ModItems.CELL_EMPTY::get, ModItems.CELL_TRITIUM::get, 1_000),
                ModItems.CELL_TRITIUM.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> SourceFluidContainerHandler.consumable(stack, ModFluids.OIL::get, 250),
                ModItems.ORE_OIL_ITEM.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> SourceFluidContainerHandler.consumable(stack, ModFluids.MERCURY::get, 125),
                ModItems.get("nugget_mercury").get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new UniversalFluidContainerHandler(stack, false),
                ModItems.FLUID_TANK_EMPTY.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new UniversalFluidContainerHandler(stack, true),
                ModItems.FLUID_TANK_FULL.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new BlowtorchFluidHandler(stack),
                ModItems.BLOWTORCH.get());
        event.registerItem(Capabilities.FluidHandler.ITEM,
                (stack, context) -> new InfiniteFluidBarrelHandler(stack),
                ModItems.FLUID_BARREL_INFINITE.get());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_ASSEMBLY_MACHINE_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_ASSEMBLY_MACHINE_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_CHEMICAL_PLANT.get(),
                (plant, side) -> new SidedInvWrapper(plant, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_CHEMICAL_PLANT.get(),
                (plant, side) -> plant.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_CHEMICAL_PLANT_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_CHEMICAL_PLANT_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_SOLDERING_STATION.get(),
                (station, side) -> new SidedInvWrapper(station, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_SOLDERING_STATION.get(),
                (station, side) -> station.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_SOLDERING_STATION_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_SOLDERING_STATION_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_ARC_WELDER.get(),
                (welder, side) -> new SidedInvWrapper(welder, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_ARC_WELDER.get(),
                (welder, side) -> welder.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_ARC_WELDER_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_ARC_WELDER_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_ARC_FURNACE.get(),
                (furnace, side) -> new SidedInvWrapper(furnace, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_ARC_FURNACE_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_REFINERY.get(),
                (refinery, side) -> new SidedInvWrapper(refinery, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_REFINERY.get(),
                (refinery, side) -> side == Direction.DOWN ? null : refinery.inputFluidHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_REFINERY_PROXY.get(),
                (proxy, side) -> side != null && proxy.canConnect(side)
                        ? new SidedInvWrapper(proxy, side) : null);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_REFINERY_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_CENTRIFUGE.get(),
                (centrifuge, side) -> new SidedInvWrapper(centrifuge, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MACHINE_CENTRIFUGE_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_CATALYTIC_CRACKER.get(),
                (tower, side) -> tower.fluidHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_CATALYTIC_CRACKER_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_FRACTION_TOWER.get(),
                (tower, side) -> tower.fluidHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MACHINE_FRACTION_TOWER_PROXY.get(),
                (proxy, side) -> proxy.fluidHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_CRUCIBLE.get(),
                (crucible, side) -> new SidedInvWrapper(crucible, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MACHINE_CRUCIBLE_PROXY.get(),
                (proxy, side) -> new SidedInvWrapper(proxy, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.FOUNDRY_MOLD.get(),
                (mold, side) -> new SidedInvWrapper(mold, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CRANE_BOXER.get(),
                (boxer, side) -> new SidedInvWrapper(boxer, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CRANE_EXTRACTOR.get(),
                (extractor, side) -> new SidedInvWrapper(extractor, side == null ? Direction.DOWN : side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.CRANE_INSERTER.get(),
                (inserter, side) -> new SidedInvWrapper(inserter, side == null ? Direction.DOWN : side));
    }
}
