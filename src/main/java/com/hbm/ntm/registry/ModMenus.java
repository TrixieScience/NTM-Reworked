package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.inventory.ArmorTableMenu;
import com.hbm.ntm.inventory.ArcWelderMenu;
import com.hbm.ntm.inventory.ArcFurnaceMenu;
import com.hbm.ntm.inventory.AnvilMenu;
import com.hbm.ntm.inventory.AssemblyMachineMenu;
import com.hbm.ntm.inventory.SolderingStationMenu;
import com.hbm.ntm.inventory.AshpitMenu;
import com.hbm.ntm.inventory.BatterySocketMenu;
import com.hbm.ntm.inventory.BlastFurnaceMenu;
import com.hbm.ntm.inventory.BrickFurnaceMenu;
import com.hbm.ntm.inventory.BreedingReactorMenu;
import com.hbm.ntm.inventory.CombinationOvenMenu;
import com.hbm.ntm.inventory.CombustionEngineMenu;
import com.hbm.ntm.inventory.CrucibleMenu;
import com.hbm.ntm.inventory.ChemicalPlantMenu;
import com.hbm.ntm.inventory.CentrifugeMenu;
import com.hbm.ntm.inventory.ConveyorBoxerMenu;
import com.hbm.ntm.inventory.CraneExtractorMenu;
import com.hbm.ntm.inventory.CraneInserterMenu;
import com.hbm.ntm.inventory.DieselGeneratorMenu;
import com.hbm.ntm.inventory.DfcMenu;
import com.hbm.ntm.inventory.ElectricFurnaceMenu;
import com.hbm.ntm.inventory.FireboxMenu;
import com.hbm.ntm.inventory.FensuMenu;
import com.hbm.ntm.inventory.GasTurbineMenu;
import com.hbm.ntm.inventory.FluidIdentifierMenu;
import com.hbm.ntm.inventory.FluidStorageTankMenu;
import com.hbm.ntm.inventory.FluidBurnerMenu;
import com.hbm.ntm.inventory.HeatExchangerMenu;
import com.hbm.ntm.inventory.MachinePressMenu;
import com.hbm.ntm.inventory.MachineShredderMenu;
import com.hbm.ntm.inventory.MicrowaveMenu;
import com.hbm.ntm.inventory.RadioTorchMenu;
import com.hbm.ntm.inventory.LargeNukeMenu;
import com.hbm.ntm.inventory.NukeManMenu;
import com.hbm.ntm.inventory.BombMultiMenu;
import com.hbm.ntm.inventory.NukeN2Menu;
import com.hbm.ntm.inventory.NukeFleijaMenu;
import com.hbm.ntm.inventory.NukeSoliniumMenu;
import com.hbm.ntm.inventory.NukeCustomMenu;
import com.hbm.ntm.inventory.NukeFstbmbMenu;
import com.hbm.ntm.inventory.NukePrototypeMenu;
import com.hbm.ntm.inventory.OilDerrickMenu;
import com.hbm.ntm.inventory.RefineryMenu;
import com.hbm.ntm.inventory.RadGenMenu;
import com.hbm.ntm.inventory.ResearchReactorMenu;
import com.hbm.ntm.inventory.SteelFurnaceMenu;
import com.hbm.ntm.inventory.SteamTurbineMenu;
import com.hbm.ntm.inventory.TurbofanMenu;
import com.hbm.ntm.inventory.WoodBurnerMenu;
import com.hbm.ntm.inventory.WasteDrumMenu;
import com.hbm.ntm.inventory.ZirnoxMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, HbmNtm.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<DfcMenu>> DFC_CORE = MENUS.register(
            "dfc_core", () -> IMenuTypeExtension.create(DfcMenu::core));
    public static final DeferredHolder<MenuType<?>, MenuType<DfcMenu>> DFC_EMITTER = MENUS.register(
            "dfc_emitter", () -> IMenuTypeExtension.create(DfcMenu::emitter));
    public static final DeferredHolder<MenuType<?>, MenuType<DfcMenu>> DFC_INJECTOR = MENUS.register(
            "dfc_injector", () -> IMenuTypeExtension.create(DfcMenu::injector));
    public static final DeferredHolder<MenuType<?>, MenuType<DfcMenu>> DFC_RECEIVER = MENUS.register(
            "dfc_receiver", () -> IMenuTypeExtension.create(DfcMenu::receiver));
    public static final DeferredHolder<MenuType<?>, MenuType<DfcMenu>> DFC_STABILIZER = MENUS.register(
            "dfc_stabilizer", () -> IMenuTypeExtension.create(DfcMenu::stabilizer));

    public static final DeferredHolder<MenuType<?>, MenuType<NukeManMenu>> NUKE_MAN = MENUS.register(
            "nuke_man", () -> IMenuTypeExtension.create(NukeManMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BombMultiMenu>> BOMB_MULTI = MENUS.register(
            "bomb_multi", () -> IMenuTypeExtension.create(BombMultiMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<NukeN2Menu>> NUKE_N2 = MENUS.register(
            "nuke_n2", () -> IMenuTypeExtension.create(NukeN2Menu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<NukeFleijaMenu>> NUKE_FLEIJA = MENUS.register(
            "nuke_fleija", () -> IMenuTypeExtension.create(NukeFleijaMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<NukeSoliniumMenu>> NUKE_SOLINIUM = MENUS.register(
            "nuke_solinium", () -> IMenuTypeExtension.create(NukeSoliniumMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<NukePrototypeMenu>> NUKE_PROTOTYPE = MENUS.register(
            "nuke_prototype", () -> IMenuTypeExtension.create(NukePrototypeMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<NukeCustomMenu>> NUKE_CUSTOM = MENUS.register(
            "nuke_custom", () -> IMenuTypeExtension.create(NukeCustomMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<NukeFstbmbMenu>> NUKE_FSTBMB = MENUS.register(
            "nuke_fstbmb", () -> IMenuTypeExtension.create(NukeFstbmbMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<LargeNukeMenu>> LARGE_NUKE = MENUS.register(
            "large_nuke", () -> IMenuTypeExtension.create(LargeNukeMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BatterySocketMenu>> BATTERY_SOCKET = MENUS.register(
            "battery_socket", () -> IMenuTypeExtension.create(BatterySocketMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FensuMenu>> FENSU = MENUS.register(
            "machine_battery_redd", () -> IMenuTypeExtension.create(FensuMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MachinePressMenu>> MACHINE_PRESS = MENUS.register(
            "machine_press",
            () -> IMenuTypeExtension.create(MachinePressMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<MachineShredderMenu>> MACHINE_SHREDDER = MENUS.register(
            "machine_shredder",
            () -> IMenuTypeExtension.create(MachineShredderMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<FireboxMenu>> HEATER_FIREBOX = MENUS.register(
            "heater_firebox", () -> IMenuTypeExtension.create(FireboxMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FluidBurnerMenu>> HEATER_OILBURNER = MENUS.register(
            "heater_oilburner", () -> IMenuTypeExtension.create(FluidBurnerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HeatExchangerMenu>> HEATER_HEATEX = MENUS.register(
            "heater_heatex", () -> IMenuTypeExtension.create(HeatExchangerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BlastFurnaceMenu>> MACHINE_BLAST_FURNACE = MENUS.register(
            "machine_blast_furnace", () -> IMenuTypeExtension.create(BlastFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CombinationOvenMenu>> FURNACE_COMBINATION = MENUS.register(
            "furnace_combination", () -> IMenuTypeExtension.create(CombinationOvenMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SteelFurnaceMenu>> FURNACE_STEEL = MENUS.register(
            "furnace_steel", () -> IMenuTypeExtension.create(SteelFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricFurnaceMenu>> MACHINE_ELECTRIC_FURNACE = MENUS.register(
            "machine_electric_furnace_off", () -> IMenuTypeExtension.create(ElectricFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BrickFurnaceMenu>> MACHINE_FURNACE_BRICK = MENUS.register(
            "machine_furnace_brick_off", () -> IMenuTypeExtension.create(BrickFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<WoodBurnerMenu>> MACHINE_WOOD_BURNER = MENUS.register(
            "machine_wood_burner", () -> IMenuTypeExtension.create(WoodBurnerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MicrowaveMenu>> MACHINE_MICROWAVE = MENUS.register(
            "machine_microwave", () -> IMenuTypeExtension.create(MicrowaveMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AshpitMenu>> MACHINE_ASHPIT = MENUS.register(
            "machine_ashpit", () -> IMenuTypeExtension.create(AshpitMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ArmorTableMenu>> ARMOR_TABLE = MENUS.register(
            "armor_table", () -> IMenuTypeExtension.create(ArmorTableMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AnvilMenu>> ANVIL = MENUS.register(
            "anvil", () -> IMenuTypeExtension.create(AnvilMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<AssemblyMachineMenu>> MACHINE_ASSEMBLY_MACHINE = MENUS.register(
            "machine_assembly_machine", () -> IMenuTypeExtension.create(AssemblyMachineMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ChemicalPlantMenu>> MACHINE_CHEMICAL_PLANT = MENUS.register(
            "machine_chemical_plant", () -> IMenuTypeExtension.create(ChemicalPlantMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SolderingStationMenu>> MACHINE_SOLDERING_STATION = MENUS.register(
            "machine_soldering_station", () -> IMenuTypeExtension.create(SolderingStationMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ArcWelderMenu>> MACHINE_ARC_WELDER = MENUS.register(
            "machine_arc_welder", () -> IMenuTypeExtension.create(ArcWelderMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ArcFurnaceMenu>> MACHINE_ARC_FURNACE = MENUS.register(
            "machine_arc_furnace", () -> IMenuTypeExtension.create(ArcFurnaceMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RefineryMenu>> MACHINE_REFINERY = MENUS.register(
            "machine_refinery", () -> IMenuTypeExtension.create(RefineryMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CentrifugeMenu>> MACHINE_CENTRIFUGE = MENUS.register(
            "machine_centrifuge", () -> IMenuTypeExtension.create(CentrifugeMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CrucibleMenu>> MACHINE_CRUCIBLE = MENUS.register(
            "machine_crucible", () -> IMenuTypeExtension.create(CrucibleMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FluidIdentifierMenu>> FLUID_IDENTIFIER = MENUS.register(
            "fluid_identifier", () -> IMenuTypeExtension.create(FluidIdentifierMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<OilDerrickMenu>> MACHINE_WELL = MENUS.register(
            "machine_well", () -> IMenuTypeExtension.create(OilDerrickMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DieselGeneratorMenu>> MACHINE_DIESEL = MENUS.register(
            "machine_diesel", () -> IMenuTypeExtension.create(DieselGeneratorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CombustionEngineMenu>> MACHINE_COMBUSTION_ENGINE = MENUS.register(
            "machine_combustion_engine", () -> IMenuTypeExtension.create(CombustionEngineMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<TurbofanMenu>> MACHINE_TURBOFAN = MENUS.register(
            "machine_turbofan", () -> IMenuTypeExtension.create(TurbofanMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<GasTurbineMenu>> MACHINE_TURBINE_GAS = MENUS.register(
            "machine_turbinegas", () -> IMenuTypeExtension.create(GasTurbineMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SteamTurbineMenu>> MACHINE_TURBINE = MENUS.register(
            "machine_turbine", () -> IMenuTypeExtension.create(SteamTurbineMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ZirnoxMenu>> REACTOR_ZIRNOX = MENUS.register(
            "reactor_zirnox", () -> IMenuTypeExtension.create(ZirnoxMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BreedingReactorMenu>> MACHINE_REACTOR_BREEDING = MENUS.register(
            "machine_reactor_breeding", () -> IMenuTypeExtension.create(BreedingReactorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ResearchReactorMenu>> REACTOR_RESEARCH = MENUS.register(
            "reactor_research", () -> IMenuTypeExtension.create(ResearchReactorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RadGenMenu>> MACHINE_RADGEN = MENUS.register(
            "machine_radgen", () -> IMenuTypeExtension.create(RadGenMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<WasteDrumMenu>> MACHINE_WASTE_DRUM = MENUS.register(
            "machine_waste_drum", () -> IMenuTypeExtension.create(WasteDrumMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FluidStorageTankMenu>> MACHINE_FLUIDTANK = MENUS.register(
            "machine_fluidtank", () -> IMenuTypeExtension.create(FluidStorageTankMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ConveyorBoxerMenu>> CRANE_BOXER = MENUS.register(
            "crane_boxer", () -> IMenuTypeExtension.create(ConveyorBoxerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CraneExtractorMenu>> CRANE_EXTRACTOR = MENUS.register(
            "crane_extractor", () -> IMenuTypeExtension.create(CraneExtractorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CraneInserterMenu>> CRANE_INSERTER = MENUS.register(
            "crane_inserter", () -> IMenuTypeExtension.create(CraneInserterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<RadioTorchMenu>> RADIO_TORCH = MENUS.register(
            "radio_torch", () -> IMenuTypeExtension.create(RadioTorchMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
