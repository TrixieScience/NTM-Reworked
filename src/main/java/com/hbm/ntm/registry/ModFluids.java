package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, HbmNtm.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, HbmNtm.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> SMOKE_TYPE = FLUID_TYPES.register(
            "smoke", () -> new FluidType(FluidType.Properties.create())
    );
    public static final DeferredHolder<Fluid, FlowingFluid> SMOKE = FLUIDS.register(
            "smoke", () -> new BaseFlowingFluid.Source(smokeProperties())
    );
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SMOKE = FLUIDS.register(
            "flowing_smoke", () -> new BaseFlowingFluid.Flowing(smokeProperties())
    );
    public static final DeferredHolder<FluidType, FluidType> STEAM_TYPE = FLUID_TYPES.register(
            "steam", () -> new FluidType(FluidType.Properties.create().density(-100).temperature(373)));
    public static final DeferredHolder<Fluid, FlowingFluid> STEAM = FLUIDS.register(
            "steam", () -> new BaseFlowingFluid.Source(steamProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_STEAM = FLUIDS.register(
            "flowing_steam", () -> new BaseFlowingFluid.Flowing(steamProperties()));
    public static final DeferredHolder<FluidType, FluidType> HOTSTEAM_TYPE = FLUID_TYPES.register(
            "hotsteam", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(573)));
    public static final DeferredHolder<Fluid, FlowingFluid> HOTSTEAM = FLUIDS.register(
            "hotsteam", () -> new BaseFlowingFluid.Source(hotSteamProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_HOTSTEAM = FLUIDS.register(
            "flowing_hotsteam", () -> new BaseFlowingFluid.Flowing(hotSteamProperties()));
    public static final DeferredHolder<FluidType, FluidType> SUPERHOTSTEAM_TYPE = FLUID_TYPES.register(
            "superhotsteam", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(723)));
    public static final DeferredHolder<Fluid, FlowingFluid> SUPERHOTSTEAM = FLUIDS.register(
            "superhotsteam", () -> new BaseFlowingFluid.Source(superhotSteamProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SUPERHOTSTEAM = FLUIDS.register(
            "flowing_superhotsteam", () -> new BaseFlowingFluid.Flowing(superhotSteamProperties()));
    public static final DeferredHolder<FluidType, FluidType> ULTRAHOTSTEAM_TYPE = FLUID_TYPES.register(
            "ultrahotsteam", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(873)));
    public static final DeferredHolder<Fluid, FlowingFluid> ULTRAHOTSTEAM = FLUIDS.register(
            "ultrahotsteam", () -> new BaseFlowingFluid.Source(ultrahotSteamProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_ULTRAHOTSTEAM = FLUIDS.register(
            "flowing_ultrahotsteam", () -> new BaseFlowingFluid.Flowing(ultrahotSteamProperties()));
    public static final DeferredHolder<FluidType, FluidType> COOLANT_TYPE = FLUID_TYPES.register(
            "coolant", () -> new FluidType(FluidType.Properties.create().density(1_000).temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> COOLANT = FLUIDS.register(
            "coolant", () -> new BaseFlowingFluid.Source(coolantProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_COOLANT = FLUIDS.register(
            "flowing_coolant", () -> new BaseFlowingFluid.Flowing(coolantProperties()));
    public static final DeferredHolder<FluidType, FluidType> COOLANT_HOT_TYPE = FLUID_TYPES.register(
            "coolant_hot", () -> new FluidType(FluidType.Properties.create().density(1_000).temperature(873)));
    public static final DeferredHolder<Fluid, FlowingFluid> COOLANT_HOT = FLUIDS.register(
            "coolant_hot", () -> new BaseFlowingFluid.Source(hotCoolantProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_COOLANT_HOT = FLUIDS.register(
            "flowing_coolant_hot", () -> new BaseFlowingFluid.Flowing(hotCoolantProperties()));
    public static final DeferredHolder<FluidType, FluidType> PEROXIDE_TYPE = FLUID_TYPES.register(
            "peroxide", () -> new FluidType(FluidType.Properties.create()));
    public static final DeferredHolder<Fluid, FlowingFluid> PEROXIDE = FLUIDS.register(
            "peroxide", () -> new BaseFlowingFluid.Source(peroxideProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_PEROXIDE = FLUIDS.register(
            "flowing_peroxide", () -> new BaseFlowingFluid.Flowing(peroxideProperties()));
    public static final DeferredHolder<FluidType, FluidType> SULFURIC_ACID_TYPE = FLUID_TYPES.register(
            "sulfuric_acid", () -> new FluidType(FluidType.Properties.create().density(1840).viscosity(1000)));
    public static final DeferredHolder<Fluid, FlowingFluid> SULFURIC_ACID = FLUIDS.register(
            "sulfuric_acid", () -> new BaseFlowingFluid.Source(sulfuricAcidProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SULFURIC_ACID = FLUIDS.register(
            "flowing_sulfuric_acid", () -> new BaseFlowingFluid.Flowing(sulfuricAcidProperties()));
    public static final DeferredHolder<FluidType, FluidType> OIL_TYPE = FLUID_TYPES.register(
            "oil", () -> new FluidType(FluidType.Properties.create().density(900).viscosity(3_000).temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> OIL = FLUIDS.register(
            "oil", () -> new BaseFlowingFluid.Source(oilProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_OIL = FLUIDS.register(
            "flowing_oil", () -> new BaseFlowingFluid.Flowing(oilProperties()));
    public static final DeferredHolder<FluidType, FluidType> HOTOIL_TYPE = FLUID_TYPES.register(
            "hotoil", () -> new FluidType(FluidType.Properties.create().density(900).viscosity(3_000)
                    .temperature(623)));
    public static final DeferredHolder<Fluid, FlowingFluid> HOTOIL = FLUIDS.register(
            "hotoil", () -> new BaseFlowingFluid.Source(hotOilProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_HOTOIL = FLUIDS.register(
            "flowing_hotoil", () -> new BaseFlowingFluid.Flowing(hotOilProperties()));
    public static final DeferredHolder<FluidType, FluidType> HEAVYOIL_TYPE = FLUID_TYPES.register(
            "heavyoil", () -> new FluidType(FluidType.Properties.create().density(900).viscosity(3_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> HEAVYOIL = FLUIDS.register(
            "heavyoil", () -> new BaseFlowingFluid.Source(heavyOilProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_HEAVYOIL = FLUIDS.register(
            "flowing_heavyoil", () -> new BaseFlowingFluid.Flowing(heavyOilProperties()));
    public static final DeferredHolder<FluidType, FluidType> NAPHTHA_TYPE = FLUID_TYPES.register(
            "naphtha", () -> new FluidType(FluidType.Properties.create().density(900).viscosity(3_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> NAPHTHA = FLUIDS.register(
            "naphtha", () -> new BaseFlowingFluid.Source(naphthaProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_NAPHTHA = FLUIDS.register(
            "flowing_naphtha", () -> new BaseFlowingFluid.Flowing(naphthaProperties()));
    public static final DeferredHolder<FluidType, FluidType> LIGHTOIL_TYPE = FLUID_TYPES.register(
            "lightoil", () -> new FluidType(FluidType.Properties.create().density(800).viscosity(1_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> LIGHTOIL = FLUIDS.register(
            "lightoil", () -> new BaseFlowingFluid.Source(lightOilProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_LIGHTOIL = FLUIDS.register(
            "flowing_lightoil", () -> new BaseFlowingFluid.Flowing(lightOilProperties()));
    public static final DeferredHolder<FluidType, FluidType> BITUMEN_TYPE = FLUID_TYPES.register(
            "bitumen", () -> new FluidType(FluidType.Properties.create().density(950).viscosity(6_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> BITUMEN = FLUIDS.register(
            "bitumen", () -> new BaseFlowingFluid.Source(bitumenProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_BITUMEN = FLUIDS.register(
            "flowing_bitumen", () -> new BaseFlowingFluid.Flowing(bitumenProperties()));
    public static final DeferredHolder<FluidType, FluidType> SMEAR_TYPE = FLUID_TYPES.register(
            "smear", () -> new FluidType(FluidType.Properties.create().density(900).viscosity(3_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> SMEAR = FLUIDS.register(
            "smear", () -> new BaseFlowingFluid.Source(smearProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SMEAR = FLUIDS.register(
            "flowing_smear", () -> new BaseFlowingFluid.Flowing(smearProperties()));
    public static final DeferredHolder<FluidType, FluidType> HEATINGOIL_TYPE = FLUID_TYPES.register(
            "heatingoil", () -> new FluidType(FluidType.Properties.create().density(900).viscosity(3_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> HEATINGOIL = FLUIDS.register(
            "heatingoil", () -> new BaseFlowingFluid.Source(heatingOilProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_HEATINGOIL = FLUIDS.register(
            "flowing_heatingoil", () -> new BaseFlowingFluid.Flowing(heatingOilProperties()));
    public static final DeferredHolder<FluidType, FluidType> WOODOIL_TYPE = FLUID_TYPES.register(
            "woodoil", () -> new FluidType(FluidType.Properties.create().density(900).viscosity(3_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> WOODOIL = FLUIDS.register(
            "woodoil", () -> new BaseFlowingFluid.Source(woodOilProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_WOODOIL = FLUIDS.register(
            "flowing_woodoil", () -> new BaseFlowingFluid.Flowing(woodOilProperties()));
    public static final DeferredHolder<FluidType, FluidType> COALCREOSOTE_TYPE = FLUID_TYPES.register(
            "coalcreosote", () -> new FluidType(FluidType.Properties.create().density(900).viscosity(3_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> COALCREOSOTE = FLUIDS.register(
            "coalcreosote", () -> new BaseFlowingFluid.Source(coalCreosoteProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_COALCREOSOTE = FLUIDS.register(
            "flowing_coalcreosote", () -> new BaseFlowingFluid.Flowing(coalCreosoteProperties()));
    public static final DeferredHolder<FluidType, FluidType> LUBRICANT_TYPE = FLUID_TYPES.register(
            "lubricant", () -> new FluidType(FluidType.Properties.create().density(850).viscosity(2_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> LUBRICANT = FLUIDS.register(
            "lubricant", () -> new BaseFlowingFluid.Source(lubricantProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_LUBRICANT = FLUIDS.register(
            "flowing_lubricant", () -> new BaseFlowingFluid.Flowing(lubricantProperties()));
    public static final DeferredHolder<FluidType, FluidType> DIESEL_TYPE = FLUID_TYPES.register(
            "diesel", () -> new FluidType(FluidType.Properties.create().density(830).viscosity(1_500)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> DIESEL = FLUIDS.register(
            "diesel", () -> new BaseFlowingFluid.Source(dieselProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_DIESEL = FLUIDS.register(
            "flowing_diesel", () -> new BaseFlowingFluid.Flowing(dieselProperties()));
    public static final DeferredHolder<FluidType, FluidType> KEROSENE_TYPE = FLUID_TYPES.register(
            "kerosene", () -> new FluidType(FluidType.Properties.create().density(800).viscosity(1_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> KEROSENE = FLUIDS.register(
            "kerosene", () -> new BaseFlowingFluid.Source(keroseneProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_KEROSENE = FLUIDS.register(
            "flowing_kerosene", () -> new BaseFlowingFluid.Flowing(keroseneProperties()));
    public static final DeferredHolder<FluidType, FluidType> PETROLEUM_TYPE = FLUID_TYPES.register(
            "petroleum", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> PETROLEUM = FLUIDS.register(
            "petroleum", () -> new BaseFlowingFluid.Source(petroleumProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_PETROLEUM = FLUIDS.register(
            "flowing_petroleum", () -> new BaseFlowingFluid.Flowing(petroleumProperties()));
    public static final DeferredHolder<FluidType, FluidType> GAS_TYPE = FLUID_TYPES.register(
            "gas", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100).temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> GAS = FLUIDS.register(
            "gas", () -> new BaseFlowingFluid.Source(gasProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_GAS = FLUIDS.register(
            "flowing_gas", () -> new BaseFlowingFluid.Flowing(gasProperties()));
    public static final DeferredHolder<FluidType, FluidType> CARBONDIOXIDE_TYPE = FLUID_TYPES.register(
            "carbondioxide", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> CARBONDIOXIDE = FLUIDS.register(
            "carbondioxide", () -> new BaseFlowingFluid.Source(carbonDioxideProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CARBONDIOXIDE = FLUIDS.register(
            "flowing_carbondioxide", () -> new BaseFlowingFluid.Flowing(carbonDioxideProperties()));
    public static final DeferredHolder<FluidType, FluidType> HYDROGEN_TYPE = FLUID_TYPES.register(
            "hydrogen", () -> new FluidType(FluidType.Properties.create().density(71).viscosity(100)
                    .temperature(13)));
    public static final DeferredHolder<Fluid, FlowingFluid> HYDROGEN = FLUIDS.register(
            "hydrogen", () -> new BaseFlowingFluid.Source(hydrogenProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_HYDROGEN = FLUIDS.register(
            "flowing_hydrogen", () -> new BaseFlowingFluid.Flowing(hydrogenProperties()));
    public static final DeferredHolder<FluidType, FluidType> DEUTERIUM_TYPE = FLUID_TYPES.register(
            "deuterium", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(23)));
    public static final DeferredHolder<Fluid, FlowingFluid> DEUTERIUM = FLUIDS.register(
            "deuterium", () -> new BaseFlowingFluid.Source(deuteriumProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_DEUTERIUM = FLUIDS.register(
            "flowing_deuterium", () -> new BaseFlowingFluid.Flowing(deuteriumProperties()));
    public static final DeferredHolder<FluidType, FluidType> TRITIUM_TYPE = FLUID_TYPES.register(
            "tritium", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(25)));
    public static final DeferredHolder<Fluid, FlowingFluid> TRITIUM = FLUIDS.register(
            "tritium", () -> new BaseFlowingFluid.Source(tritiumProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_TRITIUM = FLUIDS.register(
            "flowing_tritium", () -> new BaseFlowingFluid.Flowing(tritiumProperties()));
    public static final DeferredHolder<FluidType, FluidType> SAS3_TYPE = FLUID_TYPES.register(
            "sas3", () -> new FluidType(FluidType.Properties.create().density(1_000).viscosity(1_000)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> SAS3 = FLUIDS.register(
            "sas3", () -> new BaseFlowingFluid.Source(sas3Properties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SAS3 = FLUIDS.register(
            "flowing_sas3", () -> new BaseFlowingFluid.Flowing(sas3Properties()));
    public static final DeferredHolder<FluidType, FluidType> CRYOGEL_TYPE = FLUID_TYPES.register(
            "cryogel", () -> new FluidType(FluidType.Properties.create().density(1_000).viscosity(3_000)
                    .temperature(103)));
    public static final DeferredHolder<Fluid, FlowingFluid> CRYOGEL = FLUIDS.register(
            "cryogel", () -> new BaseFlowingFluid.Source(cryogelProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CRYOGEL = FLUIDS.register(
            "flowing_cryogel", () -> new BaseFlowingFluid.Flowing(cryogelProperties()));
    public static final DeferredHolder<FluidType, FluidType> UNSATURATEDS_TYPE = FLUID_TYPES.register(
            "unsaturateds", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> UNSATURATEDS = FLUIDS.register(
            "unsaturateds", () -> new BaseFlowingFluid.Source(unsaturatedsProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_UNSATURATEDS = FLUIDS.register(
            "flowing_unsaturateds", () -> new BaseFlowingFluid.Flowing(unsaturatedsProperties()));
    public static final DeferredHolder<FluidType, FluidType> SPENTSTEAM_TYPE = FLUID_TYPES.register(
            "spentsteam", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(373)));
    public static final DeferredHolder<Fluid, FlowingFluid> SPENTSTEAM = FLUIDS.register(
            "spentsteam", () -> new BaseFlowingFluid.Source(spentSteamProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SPENTSTEAM = FLUIDS.register(
            "flowing_spentsteam", () -> new BaseFlowingFluid.Flowing(spentSteamProperties()));
    public static final DeferredHolder<FluidType, FluidType> AIR_TYPE = FLUID_TYPES.register(
            "air", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> AIR = FLUIDS.register(
            "air", () -> new BaseFlowingFluid.Source(airProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_AIR = FLUIDS.register(
            "flowing_air", () -> new BaseFlowingFluid.Flowing(airProperties()));
    public static final DeferredHolder<FluidType, FluidType> AIRBLAST_TYPE = FLUID_TYPES.register(
            "airblast", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(1_200)));
    public static final DeferredHolder<Fluid, FlowingFluid> AIRBLAST = FLUIDS.register(
            "airblast", () -> new BaseFlowingFluid.Source(airBlastProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_AIRBLAST = FLUIDS.register(
            "flowing_airblast", () -> new BaseFlowingFluid.Flowing(airBlastProperties()));
    public static final DeferredHolder<FluidType, FluidType> FLUE_TYPE = FLUID_TYPES.register(
            "flue", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> FLUE = FLUIDS.register(
            "flue", () -> new BaseFlowingFluid.Source(flueProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_FLUE = FLUIDS.register(
            "flowing_flue", () -> new BaseFlowingFluid.Flowing(flueProperties()));
    public static final DeferredHolder<FluidType, FluidType> MERCURY_TYPE = FLUID_TYPES.register(
            "mercury", () -> new FluidType(FluidType.Properties.create().density(13_534).viscosity(1_500)
                    .temperature(293)));
    public static final DeferredHolder<Fluid, FlowingFluid> MERCURY = FLUIDS.register(
            "mercury", () -> new BaseFlowingFluid.Source(mercuryProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_MERCURY = FLUIDS.register(
            "flowing_mercury", () -> new BaseFlowingFluid.Flowing(mercuryProperties()));
    public static final DeferredHolder<FluidType, FluidType> BLOOD_TYPE = FLUID_TYPES.register(
            "blood", () -> new FluidType(FluidType.Properties.create().density(1_060).viscosity(3_000)
                    .temperature(310)));
    public static final DeferredHolder<Fluid, FlowingFluid> BLOOD = FLUIDS.register(
            "blood", () -> new BaseFlowingFluid.Source(bloodProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_BLOOD = FLUIDS.register(
            "flowing_blood", () -> new BaseFlowingFluid.Flowing(bloodProperties()));
    public static final DeferredHolder<FluidType, FluidType> OXYGEN_TYPE = FLUID_TYPES.register(
            "oxygen", () -> new FluidType(FluidType.Properties.create().temperature(173)));
    public static final DeferredHolder<Fluid, FlowingFluid> OXYGEN = FLUIDS.register(
            "oxygen", () -> new BaseFlowingFluid.Source(oxygenProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_OXYGEN = FLUIDS.register(
            "flowing_oxygen", () -> new BaseFlowingFluid.Flowing(oxygenProperties()));
    public static final DeferredHolder<FluidType, FluidType> PAIN_TYPE = FLUID_TYPES.register(
            "pain", () -> new FluidType(FluidType.Properties.create().viscosity(3_000).temperature(300)));
    public static final DeferredHolder<Fluid, FlowingFluid> PAIN = FLUIDS.register(
            "pain", () -> new BaseFlowingFluid.Source(painProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_PAIN = FLUIDS.register(
            "flowing_pain", () -> new BaseFlowingFluid.Flowing(painProperties()));

    private ModFluids() {
    }

    private static BaseFlowingFluid.Properties smokeProperties() {
        return new BaseFlowingFluid.Properties(SMOKE_TYPE::get, SMOKE::get, FLOWING_SMOKE::get)
                .slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties steamProperties() {
        return new BaseFlowingFluid.Properties(STEAM_TYPE::get, STEAM::get, FLOWING_STEAM::get)
                .slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties hotSteamProperties() {
        return new BaseFlowingFluid.Properties(HOTSTEAM_TYPE::get, HOTSTEAM::get, FLOWING_HOTSTEAM::get)
                .slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties superhotSteamProperties() {
        return new BaseFlowingFluid.Properties(SUPERHOTSTEAM_TYPE::get, SUPERHOTSTEAM::get,
                FLOWING_SUPERHOTSTEAM::get).slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties ultrahotSteamProperties() {
        return new BaseFlowingFluid.Properties(ULTRAHOTSTEAM_TYPE::get, ULTRAHOTSTEAM::get,
                FLOWING_ULTRAHOTSTEAM::get).slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties coolantProperties() {
        return new BaseFlowingFluid.Properties(COOLANT_TYPE::get, COOLANT::get, FLOWING_COOLANT::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties hotCoolantProperties() {
        return new BaseFlowingFluid.Properties(COOLANT_HOT_TYPE::get, COOLANT_HOT::get,
                FLOWING_COOLANT_HOT::get).slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties peroxideProperties() {
        return new BaseFlowingFluid.Properties(PEROXIDE_TYPE::get, PEROXIDE::get, FLOWING_PEROXIDE::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties sulfuricAcidProperties() {
        return new BaseFlowingFluid.Properties(SULFURIC_ACID_TYPE::get, SULFURIC_ACID::get,
                FLOWING_SULFURIC_ACID::get).slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties oilProperties() {
        return new BaseFlowingFluid.Properties(OIL_TYPE::get, OIL::get, FLOWING_OIL::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties hotOilProperties() {
        return new BaseFlowingFluid.Properties(HOTOIL_TYPE::get, HOTOIL::get, FLOWING_HOTOIL::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties heavyOilProperties() {
        return new BaseFlowingFluid.Properties(HEAVYOIL_TYPE::get, HEAVYOIL::get, FLOWING_HEAVYOIL::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties naphthaProperties() {
        return new BaseFlowingFluid.Properties(NAPHTHA_TYPE::get, NAPHTHA::get, FLOWING_NAPHTHA::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties lightOilProperties() {
        return new BaseFlowingFluid.Properties(LIGHTOIL_TYPE::get, LIGHTOIL::get, FLOWING_LIGHTOIL::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties bitumenProperties() {
        return new BaseFlowingFluid.Properties(BITUMEN_TYPE::get, BITUMEN::get, FLOWING_BITUMEN::get)
                .slopeFindDistance(2).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties smearProperties() {
        return new BaseFlowingFluid.Properties(SMEAR_TYPE::get, SMEAR::get, FLOWING_SMEAR::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties heatingOilProperties() {
        return new BaseFlowingFluid.Properties(HEATINGOIL_TYPE::get, HEATINGOIL::get,
                FLOWING_HEATINGOIL::get).slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties woodOilProperties() {
        return new BaseFlowingFluid.Properties(WOODOIL_TYPE::get, WOODOIL::get,
                FLOWING_WOODOIL::get).slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties coalCreosoteProperties() {
        return new BaseFlowingFluid.Properties(COALCREOSOTE_TYPE::get, COALCREOSOTE::get,
                FLOWING_COALCREOSOTE::get).slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties lubricantProperties() {
        return new BaseFlowingFluid.Properties(LUBRICANT_TYPE::get, LUBRICANT::get, FLOWING_LUBRICANT::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties dieselProperties() {
        return new BaseFlowingFluid.Properties(DIESEL_TYPE::get, DIESEL::get, FLOWING_DIESEL::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties keroseneProperties() {
        return new BaseFlowingFluid.Properties(KEROSENE_TYPE::get, KEROSENE::get, FLOWING_KEROSENE::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties petroleumProperties() {
        return new BaseFlowingFluid.Properties(PETROLEUM_TYPE::get, PETROLEUM::get, FLOWING_PETROLEUM::get)
                .slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties gasProperties() {
        return new BaseFlowingFluid.Properties(GAS_TYPE::get, GAS::get, FLOWING_GAS::get)
                .slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties carbonDioxideProperties() {
        return new BaseFlowingFluid.Properties(CARBONDIOXIDE_TYPE::get, CARBONDIOXIDE::get,
                FLOWING_CARBONDIOXIDE::get).slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties hydrogenProperties() {
        return new BaseFlowingFluid.Properties(HYDROGEN_TYPE::get, HYDROGEN::get, FLOWING_HYDROGEN::get)
                .slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties deuteriumProperties() {
        return new BaseFlowingFluid.Properties(DEUTERIUM_TYPE::get, DEUTERIUM::get, FLOWING_DEUTERIUM::get)
                .slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties tritiumProperties() {
        return new BaseFlowingFluid.Properties(TRITIUM_TYPE::get, TRITIUM::get, FLOWING_TRITIUM::get)
                .slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties sas3Properties() {
        return new BaseFlowingFluid.Properties(SAS3_TYPE::get, SAS3::get, FLOWING_SAS3::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties cryogelProperties() {
        return new BaseFlowingFluid.Properties(CRYOGEL_TYPE::get, CRYOGEL::get, FLOWING_CRYOGEL::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties unsaturatedsProperties() {
        return new BaseFlowingFluid.Properties(UNSATURATEDS_TYPE::get, UNSATURATEDS::get,
                FLOWING_UNSATURATEDS::get).slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties spentSteamProperties() {
        return new BaseFlowingFluid.Properties(SPENTSTEAM_TYPE::get, SPENTSTEAM::get,
                FLOWING_SPENTSTEAM::get).slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties airProperties() {
        return new BaseFlowingFluid.Properties(AIR_TYPE::get, AIR::get,
                FLOWING_AIR::get).slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties airBlastProperties() {
        return new BaseFlowingFluid.Properties(AIRBLAST_TYPE::get, AIRBLAST::get,
                FLOWING_AIRBLAST::get).slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties flueProperties() {
        return new BaseFlowingFluid.Properties(FLUE_TYPE::get, FLUE::get,
                FLOWING_FLUE::get).slopeFindDistance(1).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties mercuryProperties() {
        return new BaseFlowingFluid.Properties(MERCURY_TYPE::get, MERCURY::get, FLOWING_MERCURY::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties bloodProperties() {
        return new BaseFlowingFluid.Properties(BLOOD_TYPE::get, BLOOD::get, FLOWING_BLOOD::get)
                .slopeFindDistance(2).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties oxygenProperties() {
        return new BaseFlowingFluid.Properties(OXYGEN_TYPE::get, OXYGEN::get, FLOWING_OXYGEN::get)
                .slopeFindDistance(4).levelDecreasePerBlock(1);
    }
    private static BaseFlowingFluid.Properties painProperties() {
        return new BaseFlowingFluid.Properties(PAIN_TYPE::get, PAIN::get, FLOWING_PAIN::get)
                .slopeFindDistance(2).levelDecreasePerBlock(1);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}
