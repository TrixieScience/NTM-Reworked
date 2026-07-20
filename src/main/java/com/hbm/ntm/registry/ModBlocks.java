package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.ArmorTableBlock;
import com.hbm.ntm.block.AirIntakeBlock;
import com.hbm.ntm.block.ArcWelderBlock;
import com.hbm.ntm.block.ArcFurnaceBlock;
import com.hbm.ntm.block.AshpitBlock;
import com.hbm.ntm.block.AssemblyMachineBlock;
import com.hbm.ntm.block.AsbestosGasBlock;
import com.hbm.ntm.block.AsbestosOreBlock;
import com.hbm.ntm.block.BatterySocketBlock;
import com.hbm.ntm.block.BlastFurnaceBlock;
import com.hbm.ntm.block.BrickFurnaceBlock;
import com.hbm.ntm.block.BreedingReactorBlock;
import com.hbm.ntm.block.ChargeBlock;
import com.hbm.ntm.block.ChargeType;
import com.hbm.ntm.block.ChlorineGasBlock;
import com.hbm.ntm.block.ChlorineSealBlock;
import com.hbm.ntm.block.ChlorineVentBlock;
import com.hbm.ntm.block.CoalDustGasBlock;
import com.hbm.ntm.block.CarbonMonoxideGasBlock;
import com.hbm.ntm.block.ChemicalPlantBlock;
import com.hbm.ntm.block.CombinationOvenBlock;
import com.hbm.ntm.block.CombustionEngineBlock;
import com.hbm.ntm.block.CentrifugeBlock;
import com.hbm.ntm.block.ConventionalExplosiveBlock;
import com.hbm.ntm.block.ConveyorBlock;
import com.hbm.ntm.block.ConveyorBoxerBlock;
import com.hbm.ntm.block.ConveyorChuteBlock;
import com.hbm.ntm.block.ConveyorLiftBlock;
import com.hbm.ntm.block.CraneExtractorBlock;
import com.hbm.ntm.block.CraneInserterBlock;
import com.hbm.ntm.block.CrackingTowerBlock;
import com.hbm.ntm.block.CrucibleBlock;
import com.hbm.ntm.block.FireboxBlock;
import com.hbm.ntm.block.FensuBlock;
import com.hbm.ntm.block.FractionTowerBlock;
import com.hbm.ntm.block.FractionTowerSeparatorBlock;
import com.hbm.ntm.block.GasTurbineBlock;
import com.hbm.ntm.block.FluidDuctBlock;
import com.hbm.ntm.block.FluidBurnerBlock;
import com.hbm.ntm.block.FluidStorageTankBlock;
import com.hbm.ntm.block.FoundryMoldBlock;
import com.hbm.ntm.block.FoundryBasinBlock;
import com.hbm.ntm.block.FoundryChannelBlock;
import com.hbm.ntm.block.FoundryTankBlock;
import com.hbm.ntm.block.FoundryOutletBlock;
import com.hbm.ntm.block.DynamicSlagBlock;
import com.hbm.ntm.block.FalloutBlock;
import com.hbm.ntm.block.GeigerCounterBlock;
import com.hbm.ntm.block.HeatBoilerBlock;
import com.hbm.ntm.block.HeatExchangerBlock;
import com.hbm.ntm.block.HighPowerCondenserBlock;
import com.hbm.ntm.block.HeatingOvenBlock;
import com.hbm.ntm.block.HeCableBlock;
import com.hbm.ntm.block.HydroactiveBlock;
import com.hbm.ntm.block.IndustrialTurbineBlock;
import com.hbm.ntm.block.TurbofanBlock;
import com.hbm.ntm.block.MachinePressBlock;
import com.hbm.ntm.block.MachineShredderBlock;
import com.hbm.ntm.block.MicrowaveBlock;
import com.hbm.ntm.block.LargeNukeBlock;
import com.hbm.ntm.block.LargeNukeType;
import com.hbm.ntm.block.LevitationBombBlock;
import com.hbm.ntm.block.NukeManBlock;
import com.hbm.ntm.block.NukeBalefireBlock;
import com.hbm.ntm.block.BombMultiBlock;
import com.hbm.ntm.block.BombThermoBlock;
import com.hbm.ntm.block.LandmineBlock;
import com.hbm.ntm.block.FrozenBlock;
import com.hbm.ntm.block.BalefireBlock;
import com.hbm.ntm.block.NukeN2Block;
import com.hbm.ntm.block.NukeCustomBlock;
import com.hbm.ntm.block.NukeFleijaBlock;
import com.hbm.ntm.block.NukeSoliniumBlock;
import com.hbm.ntm.block.NukePrototypeBlock;
import com.hbm.ntm.block.NtmAnvilBlock;
import com.hbm.ntm.block.NetherCoalOreBlock;
import com.hbm.ntm.block.OilDepositBlock;
import com.hbm.ntm.block.OilDerrickBlock;
import com.hbm.ntm.block.OilSpillBlock;
import com.hbm.ntm.block.PumpBlock;
import com.hbm.ntm.block.DeadPlantBlock;
import com.hbm.ntm.block.DieselGeneratorBlock;
import com.hbm.ntm.block.DfcComponentBlock;
import com.hbm.ntm.block.DfcCoreBlock;
import com.hbm.ntm.block.DigammaAshBlock;
import com.hbm.ntm.block.ElectricHeaterBlock;
import com.hbm.ntm.block.ElectricFurnaceBlock;
import com.hbm.ntm.block.RadioactiveBlock;
import com.hbm.ntm.block.RadGenBlock;
import com.hbm.ntm.block.RadioTorchBlock;
import com.hbm.ntm.block.RadonGasBlock;
import com.hbm.ntm.block.RefineryBlock;
import com.hbm.ntm.block.ResearchReactorBlock;
import com.hbm.ntm.block.SandbagsBlock;
import com.hbm.ntm.block.ScaffoldBlock;
import com.hbm.ntm.block.SteelBeamBlock;
import com.hbm.ntm.block.SteelGrateBlock;
import com.hbm.ntm.block.StirlingBlock;
import com.hbm.ntm.block.SawmillBlock;
import com.hbm.ntm.block.SellafieldBlock;
import com.hbm.ntm.block.SirenBlock;
import com.hbm.ntm.block.SteamEngineBlock;
import com.hbm.ntm.block.SteamTurbineBlock;
import com.hbm.ntm.block.SteamCondenserBlock;
import com.hbm.ntm.block.SolderingStationBlock;
import com.hbm.ntm.block.SteelFurnaceBlock;
import com.hbm.ntm.block.StoneResourceBlock;
import com.hbm.ntm.block.LegacyGasBlock;
import com.hbm.ntm.block.LegacyOreBlock;
import com.hbm.ntm.block.UraniumOutgassingOreBlock;
import com.hbm.ntm.block.WasteEarthBlock;
import com.hbm.ntm.block.WasteLogBlock;
import com.hbm.ntm.block.WasteOreBlock;
import com.hbm.ntm.block.WoodBurnerBlock;
import com.hbm.ntm.block.WasteDrumBlock;
import com.hbm.ntm.block.ZirnoxBlock;
import com.hbm.ntm.block.ZirnoxDestroyedBlock;
import com.hbm.ntm.content.HazardousMaterialDefinitions;
import com.hbm.ntm.content.MaterialDefinitions;
import com.hbm.ntm.conveyor.ConveyorType;
import com.hbm.ntm.dfc.DfcKind;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HbmNtm.MOD_ID);
    public static final Map<String, DeferredBlock<Block>> MATERIAL_BLOCKS;
    public static final Map<String, DeferredBlock<Block>> LEGACY_ORE_BLOCKS;
    public static final DeferredBlock<Block> URANIUM_BLOCK;
    public static final DeferredBlock<Block> ORE_TITANIUM;
    public static final DeferredBlock<Block> ORE_TUNGSTEN;
    public static final DeferredBlock<Block> ORE_COBALT;
    public static final DeferredBlock<Block> ORE_RARE;
    public static final DeferredBlock<Block> ORE_COLTAN;
    public static final DeferredBlock<StoneResourceBlock> STONE_RESOURCE;
    public static final DeferredBlock<OilDepositBlock> ORE_OIL;
    public static final DeferredBlock<Block> ORE_OIL_EMPTY;
    public static final DeferredBlock<ColoredFallingBlock> DIRT_OILY;
    public static final DeferredBlock<ColoredFallingBlock> DIRT_DEAD;
    public static final DeferredBlock<ColoredFallingBlock> SAND_DIRTY;
    public static final DeferredBlock<ColoredFallingBlock> SAND_DIRTY_RED;
    public static final DeferredBlock<ColoredFallingBlock> STONE_CRACKED;
    public static final DeferredBlock<DeadPlantBlock> PLANT_DEAD;
    public static final DeferredBlock<OilSpillBlock> OIL_SPILL;
    public static final DeferredBlock<Block> OIL_PIPE;
    public static final DeferredBlock<WasteEarthBlock> WASTE_EARTH;
    public static final DeferredBlock<WasteEarthBlock> WASTE_MYCELIUM;
    public static final DeferredBlock<WasteOreBlock> WASTE_TRINITITE;
    public static final DeferredBlock<WasteOreBlock> WASTE_TRINITITE_RED;
    public static final DeferredBlock<WasteLogBlock> WASTE_LOG;
    public static final DeferredBlock<WasteOreBlock> WASTE_PLANKS;
    public static final DeferredBlock<FrozenBlock> FROZEN_DIRT;
    public static final DeferredBlock<WasteEarthBlock> FROZEN_GRASS;
    public static final DeferredBlock<WasteLogBlock> FROZEN_LOG;
    public static final DeferredBlock<FrozenBlock> FROZEN_PLANKS;
    public static final DeferredBlock<RadioactiveBlock> BLOCK_TRINITITE;
    public static final DeferredBlock<RadioactiveBlock> BLOCK_WASTE;
    public static final DeferredBlock<SellafieldBlock> SELLAFIELD;
    public static final DeferredBlock<Block> SELLAFIELD_SLAKED;
    public static final DeferredBlock<OilDerrickBlock> MACHINE_WELL;
    public static final DeferredBlock<RadioTorchBlock> RADIO_TORCH_SENDER;
    public static final DeferredBlock<RadioTorchBlock> RADIO_TORCH_RECEIVER;
    public static final DeferredBlock<RadioTorchBlock> RADIO_TORCH_COUNTER;
    public static final DeferredBlock<RadioTorchBlock> RADIO_TORCH_LOGIC;
    public static final DeferredBlock<RadioTorchBlock> RADIO_TORCH_READER;
    public static final DeferredBlock<RadioTorchBlock> RADIO_TORCH_CONTROLLER;
    public static final DeferredBlock<DieselGeneratorBlock> MACHINE_DIESEL;
    public static final DeferredBlock<CombustionEngineBlock> MACHINE_COMBUSTION_ENGINE;
    public static final DeferredBlock<DfcCoreBlock> DFC_CORE;
    public static final DeferredBlock<DfcComponentBlock> DFC_EMITTER;
    public static final DeferredBlock<DfcComponentBlock> DFC_INJECTOR;
    public static final DeferredBlock<DfcComponentBlock> DFC_RECEIVER;
    public static final DeferredBlock<DfcComponentBlock> DFC_STABILIZER;
    public static final DeferredBlock<FluidStorageTankBlock> MACHINE_FLUIDTANK;
    public static final DeferredBlock<HeCableBlock> RED_CABLE;
    public static final DeferredBlock<BatterySocketBlock> MACHINE_BATTERY_SOCKET;
    public static final DeferredBlock<FensuBlock> MACHINE_BATTERY_REDD;
    public static final DeferredBlock<MachinePressBlock> MACHINE_PRESS;
    public static final DeferredBlock<Block> PRESS_PREHEATER;
    public static final DeferredBlock<MachineShredderBlock> MACHINE_SHREDDER;
    public static final DeferredBlock<ColoredFallingBlock> GRAVEL_OBSIDIAN;
    public static final DeferredBlock<ColoredFallingBlock> GRAVEL_DIAMOND;
    public static final DeferredBlock<FireboxBlock> HEATER_FIREBOX;
    public static final DeferredBlock<HeatingOvenBlock> HEATER_OVEN;
    public static final DeferredBlock<AshpitBlock> MACHINE_ASHPIT;
    public static final DeferredBlock<StirlingBlock> MACHINE_STIRLING;
    public static final DeferredBlock<SawmillBlock> MACHINE_SAWMILL;
    public static final DeferredBlock<SteamEngineBlock> MACHINE_STEAM_ENGINE;
    public static final DeferredBlock<IndustrialTurbineBlock> MACHINE_INDUSTRIAL_TURBINE;
    public static final DeferredBlock<GasTurbineBlock> MACHINE_TURBINE_GAS;
    public static final DeferredBlock<TurbofanBlock> MACHINE_TURBOFAN;
    public static final DeferredBlock<SteamTurbineBlock> MACHINE_TURBINE;
    public static final DeferredBlock<ZirnoxBlock> REACTOR_ZIRNOX;
    public static final DeferredBlock<ZirnoxDestroyedBlock> ZIRNOX_DESTROYED;
    public static final DeferredBlock<BreedingReactorBlock> MACHINE_REACTOR_BREEDING;
    public static final DeferredBlock<ResearchReactorBlock> REACTOR_RESEARCH;
    public static final DeferredBlock<RadGenBlock> MACHINE_RADGEN;
    public static final DeferredBlock<WasteDrumBlock> MACHINE_WASTE_DRUM;
    public static final DeferredBlock<SirenBlock> MACHINE_SIREN;
    public static final DeferredBlock<PumpBlock> PUMP_STEAM;
    public static final DeferredBlock<PumpBlock> PUMP_ELECTRIC;
    public static final DeferredBlock<AirIntakeBlock> MACHINE_INTAKE;
    public static final DeferredBlock<SteamCondenserBlock> MACHINE_CONDENSER;
    public static final DeferredBlock<HighPowerCondenserBlock> MACHINE_CONDENSER_POWERED;
    public static final DeferredBlock<HeatBoilerBlock> MACHINE_BOILER;
    public static final DeferredBlock<Block> REINFORCED_STONE;
    public static final DeferredBlock<Block> GNEISS_TILE;
    public static final DeferredBlock<Block> GNEISS_BRICK;
    public static final DeferredBlock<Block> GNEISS_CHISELED;
    public static final DeferredBlock<Block> REINFORCED_LIGHT;
    public static final DeferredBlock<Block> REINFORCED_SAND;
    public static final DeferredBlock<Block> DEPTH_BRICK;
    public static final DeferredBlock<Block> DEPTH_TILES;
    public static final DeferredBlock<Block> DEPTH_NETHER_BRICK;
    public static final DeferredBlock<Block> DEPTH_NETHER_TILES;
    public static final DeferredBlock<ElectricHeaterBlock> HEATER_ELECTRIC;
    public static final DeferredBlock<FluidBurnerBlock> HEATER_OILBURNER;
    public static final DeferredBlock<HeatExchangerBlock> HEATER_HEATEX;
    public static final DeferredBlock<BlastFurnaceBlock> MACHINE_BLAST_FURNACE;
    public static final DeferredBlock<CombinationOvenBlock> FURNACE_COMBINATION;
    public static final DeferredBlock<SteelFurnaceBlock> FURNACE_STEEL;
    public static final DeferredBlock<ElectricFurnaceBlock> MACHINE_ELECTRIC_FURNACE;
    public static final DeferredBlock<BrickFurnaceBlock> MACHINE_FURNACE_BRICK;
    public static final DeferredBlock<WoodBurnerBlock> MACHINE_WOOD_BURNER;
    public static final DeferredBlock<MicrowaveBlock> MACHINE_MICROWAVE;
    public static final DeferredBlock<FluidDuctBlock> FLUID_DUCT_NEO;
    public static final DeferredBlock<ConventionalExplosiveBlock> DYNAMITE;
    public static final DeferredBlock<ConventionalExplosiveBlock> TNT_NTM;
    public static final DeferredBlock<ConventionalExplosiveBlock> SEMTEX;
    public static final DeferredBlock<ConventionalExplosiveBlock> C4;
    public static final DeferredBlock<ChargeBlock> CHARGE_DYNAMITE;
    public static final DeferredBlock<ChargeBlock> CHARGE_MINER;
    public static final DeferredBlock<ChargeBlock> CHARGE_C4;
    public static final DeferredBlock<ChargeBlock> CHARGE_SEMTEX;
    public static final DeferredBlock<LandmineBlock> MINE_AP;
    public static final DeferredBlock<LandmineBlock> MINE_HE;
    public static final DeferredBlock<LandmineBlock> MINE_SHRAP;
    public static final DeferredBlock<LandmineBlock> MINE_FAT;
    public static final DeferredBlock<LandmineBlock> MINE_NAVAL;
    public static final DeferredBlock<LargeNukeBlock> NUKE_GADGET;
    public static final DeferredBlock<LargeNukeBlock> NUKE_BOY;
    public static final DeferredBlock<NukeManBlock> NUKE_MAN;
    public static final DeferredBlock<LargeNukeBlock> NUKE_MIKE;
    public static final DeferredBlock<LargeNukeBlock> NUKE_TSAR;
    public static final DeferredBlock<NukeFleijaBlock> NUKE_FLEIJA;
    public static final DeferredBlock<NukeSoliniumBlock> NUKE_SOLINIUM;
    public static final DeferredBlock<NukeN2Block> NUKE_N2;
    public static final DeferredBlock<NukePrototypeBlock> NUKE_PROTOTYPE;
    public static final DeferredBlock<NukeCustomBlock> NUKE_CUSTOM;
    public static final DeferredBlock<NukeBalefireBlock> NUKE_FSTBMB;
    public static final DeferredBlock<BombMultiBlock> BOMB_MULTI;
    public static final DeferredBlock<LevitationBombBlock> FLOAT_BOMB;
    public static final DeferredBlock<BombThermoBlock> THERM_ENDO;
    public static final DeferredBlock<BombThermoBlock> THERM_EXO;
    public static final DeferredBlock<BalefireBlock> BALEFIRE;
    public static final DeferredBlock<FalloutBlock> FALLOUT;
    public static final DeferredBlock<DigammaAshBlock> ASH_DIGAMMA;
    public static final DeferredBlock<ChlorineVentBlock> VENT_CHLORINE;
    public static final DeferredBlock<ChlorineGasBlock> CHLORINE_GAS;
    public static final DeferredBlock<ChlorineSealBlock> VENT_CHLORINE_SEAL;
    public static final DeferredBlock<GeigerCounterBlock> GEIGER;
    public static final DeferredBlock<ArmorTableBlock> MACHINE_ARMOR_TABLE;
    public static final DeferredBlock<RotatedPillarBlock> BLOCK_INSULATOR;
    public static final DeferredBlock<SandbagsBlock> SANDBAGS;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_IRON;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_LEAD;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_STEEL;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_DESH;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_FERROURANIUM;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_SATURNITE;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_BISMUTH_BRONZE;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_ARSENIC_BRONZE;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_SCHRABIDATE;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_DNT;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_OSMIRIDIUM;
    public static final DeferredBlock<NtmAnvilBlock> ANVIL_MURKY;
    public static final DeferredBlock<AssemblyMachineBlock> MACHINE_ASSEMBLY_MACHINE;
    public static final DeferredBlock<ChemicalPlantBlock> MACHINE_CHEMICAL_PLANT;
    public static final DeferredBlock<SolderingStationBlock> MACHINE_SOLDERING_STATION;
    public static final DeferredBlock<Block> MACHINE_TRANSFORMER;
    public static final DeferredBlock<ArcWelderBlock> MACHINE_ARC_WELDER;
    public static final DeferredBlock<ArcFurnaceBlock> MACHINE_ARC_FURNACE;
    public static final DeferredBlock<RefineryBlock> MACHINE_REFINERY;
    public static final DeferredBlock<CentrifugeBlock> MACHINE_CENTRIFUGE;
    public static final DeferredBlock<CrackingTowerBlock> MACHINE_CATALYTIC_CRACKER;
    public static final DeferredBlock<FractionTowerBlock> MACHINE_FRACTION_TOWER;
    public static final DeferredBlock<FractionTowerSeparatorBlock> FRACTION_SPACER;
    public static final DeferredBlock<CrucibleBlock> MACHINE_CRUCIBLE;
    public static final DeferredBlock<FoundryMoldBlock> FOUNDRY_MOLD;
    public static final DeferredBlock<FoundryBasinBlock> FOUNDRY_BASIN;
    public static final DeferredBlock<FoundryChannelBlock> FOUNDRY_CHANNEL;
    public static final DeferredBlock<FoundryTankBlock> FOUNDRY_TANK;
    public static final DeferredBlock<FoundryOutletBlock> FOUNDRY_OUTLET;
    public static final DeferredBlock<FoundryOutletBlock> FOUNDRY_SLAGTAP;
    public static final DeferredBlock<DynamicSlagBlock> DYNAMIC_SLAG;
    public static final DeferredBlock<ScaffoldBlock> STEEL_SCAFFOLD;
    public static final DeferredBlock<SteelBeamBlock> STEEL_BEAM;
    public static final DeferredBlock<SteelGrateBlock> STEEL_GRATE;
    public static final DeferredBlock<ConveyorBlock> CONVEYOR;
    public static final DeferredBlock<ConveyorBlock> CONVEYOR_EXPRESS;
    public static final DeferredBlock<ConveyorBlock> CONVEYOR_DOUBLE;
    public static final DeferredBlock<ConveyorBlock> CONVEYOR_TRIPLE;
    public static final DeferredBlock<ConveyorLiftBlock> CONVEYOR_LIFT;
    public static final DeferredBlock<ConveyorChuteBlock> CONVEYOR_CHUTE;
    public static final DeferredBlock<CraneExtractorBlock> CRANE_EXTRACTOR;
    public static final DeferredBlock<CraneInserterBlock> CRANE_INSERTER;
    public static final DeferredBlock<ConveyorBoxerBlock> CRANE_BOXER;
    public static final DeferredBlock<Block> BLOCK_COKE_COAL;
    public static final DeferredBlock<Block> BLOCK_COKE_LIGNITE;
    public static final DeferredBlock<Block> BLOCK_COKE_PETROLEUM;

    static {
        Map<String, DeferredBlock<Block>> blocks = new LinkedHashMap<>();

        for (HazardousMaterialDefinitions.BlockDefinition definition : HazardousMaterialDefinitions.BLOCKS) {
            DeferredBlock<Block> block = BLOCKS.register(
                    definition.id(),
                    () -> {
                        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                                .mapColor(definition.mapColor())
                                .strength(definition.hardness(), definition.modernExplosionResistance())
                                .sound(definition.sound())
                                .lightLevel(state -> definition.lightLevel())
                                .requiresCorrectToolForDrops();
                        if (definition.adjacentWaterExplosion() > 0.0F) {
                            return new HydroactiveBlock(properties, definition.adjacentWaterExplosion());
                        }
                        if (definition.placedEmission() > 0.0F || definition.radiationFog()) {
                            return new RadioactiveBlock(properties, definition.placedEmission(), definition.radiationFog());
                        }
                        return new Block(properties);
                    }
            );
            blocks.put(definition.id(), block);
        }

        for (MaterialDefinitions.BlockDefinition definition : MaterialDefinitions.BLOCKS) {
            DeferredBlock<Block> block = BLOCKS.register(
                    definition.id(),
                    () -> new Block(BlockBehaviour.Properties.of()
                            .mapColor(definition.mapColor())
                            .strength(definition.hardness(), definition.modernExplosionResistance())
                            .sound(definition.sound())
                            .requiresCorrectToolForDrops())
            );
            blocks.put(definition.id(), block);
        }

        MATERIAL_BLOCKS = Collections.unmodifiableMap(blocks);
        URANIUM_BLOCK = get("block_uranium");
        ORE_TITANIUM = registerEarlyOre("ore_titanium");
        ORE_TUNGSTEN = registerEarlyOre("ore_tungsten");
        ORE_COBALT = registerEarlyOre("ore_cobalt");
        ORE_RARE = registerEarlyOre("ore_rare");
        ORE_COLTAN = BLOCKS.register("ore_coltan", () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE).strength(15.0F, 6.0F).sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));
        STONE_RESOURCE = BLOCKS.register("stone_resource", () -> new StoneResourceBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops()));

        Map<String, DeferredBlock<Block>> legacyOres = new LinkedHashMap<>();
        String[] ordinary = {"ore_uranium", "ore_thorium", "ore_sulfur", "ore_aluminium", "ore_copper",
                "ore_fluorite", "ore_niter", "ore_lead", "ore_beryllium", "ore_lignite", "ore_asbestos",
                "ore_cinnebar", "ore_alexandrite"};
        String[] clusters = {"cluster_iron", "cluster_titanium", "cluster_aluminium", "cluster_copper"};
        String[] gneiss = {"stone_gneiss", "ore_gneiss_iron", "ore_gneiss_gold", "ore_gneiss_uranium",
                "ore_gneiss_copper", "ore_gneiss_asbestos", "ore_gneiss_lithium", "ore_gneiss_rare",
                "ore_gneiss_gas"};
        String[] depth = {"stone_depth", "cluster_depth_iron", "cluster_depth_titanium",
                "cluster_depth_tungsten", "ore_depth_cinnebar", "ore_depth_zirconium", "ore_depth_borax",
                "stone_depth_nether", "ore_depth_nether_neodymium"};
        String[] nether = {"ore_nether_uranium", "ore_nether_tungsten", "ore_nether_sulfur",
                "ore_nether_fire", "ore_nether_coal", "ore_nether_cobalt", "ore_nether_plutonium"};
        registerLegacyOres(legacyOres, ordinary);
        registerLegacyOres(legacyOres, clusters);
        registerLegacyOres(legacyOres, gneiss);
        registerLegacyOres(legacyOres, depth);
        registerLegacyOres(legacyOres, nether);
        registerLegacyOres(legacyOres, "ore_tikite");
        legacyOres.put("ore_oil_sand", BLOCKS.register("ore_oil_sand", () -> new ColoredFallingBlock(
                new ColorRGBA(0x40372AFF), BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                .strength(0.5F, 1.0F).sound(SoundType.SAND))));
        legacyOres.put("ore_bedrock_oil", BLOCKS.register("ore_bedrock_oil", () -> new Block(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(-1.0F, 1_000_000.0F)
                        .sound(SoundType.STONE))));
        legacyOres.put("ore_bedrock", BLOCKS.register("ore_bedrock", () -> new Block(
                BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(-1.0F, 1_000_000.0F)
                        .sound(SoundType.STONE))));
        legacyOres.put("gas_flammable", BLOCKS.register("gas_flammable", () -> new LegacyGasBlock(
                BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().strength(0.0F), false)));
        legacyOres.put("gas_explosive", BLOCKS.register("gas_explosive", () -> new LegacyGasBlock(
                BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().strength(0.0F), true)));
        legacyOres.put("gas_coal", BLOCKS.register("gas_coal", () -> new CoalDustGasBlock(
                BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().strength(0.0F))));
        legacyOres.put("gas_asbestos", BLOCKS.register("gas_asbestos", () -> new AsbestosGasBlock(
                BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().strength(0.0F))));
        legacyOres.put("gas_monoxide", BLOCKS.register("gas_monoxide", () -> new CarbonMonoxideGasBlock(
                BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().strength(0.0F))));
        legacyOres.put("gas_radon", BLOCKS.register("gas_radon", () -> new RadonGasBlock(
                BlockBehaviour.Properties.of().replaceable().noCollission().noOcclusion().strength(0.0F))));
        LEGACY_ORE_BLOCKS = Collections.unmodifiableMap(legacyOres);
        ORE_OIL = BLOCKS.register("ore_oil", () -> new OilDepositBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE).strength(5.0F, 6.0F).sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));
        ORE_OIL_EMPTY = BLOCKS.register("ore_oil_empty", () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE).strength(5.0F, 6.0F).sound(SoundType.STONE)
                .requiresCorrectToolForDrops()));
        DIRT_OILY = BLOCKS.register("dirt_oily", () -> new ColoredFallingBlock(new ColorRGBA(0x252019FF),
                BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));
        DIRT_DEAD = BLOCKS.register("dirt_dead", () -> new ColoredFallingBlock(new ColorRGBA(0x675D4BFF),
                BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F).sound(SoundType.GRAVEL)));
        SAND_DIRTY = BLOCKS.register("sand_dirty", () -> new ColoredFallingBlock(new ColorRGBA(0x75674CFF),
                BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.5F).sound(SoundType.SAND)));
        SAND_DIRTY_RED = BLOCKS.register("sand_dirty_red", () -> new ColoredFallingBlock(new ColorRGBA(0x8B4C36FF),
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.5F).sound(SoundType.SAND)));
        STONE_CRACKED = BLOCKS.register("stone_cracked", () -> new ColoredFallingBlock(new ColorRGBA(0x6D6D6DFF),
                BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.0F, 0.0F).sound(SoundType.STONE)));
        PLANT_DEAD = BLOCKS.register("plant_dead", () -> new DeadPlantBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT).noCollission().instabreak().sound(SoundType.GRASS)));
        OIL_SPILL = BLOCKS.register("oil_spill", () -> new OilSpillBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK).strength(0.1F, 0.0F).sound(SoundType.SNOW)
                .noCollission().noOcclusion().replaceable()));
        OIL_PIPE = BLOCKS.register("oil_pipe", () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(5.0F, 6.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
        WASTE_EARTH = BLOCKS.register("waste_earth", () -> new WasteEarthBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.6F)
                        .sound(SoundType.GRASS).randomTicks(), WasteEarthBlock.Variant.WASTE));
        WASTE_MYCELIUM = BLOCKS.register("waste_mycelium", () -> new WasteEarthBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.6F)
                        .sound(SoundType.GRASS).lightLevel(state -> 15).randomTicks(), WasteEarthBlock.Variant.MYCELIUM));
        WASTE_TRINITITE = BLOCKS.register("waste_trinitite", () -> new WasteOreBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.5F, 1.5F)
                        .sound(SoundType.SAND), WasteOreBlock.Type.TRINITITE));
        WASTE_TRINITITE_RED = BLOCKS.register("waste_trinitite_red", () -> new WasteOreBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(0.5F, 1.5F)
                        .sound(SoundType.SAND), WasteOreBlock.Type.TRINITITE));
        WASTE_LOG = BLOCKS.register("waste_log", () -> new WasteLogBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(5.0F, 1.5F)
                        .sound(SoundType.WOOD).ignitedByLava(), false));
        WASTE_PLANKS = BLOCKS.register("waste_planks", () -> new WasteOreBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F, 1.5F)
                        .sound(SoundType.WOOD).ignitedByLava(), WasteOreBlock.Type.CHARRED_PLANKS));
        // Frozen dirt: now 40% less dirt and 100% more snowball.
        // frozen_grass never ticked, and it is not starting now.
        FROZEN_DIRT = BLOCKS.register("frozen_dirt", () -> new FrozenBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5F, 1.5F)
                        .sound(SoundType.GLASS), true));
        FROZEN_GRASS = BLOCKS.register("frozen_grass", () -> new WasteEarthBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.5F, 1.5F)
                        .sound(SoundType.GLASS), WasteEarthBlock.Variant.FROZEN));
        FROZEN_LOG = BLOCKS.register("frozen_log", () -> new WasteLogBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F, 1.5F)
                        .sound(SoundType.GLASS), true));
        FROZEN_PLANKS = BLOCKS.register("frozen_planks", () -> new FrozenBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F, 1.5F)
                        .sound(SoundType.GLASS), false));
        BLOCK_TRINITITE = BLOCKS.register("block_trinitite", () -> new RadioactiveBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops(), 0.1F));
        BLOCK_WASTE = BLOCKS.register("block_waste", () -> new RadioactiveBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops(), 15.0F, true));
        SELLAFIELD = BLOCKS.register("sellafield", () -> new SellafieldBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(5.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops().randomTicks()));
        SELLAFIELD_SLAKED = BLOCKS.register("sellafield_slaked", () -> new Block(
                BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops()));
        MACHINE_WELL = BLOCKS.register("machine_well", () -> new OilDerrickBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(5.0F, 12.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion()));
        RADIO_TORCH_SENDER = radioTorch("radio_torch_sender", RadioTorchBlock.Kind.SENDER);
        RADIO_TORCH_RECEIVER = radioTorch("radio_torch_receiver", RadioTorchBlock.Kind.RECEIVER);
        RADIO_TORCH_COUNTER = radioTorch("radio_torch_counter", RadioTorchBlock.Kind.COUNTER);
        RADIO_TORCH_LOGIC = radioTorch("radio_torch_logic", RadioTorchBlock.Kind.LOGIC);
        RADIO_TORCH_READER = radioTorch("radio_torch_reader", RadioTorchBlock.Kind.READER);
        RADIO_TORCH_CONTROLLER = radioTorch("radio_torch_controller", RadioTorchBlock.Kind.CONTROLLER);
        MACHINE_DIESEL = BLOCKS.register("machine_diesel", () -> new DieselGeneratorBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_COMBUSTION_ENGINE = BLOCKS.register("machine_combustion_engine", () -> new CombustionEngineBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));
        DFC_CORE = BLOCKS.register("dfc_core", () -> new DfcCoreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion()));
        DFC_EMITTER = BLOCKS.register("dfc_emitter", () -> new DfcComponentBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion(), DfcKind.EMITTER));
        DFC_INJECTOR = BLOCKS.register("dfc_injector", () -> new DfcComponentBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion(), DfcKind.INJECTOR));
        DFC_RECEIVER = BLOCKS.register("dfc_receiver", () -> new DfcComponentBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion(), DfcKind.RECEIVER));
        DFC_STABILIZER = BLOCKS.register("dfc_stabilizer", () -> new DfcComponentBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(5.0F, 10.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion(), DfcKind.STABILIZER));
        MACHINE_FLUIDTANK = BLOCKS.register("machine_fluidtank", () -> new FluidStorageTankBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 12.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));
        RED_CABLE = BLOCKS.register(
                "red_cable",
                () -> new HeCableBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()
                        .noOcclusion())
        );
        MACHINE_BATTERY_SOCKET = BLOCKS.register(
                "machine_battery_socket",
                () -> new BatterySocketBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion())
        );
        MACHINE_BATTERY_REDD = BLOCKS.register(
                "machine_battery_redd",
                () -> new FensuBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()
                        .noOcclusion())
        );
        MACHINE_PRESS = BLOCKS.register(
                "machine_press",
                () -> new MachinePressBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops()
                        .noOcclusion())
        );
        PRESS_PREHEATER = BLOCKS.register(
                "press_preheater",
                () -> new Block(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops())
        );
        MACHINE_SHREDDER = BLOCKS.register(
                "machine_shredder",
                () -> new MachineShredderBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops())
        );
        GRAVEL_OBSIDIAN = BLOCKS.register(
                "gravel_obsidian",
                () -> new ColoredFallingBlock(new ColorRGBA(0x2A1F35FF), BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_BLACK)
                        .strength(5.0F, 144.0F)
                        .sound(SoundType.GRAVEL)
                        .requiresCorrectToolForDrops())
        );
        GRAVEL_DIAMOND = BLOCKS.register(
                "gravel_diamond",
                () -> new ColoredFallingBlock(new ColorRGBA(0x58D8D0FF), BlockBehaviour.Properties.of()
                        .mapColor(MapColor.DIAMOND)
                        .strength(0.6F, 0.0F)
                        .sound(SoundType.GRAVEL))
        );
        HEATER_FIREBOX = BLOCKS.register(
                "heater_firebox",
                () -> new FireboxBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion())
        );
        HEATER_OVEN = BLOCKS.register(
                "heater_oven",
                () -> new HeatingOvenBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion())
        );
        MACHINE_ASHPIT = BLOCKS.register(
                "machine_ashpit",
                () -> new AshpitBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion())
        );
        MACHINE_STIRLING = BLOCKS.register(
                "machine_stirling",
                () -> new StirlingBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion())
        );
        MACHINE_SAWMILL = BLOCKS.register(
                "machine_sawmill",
                () -> new SawmillBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion())
        );
        MACHINE_STEAM_ENGINE = BLOCKS.register(
                "machine_steam_engine",
                () -> new SteamEngineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion())
        );
        MACHINE_INDUSTRIAL_TURBINE = BLOCKS.register(
                "machine_industrial_turbine",
                () -> new IndustrialTurbineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 10.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion())
        );
        MACHINE_TURBINE_GAS = BLOCKS.register(
                "machine_turbinegas",
                () -> new GasTurbineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 10.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion())
        );
        MACHINE_TURBOFAN = BLOCKS.register(
                "machine_turbofan",
                () -> new TurbofanBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 10.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion())
        );
        MACHINE_TURBINE = BLOCKS.register(
                "machine_turbine",
                () -> new SteamTurbineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops())
        );
        REACTOR_ZIRNOX = BLOCKS.register(
                "machine_zirnox",
                () -> new ZirnoxBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 60.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion())
        );
        ZIRNOX_DESTROYED = BLOCKS.register(
                "zirnox_destroyed",
                () -> new ZirnoxDestroyedBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(100.0F, 480.0F)
                        .sound(SoundType.METAL).noLootTable().noOcclusion())
        );
        MACHINE_REACTOR_BREEDING = BLOCKS.register(
                "machine_reactor_breeding",
                () -> new BreedingReactorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion())
        );
        REACTOR_RESEARCH = BLOCKS.register(
                "machine_reactor_small",
                () -> new ResearchReactorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 10.0F)
                        .sound(SoundType.METAL).noOcclusion())
        );
        MACHINE_RADGEN = BLOCKS.register(
                "machine_radgen",
                () -> new RadGenBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL).noOcclusion())
        );
        MACHINE_WASTE_DRUM = BLOCKS.register(
                "machine_waste_drum",
                () -> new WasteDrumBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL))
        );
        MACHINE_SIREN = BLOCKS.register("machine_siren", () -> new SirenBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL)));
        PUMP_STEAM = BLOCKS.register(
                "pump_steam",
                () -> new PumpBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion(), false)
        );
        PUMP_ELECTRIC = BLOCKS.register(
                "pump_electric",
                () -> new PumpBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion(), true)
        );
        MACHINE_INTAKE = BLOCKS.register("machine_intake",
                () -> new AirIntakeBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(10.0F, 12.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_CONDENSER = BLOCKS.register(
                "machine_condenser",
                () -> new SteamCondenserBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops())
        );
        MACHINE_CONDENSER_POWERED = BLOCKS.register(
                "machine_condenser_powered",
                () -> new HighPowerCondenserBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion())
        );
        REINFORCED_STONE = BLOCKS.register("reinforced_stone",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(15.0F, 60.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        GNEISS_TILE = BLOCKS.register("gneiss_tile",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(1.5F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        GNEISS_BRICK = BLOCKS.register("gneiss_brick",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(1.5F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        GNEISS_CHISELED = BLOCKS.register("gneiss_chiseled",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(1.5F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        REINFORCED_LIGHT = BLOCKS.register("reinforced_light",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(15.0F, 48.0F).sound(SoundType.STONE).lightLevel(state -> 15)
                        .requiresCorrectToolForDrops()));
        REINFORCED_SAND = BLOCKS.register("reinforced_sand",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(15.0F, 24.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        DEPTH_BRICK = registerDepthBlock("depth_brick");
        DEPTH_TILES = registerDepthBlock("depth_tiles");
        DEPTH_NETHER_BRICK = registerDepthBlock("depth_nether_brick");
        DEPTH_NETHER_TILES = registerDepthBlock("depth_nether_tiles");
        MACHINE_BOILER = BLOCKS.register("machine_boiler",
                () -> new HeatBoilerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FLUID_DUCT_NEO = BLOCKS.register("fluid_duct_neo",
                () -> new FluidDuctBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        DYNAMITE = registerExplosive("dynamite", 8.0F);
        TNT_NTM = registerExplosive("tnt_ntm", 10.0F);
        SEMTEX = registerExplosive("semtex", 12.0F);
        C4 = registerExplosive("c4", 15.0F);
        CHARGE_DYNAMITE = registerCharge(ChargeType.DYNAMITE);
        CHARGE_MINER = registerCharge(ChargeType.MINER);
        CHARGE_C4 = registerCharge(ChargeType.C4);
        CHARGE_SEMTEX = registerCharge(ChargeType.SEMTEX);
        // Mines, ordered from rude to maritime.
        MINE_AP = registerMine("mine_ap", LandmineBlock.Type.AP);
        MINE_SHRAP = registerMine("mine_shrap", LandmineBlock.Type.SHRAP);
        MINE_HE = registerMine("mine_he", LandmineBlock.Type.HE);
        MINE_FAT = registerMine("mine_fat", LandmineBlock.Type.FAT);
        MINE_NAVAL = registerMine("mine_naval", LandmineBlock.Type.NAVAL);
        NUKE_GADGET = registerLargeNuke("nuke_gadget", LargeNukeType.GADGET);
        NUKE_BOY = registerLargeNuke("nuke_boy", LargeNukeType.BOY);
        NUKE_MAN = BLOCKS.register("nuke_man", () -> new NukeManBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL).noOcclusion()));
        NUKE_MIKE = registerLargeNuke("nuke_mike", LargeNukeType.MIKE);
        NUKE_TSAR = registerLargeNuke("nuke_tsar", LargeNukeType.TSAR);
        // 200 old resistance = 120 new resistance. Thanks, Mojang.
        NUKE_FLEIJA = BLOCKS.register("nuke_fleija", () -> new NukeFleijaBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL).noOcclusion()));
        // Same 5/120 shell as the other expensive lawn ornaments.
        NUKE_PROTOTYPE = BLOCKS.register("nuke_prototype", () -> new NukePrototypeBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL).noOcclusion()));
        // Iron, hardness 5, resistance 120. The usual bomb suit.
        NUKE_CUSTOM = BLOCKS.register("nuke_custom", () -> new NukeCustomBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL).noOcclusion()));
        // Blue bomb, same old iron pajamas.
        NUKE_SOLINIUM = BLOCKS.register("nuke_solinium", () -> new NukeSoliniumBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL).noOcclusion()));
        // N2 also gets the standard 5/120 steel lunchbox.
        NUKE_N2 = BLOCKS.register("nuke_n2", () -> new NukeN2Block(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL).noOcclusion()));
        // Balefire bomb: flammable contents, reassuringly ordinary casing.
        NUKE_FSTBMB = BLOCKS.register("nuke_fstbmb", () -> new NukeBalefireBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL).noOcclusion()));
        // Zero hardness, 120 resistance. Touch gently, explode firmly.
        BOMB_MULTI = BLOCKS.register("bomb_multi", () -> new BombMultiBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(0.0F, 120.0F)
                        .sound(SoundType.METAL).noOcclusion()));
        // Full cube, no tool requirement, and enough resistance to make mining it annoying.
        FLOAT_BOMB = BLOCKS.register("float_bomb", () -> new LevitationBombBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL)));
        // One bomb class, two thermostats, neither has a tool requirement.
        THERM_ENDO = BLOCKS.register("therm_endo", () -> new BombThermoBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL), BombThermoBlock.Type.ENDOTHERMIC));
        THERM_EXO = BLOCKS.register("therm_exo", () -> new BombThermoBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL), BombThermoBlock.Type.EXOTHERMIC));
        // Green fire: bright, intangible, unobtainable and deeply impolite.
        BALEFIRE = BLOCKS.register("balefire", () -> new BalefireBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).noCollission().instabreak()
                        .lightLevel(state -> 15).sound(SoundType.WOOL).replaceable().noLootTable()
                        .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));
        FALLOUT = BLOCKS.register("fallout", () -> new FalloutBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(0.1F)
                        .sound(SoundType.GRAVEL).noOcclusion().replaceable()));
        ASH_DIGAMMA = BLOCKS.register("ash_digamma", () -> new DigammaAshBlock(
                new ColorRGBA(0x959190FF), BlockBehaviour.Properties.of().mapColor(MapColor.SAND)
                        .strength(0.5F, 90.0F).sound(SoundType.SAND)));
        VENT_CHLORINE = BLOCKS.register("vent_chlorine", () -> new ChlorineVentBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops()));
        CHLORINE_GAS = BLOCKS.register("chlorine_gas", () -> new ChlorineGasBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GREEN)
                        .replaceable().noCollission().noOcclusion().strength(0.0F, 0.0F)));
        VENT_CHLORINE_SEAL = BLOCKS.register("vent_chlorine_seal", () -> new ChlorineSealBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 6.0F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops()));
        GEIGER = BLOCKS.register("geiger", () -> new GeigerCounterBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(15.0F, 0.15F)
                        .sound(SoundType.STONE).requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_ARMOR_TABLE = BLOCKS.register("machine_armor_table", () -> new ArmorTableBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F)
                        .sound(SoundType.METAL).requiresCorrectToolForDrops()));
        BLOCK_INSULATOR = BLOCKS.register("block_insulator", () -> new RotatedPillarBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(5.0F, 6.0F)
                        .sound(SoundType.WOOL)));
        SANDBAGS = BLOCKS.register("sandbags", () -> new SandbagsBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(5.0F, 18.0F)
                        .sound(SoundType.STONE).noOcclusion()));
        ANVIL_IRON = registerAnvil("anvil_iron", 1);
        ANVIL_LEAD = registerAnvil("anvil_lead", 1);
        ANVIL_STEEL = registerAnvil("anvil_steel", 2);
        ANVIL_DESH = registerAnvil("anvil_desh", 3);
        ANVIL_FERROURANIUM = registerAnvil("anvil_ferrouranium", 4);
        ANVIL_SATURNITE = registerAnvil("anvil_saturnite", 5);
        ANVIL_BISMUTH_BRONZE = registerAnvil("anvil_bismuth_bronze", 5);
        ANVIL_ARSENIC_BRONZE = registerAnvil("anvil_arsenic_bronze", 5);
        ANVIL_SCHRABIDATE = registerAnvil("anvil_schrabidate", 6);
        ANVIL_DNT = registerAnvil("anvil_dnt", 7);
        ANVIL_OSMIRIDIUM = registerAnvil("anvil_osmiridium", 8);
        ANVIL_MURKY = registerAnvil("anvil_murky", 1_916_169);
        HEATER_ELECTRIC = BLOCKS.register("heater_electric",
                () -> new ElectricHeaterBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        HEATER_OILBURNER = BLOCKS.register("heater_oilburner",
                () -> new FluidBurnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        HEATER_HEATEX = BLOCKS.register("heater_heatex",
                () -> new HeatExchangerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_BLAST_FURNACE = BLOCKS.register("machine_blast_furnace",
                () -> new BlastFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(5.0F, 6.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FURNACE_COMBINATION = BLOCKS.register("furnace_combination",
                () -> new CombinationOvenBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(5.0F, 6.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FURNACE_STEEL = BLOCKS.register("furnace_steel",
                () -> new SteelFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_ELECTRIC_FURNACE = BLOCKS.register("machine_electric_furnace_off",
                () -> new ElectricFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
                        .lightLevel(state -> state.getValue(ElectricFurnaceBlock.LIT) ? 15 : 0)));
        MACHINE_FURNACE_BRICK = BLOCKS.register("machine_furnace_brick_off",
                () -> new BrickFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()
                        .lightLevel(state -> state.getValue(BrickFurnaceBlock.LIT) ? 15 : 0)));
        MACHINE_WOOD_BURNER = BLOCKS.register("machine_wood_burner",
                () -> new WoodBurnerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 10.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_MICROWAVE = BLOCKS.register("machine_microwave",
                () -> new MicrowaveBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_ASSEMBLY_MACHINE = BLOCKS.register("machine_assembly_machine",
                () -> new AssemblyMachineBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 18.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_CHEMICAL_PLANT = BLOCKS.register("machine_chemical_plant",
                () -> new ChemicalPlantBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 18.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_SOLDERING_STATION = BLOCKS.register("machine_soldering_station",
                () -> new SolderingStationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 18.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_TRANSFORMER = BLOCKS.register("machine_transformer",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()));
        MACHINE_ARC_WELDER = BLOCKS.register("machine_arc_welder",
                () -> new ArcWelderBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 18.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_ARC_FURNACE = BLOCKS.register("machine_arc_furnace",
                () -> new ArcFurnaceBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 12.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_REFINERY = BLOCKS.register("machine_refinery",
                () -> new RefineryBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 12.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_CENTRIFUGE = BLOCKS.register("machine_centrifuge",
                () -> new CentrifugeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_CATALYTIC_CRACKER = BLOCKS.register("machine_catalytic_cracker",
                () -> new CrackingTowerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_FRACTION_TOWER = BLOCKS.register("machine_fraction_tower",
                () -> new FractionTowerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FRACTION_SPACER = BLOCKS.register("fraction_spacer",
                () -> new FractionTowerSeparatorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 6.0F).sound(SoundType.METAL)
                        .requiresCorrectToolForDrops().noOcclusion()));
        MACHINE_CRUCIBLE = BLOCKS.register("machine_crucible",
                () -> new CrucibleBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(5.0F, 6.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FOUNDRY_MOLD = BLOCKS.register("foundry_mold",
                () -> new FoundryMoldBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(5.0F, 6.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FOUNDRY_BASIN = BLOCKS.register("foundry_basin",
                () -> new FoundryBasinBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(5.0F, 6.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FOUNDRY_CHANNEL = BLOCKS.register("foundry_channel",
                () -> new FoundryChannelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(5.0F, 10.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FOUNDRY_TANK = BLOCKS.register("foundry_tank",
                () -> new FoundryTankBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(5.0F, 10.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FOUNDRY_OUTLET = BLOCKS.register("foundry_outlet",
                () -> new FoundryOutletBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(5.0F, 10.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        FOUNDRY_SLAGTAP = BLOCKS.register("foundry_slagtap",
                () -> new FoundryOutletBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(5.0F, 10.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        DYNAMIC_SLAG = BLOCKS.register("slag",
                () -> new DynamicSlagBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
                        .strength(5.0F, 10.0F).sound(SoundType.STONE).noOcclusion().dynamicShape()));
        STEEL_SCAFFOLD = BLOCKS.register("steel_scaffold",
                () -> new ScaffoldBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                        .strength(5.0F, 9.0F).sound(SoundType.STONE)
                        .requiresCorrectToolForDrops().noOcclusion()));
        STEEL_BEAM = BLOCKS.register("steel_beam", () -> new SteelBeamBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(5.0F, 9.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion()));
        STEEL_GRATE = BLOCKS.register("steel_grate", () -> new SteelGrateBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(2.0F, 3.0F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion()));
        CONVEYOR = registerConveyor("conveyor", ConveyorType.REGULAR);
        CONVEYOR_EXPRESS = registerConveyor("conveyor_express", ConveyorType.EXPRESS);
        CONVEYOR_DOUBLE = registerConveyor("conveyor_double", ConveyorType.DOUBLE);
        CONVEYOR_TRIPLE = registerConveyor("conveyor_triple", ConveyorType.TRIPLE);
        CONVEYOR_LIFT = BLOCKS.register("conveyor_lift", () -> new ConveyorLiftBlock(conveyorProperties()));
        CONVEYOR_CHUTE = BLOCKS.register("conveyor_chute", () -> new ConveyorChuteBlock(conveyorProperties()));
        CRANE_EXTRACTOR = BLOCKS.register("crane_extractor", () -> new CraneExtractorBlock(craneProperties()));
        CRANE_INSERTER = BLOCKS.register("crane_inserter", () -> new CraneInserterBlock(craneProperties()));
        CRANE_BOXER = BLOCKS.register("crane_boxer", () -> new ConveyorBoxerBlock(
                craneProperties()));
        BLOCK_COKE_COAL = registerCokeBlock("block_coke_coal");
        BLOCK_COKE_LIGNITE = registerCokeBlock("block_coke_lignite");
        BLOCK_COKE_PETROLEUM = registerCokeBlock("block_coke_petroleum");
    }

    private ModBlocks() {
    }

    private static DeferredBlock<ConveyorBlock> registerConveyor(String id, ConveyorType type) {
        return BLOCKS.register(id, () -> new ConveyorBlock(conveyorProperties(), type));
    }

    private static BlockBehaviour.Properties conveyorProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F)
                .sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion();
    }

    private static BlockBehaviour.Properties craneProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 10.0F)
                .sound(SoundType.STONE).requiresCorrectToolForDrops();
    }

    private static DeferredBlock<Block> registerEarlyOre(String id) {
        return BLOCKS.register(id, () -> new LegacyOreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops(), legacyDrop(id)));
    }

    private static DeferredBlock<Block> registerCokeBlock(String id) {
        return BLOCKS.register(id, () -> new Block(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .strength(5.0F, 6.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> registerDepthBlock(String id) {
        return BLOCKS.register(id, () -> new LegacyOreBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops(), LegacyOreBlock.Drop.self(), true));
    }

    private static void registerLegacyOres(Map<String, DeferredBlock<Block>> blocks, String... ids) {
        for (String id : ids) blocks.put(id, BLOCKS.register(id, () -> createLegacyOre(id)));
    }

    private static Block createLegacyOre(String id) {
        float hardness = 5.0F;
        float resistance = 6.0F;
        MapColor color = MapColor.STONE;
        SoundType sound = SoundType.STONE;
        if (id.startsWith("ore_gneiss_") || id.equals("stone_gneiss")) hardness = 1.5F;
        if (id.startsWith("ore_nether_")) hardness = 0.4F;
        if (id.startsWith("cluster_") && !id.startsWith("cluster_depth_")) resistance = 9.0F;
        boolean depthRock = id.startsWith("stone_depth") || id.startsWith("cluster_depth_")
                || id.startsWith("ore_depth_") || id.equals("ore_alexandrite");
        if (depthRock) hardness = -1.0F;
        if (id.equals("ore_asbestos") || id.equals("ore_gneiss_asbestos")) {
            return new AsbestosOreBlock(BlockBehaviour.Properties.of().mapColor(color).strength(hardness, resistance)
                    .sound(sound).requiresCorrectToolForDrops().randomTicks(), legacyDrop(id));
        }
        if (id.equals("ore_uranium") || id.equals("ore_gneiss_uranium") || id.equals("ore_nether_uranium")) {
            BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().mapColor(color)
                    .strength(hardness, resistance).sound(sound).requiresCorrectToolForDrops().randomTicks();
            return new UraniumOutgassingOreBlock(properties, legacyDrop(id));
        }
        if (id.equals("ore_nether_coal")) {
            return new NetherCoalOreBlock(BlockBehaviour.Properties.of().mapColor(color).strength(hardness, resistance)
                    .sound(sound).lightLevel(state -> 10).requiresCorrectToolForDrops(), legacyDrop(id));
        }
        return new LegacyOreBlock(BlockBehaviour.Properties.of().mapColor(color).strength(hardness, resistance)
                .sound(sound).requiresCorrectToolForDrops(), legacyDrop(id), depthRock);
    }

    private static LegacyOreBlock.Drop legacyDrop(String id) {
        return switch (id) {
            case "ore_sulfur", "ore_nether_sulfur" -> LegacyOreBlock.Drop.item("sulfur", 2, 4, true);
            case "ore_fluorite" -> LegacyOreBlock.Drop.item("fluorite", 2, 4, true);
            case "ore_niter" -> LegacyOreBlock.Drop.item("niter", 2, 4, true);
            case "ore_lignite" -> LegacyOreBlock.Drop.item("lignite");
            case "ore_asbestos", "ore_gneiss_asbestos" -> LegacyOreBlock.Drop.item("ingot_asbestos");
            case "ore_cinnebar" -> LegacyOreBlock.Drop.item("cinnebar");
            case "ore_rare", "ore_gneiss_rare" -> LegacyOreBlock.Drop.chunk(com.hbm.ntm.item.OreChunkItem.ChunkType.RARE);
            case "ore_cobalt" -> LegacyOreBlock.Drop.item("fragment_cobalt", 4, 9, true);
            case "ore_nether_cobalt" -> LegacyOreBlock.Drop.item("fragment_cobalt", 5, 12, true);
            case "cluster_iron", "cluster_depth_iron" -> LegacyOreBlock.Drop.item("crystal_iron");
            case "cluster_titanium", "cluster_depth_titanium" -> LegacyOreBlock.Drop.item("crystal_titanium");
            case "cluster_aluminium" -> LegacyOreBlock.Drop.item("crystal_aluminium");
            case "cluster_copper" -> LegacyOreBlock.Drop.item("crystal_copper");
            case "cluster_depth_tungsten" -> LegacyOreBlock.Drop.item("crystal_tungsten");
            case "ore_depth_cinnebar" -> LegacyOreBlock.Drop.item("cinnebar", 2, 4, true);
            case "ore_depth_zirconium" -> LegacyOreBlock.Drop.item("nugget_zirconium", 2, 3, true);
            case "ore_depth_borax" -> LegacyOreBlock.Drop.item("powder_borax");
            case "ore_depth_nether_neodymium" -> LegacyOreBlock.Drop.item("fragment_neodymium", 2, 3, true);
            case "ore_alexandrite" -> LegacyOreBlock.Drop.alexandrite();
            case "ore_nether_fire" -> LegacyOreBlock.Drop.netherFire();
            case "ore_nether_coal" -> LegacyOreBlock.Drop.item("coal_infernal");
            default -> LegacyOreBlock.Drop.self();
        };
    }

    public static DeferredBlock<Block> legacy(String id) {
        DeferredBlock<Block> block = LEGACY_ORE_BLOCKS.get(id);
        if (block == null) throw new IllegalArgumentException("Unknown legacy ore block: " + id);
        return block;
    }

    private static DeferredBlock<NtmAnvilBlock> registerAnvil(String id, int tier) {
        return BLOCKS.register(id, () -> new NtmAnvilBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 60.0F)
                        .sound(SoundType.ANVIL).requiresCorrectToolForDrops().noOcclusion(), tier));
    }

    private static DeferredBlock<LargeNukeBlock> registerLargeNuke(String id, LargeNukeType type) {
        return BLOCKS.register(id, () -> new LargeNukeBlock(
                BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0F, 120.0F)
                        .sound(SoundType.METAL).noOcclusion(), type));
    }

    private static DeferredBlock<ConventionalExplosiveBlock> registerExplosive(String id, float blastPower) {
        return BLOCKS.register(id, () -> new ConventionalExplosiveBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.FIRE)
                        .strength(0.0F, 0.0F)
                        .sound(SoundType.GRASS)
                        .ignitedByLava(),
                blastPower
        ));
    }

    private static DeferredBlock<ChargeBlock> registerCharge(ChargeType type) {
        return BLOCKS.register(type.id(), () -> new ChargeBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.FIRE)
                        .strength(0.0F, 0.6F)
                        .sound(SoundType.GRASS)
                        .noOcclusion()
                        .noCollission(),
                type
        ));
    }

    // Landmines are invisible, weak and famously safe to break.
    private static DeferredBlock<LandmineBlock> registerMine(String id, LandmineBlock.Type type) {
        return BLOCKS.register(id, () -> new LandmineBlock(
                BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(1.0F, 1.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion(),
                type
        ));
    }

    public static DeferredBlock<Block> get(String id) {
        DeferredBlock<Block> block = MATERIAL_BLOCKS.get(id);
        if (block == null) {
            throw new IllegalArgumentException("Unknown HBM block: " + id);
        }
        return block;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }

    private static DeferredBlock<RadioTorchBlock> radioTorch(String id, RadioTorchBlock.Kind kind) {
        return BLOCKS.register(id, () -> new RadioTorchBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(0.1F, 6.0F).sound(SoundType.METAL)
                .noCollission().noOcclusion(), kind));
    }
}
