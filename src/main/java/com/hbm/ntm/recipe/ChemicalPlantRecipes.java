package com.hbm.ntm.recipe;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.ArcElectrodeItem;
import com.hbm.ntm.item.BatteryPackItem;
import com.hbm.ntm.item.SourceFluidContainerItem;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.function.Supplier;

/** Chemical Plant recipes whose ingredients and outputs are registered. */
public final class ChemicalPlantRecipes {
    public static final ResourceLocation HYDROGEN = id("chem.hydrogen");
    public static final ResourceLocation HYDROGEN_COKE = id("chem.hydrogencoke");
    public static final ResourceLocation OXYGEN = id("chem.oxygen");
    public static final ResourceLocation PEROXIDE = id("chem.peroxide");
    public static final ResourceLocation SULFURIC_ACID = id("chem.sulfuricacid");
    public static final ResourceLocation SAS3 = id("chem.sas3");
    public static final ResourceLocation COLTAN_CLEANING = id("chem.coltancleaning");
    public static final ResourceLocation COLTAN_PAIN = id("chem.coltanpain");
    public static final ResourceLocation COLTAN_CRYSTAL = id("chem.coltancrystal");
    public static final ResourceLocation DESH = id("chem.desh");
    public static final ResourceLocation POLYMER = id("chem.polymer");
    public static final ResourceLocation RUBBER = id("chem.rubber");
    public static final ResourceLocation COAL_LUBE = id("chem.coallube");
    public static final ResourceLocation HEAVY_LUBE = id("chem.heavylube");
    public static final ResourceLocation COBBLE = id("chem.cobble");
    public static final ResourceLocation AGGREGATE = id("chem.aggregate");
    public static final ResourceLocation CONCRETE = id("chem.concrete");
    public static final ResourceLocation BATTERY_LEAD = id("chem.batterylead");
    public static final ResourceLocation OIL_ELECTRODES = id("chem.oilelectrodes");
    public static final ResourceLocation LUBE_ELECTRODES = id("chem.lubeelectrodes");
    public static final ResourceLocation CORDITE = id("chem.cordite");
    public static final ResourceLocation DYNAMITE = id("chem.dynamite");

