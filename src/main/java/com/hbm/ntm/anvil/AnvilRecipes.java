package com.hbm.ntm.anvil;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.ArcElectrodeItem;
import com.hbm.ntm.item.BoltItem;
import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.DenseWireItem;
import com.hbm.ntm.item.StirlingMachineBlockItem;
import com.hbm.ntm.item.PipeItem;
import com.hbm.ntm.item.ShellItem;
import com.hbm.ntm.item.SirenTrackItem;
import com.hbm.ntm.item.WireFineItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Everything the anvil can be convinced to flatten. */
public final class AnvilRecipes {
    public enum Overlay { NONE, CONSTRUCTION, RECYCLING, SMITHING }

    public record Input(Ingredient ingredient, int count, Predicate<ItemStack> extra, Supplier<ItemStack> display) {
        public boolean matches(ItemStack stack) { return ingredient.test(stack) && extra.test(stack); }
        public static Input item(Item item, int count) {
            return new Input(Ingredient.of(item), count, stack -> true, () -> new ItemStack(item));
        }
        public static Input item(Supplier<? extends Item> item, int count) {
            return new Input(Ingredient.of(item.get()), count, stack -> true, () -> new ItemStack(item.get()));
        }
        public static Input tag(String id, int count, Supplier<ItemStack> display) {
            ResourceLocation key = ResourceLocation.parse(id);
            return new Input(Ingredient.of(TagKey.create(Registries.ITEM, key)), count, stack -> true, display);
        }
        public static Input exact(Supplier<ItemStack> expected, int count) {
            return new Input(Ingredient.of(expected.get().getItem()), count,
                    stack -> ItemStack.isSameItemSameComponents(stack, expected.get()), expected);
        }
        public static Input undamaged(Supplier<? extends Item> item, int count) {
            return new Input(Ingredient.of(item.get()), count, stack -> stack.getDamageValue() == 0,
                    () -> new ItemStack(item.get()));
        }
    }

    public record Output(Supplier<ItemStack> stack, float chance) { }
    public record Smithing(int tier, Input left, Input right, Supplier<ItemStack> output,
                           int leftConsumed, int rightConsumed) {
        public Smithing(int tier, Input left, Input right, Supplier<ItemStack> output) {
            this(tier, left, right, output, left.count(), right.count());
        }
    }
    public record Construction(ResourceLocation id, int tierLower, int tierUpper, Overlay overlay,
                               List<Input> inputs, List<Output> outputs) {
        public boolean validForTier(int tier) {
            return tier >= tierLower && (tierUpper < 0 || tier <= tierUpper);
        }
        public ItemStack icon() { return outputs.getFirst().stack().get(); }
        public int bulkAttempts() {
            if (outputs.size() > 1) return 64;
            ItemStack output = outputs.getFirst().stack().get();
            return Math.max(1, output.getMaxStackSize() / output.getCount());
        }
    }

    private static final List<Smithing> SMITHING = new ArrayList<>();
    private static final List<Construction> CONSTRUCTION = new ArrayList<>();
    private static final Map<ResourceLocation, Construction> BY_ID = new LinkedHashMap<>();

