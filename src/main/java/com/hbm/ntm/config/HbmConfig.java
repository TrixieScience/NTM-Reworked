package com.hbm.ntm.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HbmConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_GUIDE_BOOK;
    public static final ModConfigSpec.BooleanValue ENABLE_EXTENDED_LOGGING;
    public static final ModConfigSpec.BooleanValue ENABLE_MKU;
    public static final ModConfigSpec.BooleanValue ENABLE_CONTAMINATION;
    public static final ModConfigSpec.BooleanValue ENABLE_CHUNK_RADIATION;
    public static final ModConfigSpec.DoubleValue NETHER_RADIATION;
    public static final ModConfigSpec.BooleanValue CLEANUP_WASTE_EARTH;
    public static final ModConfigSpec.BooleanValue ENABLE_WASTE_MYCELIUM_SPREAD;
    public static final ModConfigSpec.BooleanValue DISABLE_HOT;
    public static final ModConfigSpec.BooleanValue DISABLE_EXPLOSIVE;
    public static final ModConfigSpec.BooleanValue DISABLE_HYDROACTIVE;
    public static final ModConfigSpec.BooleanValue DISABLE_ASBESTOS;
    public static final ModConfigSpec.BooleanValue DISABLE_COAL_DUST;
    public static final ModConfigSpec.BooleanValue DISABLE_BLINDING;
    public static final ModConfigSpec.IntValue ITEM_HAZARD_DROP_TICKRATE;
    public static final ModConfigSpec.BooleanValue ENABLE_POLLUTION;
    public static final ModConfigSpec.DoubleValue POLLUTION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue SOOT_MOB_THRESHOLD;
    public static final ModConfigSpec.ConfigValue<Integer> GADGET_RADIUS;
    public static final ModConfigSpec.ConfigValue<Integer> BOY_RADIUS;
    public static final ModConfigSpec.ConfigValue<Integer> FAT_MAN_RADIUS;
    public static final ModConfigSpec.ConfigValue<Integer> MIKE_RADIUS;
    public static final ModConfigSpec.ConfigValue<Integer> TSAR_RADIUS;
    public static final ModConfigSpec.ConfigValue<Integer> PROTOTYPE_RADIUS;
    public static final ModConfigSpec.ConfigValue<Integer> FLEIJA_RADIUS;
    public static final ModConfigSpec.ConfigValue<Integer> SOLINIUM_RADIUS;
    public static final ModConfigSpec.ConfigValue<Integer> N2_RADIUS;
    public static final ModConfigSpec.ConfigValue<Integer> EXPLOSION_LIFESPAN_SECONDS;
    public static final ModConfigSpec.ConfigValue<Integer> FLEIJA_BLAST_SPEED;
    public static final ModConfigSpec.ConfigValue<Integer> MK5_BLAST_TIME;
    public static final ModConfigSpec.ConfigValue<Integer> FALLOUT_RANGE;
    public static final ModConfigSpec.ConfigValue<Integer> FALLOUT_DELAY;
    public static final ModConfigSpec.BooleanValue ENABLE_EXPLOSION_CHUNK_LOADING;
    public static final ModConfigSpec.BooleanValue ENABLE_GUNS;
    public static final ModConfigSpec.BooleanValue ENABLE_CROSSHAIRS;
    public static final ModConfigSpec.BooleanValue ENABLE_528;
    public static final ModConfigSpec.BooleanValue DANGEROUS_DROP_STAR;
    public static final ModConfigSpec.BooleanValue DANGEROUS_DROP_DEAD;
    public static final ModConfigSpec.BooleanValue OVERWORLD_ORES;
    public static final ModConfigSpec.IntValue TITANIUM_SPAWN_RATE;
    public static final ModConfigSpec.IntValue TUNGSTEN_SPAWN_RATE;
    public static final ModConfigSpec.IntValue COBALT_SPAWN_RATE;
    public static final ModConfigSpec.IntValue RARE_EARTH_SPAWN_RATE;
    public static final ModConfigSpec.BooleanValue HEMATITE_DEPOSITS;
    public static final ModConfigSpec.BooleanValue MALACHITE_DEPOSITS;
    public static final ModConfigSpec.BooleanValue BAUXITE_DEPOSITS;
    public static final ModConfigSpec.BooleanValue SULFUR_CAVES;
    public static final ModConfigSpec.BooleanValue ASBESTOS_CAVES;
    public static final ModConfigSpec.BooleanValue NETHER_ORES;
    public static final ModConfigSpec.BooleanValue END_ORES;
    public static final ModConfigSpec.IntValue GAS_BUBBLE_RATE;
    public static final ModConfigSpec.IntValue EXPLOSIVE_GAS_BUBBLE_RATE;
    public static final ModConfigSpec.IntValue ALEXANDRITE_RATE;
    public static final ModConfigSpec.IntValue BEDROCK_OIL_RATE;
    public static final ModConfigSpec.IntValue OIL_SAND_RATE;
    public static final ModConfigSpec.BooleanValue REGULAR_COLTAN_ORE;
    public static final ModConfigSpec.BooleanValue PLUTONIUM_NETHER_ORE;
    public static final ModConfigSpec.BooleanValue COLTAN_DEPOSIT;
    public static final ModConfigSpec.IntValue OIL_SPAWN_RATE;
    public static final ModConfigSpec.LongValue DERRICK_POWER_CAPACITY;
    public static final ModConfigSpec.IntValue DERRICK_CONSUMPTION;
    public static final ModConfigSpec.IntValue DERRICK_DELAY;
    public static final ModConfigSpec.IntValue DERRICK_OIL_PER_DEPOSIT;
    public static final ModConfigSpec.IntValue DERRICK_GAS_PER_DEPOSIT_MIN;
    public static final ModConfigSpec.IntValue DERRICK_GAS_PER_DEPOSIT_MAX;
    public static final ModConfigSpec.DoubleValue DERRICK_DRAIN_CHANCE;
    public static final ModConfigSpec.LongValue DIESEL_POWER_CAPACITY;
    public static final ModConfigSpec.IntValue DIESEL_CONFIG_FUEL_CAPACITY;
    public static final ModConfigSpec.DoubleValue DIESEL_EFFICIENCY_MEDIUM;
    public static final ModConfigSpec.DoubleValue DIESEL_EFFICIENCY_HIGH;
    public static final ModConfigSpec.DoubleValue DIESEL_EFFICIENCY_AERO;
    public static final ModConfigSpec.IntValue STEAM_ENGINE_STEAM_CAPACITY;
    public static final ModConfigSpec.IntValue STEAM_ENGINE_SPENT_STEAM_CAPACITY;
    public static final ModConfigSpec.DoubleValue STEAM_ENGINE_EFFICIENCY;
    public static final ModConfigSpec.IntValue INDUSTRIAL_TURBINE_INPUT_CAPACITY;
    public static final ModConfigSpec.IntValue INDUSTRIAL_TURBINE_OUTPUT_CAPACITY;
    public static final ModConfigSpec.DoubleValue INDUSTRIAL_TURBINE_EFFICIENCY;
    public static final ModConfigSpec.LongValue STEAM_TURBINE_POWER_CAPACITY;
    public static final ModConfigSpec.IntValue STEAM_TURBINE_INPUT_CAPACITY;
    public static final ModConfigSpec.IntValue STEAM_TURBINE_OUTPUT_CAPACITY;
    public static final ModConfigSpec.IntValue STEAM_TURBINE_MAX_STEAM_PER_TICK;
    public static final ModConfigSpec.DoubleValue STEAM_TURBINE_EFFICIENCY;
    public static final ModConfigSpec.IntValue WATER_PUMP_GROUND_HEIGHT;
    public static final ModConfigSpec.IntValue WATER_PUMP_GROUND_DEPTH;
    public static final ModConfigSpec.IntValue WATER_PUMP_STEAM_SPEED;
    public static final ModConfigSpec.IntValue WATER_PUMP_ELECTRIC_SPEED;
    public static final ModConfigSpec.IntValue CONDENSER_INPUT_CAPACITY;
    public static final ModConfigSpec.IntValue CONDENSER_OUTPUT_CAPACITY;
    public static final ModConfigSpec.LongValue POWERED_CONDENSER_MAX_POWER;
    public static final ModConfigSpec.IntValue POWERED_CONDENSER_INPUT_CAPACITY;
    public static final ModConfigSpec.IntValue POWERED_CONDENSER_OUTPUT_CAPACITY;
    public static final ModConfigSpec.IntValue POWERED_CONDENSER_POWER_CONSUMPTION;
    public static final ModConfigSpec.DoubleValue MINE_AP_DAMAGE;
    public static final ModConfigSpec.DoubleValue MINE_HE_DAMAGE;
    public static final ModConfigSpec.DoubleValue MINE_SHRAP_DAMAGE;
    public static final ModConfigSpec.DoubleValue MINE_NUKE_DAMAGE;
    public static final ModConfigSpec.DoubleValue MINE_NAVAL_DAMAGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("01_general");
        ENABLE_GUIDE_BOOK = builder
                .comment("Gives the starter Guide Book once when a player first joins this world.")
                .define("1.37_enableGuideBook", true);
        builder.pop();

        builder.push("radiation");
        ENABLE_CONTAMINATION = builder
                .comment("Toggles accumulated living-entity radiation and radiation sickness.")
                .define("RADIATION_00_enableContamination", true);
        ENABLE_CHUNK_RADIATION = builder
                .comment("Toggles the persistent two-dimensional chunk radiation field.")
                .define("RADIATION_01_enableChunkRads", true);
        NETHER_RADIATION = builder
                .comment("Minimum ambient RAD/s in the Nether.")
                .defineInRange("AMBIENT_00_nether", 0.1D, 0.0D, 100000.0D);
        CLEANUP_WASTE_EARTH = builder
                .comment("Lets dead grass and glowing mycelium decay into dirt. Original default: false.")
                .define("RADWORLD_03_regrow", false);
        builder.pop();

        builder.push("ores");
        OVERWORLD_ORES = builder
                .comment("General switch for overworld ores. Special structures such as Oil bubbles are separate.")
                .define("2.D00_overworldOres", true);
        TITANIUM_SPAWN_RATE = builder
                .comment("Amount of Titanium ore veins per chunk. Original default: 8.")
                .defineInRange("2.01_titaniumSpawnrate", 8, 0, 1024);
        TUNGSTEN_SPAWN_RATE = builder
                .comment("Amount of Tungsten ore veins per chunk. Original default: 10.")
                .defineInRange("2.07_tungstenSpawnrate", 10, 0, 1024);
        COBALT_SPAWN_RATE = builder
                .comment("Amount of Cobalt ore veins per chunk. Original default: 2.")
                .defineInRange("2.18_cobaltSpawnRate", 2, 0, 1024);
        RARE_EARTH_SPAWN_RATE = builder
                .comment("Amount of Rare Earth ore veins per chunk. Original default: 6.")
                .defineInRange("2.14_rareEarthSpawnRate", 6, 0, 1024);
        HEMATITE_DEPOSITS = builder
                .comment("Toggles source-style Hematite layer deposits. Original default: true.")
                .define("2.L00_enableHematite", true);
        MALACHITE_DEPOSITS = builder
                .comment("Toggles source-style Malachite layer deposits. Original default: true.")
                .define("2.L01_enableMalachite", true);
        BAUXITE_DEPOSITS = builder.comment("Toggles source-style Bauxite layer deposits.")
                .define("2.L02_enableBauxite", true);
        SULFUR_CAVES = builder.comment("Toggles exposed Sulfur cave strata.")
                .define("2.C00_enableSulfurCave", true);
        ASBESTOS_CAVES = builder.comment("Toggles exposed Asbestos cave strata.")
                .define("2.C01_enableAsbestosCave", true);
        NETHER_ORES = builder.define("2.D01_netherOres", true);
        END_ORES = builder.define("2.D02_endOres", true);
        GAS_BUBBLE_RATE = builder.defineInRange("2.17_gasBubbleSpawnRate", 12, 0, Integer.MAX_VALUE);
        EXPLOSIVE_GAS_BUBBLE_RATE = builder.defineInRange("2.19_explosiveBubbleSpawnRate", 0, 0, Integer.MAX_VALUE);
        ALEXANDRITE_RATE = builder.defineInRange("2.20_alexandriteSpawnRate", 100, 0, Integer.MAX_VALUE);
        BEDROCK_OIL_RATE = builder.defineInRange("2.22_bedrockOilSpawnRate", 200, 0, Integer.MAX_VALUE);
        OIL_SAND_RATE = builder.defineInRange("2.24_oilSandBubbleSpawnRate", 200, 0, Integer.MAX_VALUE);
        PLUTONIUM_NETHER_ORE = builder.comment("Enables the optional source Nether plutonium veins.")
                .define("1.02_enablePlutoniumNetherOre", false);
        OIL_SPAWN_RATE = builder
                .comment("Spawns an ordinary finite Oil bubble every nth overworld chunk. "
                        + "Hot, dry biomes divide this value by three. Original default: 100; 0 disables Oil bubbles.")
                .defineInRange("2.21_oilSpawnRate", 100, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("machines");
        builder.push("derrick");
        DERRICK_POWER_CAPACITY = builder.defineInRange("powerCap", 100_000L, 1L, Long.MAX_VALUE);
        DERRICK_CONSUMPTION = builder.defineInRange("consumption", 100, 0, Integer.MAX_VALUE);
        DERRICK_DELAY = builder.defineInRange("delay", 50, 1, Integer.MAX_VALUE);
        DERRICK_OIL_PER_DEPOSIT = builder.defineInRange("oilPerDeposit", 500, 0, Integer.MAX_VALUE);
        DERRICK_GAS_PER_DEPOSIT_MIN = builder.defineInRange("gasPerDepositMin", 100, 0, Integer.MAX_VALUE);
        DERRICK_GAS_PER_DEPOSIT_MAX = builder.defineInRange("gasPerDepositMax", 500, 0, Integer.MAX_VALUE);
        DERRICK_DRAIN_CHANCE = builder.defineInRange("drainChance", 0.05D, 0.0D, 1.0D);
        builder.pop();
        builder.push("dieselgen");
        DIESEL_POWER_CAPACITY = builder.defineInRange("powerCap", 50_000L, 1L, Long.MAX_VALUE);
        DIESEL_CONFIG_FUEL_CAPACITY = builder
                .comment("Legacy configurable value. The source constructor always uses its fixed 4,000 mB tank.")
                .defineInRange("fuelCap", 16_000, 1, Integer.MAX_VALUE);
        DIESEL_EFFICIENCY_MEDIUM = builder.defineInRange("efficiencyMedium", 0.5D, 0D, 100D);
        DIESEL_EFFICIENCY_HIGH = builder.defineInRange("efficiencyHigh", 0.75D, 0D, 100D);
        DIESEL_EFFICIENCY_AERO = builder.defineInRange("efficiencyAviation", 0.1D, 0D, 100D);
        builder.pop();
        builder.push("steamengine");
        STEAM_ENGINE_STEAM_CAPACITY = builder.defineInRange("steamCap", 2_000, 1, Integer.MAX_VALUE);
        STEAM_ENGINE_SPENT_STEAM_CAPACITY = builder.defineInRange("ldsCap", 20, 1, Integer.MAX_VALUE);
        STEAM_ENGINE_EFFICIENCY = builder.defineInRange("efficiency", 0.85D, 0D, 100D);
        builder.pop();
        builder.push("steamturbineIndustrialMk2");
        INDUSTRIAL_TURBINE_INPUT_CAPACITY = builder.defineInRange(
                "inputTankSize", 750_000, 1_000, Integer.MAX_VALUE);
        INDUSTRIAL_TURBINE_OUTPUT_CAPACITY = builder.defineInRange(
                "outputTankSize", 3_000_000, 1_000, Integer.MAX_VALUE);
        INDUSTRIAL_TURBINE_EFFICIENCY = builder.defineInRange("efficiency", 1.0D, 0D, 100D);
        builder.pop();
        builder.push("steamturbine");
        STEAM_TURBINE_POWER_CAPACITY = builder.defineInRange("maxPower", 1_000_000L, 1L, Long.MAX_VALUE);
        STEAM_TURBINE_INPUT_CAPACITY = builder.defineInRange("inputTankSize", 64_000, 1, Integer.MAX_VALUE);
        STEAM_TURBINE_OUTPUT_CAPACITY = builder.defineInRange("outputTankSize", 128_000, 1, Integer.MAX_VALUE);
        STEAM_TURBINE_MAX_STEAM_PER_TICK = builder.defineInRange("maxSteamPerTick", 6_000, 0,
                Integer.MAX_VALUE);
        STEAM_TURBINE_EFFICIENCY = builder.defineInRange("efficiency", 0.85D, 0D, 100D);
        builder.pop();
        builder.push("waterpump");
        WATER_PUMP_GROUND_HEIGHT = builder.defineInRange("groundHeight", 70, Integer.MIN_VALUE, Integer.MAX_VALUE);
        WATER_PUMP_GROUND_DEPTH = builder.defineInRange("groundDepth", 4, 1, 64);
        WATER_PUMP_STEAM_SPEED = builder.defineInRange("steamSpeed", 1_000, 1, Integer.MAX_VALUE / 100);
        WATER_PUMP_ELECTRIC_SPEED = builder.defineInRange("electricSpeed", 10_000, 1, Integer.MAX_VALUE / 100);
        builder.pop();
        builder.push("condenser");
        CONDENSER_INPUT_CAPACITY = builder.defineInRange("inputTankSize", 100, 1, Integer.MAX_VALUE);
        CONDENSER_OUTPUT_CAPACITY = builder.defineInRange("outputTankSize", 100, 1, Integer.MAX_VALUE);
        builder.pop();
        builder.push("condenserPowered");
        POWERED_CONDENSER_MAX_POWER = builder.defineInRange("maxPower", 10_000_000L, 1L, Long.MAX_VALUE);
        POWERED_CONDENSER_INPUT_CAPACITY = builder.defineInRange(
                "inputTankSize", 1_000_000, 1, Integer.MAX_VALUE);
        POWERED_CONDENSER_OUTPUT_CAPACITY = builder.defineInRange(
                "outputTankSize", 1_000_000, 1, Integer.MAX_VALUE);
        POWERED_CONDENSER_POWER_CONSUMPTION = builder.defineInRange(
                "powerConsumption", 10, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.pop();

        builder.push("hazards");
        DISABLE_ASBESTOS = builder.define("HAZ_00_disableAsbestos", false);
        DISABLE_COAL_DUST = builder.define("HAZ_01_disableCoaldust", false);
        DISABLE_HOT = builder.define("HAZ_02_disableHot", false);
        DISABLE_EXPLOSIVE = builder.define("HAZ_03_disableExplosive", false);
        DISABLE_HYDROACTIVE = builder.define("HAZ_04_disableHydroactive", false);
        DISABLE_BLINDING = builder.define("HAZ_05_disableBlinding", false);
        ITEM_HAZARD_DROP_TICKRATE = builder
                .comment("Ticks between dropped-item hazard checks. The original default is 2.")
                .defineInRange("HAZ_06_dropTickrate", 2, 1, 1200);
        builder.pop();

        builder.push("pollution");
        ENABLE_POLLUTION = builder
                .comment("If disabled, pollution generation, spread and pollution effects do not run.")
                .define("POL_00_enablePollution", true);
        POLLUTION_MULTIPLIER = builder
                .comment("Multiplier for emitted pollution. The effective original default is 3.")
                .defineInRange("POL_01_pollutionMultiplier", 3.0D, 0.0D, 1000.0D);
        SOOT_MOB_THRESHOLD = builder
                .comment("Soot required to strengthen newly spawned hostile mobs.")
                .defineInRange("POL_05_buffMobThreshold", 15.0D, 0.0D, 10000.0D);
        builder.pop();

        builder.push("mines");
        MINE_AP_DAMAGE = builder
                .comment("Base damage of the Anti-Personnel Mine blast. Original default: 10.")
                .defineInRange("mineApDamage", 10.0D, 0.0D, 100000.0D);
        MINE_HE_DAMAGE = builder
                .comment("Base damage of the Anti-Tank Mine blast. Original default: 35.")
                .defineInRange("mineHeDamage", 35.0D, 0.0D, 100000.0D);
        MINE_SHRAP_DAMAGE = builder
                .comment("Base damage of the Shrapnel Mine blast. Original default: 7.5.")
                .defineInRange("mineShrapDamage", 7.5D, 0.0D, 100000.0D);
        MINE_NUKE_DAMAGE = builder
                .comment("Base damage of the Fat Mine blast. Original default: 100.")
                .defineInRange("mineNukeDamage", 100.0D, 0.0D, 100000.0D);
        MINE_NAVAL_DAMAGE = builder
                .comment("Base damage of the Naval Mine blast. Original default: 60.")
                .defineInRange("mineNavalDamage", 60.0D, 0.0D, 100000.0D);
        builder.pop();

        builder.push("nukes");
        GADGET_RADIUS = builder
                .comment("Radius of The Gadget. Original default: 150.")
                .define("3.00_gadgetRadius", 150);
        BOY_RADIUS = builder
                .comment("Radius of Little Boy. Original default: 120.")
                .define("3.01_boyRadius", 120);
        FAT_MAN_RADIUS = builder
                .comment("Radius of the placed Fat Man. Original default: 175.")
                .define("3.02_manRadius", 175);
        MIKE_RADIUS = builder
                .comment("Radius of Ivy Mike. Original default: 250.")
                .define("3.03_mikeRadius", 250);
        TSAR_RADIUS = builder
                .comment("Radius of Tsar Bomba. Original default: 500.")
                .define("3.04_tsarRadius", 500);
        PROTOTYPE_RADIUS = builder
                .comment("Radius of The Prototype's FLEIJA blast. Original default: 150.")
                .define("3.05_prototypeRadius", 150);
        FLEIJA_RADIUS = builder
                .comment("Radius of F.L.E.I.J.A. Original default: 50.")
                .define("3.06_fleijaRadius", 50);
        SOLINIUM_RADIUS = builder
                .comment("Radius of the blue rinse. Original default: 150.")
                .define("3.12_soliniumRadius", 150);
        N2_RADIUS = builder
                .comment("Radius of the N2 mine. Original default: 200.")
                .define("3.13_n2Radius", 200);
        EXPLOSION_LIFESPAN_SECONDS = builder
                .comment("Seconds a procedural explosion may remain unloaded before expiring; 0 disables the limit.")
                .define("6.00_limitExplosionLifespan", 0);
        FLEIJA_BLAST_SPEED = builder
                .comment("Base columns per tick for MK3/FLEIJA explosions. Original default: 1024.")
                .define("6.01_blastSpeed", 1024);
        MK5_BLAST_TIME = builder
                .comment("Maximum milliseconds per tick spent collecting or destroying MK5 explosion work.")
                .define("6.02_mk5BlastTime", 50);
        FALLOUT_RANGE = builder
                .comment("Fallout radius as a percentage of the base 2.5x radius.")
                .define("6.03_falloutRange", 100);
        FALLOUT_DELAY = builder
                .comment("Ticks to wait between fallout chunk computations. Original default: 4.")
                .define("6.04_falloutDelay", 4);
        ENABLE_EXPLOSION_CHUNK_LOADING = builder
                .comment("Allows procedural explosions to keep their central chunk loaded.")
                .define("6.05_enableChunkLoading", true);
        builder.pop();

        builder.push("10_dangerous_drops");
        DANGEROUS_DROP_STAR = builder
                .comment("Whether rigged star blaster cells should explode when dropped.")
                .define("10.02_dropStar", true);
        DANGEROUS_DROP_DEAD = builder
                .comment("Whether dead man's explosives should explode when dropped. Original default: true.")
                .define("10.05_dropDead", true);
        builder.pop();

        builder.push("general");
        ENABLE_EXTENDED_LOGGING = builder
                .comment("Logs detonator uses, nuclear explosions, missile launches, grenades, and similar events.")
                .define("1.18_enableExtendedLogging", false);
        ENABLE_MKU = builder
                .comment("Enables the source MKU contagion system. Original server-config default: true.")
                .define("ENABLE_MKU", true);
        ENABLE_WASTE_MYCELIUM_SPREAD = builder
                .comment("Allows Glowing Mycelium to spread. Original default: false.")
                .define("1.01_enableMyceliumSpread", false);
        ENABLE_GUNS = builder
                .comment("Enables HBM firearm input and firing. Original default: true.")
                .define("1.20_enableGuns", true);
        ENABLE_CROSSHAIRS = builder
                .comment("Shows custom crosshairs while an HBM gun is held. Original default: true.")
                .define("1.22_enableCrosshairs", true);
        builder.pop();

        builder.push("528");
        ENABLE_528 = builder
                .comment("Central 528-mode toggle. Only implemented 528-dependent behavior is affected.")
                .define("enable528Mode", false);
        COLTAN_DEPOSIT = builder
                .comment("Enables the source seed-anchored Coltan deposit. Original default: true.")
                .define("X528_enableColtanDeposit", true);
        REGULAR_COLTAN_ORE = builder.comment("Enables random Coltan veins outside the central deposit.")
                .define("X528_enableColtanSpawning", false);
        builder.pop();

        SPEC = builder.build();
    }

    private HbmConfig() {
    }
}
