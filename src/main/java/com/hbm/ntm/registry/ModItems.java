package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.content.HazardousMaterialDefinitions;
import com.hbm.ntm.content.MaterialDefinitions;
import com.hbm.ntm.hazard.HazardProfile;
import com.hbm.ntm.item.HazardousBlockItem;
import com.hbm.ntm.item.AmmoStandardItem;
import com.hbm.ntm.item.AmmoSecretItem;
import com.hbm.ntm.item.ArcElectrodeItem;
import com.hbm.ntm.item.ArcElectrodeBurntItem;
import com.hbm.ntm.item.ArmorCladdingItem;
import com.hbm.ntm.item.AshItem;
import com.hbm.ntm.item.B92EnergyCellItem;
import com.hbm.ntm.item.B92Item;
import com.hbm.ntm.item.B93Item;
import com.hbm.ntm.item.BatteryPackItem;
import com.hbm.ntm.item.BlastInfoBlockItem;
import com.hbm.ntm.item.BloodBagItem;
import com.hbm.ntm.item.BlueprintItem;
import com.hbm.ntm.item.BlowtorchItem;
import com.hbm.ntm.item.BoltItem;
import com.hbm.ntm.item.BreakActionRevolverItem;
import com.hbm.ntm.item.BreedingRodItem;
import com.hbm.ntm.item.ChargeBlockItem;
import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.item.CasingItem;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.item.CustomLoreItem;
import com.hbm.ntm.item.DeadMansDetonatorItem;
import com.hbm.ntm.item.DeadMansExplosiveItem;
import com.hbm.ntm.item.DefuserItem;
import com.hbm.ntm.item.DetonatorItem;
import com.hbm.ntm.item.DepletedPlateFuelItem;
import com.hbm.ntm.item.DieselGeneratorBlockItem;
import com.hbm.ntm.item.CombustionEngineBlockItem;
import com.hbm.ntm.item.DfcCatalystItem;
import com.hbm.ntm.item.DfcCoreItem;
import com.hbm.ntm.item.DfcLensItem;
import com.hbm.ntm.item.DenseWireItem;
import com.hbm.ntm.item.FluidStorageTankBlockItem;
import com.hbm.ntm.item.FortyMillimeterGunItem;
import com.hbm.ntm.item.FlamerGunItem;
import com.hbm.ntm.item.RocketLauncherItem;
import com.hbm.ntm.item.QuadRocketLauncherItem;
import com.hbm.ntm.item.MissileLauncherItem;
import com.hbm.ntm.item.StingerLauncherItem;
import com.hbm.ntm.item.FlamePonyItem;
import com.hbm.ntm.item.DosimeterItem;
import com.hbm.ntm.item.BrokenMaresLegItem;
import com.hbm.ntm.item.DualMaresLegItem;
import com.hbm.ntm.item.DualUziItem;
import com.hbm.ntm.item.DualStarFItem;
import com.hbm.ntm.item.DepthRockBlockItem;
import com.hbm.ntm.item.DepthRockPickaxeItem;
import com.hbm.ntm.item.EnvsuitArmorItem;
import com.hbm.ntm.item.DntArmorItem;
import com.hbm.ntm.item.GeigerCounterItem;
import com.hbm.ntm.item.G3Item;
import com.hbm.ntm.item.Stg77Item;
import com.hbm.ntm.item.M2Item;
import com.hbm.ntm.item.TeslaCannonItem;
import com.hbm.ntm.item.AmatItem;
import com.hbm.ntm.item.SubtletyItem;
import com.hbm.ntm.item.PenanceItem;
import com.hbm.ntm.item.GuideBookItem;
import com.hbm.ntm.item.HenryRifleItem;
import com.hbm.ntm.item.HangmanItem;
import com.hbm.ntm.item.HeavyRevolverItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryPartItem;
import com.hbm.ntm.item.FoundryIngotItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.item.FluidDuctItem;
import com.hbm.ntm.item.FluidCellItem;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FensuBlockItem;
import com.hbm.ntm.item.HazmatArmorItem;
import com.hbm.ntm.item.HazmatFilterItem;
import com.hbm.ntm.item.ClothRagItem;
import com.hbm.ntm.item.ProtectiveMaskItem;
import com.hbm.ntm.item.PistonSetItem;
import com.hbm.ntm.item.RadioactiveItem;
import com.hbm.ntm.item.FleijaPartItem;
import com.hbm.ntm.item.N2ChargeItem;
import com.hbm.ntm.item.SoliniumPartItem;
import com.hbm.ntm.item.RadiationMedicineItem;
import com.hbm.ntm.item.DescriptionBlockItem;
import com.hbm.ntm.item.HazardousItem;
import com.hbm.ntm.item.InfiniteBatteryItem;
import com.hbm.ntm.item.InfiniteFluidBarrelItem;
import com.hbm.ntm.item.LagPistolItem;
import com.hbm.ntm.item.LaserDetonatorItem;
import com.hbm.ntm.item.AutoShotgunItem;
import com.hbm.ntm.item.LiberatorItem;
import com.hbm.ntm.item.MaresLegItem;
import com.hbm.ntm.item.NtmAnvilBlockItem;
import com.hbm.ntm.item.NuclearWasteItem;
import com.hbm.ntm.item.MachineUpgradeItem;
import com.hbm.ntm.item.MultiDetonatorItem;
import com.hbm.ntm.item.MkuSyringeItem;
import com.hbm.ntm.item.NineMillimeterGunItem;
import com.hbm.ntm.item.OilDerrickBlockItem;
import com.hbm.ntm.item.OreChunkItem;
import com.hbm.ntm.item.OreDensityScannerItem;
import com.hbm.ntm.item.RefineryBlockItem;
import com.hbm.ntm.item.ResearchReactorBlockItem;
import com.hbm.ntm.item.RangefinderItem;
import com.hbm.ntm.item.SourceFluidContainerItem;
import com.hbm.ntm.item.SurveyScannerItem;
import com.hbm.ntm.item.PepperboxItem;
import com.hbm.ntm.item.PlateFuelItem;
import com.hbm.ntm.item.PipeItem;
import com.hbm.ntm.item.PowerFistItem;
import com.hbm.ntm.item.ShredderBladeItem;
import com.hbm.ntm.item.ScrewdriverItem;
import com.hbm.ntm.item.SellafieldBlockItem;
import com.hbm.ntm.item.SirenTrackItem;
import com.hbm.ntm.conveyor.ConveyorType;
import com.hbm.ntm.item.ScaffoldBlockItem;
import com.hbm.ntm.item.ShellItem;
import com.hbm.ntm.item.ShredderItem;
import com.hbm.ntm.item.SexyItem;
import com.hbm.ntm.item.SevenSixTwoGunItem;
import com.hbm.ntm.item.TwentyTwoGunItem;
import com.hbm.ntm.item.SpasItem;
import com.hbm.ntm.item.StampItem;
import com.hbm.ntm.item.StirlingMachineBlockItem;
import com.hbm.ntm.item.TurbofanBlockItem;
import com.hbm.ntm.item.ZirnoxRodItem;
import com.hbm.ntm.item.StoneResourceBlockItem;
import com.hbm.ntm.item.SawmillMachineBlockItem;
import com.hbm.ntm.item.UniversalFluidTankItem;
import com.hbm.ntm.item.WeaponizedStarblasterCellItem;
import com.hbm.ntm.item.WireFineItem;
import com.hbm.ntm.item.WeldedPlateItem;
import com.hbm.ntm.item.ModToolTiers;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HbmNtm.MOD_ID);
    public static final Map<String, DeferredItem<? extends Item>> MATERIAL_ITEMS;
    /** Every registered HBM item indexed by its path, so lookups resolve standalone-field and
     * block items that are not routed through the material catalogs. Built after registration. */
    private static final Map<String, DeferredItem<? extends Item>> ALL_ITEMS;
    public static final Map<String, DeferredItem<? extends BlockItem>> MATERIAL_BLOCK_ITEMS;
    public static final Map<String, DeferredItem<BlockItem>> LEGACY_ORE_BLOCK_ITEMS;
    public static final Map<String, DeferredItem<Item>> LEGACY_ORE_RESOURCE_ITEMS;
    public static final DeferredItem<? extends Item> URANIUM_INGOT;
    public static final DeferredItem<? extends BlockItem> URANIUM_BLOCK_ITEM;
    public static final DeferredItem<BlockItem> ORE_TITANIUM_ITEM;
    public static final DeferredItem<BlockItem> ORE_TUNGSTEN_ITEM;
    public static final DeferredItem<BlockItem> ORE_COBALT_ITEM;
    public static final DeferredItem<BlockItem> ORE_RARE_ITEM;
    public static final DeferredItem<BlockItem> ORE_COLTAN_ITEM;
    public static final DeferredItem<StoneResourceBlockItem> STONE_RESOURCE_ITEM;
    public static final DeferredItem<BlockItem> ORE_OIL_ITEM;
    public static final DeferredItem<BlockItem> ORE_OIL_EMPTY_ITEM;
    public static final DeferredItem<BlockItem> DIRT_OILY_ITEM;
    public static final DeferredItem<BlockItem> DIRT_DEAD_ITEM;
    public static final DeferredItem<BlockItem> SAND_DIRTY_ITEM;
    public static final DeferredItem<BlockItem> SAND_DIRTY_RED_ITEM;
    public static final DeferredItem<BlockItem> STONE_CRACKED_ITEM;
    public static final DeferredItem<BlockItem> PLANT_DEAD_ITEM;
    public static final DeferredItem<BlockItem> OIL_SPILL_ITEM;
    public static final DeferredItem<HazardousItem> TRINITITE;
    public static final DeferredItem<CustomLoreItem> BURNT_BARK;
    public static final DeferredItem<BlockItem> WASTE_EARTH_ITEM;
    public static final DeferredItem<BlockItem> WASTE_MYCELIUM_ITEM;
    public static final DeferredItem<BlockItem> WASTE_TRINITITE_ITEM;
    public static final DeferredItem<BlockItem> WASTE_TRINITITE_RED_ITEM;
    public static final DeferredItem<BlockItem> WASTE_LOG_ITEM;
    public static final DeferredItem<BlockItem> WASTE_PLANKS_ITEM;
    public static final DeferredItem<BlockItem> FROZEN_DIRT_ITEM;
    public static final DeferredItem<BlockItem> FROZEN_GRASS_ITEM;
    public static final DeferredItem<BlockItem> FROZEN_LOG_ITEM;
    public static final DeferredItem<BlockItem> FROZEN_PLANKS_ITEM;
    public static final DeferredItem<HazardousBlockItem> BLOCK_TRINITITE_ITEM;
    public static final DeferredItem<HazardousBlockItem> BLOCK_WASTE_ITEM;
    public static final DeferredItem<SellafieldBlockItem> SELLAFIELD_ITEM;
    public static final DeferredItem<BlockItem> SELLAFIELD_SLAKED_ITEM;
    public static final DeferredItem<Item> OIL_TAR;
    public static final DeferredItem<Item> COKE_COAL;
    public static final DeferredItem<Item> COKE_LIGNITE;
    public static final DeferredItem<Item> COKE_PETROLEUM;
    public static final DeferredItem<BlockItem> BLOCK_COKE_COAL_ITEM;
    public static final DeferredItem<BlockItem> BLOCK_COKE_LIGNITE_ITEM;
    public static final DeferredItem<BlockItem> BLOCK_COKE_PETROLEUM_ITEM;
    public static final DeferredItem<OreChunkItem> CHUNK_ORE;
    public static final DeferredItem<Item> CANISTER_EMPTY;
    public static final DeferredItem<SourceFluidContainerItem> CANISTER_FULL;
    public static final DeferredItem<Item> GAS_EMPTY;
    public static final DeferredItem<SourceFluidContainerItem> GAS_FULL;
    public static final DeferredItem<Item> CELL_EMPTY;
    public static final DeferredItem<FluidCellItem> CELL_TRITIUM;
    public static final DeferredItem<FluidCellItem> CELL_SAS3;
    public static final DeferredItem<OilDerrickBlockItem> MACHINE_WELL_ITEM;
    public static final DeferredItem<BlockItem> RADIO_TORCH_SENDER_ITEM;
    public static final DeferredItem<BlockItem> RADIO_TORCH_RECEIVER_ITEM;
    public static final DeferredItem<BlockItem> RADIO_TORCH_COUNTER_ITEM;
    public static final DeferredItem<BlockItem> RADIO_TORCH_LOGIC_ITEM;
    public static final DeferredItem<BlockItem> RADIO_TORCH_READER_ITEM;
    public static final DeferredItem<BlockItem> RADIO_TORCH_CONTROLLER_ITEM;
    public static final DeferredItem<BlockItem> RED_CABLE_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_PRESS_ITEM;
    public static final DeferredItem<BlockItem> PRESS_PREHEATER_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_SHREDDER_ITEM;
    public static final DeferredItem<? extends BlockItem> GRAVEL_OBSIDIAN_ITEM;
    public static final DeferredItem<? extends BlockItem> GRAVEL_DIAMOND_ITEM;
    public static final DeferredItem<ShredderBladeItem> BLADES_STEEL;
    public static final DeferredItem<ShredderBladeItem> BLADES_TITANIUM;
    public static final DeferredItem<ShredderBladeItem> BLADES_DESH;
    public static final DeferredItem<Item> BLADE_TITANIUM;
    public static final DeferredItem<Item> TURBINE_TITANIUM;
    public static final DeferredItem<Item> BLADE_TUNGSTEN;
    public static final DeferredItem<Item> TURBINE_TUNGSTEN;
    public static final DeferredItem<FlamePonyItem> FLAME_PONY;
    public static final DeferredItem<InfiniteBatteryItem> BATTERY_CREATIVE;
    public static final DeferredItem<InfiniteFluidBarrelItem> FLUID_BARREL_INFINITE;
    public static final DeferredItem<BatteryPackItem> BATTERY_PACK;
    public static final DeferredItem<? extends BlockItem> MACHINE_BATTERY_SOCKET_ITEM;
    public static final DeferredItem<FensuBlockItem> MACHINE_BATTERY_REDD_ITEM;
    public static final DeferredItem<Item> GEAR_LARGE;
    public static final DeferredItem<Item> SAWBLADE;
    public static final DeferredItem<Item> POWDER_SAWDUST;
    public static final DeferredItem<Item> SOLID_FUEL;
    public static final DeferredItem<Item> SOLID_FUEL_PRESTO;
    public static final DeferredItem<Item> SOLID_FUEL_PRESTO_TRIPLET;
    public static final DeferredItem<Item> SOLID_FUEL_BF;
    public static final DeferredItem<Item> SOLID_FUEL_PRESTO_BF;
    public static final DeferredItem<Item> SOLID_FUEL_PRESTO_TRIPLET_BF;
    public static final DeferredItem<Item> ROCKET_FUEL;
    public static final DeferredItem<AshItem> POWDER_ASH;
    public static final DeferredItem<? extends BlockItem> HEATER_FIREBOX_ITEM;
    public static final DeferredItem<? extends BlockItem> HEATER_OVEN_ITEM;
    public static final DeferredItem<? extends BlockItem> MACHINE_ASHPIT_ITEM;
    public static final DeferredItem<StirlingMachineBlockItem> MACHINE_STIRLING_ITEM;
    public static final DeferredItem<SawmillMachineBlockItem> MACHINE_SAWMILL_ITEM;
    public static final DeferredItem<DescriptionBlockItem> MACHINE_STEAM_ENGINE_ITEM;
    public static final DeferredItem<DescriptionBlockItem> MACHINE_INDUSTRIAL_TURBINE_ITEM;
    public static final DeferredItem<DescriptionBlockItem> MACHINE_TURBINE_GAS_ITEM;
    public static final DeferredItem<TurbofanBlockItem> MACHINE_TURBOFAN_ITEM;
    public static final DeferredItem<DescriptionBlockItem> MACHINE_TURBINE_ITEM;
    public static final DeferredItem<DescriptionBlockItem> REACTOR_ZIRNOX_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_REACTOR_BREEDING_ITEM;
    public static final DeferredItem<ResearchReactorBlockItem> REACTOR_RESEARCH_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_RADGEN_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_WASTE_DRUM_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_SIREN_ITEM;
    public static final DeferredItem<SirenTrackItem> SIREN_TRACK;
    public static final DeferredItem<NuclearWasteItem> NUCLEAR_WASTE_LONG;
    public static final DeferredItem<NuclearWasteItem> NUCLEAR_WASTE_LONG_TINY;
    public static final DeferredItem<NuclearWasteItem> NUCLEAR_WASTE_SHORT;
    public static final DeferredItem<NuclearWasteItem> NUCLEAR_WASTE_SHORT_TINY;
    public static final DeferredItem<NuclearWasteItem> NUCLEAR_WASTE_LONG_DEPLETED;
    public static final DeferredItem<NuclearWasteItem> NUCLEAR_WASTE_LONG_DEPLETED_TINY;
    public static final DeferredItem<NuclearWasteItem> NUCLEAR_WASTE_SHORT_DEPLETED;
    public static final DeferredItem<NuclearWasteItem> NUCLEAR_WASTE_SHORT_DEPLETED_TINY;
    public static final DeferredItem<HazardousItem> SCRAP_NUCLEAR;
    public static final DeferredItem<HazardousItem> GEM_RAD;
    public static final DeferredItem<Item> ROD_EMPTY;
    public static final DeferredItem<Item> ROD_DUAL_EMPTY;
    public static final DeferredItem<Item> ROD_QUAD_EMPTY;
    public static final DeferredItem<BreedingRodItem> ROD;
    public static final DeferredItem<BreedingRodItem> ROD_DUAL;
    public static final DeferredItem<BreedingRodItem> ROD_QUAD;
    public static final DeferredItem<PlateFuelItem> PLATE_FUEL_U233;
    public static final DeferredItem<PlateFuelItem> PLATE_FUEL_U235;
    public static final DeferredItem<PlateFuelItem> PLATE_FUEL_MOX;
    public static final DeferredItem<PlateFuelItem> PLATE_FUEL_PU239;
    public static final DeferredItem<PlateFuelItem> PLATE_FUEL_SA326;
    public static final DeferredItem<PlateFuelItem> PLATE_FUEL_RA226BE;
    public static final DeferredItem<PlateFuelItem> PLATE_FUEL_PU238BE;
    public static final DeferredItem<DepletedPlateFuelItem> WASTE_PLATE_U233;
    public static final DeferredItem<DepletedPlateFuelItem> WASTE_PLATE_U235;
    public static final DeferredItem<DepletedPlateFuelItem> WASTE_PLATE_MOX;
    public static final DeferredItem<DepletedPlateFuelItem> WASTE_PLATE_PU239;
    public static final DeferredItem<DepletedPlateFuelItem> WASTE_PLATE_SA326;
    public static final DeferredItem<DepletedPlateFuelItem> WASTE_PLATE_RA226BE;
    public static final DeferredItem<DepletedPlateFuelItem> WASTE_PLATE_PU238BE;
    public static final DeferredItem<Item> ROD_ZIRNOX_EMPTY;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_NATURAL_URANIUM_FUEL;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_URANIUM_FUEL;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_TH232;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_THORIUM_FUEL;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_MOX_FUEL;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_PLUTONIUM_FUEL;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_U233_FUEL;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_U235_FUEL;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_LES_FUEL;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_LITHIUM;
    public static final DeferredItem<ZirnoxRodItem> ROD_ZIRNOX_ZFB_MOX;
    public static final DeferredItem<Item> ROD_ZIRNOX_TRITIUM;
    public static final DeferredItem<Item> ROD_ZIRNOX_NATURAL_URANIUM_FUEL_DEPLETED;
    public static final DeferredItem<Item> ROD_ZIRNOX_URANIUM_FUEL_DEPLETED;
    public static final DeferredItem<Item> ROD_ZIRNOX_THORIUM_FUEL_DEPLETED;
    public static final DeferredItem<Item> ROD_ZIRNOX_MOX_FUEL_DEPLETED;
    public static final DeferredItem<Item> ROD_ZIRNOX_PLUTONIUM_FUEL_DEPLETED;
    public static final DeferredItem<Item> ROD_ZIRNOX_U233_FUEL_DEPLETED;
    public static final DeferredItem<Item> ROD_ZIRNOX_U235_FUEL_DEPLETED;
    public static final DeferredItem<Item> ROD_ZIRNOX_LES_FUEL_DEPLETED;
    public static final DeferredItem<Item> ROD_ZIRNOX_ZFB_MOX_DEPLETED;
    public static final DeferredItem<DescriptionBlockItem> PUMP_STEAM_ITEM;
    public static final DeferredItem<DescriptionBlockItem> PUMP_ELECTRIC_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_INTAKE_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_CONDENSER_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_CONDENSER_POWERED_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_BOILER_ITEM;
    public static final DeferredItem<BlockItem> REINFORCED_STONE_ITEM;
    public static final DeferredItem<BlastInfoBlockItem> REINFORCED_GLASS_ITEM;
    public static final DeferredItem<BlastInfoBlockItem> REINFORCED_GLASS_PANE_ITEM;
    public static final DeferredItem<BlockItem> GNEISS_TILE_ITEM;
    public static final DeferredItem<BlockItem> GNEISS_BRICK_ITEM;
    public static final DeferredItem<BlockItem> GNEISS_CHISELED_ITEM;
    public static final DeferredItem<BlastInfoBlockItem> REINFORCED_LIGHT_ITEM;
    public static final DeferredItem<BlastInfoBlockItem> REINFORCED_SAND_ITEM;
    public static final DeferredItem<DepthRockBlockItem> DEPTH_BRICK_ITEM;
    public static final DeferredItem<DepthRockBlockItem> DEPTH_TILES_ITEM;
    public static final DeferredItem<DepthRockBlockItem> DEPTH_NETHER_BRICK_ITEM;
    public static final DeferredItem<DepthRockBlockItem> DEPTH_NETHER_TILES_ITEM;
    public static final DeferredItem<BlockItem> FLUID_DUCT_NEO_ITEM;
    public static final DeferredItem<FluidDuctItem> FLUID_DUCT;
    public static final DeferredItem<BlockItem> DYNAMITE_ITEM;
    public static final DeferredItem<BlockItem> TNT_NTM_ITEM;
    public static final DeferredItem<BlockItem> SEMTEX_ITEM;
    public static final DeferredItem<BlockItem> C4_ITEM;
    public static final DeferredItem<Item> SAFETY_FUSE;
    public static final DeferredItem<HazardousItem> BALL_DYNAMITE;
    public static final DeferredItem<HazardousItem> STICK_DYNAMITE;
    public static final DeferredItem<HazardousItem> STICK_TNT;
    public static final DeferredItem<HazardousItem> STICK_SEMTEX;
    public static final DeferredItem<HazardousItem> STICK_C4;
    public static final DeferredItem<Item> DUCTTAPE;
    public static final DeferredItem<DefuserItem> DEFUSER;
    public static final DeferredItem<RangefinderItem> RANGEFINDER;
    public static final DeferredItem<DetonatorItem> DETONATOR;
    public static final DeferredItem<MultiDetonatorItem> DETONATOR_MULTI;
    public static final DeferredItem<LaserDetonatorItem> DETONATOR_LASER;
    public static final DeferredItem<DeadMansDetonatorItem> DETONATOR_DEADMAN;
    public static final DeferredItem<DeadMansExplosiveItem> DETONATOR_DE;
    public static final DeferredItem<ChargeBlockItem> CHARGE_DYNAMITE_ITEM;
    public static final DeferredItem<ChargeBlockItem> CHARGE_MINER_ITEM;
    public static final DeferredItem<ChargeBlockItem> CHARGE_C4_ITEM;
    public static final DeferredItem<ChargeBlockItem> CHARGE_SEMTEX_ITEM;
    public static final DeferredItem<BlockItem> MINE_AP_ITEM;
    public static final DeferredItem<BlockItem> MINE_HE_ITEM;
    public static final DeferredItem<BlockItem> MINE_SHRAP_ITEM;
    public static final DeferredItem<BlockItem> MINE_FAT_ITEM;
    public static final DeferredItem<BlockItem> MINE_NAVAL_ITEM;
    public static final DeferredItem<CustomLoreItem> EARLY_EXPLOSIVE_LENSES;
    public static final DeferredItem<CustomLoreItem> EXPLOSIVE_LENSES;
    public static final DeferredItem<Item> GADGET_WIREING;
    public static final DeferredItem<HazardousItem> GADGET_CORE;
    public static final DeferredItem<Item> BOY_SHIELDING;
    public static final DeferredItem<HazardousItem> BOY_TARGET;
    public static final DeferredItem<HazardousItem> BOY_BULLET;
    public static final DeferredItem<HazardousItem> BOY_PROPELLANT;
    public static final DeferredItem<Item> BOY_IGNITER;
    public static final DeferredItem<Item> MAN_IGNITER;
    public static final DeferredItem<RadioactiveItem> MAN_CORE;
    public static final DeferredItem<HazardousItem> MIKE_CORE;
    public static final DeferredItem<Item> MIKE_DEUT;
    public static final DeferredItem<Item> MIKE_COOLING_UNIT;
    public static final DeferredItem<HazardousItem> TSAR_CORE;
    public static final DeferredItem<N2ChargeItem> N2_CHARGE;
    public static final DeferredItem<FleijaPartItem> FLEIJA_IGNITER;
    public static final DeferredItem<FleijaPartItem> FLEIJA_PROPELLANT;
    public static final DeferredItem<FleijaPartItem> FLEIJA_CORE;
    public static final DeferredItem<SoliniumPartItem> SOLINIUM_IGNITER;
    public static final DeferredItem<SoliniumPartItem> SOLINIUM_PROPELLANT;
    public static final DeferredItem<SoliniumPartItem> SOLINIUM_CORE;
    public static final DeferredItem<Item> FALLOUT;
    public static final DeferredItem<BlockItem> ASH_DIGAMMA_ITEM;
    public static final DeferredItem<CustomLoreItem> PELLET_GAS;
    public static final DeferredItem<BlockItem> VENT_CHLORINE_ITEM;
    public static final DeferredItem<BlockItem> CHLORINE_GAS_ITEM;
    public static final DeferredItem<BlockItem> VENT_CHLORINE_SEAL_ITEM;
    public static final DeferredItem<BlockItem> NUKE_GADGET_ITEM;
    public static final DeferredItem<BlockItem> NUKE_BOY_ITEM;
    public static final DeferredItem<BlockItem> NUKE_MAN_ITEM;
    public static final DeferredItem<BlockItem> NUKE_MIKE_ITEM;
    public static final DeferredItem<BlockItem> NUKE_TSAR_ITEM;
    public static final DeferredItem<BlockItem> NUKE_FLEIJA_ITEM;
    public static final DeferredItem<BlockItem> NUKE_SOLINIUM_ITEM;
    public static final DeferredItem<BlockItem> NUKE_N2_ITEM;
    public static final DeferredItem<DescriptionBlockItem> NUKE_PROTOTYPE_ITEM;
    public static final DeferredItem<BlockItem> NUKE_CUSTOM_ITEM;
    public static final DeferredItem<BlockItem> NUKE_FSTBMB_ITEM;
    public static final DeferredItem<BlockItem> BOMB_MULTI_ITEM;
    public static final DeferredItem<BlockItem> FLOAT_BOMB_ITEM;
    public static final DeferredItem<BlockItem> THERM_ENDO_ITEM;
    public static final DeferredItem<BlockItem> THERM_EXO_ITEM;
    public static final DeferredItem<Item> EGG_BALEFIRE_SHARD;
    public static final DeferredItem<CustomLoreItem> EGG_BALEFIRE;
    public static final DeferredItem<Item> BATTERY_SPARK;
    public static final DeferredItem<Item> BATTERY_TRIXITE;
    public static final DeferredItem<Item> CUSTOM_TNT;
    public static final DeferredItem<Item> CUSTOM_NUKE;
    public static final DeferredItem<Item> CUSTOM_SCHRAB;
    public static final DeferredItem<CustomLoreItem> IGNITER;
    public static final DeferredItem<AmmoStandardItem> AMMO_STANDARD;
    public static final DeferredItem<AmmoSecretItem> AMMO_SECRET;
    public static final DeferredItem<PepperboxItem> GUN_PEPPERBOX;
    public static final DeferredItem<BreakActionRevolverItem> GUN_LIGHT_REVOLVER;
    public static final DeferredItem<BreakActionRevolverItem> GUN_LIGHT_REVOLVER_ATLAS;
    public static final DeferredItem<HenryRifleItem> GUN_HENRY;
    public static final DeferredItem<HenryRifleItem> GUN_HENRY_LINCOLN;
    public static final DeferredItem<HeavyRevolverItem> GUN_HEAVY_REVOLVER;
    public static final DeferredItem<HangmanItem> GUN_HANGMAN;
    public static final DeferredItem<LagPistolItem> GUN_LAG;
    public static final DeferredItem<NineMillimeterGunItem> GUN_GREASEGUN;
    public static final DeferredItem<NineMillimeterGunItem> GUN_UZI;
    public static final DeferredItem<DualUziItem> GUN_UZI_AKIMBO;
    public static final DeferredItem<MaresLegItem> GUN_MARESLEG;
    public static final DeferredItem<DualMaresLegItem> GUN_MARESLEG_AKIMBO;
    public static final DeferredItem<BrokenMaresLegItem> GUN_MARESLEG_BROKEN;
    public static final DeferredItem<LiberatorItem> GUN_LIBERATOR;
    public static final DeferredItem<SpasItem> GUN_SPAS12;
    public static final DeferredItem<AutoShotgunItem> GUN_AUTOSHOTGUN;
    public static final DeferredItem<ShredderItem> GUN_AUTOSHOTGUN_SHREDDER;
    public static final DeferredItem<SexyItem> GUN_AUTOSHOTGUN_SEXY;
    public static final DeferredItem<B92Item> GUN_B92;
    public static final DeferredItem<B93Item> GUN_B93;
    public static final DeferredItem<B92EnergyCellItem> GUN_B92_AMMO;
    public static final DeferredItem<FortyMillimeterGunItem> GUN_FLAREGUN;
    public static final DeferredItem<FortyMillimeterGunItem> GUN_CONGOLAKE;
    public static final DeferredItem<FortyMillimeterGunItem> GUN_MK108;
    public static final DeferredItem<SevenSixTwoGunItem> GUN_CARBINE;
    public static final DeferredItem<SevenSixTwoGunItem> GUN_MINIGUN;
    public static final DeferredItem<SevenSixTwoGunItem> GUN_MAS36;
    public static final DeferredItem<TwentyTwoGunItem> GUN_AM180;
    public static final DeferredItem<TwentyTwoGunItem> GUN_STAR_F;
    public static final DeferredItem<DualStarFItem> GUN_STAR_F_AKIMBO;
    public static final DeferredItem<FlamerGunItem> GUN_FLAMER;
    public static final DeferredItem<FlamerGunItem> GUN_FLAMER_TOPAZ;
    public static final DeferredItem<FlamerGunItem> GUN_FLAMER_DAYBREAKER;
    public static final DeferredItem<RocketLauncherItem> GUN_PANZERSCHRECK;
    public static final DeferredItem<StingerLauncherItem> GUN_STINGER;
    public static final DeferredItem<QuadRocketLauncherItem> GUN_QUADRO;
    public static final DeferredItem<MissileLauncherItem> GUN_MISSILE_LAUNCHER;
    public static final DeferredItem<G3Item> GUN_G3;
    public static final DeferredItem<G3Item> GUN_G3_ZEBRA;
    public static final DeferredItem<Stg77Item> GUN_STG77;
    public static final DeferredItem<M2Item> GUN_M2;
    public static final DeferredItem<TeslaCannonItem> GUN_TESLA_CANNON;
    public static final DeferredItem<AmatItem> GUN_AMAT;
    public static final DeferredItem<SubtletyItem> GUN_AMAT_SUBTLETY;
    public static final DeferredItem<PenanceItem> GUN_AMAT_PENANCE;
    public static final DeferredItem<WeaponizedStarblasterCellItem> WEAPONIZED_STARBLASTER_CELL;
    public static final DeferredItem<PowerFistItem> MULTITOOL_DIG;
    public static final DeferredItem<PowerFistItem> MULTITOOL_SILK;
    public static final DeferredItem<PowerFistItem> MULTITOOL_EXT;
    public static final DeferredItem<PowerFistItem> MULTITOOL_MINER;
    public static final DeferredItem<PowerFistItem> MULTITOOL_HIT;
    public static final DeferredItem<PowerFistItem> MULTITOOL_BEAM;
    public static final DeferredItem<PowerFistItem> MULTITOOL_SKY;
    public static final DeferredItem<PowerFistItem> MULTITOOL_MEGA;
    public static final DeferredItem<PowerFistItem> MULTITOOL_JOULE;
    public static final DeferredItem<PowerFistItem> MULTITOOL_DECON;
    public static final DeferredItem<PowerFistItem> MULTITOOL_PANE;
    public static final DeferredItem<GeigerCounterItem> GEIGER_COUNTER;
    public static final DeferredItem<GuideBookItem> BOOK_GUIDE;
    public static final DeferredItem<DosimeterItem> DOSIMETER;
    public static final DeferredItem<SwordItem> STEEL_SWORD;
    public static final DeferredItem<PickaxeItem> STEEL_PICKAXE;
    public static final DeferredItem<AxeItem> STEEL_AXE;
    public static final DeferredItem<ShovelItem> STEEL_SHOVEL;
    public static final DeferredItem<HoeItem> STEEL_HOE;
    public static final DeferredItem<SwordItem> TITANIUM_SWORD;
    public static final DeferredItem<PickaxeItem> TITANIUM_PICKAXE;
    public static final DeferredItem<AxeItem> TITANIUM_AXE;
    public static final DeferredItem<ShovelItem> TITANIUM_SHOVEL;
    public static final DeferredItem<HoeItem> TITANIUM_HOE;
    public static final DeferredItem<SwordItem> COBALT_SWORD;
    public static final DeferredItem<PickaxeItem> COBALT_PICKAXE;
    public static final DeferredItem<AxeItem> COBALT_AXE;
    public static final DeferredItem<ShovelItem> COBALT_SHOVEL;
    public static final DeferredItem<HoeItem> COBALT_HOE;
    public static final DeferredItem<DepthRockPickaxeItem> BISMUTH_PICKAXE;
    public static final DeferredItem<SurveyScannerItem> SURVEY_SCANNER;
    public static final DeferredItem<OreDensityScannerItem> ORE_DENSITY_SCANNER;
    public static final DeferredItem<BlockItem> GEIGER_BLOCK_ITEM;
    public static final DeferredItem<BloodBagItem> IV_EMPTY;
    public static final DeferredItem<BloodBagItem> IV_BLOOD;
    public static final DeferredItem<RadiationMedicineItem> RADAWAY;
    public static final DeferredItem<RadiationMedicineItem> RADAWAY_STRONG;
    public static final DeferredItem<RadiationMedicineItem> RADAWAY_FLUSH;
    public static final DeferredItem<RadiationMedicineItem> RAD_X;
    public static final DeferredItem<RadiationMedicineItem> PILL_HERBAL;
    public static final DeferredItem<MkuSyringeItem> SYRINGE_MKUNICORN;
    public static final DeferredItem<Item> CHEESE;
    public static final DeferredItem<Item> REACHER;
    public static final DeferredItem<Item> HAZMAT_CLOTH;
    public static final DeferredItem<ClothRagItem> RAG;
    public static final DeferredItem<Item> RAG_DAMP;
    public static final DeferredItem<Item> RAG_PISS;
    public static final DeferredItem<Item> FILTER_COAL;
    public static final DeferredItem<Item> CATALYST_CLAY;
    public static final DeferredItem<HazmatFilterItem> GAS_MASK_FILTER;
    public static final DeferredItem<HazmatFilterItem> GAS_MASK_FILTER_MONO;
    public static final DeferredItem<HazmatFilterItem> GAS_MASK_FILTER_COMBO;
    public static final DeferredItem<HazmatFilterItem> GAS_MASK_FILTER_RAG;
    public static final DeferredItem<HazmatFilterItem> GAS_MASK_FILTER_PISS;
    public static final DeferredItem<HazmatArmorItem> HAZMAT_HELMET;
    public static final DeferredItem<HazmatArmorItem> HAZMAT_PLATE;
    public static final DeferredItem<HazmatArmorItem> HAZMAT_LEGS;
    public static final DeferredItem<HazmatArmorItem> HAZMAT_BOOTS;
    public static final DeferredItem<EnvsuitArmorItem> ENVSUIT_HELMET;
    public static final DeferredItem<EnvsuitArmorItem> ENVSUIT_PLATE;
    public static final DeferredItem<EnvsuitArmorItem> ENVSUIT_LEGS;
    public static final DeferredItem<EnvsuitArmorItem> ENVSUIT_BOOTS;
    public static final DeferredItem<DntArmorItem> DNS_HELMET;
    public static final DeferredItem<DntArmorItem> DNS_PLATE;
    public static final DeferredItem<DntArmorItem> DNS_LEGS;
    public static final DeferredItem<DntArmorItem> DNS_BOOTS;
    public static final DeferredItem<ProtectiveMaskItem> GOGGLES;
    public static final DeferredItem<ProtectiveMaskItem> ASHGLASSES;
    public static final DeferredItem<ProtectiveMaskItem> GAS_MASK;
    public static final DeferredItem<ProtectiveMaskItem> GAS_MASK_M65;
    public static final DeferredItem<ProtectiveMaskItem> GAS_MASK_MONO;
    public static final DeferredItem<ProtectiveMaskItem> GAS_MASK_OLDE;
    public static final DeferredItem<ProtectiveMaskItem> MASK_RAG;
    public static final DeferredItem<ProtectiveMaskItem> MASK_PISS;
    public static final DeferredItem<BlockItem> MACHINE_ARMOR_TABLE_ITEM;
    public static final DeferredItem<ArmorCladdingItem> CLADDING_PAINT;
    public static final DeferredItem<ArmorCladdingItem> CLADDING_RUBBER;
    public static final DeferredItem<ArmorCladdingItem> CLADDING_LEAD;
    public static final DeferredItem<ArmorCladdingItem> CLADDING_DESH;
    public static final DeferredItem<ArmorCladdingItem> CLADDING_GHIORSIUM;
    public static final DeferredItem<ArmorCladdingItem> CLADDING_IRON;
    public static final DeferredItem<ArmorCladdingItem> CLADDING_OBSIDIAN;
    public static final DeferredItem<Item> PLATE_POLYMER;
    public static final DeferredItem<WireFineItem> WIRE_FINE;
    public static final DeferredItem<DenseWireItem> WIRE_DENSE;
    public static final DeferredItem<CasingItem> CASING;
    public static final Map<FoundryPartItem.PartType, DeferredItem<FoundryPartItem>> FOUNDRY_PARTS;
    public static final DeferredItem<BoltItem> BOLT;
    public static final DeferredItem<CastPlateItem> PLATE_CAST;
    public static final DeferredItem<WeldedPlateItem> PLATE_WELDED;
    public static final DeferredItem<ArcElectrodeItem> ARC_ELECTRODE;
    public static final DeferredItem<ArcElectrodeBurntItem> ARC_ELECTRODE_BURNT;
    public static final DeferredItem<Item> POWDER_FLUX;
    public static final DeferredItem<Item> BALL_FIRECLAY;
    public static final DeferredItem<Item> MOLD_BASE;
    public static final DeferredItem<FoundryMoldItem> MOLD;
    public static final DeferredItem<FoundryScrapsItem> SCRAPS;
    public static final DeferredItem<FoundryIngotItem> INGOT_RAW;
    public static final DeferredItem<ScrewdriverItem> SCREWDRIVER;
    public static final DeferredItem<BlowtorchItem> BLOWTORCH;
    public static final DeferredItem<Item> PART_GENERIC;
    public static final DeferredItem<ShellItem> SHELL;
    public static final DeferredItem<FluidIdentifierItem> FLUID_IDENTIFIER_MULTI;
    public static final DeferredItem<CircuitItem> CIRCUIT;
    public static final DeferredItem<Item> CRT_DISPLAY;
    public static final DeferredItem<Item> REACTOR_CORE;
    public static final DeferredItem<Item> COIL_COPPER;
    public static final DeferredItem<Item> COIL_COPPER_TORUS;
    public static final DeferredItem<Item> COIL_GOLD;
    public static final DeferredItem<Item> COIL_GOLD_TORUS;
    public static final DeferredItem<Item> COIL_TUNGSTEN;
    public static final DeferredItem<Item> TANK_STEEL;
    public static final DeferredItem<PipeItem> PIPE;
    public static final DeferredItem<Item> FLUID_TANK_EMPTY;
    public static final DeferredItem<UniversalFluidTankItem> FLUID_TANK_FULL;
    public static final DeferredItem<Item> MOTOR;
    public static final DeferredItem<Item> MOTOR_DESH;
    public static final DeferredItem<Item> MAGNETRON;
    public static final DeferredItem<Item> DRILL_TITANIUM;
    public static final DeferredItem<BlockItem> BLOCK_INSULATOR_ITEM;
    public static final DeferredItem<BlockItem> SANDBAGS_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_IRON_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_LEAD_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_STEEL_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_DESH_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_FERROURANIUM_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_SATURNITE_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_BISMUTH_BRONZE_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_ARSENIC_BRONZE_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_SCHRABIDATE_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_DNT_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_OSMIRIDIUM_ITEM;
    public static final DeferredItem<NtmAnvilBlockItem> ANVIL_MURKY_ITEM;
    public static final DeferredItem<DescriptionBlockItem> HEATER_ELECTRIC_ITEM;
    public static final DeferredItem<DescriptionBlockItem> HEATER_OILBURNER_ITEM;
    public static final DeferredItem<DescriptionBlockItem> HEATER_HEATEX_ITEM;
    public static final DeferredItem<DescriptionBlockItem> MACHINE_BLAST_FURNACE_ITEM;
    public static final DeferredItem<DescriptionBlockItem> FURNACE_COMBINATION_ITEM;
    public static final DeferredItem<DescriptionBlockItem> FURNACE_STEEL_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_ELECTRIC_FURNACE_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_FURNACE_BRICK_ITEM;
    public static final DeferredItem<DescriptionBlockItem> MACHINE_WOOD_BURNER_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_MICROWAVE_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_ASSEMBLY_MACHINE_ITEM;
    public static final DeferredItem<DieselGeneratorBlockItem> MACHINE_DIESEL_ITEM;
    public static final DeferredItem<CombustionEngineBlockItem> MACHINE_COMBUSTION_ENGINE_ITEM;
    public static final DeferredItem<PistonSetItem> PISTON_SET_STEEL;
    public static final DeferredItem<PistonSetItem> PISTON_SET_DURA;
    public static final DeferredItem<PistonSetItem> PISTON_SET_DESH;
    public static final DeferredItem<PistonSetItem> PISTON_SET_STARMETAL;
    public static final DeferredItem<FluidStorageTankBlockItem> MACHINE_FLUIDTANK_ITEM;
    public static final DeferredItem<BlockItem> DFC_CORE_ITEM;
    public static final DeferredItem<BlockItem> DFC_EMITTER_ITEM;
    public static final DeferredItem<BlockItem> DFC_INJECTOR_ITEM;
    public static final DeferredItem<BlockItem> DFC_RECEIVER_ITEM;
    public static final DeferredItem<BlockItem> DFC_STABILIZER_ITEM;
    public static final DeferredItem<Item> AMS_CATALYST_BLANK;
    public static final Map<String, DeferredItem<DfcCatalystItem>> DFC_CATALYSTS;
    public static final DeferredItem<DfcLensItem> AMS_LENS;
    public static final DeferredItem<DfcCoreItem> AMS_CORE_SING;
    public static final DeferredItem<DfcCoreItem> AMS_CORE_WORMHOLE;
    public static final DeferredItem<DfcCoreItem> AMS_CORE_EYEOFHARMONY;
    public static final DeferredItem<DfcCoreItem> AMS_CORE_THINGY;
    public static final DeferredItem<BlockItem> MACHINE_CHEMICAL_PLANT_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_SOLDERING_STATION_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_TRANSFORMER_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_ARC_WELDER_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_ARC_FURNACE_ITEM;
    public static final DeferredItem<RefineryBlockItem> MACHINE_REFINERY_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_CENTRIFUGE_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_CATALYTIC_CRACKER_ITEM;
    public static final DeferredItem<BlockItem> MACHINE_FRACTION_TOWER_ITEM;
    public static final DeferredItem<BlockItem> FRACTION_SPACER_ITEM;
    public static final DeferredItem<Item> CENTRIFUGE_ELEMENT;
    public static final DeferredItem<BlockItem> MACHINE_CRUCIBLE_ITEM;
    public static final DeferredItem<BlockItem> FOUNDRY_MOLD_ITEM;
    public static final DeferredItem<BlockItem> FOUNDRY_BASIN_ITEM;
    public static final DeferredItem<BlockItem> FOUNDRY_CHANNEL_ITEM;
    public static final DeferredItem<BlockItem> FOUNDRY_TANK_ITEM;
    public static final DeferredItem<BlockItem> FOUNDRY_OUTLET_ITEM;
    public static final DeferredItem<BlockItem> FOUNDRY_SLAGTAP_ITEM;
    public static final DeferredItem<BlockItem> CONCRETE_SMOOTH_ITEM;
    public static final DeferredItem<ScaffoldBlockItem> STEEL_SCAFFOLD_ITEM;
    public static final DeferredItem<BlockItem> STEEL_BEAM_ITEM;
    public static final DeferredItem<BlockItem> STEEL_GRATE_ITEM;
    public static final DeferredItem<ConveyorWandItem> CONVEYOR_WAND;
    public static final DeferredItem<ConveyorWandItem> CONVEYOR_WAND_EXPRESS;
    public static final DeferredItem<ConveyorWandItem> CONVEYOR_WAND_DOUBLE;
    public static final DeferredItem<ConveyorWandItem> CONVEYOR_WAND_TRIPLE;
    public static final DeferredItem<DescriptionBlockItem> CRANE_EXTRACTOR_ITEM;
    public static final DeferredItem<DescriptionBlockItem> CRANE_INSERTER_ITEM;
    public static final DeferredItem<BlockItem> CRANE_BOXER_ITEM;
    public static final DeferredItem<BlueprintItem> BLUEPRINTS;
    public static final Map<String, DeferredItem<MachineUpgradeItem>> MACHINE_UPGRADES;
    public static final Map<String, DeferredItem<StampItem>> STAMPS;

    static {
        Map<String, DeferredItem<? extends Item>> items = new LinkedHashMap<>();

        for (HazardousMaterialDefinitions.ItemDefinition definition : HazardousMaterialDefinitions.ITEMS) {
            Item.Properties properties = new Item.Properties();
            if ("schrabidium".equals(definition.commonMaterial())) {
                properties.rarity(net.minecraft.world.item.Rarity.RARE);
            }
            items.put(
                    definition.id(),
                    ITEMS.register(definition.id(), () -> new HazardousItem(properties, definition.hazards()))
            );
        }

        for (MaterialDefinitions.ItemDefinition definition : MaterialDefinitions.ITEMS) {
            if (definition.id().equals("dust")) {
                items.put(definition.id(), ITEMS.register(definition.id(),
                        () -> new CustomLoreItem("item.hbm.dust.desc")));
            } else if (definition.id().equals("ingot_tantalium")
                    || definition.id().equals("nugget_tantalium")
                    || definition.id().equals("gem_tantalium")
                    || definition.id().equals("powder_tantalium")) {
                items.put(definition.id(), ITEMS.register(definition.id(),
                        () -> new CustomLoreItem("item.hbm." + definition.id() + ".desc")));
            } else if (definition.id().equals("ingot_combine_steel")) {
                items.put(definition.id(), ITEMS.register(definition.id(),
                        () -> new CustomLoreItem("item.hbm.ingot_combine_steel.desc")));
            } else if (definition.id().equals("powder_cement")) {
                items.put(definition.id(), ITEMS.registerSimpleItem(definition.id(), new Item.Properties().food(
                        new FoodProperties.Builder().nutrition(2).saturationModifier(0.5F).build())));
            } else {
                Item.Properties properties = new Item.Properties();
                if (definition.id().equals("powder_niobium")) {
                    properties.rarity(net.minecraft.world.item.Rarity.EPIC);
                }
                items.put(definition.id(), ITEMS.registerSimpleItem(definition.id(), properties));
            }
        }

        MATERIAL_ITEMS = Collections.unmodifiableMap(items);

        Map<String, DeferredItem<? extends BlockItem>> blockItems = new LinkedHashMap<>();
        for (HazardousMaterialDefinitions.BlockDefinition definition : HazardousMaterialDefinitions.BLOCKS) {
            blockItems.put(
                    definition.id(),
                    ITEMS.register(
                            definition.id(),
                            () -> new HazardousBlockItem(
                                    ModBlocks.get(definition.id()).get(),
                                    new Item.Properties(),
                                    definition.hazards()
                            )
                    )
            );
        }
        for (MaterialDefinitions.BlockDefinition definition : MaterialDefinitions.BLOCKS) {
            blockItems.put(
                    definition.id(),
                    ITEMS.registerSimpleBlockItem(definition.id(), ModBlocks.get(definition.id()))
            );
        }
        MATERIAL_BLOCK_ITEMS = Collections.unmodifiableMap(blockItems);

        URANIUM_INGOT = get("ingot_uranium");
        URANIUM_BLOCK_ITEM = getBlockItem("block_uranium");
        ORE_TITANIUM_ITEM = ITEMS.registerSimpleBlockItem("ore_titanium", ModBlocks.ORE_TITANIUM);
        ORE_TUNGSTEN_ITEM = ITEMS.registerSimpleBlockItem("ore_tungsten", ModBlocks.ORE_TUNGSTEN);
        ORE_COBALT_ITEM = ITEMS.registerSimpleBlockItem("ore_cobalt", ModBlocks.ORE_COBALT);
        ORE_RARE_ITEM = ITEMS.registerSimpleBlockItem("ore_rare", ModBlocks.ORE_RARE);
        ORE_COLTAN_ITEM = ITEMS.registerSimpleBlockItem("ore_coltan", ModBlocks.ORE_COLTAN);
        STONE_RESOURCE_ITEM = ITEMS.register("stone_resource",
                () -> new StoneResourceBlockItem(ModBlocks.STONE_RESOURCE.get(), new Item.Properties()));
        Map<String, DeferredItem<Item>> oreResources = new LinkedHashMap<>();
        for (String id : new String[]{"lignite", "cinnebar", "crystal_iron", "crystal_titanium",
                "crystal_aluminium", "crystal_copper", "crystal_tungsten", "fragment_neodymium",
                "powder_borax", "gem_alexandrite", "powder_fire", "powder_ice", "coal_infernal"}) {
            oreResources.put(id, ITEMS.registerSimpleItem(id, new Item.Properties()));
        }
        LEGACY_ORE_RESOURCE_ITEMS = Collections.unmodifiableMap(oreResources);

        Map<String, DeferredItem<BlockItem>> legacyOreItems = new LinkedHashMap<>();
        ModBlocks.LEGACY_ORE_BLOCKS.forEach((id, block) -> {
            boolean depthRock = id.startsWith("stone_depth") || id.startsWith("cluster_depth_")
                    || id.startsWith("ore_depth_") || id.equals("ore_alexandrite");
            legacyOreItems.put(id, depthRock
                    ? ITEMS.<BlockItem>register(id,
                            () -> new DepthRockBlockItem(block.get(), new Item.Properties()))
                    : ITEMS.registerSimpleBlockItem(id, block));
        });
        LEGACY_ORE_BLOCK_ITEMS = Collections.unmodifiableMap(legacyOreItems);
        ORE_OIL_ITEM = ITEMS.registerSimpleBlockItem("ore_oil", ModBlocks.ORE_OIL);
        ORE_OIL_EMPTY_ITEM = ITEMS.registerSimpleBlockItem("ore_oil_empty", ModBlocks.ORE_OIL_EMPTY);
        DIRT_OILY_ITEM = ITEMS.registerSimpleBlockItem("dirt_oily", ModBlocks.DIRT_OILY);
        DIRT_DEAD_ITEM = ITEMS.registerSimpleBlockItem("dirt_dead", ModBlocks.DIRT_DEAD);
        SAND_DIRTY_ITEM = ITEMS.registerSimpleBlockItem("sand_dirty", ModBlocks.SAND_DIRTY);
        SAND_DIRTY_RED_ITEM = ITEMS.registerSimpleBlockItem("sand_dirty_red", ModBlocks.SAND_DIRTY_RED);
        STONE_CRACKED_ITEM = ITEMS.registerSimpleBlockItem("stone_cracked", ModBlocks.STONE_CRACKED);
        PLANT_DEAD_ITEM = ITEMS.registerSimpleBlockItem("plant_dead", ModBlocks.PLANT_DEAD);
        OIL_SPILL_ITEM = ITEMS.registerSimpleBlockItem("oil_spill", ModBlocks.OIL_SPILL);
        TRINITITE = ITEMS.register("trinitite", () -> new HazardousItem(
                new Item.Properties(), HazardProfile.radiation(0.1F)));
        BURNT_BARK = ITEMS.register("burnt_bark",
                () -> new CustomLoreItem("item.hbm.burnt_bark.desc"));
        WASTE_EARTH_ITEM = ITEMS.registerSimpleBlockItem("waste_earth", ModBlocks.WASTE_EARTH);
        WASTE_MYCELIUM_ITEM = ITEMS.registerSimpleBlockItem("waste_mycelium", ModBlocks.WASTE_MYCELIUM);
        WASTE_TRINITITE_ITEM = ITEMS.registerSimpleBlockItem("waste_trinitite", ModBlocks.WASTE_TRINITITE);
        WASTE_TRINITITE_RED_ITEM = ITEMS.registerSimpleBlockItem(
                "waste_trinitite_red", ModBlocks.WASTE_TRINITITE_RED);
        WASTE_LOG_ITEM = ITEMS.registerSimpleBlockItem("waste_log", ModBlocks.WASTE_LOG);
        WASTE_PLANKS_ITEM = ITEMS.registerSimpleBlockItem("waste_planks", ModBlocks.WASTE_PLANKS);
        FROZEN_DIRT_ITEM = ITEMS.registerSimpleBlockItem("frozen_dirt", ModBlocks.FROZEN_DIRT);
        FROZEN_GRASS_ITEM = ITEMS.registerSimpleBlockItem("frozen_grass", ModBlocks.FROZEN_GRASS);
        FROZEN_LOG_ITEM = ITEMS.registerSimpleBlockItem("frozen_log", ModBlocks.FROZEN_LOG);
        FROZEN_PLANKS_ITEM = ITEMS.registerSimpleBlockItem("frozen_planks", ModBlocks.FROZEN_PLANKS);
        BLOCK_TRINITITE_ITEM = ITEMS.register("block_trinitite", () -> new HazardousBlockItem(
                ModBlocks.BLOCK_TRINITITE.get(), new Item.Properties(), HazardProfile.radiation(1.0F)));
        BLOCK_WASTE_ITEM = ITEMS.register("block_waste", () -> new HazardousBlockItem(
                ModBlocks.BLOCK_WASTE.get(), new Item.Properties(), HazardProfile.radiation(150.0F)));
        SELLAFIELD_ITEM = ITEMS.register("sellafield",
                () -> new SellafieldBlockItem(ModBlocks.SELLAFIELD.get(), new Item.Properties()));
        SELLAFIELD_SLAKED_ITEM = ITEMS.registerSimpleBlockItem(
                "sellafield_slaked", ModBlocks.SELLAFIELD_SLAKED);
        OIL_TAR = ITEMS.registerSimpleItem("oil_tar", new Item.Properties());
        COKE_COAL = ITEMS.registerSimpleItem("coke_coal", new Item.Properties());
        COKE_LIGNITE = ITEMS.registerSimpleItem("coke_lignite", new Item.Properties());
        COKE_PETROLEUM = ITEMS.registerSimpleItem("coke_petroleum", new Item.Properties());
        BLOCK_COKE_COAL_ITEM = ITEMS.registerSimpleBlockItem("block_coke_coal", ModBlocks.BLOCK_COKE_COAL);
        BLOCK_COKE_LIGNITE_ITEM = ITEMS.registerSimpleBlockItem("block_coke_lignite", ModBlocks.BLOCK_COKE_LIGNITE);
        BLOCK_COKE_PETROLEUM_ITEM = ITEMS.registerSimpleBlockItem("block_coke_petroleum", ModBlocks.BLOCK_COKE_PETROLEUM);
        CHUNK_ORE = ITEMS.register("chunk_ore", OreChunkItem::new);
        CANISTER_EMPTY = ITEMS.registerSimpleItem("canister_empty", new Item.Properties());
        CANISTER_FULL = ITEMS.register("canister_full",
                () -> new SourceFluidContainerItem(SourceFluidContainerItem.ContainedFluid.OIL,
                        CANISTER_EMPTY::get));
        GAS_EMPTY = ITEMS.registerSimpleItem("gas_empty", new Item.Properties());
        GAS_FULL = ITEMS.register("gas_full",
                () -> new SourceFluidContainerItem(SourceFluidContainerItem.ContainedFluid.GAS,
                        GAS_EMPTY::get));
        CELL_EMPTY = ITEMS.registerSimpleItem("cell_empty", new Item.Properties());
        CELL_TRITIUM = ITEMS.register("cell_tritium", () -> new FluidCellItem(
                new Item.Properties(), HazardProfile.radiation(0.001F), CELL_EMPTY::get));
        CELL_SAS3 = ITEMS.register("cell_sas3", () -> new FluidCellItem(
                new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE),
                HazardProfile.radiation(5.0F).withBlinding(60.0F), CELL_EMPTY::get));
        MACHINE_WELL_ITEM = ITEMS.register("machine_well",
                () -> new OilDerrickBlockItem(ModBlocks.MACHINE_WELL.get(), new Item.Properties()));
        RADIO_TORCH_SENDER_ITEM = ITEMS.registerSimpleBlockItem("radio_torch_sender", ModBlocks.RADIO_TORCH_SENDER);
        RADIO_TORCH_RECEIVER_ITEM = ITEMS.registerSimpleBlockItem("radio_torch_receiver", ModBlocks.RADIO_TORCH_RECEIVER);
        RADIO_TORCH_COUNTER_ITEM = ITEMS.registerSimpleBlockItem("radio_torch_counter", ModBlocks.RADIO_TORCH_COUNTER);
        RADIO_TORCH_LOGIC_ITEM = ITEMS.registerSimpleBlockItem("radio_torch_logic", ModBlocks.RADIO_TORCH_LOGIC);
        RADIO_TORCH_READER_ITEM = ITEMS.registerSimpleBlockItem("radio_torch_reader", ModBlocks.RADIO_TORCH_READER);
        RADIO_TORCH_CONTROLLER_ITEM = ITEMS.registerSimpleBlockItem("radio_torch_controller", ModBlocks.RADIO_TORCH_CONTROLLER);
        DFC_CORE_ITEM = ITEMS.registerSimpleBlockItem("dfc_core", ModBlocks.DFC_CORE);
        DFC_EMITTER_ITEM = ITEMS.registerSimpleBlockItem("dfc_emitter", ModBlocks.DFC_EMITTER);
        DFC_INJECTOR_ITEM = ITEMS.registerSimpleBlockItem("dfc_injector", ModBlocks.DFC_INJECTOR);
        DFC_RECEIVER_ITEM = ITEMS.registerSimpleBlockItem("dfc_receiver", ModBlocks.DFC_RECEIVER);
        DFC_STABILIZER_ITEM = ITEMS.registerSimpleBlockItem("dfc_stabilizer", ModBlocks.DFC_STABILIZER);
        AMS_CATALYST_BLANK = ITEMS.registerSimpleItem("ams_catalyst_blank", new Item.Properties().stacksTo(1));
        Map<String, DeferredItem<DfcCatalystItem>> dfcCatalysts = new LinkedHashMap<>();
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_aluminium", 0xCCCCCC, 1_000_000, 1.15F, 0.85F, 1.15F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_beryllium", 0x97978B, 0, 1.25F, 0.95F, 1.05F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_caesium", 0x6400FF, 2_500_000, 1.00F, 0.85F, 1.15F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_cerium", 0x1D3FFF, 1_000_000, 1.15F, 1.15F, 0.85F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_cobalt", 0x789BBE, 0, 1.25F, 1.05F, 0.95F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_copper", 0xAADE29, 0, 1.25F, 1.00F, 1.00F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_dineutronium", 0x334077, 2_500_000, 1.00F, 1.15F, 0.85F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_euphemium", 0xFF9CD2, 2_500_000, 1.00F, 1.00F, 1.00F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_iron", 0xFF7E22, 1_000_000, 1.15F, 0.95F, 1.05F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_lithium", 0xFF2727, 0, 1.25F, 0.85F, 1.15F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_niobium", 0x3BF1B6, 1_000_000, 1.15F, 1.05F, 0.95F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_schrabidium", 0x32FFFF, 2_500_000, 1.00F, 1.05F, 0.95F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_strontium", 0xDD0D35, 1_000_000, 1.15F, 1.00F, 1.00F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_thorium", 0x653B22, 2_500_000, 1.00F, 0.95F, 1.05F);
        registerDfcCatalyst(dfcCatalysts, "ams_catalyst_tungsten", 0xF5FF48, 0, 1.25F, 1.15F, 0.85F);
        DFC_CATALYSTS = Collections.unmodifiableMap(dfcCatalysts);
        AMS_LENS = ITEMS.register("ams_lens", DfcLensItem::new);
        AMS_CORE_SING = ITEMS.register("ams_core_sing", () -> new DfcCoreItem(
                DfcCoreItem.Kind.SINGULARITY, 1_000_000_000L, 200, 10, 500));
        AMS_CORE_WORMHOLE = ITEMS.register("ams_core_wormhole", () -> new DfcCoreItem(
                DfcCoreItem.Kind.WORMHOLE, 1_500_000_000L, 200, 15, 650));
        AMS_CORE_EYEOFHARMONY = ITEMS.register("ams_core_eyeofharmony", () -> new DfcCoreItem(
                DfcCoreItem.Kind.EYE_OF_HARMONY, 2_500_000_000L, 300, 10, 800));
        AMS_CORE_THINGY = ITEMS.register("ams_core_thingy", () -> new DfcCoreItem(
                DfcCoreItem.Kind.THINGY, 5_000_000_000L, 250, 5, 2_500));
        RED_CABLE_ITEM = ITEMS.registerSimpleBlockItem("red_cable", ModBlocks.RED_CABLE);
        MACHINE_PRESS_ITEM = ITEMS.registerSimpleBlockItem("machine_press", ModBlocks.MACHINE_PRESS);
        PRESS_PREHEATER_ITEM = ITEMS.registerSimpleBlockItem("press_preheater", ModBlocks.PRESS_PREHEATER);
        MACHINE_SHREDDER_ITEM = ITEMS.registerSimpleBlockItem("machine_shredder", ModBlocks.MACHINE_SHREDDER);
        GRAVEL_OBSIDIAN_ITEM = ITEMS.register("gravel_obsidian",
                () -> new BlastInfoBlockItem(ModBlocks.GRAVEL_OBSIDIAN.get(), new Item.Properties(), 144.0F));
        GRAVEL_DIAMOND_ITEM = ITEMS.registerSimpleBlockItem("gravel_diamond", ModBlocks.GRAVEL_DIAMOND);
        BLADES_STEEL = ITEMS.register("blades_steel", () -> new ShredderBladeItem(400));
        BLADES_TITANIUM = ITEMS.register("blades_titanium", () -> new ShredderBladeItem(500));
        BLADES_DESH = ITEMS.register("blades_desh", () -> new ShredderBladeItem(0));
        BLADE_TITANIUM = ITEMS.registerSimpleItem("blade_titanium", new Item.Properties());
        TURBINE_TITANIUM = ITEMS.registerSimpleItem("turbine_titanium", new Item.Properties());
        BLADE_TUNGSTEN = ITEMS.registerSimpleItem("blade_tungsten", new Item.Properties());
        TURBINE_TUNGSTEN = ITEMS.registerSimpleItem("turbine_tungsten", new Item.Properties());
        FLAME_PONY = ITEMS.register("flame_pony", FlamePonyItem::new);
        BATTERY_CREATIVE = ITEMS.register("battery_creative", InfiniteBatteryItem::new);
        FLUID_BARREL_INFINITE = ITEMS.register("fluid_barrel_infinite", InfiniteFluidBarrelItem::new);
        BATTERY_PACK = ITEMS.register("battery_pack", BatteryPackItem::new);
        MACHINE_BATTERY_SOCKET_ITEM = ITEMS.register("machine_battery_socket",
                () -> new DescriptionBlockItem(ModBlocks.MACHINE_BATTERY_SOCKET.get(), new Item.Properties(),
                        "block.hbm.machine_battery_socket.desc.0",
                        "block.hbm.machine_battery_socket.desc.1",
                        "block.hbm.machine_battery_socket.desc.2",
                        "block.hbm.machine_battery_socket.desc.3"));
        MACHINE_BATTERY_REDD_ITEM = ITEMS.register("machine_battery_redd",
                () -> new FensuBlockItem(ModBlocks.MACHINE_BATTERY_REDD.get(), new Item.Properties().stacksTo(1)));
        GEAR_LARGE = ITEMS.registerSimpleItem("gear_large", new Item.Properties());
        SAWBLADE = ITEMS.registerSimpleItem("sawblade", new Item.Properties());
        POWDER_SAWDUST = ITEMS.registerSimpleItem("powder_sawdust", new Item.Properties());
        SOLID_FUEL = ITEMS.registerSimpleItem("solid_fuel", new Item.Properties());
        SOLID_FUEL_PRESTO = ITEMS.registerSimpleItem("solid_fuel_presto", new Item.Properties());
        SOLID_FUEL_PRESTO_TRIPLET = ITEMS.registerSimpleItem("solid_fuel_presto_triplet", new Item.Properties());
        SOLID_FUEL_BF = ITEMS.registerSimpleItem("solid_fuel_bf", new Item.Properties());
        SOLID_FUEL_PRESTO_BF = ITEMS.registerSimpleItem("solid_fuel_presto_bf", new Item.Properties());
        SOLID_FUEL_PRESTO_TRIPLET_BF = ITEMS.registerSimpleItem("solid_fuel_presto_triplet_bf", new Item.Properties());
        ROCKET_FUEL = ITEMS.registerSimpleItem("rocket_fuel", new Item.Properties());
        POWDER_ASH = ITEMS.register("powder_ash", AshItem::new);
        HEATER_FIREBOX_ITEM = ITEMS.register("heater_firebox",
                () -> new DescriptionBlockItem(ModBlocks.HEATER_FIREBOX.get(), new Item.Properties(),
                        "block.hbm.heater_firebox.desc.0"));
        HEATER_OVEN_ITEM = ITEMS.register("heater_oven",
                () -> new DescriptionBlockItem(ModBlocks.HEATER_OVEN.get(), new Item.Properties(),
                        "block.hbm.heater_oven.desc.0", "block.hbm.heater_oven.desc.1"));
        MACHINE_ASHPIT_ITEM = ITEMS.register("machine_ashpit",
                () -> new DescriptionBlockItem(ModBlocks.MACHINE_ASHPIT.get(), new Item.Properties(),
                        "block.hbm.machine_ashpit.desc.0"));
        MACHINE_STIRLING_ITEM = ITEMS.register("machine_stirling",
                () -> new StirlingMachineBlockItem(ModBlocks.MACHINE_STIRLING.get(), new Item.Properties()));
        MACHINE_SAWMILL_ITEM = ITEMS.register("machine_sawmill",
                () -> new SawmillMachineBlockItem(ModBlocks.MACHINE_SAWMILL.get(), new Item.Properties()));
        MACHINE_STEAM_ENGINE_ITEM = ITEMS.register("machine_steam_engine",
                () -> new DescriptionBlockItem(ModBlocks.MACHINE_STEAM_ENGINE.get(), new Item.Properties(),
                        "block.hbm.machine_steam_engine.desc.0"));
        MACHINE_INDUSTRIAL_TURBINE_ITEM = ITEMS.register("machine_industrial_turbine",
                () -> new DescriptionBlockItem(ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get(), new Item.Properties(),
                        "block.hbm.machine_industrial_turbine.desc.0"));
        MACHINE_TURBINE_GAS_ITEM = ITEMS.register("machine_turbinegas",
                () -> new DescriptionBlockItem(ModBlocks.MACHINE_TURBINE_GAS.get(), new Item.Properties()));
        MACHINE_TURBOFAN_ITEM = ITEMS.register("machine_turbofan",
                () -> new TurbofanBlockItem(ModBlocks.MACHINE_TURBOFAN.get(), new Item.Properties()));
        MACHINE_TURBINE_ITEM = ITEMS.register("machine_turbine",
                () -> new DescriptionBlockItem(ModBlocks.MACHINE_TURBINE.get(), new Item.Properties(),
                        "block.hbm.machine_turbine.desc.0"));
        REACTOR_ZIRNOX_ITEM = ITEMS.register("machine_zirnox",
                () -> new DescriptionBlockItem(ModBlocks.REACTOR_ZIRNOX.get(), new Item.Properties().stacksTo(1),
                        "block.hbm.machine_zirnox.desc.0", "block.hbm.machine_zirnox.desc.1"));
        MACHINE_REACTOR_BREEDING_ITEM = ITEMS.registerSimpleBlockItem(
                "machine_reactor_breeding", ModBlocks.MACHINE_REACTOR_BREEDING);
        REACTOR_RESEARCH_ITEM = ITEMS.register("machine_reactor_small",
                () -> new ResearchReactorBlockItem(ModBlocks.REACTOR_RESEARCH.get(), new Item.Properties()));
        MACHINE_RADGEN_ITEM = ITEMS.registerSimpleBlockItem("machine_radgen", ModBlocks.MACHINE_RADGEN);
        MACHINE_WASTE_DRUM_ITEM = ITEMS.registerSimpleBlockItem("machine_waste_drum", ModBlocks.MACHINE_WASTE_DRUM);
        MACHINE_SIREN_ITEM = ITEMS.registerSimpleBlockItem("machine_siren", ModBlocks.MACHINE_SIREN);
        SIREN_TRACK = ITEMS.register("siren_track", SirenTrackItem::new);
        NUCLEAR_WASTE_LONG = ITEMS.register("nuclear_waste_long", () -> new NuclearWasteItem(
                new Item.Properties(), NuclearWasteItem.Family.LONG, false, false));
        NUCLEAR_WASTE_LONG_TINY = ITEMS.register("nuclear_waste_long_tiny", () -> new NuclearWasteItem(
                new Item.Properties(), NuclearWasteItem.Family.LONG, true, false));
        NUCLEAR_WASTE_SHORT = ITEMS.register("nuclear_waste_short", () -> new NuclearWasteItem(
                new Item.Properties(), NuclearWasteItem.Family.SHORT, false, false));
        NUCLEAR_WASTE_SHORT_TINY = ITEMS.register("nuclear_waste_short_tiny", () -> new NuclearWasteItem(
                new Item.Properties(), NuclearWasteItem.Family.SHORT, true, false));
        NUCLEAR_WASTE_LONG_DEPLETED = ITEMS.register("nuclear_waste_long_depleted", () -> new NuclearWasteItem(
                new Item.Properties(), NuclearWasteItem.Family.LONG, false, true));
        NUCLEAR_WASTE_LONG_DEPLETED_TINY = ITEMS.register("nuclear_waste_long_depleted_tiny", () -> new NuclearWasteItem(
                new Item.Properties(), NuclearWasteItem.Family.LONG, true, true));
        NUCLEAR_WASTE_SHORT_DEPLETED = ITEMS.register("nuclear_waste_short_depleted", () -> new NuclearWasteItem(
                new Item.Properties(), NuclearWasteItem.Family.SHORT, false, true));
        NUCLEAR_WASTE_SHORT_DEPLETED_TINY = ITEMS.register("nuclear_waste_short_depleted_tiny", () -> new NuclearWasteItem(
                new Item.Properties(), NuclearWasteItem.Family.SHORT, true, true));
        SCRAP_NUCLEAR = ITEMS.register("scrap_nuclear", () -> new HazardousItem(
                new Item.Properties(), HazardProfile.radiation(1.0F)));
        GEM_RAD = ITEMS.register("gem_rad", () -> new HazardousItem(
                new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                HazardProfile.radiation(25.0F)));
        ROD_EMPTY = ITEMS.registerSimpleItem("rod_empty", new Item.Properties());
        ROD_DUAL_EMPTY = ITEMS.registerSimpleItem("rod_dual_empty", new Item.Properties());
        ROD_QUAD_EMPTY = ITEMS.registerSimpleItem("rod_quad_empty", new Item.Properties());
        ROD = ITEMS.register("rod",
                () -> new BreedingRodItem(new Item.Properties().stacksTo(1), BreedingRodItem.Form.SINGLE));
        ROD_DUAL = ITEMS.register("rod_dual",
                () -> new BreedingRodItem(new Item.Properties().stacksTo(1), BreedingRodItem.Form.DUAL));
        ROD_QUAD = ITEMS.register("rod_quad",
                () -> new BreedingRodItem(new Item.Properties().stacksTo(1), BreedingRodItem.Form.QUAD));
        PLATE_FUEL_U233 = ITEMS.register("plate_fuel_u233",
                () -> new PlateFuelItem(new Item.Properties(), PlateFuelItem.Type.U233));
        PLATE_FUEL_U235 = ITEMS.register("plate_fuel_u235",
                () -> new PlateFuelItem(new Item.Properties(), PlateFuelItem.Type.U235));
        PLATE_FUEL_MOX = ITEMS.register("plate_fuel_mox",
                () -> new PlateFuelItem(new Item.Properties(), PlateFuelItem.Type.MOX));
        PLATE_FUEL_PU239 = ITEMS.register("plate_fuel_pu239",
                () -> new PlateFuelItem(new Item.Properties(), PlateFuelItem.Type.PU239));
        PLATE_FUEL_SA326 = ITEMS.register("plate_fuel_sa326",
                () -> new PlateFuelItem(new Item.Properties(), PlateFuelItem.Type.SA326));
        PLATE_FUEL_RA226BE = ITEMS.register("plate_fuel_ra226be",
                () -> new PlateFuelItem(new Item.Properties(), PlateFuelItem.Type.RA226BE));
        PLATE_FUEL_PU238BE = ITEMS.register("plate_fuel_pu238be",
                () -> new PlateFuelItem(new Item.Properties(), PlateFuelItem.Type.PU238BE));
        WASTE_PLATE_U233 = ITEMS.register("waste_plate_u233",
                () -> new DepletedPlateFuelItem(new Item.Properties(), DepletedPlateFuelItem.Type.U233));
        WASTE_PLATE_U235 = ITEMS.register("waste_plate_u235",
                () -> new DepletedPlateFuelItem(new Item.Properties(), DepletedPlateFuelItem.Type.U235));
        WASTE_PLATE_MOX = ITEMS.register("waste_plate_mox",
                () -> new DepletedPlateFuelItem(new Item.Properties(), DepletedPlateFuelItem.Type.MOX));
        WASTE_PLATE_PU239 = ITEMS.register("waste_plate_pu239",
                () -> new DepletedPlateFuelItem(new Item.Properties(), DepletedPlateFuelItem.Type.PU239));
        WASTE_PLATE_SA326 = ITEMS.register("waste_plate_sa326",
                () -> new DepletedPlateFuelItem(new Item.Properties(), DepletedPlateFuelItem.Type.SA326));
        WASTE_PLATE_RA226BE = ITEMS.register("waste_plate_ra226be",
                () -> new DepletedPlateFuelItem(new Item.Properties(), DepletedPlateFuelItem.Type.RA226BE));
        WASTE_PLATE_PU238BE = ITEMS.register("waste_plate_pu238be",
                () -> new DepletedPlateFuelItem(new Item.Properties(), DepletedPlateFuelItem.Type.PU238BE));
        ROD_ZIRNOX_EMPTY = ITEMS.registerSimpleItem("rod_zirnox_empty", new Item.Properties());
        ROD_ZIRNOX_NATURAL_URANIUM_FUEL = zirnoxRod("rod_zirnox_natural_uranium_fuel", ZirnoxRodItem.Type.NATURAL_URANIUM_FUEL);
        ROD_ZIRNOX_URANIUM_FUEL = zirnoxRod("rod_zirnox_uranium_fuel", ZirnoxRodItem.Type.URANIUM_FUEL);
        ROD_ZIRNOX_TH232 = zirnoxRod("rod_zirnox_th232", ZirnoxRodItem.Type.TH232);
        ROD_ZIRNOX_THORIUM_FUEL = zirnoxRod("rod_zirnox_thorium_fuel", ZirnoxRodItem.Type.THORIUM_FUEL);
        ROD_ZIRNOX_MOX_FUEL = zirnoxRod("rod_zirnox_mox_fuel", ZirnoxRodItem.Type.MOX_FUEL);
        ROD_ZIRNOX_PLUTONIUM_FUEL = zirnoxRod("rod_zirnox_plutonium_fuel", ZirnoxRodItem.Type.PLUTONIUM_FUEL);
        ROD_ZIRNOX_U233_FUEL = zirnoxRod("rod_zirnox_u233_fuel", ZirnoxRodItem.Type.U233_FUEL);
        ROD_ZIRNOX_U235_FUEL = zirnoxRod("rod_zirnox_u235_fuel", ZirnoxRodItem.Type.U235_FUEL);
        ROD_ZIRNOX_LES_FUEL = zirnoxRod("rod_zirnox_les_fuel", ZirnoxRodItem.Type.LES_FUEL);
        ROD_ZIRNOX_LITHIUM = zirnoxRod("rod_zirnox_lithium", ZirnoxRodItem.Type.LITHIUM);
        ROD_ZIRNOX_ZFB_MOX = zirnoxRod("rod_zirnox_zfb_mox", ZirnoxRodItem.Type.ZFB_MOX);
        ROD_ZIRNOX_TRITIUM = ITEMS.registerSimpleItem("rod_zirnox_tritium", new Item.Properties().stacksTo(1));
        ROD_ZIRNOX_NATURAL_URANIUM_FUEL_DEPLETED = ITEMS.registerSimpleItem("rod_zirnox_natural_uranium_fuel_depleted", new Item.Properties().stacksTo(1));
        ROD_ZIRNOX_URANIUM_FUEL_DEPLETED = ITEMS.registerSimpleItem("rod_zirnox_uranium_fuel_depleted", new Item.Properties().stacksTo(1));
        ROD_ZIRNOX_THORIUM_FUEL_DEPLETED = ITEMS.registerSimpleItem("rod_zirnox_thorium_fuel_depleted", new Item.Properties().stacksTo(1));
        ROD_ZIRNOX_MOX_FUEL_DEPLETED = ITEMS.registerSimpleItem("rod_zirnox_mox_fuel_depleted", new Item.Properties().stacksTo(1));
        ROD_ZIRNOX_PLUTONIUM_FUEL_DEPLETED = ITEMS.registerSimpleItem("rod_zirnox_plutonium_fuel_depleted", new Item.Properties().stacksTo(1));
        ROD_ZIRNOX_U233_FUEL_DEPLETED = ITEMS.registerSimpleItem("rod_zirnox_u233_fuel_depleted", new Item.Properties().stacksTo(1));
        ROD_ZIRNOX_U235_FUEL_DEPLETED = ITEMS.registerSimpleItem("rod_zirnox_u235_fuel_depleted", new Item.Properties().stacksTo(1));
        ROD_ZIRNOX_LES_FUEL_DEPLETED = ITEMS.registerSimpleItem("rod_zirnox_les_fuel_depleted", new Item.Properties().stacksTo(1));
        ROD_ZIRNOX_ZFB_MOX_DEPLETED = ITEMS.registerSimpleItem("rod_zirnox_zfb_mox_depleted", new Item.Properties().stacksTo(1));
        PUMP_STEAM_ITEM = ITEMS.register("pump_steam",
                () -> new DescriptionBlockItem(ModBlocks.PUMP_STEAM.get(), new Item.Properties(),
                        "block.hbm.pump_steam.desc.0", "block.hbm.pump_steam.desc.1",
                        "block.hbm.pump_steam.desc.2"));
        PUMP_ELECTRIC_ITEM = ITEMS.register("pump_electric",
                () -> new DescriptionBlockItem(ModBlocks.PUMP_ELECTRIC.get(), new Item.Properties(),
                        "block.hbm.pump_electric.desc.0", "block.hbm.pump_electric.desc.1",
                        "block.hbm.pump_electric.desc.2"));
        MACHINE_INTAKE_ITEM = ITEMS.registerSimpleBlockItem("machine_intake", ModBlocks.MACHINE_INTAKE);
        MACHINE_CONDENSER_ITEM = ITEMS.registerSimpleBlockItem("machine_condenser", ModBlocks.MACHINE_CONDENSER);
        MACHINE_CONDENSER_POWERED_ITEM = ITEMS.registerSimpleBlockItem(
                "machine_condenser_powered", ModBlocks.MACHINE_CONDENSER_POWERED);
        REINFORCED_STONE_ITEM = ITEMS.registerSimpleBlockItem("reinforced_stone", ModBlocks.REINFORCED_STONE);
        REINFORCED_GLASS_ITEM = ITEMS.register("reinforced_glass",
                () -> new BlastInfoBlockItem(ModBlocks.REINFORCED_GLASS.get(), new Item.Properties(), 15.0F));
        REINFORCED_GLASS_PANE_ITEM = ITEMS.register("reinforced_glass_pane",
                () -> new BlastInfoBlockItem(ModBlocks.REINFORCED_GLASS_PANE.get(), new Item.Properties(), 15.0F));
        GNEISS_TILE_ITEM = ITEMS.registerSimpleBlockItem("gneiss_tile", ModBlocks.GNEISS_TILE);
        GNEISS_BRICK_ITEM = ITEMS.registerSimpleBlockItem("gneiss_brick", ModBlocks.GNEISS_BRICK);
        GNEISS_CHISELED_ITEM = ITEMS.registerSimpleBlockItem("gneiss_chiseled", ModBlocks.GNEISS_CHISELED);
        REINFORCED_LIGHT_ITEM = ITEMS.register("reinforced_light",
                () -> new BlastInfoBlockItem(ModBlocks.REINFORCED_LIGHT.get(), new Item.Properties(), 48.0F));
        REINFORCED_SAND_ITEM = ITEMS.register("reinforced_sand",
                () -> new BlastInfoBlockItem(ModBlocks.REINFORCED_SAND.get(), new Item.Properties(), 24.0F));
        DEPTH_BRICK_ITEM = ITEMS.register("depth_brick",
                () -> new DepthRockBlockItem(ModBlocks.DEPTH_BRICK.get(), new Item.Properties()));
        DEPTH_TILES_ITEM = ITEMS.register("depth_tiles",
                () -> new DepthRockBlockItem(ModBlocks.DEPTH_TILES.get(), new Item.Properties()));
        DEPTH_NETHER_BRICK_ITEM = ITEMS.register("depth_nether_brick",
                () -> new DepthRockBlockItem(ModBlocks.DEPTH_NETHER_BRICK.get(), new Item.Properties()));
        DEPTH_NETHER_TILES_ITEM = ITEMS.register("depth_nether_tiles",
                () -> new DepthRockBlockItem(ModBlocks.DEPTH_NETHER_TILES.get(), new Item.Properties()));
        MACHINE_BOILER_ITEM = ITEMS.registerSimpleBlockItem("machine_boiler", ModBlocks.MACHINE_BOILER);
        FLUID_DUCT_NEO_ITEM = ITEMS.registerSimpleBlockItem("fluid_duct_neo", ModBlocks.FLUID_DUCT_NEO);
        FLUID_DUCT = ITEMS.register("fluid_duct", () -> new FluidDuctItem(ModBlocks.FLUID_DUCT_NEO.get()));
        DYNAMITE_ITEM = ITEMS.registerSimpleBlockItem("dynamite", ModBlocks.DYNAMITE);
        TNT_NTM_ITEM = ITEMS.registerSimpleBlockItem("tnt_ntm", ModBlocks.TNT_NTM);
        SEMTEX_ITEM = ITEMS.registerSimpleBlockItem("semtex", ModBlocks.SEMTEX);
        C4_ITEM = ITEMS.registerSimpleBlockItem("c4", ModBlocks.C4);
        SAFETY_FUSE = ITEMS.registerSimpleItem("safety_fuse", new Item.Properties());
        BALL_DYNAMITE = registerExplosiveItem("ball_dynamite", 2.0F);
        STICK_DYNAMITE = registerExplosiveItem("stick_dynamite", 1.0F);
        STICK_TNT = registerExplosiveItem("stick_tnt", 1.5F);
        STICK_SEMTEX = registerExplosiveItem("stick_semtex", 2.5F);
        STICK_C4 = registerExplosiveItem("stick_c4", 2.5F);
        DUCTTAPE = ITEMS.registerSimpleItem("ducttape", new Item.Properties());
        DEFUSER = ITEMS.register("defuser", DefuserItem::new);
        RANGEFINDER = ITEMS.register("rangefinder", RangefinderItem::new);
        DETONATOR = ITEMS.register("detonator", DetonatorItem::new);
        DETONATOR_MULTI = ITEMS.register("detonator_multi", MultiDetonatorItem::new);
        DETONATOR_LASER = ITEMS.register("detonator_laser", LaserDetonatorItem::new);
        DETONATOR_DEADMAN = ITEMS.register("detonator_deadman", DeadMansDetonatorItem::new);
        DETONATOR_DE = ITEMS.register("detonator_de", DeadMansExplosiveItem::new);
        CHARGE_DYNAMITE_ITEM = ITEMS.register("charge_dynamite",
                () -> new ChargeBlockItem(ModBlocks.CHARGE_DYNAMITE.get(), new Item.Properties(),
                        com.hbm.ntm.block.ChargeType.DYNAMITE));
        CHARGE_MINER_ITEM = ITEMS.register("charge_miner",
                () -> new ChargeBlockItem(ModBlocks.CHARGE_MINER.get(), new Item.Properties(),
                        com.hbm.ntm.block.ChargeType.MINER));
        CHARGE_C4_ITEM = ITEMS.register("charge_c4",
                () -> new ChargeBlockItem(ModBlocks.CHARGE_C4.get(), new Item.Properties(),
                        com.hbm.ntm.block.ChargeType.C4));
        CHARGE_SEMTEX_ITEM = ITEMS.register("charge_semtex",
                () -> new ChargeBlockItem(ModBlocks.CHARGE_SEMTEX.get(), new Item.Properties(),
                        com.hbm.ntm.block.ChargeType.SEMTEX));
        // Landmines use plain BlockItems; their 2D inventory icon comes from the old
        // setBlockTextureName (textures/blocks/mine_*.png), since the block itself renders only via its TESR.
        MINE_AP_ITEM = ITEMS.registerSimpleBlockItem("mine_ap", ModBlocks.MINE_AP);
        MINE_HE_ITEM = ITEMS.registerSimpleBlockItem("mine_he", ModBlocks.MINE_HE);
        MINE_SHRAP_ITEM = ITEMS.registerSimpleBlockItem("mine_shrap", ModBlocks.MINE_SHRAP);
        MINE_FAT_ITEM = ITEMS.registerSimpleBlockItem("mine_fat", ModBlocks.MINE_FAT);
        MINE_NAVAL_ITEM = ITEMS.registerSimpleBlockItem("mine_naval", ModBlocks.MINE_NAVAL);
        EARLY_EXPLOSIVE_LENSES = ITEMS.register("early_explosive_lenses",
                () -> new CustomLoreItem(new Item.Properties().stacksTo(1),
                        "item.hbm.early_explosive_lenses.desc"));
        EXPLOSIVE_LENSES = ITEMS.register("explosive_lenses",
                () -> new CustomLoreItem(new Item.Properties().stacksTo(1),
                        "item.hbm.explosive_lenses.desc"));
        GADGET_WIREING = ITEMS.registerSimpleItem("gadget_wireing", new Item.Properties().stacksTo(1));
        GADGET_CORE = ITEMS.register("gadget_core", () -> new HazardousItem(
                new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                HazardProfile.NONE.withRadiation(5.0F)));
        BOY_SHIELDING = ITEMS.registerSimpleItem("boy_shielding", new Item.Properties().stacksTo(1));
        BOY_TARGET = ITEMS.register("boy_target", () -> new HazardousItem(
                new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                HazardProfile.NONE.withRadiation(2.0F)));
        BOY_BULLET = ITEMS.register("boy_bullet", () -> new HazardousItem(
                new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON),
                HazardProfile.NONE.withRadiation(1.0F)));
        BOY_PROPELLANT = ITEMS.register("boy_propellant", () -> new HazardousItem(
                new Item.Properties().stacksTo(1), HazardProfile.NONE.withExplosive(2.0F)));
        BOY_IGNITER = ITEMS.registerSimpleItem("boy_igniter", new Item.Properties().stacksTo(1));
        MAN_IGNITER = ITEMS.registerSimpleItem("man_igniter", new Item.Properties().stacksTo(1));
        MAN_CORE = ITEMS.register("man_core",
                () -> new RadioactiveItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.UNCOMMON), 5.0F));
        MIKE_CORE = ITEMS.register("mike_core", () -> new HazardousItem(
                new Item.Properties().stacksTo(1), HazardProfile.NONE.withRadiation(0.25F)));
        MIKE_DEUT = ITEMS.registerSimpleItem("mike_deut", new Item.Properties().stacksTo(1));
        MIKE_COOLING_UNIT = ITEMS.registerSimpleItem("mike_cooling_unit", new Item.Properties().stacksTo(1));
        TSAR_CORE = ITEMS.register("tsar_core", () -> new HazardousItem(
                new Item.Properties().stacksTo(1), HazardProfile.NONE.withRadiation(7.5F)));
        // Source items/bomb/ItemN2 ("Large Explosive Charge"): stack 1, a "Used in: N2 Mine" tooltip
        // and no carried hazard. Twelve of these fill the N2 Mine.
        N2_CHARGE = ITEMS.register("n2_charge", () -> new N2ChargeItem(new Item.Properties().stacksTo(1)));
        // Source items/bomb/ItemFleija registered as a group: igniter, propellant, core.
        // Hazards from HazardRegistry: igniter none; propellant rad 15 + explosive 8 + blinding 50;
        // core rad 10. Rarity: propellant RARE, igniter/core COMMON (ItemFleija.getRarity).
        FLEIJA_IGNITER = ITEMS.register("fleija_igniter", () -> new FleijaPartItem(
                new Item.Properties().stacksTo(1), HazardProfile.NONE));
        FLEIJA_PROPELLANT = ITEMS.register("fleija_propellant", () -> new FleijaPartItem(
                new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE),
                HazardProfile.NONE.withRadiation(15.0F).withExplosive(8.0F).withBlinding(50.0F)));
        FLEIJA_CORE = ITEMS.register("fleija_core", () -> new FleijaPartItem(
                new Item.Properties().stacksTo(1), HazardProfile.NONE.withRadiation(10.0F)));
        // Source items/bomb/ItemSolinium, all stack 1, all COMMON rarity (no getRarity override).
        // Hazards from HazardRegistry: igniter none; propellant explosive 10; core radiation
        // (sa327 17.5 * nugget 0.1 * 8 = 14.0) + blinding 45.
        SOLINIUM_IGNITER = ITEMS.register("solinium_igniter", () -> new SoliniumPartItem(
                new Item.Properties().stacksTo(1), HazardProfile.NONE));
        SOLINIUM_PROPELLANT = ITEMS.register("solinium_propellant", () -> new SoliniumPartItem(
                new Item.Properties().stacksTo(1), HazardProfile.NONE.withExplosive(10.0F)));
        SOLINIUM_CORE = ITEMS.register("solinium_core", () -> new SoliniumPartItem(
                new Item.Properties().stacksTo(1), HazardProfile.NONE.withRadiation(14.0F).withBlinding(45.0F)));
        FALLOUT = ITEMS.register("fallout",
                () -> new BlockItem(ModBlocks.FALLOUT.get(), new Item.Properties()));
        ASH_DIGAMMA_ITEM = ITEMS.registerSimpleBlockItem("ash_digamma", ModBlocks.ASH_DIGAMMA);
        PELLET_GAS = ITEMS.register("pellet_gas", () -> new CustomLoreItem(
                "item.hbm.pellet_gas.desc.0", "item.hbm.pellet_gas.desc.1"));
        VENT_CHLORINE_ITEM = ITEMS.registerSimpleBlockItem("vent_chlorine", ModBlocks.VENT_CHLORINE);
        CHLORINE_GAS_ITEM = ITEMS.registerSimpleBlockItem("chlorine_gas", ModBlocks.CHLORINE_GAS);
        VENT_CHLORINE_SEAL_ITEM = ITEMS.registerSimpleBlockItem(
                "vent_chlorine_seal", ModBlocks.VENT_CHLORINE_SEAL);
        NUKE_GADGET_ITEM = ITEMS.register("nuke_gadget",
                () -> new BlockItem(ModBlocks.NUKE_GADGET.get(), new Item.Properties()));
        NUKE_BOY_ITEM = ITEMS.register("nuke_boy",
                () -> new BlockItem(ModBlocks.NUKE_BOY.get(), new Item.Properties()));
        NUKE_MAN_ITEM = ITEMS.register("nuke_man",
                () -> new BlockItem(ModBlocks.NUKE_MAN.get(), new Item.Properties()));
        NUKE_MIKE_ITEM = ITEMS.register("nuke_mike",
                () -> new BlockItem(ModBlocks.NUKE_MIKE.get(), new Item.Properties()));
        NUKE_TSAR_ITEM = ITEMS.register("nuke_tsar",
                () -> new BlockItem(ModBlocks.NUKE_TSAR.get(), new Item.Properties()));
        NUKE_FLEIJA_ITEM = ITEMS.register("nuke_fleija",
                () -> new BlockItem(ModBlocks.NUKE_FLEIJA.get(), new Item.Properties()));
        NUKE_SOLINIUM_ITEM = ITEMS.register("nuke_solinium",
                () -> new BlockItem(ModBlocks.NUKE_SOLINIUM.get(), new Item.Properties()));
        NUKE_N2_ITEM = ITEMS.register("nuke_n2",
                () -> new BlockItem(ModBlocks.NUKE_N2.get(), new Item.Properties()));
        // Prototype block gets three Euphemia memorial lines. The fourth stayed commented out.
        NUKE_PROTOTYPE_ITEM = ITEMS.register("nuke_prototype",
                () -> new DescriptionBlockItem(ModBlocks.NUKE_PROTOTYPE.get(), new Item.Properties(),
                        "block.hbm.nuke_prototype.desc.0", "block.hbm.nuke_prototype.desc.1",
                        "block.hbm.nuke_prototype.desc.2"));
        // Source NukeCustom block plus its three reachable high-yield rods (custom_tnt/nuke/schrab,
        // stack 1). custom_hydro/amat/dirty/fall wait for their missing inputs and recipes.
        NUKE_CUSTOM_ITEM = ITEMS.register("nuke_custom",
                () -> new BlockItem(ModBlocks.NUKE_CUSTOM.get(), new Item.Properties()));
        // Source NukeBalefire block (the Balefire Bomb) plus its loadable components. The block and all
        // four items are creative-tab-only; every recipe (ass.balefirebomb, egg/shard, both batteries)
        // still needs inputs that are not registered. Source stack sizes:
        // shard 16, egg/batteries 1.
        NUKE_FSTBMB_ITEM = ITEMS.register("nuke_fstbmb",
                () -> new BlockItem(ModBlocks.NUKE_FSTBMB.get(), new Item.Properties()));
        // Source blocks/bomb/BombMulti, registered right after nuke_fstbmb (ModBlocks:1701).
        BOMB_MULTI_ITEM = ITEMS.register("bomb_multi",
                () -> new BlockItem(ModBlocks.BOMB_MULTI.get(), new Item.Properties()));
        // Source blocks/bomb/BombFloat float_bomb, registered after bomb_multi (ModBlocks:1704). A plain
        // full-cube block item; the Levitation Bomb has no tooltip, custom name or extra components.
        FLOAT_BOMB_ITEM = ITEMS.registerSimpleBlockItem("float_bomb", ModBlocks.FLOAT_BOMB);
        // Source blocks/bomb/BombThermo therm_endo/therm_exo, registered after float_bomb (ModBlocks:1705-
        // 1706). Plain full-cube block items with no tooltip, custom name or extra components.
        THERM_ENDO_ITEM = ITEMS.registerSimpleBlockItem("therm_endo", ModBlocks.THERM_ENDO);
        THERM_EXO_ITEM = ITEMS.registerSimpleBlockItem("therm_exo", ModBlocks.THERM_EXO);
        EGG_BALEFIRE_SHARD = ITEMS.registerSimpleItem("egg_balefire_shard", new Item.Properties().stacksTo(16));
        EGG_BALEFIRE = ITEMS.register("egg_balefire",
                () -> new CustomLoreItem(new Item.Properties().stacksTo(1), "item.hbm.egg_balefire.desc"));
        BATTERY_SPARK = ITEMS.registerSimpleItem("battery_spark", new Item.Properties().stacksTo(1));
        BATTERY_TRIXITE = ITEMS.registerSimpleItem("battery_trixite", new Item.Properties().stacksTo(1));
        CUSTOM_TNT = ITEMS.registerSimpleItem("custom_tnt", new Item.Properties().stacksTo(1));
        CUSTOM_NUKE = ITEMS.registerSimpleItem("custom_nuke", new Item.Properties().stacksTo(1));
        CUSTOM_SCHRAB = ITEMS.registerSimpleItem("custom_schrab", new Item.Properties().stacksTo(1));
        // Source ModItems.igniter: an ItemCustomLore detonation tool for The Prototype. Its
        // recipe (steel plate, schrabidium fine wire, advanced circuit, euphemium ingot) stays
        // needs ingredients that are not registered yet, so it is creative-only.
        IGNITER = ITEMS.register("igniter", () -> new CustomLoreItem(
                new Item.Properties().stacksTo(1),
                "item.hbm.igniter.desc.0", "item.hbm.igniter.desc.1", "item.hbm.igniter.desc.2",
                "item.hbm.igniter.desc.3", "item.hbm.igniter.desc.4", "item.hbm.igniter.desc.5"));
        AMMO_STANDARD = ITEMS.register("ammo_standard", AmmoStandardItem::new);
        GUN_PEPPERBOX = ITEMS.register("gun_pepperbox", PepperboxItem::new);
        GUN_LIGHT_REVOLVER = ITEMS.register("gun_light_revolver", () -> new BreakActionRevolverItem(false));
        GUN_LIGHT_REVOLVER_ATLAS = ITEMS.register("gun_light_revolver_atlas", () -> new BreakActionRevolverItem(true));
        GUN_HENRY = ITEMS.register("gun_henry", () -> new HenryRifleItem(false));
        GUN_HENRY_LINCOLN = ITEMS.register("gun_henry_lincoln", () -> new HenryRifleItem(true));
        GUN_HEAVY_REVOLVER = ITEMS.register("gun_heavy_revolver", HeavyRevolverItem::new);
        GUN_HANGMAN = ITEMS.register("gun_hangman", HangmanItem::new);
        AMMO_SECRET = ITEMS.register("ammo_secret", AmmoSecretItem::new);
        GUN_GREASEGUN = ITEMS.register("gun_greasegun",
                () -> new NineMillimeterGunItem(NineMillimeterGunItem.Variant.GREASE_GUN));
        GUN_LAG = ITEMS.register("gun_lag", LagPistolItem::new);
        GUN_UZI = ITEMS.register("gun_uzi",
                () -> new NineMillimeterGunItem(NineMillimeterGunItem.Variant.UZI));
        GUN_UZI_AKIMBO = ITEMS.register("gun_uzi_akimbo", DualUziItem::new);
        GUN_MARESLEG = ITEMS.register("gun_maresleg", MaresLegItem::new);
        GUN_MARESLEG_AKIMBO = ITEMS.register("gun_maresleg_akimbo", DualMaresLegItem::new);
        GUN_MARESLEG_BROKEN = ITEMS.register("gun_maresleg_broken", BrokenMaresLegItem::new);
        GUN_LIBERATOR = ITEMS.register("gun_liberator", LiberatorItem::new);
        GUN_SPAS12 = ITEMS.register("gun_spas12", SpasItem::new);
        GUN_AUTOSHOTGUN = ITEMS.register("gun_autoshotgun", AutoShotgunItem::new);
        GUN_AUTOSHOTGUN_SHREDDER = ITEMS.register("gun_autoshotgun_shredder", ShredderItem::new);
        GUN_AUTOSHOTGUN_SEXY = ITEMS.register("gun_autoshotgun_sexy", SexyItem::new);
        GUN_B92 = ITEMS.register("gun_b92", B92Item::new);
        GUN_B93 = ITEMS.register("gun_b93", B93Item::new);
        GUN_B92_AMMO = ITEMS.register("gun_b92_ammo", B92EnergyCellItem::new);
        GUN_FLAREGUN = ITEMS.register("gun_flaregun",
                () -> new FortyMillimeterGunItem(FortyMillimeterGunItem.Variant.FLARE_GUN));
        GUN_CONGOLAKE = ITEMS.register("gun_congolake",
                () -> new FortyMillimeterGunItem(FortyMillimeterGunItem.Variant.CONGO_LAKE));
        GUN_MK108 = ITEMS.register("gun_mk108",
                () -> new FortyMillimeterGunItem(FortyMillimeterGunItem.Variant.MK108));
        GUN_CARBINE = ITEMS.register("gun_carbine",
                () -> new SevenSixTwoGunItem(SevenSixTwoGunItem.Variant.CARBINE));
        GUN_MINIGUN = ITEMS.register("gun_minigun",
                () -> new SevenSixTwoGunItem(SevenSixTwoGunItem.Variant.MINIGUN));
        GUN_MAS36 = ITEMS.register("gun_mas36",
                () -> new SevenSixTwoGunItem(SevenSixTwoGunItem.Variant.MAS36));
        GUN_AM180 = ITEMS.register("gun_am180",
                () -> new TwentyTwoGunItem(TwentyTwoGunItem.Variant.AM180));
        GUN_STAR_F = ITEMS.register("gun_star_f",
                () -> new TwentyTwoGunItem(TwentyTwoGunItem.Variant.STAR_F));
        GUN_STAR_F_AKIMBO = ITEMS.register("gun_star_f_akimbo", DualStarFItem::new);
        GUN_FLAMER = ITEMS.register("gun_flamer",
                () -> new FlamerGunItem(FlamerGunItem.Variant.FLAMETHROWER));
        GUN_FLAMER_TOPAZ = ITEMS.register("gun_flamer_topaz",
                () -> new FlamerGunItem(FlamerGunItem.Variant.TOPAZ));
        GUN_FLAMER_DAYBREAKER = ITEMS.register("gun_flamer_daybreaker",
                () -> new FlamerGunItem(FlamerGunItem.Variant.DAYBREAKER));
        GUN_PANZERSCHRECK = ITEMS.register("gun_panzerschreck", RocketLauncherItem::new);
        GUN_STINGER = ITEMS.register("gun_stinger", StingerLauncherItem::new);
        GUN_QUADRO = ITEMS.register("gun_quadro", QuadRocketLauncherItem::new);
        GUN_MISSILE_LAUNCHER = ITEMS.register("gun_missile_launcher", MissileLauncherItem::new);
        GUN_G3 = ITEMS.register("gun_g3", () -> new G3Item());
        GUN_G3_ZEBRA = ITEMS.register("gun_g3_zebra",
                () -> new G3Item(G3Item.Variant.ZEBRA));
        GUN_STG77 = ITEMS.register("gun_stg77", Stg77Item::new);
        GUN_AMAT = ITEMS.register("gun_amat", () -> new AmatItem());
        GUN_AMAT_SUBTLETY = ITEMS.register("gun_amat_subtlety", SubtletyItem::new);
        GUN_AMAT_PENANCE = ITEMS.register("gun_amat_penance", PenanceItem::new);
        GUN_M2 = ITEMS.register("gun_m2", M2Item::new);
        GUN_TESLA_CANNON = ITEMS.register("gun_tesla_cannon", TeslaCannonItem::new);
        WEAPONIZED_STARBLASTER_CELL = ITEMS.register(
                "weaponized_starblaster_cell", WeaponizedStarblasterCellItem::new);
        MULTITOOL_DIG = ITEMS.register("multitool_dig",
                () -> new PowerFistItem(PowerFistItem.Mode.DIG));
        MULTITOOL_SILK = ITEMS.register("multitool_silk",
                () -> new PowerFistItem(PowerFistItem.Mode.SILK));
        MULTITOOL_EXT = ITEMS.register("multitool_ext",
                () -> new PowerFistItem(PowerFistItem.Mode.EXT));
        MULTITOOL_MINER = ITEMS.register("multitool_miner",
                () -> new PowerFistItem(PowerFistItem.Mode.MINER));
        MULTITOOL_HIT = ITEMS.register("multitool_hit",
                () -> new PowerFistItem(PowerFistItem.Mode.HIT));
        MULTITOOL_BEAM = ITEMS.register("multitool_beam",
                () -> new PowerFistItem(PowerFistItem.Mode.BEAM));
        MULTITOOL_SKY = ITEMS.register("multitool_sky",
                () -> new PowerFistItem(PowerFistItem.Mode.SKY));
        MULTITOOL_MEGA = ITEMS.register("multitool_mega",
                () -> new PowerFistItem(PowerFistItem.Mode.MEGA));
        MULTITOOL_JOULE = ITEMS.register("multitool_joule",
                () -> new PowerFistItem(PowerFistItem.Mode.JOULE));
        MULTITOOL_DECON = ITEMS.register("multitool_decon",
                () -> new PowerFistItem(PowerFistItem.Mode.DECON));
        MULTITOOL_PANE = ITEMS.register("multitool_pane",
                () -> new PowerFistItem(PowerFistItem.Mode.PANE));
        GEIGER_COUNTER = ITEMS.register("geiger_counter", GeigerCounterItem::new);
        BOOK_GUIDE = ITEMS.register("book_guide", GuideBookItem::new);
        DOSIMETER = ITEMS.register("dosimeter", DosimeterItem::new);
        STEEL_SWORD = ITEMS.register("steel_sword", () -> new SwordItem(ModToolTiers.STEEL,
                new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.STEEL, 4.0F, -2.4F))));
        STEEL_PICKAXE = ITEMS.register("steel_pickaxe", () -> new PickaxeItem(ModToolTiers.STEEL,
                new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.STEEL, 2.0F, -2.8F))));
        STEEL_AXE = ITEMS.register("steel_axe", () -> new AxeItem(ModToolTiers.STEEL,
                new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.STEEL, 3.0F, -3.0F))));
        STEEL_SHOVEL = ITEMS.register("steel_shovel", () -> new ShovelItem(ModToolTiers.STEEL,
                new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.STEEL, 1.0F, -3.0F))));
        STEEL_HOE = ITEMS.register("steel_hoe", () -> new HoeItem(ModToolTiers.STEEL,
                new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.STEEL, -2.0F, -3.0F))));
        TITANIUM_SWORD = ITEMS.register("titanium_sword", () -> new SwordItem(ModToolTiers.TITANIUM,
                new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.TITANIUM, 4.0F, -2.4F))));
        TITANIUM_PICKAXE = ITEMS.register("titanium_pickaxe", () -> new PickaxeItem(ModToolTiers.TITANIUM,
                new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.TITANIUM, 2.0F, -2.8F))));
        TITANIUM_AXE = ITEMS.register("titanium_axe", () -> new AxeItem(ModToolTiers.TITANIUM,
                new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.TITANIUM, 3.0F, -3.0F))));
        TITANIUM_SHOVEL = ITEMS.register("titanium_shovel", () -> new ShovelItem(ModToolTiers.TITANIUM,
                new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.TITANIUM, 1.0F, -3.0F))));
        TITANIUM_HOE = ITEMS.register("titanium_hoe", () -> new HoeItem(ModToolTiers.TITANIUM,
                new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.TITANIUM, -2.5F, -3.0F))));
        COBALT_SWORD = ITEMS.register("cobalt_sword", () -> new SwordItem(ModToolTiers.COBALT,
                new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.COBALT, 9.5F, -2.4F))));
        COBALT_PICKAXE = ITEMS.register("cobalt_pickaxe", () -> new PickaxeItem(ModToolTiers.COBALT,
                new Item.Properties().attributes(PickaxeItem.createAttributes(ModToolTiers.COBALT, 1.5F, -2.8F))));
        COBALT_AXE = ITEMS.register("cobalt_axe", () -> new AxeItem(ModToolTiers.COBALT,
                new Item.Properties().attributes(AxeItem.createAttributes(ModToolTiers.COBALT, 3.5F, -3.0F))));
        COBALT_SHOVEL = ITEMS.register("cobalt_shovel", () -> new ShovelItem(ModToolTiers.COBALT,
                new Item.Properties().attributes(ShovelItem.createAttributes(ModToolTiers.COBALT, 1.0F, -3.0F))));
        COBALT_HOE = ITEMS.register("cobalt_hoe", () -> new HoeItem(ModToolTiers.COBALT,
                new Item.Properties().attributes(HoeItem.createAttributes(ModToolTiers.COBALT, -2.5F, -3.0F))));
        BISMUTH_PICKAXE = ITEMS.register("bismuth_pickaxe", DepthRockPickaxeItem::new);
        SURVEY_SCANNER = ITEMS.register("survey_scanner", SurveyScannerItem::new);
        ORE_DENSITY_SCANNER = ITEMS.register("ore_density_scanner", OreDensityScannerItem::new);
        GEIGER_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("geiger", ModBlocks.GEIGER);
        IV_EMPTY = ITEMS.register("iv_empty", () -> new BloodBagItem(BloodBagItem.Type.EMPTY));
        IV_BLOOD = ITEMS.register("iv_blood", () -> new BloodBagItem(BloodBagItem.Type.BLOOD));
        RADAWAY = ITEMS.register("radaway", () -> new RadiationMedicineItem(
                new Item.Properties(), RadiationMedicineItem.Treatment.RADAWAY));
        RADAWAY_STRONG = ITEMS.register("radaway_strong", () -> new RadiationMedicineItem(
                new Item.Properties(), RadiationMedicineItem.Treatment.RADAWAY_STRONG));
        RADAWAY_FLUSH = ITEMS.register("radaway_flush", () -> new RadiationMedicineItem(
                new Item.Properties(), RadiationMedicineItem.Treatment.RADAWAY_FLUSH));
        RAD_X = ITEMS.register("radx", () -> new RadiationMedicineItem(
                new Item.Properties(), RadiationMedicineItem.Treatment.RAD_X));
        PILL_HERBAL = ITEMS.register("pill_herbal", () -> new RadiationMedicineItem(
                new Item.Properties(), RadiationMedicineItem.Treatment.HERBAL));
        SYRINGE_MKUNICORN = ITEMS.register("syringe_mkunicorn", MkuSyringeItem::new);
        CHEESE = ITEMS.registerSimpleItem("cheese", new Item.Properties().food(
                new FoodProperties.Builder().nutrition(5).saturationModifier(0.75F).build()));
        REACHER = ITEMS.registerSimpleItem("reacher", new Item.Properties().stacksTo(1));
        HAZMAT_CLOTH = ITEMS.registerSimpleItem("hazmat_cloth", new Item.Properties());
        RAG = ITEMS.register("rag", ClothRagItem::new);
        RAG_DAMP = ITEMS.registerSimpleItem("rag_damp", new Item.Properties());
        RAG_PISS = ITEMS.registerSimpleItem("rag_piss", new Item.Properties());
        FILTER_COAL = ITEMS.registerSimpleItem("filter_coal", new Item.Properties());
        CATALYST_CLAY = ITEMS.registerSimpleItem("catalyst_clay", new Item.Properties());
        GAS_MASK_FILTER = ITEMS.register("gas_mask_filter",
                () -> new HazmatFilterItem(HazmatFilterItem.FilterType.STANDARD));
        GAS_MASK_FILTER_MONO = ITEMS.register("gas_mask_filter_mono",
                () -> new HazmatFilterItem(HazmatFilterItem.FilterType.MONO));
        GAS_MASK_FILTER_COMBO = ITEMS.register("gas_mask_filter_combo",
                () -> new HazmatFilterItem(HazmatFilterItem.FilterType.COMBO));
        GAS_MASK_FILTER_RAG = ITEMS.register("gas_mask_filter_rag",
                () -> new HazmatFilterItem(HazmatFilterItem.FilterType.RAG));
        GAS_MASK_FILTER_PISS = ITEMS.register("gas_mask_filter_piss",
                () -> new HazmatFilterItem(HazmatFilterItem.FilterType.PISS));
        HAZMAT_HELMET = ITEMS.register("hazmat_helmet",
                () -> new HazmatArmorItem(net.minecraft.world.item.ArmorItem.Type.HELMET, 0.12F));
        HAZMAT_PLATE = ITEMS.register("hazmat_plate",
                () -> new HazmatArmorItem(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, 0.24F));
        HAZMAT_LEGS = ITEMS.register("hazmat_legs",
                () -> new HazmatArmorItem(net.minecraft.world.item.ArmorItem.Type.LEGGINGS, 0.18F));
        HAZMAT_BOOTS = ITEMS.register("hazmat_boots",
                () -> new HazmatArmorItem(net.minecraft.world.item.ArmorItem.Type.BOOTS, 0.06F));
        ENVSUIT_HELMET = ITEMS.register("envsuit_helmet",
                () -> new EnvsuitArmorItem(net.minecraft.world.item.ArmorItem.Type.HELMET));
        ENVSUIT_PLATE = ITEMS.register("envsuit_plate",
                () -> new EnvsuitArmorItem(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE));
        ENVSUIT_LEGS = ITEMS.register("envsuit_legs",
                () -> new EnvsuitArmorItem(net.minecraft.world.item.ArmorItem.Type.LEGGINGS));
        ENVSUIT_BOOTS = ITEMS.register("envsuit_boots",
                () -> new EnvsuitArmorItem(net.minecraft.world.item.ArmorItem.Type.BOOTS));
        DNS_HELMET = ITEMS.register("dns_helmet",
                () -> new DntArmorItem(net.minecraft.world.item.ArmorItem.Type.HELMET));
        DNS_PLATE = ITEMS.register("dns_plate",
                () -> new DntArmorItem(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE));
        DNS_LEGS = ITEMS.register("dns_legs",
                () -> new DntArmorItem(net.minecraft.world.item.ArmorItem.Type.LEGGINGS));
        DNS_BOOTS = ITEMS.register("dns_boots",
                () -> new DntArmorItem(net.minecraft.world.item.ArmorItem.Type.BOOTS));
        GOGGLES = ITEMS.register("goggles", () -> new ProtectiveMaskItem(
                ModArmorMaterials.GOGGLES, ProtectiveMaskItem.MaskType.GOGGLES));
        ASHGLASSES = ITEMS.register("ashglasses", () -> new ProtectiveMaskItem(
                ModArmorMaterials.GOGGLES, ProtectiveMaskItem.MaskType.ASH_GLASSES));
        GAS_MASK = ITEMS.register("gas_mask", () -> new ProtectiveMaskItem(
                ModArmorMaterials.GAS_MASK, ProtectiveMaskItem.MaskType.GAS_MASK));
        GAS_MASK_M65 = ITEMS.register("gas_mask_m65", () -> new ProtectiveMaskItem(
                ModArmorMaterials.GAS_MASK_M65, ProtectiveMaskItem.MaskType.M65));
        GAS_MASK_MONO = ITEMS.register("gas_mask_mono", () -> new ProtectiveMaskItem(
                ModArmorMaterials.GAS_MASK_MONO, ProtectiveMaskItem.MaskType.HALF_MASK));
        GAS_MASK_OLDE = ITEMS.register("gas_mask_olde", () -> new ProtectiveMaskItem(
                ModArmorMaterials.GAS_MASK_OLDE, ProtectiveMaskItem.MaskType.LEATHER));
        MASK_RAG = ITEMS.register("mask_rag", () -> new ProtectiveMaskItem(
                ModArmorMaterials.MASK_RAG, ProtectiveMaskItem.MaskType.DAMP_RAG));
        MASK_PISS = ITEMS.register("mask_piss", () -> new ProtectiveMaskItem(
                ModArmorMaterials.MASK_PISS, ProtectiveMaskItem.MaskType.SOAKED_RAG));
        MACHINE_ARMOR_TABLE_ITEM = ITEMS.registerSimpleBlockItem("machine_armor_table", ModBlocks.MACHINE_ARMOR_TABLE);
        CLADDING_PAINT = registerCladding("cladding_paint", ArmorCladdingItem.Effect.RADIATION, 0.025F);
        CLADDING_RUBBER = registerCladding("cladding_rubber", ArmorCladdingItem.Effect.RADIATION, 0.005F);
        CLADDING_LEAD = registerCladding("cladding_lead", ArmorCladdingItem.Effect.RADIATION, 0.1F);
        CLADDING_DESH = registerCladding("cladding_desh", ArmorCladdingItem.Effect.RADIATION, 0.2F);
        CLADDING_GHIORSIUM = registerCladding("cladding_ghiorsium", ArmorCladdingItem.Effect.RADIATION, 0.5F);
        CLADDING_IRON = registerCladding("cladding_iron", ArmorCladdingItem.Effect.IRON, 0.0F);
        CLADDING_OBSIDIAN = registerCladding("cladding_obsidian", ArmorCladdingItem.Effect.OBSIDIAN, 0.0F);
        PLATE_POLYMER = ITEMS.registerSimpleItem("plate_polymer", new Item.Properties());
        WIRE_FINE = ITEMS.register("wire_fine", WireFineItem::new);
        WIRE_DENSE = ITEMS.register("wire_dense", DenseWireItem::new);
        CASING = ITEMS.register("casing", CasingItem::new);
        Map<FoundryPartItem.PartType, DeferredItem<FoundryPartItem>> foundryParts = new LinkedHashMap<>();
        for (FoundryPartItem.PartType type : FoundryPartItem.PartType.values()) {
            foundryParts.put(type, ITEMS.register(type.id(), () -> new FoundryPartItem(type)));
        }
        FOUNDRY_PARTS = Collections.unmodifiableMap(foundryParts);
        BOLT = ITEMS.register("bolt", BoltItem::new);
        PLATE_CAST = ITEMS.register("plate_cast", CastPlateItem::new);
        PLATE_WELDED = ITEMS.register("plate_welded", WeldedPlateItem::new);
        ARC_ELECTRODE = ITEMS.register("arc_electrode", ArcElectrodeItem::new);
        ARC_ELECTRODE_BURNT = ITEMS.register("arc_electrode_burnt", ArcElectrodeBurntItem::new);
        POWDER_FLUX = ITEMS.registerSimpleItem("powder_flux", new Item.Properties());
        BALL_FIRECLAY = ITEMS.registerSimpleItem("ball_fireclay", new Item.Properties());
        MOLD_BASE = ITEMS.registerSimpleItem("mold_base", new Item.Properties());
        MOLD = ITEMS.register("mold", FoundryMoldItem::new);
        SCRAPS = ITEMS.register("scraps", FoundryScrapsItem::new);
        INGOT_RAW = ITEMS.register("ingot_raw", FoundryIngotItem::new);
        SCREWDRIVER = ITEMS.register("screwdriver", ScrewdriverItem::new);
        BLOWTORCH = ITEMS.register("blowtorch", BlowtorchItem::new);
        // Source metadata zero of part_generic is the Pneumatic Piston. Other metadata variants
        // wait for the remaining component families.
        PART_GENERIC = ITEMS.registerSimpleItem("part_generic", new Item.Properties());
        SHELL = ITEMS.register("shell", ShellItem::new);
        FLUID_IDENTIFIER_MULTI = ITEMS.register("fluid_identifier_multi", FluidIdentifierItem::new);
        CIRCUIT = ITEMS.register("circuit", CircuitItem::new);
        CRT_DISPLAY = ITEMS.registerSimpleItem("crt_display", new Item.Properties());
        REACTOR_CORE = ITEMS.registerSimpleItem("reactor_core", new Item.Properties());
        COIL_COPPER = ITEMS.registerSimpleItem("coil_copper", new Item.Properties());
        COIL_COPPER_TORUS = ITEMS.registerSimpleItem("coil_copper_torus", new Item.Properties());
        COIL_GOLD = ITEMS.registerSimpleItem("coil_gold", new Item.Properties());
        COIL_GOLD_TORUS = ITEMS.registerSimpleItem("coil_gold_torus", new Item.Properties());
        COIL_TUNGSTEN = ITEMS.registerSimpleItem("coil_tungsten", new Item.Properties());
        TANK_STEEL = ITEMS.registerSimpleItem("tank_steel", new Item.Properties());
        PIPE = ITEMS.register("pipe", PipeItem::new);
        FLUID_TANK_EMPTY = ITEMS.registerSimpleItem("fluid_tank_empty", new Item.Properties());
        FLUID_TANK_FULL = ITEMS.register("fluid_tank_full", UniversalFluidTankItem::new);
        MOTOR = ITEMS.registerSimpleItem("motor", new Item.Properties());
        MOTOR_DESH = ITEMS.registerSimpleItem("motor_desh", new Item.Properties());
        MAGNETRON = ITEMS.registerSimpleItem("magnetron", new Item.Properties());
        DRILL_TITANIUM = ITEMS.registerSimpleItem("drill_titanium", new Item.Properties());
        BLOCK_INSULATOR_ITEM = ITEMS.registerSimpleBlockItem("block_insulator", ModBlocks.BLOCK_INSULATOR);
        SANDBAGS_ITEM = ITEMS.registerSimpleBlockItem("sandbags", ModBlocks.SANDBAGS);
        ANVIL_IRON_ITEM = ITEMS.register("anvil_iron",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_IRON.get(), new Item.Properties(), 1));
        ANVIL_LEAD_ITEM = ITEMS.register("anvil_lead",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_LEAD.get(), new Item.Properties(), 1));
        ANVIL_STEEL_ITEM = ITEMS.register("anvil_steel",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_STEEL.get(), new Item.Properties(), 2));
        ANVIL_DESH_ITEM = ITEMS.register("anvil_desh",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_DESH.get(), new Item.Properties(), 3));
        ANVIL_FERROURANIUM_ITEM = ITEMS.register("anvil_ferrouranium",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_FERROURANIUM.get(), new Item.Properties(), 4));
        ANVIL_SATURNITE_ITEM = ITEMS.register("anvil_saturnite",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_SATURNITE.get(), new Item.Properties(), 5));
        ANVIL_BISMUTH_BRONZE_ITEM = ITEMS.register("anvil_bismuth_bronze",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_BISMUTH_BRONZE.get(), new Item.Properties(), 5));
        ANVIL_ARSENIC_BRONZE_ITEM = ITEMS.register("anvil_arsenic_bronze",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_ARSENIC_BRONZE.get(), new Item.Properties(), 5));
        ANVIL_SCHRABIDATE_ITEM = ITEMS.register("anvil_schrabidate",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_SCHRABIDATE.get(), new Item.Properties(), 6));
        ANVIL_DNT_ITEM = ITEMS.register("anvil_dnt",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_DNT.get(), new Item.Properties(), 7));
        ANVIL_OSMIRIDIUM_ITEM = ITEMS.register("anvil_osmiridium",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_OSMIRIDIUM.get(), new Item.Properties(), 8));
        ANVIL_MURKY_ITEM = ITEMS.register("anvil_murky",
                () -> new NtmAnvilBlockItem(ModBlocks.ANVIL_MURKY.get(), new Item.Properties(), 1_916_169));
        HEATER_ELECTRIC_ITEM = ITEMS.register("heater_electric",
                () -> new DescriptionBlockItem(ModBlocks.HEATER_ELECTRIC.get(), new Item.Properties(),
                        "block.hbm.heater_electric.desc.0",
                        "block.hbm.heater_electric.desc.1",
                        "block.hbm.heater_electric.desc.2"));
        HEATER_OILBURNER_ITEM = ITEMS.register("heater_oilburner",
                () -> new DescriptionBlockItem(ModBlocks.HEATER_OILBURNER.get(), new Item.Properties(),
                        "block.hbm.heater_oilburner.desc.0", "block.hbm.heater_oilburner.desc.1"));
        HEATER_HEATEX_ITEM = ITEMS.register("heater_heatex",
                () -> new DescriptionBlockItem(ModBlocks.HEATER_HEATEX.get(), new Item.Properties(),
                        "block.hbm.heater_heatex.desc.0"));
        MACHINE_BLAST_FURNACE_ITEM = ITEMS.register("machine_blast_furnace",
                () -> new DescriptionBlockItem(ModBlocks.MACHINE_BLAST_FURNACE.get(), new Item.Properties(),
                        "block.hbm.machine_blast_furnace.desc.0",
                        "block.hbm.machine_blast_furnace.desc.1",
                        "block.hbm.machine_blast_furnace.desc.2"));
        FURNACE_COMBINATION_ITEM = ITEMS.register("furnace_combination",
                () -> new DescriptionBlockItem(ModBlocks.FURNACE_COMBINATION.get(), new Item.Properties(),
                        "block.hbm.furnace_combination.desc.0",
                        "block.hbm.furnace_combination.desc.1",
                        "block.hbm.furnace_combination.desc.2"));
        FURNACE_STEEL_ITEM = ITEMS.register("furnace_steel",
                () -> new DescriptionBlockItem(ModBlocks.FURNACE_STEEL.get(), new Item.Properties(),
                        "block.hbm.furnace_steel.desc.0",
                        "block.hbm.furnace_steel.desc.1",
                        "block.hbm.furnace_steel.desc.2",
                        "block.hbm.furnace_steel.desc.3"));
        MACHINE_ELECTRIC_FURNACE_ITEM = ITEMS.registerSimpleBlockItem(
                "machine_electric_furnace_off", ModBlocks.MACHINE_ELECTRIC_FURNACE);
        MACHINE_FURNACE_BRICK_ITEM = ITEMS.registerSimpleBlockItem(
                "machine_furnace_brick_off", ModBlocks.MACHINE_FURNACE_BRICK);
        MACHINE_WOOD_BURNER_ITEM = ITEMS.register("machine_wood_burner",
                () -> new DescriptionBlockItem(ModBlocks.MACHINE_WOOD_BURNER.get(), new Item.Properties(),
                        "block.hbm.machine_wood_burner.desc.0",
                        "block.hbm.machine_wood_burner.desc.1",
                        "block.hbm.machine_wood_burner.desc.2"));
        MACHINE_MICROWAVE_ITEM = ITEMS.registerSimpleBlockItem("machine_microwave", ModBlocks.MACHINE_MICROWAVE);
        MACHINE_ASSEMBLY_MACHINE_ITEM = ITEMS.registerSimpleBlockItem(
                "machine_assembly_machine", ModBlocks.MACHINE_ASSEMBLY_MACHINE);
        MACHINE_DIESEL_ITEM = ITEMS.register("machine_diesel",
                () -> new DieselGeneratorBlockItem(ModBlocks.MACHINE_DIESEL.get(), new Item.Properties()));
        MACHINE_COMBUSTION_ENGINE_ITEM = ITEMS.register("machine_combustion_engine",
                () -> new CombustionEngineBlockItem(ModBlocks.MACHINE_COMBUSTION_ENGINE.get(), new Item.Properties()));
        PISTON_SET_STEEL = ITEMS.register("piston_set_steel", () -> new PistonSetItem(PistonSetItem.Type.STEEL));
        PISTON_SET_DURA = ITEMS.register("piston_set_dura", () -> new PistonSetItem(PistonSetItem.Type.DURA));
        PISTON_SET_DESH = ITEMS.register("piston_set_desh", () -> new PistonSetItem(PistonSetItem.Type.DESH));
        PISTON_SET_STARMETAL = ITEMS.register("piston_set_starmetal", () -> new PistonSetItem(PistonSetItem.Type.STARMETAL));
        MACHINE_FLUIDTANK_ITEM = ITEMS.register("machine_fluidtank",
                () -> new FluidStorageTankBlockItem(ModBlocks.MACHINE_FLUIDTANK.get(), new Item.Properties()));
        MACHINE_CHEMICAL_PLANT_ITEM = ITEMS.registerSimpleBlockItem(
                "machine_chemical_plant", ModBlocks.MACHINE_CHEMICAL_PLANT);
        MACHINE_SOLDERING_STATION_ITEM = ITEMS.registerSimpleBlockItem(
                "machine_soldering_station", ModBlocks.MACHINE_SOLDERING_STATION);
        MACHINE_TRANSFORMER_ITEM = ITEMS.registerSimpleBlockItem("machine_transformer", ModBlocks.MACHINE_TRANSFORMER);
        MACHINE_ARC_WELDER_ITEM = ITEMS.registerSimpleBlockItem("machine_arc_welder", ModBlocks.MACHINE_ARC_WELDER);
        MACHINE_ARC_FURNACE_ITEM = ITEMS.registerSimpleBlockItem("machine_arc_furnace", ModBlocks.MACHINE_ARC_FURNACE);
        MACHINE_REFINERY_ITEM = ITEMS.register("machine_refinery",
                () -> new RefineryBlockItem(ModBlocks.MACHINE_REFINERY.get(), new Item.Properties()));
        MACHINE_CENTRIFUGE_ITEM = ITEMS.registerSimpleBlockItem("machine_centrifuge", ModBlocks.MACHINE_CENTRIFUGE);
        MACHINE_CATALYTIC_CRACKER_ITEM = ITEMS.registerSimpleBlockItem(
                "machine_catalytic_cracker", ModBlocks.MACHINE_CATALYTIC_CRACKER);
        MACHINE_FRACTION_TOWER_ITEM = ITEMS.registerSimpleBlockItem(
                "machine_fraction_tower", ModBlocks.MACHINE_FRACTION_TOWER);
        FRACTION_SPACER_ITEM = ITEMS.registerSimpleBlockItem("fraction_spacer", ModBlocks.FRACTION_SPACER);
        CENTRIFUGE_ELEMENT = ITEMS.registerSimpleItem("centrifuge_element", new Item.Properties());
        MACHINE_CRUCIBLE_ITEM = ITEMS.registerSimpleBlockItem("machine_crucible", ModBlocks.MACHINE_CRUCIBLE);
        FOUNDRY_MOLD_ITEM = ITEMS.registerSimpleBlockItem("foundry_mold", ModBlocks.FOUNDRY_MOLD);
        FOUNDRY_BASIN_ITEM = ITEMS.registerSimpleBlockItem("foundry_basin", ModBlocks.FOUNDRY_BASIN);
        FOUNDRY_CHANNEL_ITEM = ITEMS.registerSimpleBlockItem("foundry_channel", ModBlocks.FOUNDRY_CHANNEL);
        FOUNDRY_TANK_ITEM = ITEMS.registerSimpleBlockItem("foundry_tank", ModBlocks.FOUNDRY_TANK);
        FOUNDRY_OUTLET_ITEM = ITEMS.registerSimpleBlockItem("foundry_outlet", ModBlocks.FOUNDRY_OUTLET);
        FOUNDRY_SLAGTAP_ITEM = ITEMS.registerSimpleBlockItem("foundry_slagtap", ModBlocks.FOUNDRY_SLAGTAP);
        CONCRETE_SMOOTH_ITEM = ITEMS.registerSimpleBlockItem("concrete_smooth", ModBlocks.CONCRETE_SMOOTH);
        STEEL_SCAFFOLD_ITEM = ITEMS.register("steel_scaffold",
                () -> new ScaffoldBlockItem(ModBlocks.STEEL_SCAFFOLD.get(), new Item.Properties()));
        STEEL_BEAM_ITEM = ITEMS.registerSimpleBlockItem("steel_beam", ModBlocks.STEEL_BEAM);
        STEEL_GRATE_ITEM = ITEMS.registerSimpleBlockItem("steel_grate", ModBlocks.STEEL_GRATE);
        CONVEYOR_WAND = ITEMS.register("conveyor_wand",
                () -> new ConveyorWandItem(ConveyorType.REGULAR));
        CONVEYOR_WAND_EXPRESS = ITEMS.register("conveyor_wand_express",
                () -> new ConveyorWandItem(ConveyorType.EXPRESS));
        CONVEYOR_WAND_DOUBLE = ITEMS.register("conveyor_wand_double",
                () -> new ConveyorWandItem(ConveyorType.DOUBLE));
        CONVEYOR_WAND_TRIPLE = ITEMS.register("conveyor_wand_triple",
                () -> new ConveyorWandItem(ConveyorType.TRIPLE));
        CRANE_EXTRACTOR_ITEM = ITEMS.register("crane_extractor",
                () -> new DescriptionBlockItem(ModBlocks.CRANE_EXTRACTOR.get(), new Item.Properties(),
                        "block.hbm.crane_extractor.desc.0", "block.hbm.crane_extractor.desc.1",
                        "block.hbm.crane_extractor.desc.2", "block.hbm.crane_extractor.desc.3",
                        "block.hbm.crane_extractor.desc.4"));
        CRANE_INSERTER_ITEM = ITEMS.register("crane_inserter",
                () -> new DescriptionBlockItem(ModBlocks.CRANE_INSERTER.get(), new Item.Properties(),
                        "block.hbm.crane_inserter.desc.0", "block.hbm.crane_inserter.desc.1",
                        "block.hbm.crane_inserter.desc.2", "block.hbm.crane_inserter.desc.3"));
        CRANE_BOXER_ITEM = ITEMS.register("crane_boxer",
                () -> new DescriptionBlockItem(ModBlocks.CRANE_BOXER.get(), new Item.Properties(),
                        "block.hbm.crane_boxer.desc.0",
                        "block.hbm.crane_boxer.desc.1",
                        "block.hbm.crane_boxer.desc.2",
                        "block.hbm.crane_boxer.desc.3"));
        BLUEPRINTS = ITEMS.register("blueprints", BlueprintItem::new);
        Map<String, DeferredItem<MachineUpgradeItem>> upgrades = new LinkedHashMap<>();
        for (MachineUpgradeItem.Type type : MachineUpgradeItem.Type.values()) {
            String prefix = "upgrade_" + type.name().toLowerCase(java.util.Locale.ROOT) + "_";
            for (int level = 1; level <= 3; level++) {
                int upgradeLevel = level;
                upgrades.put(prefix + level, ITEMS.register(prefix + level,
                        () -> new MachineUpgradeItem(type, upgradeLevel)));
            }
        }
        MACHINE_UPGRADES = Collections.unmodifiableMap(upgrades);

        Map<String, DeferredItem<StampItem>> stamps = new LinkedHashMap<>();
        registerStampTier(stamps, "stone", 32);
        registerStampTier(stamps, "iron", 64);
        registerStampTier(stamps, "steel", 192);
        registerStampTier(stamps, "titanium", 256);
        registerStampTier(stamps, "obsidian", 512);
        registerStampTier(stamps, "desh", 0);
        registerStamp(stamps, "stamp_357", 1000, StampItem.StampType.C357);
        registerStamp(stamps, "stamp_44", 1000, StampItem.StampType.C44);
        registerStamp(stamps, "stamp_9", 1000, StampItem.StampType.C9);
        registerStamp(stamps, "stamp_50", 1000, StampItem.StampType.C50);
        registerStamp(stamps, "stamp_desh_357", 0, StampItem.StampType.C357);
        registerStamp(stamps, "stamp_desh_44", 0, StampItem.StampType.C44);
        registerStamp(stamps, "stamp_desh_9", 0, StampItem.StampType.C9);
        registerStamp(stamps, "stamp_desh_50", 0, StampItem.StampType.C50);
        STAMPS = Collections.unmodifiableMap(stamps);

        ALL_ITEMS = buildAllItemsIndex();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, DeferredItem<? extends Item>> buildAllItemsIndex() {
        Map<String, DeferredItem<? extends Item>> all = new LinkedHashMap<>();
        for (DeferredHolder<Item, ? extends Item> holder : ITEMS.getEntries()) {
            all.put(holder.getId().getPath(), (DeferredItem<? extends Item>) holder);
        }
        return Collections.unmodifiableMap(all);
    }

    private ModItems() {
    }

    private static DeferredItem<HazardousItem> registerExplosiveItem(String id, float explosive) {
        return ITEMS.register(id,
                () -> new HazardousItem(new Item.Properties(), HazardProfile.NONE.withExplosive(explosive)));
    }

    private static DeferredItem<ZirnoxRodItem> zirnoxRod(String id, ZirnoxRodItem.Type type) {
        return ITEMS.register(id, () -> new ZirnoxRodItem(new Item.Properties(), type));
    }

    private static void registerDfcCatalyst(Map<String, DeferredItem<DfcCatalystItem>> catalysts,
                                            String id, int color, long absorption, float power,
                                            float heat, float fuel) {
        catalysts.put(id, ITEMS.register(id,
                () -> new DfcCatalystItem(color, absorption, power, heat, fuel)));
    }

    private static DeferredItem<ArmorCladdingItem> registerCladding(String id, ArmorCladdingItem.Effect effect,
                                                                    float radiationResistance) {
        return ITEMS.register(id, () -> new ArmorCladdingItem(effect, radiationResistance));
    }

    private static void registerStampTier(Map<String, DeferredItem<StampItem>> stamps, String tier, int uses) {
        registerStamp(stamps, "stamp_" + tier + "_flat", uses, StampItem.StampType.FLAT);
        registerStamp(stamps, "stamp_" + tier + "_plate", uses, StampItem.StampType.PLATE);
        registerStamp(stamps, "stamp_" + tier + "_wire", uses, StampItem.StampType.WIRE);
        registerStamp(stamps, "stamp_" + tier + "_circuit", uses, StampItem.StampType.CIRCUIT);
    }

    private static void registerStamp(
            Map<String, DeferredItem<StampItem>> stamps,
            String id,
            int uses,
            StampItem.StampType type
    ) {
        stamps.put(id, ITEMS.register(id, () -> new StampItem(uses, type)));
    }

    public static DeferredItem<? extends Item> get(String id) {
        DeferredItem<? extends Item> item = MATERIAL_ITEMS.get(id);
        if (item == null) {
            item = ALL_ITEMS.get(id);
        }
        if (item == null) {
            throw new IllegalArgumentException("Unknown HBM item: " + id);
        }
        return item;
    }

    public static DeferredItem<? extends BlockItem> getBlockItem(String id) {
        DeferredItem<? extends BlockItem> item = MATERIAL_BLOCK_ITEMS.get(id);
        if (item == null) {
            throw new IllegalArgumentException("Unknown HBM block item: " + id);
        }
        return item;
    }

    public static DeferredItem<BlockItem> legacyOreBlockItem(String id) {
        DeferredItem<BlockItem> item = LEGACY_ORE_BLOCK_ITEMS.get(id);
        if (item == null) throw new IllegalArgumentException("Unknown legacy ore block item: " + id);
        return item;
    }

    public static DeferredItem<Item> legacyOreResourceItem(String id) {
        DeferredItem<Item> item = LEGACY_ORE_RESOURCE_ITEMS.get(id);
        if (item == null) throw new IllegalArgumentException("Unknown legacy ore resource item: " + id);
        return item;
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