    static {
        // Smithing recipes whose ingredients have entered this century.
        smith(1, item(ModItems.ANVIL_IRON_ITEM, 1), tag("c:ingots/steel", 10, itemStack("ingot_steel")),
                () -> new ItemStack(ModItems.ANVIL_STEEL_ITEM.get()));
        smith(1, item(ModItems.ANVIL_LEAD_ITEM, 1), tag("c:ingots/steel", 10, itemStack("ingot_steel")),
                () -> new ItemStack(ModItems.ANVIL_STEEL_ITEM.get()));
        smith(1, item(ModItems.ANVIL_IRON_ITEM, 1), tag("c:ingots/desh", 10, itemStack("ingot_desh")),
                () -> new ItemStack(ModItems.ANVIL_DESH_ITEM.get()));
        smith(1, item(ModItems.ANVIL_LEAD_ITEM, 1), tag("c:ingots/desh", 10, itemStack("ingot_desh")),
                () -> new ItemStack(ModItems.ANVIL_DESH_ITEM.get()));
        smith(1, item(ModItems.ANVIL_IRON_ITEM, 1),
                tag("c:ingots/ferrouranium", 10, itemStack("ingot_ferrouranium")),
                () -> new ItemStack(ModItems.ANVIL_FERROURANIUM_ITEM.get()));
        smith(1, item(ModItems.ANVIL_LEAD_ITEM, 1),
                tag("c:ingots/ferrouranium", 10, itemStack("ingot_ferrouranium")),
                () -> new ItemStack(ModItems.ANVIL_FERROURANIUM_ITEM.get()));
        smith(1, item(ModItems.ANVIL_IRON_ITEM, 1),
                tag("c:ingots/bismuth_bronze", 10, itemStack("ingot_bismuth_bronze")),
                () -> new ItemStack(ModItems.ANVIL_BISMUTH_BRONZE_ITEM.get()));
        smith(1, item(ModItems.ANVIL_LEAD_ITEM, 1),
                tag("c:ingots/bismuth_bronze", 10, itemStack("ingot_bismuth_bronze")),
                () -> new ItemStack(ModItems.ANVIL_BISMUTH_BRONZE_ITEM.get()));
        smith(1, item(ModItems.ANVIL_IRON_ITEM, 1),
                tag("c:ingots/arsenic_bronze", 10, itemStack("ingot_arsenic_bronze")),
                () -> new ItemStack(ModItems.ANVIL_ARSENIC_BRONZE_ITEM.get()));
        smith(1, item(ModItems.ANVIL_LEAD_ITEM, 1),
                tag("c:ingots/arsenic_bronze", 10, itemStack("ingot_arsenic_bronze")),
                () -> new ItemStack(ModItems.ANVIL_ARSENIC_BRONZE_ITEM.get()));
        smith(1, tag("c:ingots/copper", 1, itemStack("ingot_copper")),
                tag("c:ingots/aluminum", 1, itemStack("ingot_aluminium")), itemOutput("ingot_gunmetal", 1));

        // Left item demonstrates, right blank mold sacrifices itself.
        moldSmith(FoundryMoldItem.Mold.NUGGET,
                exactCountTag("c:nuggets/gold", 1, () -> new ItemStack(Items.GOLD_NUGGET)));
        moldSmith(FoundryMoldItem.Mold.BILLET,
                exactCountItem(() -> ModItems.get("billet_uranium").get(), 1));
        moldSmith(FoundryMoldItem.Mold.INGOT,
                exactCountTag("c:ingots/iron", 1, () -> new ItemStack(Items.IRON_INGOT)));
        moldSmith(FoundryMoldItem.Mold.PLATE,
                exactCountTag("c:plates/iron", 1, itemStack("plate_iron")));
        moldSmith(FoundryMoldItem.Mold.CAST_PLATE, exactCountSubtype(ModItems.PLATE_CAST, 1,
                stack -> CastPlateItem.material(stack) == CastPlateItem.CastPlateMaterial.IRON,
                () -> CastPlateItem.create(ModItems.PLATE_CAST.get(), CastPlateItem.CastPlateMaterial.IRON, 1)));
        moldSmith(FoundryMoldItem.Mold.CAST_PLATES, exactCountSubtype(ModItems.PLATE_CAST, 3,
                stack -> CastPlateItem.material(stack) == CastPlateItem.CastPlateMaterial.IRON,
                () -> CastPlateItem.create(ModItems.PLATE_CAST.get(), CastPlateItem.CastPlateMaterial.IRON, 3)));
        moldSmith(FoundryMoldItem.Mold.WIRES, exactCountSubtype(ModItems.WIRE_FINE, 1,
                stack -> WireFineItem.material(stack) == WireFineItem.WireMaterial.COPPER,
                () -> WireFineItem.create(ModItems.WIRE_FINE.get(), WireFineItem.WireMaterial.COPPER, 1)));
        moldSmith(FoundryMoldItem.Mold.BLADE, exactCountAny(1, () -> new ItemStack(ModItems.BLADE_TITANIUM.get()),
                ModItems.BLADE_TITANIUM.get(), ModItems.BLADE_TUNGSTEN.get()));
        moldSmith(FoundryMoldItem.Mold.BLADES, exactCountAny(1, () -> new ItemStack(ModItems.BLADES_STEEL.get()),
                ModItems.BLADES_STEEL.get(), ModItems.BLADES_TITANIUM.get()));
        moldSmith(FoundryMoldItem.Mold.STAMP, exactCountAny(1,
                () -> new ItemStack(ModItems.STAMPS.get("stamp_iron_flat").get()),
                ModItems.STAMPS.get("stamp_stone_flat").get(), ModItems.STAMPS.get("stamp_iron_flat").get(),
                ModItems.STAMPS.get("stamp_steel_flat").get(), ModItems.STAMPS.get("stamp_titanium_flat").get(),
                ModItems.STAMPS.get("stamp_obsidian_flat").get()));
        moldSmith(FoundryMoldItem.Mold.SHELL, exactCountSubtype(ModItems.SHELL, 1, ShellItem::isSteel,
                () -> ShellItem.steel(ModItems.SHELL.get(), 1)));
        moldSmith(FoundryMoldItem.Mold.PIPE, exactCountSubtype(ModItems.PIPE, 1, PipeItem::isSteel,
                () -> PipeItem.steel(ModItems.PIPE.get(), 1)));
        moldSmith(FoundryMoldItem.Mold.INGOTS,
                exactCountTag("c:ingots/iron", 9, () -> new ItemStack(Items.IRON_INGOT, 9)));
        moldSmith(FoundryMoldItem.Mold.PLATES,
                exactCountTag("c:plates/iron", 9, () -> new ItemStack(ModItems.get("plate_iron").get(), 9)));
        moldSmith(FoundryMoldItem.Mold.BLOCK,
                exactCountTag("c:storage_blocks/iron", 1, () -> new ItemStack(Items.IRON_BLOCK)));
        moldSmith(FoundryMoldItem.Mold.DENSE_WIRE, exactCountSubtype(ModItems.WIRE_DENSE, 1,
                stack -> DenseWireItem.material(stack) == com.hbm.ntm.foundry.FoundryMaterial.RED_COPPER,
                () -> DenseWireItem.create(ModItems.WIRE_DENSE.get(),
                        com.hbm.ntm.foundry.FoundryMaterial.RED_COPPER, 1)));
        moldSmith(FoundryMoldItem.Mold.DENSE_WIRES, exactCountSubtype(ModItems.WIRE_DENSE, 9,
                stack -> DenseWireItem.material(stack) == com.hbm.ntm.foundry.FoundryMaterial.RED_COPPER,
                () -> DenseWireItem.create(ModItems.WIRE_DENSE.get(),
                        com.hbm.ntm.foundry.FoundryMaterial.RED_COPPER, 9)));

        addMoldConstruction("mold_c9", 1, FoundryMoldItem.Mold.SMALL_CASING,
                tag("c:ingots/iron", 2, () -> new ItemStack(Items.IRON_INGOT)));
        addMoldConstruction("mold_c50", 1, FoundryMoldItem.Mold.LARGE_CASING,
                tag("c:ingots/iron", 2, () -> new ItemStack(Items.IRON_INGOT)));
        addMoldConstruction("mold_barrel_light", 2, FoundryMoldItem.Mold.LIGHT_BARREL,
                tag("c:ingots/steel", 4, itemStack("ingot_steel")));
        addMoldConstruction("mold_barrel_heavy", 2, FoundryMoldItem.Mold.HEAVY_BARREL,
                tag("c:ingots/steel", 4, itemStack("ingot_steel")));
        addMoldConstruction("mold_receiver_light", 2, FoundryMoldItem.Mold.LIGHT_RECEIVER,
                tag("c:ingots/steel", 4, itemStack("ingot_steel")));
        addMoldConstruction("mold_receiver_heavy", 2, FoundryMoldItem.Mold.HEAVY_RECEIVER,
                tag("c:ingots/steel", 4, itemStack("ingot_steel")));
        addMoldConstruction("mold_mechanism", 2, FoundryMoldItem.Mold.MECHANISM,
                tag("c:ingots/steel", 4, itemStack("ingot_steel")));
        addMoldConstruction("mold_stock", 2, FoundryMoldItem.Mold.STOCK,
                tag("c:ingots/steel", 4, itemStack("ingot_steel")));
        addMoldConstruction("mold_grip", 2, FoundryMoldItem.Mold.GRIP,
                tag("c:ingots/steel", 4, itemStack("ingot_steel")));

        add("pipe_copper", 1, Overlay.NONE,
                List.of(tag("c:plates/copper", 3, itemStack("plate_copper"))),
                List.of(new Output(() -> PipeItem.copper(ModItems.PIPE.get(), 1), 1F)));
        add("pipe_steel", 1, Overlay.NONE,
                List.of(tag("c:plates/steel", 3, itemStack("plate_steel"))),
                List.of(new Output(() -> PipeItem.steel(ModItems.PIPE.get(), 1), 1F)));
        add("pipe_lead", 1, Overlay.NONE,
                List.of(tag("c:plates/lead", 3, itemStack("plate_lead"))),
                List.of(new Output(() -> PipeItem.lead(ModItems.PIPE.get(), 1), 1F)));
        for (SirenTrackItem.Track track : SirenTrackItem.Track.values()) {
            if (track == SirenTrackItem.Track.NONE) continue;
            add("siren_track_" + track.id(), 2, Overlay.CONSTRUCTION,
                    List.of(tag("c:plates/steel", 1, itemStack("plate_steel")),
                            item(ModItems.PLATE_POLYMER, 1)),
                    List.of(new Output(() -> SirenTrackItem.create(ModItems.SIREN_TRACK.get(), track), 1F)));
        }
        add("shell_steel", 1, Overlay.NONE,
                List.of(tag("c:plates/steel", 4, itemStack("plate_steel"))),
                List.of(new Output(() -> ShellItem.steel(ModItems.SHELL.get(), 1), 1F)));
        add("coil_copper_torus", 1, Overlay.CONSTRUCTION,
                List.of(item(ModItems.COIL_COPPER, 2)), outputs(itemOutputStack(ModItems.COIL_COPPER_TORUS, 1)));
        add("coil_gold_torus", 1, Overlay.CONSTRUCTION,
                List.of(item(ModItems.COIL_GOLD, 2)), outputs(itemOutputStack(ModItems.COIL_GOLD_TORUS, 1)));
        add("motor", 1, Overlay.CONSTRUCTION,
                List.of(tag("c:plates/iron", 2, itemStack("plate_iron")), item(ModItems.COIL_COPPER, 1),
                        item(ModItems.COIL_COPPER_TORUS, 1)), outputs(itemOutputStack(ModItems.MOTOR, 2)));
        add("motor_desh", 3, Overlay.CONSTRUCTION,
                List.of(item(ModItems.MOTOR, 1),
                        tag("c:ingots/polymer", 2, itemStack("ingot_polymer")),
                        tag("c:ingots/desh", 2, itemStack("ingot_desh")),
                        subtype(ModItems.WIRE_DENSE, 1,
                                stack -> DenseWireItem.material(stack)
                                        == com.hbm.ntm.foundry.FoundryMaterial.GOLD,
                                () -> DenseWireItem.create(ModItems.WIRE_DENSE.get(),
                                        com.hbm.ntm.foundry.FoundryMaterial.GOLD, 1))),
                outputs(itemOutputStack(ModItems.MOTOR_DESH, 1)));
        add("plate_desh", 3, Overlay.CONSTRUCTION,
                List.of(tag("c:ingots/desh", 4, itemStack("ingot_desh")),
                        tag("c:dusts/polymer", 2, itemStack("powder_polymer")),
                        tag("c:ingots/dura_steel", 1, itemStack("ingot_dura_steel"))),
                outputs(itemOutput("plate_desh", 4)));
        add("machine_blast_furnace", 1, Overlay.CONSTRUCTION,
                List.of(Input.item(Items.STONE_BRICKS, 4),
                        item(() -> ModItems.get("ingot_firebrick").get(), 32),
                        tag("c:plates/copper", 8, itemStack("plate_copper"))),
                outputs(itemOutputStack(ModItems.MACHINE_BLAST_FURNACE_ITEM, 1)));
        add("furnace_combination", 2, Overlay.CONSTRUCTION,
                List.of(Input.item(Items.STONE_BRICKS, 8),
                        Input.tag(ItemTags.LOGS.location().toString(), 16, () -> new ItemStack(Items.OAK_LOG)),
                        exact(() -> CastPlateItem.create(ModItems.PLATE_CAST.get(),
                                CastPlateItem.CastPlateMaterial.COPPER, 1), 2),
                        Input.item(Items.BRICK, 16)),
                outputs(itemOutputStack(ModItems.FURNACE_COMBINATION_ITEM, 1)));

        for (String tier : List.of("stone", "iron")) addStampConversions(tier, 1);

        add("heater_firebox", 2, Overlay.CONSTRUCTION,
                List.of(Input.item(Items.FURNACE, 1), tag("c:plates/steel", 8, itemStack("plate_steel")),
                        tag("c:ingots/copper", 8, itemStack("ingot_copper"))),
                outputs(itemOutputStack(ModItems.HEATER_FIREBOX_ITEM, 1)));
        add("heater_oven", 2, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("ingot_firebrick").get(), 16),
                        tag("c:plates/steel", 4, itemStack("plate_steel")),
                        tag("c:ingots/copper", 8, itemStack("ingot_copper"))),
                outputs(itemOutputStack(ModItems.HEATER_OVEN_ITEM, 1)));
        add("heater_oilburner", 2, Overlay.CONSTRUCTION,
                List.of(item(ModItems.TANK_STEEL, 4),
                        subtype(ModItems.PIPE, 3, PipeItem::isSteel,
                                () -> PipeItem.steel(ModItems.PIPE.get(), 1)),
                        tag("c:ingots/titanium", 12, itemStack("ingot_titanium")),
                        tag("c:ingots/copper", 8, itemStack("ingot_copper"))),
                outputs(itemOutputStack(ModItems.HEATER_OILBURNER_ITEM, 1)));
        add("machine_ashpit", 2, Overlay.CONSTRUCTION,
                List.of(Input.item(Items.STONE, 8), tag("c:plates/steel", 2, itemStack("plate_steel")),
                        tag("c:ingots/iron", 4, () -> new ItemStack(Items.IRON_INGOT))),
                outputs(itemOutputStack(ModItems.MACHINE_ASHPIT_ITEM, 1)));
        add("heater_electric", 3, Overlay.CONSTRUCTION,
                List.of(tag("c:ingots/polymer", 4, itemStack("ingot_polymer")),
                        tag("c:ingots/copper", 8, itemStack("ingot_copper")),
                        tag("c:plates/steel", 8, itemStack("plate_steel")),
                        item(ModItems.COIL_TUNGSTEN, 8),
                        exact(() -> CircuitItem.create(ModItems.CIRCUIT.get(),
                                CircuitItem.CircuitType.BASIC, 1), 1)),
                outputs(itemOutputStack(ModItems.HEATER_ELECTRIC_ITEM, 1)));
        add("heater_heatex", 3, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("ingot_rubber").get(), 4),
                        tag("c:ingots/copper", 16, itemStack("ingot_copper")),
                        tag("c:plates/steel", 16, itemStack("plate_steel")),
                        subtype(ModItems.PIPE, 3, PipeItem::isSteel,
                                () -> PipeItem.steel(ModItems.PIPE.get(), 1))),
                outputs(itemOutputStack(ModItems.HEATER_HEATEX_ITEM, 1)));
        add("furnace_steel", 2, Overlay.CONSTRUCTION,
                List.of(Input.item(Items.STONE_BRICKS, 16),
                        tag("c:ingots/iron", 4, () -> new ItemStack(Items.IRON_INGOT)),
                        tag("c:plates/steel", 16, itemStack("plate_steel")),
                        tag("c:ingots/copper", 8, itemStack("ingot_copper")),
                        item(ModItems.STEEL_GRATE_ITEM, 16)),
                outputs(itemOutputStack(ModItems.FURNACE_STEEL_ITEM, 1)));
        add("machine_stirling", 2, Overlay.CONSTRUCTION,
                List.of(Input.tag(ItemTags.PLANKS.location().toString(), 16, () -> new ItemStack(Items.OAK_PLANKS)),
                        tag("c:plates/steel", 6, itemStack("plate_steel")),
                        tag("c:ingots/copper", 8, itemStack("ingot_copper")), item(ModItems.COIL_COPPER, 4),
                        exact(() -> new ItemStack(ModItems.GEAR_LARGE.get()), 1)),
                outputs(itemOutputStack(ModItems.MACHINE_STIRLING_ITEM, 1)));
        add("machine_sawmill", 2, Overlay.CONSTRUCTION,
                List.of(Input.tag(ItemTags.PLANKS.location().toString(), 16, () -> new ItemStack(Items.OAK_PLANKS)),
                        tag("c:plates/steel", 6, itemStack("plate_steel")),
                        tag("c:ingots/copper", 8, itemStack("ingot_copper")),
                        tag("c:ingots/iron", 4, () -> new ItemStack(Items.IRON_INGOT)),
                        item(ModItems.SAWBLADE, 1)),
                outputs(itemOutputStack(ModItems.MACHINE_SAWMILL_ITEM, 1)));
        add("machine_steam_engine", 2, Overlay.CONSTRUCTION,
                List.of(item(ModItems.REINFORCED_STONE_ITEM, 16),
                        tag("c:plates/steel", 12, itemStack("plate_steel")),
                        subtype(ModItems.SHELL, 2, ShellItem::isSteel,
                                () -> ShellItem.steel(ModItems.SHELL.get(), 1)),
                        item(ModItems.COIL_COPPER, 4),
                        exact(() -> new ItemStack(ModItems.GEAR_LARGE.get()), 1)),
                outputs(itemOutputStack(ModItems.MACHINE_STEAM_ENGINE_ITEM, 1)));
        add("machine_turbine", 3, Overlay.CONSTRUCTION,
                List.of(item(ModItems.TURBINE_TITANIUM, 1),
                        item(ModItems.COIL_COPPER, 2),
                        item(() -> ModItems.get("ingot_steel").get(), 4)),
                outputs(itemOutputStack(ModItems.MACHINE_TURBINE_ITEM, 1)));
        add("pump_steam", 2, Overlay.CONSTRUCTION,
                List.of(tag("c:cobblestones", 8, () -> new ItemStack(Items.COBBLESTONE)),
                        Input.tag(ItemTags.PLANKS.location().toString(), 16,
                                () -> new ItemStack(Items.OAK_PLANKS)),
                        tag("c:plates/copper", 8, itemStack("plate_copper")),
                        subtype(ModItems.PIPE, 2, PipeItem::isLead,
                                () -> PipeItem.lead(ModItems.PIPE.get(), 1))),
                outputs(itemOutputStack(ModItems.PUMP_STEAM_ITEM, 1)));
        add("pump_electric", 3, Overlay.CONSTRUCTION,
                List.of(Input.item(Items.STONE_BRICKS, 8),
                        tag("c:plates/steel", 16, itemStack("plate_steel")),
                        subtype(ModItems.PIPE, 4, PipeItem::isLead,
                                () -> PipeItem.lead(ModItems.PIPE.get(), 1)),
                        item(ModItems.MOTOR, 2),
                        exact(() -> CircuitItem.create(ModItems.CIRCUIT.get(),
                                CircuitItem.CircuitType.VACUUM_TUBE, 1), 4)),
                outputs(itemOutputStack(ModItems.PUMP_ELECTRIC_ITEM, 1)));

        for (String tier : List.of("steel", "titanium", "obsidian")) addStampConversions(tier, 2);
        add("machine_assembly_machine", 2, Overlay.CONSTRUCTION,
                List.of(tag("c:ingots/steel", 8, itemStack("ingot_steel")),
                        tag("c:plates/copper", 4, itemStack("plate_copper")),
                        item(ModItems.MOTOR, 2),
                        exact(() -> CircuitItem.create(ModItems.CIRCUIT.get(),
                                CircuitItem.CircuitType.VACUUM_TUBE, 1), 4)),
                outputs(itemOutputStack(ModItems.MACHINE_ASSEMBLY_MACHINE_ITEM, 1)));
        add("machine_crucible", 2, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("ingot_firebrick").get(), 20),
                        tag("c:ingots/copper", 8, itemStack("ingot_copper")),
                        tag("c:plates/steel", 8, itemStack("plate_steel"))),
                outputs(itemOutputStack(ModItems.MACHINE_CRUCIBLE_ITEM, 1)));
        add("machine_boiler", 2, Overlay.CONSTRUCTION,
                List.of(tag("c:ingots/steel", 4, itemStack("ingot_steel")),
                        tag("c:plates/copper", 16, itemStack("plate_copper")),
                        item(ModItems.PLATE_POLYMER, 8)),
                outputs(itemOutputStack(ModItems.MACHINE_BOILER_ITEM, 1)));
        add("machine_soldering_station", 2, Overlay.CONSTRUCTION,
                List.of(castSteelPlate(2),
                        item(ModItems.COIL_COPPER, 4),
                        subtype(ModItems.BOLT, 4,
                                stack -> BoltItem.material(stack) == BoltItem.BoltMaterial.TUNGSTEN,
                                () -> BoltItem.create(ModItems.BOLT.get(), BoltItem.BoltMaterial.TUNGSTEN, 1)),
                        subtype(ModItems.CIRCUIT, 2,
                                stack -> CircuitItem.type(stack) == CircuitItem.CircuitType.VACUUM_TUBE,
                                () -> CircuitItem.create(ModItems.CIRCUIT.get(), CircuitItem.CircuitType.VACUUM_TUBE, 1))),
                outputs(itemOutputStack(ModItems.MACHINE_SOLDERING_STATION_ITEM, 1)));
        add("machine_arc_welder", 2, Overlay.CONSTRUCTION,
                List.of(castSteelPlate(4),
                        tag("c:ingots/tungsten", 8, itemStack("ingot_tungsten")),
                        item(ModItems.MACHINE_TRANSFORMER_ITEM, 1),
                        subtype(ModItems.ARC_ELECTRODE, 2,
                                stack -> ArcElectrodeItem.type(stack) == ArcElectrodeItem.ElectrodeType.GRAPHITE,
                                () -> ArcElectrodeItem.create(ModItems.ARC_ELECTRODE.get(),
                                        ArcElectrodeItem.ElectrodeType.GRAPHITE, 1))),
                outputs(itemOutputStack(ModItems.MACHINE_ARC_WELDER_ITEM, 1)));

        // Tier-four reactor pancakes.
        add("plate_fuel_u233", 4, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("ingot_u233").get(), 1)),
                outputs(itemOutputStack(ModItems.PLATE_FUEL_U233, 1)));
        add("plate_fuel_u235", 4, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("ingot_u235").get(), 1)),
                outputs(itemOutputStack(ModItems.PLATE_FUEL_U235, 1)));
        add("plate_fuel_mox", 4, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("ingot_mox_fuel").get(), 1)),
                outputs(itemOutputStack(ModItems.PLATE_FUEL_MOX, 1)));
        add("plate_fuel_pu239", 4, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("ingot_pu239").get(), 1)),
                outputs(itemOutputStack(ModItems.PLATE_FUEL_PU239, 1)));
        add("plate_fuel_sa326", 4, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("ingot_schrabidium").get(), 1)),
                outputs(itemOutputStack(ModItems.PLATE_FUEL_SA326, 1)));
        add("plate_fuel_ra226be", 4, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("billet_ra226be").get(), 1)),
                outputs(itemOutputStack(ModItems.PLATE_FUEL_RA226BE, 1)));
        add("plate_fuel_pu238be", 4, Overlay.CONSTRUCTION,
                List.of(item(() -> ModItems.get("billet_pu238be").get(), 1)),
                outputs(itemOutputStack(ModItems.PLATE_FUEL_PU238BE, 1)));
        add("stamp_9", 2, Overlay.CONSTRUCTION,
                List.of(undamagedStamp("iron", "flat"), tag("c:ingots/gunmetal", 2, itemStack("ingot_gunmetal"))),
                outputs(itemOutputStack(ModItems.STAMPS.get("stamp_9"), 1)));
        add("stamp_50", 2, Overlay.CONSTRUCTION,
                List.of(undamagedStamp("iron", "flat"), tag("c:ingots/gunmetal", 2, itemStack("ingot_gunmetal"))),
                outputs(itemOutputStack(ModItems.STAMPS.get("stamp_50"), 1)));

        add("recycle_firebox", 2, Overlay.RECYCLING, List.of(item(ModItems.HEATER_FIREBOX_ITEM, 1)),
                outputs(itemOutput("plate_steel", 8), itemOutput("ingot_copper", 6)));
        add("recycle_heater_oven", 2, Overlay.RECYCLING, List.of(item(ModItems.HEATER_OVEN_ITEM, 1)),
                outputs(itemOutput("ingot_firebrick", 16), itemOutput("ingot_copper", 8)));
        add("recycle_stirling", 2, Overlay.RECYCLING,
                List.of(new Input(Ingredient.of(ModItems.MACHINE_STIRLING_ITEM.get()), 1,
                        stack -> !StirlingMachineBlockItem.isMissingCog(stack),
                        () -> new ItemStack(ModItems.MACHINE_STIRLING_ITEM.get()))),
                outputs(itemOutput("plate_steel", 6), itemOutput("ingot_copper", 8),
                        itemOutputStack(ModItems.COIL_COPPER, 4), itemOutputStack(ModItems.GEAR_LARGE, 1)));
        add("recycle_stirling_missing_cog", 2, Overlay.RECYCLING,
                List.of(Input.exact(() -> StirlingMachineBlockItem.withoutCog(ModItems.MACHINE_STIRLING_ITEM.get()), 1)),
                outputs(itemOutput("plate_steel", 6), itemOutput("ingot_copper", 8),
                        itemOutputStack(ModItems.COIL_COPPER, 4)));
        add("recycle_gear_large", 2, Overlay.RECYCLING,
                List.of(exact(() -> new ItemStack(ModItems.GEAR_LARGE.get()), 1)),
                outputs(itemOutput("plate_iron", 8), itemOutput("ingot_copper", 1)));
    }

    private AnvilRecipes() { }

    public static List<Smithing> smithing() { return List.copyOf(SMITHING); }
    public static List<Construction> construction() { return List.copyOf(CONSTRUCTION); }
    public static Construction byId(ResourceLocation id) { return BY_ID.get(id); }

    public static Smithing findSmithing(ItemStack left, ItemStack right, int tier) {
        for (Smithing recipe : SMITHING) {
            if (recipe.tier <= tier && recipe.left.matches(left) && left.getCount() >= recipe.left.count
                    && recipe.right.matches(right) && right.getCount() >= recipe.right.count) return recipe;
        }
        return null;
    }

    public static boolean craft(Player player, Construction recipe, boolean bulk) {
        if (recipe == null) return false;
        int attempts = bulk ? recipe.bulkAttempts() : 1;
        boolean crafted = false;
        for (int i = 0; i < attempts; i++) {
            int[] removals = planRemoval(player.getInventory(), recipe.inputs());
            if (removals == null) break;
            Inventory inventory = player.getInventory();
            for (int slot = 0; slot < removals.length; slot++) if (removals[slot] > 0) inventory.getItem(slot).shrink(removals[slot]);
            for (Output output : recipe.outputs()) if (player.getRandom().nextFloat() <= output.chance()) {
                ItemStack stack = output.stack().get().copy();
                if (!inventory.add(stack)) player.drop(stack, false);
            }
            crafted = true;
        }
        if (crafted) player.inventoryMenu.broadcastChanges();
        return crafted;
    }

    public static boolean canCraft(Inventory inventory, Construction recipe) {
        return planRemoval(inventory, recipe.inputs()) != null;
    }

    private static int[] planRemoval(Inventory inventory, List<Input> inputs) {
        int size = inventory.items.size();
        int[] available = new int[size];
        int[] removals = new int[size];
        for (int i = 0; i < size; i++) available[i] = inventory.getItem(i).getCount();
        for (Input input : inputs) {
            int needed = input.count();
            for (int slot = 0; slot < size && needed > 0; slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (available[slot] > 0 && input.matches(stack)) {
                    int take = Math.min(needed, available[slot]);
                    available[slot] -= take;
                    removals[slot] += take;
                    needed -= take;
                }
            }
            if (needed > 0) return null;
        }
        return removals;
    }

    private static void addStampConversions(String tier, int anvilTier) {
        for (String shape : List.of("plate", "wire", "circuit")) {
            add("stamp_" + tier + "_" + shape, anvilTier, Overlay.SMITHING,
                    List.of(undamagedStamp(tier, "flat")),
                    outputs(itemOutputStack(ModItems.STAMPS.get("stamp_" + tier + "_" + shape), 1)));
        }
    }

    private static Input undamagedStamp(String tier, String shape) {
        return Input.undamaged(ModItems.STAMPS.get("stamp_" + tier + "_" + shape), 1);
    }

    private static void smith(int tier, Input left, Input right, Supplier<ItemStack> output) {
        SMITHING.add(new Smithing(tier, left, right, output));
    }

    private static void moldSmith(FoundryMoldItem.Mold mold, Input demonstration) {
        SMITHING.add(new Smithing(1, demonstration, item(ModItems.MOLD_BASE, 1),
                () -> FoundryMoldItem.create(ModItems.MOLD.get(), mold), 0, 1));
    }

    private static void addMoldConstruction(String path, int tier, FoundryMoldItem.Mold mold, Input metal) {
        add(path, tier, Overlay.CONSTRUCTION, List.of(item(ModItems.MOLD_BASE, 1), metal),
                outputs(() -> FoundryMoldItem.create(ModItems.MOLD.get(), mold)));
    }

    private static Input exactCountTag(String id, int count, Supplier<ItemStack> display) {
        Input tagged = tag(id, count, display);
        return new Input(tagged.ingredient(), count, stack -> stack.getCount() == count, display);
    }

    private static Input exactCountItem(Supplier<? extends Item> item, int count) {
        return new Input(Ingredient.of(item.get()), count, stack -> stack.getCount() == count,
                () -> new ItemStack(item.get(), count));
    }

    private static Input exactCountSubtype(Supplier<? extends Item> item, int count,
                                            Predicate<ItemStack> predicate, Supplier<ItemStack> display) {
        return new Input(Ingredient.of(item.get()), count,
                stack -> stack.getCount() == count && predicate.test(stack), display);
    }

    private static Input exactCountAny(int count, Supplier<ItemStack> display, Item... items) {
        return new Input(Ingredient.of(items), count, stack -> stack.getCount() == count, display);
    }

    private static void add(String path, int tier, Overlay overlay, List<Input> inputs, List<Output> outputs) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "anvil/" + path);
        Construction recipe = new Construction(id, tier, -1, overlay, inputs, outputs);
        CONSTRUCTION.add(recipe);
        BY_ID.put(id, recipe);
    }

    private static Input item(Supplier<? extends Item> item, int count) { return Input.item(item, count); }
    private static Input tag(String id, int count, Supplier<ItemStack> display) { return Input.tag(id, count, display); }
    private static Input exact(Supplier<ItemStack> stack, int count) { return Input.exact(stack, count); }

    private static Input castSteelPlate(int count) {
        ItemStack steel = CastPlateItem.create(ModItems.PLATE_CAST.get(),
                CastPlateItem.CastPlateMaterial.STEEL, 1);
        Ingredient hbmSubtype = DataComponentIngredient.of(false, steel);
        Ingredient externalCompatibility = DifferenceIngredient.of(
                Ingredient.of(TagKey.create(Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath("c", "plates/cast/steel"))),
                Ingredient.of(ModItems.PLATE_CAST.get()));
        return new Input(CompoundIngredient.of(hbmSubtype, externalCompatibility), count,
                stack -> true, () -> steel.copy());
    }
    private static Input subtype(Supplier<? extends Item> item, int count, Predicate<ItemStack> predicate,
                                 Supplier<ItemStack> display) {
        return new Input(Ingredient.of(item.get()), count, predicate, display);
    }
    private static Supplier<ItemStack> itemStack(String id) { return () -> new ItemStack(ModItems.get(id).get()); }
    private static Supplier<ItemStack> itemOutput(String id, int count) { return () -> new ItemStack(ModItems.get(id).get(), count); }
    private static Supplier<ItemStack> itemOutputStack(Supplier<? extends Item> item, int count) { return () -> new ItemStack(item.get(), count); }
    @SafeVarargs private static List<Output> outputs(Supplier<ItemStack>... stacks) {
        List<Output> outputs = new ArrayList<>();
        for (Supplier<ItemStack> stack : stacks) outputs.add(new Output(stack, 1.0F));
        return outputs;
    }
}
