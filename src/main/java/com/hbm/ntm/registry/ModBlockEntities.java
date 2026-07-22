package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.ConverterFEtoHEBlock;
import com.hbm.ntm.blockentity.AshpitBlockEntity;
import com.hbm.ntm.blockentity.AmmoPressBlockEntity;
import com.hbm.ntm.blockentity.AmmoPressProxyBlockEntity;
import com.hbm.ntm.blockentity.AirIntakeBlockEntity;
import com.hbm.ntm.blockentity.AirIntakeProxyBlockEntity;
import com.hbm.ntm.blockentity.ArcWelderBlockEntity;
import com.hbm.ntm.blockentity.ArcWelderProxyBlockEntity;
import com.hbm.ntm.blockentity.ArcFurnaceBlockEntity;
import com.hbm.ntm.blockentity.ArcFurnaceProxyBlockEntity;
import com.hbm.ntm.blockentity.AssemblyMachineBlockEntity;
import com.hbm.ntm.blockentity.AssemblyMachineProxyBlockEntity;
import com.hbm.ntm.blockentity.BatterySocketBlockEntity;
import com.hbm.ntm.blockentity.BatterySocketProxyBlockEntity;
import com.hbm.ntm.blockentity.BlastFurnaceBlockEntity;
import com.hbm.ntm.blockentity.BlastFurnaceProxyBlockEntity;
import com.hbm.ntm.blockentity.BrickFurnaceBlockEntity;
import com.hbm.ntm.blockentity.BreedingReactorBlockEntity;
import com.hbm.ntm.blockentity.BreedingReactorProxyBlockEntity;
import com.hbm.ntm.blockentity.ChargeBlockEntity;
import com.hbm.ntm.blockentity.ChlorineSealBlockEntity;
import com.hbm.ntm.blockentity.ChlorineVentBlockEntity;
import com.hbm.ntm.blockentity.ChemicalPlantBlockEntity;
import com.hbm.ntm.blockentity.ChemicalPlantProxyBlockEntity;
import com.hbm.ntm.blockentity.CombinationOvenBlockEntity;
import com.hbm.ntm.blockentity.CombinationOvenProxyBlockEntity;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.blockentity.CombustionEngineProxyBlockEntity;
import com.hbm.ntm.blockentity.CentrifugeBlockEntity;
import com.hbm.ntm.blockentity.CentrifugeProxyBlockEntity;
import com.hbm.ntm.blockentity.ConveyorBoxerBlockEntity;
import com.hbm.ntm.blockentity.CraneExtractorBlockEntity;
import com.hbm.ntm.blockentity.CraneInserterBlockEntity;
import com.hbm.ntm.blockentity.CrackingTowerBlockEntity;
import com.hbm.ntm.blockentity.CrackingTowerProxyBlockEntity;
import com.hbm.ntm.blockentity.CrucibleBlockEntity;
import com.hbm.ntm.blockentity.CrucibleProxyBlockEntity;
import com.hbm.ntm.blockentity.DieselGeneratorBlockEntity;
import com.hbm.ntm.blockentity.DfcCoreBlockEntity;
import com.hbm.ntm.blockentity.DfcEmitterBlockEntity;
import com.hbm.ntm.blockentity.DfcInjectorBlockEntity;
import com.hbm.ntm.blockentity.DfcReceiverBlockEntity;
import com.hbm.ntm.blockentity.DfcStabilizerBlockEntity;
import com.hbm.ntm.blockentity.ElectricHeaterBlockEntity;
import com.hbm.ntm.blockentity.ElectricHeaterProxyBlockEntity;
import com.hbm.ntm.blockentity.ElectricFurnaceBlockEntity;
import com.hbm.ntm.blockentity.FireboxBlockEntity;
import com.hbm.ntm.blockentity.FensuBlockEntity;
import com.hbm.ntm.blockentity.FensuProxyBlockEntity;
import com.hbm.ntm.blockentity.FluidDuctBlockEntity;
import com.hbm.ntm.blockentity.FluidStorageTankBlockEntity;
import com.hbm.ntm.blockentity.FluidStorageTankProxyBlockEntity;
import com.hbm.ntm.blockentity.FluidBurnerBlockEntity;
import com.hbm.ntm.blockentity.FluidBurnerProxyBlockEntity;
import com.hbm.ntm.blockentity.FractionTowerBlockEntity;
import com.hbm.ntm.blockentity.FractionTowerProxyBlockEntity;
import com.hbm.ntm.blockentity.FractionTowerSeparatorBlockEntity;
import com.hbm.ntm.blockentity.FoundryMoldBlockEntity;
import com.hbm.ntm.blockentity.FoundryChannelBlockEntity;
import com.hbm.ntm.blockentity.FoundryTankBlockEntity;
import com.hbm.ntm.blockentity.FoundryOutletBlockEntity;
import com.hbm.ntm.blockentity.DynamicSlagBlockEntity;
import com.hbm.ntm.blockentity.GeigerCounterBlockEntity;
import com.hbm.ntm.blockentity.HeCableBlockEntity;
import com.hbm.ntm.blockentity.HeatBoilerBlockEntity;
import com.hbm.ntm.blockentity.HeatBoilerProxyBlockEntity;
import com.hbm.ntm.blockentity.HeatExchangerBlockEntity;
import com.hbm.ntm.blockentity.HeatExchangerProxyBlockEntity;
import com.hbm.ntm.blockentity.HighPowerCondenserBlockEntity;
import com.hbm.ntm.blockentity.HighPowerCondenserProxyBlockEntity;
import com.hbm.ntm.blockentity.IndustrialTurbineBlockEntity;
import com.hbm.ntm.blockentity.GasTurbineBlockEntity;
import com.hbm.ntm.blockentity.GasTurbineProxyBlockEntity;
import com.hbm.ntm.blockentity.IndustrialTurbineProxyBlockEntity;
import com.hbm.ntm.blockentity.TurbofanBlockEntity;
import com.hbm.ntm.blockentity.TurbofanProxyBlockEntity;
import com.hbm.ntm.blockentity.MachinePressBlockEntity;
import com.hbm.ntm.blockentity.MachineShredderBlockEntity;
import com.hbm.ntm.blockentity.MicrowaveBlockEntity;
import com.hbm.ntm.blockentity.ConverterHEtoFEBlockEntity;
import com.hbm.ntm.blockentity.ConverterFEtoHEBlockEntity;
import com.hbm.ntm.blockentity.LargeNukeBlockEntity;
import com.hbm.ntm.blockentity.NukeManBlockEntity;
import com.hbm.ntm.blockentity.BombMultiBlockEntity;
import com.hbm.ntm.blockentity.LandmineBlockEntity;
import com.hbm.ntm.blockentity.NukeN2BlockEntity;
import com.hbm.ntm.blockentity.NukeFleijaBlockEntity;
import com.hbm.ntm.blockentity.NukeSoliniumBlockEntity;
import com.hbm.ntm.blockentity.NukeCustomBlockEntity;
import com.hbm.ntm.blockentity.NukeBalefireBlockEntity;
import com.hbm.ntm.blockentity.NukePrototypeBlockEntity;
import com.hbm.ntm.blockentity.OilDerrickBlockEntity;
import com.hbm.ntm.blockentity.PumpBlockEntity;
import com.hbm.ntm.blockentity.PumpProxyBlockEntity;
import com.hbm.ntm.blockentity.RadGenBlockEntity;
import com.hbm.ntm.blockentity.RadGenProxyBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchBlockEntity;
import com.hbm.ntm.blockentity.RefineryBlockEntity;
import com.hbm.ntm.blockentity.RefineryProxyBlockEntity;
import com.hbm.ntm.blockentity.ResearchReactorBlockEntity;
import com.hbm.ntm.blockentity.ResearchReactorProxyBlockEntity;
import com.hbm.ntm.blockentity.StirlingBlockEntity;
import com.hbm.ntm.blockentity.SawmillBlockEntity;
import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.blockentity.SteamEngineProxyBlockEntity;
import com.hbm.ntm.blockentity.SirenBlockEntity;
import com.hbm.ntm.blockentity.SteamTurbineBlockEntity;
import com.hbm.ntm.blockentity.SteamCondenserBlockEntity;
import com.hbm.ntm.blockentity.SteelFurnaceBlockEntity;
import com.hbm.ntm.blockentity.SteelFurnaceProxyBlockEntity;
import com.hbm.ntm.blockentity.SolderingStationBlockEntity;
import com.hbm.ntm.blockentity.SolderingStationProxyBlockEntity;
import com.hbm.ntm.blockentity.ThermalProxyBlockEntity;
import com.hbm.ntm.blockentity.WoodBurnerBlockEntity;
import com.hbm.ntm.blockentity.WoodBurnerProxyBlockEntity;
import com.hbm.ntm.blockentity.WasteDrumBlockEntity;
import com.hbm.ntm.blockentity.ZirnoxBlockEntity;
import com.hbm.ntm.blockentity.ZirnoxProxyBlockEntity;
import com.hbm.ntm.blockentity.ZirnoxDestroyedBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, HbmNtm.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DfcCoreBlockEntity>> DFC_CORE =
            BLOCK_ENTITY_TYPES.register("dfc_core", () -> BlockEntityType.Builder.of(
                    DfcCoreBlockEntity::new, ModBlocks.DFC_CORE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DfcEmitterBlockEntity>> DFC_EMITTER =
            BLOCK_ENTITY_TYPES.register("dfc_emitter", () -> BlockEntityType.Builder.of(
                    DfcEmitterBlockEntity::new, ModBlocks.DFC_EMITTER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DfcInjectorBlockEntity>> DFC_INJECTOR =
            BLOCK_ENTITY_TYPES.register("dfc_injector", () -> BlockEntityType.Builder.of(
                    DfcInjectorBlockEntity::new, ModBlocks.DFC_INJECTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DfcReceiverBlockEntity>> DFC_RECEIVER =
            BLOCK_ENTITY_TYPES.register("dfc_receiver", () -> BlockEntityType.Builder.of(
                    DfcReceiverBlockEntity::new, ModBlocks.DFC_RECEIVER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DfcStabilizerBlockEntity>> DFC_STABILIZER =
            BLOCK_ENTITY_TYPES.register("dfc_stabilizer", () -> BlockEntityType.Builder.of(
                    DfcStabilizerBlockEntity::new, ModBlocks.DFC_STABILIZER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChlorineSealBlockEntity>> VENT_CHLORINE_SEAL =
            BLOCK_ENTITY_TYPES.register("vent_chlorine_seal", () -> BlockEntityType.Builder.of(
                    ChlorineSealBlockEntity::new, ModBlocks.VENT_CHLORINE_SEAL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChlorineVentBlockEntity>> VENT_CHLORINE =
            BLOCK_ENTITY_TYPES.register("vent_chlorine", () -> BlockEntityType.Builder.of(
                    ChlorineVentBlockEntity::new, ModBlocks.VENT_CHLORINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NukeManBlockEntity>> NUKE_MAN =
            BLOCK_ENTITY_TYPES.register("nuke_man",
                    () -> BlockEntityType.Builder.of(NukeManBlockEntity::new, ModBlocks.NUKE_MAN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BombMultiBlockEntity>> BOMB_MULTI =
            BLOCK_ENTITY_TYPES.register("bomb_multi",
                    () -> BlockEntityType.Builder.of(BombMultiBlockEntity::new, ModBlocks.BOMB_MULTI.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NukeN2BlockEntity>> NUKE_N2 =
            BLOCK_ENTITY_TYPES.register("nuke_n2",
                    () -> BlockEntityType.Builder.of(NukeN2BlockEntity::new, ModBlocks.NUKE_N2.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NukeFleijaBlockEntity>> NUKE_FLEIJA =
            BLOCK_ENTITY_TYPES.register("nuke_fleija",
                    () -> BlockEntityType.Builder.of(NukeFleijaBlockEntity::new, ModBlocks.NUKE_FLEIJA.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NukeSoliniumBlockEntity>> NUKE_SOLINIUM =
            BLOCK_ENTITY_TYPES.register("nuke_solinium",
                    () -> BlockEntityType.Builder.of(NukeSoliniumBlockEntity::new, ModBlocks.NUKE_SOLINIUM.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NukePrototypeBlockEntity>> NUKE_PROTOTYPE =
            BLOCK_ENTITY_TYPES.register("nuke_prototype",
                    () -> BlockEntityType.Builder.of(NukePrototypeBlockEntity::new, ModBlocks.NUKE_PROTOTYPE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NukeCustomBlockEntity>> NUKE_CUSTOM =
            BLOCK_ENTITY_TYPES.register("nuke_custom",
                    () -> BlockEntityType.Builder.of(NukeCustomBlockEntity::new, ModBlocks.NUKE_CUSTOM.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NukeBalefireBlockEntity>> NUKE_FSTBMB =
            BLOCK_ENTITY_TYPES.register("nuke_fstbmb",
                    () -> BlockEntityType.Builder.of(NukeBalefireBlockEntity::new, ModBlocks.NUKE_FSTBMB.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LargeNukeBlockEntity>> LARGE_NUKE =
            BLOCK_ENTITY_TYPES.register("large_nuke",
                    () -> BlockEntityType.Builder.of(LargeNukeBlockEntity::new,
                            ModBlocks.NUKE_GADGET.get(), ModBlocks.NUKE_BOY.get(),
                            ModBlocks.NUKE_MIKE.get(), ModBlocks.NUKE_TSAR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChargeBlockEntity>> CHARGE =
            BLOCK_ENTITY_TYPES.register(
                    "charge",
                    () -> BlockEntityType.Builder.of(ChargeBlockEntity::new,
                            ModBlocks.CHARGE_DYNAMITE.get(), ModBlocks.CHARGE_MINER.get(),
                            ModBlocks.CHARGE_C4.get(), ModBlocks.CHARGE_SEMTEX.get()).build(null)
            );

    // All five mines share one block-entity class. Variety is purely explosive.
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LandmineBlockEntity>> LANDMINE =
            BLOCK_ENTITY_TYPES.register(
                    "landmine",
                    () -> BlockEntityType.Builder.of(LandmineBlockEntity::new,
                            ModBlocks.MINE_AP.get(), ModBlocks.MINE_HE.get(),
                            ModBlocks.MINE_SHRAP.get(), ModBlocks.MINE_FAT.get(),
                            ModBlocks.MINE_NAVAL.get()).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeCableBlockEntity>> RED_CABLE =
            BLOCK_ENTITY_TYPES.register(
                    "red_cable",
                    () -> BlockEntityType.Builder.of(HeCableBlockEntity::new, ModBlocks.RED_CABLE.get()).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatterySocketBlockEntity>> BATTERY_SOCKET =
            BLOCK_ENTITY_TYPES.register(
                    "battery_socket",
                    () -> BlockEntityType.Builder.of(BatterySocketBlockEntity::new,
                            ModBlocks.MACHINE_BATTERY_SOCKET.get()).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatterySocketProxyBlockEntity>> BATTERY_SOCKET_PROXY =
            BLOCK_ENTITY_TYPES.register(
                    "battery_socket_proxy",
                    () -> BlockEntityType.Builder.of(BatterySocketProxyBlockEntity::new,
                            ModBlocks.MACHINE_BATTERY_SOCKET.get()).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FensuBlockEntity>> MACHINE_BATTERY_REDD =
            BLOCK_ENTITY_TYPES.register("machine_battery_redd",
                    () -> BlockEntityType.Builder.of(FensuBlockEntity::new,
                            ModBlocks.MACHINE_BATTERY_REDD.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FensuProxyBlockEntity>> MACHINE_BATTERY_REDD_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_battery_redd_proxy",
                    () -> BlockEntityType.Builder.of(FensuProxyBlockEntity::new,
                            ModBlocks.MACHINE_BATTERY_REDD.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachinePressBlockEntity>> MACHINE_PRESS =
            BLOCK_ENTITY_TYPES.register(
                    "machine_press",
                    () -> BlockEntityType.Builder.of(MachinePressBlockEntity::new, ModBlocks.MACHINE_PRESS.get()).build(null)
            );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AmmoPressBlockEntity>> AMMO_PRESS =
            BLOCK_ENTITY_TYPES.register("machine_ammo_press",
                    () -> BlockEntityType.Builder.of(AmmoPressBlockEntity::new, ModBlocks.AMMO_PRESS.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AmmoPressProxyBlockEntity>> AMMO_PRESS_PROXY =
            BLOCK_ENTITY_TYPES.register("ammo_press_proxy",
                    () -> BlockEntityType.Builder.of(AmmoPressProxyBlockEntity::new, ModBlocks.AMMO_PRESS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineShredderBlockEntity>> MACHINE_SHREDDER =
            BLOCK_ENTITY_TYPES.register(
                    "machine_shredder",
                    () -> BlockEntityType.Builder.of(MachineShredderBlockEntity::new, ModBlocks.MACHINE_SHREDDER.get()).build(null)
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FireboxBlockEntity>> HEATER_FIREBOX =
            BLOCK_ENTITY_TYPES.register("heater_firebox",
                    () -> BlockEntityType.Builder.of(FireboxBlockEntity::new, ModBlocks.HEATER_FIREBOX.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FireboxBlockEntity>> HEATER_OVEN =
            BLOCK_ENTITY_TYPES.register("heater_oven",
                    () -> BlockEntityType.Builder.of(FireboxBlockEntity::new, ModBlocks.HEATER_OVEN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AshpitBlockEntity>> MACHINE_ASHPIT =
            BLOCK_ENTITY_TYPES.register("machine_ashpit",
                    () -> BlockEntityType.Builder.of(AshpitBlockEntity::new, ModBlocks.MACHINE_ASHPIT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StirlingBlockEntity>> MACHINE_STIRLING =
            BLOCK_ENTITY_TYPES.register("machine_stirling",
                    () -> BlockEntityType.Builder.of(StirlingBlockEntity::new, ModBlocks.MACHINE_STIRLING.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SawmillBlockEntity>> MACHINE_SAWMILL =
            BLOCK_ENTITY_TYPES.register("machine_sawmill",
                    () -> BlockEntityType.Builder.of(SawmillBlockEntity::new, ModBlocks.MACHINE_SAWMILL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineBlockEntity>> MACHINE_STEAM_ENGINE =
            BLOCK_ENTITY_TYPES.register("machine_steam_engine",
                    () -> BlockEntityType.Builder.of(SteamEngineBlockEntity::new,
                            ModBlocks.MACHINE_STEAM_ENGINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineProxyBlockEntity>> MACHINE_STEAM_ENGINE_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_steam_engine_proxy",
                    () -> BlockEntityType.Builder.of(SteamEngineProxyBlockEntity::new,
                            ModBlocks.MACHINE_STEAM_ENGINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IndustrialTurbineBlockEntity>> MACHINE_INDUSTRIAL_TURBINE =
            BLOCK_ENTITY_TYPES.register("machine_industrial_turbine",
                    () -> BlockEntityType.Builder.of(IndustrialTurbineBlockEntity::new,
                            ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IndustrialTurbineProxyBlockEntity>> MACHINE_INDUSTRIAL_TURBINE_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_industrial_turbine_proxy",
                    () -> BlockEntityType.Builder.of(IndustrialTurbineProxyBlockEntity::new,
                            ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GasTurbineBlockEntity>> MACHINE_TURBINE_GAS =
            BLOCK_ENTITY_TYPES.register("machine_turbinegas",
                    () -> BlockEntityType.Builder.of(GasTurbineBlockEntity::new,
                            ModBlocks.MACHINE_TURBINE_GAS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GasTurbineProxyBlockEntity>> MACHINE_TURBINE_GAS_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_turbinegas_proxy",
                    () -> BlockEntityType.Builder.of(GasTurbineProxyBlockEntity::new,
                            ModBlocks.MACHINE_TURBINE_GAS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TurbofanBlockEntity>> MACHINE_TURBOFAN =
            BLOCK_ENTITY_TYPES.register("machine_turbofan",
                    () -> BlockEntityType.Builder.of(TurbofanBlockEntity::new,
                            ModBlocks.MACHINE_TURBOFAN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TurbofanProxyBlockEntity>> MACHINE_TURBOFAN_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_turbofan_proxy",
                    () -> BlockEntityType.Builder.of(TurbofanProxyBlockEntity::new,
                            ModBlocks.MACHINE_TURBOFAN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamTurbineBlockEntity>> MACHINE_TURBINE =
            BLOCK_ENTITY_TYPES.register("machine_turbine",
                    () -> BlockEntityType.Builder.of(SteamTurbineBlockEntity::new,
                            ModBlocks.MACHINE_TURBINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ZirnoxBlockEntity>> REACTOR_ZIRNOX =
            BLOCK_ENTITY_TYPES.register("reactor_zirnox",
                    () -> BlockEntityType.Builder.of(ZirnoxBlockEntity::new,
                            ModBlocks.REACTOR_ZIRNOX.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ZirnoxProxyBlockEntity>> REACTOR_ZIRNOX_PROXY =
            BLOCK_ENTITY_TYPES.register("reactor_zirnox_proxy",
                    () -> BlockEntityType.Builder.of(ZirnoxProxyBlockEntity::new,
                            ModBlocks.REACTOR_ZIRNOX.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ZirnoxDestroyedBlockEntity>> ZIRNOX_DESTROYED =
            BLOCK_ENTITY_TYPES.register("zirnox_destroyed",
                    () -> BlockEntityType.Builder.of(ZirnoxDestroyedBlockEntity::new,
                            ModBlocks.ZIRNOX_DESTROYED.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreedingReactorBlockEntity>> MACHINE_REACTOR_BREEDING =
            BLOCK_ENTITY_TYPES.register("machine_reactor_breeding",
                    () -> BlockEntityType.Builder.of(BreedingReactorBlockEntity::new,
                            ModBlocks.MACHINE_REACTOR_BREEDING.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreedingReactorProxyBlockEntity>> MACHINE_REACTOR_BREEDING_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_reactor_breeding_proxy",
                    () -> BlockEntityType.Builder.of(BreedingReactorProxyBlockEntity::new,
                            ModBlocks.MACHINE_REACTOR_BREEDING.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResearchReactorBlockEntity>> REACTOR_RESEARCH =
            BLOCK_ENTITY_TYPES.register("reactor_research",
                    () -> BlockEntityType.Builder.of(ResearchReactorBlockEntity::new,
                            ModBlocks.REACTOR_RESEARCH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ResearchReactorProxyBlockEntity>> REACTOR_RESEARCH_PROXY =
            BLOCK_ENTITY_TYPES.register("reactor_research_proxy",
                    () -> BlockEntityType.Builder.of(ResearchReactorProxyBlockEntity::new,
                            ModBlocks.REACTOR_RESEARCH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadGenBlockEntity>> MACHINE_RADGEN =
            BLOCK_ENTITY_TYPES.register("machine_radgen",
                    () -> BlockEntityType.Builder.of(RadGenBlockEntity::new,
                            ModBlocks.MACHINE_RADGEN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadGenProxyBlockEntity>> MACHINE_RADGEN_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_radgen_proxy",
                    () -> BlockEntityType.Builder.of(RadGenProxyBlockEntity::new,
                            ModBlocks.MACHINE_RADGEN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WasteDrumBlockEntity>> MACHINE_WASTE_DRUM =
            BLOCK_ENTITY_TYPES.register("machine_waste_drum",
                    () -> BlockEntityType.Builder.of(WasteDrumBlockEntity::new,
                            ModBlocks.MACHINE_WASTE_DRUM.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SirenBlockEntity>> MACHINE_SIREN =
            BLOCK_ENTITY_TYPES.register("machine_siren",
                    () -> BlockEntityType.Builder.of(SirenBlockEntity::new,
                            ModBlocks.MACHINE_SIREN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PumpBlockEntity>> PUMP =
            BLOCK_ENTITY_TYPES.register("pump",
                    () -> BlockEntityType.Builder.of(PumpBlockEntity::new,
                            ModBlocks.PUMP_STEAM.get(), ModBlocks.PUMP_ELECTRIC.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PumpProxyBlockEntity>> PUMP_PROXY =
            BLOCK_ENTITY_TYPES.register("pump_proxy",
                    () -> BlockEntityType.Builder.of(PumpProxyBlockEntity::new,
                            ModBlocks.PUMP_STEAM.get(), ModBlocks.PUMP_ELECTRIC.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AirIntakeBlockEntity>> MACHINE_INTAKE =
            BLOCK_ENTITY_TYPES.register("machine_intake",
                    () -> BlockEntityType.Builder.of(AirIntakeBlockEntity::new,
                            ModBlocks.MACHINE_INTAKE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AirIntakeProxyBlockEntity>> MACHINE_INTAKE_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_intake_proxy",
                    () -> BlockEntityType.Builder.of(AirIntakeProxyBlockEntity::new,
                            ModBlocks.MACHINE_INTAKE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamCondenserBlockEntity>> MACHINE_CONDENSER =
            BLOCK_ENTITY_TYPES.register("machine_condenser",
                    () -> BlockEntityType.Builder.of(SteamCondenserBlockEntity::new,
                            ModBlocks.MACHINE_CONDENSER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HighPowerCondenserBlockEntity>> MACHINE_CONDENSER_POWERED =
            BLOCK_ENTITY_TYPES.register("machine_condenser_powered",
                    () -> BlockEntityType.Builder.of(HighPowerCondenserBlockEntity::new,
                            ModBlocks.MACHINE_CONDENSER_POWERED.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HighPowerCondenserProxyBlockEntity>> MACHINE_CONDENSER_POWERED_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_condenser_powered_proxy",
                    () -> BlockEntityType.Builder.of(HighPowerCondenserProxyBlockEntity::new,
                            ModBlocks.MACHINE_CONDENSER_POWERED.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ThermalProxyBlockEntity>> THERMAL_PROXY =
            BLOCK_ENTITY_TYPES.register("thermal_proxy",
                    () -> BlockEntityType.Builder.of(ThermalProxyBlockEntity::new,
                            ModBlocks.HEATER_FIREBOX.get(), ModBlocks.HEATER_OVEN.get(), ModBlocks.MACHINE_ASHPIT.get(),
                            ModBlocks.MACHINE_STIRLING.get(), ModBlocks.MACHINE_SAWMILL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatBoilerBlockEntity>> MACHINE_BOILER =
            BLOCK_ENTITY_TYPES.register("machine_boiler",
                    () -> BlockEntityType.Builder.of(HeatBoilerBlockEntity::new,
                            ModBlocks.MACHINE_BOILER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatBoilerProxyBlockEntity>> MACHINE_BOILER_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_boiler_proxy",
                    () -> BlockEntityType.Builder.of(HeatBoilerProxyBlockEntity::new,
                            ModBlocks.MACHINE_BOILER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricHeaterBlockEntity>> HEATER_ELECTRIC =
            BLOCK_ENTITY_TYPES.register("heater_electric",
                    () -> BlockEntityType.Builder.of(ElectricHeaterBlockEntity::new,
                            ModBlocks.HEATER_ELECTRIC.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricHeaterProxyBlockEntity>> HEATER_ELECTRIC_PROXY =
            BLOCK_ENTITY_TYPES.register("heater_electric_proxy",
                    () -> BlockEntityType.Builder.of(ElectricHeaterProxyBlockEntity::new,
                            ModBlocks.HEATER_ELECTRIC.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidBurnerBlockEntity>> HEATER_OILBURNER =
            BLOCK_ENTITY_TYPES.register("heater_oilburner",
                    () -> BlockEntityType.Builder.of(FluidBurnerBlockEntity::new,
                            ModBlocks.HEATER_OILBURNER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidBurnerProxyBlockEntity>> HEATER_OILBURNER_PROXY =
            BLOCK_ENTITY_TYPES.register("heater_oilburner_proxy",
                    () -> BlockEntityType.Builder.of(FluidBurnerProxyBlockEntity::new,
                            ModBlocks.HEATER_OILBURNER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatExchangerBlockEntity>> HEATER_HEATEX =
            BLOCK_ENTITY_TYPES.register("heater_heatex",
                    () -> BlockEntityType.Builder.of(HeatExchangerBlockEntity::new,
                            ModBlocks.HEATER_HEATEX.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatExchangerProxyBlockEntity>> HEATER_HEATEX_PROXY =
            BLOCK_ENTITY_TYPES.register("heater_heatex_proxy",
                    () -> BlockEntityType.Builder.of(HeatExchangerProxyBlockEntity::new,
                            ModBlocks.HEATER_HEATEX.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastFurnaceBlockEntity>> MACHINE_BLAST_FURNACE =
            BLOCK_ENTITY_TYPES.register("machine_blast_furnace",
                    () -> BlockEntityType.Builder.of(BlastFurnaceBlockEntity::new,
                            ModBlocks.MACHINE_BLAST_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastFurnaceProxyBlockEntity>> MACHINE_BLAST_FURNACE_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_blast_furnace_proxy",
                    () -> BlockEntityType.Builder.of(BlastFurnaceProxyBlockEntity::new,
                            ModBlocks.MACHINE_BLAST_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CombinationOvenBlockEntity>> FURNACE_COMBINATION =
            BLOCK_ENTITY_TYPES.register("furnace_combination",
                    () -> BlockEntityType.Builder.of(CombinationOvenBlockEntity::new,
                            ModBlocks.FURNACE_COMBINATION.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CombinationOvenProxyBlockEntity>> FURNACE_COMBINATION_PROXY =
            BLOCK_ENTITY_TYPES.register("furnace_combination_proxy",
                    () -> BlockEntityType.Builder.of(CombinationOvenProxyBlockEntity::new,
                            ModBlocks.FURNACE_COMBINATION.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteelFurnaceBlockEntity>> FURNACE_STEEL =
            BLOCK_ENTITY_TYPES.register("furnace_steel",
                    () -> BlockEntityType.Builder.of(SteelFurnaceBlockEntity::new,
                            ModBlocks.FURNACE_STEEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteelFurnaceProxyBlockEntity>> FURNACE_STEEL_PROXY =
            BLOCK_ENTITY_TYPES.register("furnace_steel_proxy",
                    () -> BlockEntityType.Builder.of(SteelFurnaceProxyBlockEntity::new,
                            ModBlocks.FURNACE_STEEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricFurnaceBlockEntity>> MACHINE_ELECTRIC_FURNACE =
            BLOCK_ENTITY_TYPES.register("machine_electric_furnace_off",
                    () -> BlockEntityType.Builder.of(ElectricFurnaceBlockEntity::new,
                            ModBlocks.MACHINE_ELECTRIC_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrickFurnaceBlockEntity>> MACHINE_FURNACE_BRICK =
            BLOCK_ENTITY_TYPES.register("machine_furnace_brick_off",
                    () -> BlockEntityType.Builder.of(BrickFurnaceBlockEntity::new,
                            ModBlocks.MACHINE_FURNACE_BRICK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodBurnerBlockEntity>> MACHINE_WOOD_BURNER =
            BLOCK_ENTITY_TYPES.register("machine_wood_burner",
                    () -> BlockEntityType.Builder.of(WoodBurnerBlockEntity::new,
                            ModBlocks.MACHINE_WOOD_BURNER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodBurnerProxyBlockEntity>> MACHINE_WOOD_BURNER_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_wood_burner_proxy",
                    () -> BlockEntityType.Builder.of(WoodBurnerProxyBlockEntity::new,
                            ModBlocks.MACHINE_WOOD_BURNER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MicrowaveBlockEntity>> MACHINE_MICROWAVE =
            BLOCK_ENTITY_TYPES.register("machine_microwave",
                    () -> BlockEntityType.Builder.of(MicrowaveBlockEntity::new,
                            ModBlocks.MACHINE_MICROWAVE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConverterHEtoFEBlockEntity>> MACHINE_CONVERTER_HE_FE =
            BLOCK_ENTITY_TYPES.register("machine_converter_he_fe",
                    () -> BlockEntityType.Builder.of(ConverterHEtoFEBlockEntity::new,
                            ModBlocks.MACHINE_CONVERTER_HE_FE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConverterFEtoHEBlockEntity>> MACHINE_CONVERTER_FE_HE =
            BLOCK_ENTITY_TYPES.register("machine_converter_fe_he",
                    () -> BlockEntityType.Builder.of(ConverterFEtoHEBlockEntity::new,
                            ModBlocks.MACHINE_CONVERTER_FE_HE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OilDerrickBlockEntity>> MACHINE_WELL =
            BLOCK_ENTITY_TYPES.register("machine_well",
                    () -> BlockEntityType.Builder.of(OilDerrickBlockEntity::new,
                            ModBlocks.MACHINE_WELL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DieselGeneratorBlockEntity>> MACHINE_DIESEL =
            BLOCK_ENTITY_TYPES.register("machine_diesel",
                    () -> BlockEntityType.Builder.of(DieselGeneratorBlockEntity::new,
                            ModBlocks.MACHINE_DIESEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CombustionEngineBlockEntity>> MACHINE_COMBUSTION_ENGINE =
            BLOCK_ENTITY_TYPES.register("machine_combustion_engine",
                    () -> BlockEntityType.Builder.of(CombustionEngineBlockEntity::new,
                            ModBlocks.MACHINE_COMBUSTION_ENGINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CombustionEngineProxyBlockEntity>> MACHINE_COMBUSTION_ENGINE_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_combustion_engine_proxy",
                    () -> BlockEntityType.Builder.of(CombustionEngineProxyBlockEntity::new,
                            ModBlocks.MACHINE_COMBUSTION_ENGINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidDuctBlockEntity>> FLUID_DUCT =
            BLOCK_ENTITY_TYPES.register("fluid_duct",
                    () -> BlockEntityType.Builder.of(FluidDuctBlockEntity::new,
                            ModBlocks.FLUID_DUCT_NEO.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidStorageTankBlockEntity>> MACHINE_FLUIDTANK =
            BLOCK_ENTITY_TYPES.register("machine_fluidtank",
                    () -> BlockEntityType.Builder.of(FluidStorageTankBlockEntity::new,
                            ModBlocks.MACHINE_FLUIDTANK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidStorageTankProxyBlockEntity>> MACHINE_FLUIDTANK_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_fluidtank_proxy",
                    () -> BlockEntityType.Builder.of(FluidStorageTankProxyBlockEntity::new,
                            ModBlocks.MACHINE_FLUIDTANK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AssemblyMachineBlockEntity>> MACHINE_ASSEMBLY_MACHINE =
            BLOCK_ENTITY_TYPES.register("machine_assembly_machine",
                    () -> BlockEntityType.Builder.of(AssemblyMachineBlockEntity::new,
                            ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AssemblyMachineProxyBlockEntity>> MACHINE_ASSEMBLY_MACHINE_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_assembly_machine_proxy",
                    () -> BlockEntityType.Builder.of(AssemblyMachineProxyBlockEntity::new,
                            ModBlocks.MACHINE_ASSEMBLY_MACHINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalPlantBlockEntity>> MACHINE_CHEMICAL_PLANT =
            BLOCK_ENTITY_TYPES.register("machine_chemical_plant",
                    () -> BlockEntityType.Builder.of(ChemicalPlantBlockEntity::new,
                            ModBlocks.MACHINE_CHEMICAL_PLANT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalPlantProxyBlockEntity>> MACHINE_CHEMICAL_PLANT_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_chemical_plant_proxy",
                    () -> BlockEntityType.Builder.of(ChemicalPlantProxyBlockEntity::new,
                            ModBlocks.MACHINE_CHEMICAL_PLANT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolderingStationBlockEntity>> MACHINE_SOLDERING_STATION =
            BLOCK_ENTITY_TYPES.register("machine_soldering_station",
                    () -> BlockEntityType.Builder.of(SolderingStationBlockEntity::new,
                            ModBlocks.MACHINE_SOLDERING_STATION.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolderingStationProxyBlockEntity>> MACHINE_SOLDERING_STATION_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_soldering_station_proxy",
                    () -> BlockEntityType.Builder.of(SolderingStationProxyBlockEntity::new,
                            ModBlocks.MACHINE_SOLDERING_STATION.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcWelderBlockEntity>> MACHINE_ARC_WELDER =
            BLOCK_ENTITY_TYPES.register("machine_arc_welder",
                    () -> BlockEntityType.Builder.of(ArcWelderBlockEntity::new,
                            ModBlocks.MACHINE_ARC_WELDER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcWelderProxyBlockEntity>> MACHINE_ARC_WELDER_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_arc_welder_proxy",
                    () -> BlockEntityType.Builder.of(ArcWelderProxyBlockEntity::new,
                            ModBlocks.MACHINE_ARC_WELDER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcFurnaceBlockEntity>> MACHINE_ARC_FURNACE =
            BLOCK_ENTITY_TYPES.register("machine_arc_furnace",
                    () -> BlockEntityType.Builder.of(ArcFurnaceBlockEntity::new,
                            ModBlocks.MACHINE_ARC_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArcFurnaceProxyBlockEntity>> MACHINE_ARC_FURNACE_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_arc_furnace_proxy",
                    () -> BlockEntityType.Builder.of(ArcFurnaceProxyBlockEntity::new,
                            ModBlocks.MACHINE_ARC_FURNACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RefineryBlockEntity>> MACHINE_REFINERY =
            BLOCK_ENTITY_TYPES.register("machine_refinery",
                    () -> BlockEntityType.Builder.of(RefineryBlockEntity::new,
                            ModBlocks.MACHINE_REFINERY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RefineryProxyBlockEntity>> MACHINE_REFINERY_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_refinery_proxy",
                    () -> BlockEntityType.Builder.of(RefineryProxyBlockEntity::new,
                            ModBlocks.MACHINE_REFINERY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CentrifugeBlockEntity>> MACHINE_CENTRIFUGE =
            BLOCK_ENTITY_TYPES.register("machine_centrifuge",
                    () -> BlockEntityType.Builder.of(CentrifugeBlockEntity::new,
                            ModBlocks.MACHINE_CENTRIFUGE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CentrifugeProxyBlockEntity>> MACHINE_CENTRIFUGE_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_centrifuge_proxy",
                    () -> BlockEntityType.Builder.of(CentrifugeProxyBlockEntity::new,
                            ModBlocks.MACHINE_CENTRIFUGE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrackingTowerBlockEntity>> MACHINE_CATALYTIC_CRACKER =
            BLOCK_ENTITY_TYPES.register("machine_catalytic_cracker",
                    () -> BlockEntityType.Builder.of(CrackingTowerBlockEntity::new,
                            ModBlocks.MACHINE_CATALYTIC_CRACKER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrackingTowerProxyBlockEntity>> MACHINE_CATALYTIC_CRACKER_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_catalytic_cracker_proxy",
                    () -> BlockEntityType.Builder.of(CrackingTowerProxyBlockEntity::new,
                            ModBlocks.MACHINE_CATALYTIC_CRACKER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FractionTowerBlockEntity>> MACHINE_FRACTION_TOWER =
            BLOCK_ENTITY_TYPES.register("machine_fraction_tower",
                    () -> BlockEntityType.Builder.of(FractionTowerBlockEntity::new,
                            ModBlocks.MACHINE_FRACTION_TOWER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FractionTowerProxyBlockEntity>> MACHINE_FRACTION_TOWER_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_fraction_tower_proxy",
                    () -> BlockEntityType.Builder.of(FractionTowerProxyBlockEntity::new,
                            ModBlocks.MACHINE_FRACTION_TOWER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FractionTowerSeparatorBlockEntity>> FRACTION_SPACER =
            BLOCK_ENTITY_TYPES.register("fraction_spacer",
                    () -> BlockEntityType.Builder.of(FractionTowerSeparatorBlockEntity::new,
                            ModBlocks.FRACTION_SPACER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleBlockEntity>> MACHINE_CRUCIBLE =
            BLOCK_ENTITY_TYPES.register("machine_crucible",
                    () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, ModBlocks.MACHINE_CRUCIBLE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrucibleProxyBlockEntity>> MACHINE_CRUCIBLE_PROXY =
            BLOCK_ENTITY_TYPES.register("machine_crucible_proxy",
                    () -> BlockEntityType.Builder.of(CrucibleProxyBlockEntity::new, ModBlocks.MACHINE_CRUCIBLE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryMoldBlockEntity>> FOUNDRY_MOLD =
            BLOCK_ENTITY_TYPES.register("foundry_mold",
                    () -> BlockEntityType.Builder.of(FoundryMoldBlockEntity::new,
                            ModBlocks.FOUNDRY_MOLD.get(), ModBlocks.FOUNDRY_BASIN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryChannelBlockEntity>> FOUNDRY_CHANNEL =
            BLOCK_ENTITY_TYPES.register("foundry_channel",
                    () -> BlockEntityType.Builder.of(FoundryChannelBlockEntity::new,
                            ModBlocks.FOUNDRY_CHANNEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryTankBlockEntity>> FOUNDRY_TANK =
            BLOCK_ENTITY_TYPES.register("foundry_tank",
                    () -> BlockEntityType.Builder.of(FoundryTankBlockEntity::new,
                            ModBlocks.FOUNDRY_TANK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FoundryOutletBlockEntity>> FOUNDRY_OUTLET =
            BLOCK_ENTITY_TYPES.register("foundry_outlet",
                    () -> BlockEntityType.Builder.of(FoundryOutletBlockEntity::new,
                            ModBlocks.FOUNDRY_OUTLET.get(), ModBlocks.FOUNDRY_SLAGTAP.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DynamicSlagBlockEntity>> DYNAMIC_SLAG =
            BLOCK_ENTITY_TYPES.register("slag",
                    () -> BlockEntityType.Builder.of(DynamicSlagBlockEntity::new,
                            ModBlocks.DYNAMIC_SLAG.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeigerCounterBlockEntity>> GEIGER =
            BLOCK_ENTITY_TYPES.register("geiger",
                    () -> BlockEntityType.Builder.of(GeigerCounterBlockEntity::new, ModBlocks.GEIGER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RadioTorchBlockEntity>> RADIO_TORCH =
            BLOCK_ENTITY_TYPES.register("radio_torch", () -> BlockEntityType.Builder.of(RadioTorchBlockEntity::new,
                    ModBlocks.RADIO_TORCH_SENDER.get(), ModBlocks.RADIO_TORCH_RECEIVER.get(),
                    ModBlocks.RADIO_TORCH_COUNTER.get(), ModBlocks.RADIO_TORCH_LOGIC.get(),
                    ModBlocks.RADIO_TORCH_READER.get(), ModBlocks.RADIO_TORCH_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConveyorBoxerBlockEntity>> CRANE_BOXER =
            BLOCK_ENTITY_TYPES.register("crane_boxer",
                    () -> BlockEntityType.Builder.of(ConveyorBoxerBlockEntity::new,
                            ModBlocks.CRANE_BOXER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraneExtractorBlockEntity>> CRANE_EXTRACTOR =
            BLOCK_ENTITY_TYPES.register("crane_extractor",
                    () -> BlockEntityType.Builder.of(CraneExtractorBlockEntity::new,
                            ModBlocks.CRANE_EXTRACTOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CraneInserterBlockEntity>> CRANE_INSERTER =
            BLOCK_ENTITY_TYPES.register("crane_inserter",
                    () -> BlockEntityType.Builder.of(CraneInserterBlockEntity::new,
                            ModBlocks.CRANE_INSERTER.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
