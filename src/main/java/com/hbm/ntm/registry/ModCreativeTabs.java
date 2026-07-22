package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.content.HazardousMaterialDefinitions;
import com.hbm.ntm.content.MaterialDefinitions;
import com.hbm.ntm.item.AshItem;
import com.hbm.ntm.item.ArcElectrodeItem;
import com.hbm.ntm.item.BatteryPackItem;
import com.hbm.ntm.item.BlueprintItem;
import com.hbm.ntm.item.BoltItem;
import com.hbm.ntm.item.BreedingRodItem;
import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.item.CasingItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryIngotItem;
import com.hbm.ntm.item.FoundryPartItem;
import com.hbm.ntm.foundry.FoundryMaterial;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.OreChunkItem;
import com.hbm.ntm.item.NuclearWasteItem;
import com.hbm.ntm.item.SellafieldBlockItem;
import com.hbm.ntm.item.WireFineItem;
import com.hbm.ntm.item.WeldedPlateItem;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.weapon.Magnum357AmmoType;
import com.hbm.ntm.weapon.Bolt75AmmoType;
import com.hbm.ntm.weapon.Magnum44AmmoType;
import com.hbm.ntm.weapon.Equestrian44AmmoType;
import com.hbm.ntm.weapon.NineMillimeterAmmoType;
import com.hbm.ntm.weapon.FortyMillimeterAmmoType;
import com.hbm.ntm.weapon.FiveFiveSixAmmoType;
import com.hbm.ntm.weapon.FiftyCalAmmoType;
import com.hbm.ntm.weapon.FollyAmmoType;
import com.hbm.ntm.weapon.AberratorAmmoType;
import com.hbm.ntm.weapon.PepperboxAmmoType;
import com.hbm.ntm.weapon.Shotgun12GaugeAmmoType;
import com.hbm.ntm.weapon.Shotgun10GaugeAmmoType;
import com.hbm.ntm.weapon.SevenSixTwoAmmoType;
import com.hbm.ntm.weapon.TwentyTwoAmmoType;
import com.hbm.ntm.weapon.FlamerFuelType;
import com.hbm.ntm.weapon.FireExtinguisherAmmoType;
import com.hbm.ntm.weapon.ChargeThrowerAmmoType;
import com.hbm.ntm.weapon.RocketAmmoType;
import com.hbm.ntm.weapon.EnergyAmmoType;
import com.hbm.ntm.weapon.TauAmmoType;
import com.hbm.ntm.weapon.CoilAmmoType;
import com.hbm.ntm.weapon.MiniNukeAmmoType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HbmNtm.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PARTS = TABS.register(
            "parts",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.parts"))
                    .icon(() -> ModItems.URANIUM_INGOT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        HazardousMaterialDefinitions.ITEMS.forEach(
                                definition -> output.accept(ModItems.get(definition.id()).get())
                        );
                        MaterialDefinitions.ITEMS.stream()
                                .filter(definition -> !definition.id().equals("ingot_dura_steel"))
                                .forEach(definition -> {
                                    output.accept(ModItems.get(definition.id()).get());
                                    if (definition.id().equals("fragment_coltan")) {
                                        output.accept(OreChunkItem.rare(ModItems.CHUNK_ORE.get(), 1));
                                    }
                                });
                        ModItems.LEGACY_ORE_RESOURCE_ITEMS.values().forEach(item -> output.accept(item.get()));
                        output.accept(ModItems.TRINITITE.get());
                        output.accept(ModItems.SCRAP_NUCLEAR.get());
                        output.accept(ModItems.GEM_RAD.get());
                        output.accept(OreChunkItem.create(ModItems.CHUNK_ORE.get(),
                                OreChunkItem.ChunkType.MALACHITE, 1));
                        output.accept(ModItems.GEAR_LARGE.get());
                        output.accept(ModItems.SAWBLADE.get());
                        output.accept(ModItems.POWDER_SAWDUST.get());
                        output.accept(ModItems.SOLID_FUEL.get());
                        output.accept(ModItems.SOLID_FUEL_PRESTO.get());
                        output.accept(ModItems.SOLID_FUEL_PRESTO_TRIPLET.get());
                        output.accept(ModItems.SOLID_FUEL_BF.get());
                        output.accept(ModItems.SOLID_FUEL_PRESTO_BF.get());
                        output.accept(ModItems.SOLID_FUEL_PRESTO_TRIPLET_BF.get());
                        output.accept(ModItems.ROCKET_FUEL.get());
                        output.accept(ModItems.PELLET_GAS.get());
                        output.accept(ModItems.SAFETY_FUSE.get());
                        output.accept(ModItems.BALL_DYNAMITE.get());
                        output.accept(ModItems.DUCTTAPE.get());
                        output.accept(ModItems.PART_GENERIC.get());
                        output.accept(ModItems.HAZMAT_CLOTH.get());
                        output.accept(ModItems.RAG.get());
                        output.accept(ModItems.RAG_DAMP.get());
                        output.accept(ModItems.RAG_PISS.get());
                        output.accept(ModItems.FILTER_COAL.get());
                        output.accept(ModItems.CATALYST_CLAY.get());
                        output.accept(ModItems.PLATE_POLYMER.get());
                        output.accept(ModItems.get("ingot_polymer").get());
                        output.accept(ModItems.get("powder_polymer").get());
                        output.accept(ModItems.get("plate_desh").get());
                        for (WireFineItem.WireMaterial material : WireFineItem.WireMaterial.values()) {
                            output.accept(WireFineItem.create(ModItems.WIRE_FINE.get(), material, 1));
                        }
                        for (FoundryMaterial material : FoundryMaterial.values()) {
                            ItemStack denseWire = FoundryMoldItem.Mold.DENSE_WIRE.output(material);
                            if (!denseWire.isEmpty()) output.accept(denseWire);
                        }
                        for (CasingItem.CasingType type : CasingItem.CasingType.values()) {
                            output.accept(CasingItem.create(ModItems.CASING.get(), type, 1));
                        }
                        for (FoundryMoldItem.Mold mold : List.of(
                                FoundryMoldItem.Mold.LIGHT_BARREL, FoundryMoldItem.Mold.HEAVY_BARREL,
                                FoundryMoldItem.Mold.LIGHT_RECEIVER, FoundryMoldItem.Mold.HEAVY_RECEIVER,
                                FoundryMoldItem.Mold.MECHANISM, FoundryMoldItem.Mold.STOCK,
                                FoundryMoldItem.Mold.GRIP)) {
                            for (FoundryMaterial material : FoundryMaterial.values()) {
                                ItemStack part = mold.output(material);
                                if (!part.isEmpty()) output.accept(part);
                            }
                        }
                        output.accept(CastPlateItem.create(ModItems.PLATE_CAST.get(),
                                CastPlateItem.CastPlateMaterial.IRON, 1));
                        output.accept(CastPlateItem.create(ModItems.PLATE_CAST.get(),
                                CastPlateItem.CastPlateMaterial.TITANIUM, 1));
                        output.accept(CastPlateItem.create(ModItems.PLATE_CAST.get(),
                                CastPlateItem.CastPlateMaterial.COPPER, 1));
                        output.accept(CastPlateItem.create(ModItems.PLATE_CAST.get(),
                                CastPlateItem.CastPlateMaterial.LEAD, 1));
                        output.accept(CastPlateItem.create(ModItems.PLATE_CAST.get(),
                                CastPlateItem.CastPlateMaterial.STEEL, 1));
                        output.accept(CastPlateItem.create(ModItems.PLATE_CAST.get(),
                                CastPlateItem.CastPlateMaterial.DURA_STEEL, 1));
                        output.accept(CastPlateItem.create(ModItems.PLATE_CAST.get(),
                                CastPlateItem.CastPlateMaterial.TECHNETIUM_STEEL, 1));
                        output.accept(CastPlateItem.create(ModItems.PLATE_CAST.get(),
                                CastPlateItem.CastPlateMaterial.CADMIUM_STEEL, 1));
                        output.accept(WeldedPlateItem.steel(ModItems.PLATE_WELDED.get(), 1));
                        output.accept(WeldedPlateItem.create(ModItems.PLATE_WELDED.get(),
                                WeldedPlateItem.WeldedPlateMaterial.TECHNETIUM_STEEL, 1));
                        output.accept(WeldedPlateItem.create(ModItems.PLATE_WELDED.get(),
                                WeldedPlateItem.WeldedPlateMaterial.CADMIUM_STEEL, 1));
                        output.accept(FoundryIngotItem.create(ModItems.INGOT_RAW.get(),
                                com.hbm.ntm.foundry.FoundryMaterial.SLAG, 1));
                        output.accept(ModItems.POWDER_FLUX.get());
                        output.accept(ModItems.BALL_FIRECLAY.get());
                        for (BoltItem.BoltMaterial material : BoltItem.BoltMaterial.values()) {
                            output.accept(BoltItem.create(ModItems.BOLT.get(), material, 1));
                        }
                        output.accept(ModItems.COIL_COPPER.get());
                        output.accept(ModItems.COIL_COPPER_TORUS.get());
                        output.accept(ModItems.COIL_GOLD.get());
                        output.accept(ModItems.COIL_GOLD_TORUS.get());
                        output.accept(ModItems.COIL_TUNGSTEN.get());
                        output.accept(ModItems.TANK_STEEL.get());
                        output.accept(com.hbm.ntm.item.PipeItem.copper(ModItems.PIPE.get(), 1));
                        output.accept(com.hbm.ntm.item.PipeItem.steel(ModItems.PIPE.get(), 1));
                        output.accept(com.hbm.ntm.item.PipeItem.duraSteel(ModItems.PIPE.get(), 1));
                        output.accept(com.hbm.ntm.item.PipeItem.lead(ModItems.PIPE.get(), 1));
                        output.accept(com.hbm.ntm.item.ShellItem.steel(ModItems.SHELL.get(), 1));
                        output.accept(com.hbm.ntm.item.ShellItem.titanium(ModItems.SHELL.get(), 1));
                        output.accept(ModItems.FLUID_TANK_EMPTY.get());
                        for (com.hbm.ntm.item.UniversalFluidTankItem.ContainedFluid fluid :
                                com.hbm.ntm.item.UniversalFluidTankItem.ContainedFluid.values()) {
                            if (fluid != com.hbm.ntm.item.UniversalFluidTankItem.ContainedFluid.NONE)
                                output.accept(com.hbm.ntm.item.UniversalFluidTankItem.create(
                                        ModItems.FLUID_TANK_FULL.get(), fluid, 1));
                        }
                        output.accept(ModItems.MOTOR.get());
                        output.accept(ModItems.MOTOR_DESH.get());
                        output.accept(ModItems.MAGNETRON.get());
                        output.accept(ModItems.DRILL_TITANIUM.get());
                        output.accept(ModItems.CENTRIFUGE_ELEMENT.get());
                        output.accept(ModItems.CRT_DISPLAY.get());
                        output.accept(ModItems.REACTOR_CORE.get());
                        output.accept(ModItems.OIL_TAR.get());
                        output.accept(ModItems.COKE_COAL.get());
                        output.accept(ModItems.COKE_LIGNITE.get());
                        output.accept(ModItems.COKE_PETROLEUM.get());
                        output.accept(ModItems.STEEL_BEAM_ITEM.get());
                        output.accept(ModItems.STEEL_GRATE_ITEM.get());
                        for (CircuitItem.CircuitType type : CircuitItem.CircuitType.values()) {
                            output.accept(CircuitItem.create(ModItems.CIRCUIT.get(), type, 1));
                        }
                        for (AshItem.AshType type : AshItem.AshType.values()) {
                            output.accept(AshItem.create(ModItems.POWDER_ASH.get(), type));
                        }
                        output.accept(ModItems.WASTE_PLATE_U233.get());
                        output.accept(ModItems.WASTE_PLATE_U235.get());
                        output.accept(ModItems.WASTE_PLATE_MOX.get());
                        output.accept(ModItems.WASTE_PLATE_PU239.get());
                        output.accept(ModItems.WASTE_PLATE_SA326.get());
                        output.accept(ModItems.WASTE_PLATE_RA226BE.get());
                        output.accept(ModItems.WASTE_PLATE_PU238BE.get());
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONTROL = TABS.register(
            "control",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.control"))
                    .icon(() -> ModItems.STAMPS.get("stamp_iron_flat").get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        ModItems.STAMPS.values().forEach(stamp -> output.accept(stamp.get()));
                        output.accept(ModItems.BLADES_STEEL.get());
                        output.accept(ModItems.BLADES_TITANIUM.get());
                        output.accept(ModItems.BLADES_DESH.get());
                        output.accept(ModItems.BLADE_TITANIUM.get());
                        output.accept(ModItems.TURBINE_TITANIUM.get());
                        output.accept(ModItems.BLADE_TUNGSTEN.get());
                        output.accept(ModItems.TURBINE_TUNGSTEN.get());
                        output.accept(ModItems.FLAME_PONY.get());
                        for (com.hbm.ntm.item.SirenTrackItem.Track track :
                                com.hbm.ntm.item.SirenTrackItem.Track.values()) {
                            if (track != com.hbm.ntm.item.SirenTrackItem.Track.NONE) {
                                output.accept(com.hbm.ntm.item.SirenTrackItem.create(
                                        ModItems.SIREN_TRACK.get(), track));
                            }
                        }
                        output.accept(ModItems.ROD_EMPTY.get());
                        output.accept(ModItems.ROD_DUAL_EMPTY.get());
                        output.accept(ModItems.ROD_QUAD_EMPTY.get());
                        output.accept(ModItems.CELL_EMPTY.get());
                        output.accept(ModItems.CELL_TRITIUM.get());
                        output.accept(ModItems.CELL_SAS3.get());
                        addWasteVariants(output, ModItems.NUCLEAR_WASTE_LONG.get());
                        addWasteVariants(output, ModItems.NUCLEAR_WASTE_LONG_TINY.get());
                        addWasteVariants(output, ModItems.NUCLEAR_WASTE_SHORT.get());
                        addWasteVariants(output, ModItems.NUCLEAR_WASTE_SHORT_TINY.get());
                        addWasteVariants(output, ModItems.NUCLEAR_WASTE_LONG_DEPLETED.get());
                        addWasteVariants(output, ModItems.NUCLEAR_WASTE_LONG_DEPLETED_TINY.get());
                        addWasteVariants(output, ModItems.NUCLEAR_WASTE_SHORT_DEPLETED.get());
                        addWasteVariants(output, ModItems.NUCLEAR_WASTE_SHORT_DEPLETED_TINY.get());
                        for (BreedingRodItem.Type type : BreedingRodItem.Type.values()) {
                            output.accept(BreedingRodItem.stack(ModItems.ROD.get(), type, 1));
                            output.accept(BreedingRodItem.stack(ModItems.ROD_DUAL.get(), type, 1));
                            output.accept(BreedingRodItem.stack(ModItems.ROD_QUAD.get(), type, 1));
                        }
                        output.accept(ModItems.PLATE_FUEL_U233.get());
                        output.accept(ModItems.PLATE_FUEL_U235.get());
                        output.accept(ModItems.PLATE_FUEL_MOX.get());
                        output.accept(ModItems.PLATE_FUEL_PU239.get());
                        output.accept(ModItems.PLATE_FUEL_SA326.get());
                        output.accept(ModItems.PLATE_FUEL_RA226BE.get());
                        output.accept(ModItems.PLATE_FUEL_PU238BE.get());
                        output.accept(ModItems.BATTERY_CREATIVE.get());
                        output.accept(ModItems.FLUID_BARREL_INFINITE.get());
                        output.accept(ModItems.REACHER.get());
                        output.accept(ModItems.MOLD_BASE.get());
                        for (FoundryMoldItem.Mold mold : FoundryMoldItem.Mold.values()) {
                            output.accept(FoundryMoldItem.create(ModItems.MOLD.get(), mold));
                        }
                        output.accept(ModItems.SCREWDRIVER.get());
                        output.accept(ModItems.BLOWTORCH.get());
                        output.accept(ModItems.STEEL_SWORD.get());
                        output.accept(ModItems.STEEL_PICKAXE.get());
                        output.accept(ModItems.STEEL_AXE.get());
                        output.accept(ModItems.STEEL_SHOVEL.get());
                        output.accept(ModItems.STEEL_HOE.get());
                        output.accept(ModItems.TITANIUM_SWORD.get());
                        output.accept(ModItems.TITANIUM_PICKAXE.get());
                        output.accept(ModItems.TITANIUM_AXE.get());
                        output.accept(ModItems.TITANIUM_SHOVEL.get());
                        output.accept(ModItems.TITANIUM_HOE.get());
                        output.accept(ModItems.COBALT_SWORD.get());
                        output.accept(ModItems.COBALT_PICKAXE.get());
                        output.accept(ModItems.COBALT_AXE.get());
                        output.accept(ModItems.COBALT_SHOVEL.get());
                        output.accept(ModItems.COBALT_HOE.get());
                        output.accept(ModItems.BISMUTH_PICKAXE.get());
                        output.accept(ModItems.SURVEY_SCANNER.get());
                        output.accept(ModItems.ORE_DENSITY_SCANNER.get());
                        output.accept(ArcElectrodeItem.create(ModItems.ARC_ELECTRODE.get(),
                                ArcElectrodeItem.ElectrodeType.GRAPHITE, 1));
                        output.accept(ArcElectrodeItem.burnt(ModItems.ARC_ELECTRODE_BURNT.get(),
                                ArcElectrodeItem.create(ModItems.ARC_ELECTRODE.get(),
                                        ArcElectrodeItem.ElectrodeType.GRAPHITE, 1)));
                        for (FluidIdentifierItem.Selection selection : List.of(
                                FluidIdentifierItem.Selection.AIR, FluidIdentifierItem.Selection.AIRBLAST,
                                FluidIdentifierItem.Selection.WATER, FluidIdentifierItem.Selection.STEAM,
                                FluidIdentifierItem.Selection.HOTSTEAM,
                                FluidIdentifierItem.Selection.SUPERHOTSTEAM,
                                FluidIdentifierItem.Selection.ULTRAHOTSTEAM,
                                FluidIdentifierItem.Selection.LAVA, FluidIdentifierItem.Selection.PEROXIDE,
                                FluidIdentifierItem.Selection.PAIN,
                                FluidIdentifierItem.Selection.SULFURIC_ACID, FluidIdentifierItem.Selection.OIL,
                                FluidIdentifierItem.Selection.HOTOIL,
                                FluidIdentifierItem.Selection.HEAVYOIL, FluidIdentifierItem.Selection.NAPHTHA,
                                FluidIdentifierItem.Selection.LIGHTOIL, FluidIdentifierItem.Selection.BITUMEN,
                                FluidIdentifierItem.Selection.SMEAR, FluidIdentifierItem.Selection.HEATINGOIL,
                                FluidIdentifierItem.Selection.WOODOIL,
                                FluidIdentifierItem.Selection.COALCREOSOTE,
                                FluidIdentifierItem.Selection.LUBRICANT, FluidIdentifierItem.Selection.DIESEL,
                                FluidIdentifierItem.Selection.KEROSENE, FluidIdentifierItem.Selection.PETROLEUM,
                                FluidIdentifierItem.Selection.GAS, FluidIdentifierItem.Selection.HYDROGEN,
                                FluidIdentifierItem.Selection.CARBONDIOXIDE,
                                FluidIdentifierItem.Selection.OXYGEN,
                                FluidIdentifierItem.Selection.DEUTERIUM, FluidIdentifierItem.Selection.TRITIUM,
                                FluidIdentifierItem.Selection.CRYOGEL,
                                FluidIdentifierItem.Selection.UNSATURATEDS,
                                FluidIdentifierItem.Selection.SPENTSTEAM,
                                FluidIdentifierItem.Selection.FLUE,
                                FluidIdentifierItem.Selection.MERCURY,
                                FluidIdentifierItem.Selection.BLOOD)) {
                            var identifier = new net.minecraft.world.item.ItemStack(ModItems.FLUID_IDENTIFIER_MULTI.get());
                            FluidIdentifierItem.set(identifier, selection, true);
                            output.accept(identifier);
                            output.accept(com.hbm.ntm.item.FluidDuctItem.create(
                                    ModItems.FLUID_DUCT.get(), selection, 1));
                        }
                        output.accept(ModItems.CANISTER_EMPTY.get());
                        output.accept(ModItems.CANISTER_FULL.get());
                        for (com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid fluid : List.of(
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.HEAVYOIL,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.NAPHTHA,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.LIGHTOIL,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.BITUMEN,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.SMEAR,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.HEATINGOIL,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.WOODOIL,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.COALCREOSOTE,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.LUBRICANT,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.DIESEL,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.KEROSENE,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.GASOLINE,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.GASOLINE_LEADED,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.COALGAS,
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.COALGAS_LEADED)) {
                            output.accept(com.hbm.ntm.item.SourceFluidContainerItem.create(
                                    ModItems.CANISTER_FULL.get(), fluid, 1));
                        }
                        output.accept(ModItems.GAS_EMPTY.get());
                        output.accept(ModItems.GAS_FULL.get());
                        output.accept(ModItems.legacyOreBlockItem("gas_flammable").get());
                        output.accept(ModItems.legacyOreBlockItem("gas_explosive").get());
                        output.accept(ModItems.legacyOreBlockItem("gas_coal").get());
                        output.accept(ModItems.legacyOreBlockItem("gas_asbestos").get());
                        output.accept(ModItems.legacyOreBlockItem("gas_monoxide").get());
                        output.accept(ModItems.legacyOreBlockItem("gas_radon").get());
                        output.accept(com.hbm.ntm.item.SourceFluidContainerItem.create(
                                ModItems.GAS_FULL.get(),
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.HYDROGEN, 1));
                        output.accept(com.hbm.ntm.item.SourceFluidContainerItem.create(
                                ModItems.GAS_FULL.get(),
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.OXYGEN, 1));
                        output.accept(com.hbm.ntm.item.SourceFluidContainerItem.create(
                                ModItems.GAS_FULL.get(),
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.CARBONDIOXIDE, 1));
                        output.accept(com.hbm.ntm.item.SourceFluidContainerItem.create(
                                ModItems.GAS_FULL.get(),
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.DEUTERIUM, 1));
                        output.accept(com.hbm.ntm.item.SourceFluidContainerItem.create(
                                ModItems.GAS_FULL.get(),
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.TRITIUM, 1));
                        output.accept(com.hbm.ntm.item.SourceFluidContainerItem.create(
                                ModItems.GAS_FULL.get(),
                                com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.UNSATURATEDS, 1));
                        output.accept(ModItems.AMS_CATALYST_BLANK.get());
                        ModItems.DFC_CATALYSTS.values().forEach(catalyst -> output.accept(catalyst.get()));
                        output.accept(ModItems.AMS_LENS.get());
                        output.accept(ModItems.AMS_CORE_SING.get());
                        output.accept(ModItems.AMS_CORE_WORMHOLE.get());
                        output.accept(ModItems.AMS_CORE_EYEOFHARMONY.get());
                        for (String pool : List.of(
                                "alt.plates", "alt..lube", "alt..electrodes", "discover..stone")) {
                            output.accept(BlueprintItem.forPool(ModItems.BLUEPRINTS.get(), pool));
                        }
                        output.accept(ModItems.UPGRADE_MUFFLER.get());
                        ModItems.MACHINE_UPGRADES.values().forEach(upgrade -> output.accept(upgrade.get()));
                        for (BatteryPackItem.BatteryType type : BatteryPackItem.BatteryType.values()) {
                            output.accept(BatteryPackItem.create(ModItems.BATTERY_PACK.get(), type, false));
                            output.accept(BatteryPackItem.create(ModItems.BATTERY_PACK.get(), type, true));
                        }
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MACHINES = TABS.register(
            "machines",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.machines"))
                    .icon(() -> ModItems.MACHINE_PRESS_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MACHINE_PRESS_ITEM.get());
                        output.accept(ModItems.AMMO_PRESS_ITEM.get());
                        output.accept(ModItems.PRESS_PREHEATER_ITEM.get());
                        output.accept(ModItems.MACHINE_SHREDDER_ITEM.get());
                        output.accept(ModItems.MACHINE_ASSEMBLY_MACHINE_ITEM.get());
                        output.accept(ModItems.MACHINE_CHEMICAL_PLANT_ITEM.get());
                        output.accept(ModItems.MACHINE_SOLDERING_STATION_ITEM.get());
                        output.accept(ModItems.MACHINE_TRANSFORMER_ITEM.get());
                        output.accept(ModItems.MACHINE_ARC_WELDER_ITEM.get());
                        output.accept(ModItems.MACHINE_ARC_FURNACE_ITEM.get());
                        output.accept(ModItems.MACHINE_REFINERY_ITEM.get());
                        output.accept(ModItems.MACHINE_CENTRIFUGE_ITEM.get());
                        output.accept(ModItems.MACHINE_CATALYTIC_CRACKER_ITEM.get());
                        output.accept(ModItems.MACHINE_FRACTION_TOWER_ITEM.get());
                        output.accept(ModItems.FRACTION_SPACER_ITEM.get());
                        output.accept(ModItems.MACHINE_CRUCIBLE_ITEM.get());
                        output.accept(ModItems.FOUNDRY_MOLD_ITEM.get());
                        output.accept(ModItems.FOUNDRY_BASIN_ITEM.get());
                        output.accept(ModItems.FOUNDRY_CHANNEL_ITEM.get());
                        output.accept(ModItems.FOUNDRY_TANK_ITEM.get());
                        output.accept(ModItems.FOUNDRY_OUTLET_ITEM.get());
                        output.accept(ModItems.FOUNDRY_SLAGTAP_ITEM.get());
                        output.accept(ModItems.HEATER_FIREBOX_ITEM.get());
                        output.accept(ModItems.HEATER_OVEN_ITEM.get());
                        output.accept(ModItems.MACHINE_ASHPIT_ITEM.get());
                        output.accept(ModItems.MACHINE_STIRLING_ITEM.get());
                        output.accept(ModItems.MACHINE_SAWMILL_ITEM.get());
                        output.accept(ModItems.MACHINE_STEAM_ENGINE_ITEM.get());
                        output.accept(ModItems.MACHINE_INDUSTRIAL_TURBINE_ITEM.get());
                        output.accept(ModItems.MACHINE_TURBINE_GAS_ITEM.get());
                        output.accept(ModItems.MACHINE_COMBUSTION_ENGINE_ITEM.get());
                        output.accept(ModItems.PISTON_SET_STEEL.get());
                        output.accept(ModItems.PISTON_SET_DURA.get());
                        output.accept(ModItems.PISTON_SET_DESH.get());
                        output.accept(ModItems.PISTON_SET_STARMETAL.get());
                        output.accept(ModItems.MACHINE_TURBOFAN_ITEM.get());
                        output.accept(ModItems.MACHINE_TURBINE_ITEM.get());
                        output.accept(ModItems.REACTOR_ZIRNOX_ITEM.get());
                        output.accept(ModItems.MACHINE_REACTOR_BREEDING_ITEM.get());
                        output.accept(ModItems.REACTOR_RESEARCH_ITEM.get());
                        output.accept(ModItems.MACHINE_RADGEN_ITEM.get());
                        output.accept(ModItems.MACHINE_WASTE_DRUM_ITEM.get());
                        output.accept(ModItems.MACHINE_SIREN_ITEM.get());
                        output.accept(ModItems.ROD_ZIRNOX_EMPTY.get());
                        output.accept(ModItems.ROD_ZIRNOX_NATURAL_URANIUM_FUEL.get());
                        output.accept(ModItems.ROD_ZIRNOX_URANIUM_FUEL.get());
                        output.accept(ModItems.ROD_ZIRNOX_TH232.get());
                        output.accept(ModItems.ROD_ZIRNOX_THORIUM_FUEL.get());
                        output.accept(ModItems.ROD_ZIRNOX_MOX_FUEL.get());
                        output.accept(ModItems.ROD_ZIRNOX_PLUTONIUM_FUEL.get());
                        output.accept(ModItems.ROD_ZIRNOX_U233_FUEL.get());
                        output.accept(ModItems.ROD_ZIRNOX_U235_FUEL.get());
                        output.accept(ModItems.ROD_ZIRNOX_LES_FUEL.get());
                        output.accept(ModItems.ROD_ZIRNOX_LITHIUM.get());
                        output.accept(ModItems.ROD_ZIRNOX_ZFB_MOX.get());
                        output.accept(ModItems.ROD_ZIRNOX_TRITIUM.get());
                        output.accept(ModItems.PUMP_STEAM_ITEM.get());
                        output.accept(ModItems.PUMP_ELECTRIC_ITEM.get());
                        output.accept(ModItems.MACHINE_INTAKE_ITEM.get());
                        output.accept(ModItems.MACHINE_CONDENSER_ITEM.get());
                        output.accept(ModItems.MACHINE_CONDENSER_POWERED_ITEM.get());
                        output.accept(ModItems.MACHINE_BOILER_ITEM.get());
                        output.accept(ModItems.HEATER_ELECTRIC_ITEM.get());
                        output.accept(ModItems.HEATER_OILBURNER_ITEM.get());
                        output.accept(ModItems.HEATER_HEATEX_ITEM.get());
                        output.accept(ModItems.MACHINE_BLAST_FURNACE_ITEM.get());
                        output.accept(ModItems.FURNACE_COMBINATION_ITEM.get());
                        output.accept(ModItems.FURNACE_STEEL_ITEM.get());
                        output.accept(ModItems.MACHINE_ELECTRIC_FURNACE_ITEM.get());
                        output.accept(ModItems.MACHINE_FURNACE_BRICK_ITEM.get());
                        output.accept(ModItems.MACHINE_WOOD_BURNER_ITEM.get());
                        output.accept(ModItems.MACHINE_MICROWAVE_ITEM.get());
                        output.accept(ModItems.MACHINE_WELL_ITEM.get());
                        output.accept(ModItems.RADIO_TORCH_SENDER_ITEM.get());
                        output.accept(ModItems.RADIO_TORCH_RECEIVER_ITEM.get());
                        output.accept(ModItems.RADIO_TORCH_COUNTER_ITEM.get());
                        output.accept(ModItems.RADIO_TORCH_LOGIC_ITEM.get());
                        output.accept(ModItems.RADIO_TORCH_READER_ITEM.get());
                        output.accept(ModItems.RADIO_TORCH_CONTROLLER_ITEM.get());
                        output.accept(ModItems.MACHINE_DIESEL_ITEM.get());
                        output.accept(ModItems.DFC_CORE_ITEM.get());
                        output.accept(ModItems.DFC_EMITTER_ITEM.get());
                        output.accept(ModItems.DFC_INJECTOR_ITEM.get());
                        output.accept(ModItems.DFC_RECEIVER_ITEM.get());
                        output.accept(ModItems.DFC_STABILIZER_ITEM.get());
                        output.accept(ModItems.MACHINE_FLUIDTANK_ITEM.get());
                        output.accept(ModItems.FLUID_DUCT_NEO_ITEM.get());
                        output.accept(ModItems.CONVEYOR_WAND.get());
                        output.accept(ModItems.CONVEYOR_WAND_EXPRESS.get());
                        output.accept(ModItems.CONVEYOR_WAND_DOUBLE.get());
                        output.accept(ModItems.CONVEYOR_WAND_TRIPLE.get());
                        output.accept(ModItems.CRANE_EXTRACTOR_ITEM.get());
                        output.accept(ModItems.CRANE_INSERTER_ITEM.get());
                        output.accept(ModItems.CRANE_BOXER_ITEM.get());
                        output.accept(ModItems.RED_CABLE_ITEM.get());
                        output.accept(ModItems.MACHINE_CONVERTER_HE_FE.get());
                        output.accept(ModItems.MACHINE_CONVERTER_FE_HE.get());
                        output.accept(ModItems.MACHINE_BATTERY_SOCKET_ITEM.get());
                        output.accept(ModItems.MACHINE_BATTERY_REDD_ITEM.get());
                        output.accept(ModItems.GEIGER_BLOCK_ITEM.get());
                        output.accept(ModItems.ASH_DIGAMMA_ITEM.get());
                        output.accept(ModItems.VENT_CHLORINE_ITEM.get());
                        output.accept(ModItems.VENT_CHLORINE_SEAL_ITEM.get());
                        output.accept(ModItems.CHLORINE_GAS_ITEM.get());
                        output.accept(ModItems.MACHINE_ARMOR_TABLE_ITEM.get());
                        output.accept(ModItems.ANVIL_IRON_ITEM.get());
                        output.accept(ModItems.ANVIL_LEAD_ITEM.get());
                        output.accept(ModItems.ANVIL_STEEL_ITEM.get());
                        output.accept(ModItems.ANVIL_DESH_ITEM.get());
                        output.accept(ModItems.ANVIL_FERROURANIUM_ITEM.get());
                        output.accept(ModItems.ANVIL_SATURNITE_ITEM.get());
                        output.accept(ModItems.ANVIL_BISMUTH_BRONZE_ITEM.get());
                        output.accept(ModItems.ANVIL_ARSENIC_BRONZE_ITEM.get());
                        output.accept(ModItems.ANVIL_SCHRABIDATE_ITEM.get());
                        output.accept(ModItems.ANVIL_DNT_ITEM.get());
                        output.accept(ModItems.ANVIL_OSMIRIDIUM_ITEM.get());
                        output.accept(ModItems.ANVIL_MURKY_ITEM.get());
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONSUMABLES = TABS.register(
            "consumables",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.consumables"))
                    .icon(() -> ModItems.GEIGER_COUNTER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BOOK_GUIDE.get());
                        output.accept(ModItems.GEIGER_COUNTER.get());
                        output.accept(ModItems.DOSIMETER.get());
                        output.accept(ModItems.IV_EMPTY.get());
                        output.accept(ModItems.IV_BLOOD.get());
                        output.accept(ModItems.RADAWAY.get());
                        output.accept(ModItems.RADAWAY_STRONG.get());
                        output.accept(ModItems.RADAWAY_FLUSH.get());
                        output.accept(ModItems.RAD_X.get());
                        output.accept(ModItems.PILL_HERBAL.get());
                        output.accept(ModItems.CHEESE.get());
                        output.accept(ModItems.GAS_MASK_FILTER.get());
                        output.accept(ModItems.GAS_MASK_FILTER_MONO.get());
                        output.accept(ModItems.GAS_MASK_FILTER_COMBO.get());
                        output.accept(ModItems.GAS_MASK_FILTER_RAG.get());
                        output.accept(ModItems.GAS_MASK_FILTER_PISS.get());
                        output.accept(ModItems.HAZMAT_HELMET.get());
                        output.accept(ModItems.HAZMAT_PLATE.get());
                        output.accept(ModItems.HAZMAT_LEGS.get());
                        output.accept(ModItems.HAZMAT_BOOTS.get());
                        output.accept(ModItems.ENVSUIT_HELMET.get());
                        output.accept(ModItems.ENVSUIT_PLATE.get());
                        output.accept(ModItems.ENVSUIT_LEGS.get());
                        output.accept(ModItems.ENVSUIT_BOOTS.get());
                        output.accept(ModItems.DNS_HELMET.get());
                        output.accept(ModItems.DNS_PLATE.get());
                        output.accept(ModItems.DNS_LEGS.get());
                        output.accept(ModItems.DNS_BOOTS.get());
                        output.accept(ModItems.GOGGLES.get());
                        output.accept(ModItems.ASHGLASSES.get());
                        output.accept(ModItems.GAS_MASK.get());
                        output.accept(ModItems.GAS_MASK_M65.get());
                        output.accept(ModItems.GAS_MASK_MONO.get());
                        output.accept(ModItems.GAS_MASK_OLDE.get());
                        output.accept(ModItems.MASK_RAG.get());
                        output.accept(ModItems.MASK_PISS.get());
                        output.accept(ModItems.CLADDING_PAINT.get());
                        output.accept(ModItems.CLADDING_RUBBER.get());
                        output.accept(ModItems.CLADDING_LEAD.get());
                        output.accept(ModItems.CLADDING_DESH.get());
                        output.accept(ModItems.CLADDING_GHIORSIUM.get());
                        output.accept(ModItems.CLADDING_IRON.get());
                        output.accept(ModItems.CLADDING_OBSIDIAN.get());
                        // Only the digging claw made the guest list.
                        output.accept(ModItems.MULTITOOL_DIG.get());
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WEAPONS = TABS.register(
            "weapons",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.weapon"))
                    .icon(() -> ModItems.GUN_PEPPERBOX.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.GUN_PEPPERBOX.get());
                        output.accept(ModItems.GUN_DEBUG.get());
                        output.accept(ModItems.GUN_LIGHT_REVOLVER.get());
                        output.accept(ModItems.GUN_LIGHT_REVOLVER_ATLAS.get());
                        output.accept(ModItems.GUN_LIGHT_REVOLVER_DANI.get());
                        output.accept(ModItems.GUN_HENRY.get());
                        output.accept(ModItems.GUN_HENRY_LINCOLN.get());
                        output.accept(ModItems.GUN_HEAVY_REVOLVER.get());
                        output.accept(ModItems.GUN_HEAVY_REVOLVER_LILMAC.get());
                        output.accept(ModItems.GUN_HEAVY_REVOLVER_PROTEGE.get());
                        output.accept(ModItems.GUN_HANGMAN.get());
                        output.accept(ModItems.GUN_GREASEGUN.get());
                        output.accept(ModItems.GUN_LAG.get());
                        output.accept(ModItems.GUN_UZI.get());
                        output.accept(ModItems.GUN_UZI_AKIMBO.get());
                        output.accept(ModItems.GUN_MARESLEG.get());
                        output.accept(ModItems.GUN_MARESLEG_AKIMBO.get());
                        output.accept(ModItems.GUN_MARESLEG_BROKEN.get());
                        output.accept(ModItems.GUN_LIBERATOR.get());
                        output.accept(ModItems.GUN_SPAS12.get());
                        output.accept(ModItems.GUN_AUTOSHOTGUN.get());
                        // Shredder recipe is on Saturnite vacation.
                        output.accept(ModItems.GUN_AUTOSHOTGUN_SHREDDER.get());
                        // Sexy escaped the Red Room through /give.
                        output.accept(ModItems.GUN_AUTOSHOTGUN_SEXY.get());
                        output.accept(ModItems.GUN_FLAREGUN.get());
                        output.accept(ModItems.GUN_CONGOLAKE.get());
                        output.accept(ModItems.GUN_MK108.get());
                        output.accept(ModItems.GUN_CARBINE.get());
                        output.accept(ModItems.GUN_MINIGUN.get());
                        output.accept(ModItems.GUN_MINIGUN_LACUNAE.get());
                        output.accept(ModItems.GUN_MINIGUN_DUAL.get());
                        // MAS-36 is also squatting in /give until the Red Room opens.
                        output.accept(ModItems.GUN_MAS36.get());
                        output.accept(ModItems.GUN_AM180.get());
                        output.accept(ModItems.GUN_STAR_F.get());
                        output.accept(ModItems.GUN_STAR_F_AKIMBO.get());
                        output.accept(ModItems.GUN_FLAMER.get());
                        // TODO weapon steel and the fancy sword dispenser
                        output.accept(ModItems.GUN_FLAMER_TOPAZ.get());
                        output.accept(ModItems.GUN_FLAMER_DAYBREAKER.get());
                        // TODO Desh gun bits
                        output.accept(ModItems.GUN_PANZERSCHRECK.get());
                        output.accept(ModItems.GUN_STINGER.get());
                        // TODO plastic, somehow the hard part of a rocket launcher
                        output.accept(ModItems.GUN_QUADRO.get());
                        output.accept(ModItems.GUN_MISSILE_LAUNCHER.get());
                        // TODO wood and rubber parts
                        output.accept(ModItems.GUN_G3.get());
                        // TODO cast some Saturnite witchcraft
                        output.accept(ModItems.GUN_G3_ZEBRA.get());
                        // TODO hard-plastic furniture
                        output.accept(ModItems.GUN_STG77.get());
                        output.accept(ModItems.GUN_AMAT.get());
                        output.accept(ModItems.GUN_AMAT_SUBTLETY.get());
                        output.accept(ModItems.GUN_AMAT_PENANCE.get());
                        output.accept(ModItems.GUN_M2.get());
                        output.accept(ModItems.GUN_TESLA_CANNON.get());
                        output.accept(ModItems.GUN_LASER_PISTOL.get());
                        // TODO Saturnite mechanisms; source upgrade recipe remains gated
                        output.accept(ModItems.GUN_LASER_PISTOL_PEW_PEW.get());
                        // TODO Pedestal, Morning Glory flower, and Selenium Steel secret materials
                        output.accept(ModItems.GUN_LASER_PISTOL_MORNING_GLORY.get());
                        // TODO weapon-mod scope plus the unported Bismoid Bronze weapon parts
                        output.accept(ModItems.GUN_LASRIFLE.get());
                        output.accept(ModItems.GUN_TAU.get());
                        output.accept(ModItems.GUN_COILGUN.get());
                        output.accept(ModItems.GUN_N_I_4_N_I.get());
                        output.accept(ModItems.GUN_FATMAN.get());
                        output.accept(ModItems.GUN_BOLTER.get());
                        output.accept(ModItems.GUN_DOUBLE_BARREL_SACRED_DRAGON.get());
                        output.accept(ModItems.GUN_FOLLY.get());
                        output.accept(ModItems.GUN_DOUBLE_BARREL.get());
                        output.accept(ModItems.GUN_ABERRATOR.get());
                        output.accept(ModItems.GUN_ABERRATOR_EOTT.get());
                        output.accept(ModItems.GUN_AUTOSHOTGUN_HERETIC.get());
                        output.accept(ModItems.GUN_FIREEXT.get());
                        output.accept(ModItems.GUN_CHARGE_THROWER.get());
                        output.accept(com.hbm.ntm.item.DrillItem.filledStack());
                        output.accept(ModItems.GUN_B92.get());
                        output.accept(ModItems.GUN_B93.get());
                        output.accept(ModItems.GUN_B92_AMMO.get());
                        output.accept(ModItems.WEAPONIZED_STARBLASTER_CELL.get());
                        for (PepperboxAmmoType type : PepperboxAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (Magnum357AmmoType type : Magnum357AmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (Magnum44AmmoType type : Magnum44AmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        output.accept(Equestrian44AmmoType.BOXCAR.createStack(ModItems.AMMO_SECRET.get(), 1));
                        for (NineMillimeterAmmoType type : NineMillimeterAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (FiveFiveSixAmmoType type : FiveFiveSixAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (FiftyCalAmmoType type : FiftyCalAmmoType.values()) {
                            output.accept(type.createStack(type.secret()
                                    ? ModItems.AMMO_SECRET.get() : ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (FollyAmmoType type : FollyAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_SECRET.get(), 1));
                        }
                        for (AberratorAmmoType type : AberratorAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_SECRET.get(), 1));
                        }
                        for (Shotgun12GaugeAmmoType type : Shotgun12GaugeAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (Shotgun10GaugeAmmoType type : Shotgun10GaugeAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (FortyMillimeterAmmoType type : FortyMillimeterAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (SevenSixTwoAmmoType type : SevenSixTwoAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (TwentyTwoAmmoType type : TwentyTwoAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (RocketAmmoType type : RocketAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (FlamerFuelType type : FlamerFuelType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (FireExtinguisherAmmoType type : FireExtinguisherAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_FIREEXT.get(), 1));
                        }
                        for (ChargeThrowerAmmoType type : ChargeThrowerAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (EnergyAmmoType type : EnergyAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (TauAmmoType type : TauAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (CoilAmmoType type : CoilAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (MiniNukeAmmoType type : MiniNukeAmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                        for (Bolt75AmmoType type : Bolt75AmmoType.values()) {
                            output.accept(type.createStack(ModItems.AMMO_STANDARD.get(), 1));
                        }
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NUKES = TABS.register(
            "nukes",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.nukes"))
                    .icon(() -> ModItems.DYNAMITE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.NUKE_GADGET_ITEM.get());
                        output.accept(ModItems.GADGET_WIREING.get());
                        output.accept(ModItems.GADGET_CORE.get());
                        output.accept(ModItems.EARLY_EXPLOSIVE_LENSES.get());
                        output.accept(ModItems.NUKE_BOY_ITEM.get());
                        output.accept(ModItems.BOY_SHIELDING.get());
                        output.accept(ModItems.BOY_TARGET.get());
                        output.accept(ModItems.BOY_BULLET.get());
                        output.accept(ModItems.BOY_PROPELLANT.get());
                        output.accept(ModItems.BOY_IGNITER.get());
                        output.accept(ModItems.NUKE_MAN_ITEM.get());
                        output.accept(ModItems.MAN_IGNITER.get());
                        output.accept(ModItems.MAN_CORE.get());
                        output.accept(ModItems.EXPLOSIVE_LENSES.get());
                        output.accept(ModItems.NUKE_MIKE_ITEM.get());
                        output.accept(ModItems.MIKE_CORE.get());
                        output.accept(ModItems.MIKE_DEUT.get());
                        output.accept(ModItems.MIKE_COOLING_UNIT.get());
                        output.accept(ModItems.NUKE_TSAR_ITEM.get());
                        output.accept(ModItems.TSAR_CORE.get());
                        output.accept(ModItems.NUKE_FLEIJA_ITEM.get());
                        output.accept(ModItems.FLEIJA_IGNITER.get());
                        output.accept(ModItems.FLEIJA_PROPELLANT.get());
                        output.accept(ModItems.FLEIJA_CORE.get());
                        output.accept(ModItems.NUKE_SOLINIUM_ITEM.get());
                        output.accept(ModItems.SOLINIUM_IGNITER.get());
                        output.accept(ModItems.SOLINIUM_PROPELLANT.get());
                        output.accept(ModItems.SOLINIUM_CORE.get());
                        output.accept(ModItems.NUKE_N2_ITEM.get());
                        output.accept(ModItems.N2_CHARGE.get());
                        output.accept(ModItems.NUKE_PROTOTYPE_ITEM.get());
                        output.accept(ModItems.NUKE_CUSTOM_ITEM.get());
                        output.accept(ModItems.CUSTOM_TNT.get());
                        output.accept(ModItems.CUSTOM_NUKE.get());
                        output.accept(ModItems.CUSTOM_SCHRAB.get());
                        output.accept(ModItems.NUKE_FSTBMB_ITEM.get());
                        output.accept(ModItems.EGG_BALEFIRE.get());
                        output.accept(ModItems.EGG_BALEFIRE_SHARD.get());
                        output.accept(ModItems.BOMB_MULTI_ITEM.get());
                        // flame_war and emp_bomb missed roll call.
                        output.accept(ModItems.FLOAT_BOMB_ITEM.get());
                        output.accept(ModItems.THERM_ENDO_ITEM.get());
                        output.accept(ModItems.THERM_EXO_ITEM.get());
                        output.accept(ModItems.BATTERY_SPARK.get());
                        output.accept(ModItems.BATTERY_TRIXITE.get());
                        output.accept(ModItems.IGNITER.get());
                        output.accept(ModItems.DYNAMITE_ITEM.get());
                        output.accept(ModItems.TNT_NTM_ITEM.get());
                        output.accept(ModItems.SEMTEX_ITEM.get());
                        output.accept(ModItems.C4_ITEM.get());
                        output.accept(ModItems.CHARGE_DYNAMITE_ITEM.get());
                        output.accept(ModItems.CHARGE_MINER_ITEM.get());
                        output.accept(ModItems.CHARGE_C4_ITEM.get());
                        output.accept(ModItems.CHARGE_SEMTEX_ITEM.get());
                        // Charges first, terrible stepping stones second.
                        output.accept(ModItems.MINE_AP_ITEM.get());
                        output.accept(ModItems.MINE_SHRAP_ITEM.get());
                        output.accept(ModItems.MINE_HE_ITEM.get());
                        output.accept(ModItems.MINE_FAT_ITEM.get());
                        output.accept(ModItems.MINE_NAVAL_ITEM.get());
                        output.accept(ModItems.DEFUSER.get());
                        output.accept(ModItems.RANGEFINDER.get());
                        output.accept(ModItems.DETONATOR.get());
                        output.accept(ModItems.DETONATOR_MULTI.get());
                        output.accept(ModItems.DETONATOR_LASER.get());
                        output.accept(ModItems.DETONATOR_DEADMAN.get());
                        output.accept(ModItems.DETONATOR_DE.get());
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKS = TABS.register(
            "blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.blocks"))
                    .icon(() -> ModItems.URANIUM_BLOCK_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ORE_TITANIUM_ITEM.get());
                        output.accept(ModItems.ORE_TUNGSTEN_ITEM.get());
                        output.accept(ModItems.ORE_COBALT_ITEM.get());
                        output.accept(ModItems.ORE_RARE_ITEM.get());
                        output.accept(ModItems.ORE_COLTAN_ITEM.get());
                        ModItems.LEGACY_ORE_BLOCK_ITEMS.forEach((id, item) -> {
                            if (!id.startsWith("gas_") && !id.equals("ore_bedrock")) output.accept(item.get());
                        });
                        for (com.hbm.ntm.block.StoneResourceBlock.Type type
                                : com.hbm.ntm.block.StoneResourceBlock.Type.values()) {
                            output.accept(com.hbm.ntm.item.StoneResourceBlockItem.create(
                                    ModItems.STONE_RESOURCE_ITEM.get(), type, 1));
                        }
                        output.accept(ModItems.ORE_OIL_ITEM.get());
                        output.accept(ModItems.ORE_OIL_EMPTY_ITEM.get());
                        output.accept(ModItems.DIRT_OILY_ITEM.get());
                        output.accept(ModItems.DIRT_DEAD_ITEM.get());
                        output.accept(ModItems.SAND_DIRTY_ITEM.get());
                        output.accept(ModItems.SAND_DIRTY_RED_ITEM.get());
                        output.accept(ModItems.STONE_CRACKED_ITEM.get());
                        output.accept(ModItems.REINFORCED_STONE_ITEM.get());
                        output.accept(ModItems.REINFORCED_GLASS_ITEM.get());
                        output.accept(ModItems.REINFORCED_GLASS_PANE_ITEM.get());
                        output.accept(ModItems.GNEISS_TILE_ITEM.get());
                        output.accept(ModItems.GNEISS_BRICK_ITEM.get());
                        output.accept(ModItems.GNEISS_CHISELED_ITEM.get());
                        output.accept(ModItems.REINFORCED_LIGHT_ITEM.get());
                        output.accept(ModItems.REINFORCED_SAND_ITEM.get());
                        output.accept(ModItems.DEPTH_BRICK_ITEM.get());
                        output.accept(ModItems.DEPTH_TILES_ITEM.get());
                        output.accept(ModItems.DEPTH_NETHER_BRICK_ITEM.get());
                        output.accept(ModItems.DEPTH_NETHER_TILES_ITEM.get());
                        output.accept(ModItems.PLANT_DEAD_ITEM.get());
                        output.accept(ModItems.OIL_SPILL_ITEM.get());
                        output.accept(ModItems.WASTE_EARTH_ITEM.get());
                        output.accept(ModItems.WASTE_MYCELIUM_ITEM.get());
                        output.accept(ModItems.WASTE_TRINITITE_ITEM.get());
                        output.accept(ModItems.WASTE_TRINITITE_RED_ITEM.get());
                        output.accept(ModItems.WASTE_LOG_ITEM.get());
                        output.accept(ModItems.WASTE_PLANKS_ITEM.get());
                        // Frozen family photo, in registration order.
                        output.accept(ModItems.FROZEN_GRASS_ITEM.get());
                        output.accept(ModItems.FROZEN_DIRT_ITEM.get());
                        output.accept(ModItems.FROZEN_LOG_ITEM.get());
                        output.accept(ModItems.FROZEN_PLANKS_ITEM.get());
                        output.accept(ModItems.BLOCK_TRINITITE_ITEM.get());
                        output.accept(ModItems.BLOCK_WASTE_ITEM.get());
                        for (int level = 0; level < 6; level++) {
                            output.accept(SellafieldBlockItem.create(ModItems.SELLAFIELD_ITEM.get(), level, 1));
                        }
                        output.accept(ModItems.SELLAFIELD_SLAKED_ITEM.get());
                        HazardousMaterialDefinitions.BLOCKS.forEach(
                                definition -> output.accept(ModItems.getBlockItem(definition.id()).get())
                        );
                        MaterialDefinitions.BLOCKS.forEach(
                                definition -> output.accept(ModItems.getBlockItem(definition.id()).get())
                        );
                        output.accept(ModItems.CONCRETE_SMOOTH_ITEM.get());
                        output.accept(ModItems.BLOCK_INSULATOR_ITEM.get());
                        output.accept(ModItems.SANDBAGS_ITEM.get());
                        output.accept(ModItems.GRAVEL_OBSIDIAN_ITEM.get());
                        output.accept(ModItems.GRAVEL_DIAMOND_ITEM.get());
                        output.accept(ModItems.BLOCK_COKE_COAL_ITEM.get());
                        output.accept(ModItems.BLOCK_COKE_LIGNITE_ITEM.get());
                        output.accept(ModItems.BLOCK_COKE_PETROLEUM_ITEM.get());
                        for (com.hbm.ntm.block.ScaffoldBlock.Variant variant
                                : com.hbm.ntm.block.ScaffoldBlock.Variant.values()) {
                            output.accept(com.hbm.ntm.item.ScaffoldBlockItem.create(
                                    ModItems.STEEL_SCAFFOLD_ITEM.get(), variant, 1));
                        }
                        output.accept(ModItems.FALLOUT.get());
                    })
                    .build()
    );

    private ModCreativeTabs() {
    }

    private static void addWasteVariants(CreativeModeTab.Output output, NuclearWasteItem item) {
        for (int variant = 0; variant < item.variantCount(); variant++) {
            output.accept(NuclearWasteItem.stack(item, variant, 1));
        }
    }

    public static void register(IEventBus modEventBus) {
        TABS.register(modEventBus);
    }
}
