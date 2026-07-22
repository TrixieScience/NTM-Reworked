package com.hbm.ntm.recipe;

import com.hbm.ntm.item.BoltItem;
import com.hbm.ntm.item.CasingItem;
import com.hbm.ntm.item.PipeItem;
import com.hbm.ntm.item.SourceFluidContainerItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.weapon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class AmmoPressRecipes {
    private static List<Recipe> recipes;

    private AmmoPressRecipes() { }

    public static List<Recipe> all() {
        if (recipes == null) recipes = List.copyOf(build());
        return recipes;
    }

    public static Recipe get(int index) {
        return index >= 0 && index < all().size() ? all().get(index) : null;
    }

    private static List<Recipe> build() {
        List<Recipe> out = new ArrayList<>();
        SlotIngredient lead = tag("c:ingots/lead", 1, "ingot_lead");
        SlotIngredient nugget = tag("c:nuggets/lead", 1, "nugget_lead");
        SlotIngredient flechette = bolt(BoltItem.BoltMaterial.LEAD, 1);
        SlotIngredient steel = tag("c:ingots/steel", 1, "ingot_steel");
        SlotIngredient weaponSteel = item("ingot_weaponsteel", 1);
        SlotIngredient copper = tag("c:ingots/copper", 1, "ingot_copper");
        SlotIngredient plastic = item("ingot_polymer", 1);
        SlotIngredient uranium = item("ingot_u238", 1);
        SlotIngredient ferro = item("ingot_ferrouranium", 1);
        SlotIngredient niobium = tag("c:ingots/niobium", 1, "ingot_niobium");
        SlotIngredient smokeless = tag("hbm:smokeless", 1, "cordite");
        SlotIngredient whitePhosphorus = item("ingot_phosphorus", 1);
        SlotIngredient redPhosphorus = tag("c:dusts/red_phosphorus", 1, "powder_fire");
        SlotIngredient steelPipe = pipe(PipeItem.PipeMaterial.STEEL, 1);
        SlotIngredient gunpowder = vanilla("gunpowder", 1);
        SlotIngredient rocketFuel = exact(ModItems.ROCKET_FUEL::get, 1);
        SlotIngredient small = casing(CasingItem.CasingType.SMALL, 1);
        SlotIngredient large = casing(CasingItem.CasingType.LARGE, 1);
        SlotIngredient smallSteel = casing(CasingItem.CasingType.SMALL_STEEL, 1);
        SlotIngredient largeSteel = casing(CasingItem.CasingType.LARGE_STEEL, 1);
        SlotIngredient shotshell = casing(CasingItem.CasingType.SHOTSHELL, 1);
        SlotIngredient buckshot = casing(CasingItem.CasingType.BUCKSHOT, 1);
        SlotIngredient advancedShell = casing(CasingItem.CasingType.BUCKSHOT_ADVANCED, 1);

        addPistolFamily(out, Magnum357AmmoType.values(), new int[]{16, 8, 8, 8, 8, 8}, true,
                lead, steel, copper, weaponSteel, plastic, gunpowder, smokeless, small, smallSteel);
        addPistolFamily(out, Magnum44AmmoType.values(), new int[]{12, 6, 6, 6, 6, 6}, true,
                lead, steel, copper, weaponSteel, plastic, gunpowder, smokeless, small, smallSteel);
        addPistolFamily(out, TwentyTwoAmmoType.values(), new int[]{24, 24, 24, 24}, false,
                lead, steel, copper, weaponSteel, plastic, gunpowder, smokeless, small, smallSteel);
        addPistolFamily(out, NineMillimeterAmmoType.values(), new int[]{12, 12, 12, 12}, false,
                lead, steel, copper, weaponSteel, plastic, gunpowder, smokeless, small, smallSteel);

        add(out, ammo(FiveFiveSixAmmoType.SOFT_POINT, 16), grid(null, lead.withCount(2), null,
                null, smokeless.withCount(2), null, null, small.withCount(2), null));
        add(out, ammo(FiveFiveSixAmmoType.FULL_METAL_JACKET, 16), grid(null, steel.withCount(2), null,
                null, smokeless.withCount(2), null, null, small.withCount(2), null));
        add(out, ammo(FiveFiveSixAmmoType.HOLLOW_POINT, 16), grid(plastic, copper.withCount(2), null,
                null, smokeless.withCount(2), null, null, small.withCount(2), null));
        add(out, ammo(FiveFiveSixAmmoType.ARMOR_PIERCING, 16), grid(null, weaponSteel.withCount(2), null,
                null, smokeless.withCount(4), null, null, smallSteel.withCount(2), null));

        addRifleFamily(out, new SevenSixTwoAmmoType[]{SevenSixTwoAmmoType.SOFT_POINT,
                        SevenSixTwoAmmoType.FULL_METAL_JACKET, SevenSixTwoAmmoType.HOLLOW_POINT,
                        SevenSixTwoAmmoType.ARMOR_PIERCING, SevenSixTwoAmmoType.DEPLETED_URANIUM},
                12, false, lead, steel, copper, weaponSteel, uranium, plastic, smokeless, small, smallSteel);
        addRifleFamily(out, new FiftyCalAmmoType[]{FiftyCalAmmoType.SOFT_POINT, FiftyCalAmmoType.FULL_METAL_JACKET,
                        FiftyCalAmmoType.HOLLOW_POINT, FiftyCalAmmoType.ARMOR_PIERCING,
                        FiftyCalAmmoType.DEPLETED_URANIUM},
                12, true, lead, steel, copper, weaponSteel, uranium, plastic, smokeless, large, largeSteel);

        add(out, ammo(Shotgun12GaugeAmmoType.BLACK_POWDER_BUCKSHOT, 6), grid(null, nugget.withCount(6), null,
                null, gunpowder, null, null, shotshell, null));
        add(out, ammo(Shotgun12GaugeAmmoType.BLACK_POWDER_MAGNUM, 6), grid(null, nugget.withCount(8), null,
                null, gunpowder, null, null, shotshell, null));
        add(out, ammo(Shotgun12GaugeAmmoType.BLACK_POWDER_SLUG, 6), grid(null, lead, null,
                null, gunpowder, null, null, shotshell, null));
        add(out, ammo(Shotgun12GaugeAmmoType.BUCKSHOT, 6), grid(null, nugget.withCount(6), null,
                null, smokeless, null, null, buckshot, null));
        add(out, ammo(Shotgun12GaugeAmmoType.SLUG, 6), grid(null, lead, null,
                null, smokeless, null, null, buckshot, null));
        add(out, ammo(Shotgun12GaugeAmmoType.FLECHETTE, 6), grid(null, flechette.withCount(12), null,
                null, smokeless, null, null, buckshot, null));
        add(out, ammo(Shotgun12GaugeAmmoType.MAGNUM, 6), grid(null, nugget.withCount(8), null,
                null, smokeless, null, null, advancedShell, null));
        add(out, ammo(Shotgun12GaugeAmmoType.PHOSPHORUS, 6), grid(null, whitePhosphorus, null,
                null, smokeless, null, null, advancedShell, null));

        add(out, ammo(Shotgun10GaugeAmmoType.BUCKSHOT, 4), grid(null, nugget.withCount(8), null,
                null, smokeless.withCount(2), null, null, advancedShell, null));
        add(out, ammo(Shotgun10GaugeAmmoType.SHRAPNEL, 4), grid(plastic, nugget.withCount(8), null,
                null, smokeless.withCount(2), null, null, advancedShell, null));
        add(out, ammo(Shotgun10GaugeAmmoType.DEPLETED_URANIUM, 4), grid(null, uranium, null,
                null, smokeless.withCount(2), null, null, advancedShell, null));
        add(out, ammo(Shotgun10GaugeAmmoType.SLUG, 4), grid(null, lead, null,
                null, smokeless.withCount(2), null, null, advancedShell, null));

        add(out, ammo(FortyMillimeterAmmoType.SIGNAL_FLARE, 4), grid(null, redPhosphorus, null,
                null, smokeless, null, null, large, null));
        SlotIngredient dynamite = exact(ModItems.BALL_DYNAMITE::get, 1);
        SlotIngredient dieselCanister = fluid(SourceFluidContainerItem.ContainedFluid.DIESEL);
        add(out, ammo(FortyMillimeterAmmoType.HIGH_EXPLOSIVE, 4), grid(null, dynamite, null,
                null, smokeless, null, null, large, null));
        add(out, ammo(FortyMillimeterAmmoType.INCENDIARY, 4), grid(dieselCanister, dynamite, null,
                null, smokeless, null, null, large, null));
        add(out, ammo(RocketAmmoType.HIGH_EXPLOSIVE, 2), grid(null, dynamite, null,
                null, large, null, null, smokeless.withCount(3), null));
        add(out, ammo(RocketAmmoType.HIGH_EXPLOSIVE, 2), grid(null, dynamite, null,
                null, large, null, null, rocketFuel, null));
        add(out, ammo(RocketAmmoType.INCENDIARY, 2), grid(dieselCanister, dynamite, null,
                null, large, null, null, smokeless.withCount(3), null));
        add(out, ammo(RocketAmmoType.INCENDIARY, 2), grid(dieselCanister, dynamite, null,
                null, large, null, null, rocketFuel, null));

        SlotIngredient steelPlate = tag("c:plates/steel", 1, "plate_steel");
        add(out, ammo(FlamerFuelType.DIESEL, 1), grid(null, steelPlate, null,
                null, dieselCanister, null, null, steelPlate, null));
        add(out, ammo(FlamerFuelType.GAS, 1), grid(null, steelPlate, null,
                null, fluid(SourceFluidContainerItem.ContainedFluid.GAS), null, null, steelPlate, null));

        SlotIngredient silicon = tag("c:billets/silicon", 1, "billet_silicon");
        add(out, ammo(EnergyAmmoType.STANDARD, 4), grid(null, plastic, null,
                null, silicon.withCount(4), null, null, plastic, null));
        add(out, ammo(EnergyAmmoType.OVERCHARGE, 4), grid(null, plastic, null,
                null, silicon.withCount(6), null, null, plastic, null));
        add(out, ammo(EnergyAmmoType.LOW_WAVELENGTH, 4), grid(null, plastic, null,
                null, niobium, null, null, plastic, null));
        SlotIngredient leadPlate = tag("c:plates/lead", 1, "plate_lead");
        add(out, ammo(TauAmmoType.DEPLETED_URANIUM, 16), grid(null, leadPlate, null,
                null, uranium, null, null, leadPlate, null));
        add(out, ammo(CoilAmmoType.TUNGSTEN, 4), grid(null, null, null,
                null, tag("c:ingots/tungsten", 1, "ingot_tungsten"), null, null, null, null));
        add(out, ammo(CoilAmmoType.FERROURANIUM, 4), grid(null, null, null,
                null, ferro, null, null, null, null));
        add(out, ammo(ChargeThrowerAmmoType.HOOK, 16), grid(null, steel, null,
                null, steelPipe, null, null, smokeless, null));
        return out;
    }

    private static void addPistolFamily(List<Recipe> out, SednaAmmoType[] types, int[] counts, boolean blackPowder,
                                        SlotIngredient lead, SlotIngredient steel, SlotIngredient copper,
                                        SlotIngredient weaponSteel, SlotIngredient plastic,
                                        SlotIngredient gunpowder, SlotIngredient smokeless,
                                        SlotIngredient small, SlotIngredient smallSteel) {
        for (int i = 0; i < types.length; i++) {
            int recipe = blackPowder ? i : i + 1;
            SlotIngredient[] grid = switch (recipe) {
                case 0 -> grid(null, lead.withCount(2), null, null, gunpowder, null, null, small, null);
                case 1 -> grid(null, lead, null, null, smokeless, null, null, small, null);
                case 2 -> grid(null, steel, null, null, smokeless, null, null, small, null);
                case 3 -> grid(plastic, copper, null, null, smokeless, null, null, small, null);
                case 4 -> grid(null, weaponSteel, null, null, smokeless.withCount(2), null,
                        null, smallSteel, null);
                default -> grid(null, steel, null, null, smokeless.withCount(3), null, null, small, null);
            };
            add(out, ammo(types[i], counts[i]), grid);
        }
    }

    private static void addRifleFamily(List<Recipe> out, SednaAmmoType[] types, int count,
                                       boolean fiftyCal, SlotIngredient lead, SlotIngredient steel, SlotIngredient copper,
                                       SlotIngredient weaponSteel, SlotIngredient uranium,
                                       SlotIngredient plastic, SlotIngredient smokeless,
                                       SlotIngredient normalCasing, SlotIngredient steelCasing) {
        int regularPowder = fiftyCal ? 3 : 2;
        int armorPowder = fiftyCal ? 6 : 4;
        int casingCount = fiftyCal ? 1 : 2;
        for (int i = 0; i < types.length; i++) {
            SlotIngredient[] grid = switch (i) {
                case 0 -> grid(null, lead.withCount(2), null, null, smokeless.withCount(regularPowder), null,
                        null, normalCasing.withCount(casingCount), null);
                case 1 -> grid(null, steel.withCount(2), null, null, smokeless.withCount(regularPowder), null,
                        null, normalCasing.withCount(casingCount), null);
                case 2 -> grid(plastic, copper.withCount(2), null, null, smokeless.withCount(regularPowder), null,
                        null, normalCasing.withCount(casingCount), null);
                case 3 -> grid(null, weaponSteel.withCount(2), null, null, smokeless.withCount(armorPowder), null,
                        null, steelCasing.withCount(casingCount), null);
                default -> grid(null, uranium.withCount(2), null, null, smokeless.withCount(armorPowder), null,
                        null, steelCasing.withCount(casingCount), null);
            };
            add(out, ammo(types[i], count), grid);
        }
    }

    private static void add(List<Recipe> recipes, ItemStack output, SlotIngredient[] grid) {
        recipes.add(new Recipe(recipes.size(), output, Arrays.asList(grid.clone())));
    }

    private static ItemStack ammo(SednaAmmoType type, int count) {
        return type.createStack(ModItems.AMMO_STANDARD.get(), count);
    }

    private static SlotIngredient[] grid(SlotIngredient... slots) {
        if (slots.length != 9) throw new IllegalArgumentException("Ammo Press recipes need nine slots");
        return slots;
    }

    private static SlotIngredient item(String id, int count) {
        return exact(() -> ModItems.get(id).get(), count);
    }

    private static SlotIngredient vanilla(String id, int count) {
        return exact(() -> BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(id)), count);
    }

    private static SlotIngredient exact(Supplier<Item> item, int count) {
        return new SlotIngredient(stack -> stack.is(item.get()), () -> new ItemStack(item.get(), count), count);
    }

    private static SlotIngredient tag(String id, int count, String display) {
        TagKey<Item> key = TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                ResourceLocation.parse(id));
        return new SlotIngredient(stack -> stack.is(key), () -> new ItemStack(ModItems.get(display).get(), count), count);
    }

    private static SlotIngredient casing(CasingItem.CasingType type, int count) {
        return new SlotIngredient(stack -> stack.is(ModItems.CASING.get()) && CasingItem.type(stack) == type,
                () -> CasingItem.create(ModItems.CASING.get(), type, count), count);
    }

    private static SlotIngredient bolt(BoltItem.BoltMaterial type, int count) {
        return new SlotIngredient(stack -> stack.is(ModItems.BOLT.get()) && BoltItem.material(stack) == type,
                () -> BoltItem.create(ModItems.BOLT.get(), type, count), count);
    }

    private static SlotIngredient pipe(PipeItem.PipeMaterial type, int count) {
        return new SlotIngredient(stack -> stack.is(ModItems.PIPE.get()) && PipeItem.material(stack) == type,
                () -> PipeItem.create(ModItems.PIPE.get(), type, count), count);
    }

    private static SlotIngredient fluid(SourceFluidContainerItem.ContainedFluid fluid) {
        return new SlotIngredient(stack -> stack.is(ModItems.CANISTER_FULL.get())
                        && SourceFluidContainerItem.fluid(stack) == fluid,
                () -> SourceFluidContainerItem.create(ModItems.CANISTER_FULL.get(), fluid, 1), 1);
    }

    public record Recipe(int index, ItemStack output, List<SlotIngredient> inputs) {
        public Recipe {
            output = output.copy();
            inputs = Collections.unmodifiableList(new ArrayList<>(inputs));
        }
        public SlotIngredient input(int slot) { return inputs.get(slot); }
    }

    public record SlotIngredient(Predicate<ItemStack> predicate, Supplier<ItemStack> display, int count) {
        public boolean matches(ItemStack stack, boolean ignoreCount) {
            return !stack.isEmpty() && predicate.test(stack) && (ignoreCount || stack.getCount() >= count);
        }
        public ItemStack displayStack() { return display.get(); }
        public SlotIngredient withCount(int amount) {
            return new SlotIngredient(predicate, () -> display.get().copyWithCount(amount), amount);
        }
    }
}