    private static final List<ChemicalRecipe> RECIPES = List.of(
            new ChemicalRecipe(HYDROGEN, "chem.hydrogen", 20, 400,
                    List.of(new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                            ResourceLocation.fromNamespaceAndPath("c", "gems/coal"))), 1)),
                    List.of(new FluidInput(() -> net.minecraft.world.level.material.Fluids.WATER, 8_000)),
                    List.of(), List.of(new FluidOutput(ModFluids.HYDROGEN, 500)), List.of()),
            new ChemicalRecipe(HYDROGEN_COKE, "chem.hydrogencoke", 20, 400,
                    List.of(new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                            ResourceLocation.fromNamespaceAndPath("c", "gems/coke"))), 1)),
                    List.of(new FluidInput(() -> net.minecraft.world.level.material.Fluids.WATER, 8_000)),
                    List.of(), List.of(new FluidOutput(ModFluids.HYDROGEN, 500)), List.of()),
            new ChemicalRecipe(OXYGEN, "chem.oxygen", 20, 400,
                    List.of(), List.of(new FluidInput(ModFluids.AIR, 8_000)),
                    List.of(), List.of(new FluidOutput(ModFluids.OXYGEN, 500)), List.of()),
            new ChemicalRecipe(COAL_LUBE, "chem.coallube", 40, 100,
                    List.of(), List.of(new FluidInput(ModFluids.COALCREOSOTE, 1_000)),
                    List.of(), List.of(new FluidOutput(ModFluids.LUBRICANT, 1_000)),
                    List.of("alt..lube")),
            new ChemicalRecipe(HEAVY_LUBE, "chem.heavylube", 40, 100,
                    List.of(), List.of(new FluidInput(ModFluids.HEAVYOIL, 2_000)),
                    List.of(), List.of(new FluidOutput(ModFluids.LUBRICANT, 1_000)),
                    List.of("alt..lube")),
            new ChemicalRecipe(COBBLE, "chem.cobble", 20, 100,
                    List.of(), List.of(
                            new FluidInput(() -> net.minecraft.world.level.material.Fluids.WATER, 1_000),
                            new FluidInput(() -> net.minecraft.world.level.material.Fluids.LAVA, 25)),
                    List.of(new ItemStack(Items.COBBLESTONE)), List.of(), List.of()),
            new ChemicalRecipe(AGGREGATE, "chem.aggregate", 320, 500,
                    List.of(new ItemInput(Ingredient.of(Items.COBBLESTONE), 16)), List.of(),
                    List.of(new ItemStack(Items.GRAVEL, 8), new ItemStack(Items.SAND, 8)), List.of(),
                    List.of("discover..stone")),
            new ChemicalRecipe(CONCRETE, "chem.concrete", 100, 100,
                    List.of(
                            new ItemInput(Ingredient.of(ModItems.get("powder_cement").get()), 1),
                            new ItemInput(Ingredient.of(Items.GRAVEL), 8),
                            new ItemInput(Ingredient.of(ItemTags.SAND), 8)),
                    List.of(new FluidInput(() -> net.minecraft.world.level.material.Fluids.WATER, 2_000)),
                    List.of(new ItemStack(ModItems.CONCRETE_SMOOTH_ITEM.get(), 16)), List.of(), List.of()),
            new ChemicalRecipe(BATTERY_LEAD, "chem.batterylead", 100, 100,
                    List.of(
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "plates/steel"))), 4),
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "ingots/lead"))), 4)),
                    List.of(new FluidInput(ModFluids.SULFURIC_ACID, 8_000)),
                    List.of(BatteryPackItem.create(ModItems.BATTERY_PACK.get(),
                            BatteryPackItem.BatteryType.BATTERY_LEAD, false)), List.of(), List.of()),
            new ChemicalRecipe(DESH, "chem.desh", 100, 100,
                    List.of(new ItemInput(Ingredient.of(ModItems.get("powder_desh_mix").get()), 1)),
                    List.of(new FluidInput(ModFluids.LIGHTOIL, 200),
                            new FluidInput(ModFluids.MERCURY, 200)),
                    List.of(new ItemStack(ModItems.get("ingot_desh").get())), List.of(), List.of()),
            new ChemicalRecipe(POLYMER, "chem.polymer", 100, 100,
                    List.of(
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "dusts/coal"))), 2),
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "dusts/fluorite"))), 1)),
                    List.of(new FluidInput(ModFluids.PETROLEUM, 1_000)),
                    List.of(new ItemStack(ModItems.get("ingot_polymer").get(), 4)), List.of(), List.of()),
            new ChemicalRecipe(RUBBER, "chem.rubber", 100, 200,
                    List.of(new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                            ResourceLocation.fromNamespaceAndPath("c", "dusts/sulfur"))), 1)),
                    List.of(new FluidInput(ModFluids.UNSATURATEDS, 500)),
                    List.of(new ItemStack(ModItems.get("ingot_rubber").get(), 2)), List.of(), List.of()),
            new ChemicalRecipe(OIL_ELECTRODES, "chem.oilelectrodes", 600, 100,
                    List.of(), List.of(new FluidInput(ModFluids.HEATINGOIL, 4_000)),
                    List.of(ArcElectrodeItem.create(ModItems.ARC_ELECTRODE.get(),
                            ArcElectrodeItem.ElectrodeType.GRAPHITE, 1)), List.of(),
                    List.of("alt..electrodes")),
            new ChemicalRecipe(LUBE_ELECTRODES, "chem.lubeelectrodes", 600, 100,
                    List.of(), List.of(new FluidInput(ModFluids.LUBRICANT, 8_000)),
                    List.of(ArcElectrodeItem.create(ModItems.ARC_ELECTRODE.get(),
                            ArcElectrodeItem.ElectrodeType.GRAPHITE, 1)), List.of(),
                    List.of("alt..electrodes")),
            new ChemicalRecipe(PEROXIDE, "chem.peroxide", 50, 100,
                    List.of(), List.of(new FluidInput(() -> net.minecraft.world.level.material.Fluids.WATER, 1_000)),
                    List.of(), List.of(new FluidOutput(ModFluids.PEROXIDE, 1_000)), List.of()),
            new ChemicalRecipe(SULFURIC_ACID, "chem.sulfuricacid", 50, 100,
                    List.of(new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                            ResourceLocation.fromNamespaceAndPath("c", "dusts/sulfur"))), 1)),
                    List.of(new FluidInput(ModFluids.PEROXIDE, 1_000),
                            new FluidInput(() -> net.minecraft.world.level.material.Fluids.WATER, 1_000)),
                    List.of(), List.of(new FluidOutput(ModFluids.SULFURIC_ACID, 2_000)), List.of()),
            new ChemicalRecipe(SAS3, "chem.sas3", 200, 5_000,
                    List.of(
                            new ItemInput(Ingredient.of(ModItems.get("powder_schrabidium").get()), 1),
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "dusts/sulfur"))), 2)),
                    List.of(new FluidInput(ModFluids.PEROXIDE, 2_000)),
                    List.of(), List.of(new FluidOutput(ModFluids.SAS3, 1_000)), List.of()),
            new ChemicalRecipe(COLTAN_CLEANING, "chem.coltancleaning", 60, 100,
                    List.of(
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "dusts/coltan"))), 2),
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "dusts/coal"))), 1)),
                    List.of(new FluidInput(ModFluids.PEROXIDE, 250),
                            new FluidInput(ModFluids.HYDROGEN, 500)),
                    List.of(new ItemStack(ModItems.get("powder_coltan").get()),
                            new ItemStack(ModItems.get("powder_niobium").get()),
                            new ItemStack(ModItems.get("dust").get())),
                    List.of(new FluidOutput(() -> net.minecraft.world.level.material.Fluids.WATER, 500)),
                    List.of()),
            new ChemicalRecipe(COLTAN_PAIN, "chem.coltanpain", 120, 100,
                    List.of(
                            new ItemInput(Ingredient.of(ModItems.get("powder_coltan").get()), 1),
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "dusts/fluorite"))), 1)),
                    List.of(new FluidInput(ModFluids.GAS, 1_000),
                            new FluidInput(ModFluids.OXYGEN, 500)),
                    List.of(), List.of(new FluidOutput(ModFluids.PAIN, 1_000)), List.of()),
            new ChemicalRecipe(COLTAN_CRYSTAL, "chem.coltancrystal", 80, 100,
                    List.of(), List.of(new FluidInput(ModFluids.PAIN, 1_000),
                            new FluidInput(ModFluids.PEROXIDE, 500)),
                    List.of(new ItemStack(ModItems.get("gem_tantalium").get()),
                            new ItemStack(ModItems.get("dust").get(), 3)),
                    List.of(new FluidOutput(() -> net.minecraft.world.level.material.Fluids.WATER, 250)),
                    List.of()),
            new ChemicalRecipe(CORDITE, "chem.cordite", 40, 100,
                    List.of(
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "dusts/saltpeter"))), 2),
                            new ItemInput(Ingredient.of(ModItems.POWDER_SAWDUST.get()), 2)),
                    List.of(new FluidInput(ModFluids.GAS, 200)),
                    List.of(new ItemStack(ModItems.get("cordite").get(), 4)), List.of(), List.of()),
            new ChemicalRecipe(DYNAMITE, "chem.dynamite", 50, 100,
                    List.of(
                            new ItemInput(Ingredient.of(Items.SUGAR), 1),
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("c", "dusts/saltpeter"))), 1),
                            new ItemInput(Ingredient.of(TagKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath("minecraft", "sand"))), 1)),
                    List.of(), List.of(new ItemStack(ModItems.BALL_DYNAMITE.get(), 2)), List.of(), List.of())
    );

    private ChemicalPlantRecipes() { }

    public static List<ChemicalRecipe> all() { return RECIPES; }
    public static ChemicalRecipe get(ResourceLocation id) {
        for (ChemicalRecipe recipe : RECIPES) if (recipe.id().equals(id)) return recipe;
        return null;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    public record ItemInput(Ingredient ingredient, int count) {
        public boolean matches(ItemStack stack) { return ingredient.test(stack) && stack.getCount() >= count; }
        public ItemStack display() {
            ItemStack[] values = ingredient.getItems();
            return values.length == 0 ? ItemStack.EMPTY : values[0].copyWithCount(count);
        }
    }
    public record FluidInput(Supplier<? extends Fluid> fluid, int amount) { }
    public record FluidOutput(Supplier<? extends Fluid> fluid, int amount) { }
    public record ChemicalRecipe(ResourceLocation id, String name, int duration, long power,
                                 List<ItemInput> itemInputs, List<FluidInput> fluidInputs,
                                 List<ItemStack> itemOutputs, List<FluidOutput> fluidOutputs,
                                 List<String> pools) {
        public ItemStack icon() {
            if (id.equals(HYDROGEN) || id.equals(HYDROGEN_COKE)) return com.hbm.ntm.item.SourceFluidContainerItem.create(
                    ModItems.GAS_FULL.get(),
                    com.hbm.ntm.item.SourceFluidContainerItem.ContainedFluid.HYDROGEN, 1);
            if (id.equals(OXYGEN)) return SourceFluidContainerItem.create(
                    ModItems.GAS_FULL.get(), SourceFluidContainerItem.ContainedFluid.OXYGEN, 1);
            if (id.equals(PEROXIDE)) return com.hbm.ntm.item.UniversalFluidTankItem.create(
                    ModItems.FLUID_TANK_FULL.get(),
                    com.hbm.ntm.item.UniversalFluidTankItem.ContainedFluid.PEROXIDE, 1);
            if (id.equals(SULFURIC_ACID)) return com.hbm.ntm.item.UniversalFluidTankItem.create(
                    ModItems.FLUID_TANK_FULL.get(),
                    com.hbm.ntm.item.UniversalFluidTankItem.ContainedFluid.SULFURIC_ACID, 1);
            if (id.equals(SAS3)) return new ItemStack(ModItems.CELL_SAS3.get());
            if (id.equals(COLTAN_PAIN)) return com.hbm.ntm.item.UniversalFluidTankItem.create(
                    ModItems.FLUID_TANK_FULL.get(),
                    com.hbm.ntm.item.UniversalFluidTankItem.ContainedFluid.PAIN, 1);
            if (id.equals(COAL_LUBE) || id.equals(HEAVY_LUBE)) return SourceFluidContainerItem.create(
                    ModItems.CANISTER_FULL.get(), SourceFluidContainerItem.ContainedFluid.LUBRICANT, 1);
            if (!itemOutputs.isEmpty()) return itemOutputs.getFirst().copyWithCount(1);
            return ItemStack.EMPTY;
        }

        public int animationColor() {
            if (id.equals(HYDROGEN) || id.equals(HYDROGEN_COKE)) return 0x4286F4;
            if (id.equals(OXYGEN)) return 0x98BDF9;
            if (id.equals(PEROXIDE)) return 0xFFF7AA;
            if (id.equals(SULFURIC_ACID)) return 0xB0AA64;
            if (id.equals(SAS3)) return 0x4FFFFC;
            if (id.equals(COLTAN_PAIN) || id.equals(COLTAN_CRYSTAL)) return 0x938541;
            if (id.equals(DESH)) return 0x808080;
            if (id.equals(RUBBER)) return 0x2A2927;
            if (id.equals(COAL_LUBE) || id.equals(HEAVY_LUBE)) return 0x8C7B57;
            if (id.equals(COBBLE) || id.equals(AGGREGATE)) return 0x777777;
            if (id.equals(CONCRETE)) return 0xB8B8B8;
            if (id.equals(BATTERY_LEAD)) return 0x626C78;
            if (id.equals(CORDITE)) return 0x8A6541;
            if (id.equals(DYNAMITE)) return 0xD2B27A;
            if (id.equals(OIL_ELECTRODES) || id.equals(LUBE_ELECTRODES)) return 0x3A3A3A;
            return 0x8D7AAE;
        }
    }
}
