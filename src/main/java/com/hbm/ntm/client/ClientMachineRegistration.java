package com.hbm.ntm.client;

import com.hbm.ntm.client.render.AshpitRenderer;
import com.hbm.ntm.client.render.AirIntakeItemRenderer;
import com.hbm.ntm.client.render.AirIntakeRenderer;
import com.hbm.ntm.client.render.ArcWelderRenderer;
import com.hbm.ntm.client.render.ArcFurnaceRenderer;
import com.hbm.ntm.client.render.AssemblyMachineRenderer;
import com.hbm.ntm.client.render.BatteryPackItemRenderer;
import com.hbm.ntm.client.render.BatterySocketItemRenderer;
import com.hbm.ntm.client.render.BatterySocketRenderer;
import com.hbm.ntm.client.render.BlastFurnaceRenderer;
import com.hbm.ntm.client.render.BreedingReactorItemRenderer;
import com.hbm.ntm.client.render.BreedingReactorRenderer;
import com.hbm.ntm.client.render.CombinationOvenRenderer;
import com.hbm.ntm.client.render.CombustionEngineItemRenderer;
import com.hbm.ntm.client.render.CombustionEngineRenderer;
import com.hbm.ntm.client.render.ChargeRenderer;
import com.hbm.ntm.client.render.ChlorineCloudRenderer;
import com.hbm.ntm.client.render.ChemicalPlantRenderer;
import com.hbm.ntm.client.render.CentrifugeRenderer;
import com.hbm.ntm.client.render.CogRenderer;
import com.hbm.ntm.client.render.CrucibleRenderer;
import com.hbm.ntm.client.render.CrackingTowerRenderer;
import com.hbm.ntm.client.render.DieselGeneratorRenderer;
import com.hbm.ntm.client.render.DfcComponentRenderer;
import com.hbm.ntm.client.render.DfcCoreRenderer;
import com.hbm.ntm.client.render.ElectricHeaterRenderer;
import com.hbm.ntm.client.render.FireboxRenderer;
import com.hbm.ntm.client.render.FensuItemRenderer;
import com.hbm.ntm.client.render.FensuRenderer;
import com.hbm.ntm.client.render.FluidBurnerRenderer;
import com.hbm.ntm.client.render.FluidStorageTankItemRenderer;
import com.hbm.ntm.client.render.FluidStorageTankRenderer;
import com.hbm.ntm.client.render.FractionTowerRenderer;
import com.hbm.ntm.client.render.FractionTowerSeparatorRenderer;
import com.hbm.ntm.client.render.FoundryMoldRenderer;
import com.hbm.ntm.client.render.FoundryStorageRenderer;
import com.hbm.ntm.client.render.DynamicSlagRenderer;
import com.hbm.ntm.client.render.HeatBoilerRenderer;
import com.hbm.ntm.client.render.HeatExchangerRenderer;
import com.hbm.ntm.client.render.HeatingOvenRenderer;
import com.hbm.ntm.client.render.IndustrialTurbineItemRenderer;
import com.hbm.ntm.client.render.IndustrialTurbineRenderer;
import com.hbm.ntm.client.render.GasTurbineItemRenderer;
import com.hbm.ntm.client.render.GasTurbineRenderer;
import com.hbm.ntm.client.render.MachinePressRenderer;
import com.hbm.ntm.client.render.MicrowaveRenderer;
import com.hbm.ntm.client.render.MovingConveyorItemRenderer;
import com.hbm.ntm.client.render.MovingConveyorPackageRenderer;
import com.hbm.ntm.client.render.LargeNukeRenderer;
import com.hbm.ntm.client.render.NukeManRenderer;
import com.hbm.ntm.client.render.BombMultiRenderer;
import com.hbm.ntm.client.render.LandmineRenderer;
import com.hbm.ntm.client.render.NukeN2Renderer;
import com.hbm.ntm.client.render.ShrapnelRenderer;
import com.hbm.ntm.client.render.NukeFleijaRenderer;
import com.hbm.ntm.client.render.NukeSoliniumRenderer;
import com.hbm.ntm.client.render.SoliniumCloudRenderer;
import com.hbm.ntm.client.render.NukePrototypeRenderer;
import com.hbm.ntm.client.render.NukeCustomRenderer;
import com.hbm.ntm.client.render.NukeFstbmbRenderer;
import com.hbm.ntm.client.render.OilDerrickRenderer;
import com.hbm.ntm.client.render.MushroomCloudRenderer;
import com.hbm.ntm.client.render.PrimedExplosiveRenderer;
import com.hbm.ntm.client.render.PumpItemRenderer;
import com.hbm.ntm.client.render.PumpRenderer;
import com.hbm.ntm.client.render.RefineryRenderer;
import com.hbm.ntm.client.render.RadGenItemRenderer;
import com.hbm.ntm.client.render.RadGenRenderer;
import com.hbm.ntm.client.render.ResearchReactorRenderer;
import com.hbm.ntm.client.render.StirlingRenderer;
import com.hbm.ntm.client.render.SteelFurnaceRenderer;
import com.hbm.ntm.client.render.StirlingItemRenderer;
import com.hbm.ntm.client.render.TurbofanItemRenderer;
import com.hbm.ntm.client.render.TurbofanRenderer;
import com.hbm.ntm.client.render.SawmillRenderer;
import com.hbm.ntm.client.render.SawmillItemRenderer;
import com.hbm.ntm.client.render.SteamEngineRenderer;
import com.hbm.ntm.client.render.SteamEngineItemRenderer;
import com.hbm.ntm.client.render.SawbladeRenderer;
import com.hbm.ntm.client.render.SawbladeItemRenderer;
import com.hbm.ntm.client.render.SolderingStationRenderer;
import com.hbm.ntm.client.render.WoodBurnerRenderer;
import com.hbm.ntm.client.render.ZirnoxRenderer;
import com.hbm.ntm.client.render.ZirnoxItemRenderer;
import com.hbm.ntm.client.render.ZirnoxDestroyedRenderer;
import com.hbm.ntm.client.screen.ArmorTableScreen;
import com.hbm.ntm.client.screen.AnvilScreen;
import com.hbm.ntm.client.screen.ArcWelderScreen;
import com.hbm.ntm.client.screen.ArcFurnaceScreen;
import com.hbm.ntm.client.screen.AssemblyMachineScreen;
import com.hbm.ntm.client.screen.AshpitScreen;
import com.hbm.ntm.client.screen.BatterySocketScreen;
import com.hbm.ntm.client.screen.BlastFurnaceScreen;
import com.hbm.ntm.client.screen.BreedingReactorScreen;
import com.hbm.ntm.client.screen.CombinationOvenScreen;
import com.hbm.ntm.client.screen.CombustionEngineScreen;
import com.hbm.ntm.client.screen.BrickFurnaceScreen;
import com.hbm.ntm.client.screen.ChemicalPlantScreen;
import com.hbm.ntm.client.screen.CentrifugeScreen;
import com.hbm.ntm.client.screen.ConveyorBoxerScreen;
import com.hbm.ntm.client.screen.CraneExtractorScreen;
import com.hbm.ntm.client.screen.CraneInserterScreen;
import com.hbm.ntm.client.screen.CrucibleScreen;
import com.hbm.ntm.client.screen.DieselGeneratorScreen;
import com.hbm.ntm.client.screen.DfcScreen;
import com.hbm.ntm.client.screen.ElectricFurnaceScreen;
import com.hbm.ntm.client.screen.FireboxScreen;
import com.hbm.ntm.client.screen.FensuScreen;
import com.hbm.ntm.client.screen.GasTurbineScreen;
import com.hbm.ntm.client.screen.FluidIdentifierScreen;
import com.hbm.ntm.client.screen.FluidBurnerScreen;
import com.hbm.ntm.client.screen.FluidStorageTankScreen;
import com.hbm.ntm.client.screen.HeatExchangerScreen;
import com.hbm.ntm.client.screen.MachinePressScreen;
import com.hbm.ntm.client.screen.MachineShredderScreen;
import com.hbm.ntm.client.screen.MicrowaveScreen;
import com.hbm.ntm.client.screen.LargeNukeScreen;
import com.hbm.ntm.client.screen.NukeManScreen;
import com.hbm.ntm.client.screen.BombMultiScreen;
import com.hbm.ntm.client.screen.NukeN2Screen;
import com.hbm.ntm.client.screen.NukeFleijaScreen;
import com.hbm.ntm.client.screen.NukeSoliniumScreen;
import com.hbm.ntm.client.screen.NukePrototypeScreen;
import com.hbm.ntm.client.screen.NukeCustomScreen;
import com.hbm.ntm.client.screen.NukeFstbmbScreen;
import com.hbm.ntm.client.screen.OilDerrickScreen;
import com.hbm.ntm.client.screen.RefineryScreen;
import com.hbm.ntm.client.screen.RadioTorchScreen;
import com.hbm.ntm.client.screen.RadGenScreen;
import com.hbm.ntm.client.screen.ResearchReactorScreen;
import com.hbm.ntm.client.screen.SolderingStationScreen;
import com.hbm.ntm.client.screen.SteelFurnaceScreen;
import com.hbm.ntm.client.screen.SteamTurbineScreen;
import com.hbm.ntm.client.screen.TurbofanScreen;
import com.hbm.ntm.client.screen.WoodBurnerScreen;
import com.hbm.ntm.client.screen.WasteDrumScreen;
import com.hbm.ntm.client.screen.ZirnoxScreen;
import com.hbm.ntm.client.sound.TurbofanSoundInstance;
import com.hbm.ntm.blockentity.TurbofanBlockEntity;
import com.hbm.ntm.block.FluidDuctBlock;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenus;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public final class ClientMachineRegistration {
    private ClientMachineRegistration() {
    }

    public static void register(IEventBus modEventBus) {
        TurbofanBlockEntity.installClientEffectTick(TurbofanSoundInstance::tick);
        ClientLookOverlay.register();
        ClientNuclearFlash.register();
        ClientArmorModEvents.register();
        ClientConveyorPreview.register();
        modEventBus.addListener(ClientMachineRegistration::registerScreens);
        modEventBus.addListener(ClientMachineRegistration::registerRenderers);
        modEventBus.addListener(ClientMachineRegistration::registerModels);
        modEventBus.addListener(ClientMachineRegistration::registerItemColors);
        modEventBus.addListener(ClientMachineRegistration::registerBlockColors);
        modEventBus.addListener(ClientMachineRegistration::registerExtensions);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.DFC_CORE.get(), DfcScreen::new);
        event.register(ModMenus.DFC_EMITTER.get(), DfcScreen::new);
        event.register(ModMenus.DFC_INJECTOR.get(), DfcScreen::new);
        event.register(ModMenus.DFC_RECEIVER.get(), DfcScreen::new);
        event.register(ModMenus.DFC_STABILIZER.get(), DfcScreen::new);
        event.register(ModMenus.NUKE_MAN.get(), NukeManScreen::new);
        event.register(ModMenus.BOMB_MULTI.get(), BombMultiScreen::new);
        event.register(ModMenus.NUKE_N2.get(), NukeN2Screen::new);
        event.register(ModMenus.NUKE_FLEIJA.get(), NukeFleijaScreen::new);
        event.register(ModMenus.NUKE_SOLINIUM.get(), NukeSoliniumScreen::new);
        event.register(ModMenus.NUKE_PROTOTYPE.get(), NukePrototypeScreen::new);
        event.register(ModMenus.NUKE_CUSTOM.get(), NukeCustomScreen::new);
        event.register(ModMenus.NUKE_FSTBMB.get(), NukeFstbmbScreen::new);
        event.register(ModMenus.LARGE_NUKE.get(), LargeNukeScreen::new);
        event.register(ModMenus.BATTERY_SOCKET.get(), BatterySocketScreen::new);
        event.register(ModMenus.FENSU.get(), FensuScreen::new);
        event.register(ModMenus.MACHINE_PRESS.get(), MachinePressScreen::new);
        event.register(ModMenus.MACHINE_SHREDDER.get(), MachineShredderScreen::new);
        event.register(ModMenus.HEATER_FIREBOX.get(), FireboxScreen::new);
        event.register(ModMenus.HEATER_OILBURNER.get(), FluidBurnerScreen::new);
        event.register(ModMenus.HEATER_HEATEX.get(), HeatExchangerScreen::new);
        event.register(ModMenus.MACHINE_BLAST_FURNACE.get(), BlastFurnaceScreen::new);
        event.register(ModMenus.FURNACE_COMBINATION.get(), CombinationOvenScreen::new);
        event.register(ModMenus.MACHINE_FLUIDTANK.get(), FluidStorageTankScreen::new);
        event.register(ModMenus.FURNACE_STEEL.get(), SteelFurnaceScreen::new);
        event.register(ModMenus.MACHINE_ELECTRIC_FURNACE.get(), ElectricFurnaceScreen::new);
        event.register(ModMenus.MACHINE_FURNACE_BRICK.get(), BrickFurnaceScreen::new);
        event.register(ModMenus.MACHINE_WOOD_BURNER.get(), WoodBurnerScreen::new);
        event.register(ModMenus.MACHINE_MICROWAVE.get(), MicrowaveScreen::new);
        event.register(ModMenus.MACHINE_ASHPIT.get(), AshpitScreen::new);
        event.register(ModMenus.ARMOR_TABLE.get(), ArmorTableScreen::new);
        event.register(ModMenus.ANVIL.get(), AnvilScreen::new);
        event.register(ModMenus.MACHINE_ASSEMBLY_MACHINE.get(), AssemblyMachineScreen::new);
        event.register(ModMenus.MACHINE_CHEMICAL_PLANT.get(), ChemicalPlantScreen::new);
        event.register(ModMenus.MACHINE_SOLDERING_STATION.get(), SolderingStationScreen::new);
        event.register(ModMenus.MACHINE_ARC_WELDER.get(), ArcWelderScreen::new);
        event.register(ModMenus.MACHINE_ARC_FURNACE.get(), ArcFurnaceScreen::new);
        event.register(ModMenus.MACHINE_REFINERY.get(), RefineryScreen::new);
        event.register(ModMenus.MACHINE_CENTRIFUGE.get(), CentrifugeScreen::new);
        event.register(ModMenus.MACHINE_CRUCIBLE.get(), CrucibleScreen::new);
        event.register(ModMenus.FLUID_IDENTIFIER.get(), FluidIdentifierScreen::new);
        event.register(ModMenus.MACHINE_WELL.get(), OilDerrickScreen::new);
        event.register(ModMenus.MACHINE_DIESEL.get(), DieselGeneratorScreen::new);
        event.register(ModMenus.MACHINE_TURBINE_GAS.get(), GasTurbineScreen::new);
        event.register(ModMenus.MACHINE_COMBUSTION_ENGINE.get(), CombustionEngineScreen::new);
        event.register(ModMenus.MACHINE_TURBINE.get(), SteamTurbineScreen::new);
        event.register(ModMenus.MACHINE_TURBOFAN.get(), TurbofanScreen::new);
        event.register(ModMenus.REACTOR_ZIRNOX.get(), ZirnoxScreen::new);
        event.register(ModMenus.MACHINE_REACTOR_BREEDING.get(), BreedingReactorScreen::new);
        event.register(ModMenus.REACTOR_RESEARCH.get(), ResearchReactorScreen::new);
        event.register(ModMenus.MACHINE_RADGEN.get(), RadGenScreen::new);
        event.register(ModMenus.MACHINE_WASTE_DRUM.get(), WasteDrumScreen::new);
        event.register(ModMenus.CRANE_BOXER.get(), ConveyorBoxerScreen::new);
        event.register(ModMenus.CRANE_EXTRACTOR.get(), CraneExtractorScreen::new);
        event.register(ModMenus.CRANE_INSERTER.get(), CraneInserterScreen::new);
        event.register(ModMenus.RADIO_TORCH.get(), RadioTorchScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DFC_CORE.get(), DfcCoreRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DFC_EMITTER.get(), DfcComponentRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DFC_INJECTOR.get(), DfcComponentRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DFC_RECEIVER.get(), DfcComponentRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DFC_STABILIZER.get(), DfcComponentRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_MAN.get(), NukeManRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BOMB_MULTI.get(), BombMultiRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_N2.get(), NukeN2Renderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LANDMINE.get(), LandmineRenderer::new);
        event.registerEntityRenderer(ModEntities.SHRAPNEL.get(), ShrapnelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_FLEIJA.get(), NukeFleijaRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_SOLINIUM.get(), NukeSoliniumRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_PROTOTYPE.get(), NukePrototypeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_CUSTOM.get(), NukeCustomRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUKE_FSTBMB.get(), NukeFstbmbRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LARGE_NUKE.get(), LargeNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BATTERY_SOCKET.get(), BatterySocketRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_BATTERY_REDD.get(), FensuRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHARGE.get(), ChargeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_PRESS.get(), MachinePressRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEATER_FIREBOX.get(), FireboxRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEATER_OVEN.get(), HeatingOvenRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_ASHPIT.get(), AshpitRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_STIRLING.get(), StirlingRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_SAWMILL.get(), SawmillRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_STEAM_ENGINE.get(), SteamEngineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REACTOR_ZIRNOX.get(), ZirnoxRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ZIRNOX_DESTROYED.get(), ZirnoxDestroyedRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_REACTOR_BREEDING.get(), BreedingReactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REACTOR_RESEARCH.get(), ResearchReactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_RADGEN.get(), RadGenRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_INDUSTRIAL_TURBINE.get(),
                IndustrialTurbineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_TURBINE_GAS.get(), GasTurbineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_COMBUSTION_ENGINE.get(), CombustionEngineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_TURBOFAN.get(), TurbofanRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PUMP.get(), PumpRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_INTAKE.get(), AirIntakeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_BOILER.get(), HeatBoilerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEATER_ELECTRIC.get(), ElectricHeaterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEATER_OILBURNER.get(), FluidBurnerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HEATER_HEATEX.get(), HeatExchangerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_BLAST_FURNACE.get(), BlastFurnaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FURNACE_COMBINATION.get(), CombinationOvenRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FURNACE_STEEL.get(), SteelFurnaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_WOOD_BURNER.get(), WoodBurnerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_MICROWAVE.get(), MicrowaveRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_WELL.get(), OilDerrickRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_DIESEL.get(), DieselGeneratorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_FLUIDTANK.get(), FluidStorageTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_ASSEMBLY_MACHINE.get(), AssemblyMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_CHEMICAL_PLANT.get(), ChemicalPlantRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_SOLDERING_STATION.get(), SolderingStationRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_ARC_WELDER.get(), ArcWelderRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_ARC_FURNACE.get(), ArcFurnaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_REFINERY.get(), RefineryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_CENTRIFUGE.get(), CentrifugeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_CATALYTIC_CRACKER.get(),
                CrackingTowerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_FRACTION_TOWER.get(),
                FractionTowerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FRACTION_SPACER.get(),
                FractionTowerSeparatorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_CRUCIBLE.get(), CrucibleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_MOLD.get(), FoundryMoldRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_CHANNEL.get(), FoundryStorageRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOUNDRY_TANK.get(), FoundryStorageRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DYNAMIC_SLAG.get(), DynamicSlagRenderer::new);
        event.registerEntityRenderer(ModEntities.COG.get(), CogRenderer::new);
        event.registerEntityRenderer(ModEntities.SAWBLADE.get(), SawbladeRenderer::new);
        event.registerEntityRenderer(ModEntities.PRIMED_EXPLOSIVE.get(), PrimedExplosiveRenderer::new);
        event.registerEntityRenderer(ModEntities.MOVING_CONVEYOR_ITEM.get(), MovingConveyorItemRenderer::new);
        event.registerEntityRenderer(ModEntities.MOVING_CONVEYOR_PACKAGE.get(), MovingConveyorPackageRenderer::new);
        event.registerEntityRenderer(ModEntities.NUCLEAR_EXPLOSION.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.MUSHROOM_CLOUD.get(), MushroomCloudRenderer::new);
        event.registerEntityRenderer(ModEntities.BALEFIRE.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.FALLOUT_RAIN.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.CHLORINE_CLOUD.get(), ChlorineCloudRenderer::new);
        event.registerEntityRenderer(ModEntities.SOLINIUM_EXPLOSION.get(),
                net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.SOLINIUM_CLOUD.get(), SoliniumCloudRenderer::new);
    }

    private static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(DfcComponentRenderer.EMITTER);
        event.register(DfcComponentRenderer.INJECTOR);
        event.register(DfcComponentRenderer.RECEIVER);
        event.register(DfcComponentRenderer.STABILIZER);
        event.register(NukeManRenderer.MODEL);
        event.register(BombMultiRenderer.MODEL);
        event.register(NukeN2Renderer.MODEL);
        LandmineRenderer.MODELS.forEach(event::register);
        event.register(NukeFleijaRenderer.MODEL);
        event.register(NukeSoliniumRenderer.MODEL);
        event.register(NukePrototypeRenderer.MODEL);
        event.register(NukeCustomRenderer.MODEL);
        event.register(NukeFstbmbRenderer.BODY);
        event.register(NukeFstbmbRenderer.BALEFIRE);
        LargeNukeRenderer.MODELS.values().forEach(event::register);
        event.register(LargeNukeRenderer.GADGET_WIRES);
        event.register(MachinePressRenderer.BODY_MODEL);
        event.register(MachinePressRenderer.HEAD_MODEL);
        event.register(StirlingRenderer.BASE);
        event.register(StirlingRenderer.COG);
        event.register(StirlingRenderer.COG_SMALL);
        event.register(StirlingRenderer.PISTON);
        event.register(SawmillRenderer.MAIN);
        event.register(SawmillRenderer.BLADE);
        event.register(SawmillRenderer.GEAR_LEFT);
        event.register(SawmillRenderer.GEAR_RIGHT);
        event.register(SteamEngineRenderer.BASE);
        event.register(SteamEngineRenderer.FLYWHEEL);
        event.register(SteamEngineRenderer.SHAFT);
        event.register(SteamEngineRenderer.TRANSMISSION);
        event.register(SteamEngineRenderer.PISTON);
        event.register(ZirnoxRenderer.MODEL);
        event.register(ZirnoxDestroyedRenderer.MODEL);
        event.register(BreedingReactorRenderer.MODEL);
        event.register(ResearchReactorRenderer.BASE);
        event.register(ResearchReactorRenderer.RODS);
        event.register(RadGenRenderer.BASE);
        event.register(RadGenRenderer.ROTOR);
        event.register(RadGenRenderer.GLASS);
        event.register(RadGenRenderer.LIGHT);
        event.register(IndustrialTurbineRenderer.BASE);
        event.register(IndustrialTurbineRenderer.GAUGE);
        event.register(IndustrialTurbineRenderer.FLYWHEEL);
        event.register(GasTurbineRenderer.MODEL);
        event.register(CombustionEngineRenderer.ENGINE);
        event.register(CombustionEngineRenderer.CANISTER);
        event.register(CombustionEngineRenderer.HATCH);
        event.register(CombustionEngineRenderer.ITEM);
        event.register(TurbofanRenderer.BODY);
        event.register(TurbofanRenderer.BLADES);
        event.register(TurbofanRenderer.AFTERBURNER_BACK);
        event.register(TurbofanRenderer.AFTERBURNER_HOT);
        event.register(PumpRenderer.STEAM_BASE);
        event.register(PumpRenderer.STEAM_ROTOR);
        event.register(PumpRenderer.STEAM_ARMS);
        event.register(PumpRenderer.STEAM_PISTON);
        event.register(PumpRenderer.ELECTRIC_BASE);
        event.register(PumpRenderer.ELECTRIC_ROTOR);
        event.register(PumpRenderer.ELECTRIC_ARMS);
        event.register(PumpRenderer.ELECTRIC_PISTON);
        event.register(AirIntakeRenderer.BASE);
        event.register(AirIntakeRenderer.FAN);
        event.register(HeatBoilerRenderer.NORMAL);
        event.register(HeatBoilerRenderer.BURST);
        event.register(ElectricHeaterRenderer.MODEL);
        event.register(FluidBurnerRenderer.MODEL);
        event.register(HeatExchangerRenderer.MODEL);
        event.register(BlastFurnaceRenderer.MODEL);
        event.register(CombinationOvenRenderer.MODEL);
        event.register(FluidStorageTankRenderer.FRAME);
        event.register(FluidStorageTankRenderer.DAMAGED_FRAME);
        event.register(FluidStorageTankRenderer.DAMAGED_INNER);
        FluidStorageTankRenderer.TANKS.values().forEach(event::register);
        FluidStorageTankRenderer.DAMAGED_TANKS.values().forEach(event::register);
        event.register(SteelFurnaceRenderer.MODEL);
        event.register(WoodBurnerRenderer.MODEL);
        event.register(MicrowaveRenderer.BODY);
        event.register(MicrowaveRenderer.PLATE);
        event.register(OilDerrickRenderer.MODEL);
        event.register(DieselGeneratorRenderer.GENERATOR);
        event.register(DieselGeneratorRenderer.ENGINE);
        event.register(FireboxRenderer.MAIN);
        event.register(FireboxRenderer.DOOR);
        event.register(FireboxRenderer.EMPTY);
        event.register(FireboxRenderer.BURNING);
        event.register(HeatingOvenRenderer.MAIN);
        event.register(HeatingOvenRenderer.DOOR);
        event.register(HeatingOvenRenderer.EMPTY);
        event.register(HeatingOvenRenderer.BURNING);
        event.register(AshpitRenderer.MAIN);
        AssemblyMachineRenderer.MODELS.values().forEach(event::register);
        ChemicalPlantRenderer.MODELS.values().forEach(event::register);
        event.register(SolderingStationRenderer.MODEL);
        event.register(ArcWelderRenderer.MODEL);
        event.register(ArcFurnaceRenderer.BODY);
        event.register(ArcFurnaceRenderer.COLD);
        event.register(ArcFurnaceRenderer.LID);
        for (var model : ArcFurnaceRenderer.RINGS) event.register(model);
        for (var model : ArcFurnaceRenderer.FRESH) event.register(model);
        for (var model : ArcFurnaceRenderer.HOT) event.register(model);
        for (var model : ArcFurnaceRenderer.SHORT) event.register(model);
        for (var model : ArcFurnaceRenderer.CABLES) event.register(model);
        event.register(RefineryRenderer.MODEL);
        event.register(CentrifugeRenderer.MODEL);
        event.register(CrackingTowerRenderer.MODEL);
        event.register(FractionTowerRenderer.MODEL);
        event.register(FractionTowerSeparatorRenderer.MODEL);
        event.register(CrucibleRenderer.MODEL);
        event.register(AshpitRenderer.DOOR);
        event.register(AshpitRenderer.EMPTY);
        event.register(AshpitRenderer.FULL);
        event.register(BatterySocketRenderer.SOCKET);
        event.register(BatterySocketRenderer.SUPPORTS);
        event.register(BatterySocketRenderer.HORSE);
        BatterySocketRenderer.BATTERY_MODELS.values().forEach(event::register);
        event.register(FensuRenderer.BASE);
        event.register(FensuRenderer.WHEEL);
        event.register(FensuRenderer.LIGHTS);
        event.register(FensuRenderer.PLASMA);
        event.register(FensuRenderer.PLASMA_SPARKLE);
        event.register(MovingConveyorPackageRenderer.MODEL);
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            var material = com.hbm.ntm.item.FoundryIngotItem.material(stack);
            return tintIndex == 0 && material != null ? opaque(material.moltenColor()) : -1;
        }, ModItems.INGOT_RAW.get());
        event.register((stack, tintIndex) -> {
            var contents = com.hbm.ntm.item.FoundryScrapsItem.contents(stack);
            return tintIndex == 0 && contents != null
                    && contents.material() == com.hbm.ntm.foundry.FoundryMaterial.TECHNETIUM_STEEL
                    ? opaque(contents.material().moltenColor()) : -1;
        }, ModItems.SCRAPS.get());
        event.register((stack, tintIndex) -> {
            var material = com.hbm.ntm.item.DenseWireItem.material(stack);
            return tintIndex == 0 && material != null ? opaque(material.moltenColor()) : -1;
        }, ModItems.WIRE_DENSE.get());
        event.register((stack, tintIndex) -> com.hbm.ntm.item.PipeItem.isDuraSteel(stack) && tintIndex == 0
                        ? opaque(com.hbm.ntm.foundry.FoundryMaterial.DURA_STEEL.moltenColor()) : -1,
                ModItems.PIPE.get());
        ModItems.FOUNDRY_PARTS.values().forEach(part -> event.register((stack, tintIndex) -> {
            var material = com.hbm.ntm.item.FoundryPartItem.material(stack);
            return tintIndex == 0 && material != null ? opaque(material.moltenColor()) : -1;
        }, part.get()));
        event.register((stack, tintIndex) -> tintIndex == 1
                        ? opaque(com.hbm.ntm.item.FluidIdentifierItem.primary(stack).color()) : -1,
                ModItems.FLUID_IDENTIFIER_MULTI.get());
        event.register((stack, tintIndex) -> tintIndex == 1
                        ? opaque(com.hbm.ntm.item.UniversalFluidTankItem.fluid(stack).color()) : -1,
                ModItems.FLUID_TANK_FULL.get());
        event.register((stack, tintIndex) -> tintIndex == 1
                        ? opaque(com.hbm.ntm.item.FluidDuctItem.selection(stack).color()) : -1,
                ModItems.FLUID_DUCT.get());
        // RenderTestPipe tinted the untyped block-item overlay with Fluids.NONE (0x888888).
        event.register((stack, tintIndex) -> tintIndex == 1 ? opaque(0x888888) : -1,
                ModItems.FLUID_DUCT_NEO_ITEM.get());
        event.register((stack, tintIndex) -> tintIndex == 1
                        ? opaque(com.hbm.ntm.item.SourceFluidContainerItem.fluid(stack).containerColor()) : -1,
                ModItems.CANISTER_FULL.get());
        event.register((stack, tintIndex) -> tintIndex == 1
                        ? opaque(com.hbm.ntm.item.SourceFluidContainerItem.fluid(stack).containerColor())
                        : tintIndex == 2
                        ? opaque(com.hbm.ntm.item.SourceFluidContainerItem.fluid(stack).labelColor()) : -1,
                ModItems.GAS_FULL.get());
    }

    /** ItemColor wants ARGB; fluid definitions contain only the lower 24 RGB bits. */
    private static int opaque(int rgb) {
        return 0xFF000000 | rgb & 0x00FFFFFF;
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> tintIndex == 1
                        ? state.getValue(FluidDuctBlock.TYPE).color() : 0xFFFFFF,
                ModBlocks.FLUID_DUCT_NEO.get());
        // Source Balefire.colorMultiplier: Color.HSBtoRGB(0, 0, 1 - meta / 30F) whitens fire by age.
        event.register((state, level, pos, tintIndex) -> java.awt.Color.HSBtoRGB(0.0F, 0.0F,
                        1.0F - state.getValue(net.minecraft.world.level.block.FireBlock.AGE) / 30.0F),
                ModBlocks.BALEFIRE.get());
    }

    private static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private final BatteryPackItemRenderer renderer = new BatteryPackItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ModItems.BATTERY_PACK.get());
        event.registerItem(new IClientItemExtensions() {
            private final BatterySocketItemRenderer renderer = new BatterySocketItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ModItems.MACHINE_BATTERY_SOCKET_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final FensuItemRenderer renderer = new FensuItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ModItems.MACHINE_BATTERY_REDD_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final StirlingItemRenderer renderer = new StirlingItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ModItems.MACHINE_STIRLING_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final SawmillItemRenderer renderer = new SawmillItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ModItems.MACHINE_SAWMILL_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final SteamEngineItemRenderer renderer = new SteamEngineItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ModItems.MACHINE_STEAM_ENGINE_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final ZirnoxItemRenderer renderer = new ZirnoxItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.REACTOR_ZIRNOX_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final BreedingReactorItemRenderer renderer = new BreedingReactorItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.MACHINE_REACTOR_BREEDING_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final RadGenItemRenderer renderer = new RadGenItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.MACHINE_RADGEN_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final IndustrialTurbineItemRenderer renderer = new IndustrialTurbineItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.MACHINE_INDUSTRIAL_TURBINE_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final GasTurbineItemRenderer renderer = new GasTurbineItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.MACHINE_TURBINE_GAS_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final CombustionEngineItemRenderer renderer = new CombustionEngineItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.MACHINE_COMBUSTION_ENGINE_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final TurbofanItemRenderer renderer = new TurbofanItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.MACHINE_TURBOFAN_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final PumpItemRenderer renderer = new PumpItemRenderer(false);

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.PUMP_STEAM_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final PumpItemRenderer renderer = new PumpItemRenderer(true);

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.PUMP_ELECTRIC_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final AirIntakeItemRenderer renderer = new AirIntakeItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.MACHINE_INTAKE_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final FluidStorageTankItemRenderer renderer = new FluidStorageTankItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
        }, ModItems.MACHINE_FLUIDTANK_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            private final SawbladeItemRenderer renderer = new SawbladeItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ModItems.SAWBLADE.get());
    }
}
