package com.hbm.ntm.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.content.HazardousMaterialDefinitions;
import com.hbm.ntm.content.MaterialDefinitions;
import com.hbm.ntm.item.BreedingRodItem;
import com.hbm.ntm.item.FoundryMoldItem;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Turns Java into an unreasonable quantity of JSON. */
public final class MaterialResourcesProvider implements DataProvider {
    private final PackOutput.PathProvider itemModels;
    private final PackOutput.PathProvider blockModels;
    private final PackOutput.PathProvider blockStates;
    private final PackOutput.PathProvider languages;
    private final PackOutput.PathProvider lootTables;
    private final PackOutput.PathProvider recipes;
    private final PackOutput.PathProvider itemTags;
    private final PackOutput.PathProvider blockTags;
    private final PackOutput.PathProvider damageTypes;
    private final PackOutput.PathProvider damageTypeTags;

    public MaterialResourcesProvider(PackOutput output) {
        itemModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
        blockModels = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/block");
        blockStates = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        languages = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang");
        lootTables = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_table/blocks");
        recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
        itemTags = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/item");
        blockTags = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/block");
        damageTypes = output.createPathProvider(PackOutput.Target.DATA_PACK, "damage_type");
        damageTypeTags = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/damage_type");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        JsonObject language = HbmEnglishTranslations.create();

        JsonArray aggregateIngots = new JsonArray();
        JsonArray aggregateBillets = new JsonArray();
        JsonArray aggregateNuggets = new JsonArray();
        JsonArray aggregateDusts = new JsonArray();
        JsonArray aggregatePlates = new JsonArray();
        JsonArray aggregateStorageBlockItems = new JsonArray();
        JsonArray aggregateStorageBlocks = new JsonArray();

        for (HazardousMaterialDefinitions.ItemDefinition definition : HazardousMaterialDefinitions.ITEMS) {
            ResourceLocation id = hbm(definition.id());
            language.addProperty("item.hbm." + definition.id(), definition.englishName());
            writes.add(save(output, generatedItemModel(definition.id()), itemModels, id));

            if (definition.form().tagFolder() != null && definition.commonMaterial() != null) {
                ResourceLocation materialTag = ResourceLocation.fromNamespaceAndPath(
                        "c",
                        definition.form().tagFolder() + "/" + definition.commonMaterial()
                );
                writes.add(save(output, tag(definition.id()), itemTags, materialTag));
                for (String alias : hazardousMaterialAliases(definition.commonMaterial())) {
                    ResourceLocation aliasTag = ResourceLocation.fromNamespaceAndPath(
                            "c", definition.form().tagFolder() + "/" + alias);
                    writes.add(save(output, tag(definition.id()), itemTags, aliasTag));
                }

                JsonArray aggregate = switch (definition.form()) {
                    case INGOT -> aggregateIngots;
                    case BILLET -> aggregateBillets;
                    case NUGGET -> aggregateNuggets;
                    default -> aggregateDusts;
                };
                aggregate.add("#" + materialTag);
            }
            if (definition.form() == HazardousMaterialDefinitions.Form.TINY_DUST
                    && definition.commonMaterial() != null) {
                writes.add(save(output, tag(definition.id()), itemTags,
                        hbm("dusts/tiny/" + definition.commonMaterial())));
            }
        }

        for (MaterialDefinitions.ItemDefinition definition : MaterialDefinitions.ITEMS) {
            ResourceLocation id = hbm(definition.id());
            language.addProperty("item.hbm." + definition.id(), definition.englishName());
            writes.add(save(output, generatedItemModel(definition.texture()), itemModels, id));

            if (definition.form().commonTagFolder() != null && definition.commonMaterial() != null) {
                ResourceLocation tag = ResourceLocation.fromNamespaceAndPath(
                        "c",
                        definition.form().commonTagFolder() + "/" + definition.commonMaterial()
                );
                if (definition.id().equals("nugget_cobalt")) {
                    // Both were nuggetCobalt in 1.7.10. The fragment is still the cave's cheap version.
                    JsonArray cobaltNuggets = new JsonArray();
                    cobaltNuggets.add("hbm:nugget_cobalt");
                    cobaltNuggets.add("hbm:fragment_cobalt");
                    writes.add(save(output, tag(cobaltNuggets), itemTags, tag));
                } else {
                    writes.add(save(output, tag(definition.id()), itemTags, tag));
                }

                JsonArray aggregate = switch (definition.form()) {
                    case INGOT -> aggregateIngots;
                    case BILLET -> aggregateBillets;
                    case NUGGET -> aggregateNuggets;
                    case PLATE -> aggregatePlates;
                    case DUST, MISC -> aggregateDusts;
                };
                aggregate.add("#" + tag);

                String legacyAlias = legacyMaterialAlias(definition.commonMaterial());
                if (legacyAlias != null) {
                    ResourceLocation legacyTag = ResourceLocation.fromNamespaceAndPath(
                            "c",
                            definition.form().commonTagFolder() + "/" + legacyAlias
                    );
                    writes.add(save(output, tag(definition.id()), itemTags, legacyTag));
                }
            }
        }

        writes.add(save(output, chunkOreModel(), itemModels, hbm("chunk_ore")));
        writes.add(save(output, generatedItemModel("chunk_ore.malachite"), itemModels,
                hbm("chunk_ore_malachite")));
        for (String coke : List.of("coal", "lignite", "petroleum")) {
            String item = "coke_" + coke;
            String block = "block_coke_" + coke;
            writes.add(save(output, generatedItemModel(item), itemModels, hbm(item)));
            writes.add(save(output, cubeAllBlockModel(block), blockModels, hbm(block)));
            writes.add(save(output, blockItemModel(block), itemModels, hbm(block)));
            writes.add(save(output, simpleBlockState(block), blockStates, hbm(block)));
            writes.add(save(output, selfDropLoot(block), lootTables, hbm(block)));
            writes.add(save(output, tag(item), itemTags,
                    ResourceLocation.fromNamespaceAndPath("c", "gems/coke/" + coke)));
            writes.add(save(output, tag(block), itemTags,
                    ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/coke/" + coke)));
            writes.add(save(output, tag(block), blockTags,
                    ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/coke/" + coke)));
            writes.add(save(output, compressionRecipe(item, block), recipes, hbm(block)));
            writes.add(save(output, decompressionRecipe(block, item), recipes, hbm(item + "_from_block")));
            aggregateStorageBlockItems.add("#c:storage_blocks/coke/" + coke);
            aggregateStorageBlocks.add("#c:storage_blocks/coke/" + coke);
        }
        JsonArray cokeItems = new JsonArray();
        cokeItems.add("#c:gems/coke/coal");
        cokeItems.add("#c:gems/coke/lignite");
        cokeItems.add("#c:gems/coke/petroleum");
        writes.add(save(output, tag(cokeItems), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "gems/coke")));
        JsonArray cokeBlocks = new JsonArray();
        cokeBlocks.add("#c:storage_blocks/coke/coal");
        cokeBlocks.add("#c:storage_blocks/coke/lignite");
        cokeBlocks.add("#c:storage_blocks/coke/petroleum");
        writes.add(save(output, tag(cokeBlocks), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/coke")));
        writes.add(save(output, tag(cokeBlocks), blockTags,
                ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/coke")));

        for (String stampId : stampIds()) {
            language.addProperty("item.hbm." + stampId, stampEnglishName(stampId));
            writes.add(save(output, generatedItemModel(stampTexture(stampId)), itemModels, hbm(stampId)));
        }
        for (String itemId : List.of("blades_steel", "blades_titanium", "blades_desh", "battery_creative",
                "fluid_barrel_infinite")) {
            String texture = itemId.equals("battery_creative") ? "battery_creative_new" : itemId;
            writes.add(save(output, generatedItemModel(texture), itemModels, hbm(itemId)));
        }

        writes.add(save(output, tag(aggregateIngots), itemTags, ResourceLocation.fromNamespaceAndPath("c", "ingots")));
        writes.add(save(output, tag(aggregateBillets), itemTags, hbm("billets")));
        writes.add(save(output, tag(aggregateNuggets), itemTags, ResourceLocation.fromNamespaceAndPath("c", "nuggets")));
        writes.add(save(output, tag(aggregateDusts), itemTags, ResourceLocation.fromNamespaceAndPath("c", "dusts")));
        writes.add(save(output, tag(aggregatePlates), itemTags, ResourceLocation.fromNamespaceAndPath("c", "plates")));
        writes.add(save(output, tag("powder_steel_tiny"), itemTags, hbm("dusts/tiny/steel")));
        writes.add(save(output, tag("sulfur"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "dusts/sulfur")));
        writes.add(save(output, tag("niter"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "dusts/saltpeter")));
        writes.add(save(output, tag("fluorite"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "dusts/fluorite")));
        writes.add(save(output, tag("gem_tantalium"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "gems/tantalum")));
        writes.add(save(output, tag("minecraft:coal", false), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "gems/coal")));
        writes.add(save(output, tag("lignite"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "gems/lignite")));
        writes.add(save(output, tag("cinnebar"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "gems/cinnabar")));
        writes.add(save(output, tag("minecraft:redstone", false), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "dusts/redstone")));
        // Fire powder is red phosphorus. The exothermic bomb gets upset otherwise.
        writes.add(save(output, tag("powder_fire"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "dusts/red_phosphorus")));
        writes.add(save(output, tag("ball_resin"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "gems/latex")));
        JsonArray anyRubber = new JsonArray();
        anyRubber.add("#c:ingots/latex");
        anyRubber.add("#c:ingots/rubber");
        writes.add(save(output, tag(anyRubber), itemTags, hbm("ingots/any_rubber")));
        for (String material : List.of("gold", "lead", "aluminium", "copper", "tungsten")) {
            writes.add(save(output, tag(new JsonArray()), itemTags,
                    ResourceLocation.fromNamespaceAndPath("c", "wires/fine/" + material)));
        }
        for (String material : List.of("steel", "tungsten")) {
            writes.add(save(output, tag(new JsonArray()), itemTags,
                    ResourceLocation.fromNamespaceAndPath("c", "bolts/" + material)));
        }
        for (String material : List.of("iron", "steel", "copper", "dura_steel",
                "technetium_steel", "cadmium_steel")) {
            writes.add(save(output, tag(new JsonArray()), itemTags,
                    ResourceLocation.fromNamespaceAndPath("c", "plates/cast/" + material)));
        }
        // Tagging the carrier would make every welded plate steel. Tag soup declined.
        for (String material : List.of("steel", "technetium_steel", "cadmium_steel")) {
            writes.add(save(output, tag(new JsonArray()), itemTags,
                    ResourceLocation.fromNamespaceAndPath("c", "plates/welded/" + material)));
        }
        for (String material : List.of("copper", "steel")) {
            writes.add(save(output, tag(new JsonArray()), itemTags,
                    ResourceLocation.fromNamespaceAndPath("c", "pipes/" + material)));
        }

        JsonArray mineableBlocks = new JsonArray();
        JsonArray shovelBlocks = new JsonArray();
        JsonArray axeBlocks = new JsonArray();
        JsonArray beaconBlocks = new JsonArray();

        for (HazardousMaterialDefinitions.BlockDefinition definition : HazardousMaterialDefinitions.BLOCKS) {
            ResourceLocation id = hbm(definition.id());
            language.addProperty("block.hbm." + definition.id(), definition.englishName());
            writes.add(save(output, cubeAllBlockModel(definition.id()), blockModels, id));
            writes.add(save(output, blockItemModel(definition.id()), itemModels, id));
            writes.add(save(output, simpleBlockState(definition.id()), blockStates, id));
            writes.add(save(output, selfDropLoot(definition.id()), lootTables, id));

            mineableBlocks.add("hbm:" + definition.id());
            beaconBlocks.add("hbm:" + definition.id());

            ResourceLocation storageTag = ResourceLocation.fromNamespaceAndPath(
                    "c",
                    "storage_blocks/" + definition.commonMaterial()
            );
            writes.add(save(output, tag(definition.id()), blockTags, storageTag));
            writes.add(save(output, tag(definition.id()), itemTags, storageTag));
            for (String alias : hazardousMaterialAliases(definition.commonMaterial())) {
                ResourceLocation aliasTag = ResourceLocation.fromNamespaceAndPath(
                        "c", "storage_blocks/" + alias);
                writes.add(save(output, tag(definition.id()), blockTags, aliasTag));
                writes.add(save(output, tag(definition.id()), itemTags, aliasTag));
            }
            aggregateStorageBlocks.add("#" + storageTag);
            aggregateStorageBlockItems.add("#" + storageTag);

            if (definition.compressedItemId() != null) {
                String material = definition.id().substring("block_".length());
                writes.add(save(output, compressionRecipe(definition.compressedItemId(), definition.id()), recipes, hbm(material + "_block")));
                writes.add(save(output, decompressionRecipe(definition.id(), definition.compressedItemId()), recipes,
                        hbm(definition.compressedItemId() + "_from_" + definition.id())));
            }
        }

        for (MaterialDefinitions.BlockDefinition definition : MaterialDefinitions.BLOCKS) {
            ResourceLocation id = hbm(definition.id());
            language.addProperty("block.hbm." + definition.id(), definition.englishName());

            writes.add(save(output, cubeAllBlockModel(definition.id()), blockModels, id));
            writes.add(save(output, blockItemModel(definition.id()), itemModels, id));
            writes.add(save(output, simpleBlockState(definition.id()), blockStates, id));
            writes.add(save(output, selfDropLoot(definition.id()), lootTables, id));

            mineableBlocks.add("hbm:" + definition.id());
            beaconBlocks.add("hbm:" + definition.id());

            ResourceLocation storageTag = ResourceLocation.fromNamespaceAndPath(
                    "c",
                    "storage_blocks/" + definition.commonMaterial()
            );
            writes.add(save(output, tag(definition.id()), blockTags, storageTag));
            writes.add(save(output, tag(definition.id()), itemTags, storageTag));
            for (String alias : hazardousMaterialAliases(definition.commonMaterial())) {
                ResourceLocation aliasTag = ResourceLocation.fromNamespaceAndPath(
                        "c", "storage_blocks/" + alias);
                writes.add(save(output, tag(definition.id()), blockTags, aliasTag));
                writes.add(save(output, tag(definition.id()), itemTags, aliasTag));
            }
            aggregateStorageBlocks.add("#" + storageTag);
            aggregateStorageBlockItems.add("#" + storageTag);

            String legacyAlias = legacyMaterialAlias(definition.commonMaterial());
            if (legacyAlias != null) {
                ResourceLocation legacyStorageTag = ResourceLocation.fromNamespaceAndPath(
                        "c",
                        "storage_blocks/" + legacyAlias
                );
                writes.add(save(output, tag(definition.id()), blockTags, legacyStorageTag));
                writes.add(save(output, tag(definition.id()), itemTags, legacyStorageTag));
            }

            if (definition.compressedItem() != null) {
                String material = definition.id().substring("block_".length());
                writes.add(save(
                        output,
                        compressionRecipe(definition.compressedItem(), definition.id()),
                        recipes,
                        hbm(material + "_block")
                ));
                writes.add(save(
                        output,
                        decompressionRecipe(definition.id(), definition.compressedItem()),
                        recipes,
                        hbm(definition.compressedItem() + "_from_" + definition.id())
                ));
            }
        }

        writes.add(save(output, cubeAllBlockModel("concrete_smooth", "concrete"),
                blockModels, hbm("concrete_smooth")));
        writes.add(save(output, blockItemModel("concrete_smooth"), itemModels,
                hbm("concrete_smooth")));
        writes.add(save(output, simpleBlockState("concrete_smooth"), blockStates,
                hbm("concrete_smooth")));
        writes.add(save(output, selfDropLoot("concrete_smooth"), lootTables,
                hbm("concrete_smooth")));
        writes.add(save(output, tag("concrete_smooth"), itemTags, hbm("any_concrete")));
        writes.add(save(output, tag("concrete_smooth"), blockTags, hbm("any_concrete")));
        writes.add(save(output, cementRecipe(), recipes, hbm("powder_cement")));
        mineableBlocks.add("hbm:concrete_smooth");

        for (String ore : List.of("ore_titanium", "ore_tungsten", "ore_cobalt", "ore_rare", "ore_coltan")) {
            ResourceLocation id = hbm(ore);
            writes.add(save(output, cubeAllBlockModel(ore), blockModels, id));
            writes.add(save(output, blockItemModel(ore), itemModels, id));
            writes.add(save(output, simpleBlockState(ore), blockStates, id));
            JsonObject loot = ore.equals("ore_cobalt") ? cobaltOreLoot()
                    : ore.equals("ore_rare") ? rareEarthOreLoot()
                    : ore.equals("ore_coltan") ? coltanOreLoot() : selfDropLoot(ore);
            writes.add(save(output, loot, lootTables, id));
            mineableBlocks.add("hbm:" + ore);
        }
        writes.add(save(output, cubeAllBlockModel("stone_resource_hematite", "stone_resource.hematite"),
                blockModels, hbm("stone_resource_hematite")));
        writes.add(save(output, cubeAllBlockModel("stone_resource_malachite", "stone_resource.malachite"),
                blockModels, hbm("stone_resource_malachite")));
        writes.add(save(output, stoneResourceBlockState(), blockStates, hbm("stone_resource")));
        writes.add(save(output, stoneResourceItemModel(), itemModels, hbm("stone_resource")));
        writes.add(save(output, parentItemModel("hbm:block/stone_resource_malachite"), itemModels,
                hbm("stone_resource_malachite")));
        mineableBlocks.add("hbm:stone_resource");
        writes.add(save(output, tag("ore_coltan"), blockTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores/coltan")));
        writes.add(save(output, tag("ore_coltan"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores/coltan")));
        writes.add(save(output, tag("ore_rare"), blockTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores/rare_earth")));
        writes.add(save(output, tag("ore_rare"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores/rare_earth")));
        for (String material : List.of("titanium", "tungsten", "cobalt")) {
            writes.add(save(output, tag("ore_" + material), blockTags,
                    ResourceLocation.fromNamespaceAndPath("c", "ores/" + material)));
            writes.add(save(output, tag("ore_" + material), itemTags,
                    ResourceLocation.fromNamespaceAndPath("c", "ores/" + material)));
        }
        writes.add(save(output, tag("ore_aluminium"), blockTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores/aluminum")));
        writes.add(save(output, tag("ore_aluminium"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores/aluminum")));
        writes.add(save(output, tag("ore_gneiss_lithium"), blockTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores/lithium")));
        writes.add(save(output, tag("ore_gneiss_lithium"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores/lithium")));
        JsonArray hbmOres = new JsonArray();
        for (String material : List.of("aluminum", "titanium", "tungsten", "cobalt", "rare_earth", "coltan", "lithium")) {
            hbmOres.add("#c:ores/" + material);
        }
        writes.add(save(output, tag(hbmOres), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores")));
        writes.add(save(output, tag(hbmOres), blockTags,
                ResourceLocation.fromNamespaceAndPath("c", "ores")));
        for (String block : List.of("ore_oil", "ore_oil_empty", "dirt_oily", "dirt_dead",
                "sand_dirty", "sand_dirty_red", "stone_cracked")) {
            writes.add(save(output, cubeAllBlockModel(block), blockModels, hbm(block)));
            writes.add(save(output, blockItemModel(block), itemModels, hbm(block)));
            writes.add(save(output, simpleBlockState(block), blockStates, hbm(block)));
            writes.add(save(output, block.equals("ore_oil") ? oilDepositLoot() : selfDropLoot(block),
                    lootTables, hbm(block)));
        }

        for (String block : List.of("gneiss_tile", "gneiss_brick", "reinforced_light",
                "reinforced_sand", "depth_brick", "depth_tiles", "depth_nether_brick",
                "depth_nether_tiles")) {
            writes.add(save(output, cubeAllBlockModel(block), blockModels, hbm(block)));
        }
        writes.add(save(output, cutoutCubeAllBlockModel("reinforced_glass"), blockModels,
                hbm("reinforced_glass")));
        for (String part : List.of("post", "side", "side_alt", "noside", "noside_alt")) {
            writes.add(save(output, reinforcedGlassPaneModel(part), blockModels,
                    hbm("reinforced_glass_pane_" + part)));
        }
        writes.add(save(output, legacyStandardBlockItemModel("reinforced_glass"), itemModels,
                hbm("reinforced_glass")));
        writes.add(save(output, generatedItemModel("reinforced_glass_pane", true), itemModels,
                hbm("reinforced_glass_pane")));
        writes.add(save(output, simpleBlockState("reinforced_glass"), blockStates,
                hbm("reinforced_glass")));
        writes.add(save(output, reinforcedGlassPaneBlockState(), blockStates,
                hbm("reinforced_glass_pane")));
        writes.add(save(output, silkOnlyLoot("reinforced_glass"), lootTables,
                hbm("reinforced_glass")));
        writes.add(save(output, silkOnlyLoot("reinforced_glass_pane"), lootTables,
                hbm("reinforced_glass_pane")));
        writes.add(save(output, tag("reinforced_glass"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "glass_blocks")));
        writes.add(save(output, tag("reinforced_glass_pane"), itemTags,
                ResourceLocation.fromNamespaceAndPath("c", "glass_panes")));
        mineableBlocks.add("hbm:reinforced_glass");
        mineableBlocks.add("hbm:reinforced_glass_pane");
        writes.add(save(output, cubeColumnBlockModel("gneiss_chiseled", "gneiss_chiseled",
                "gneiss_tile"), blockModels, hbm("gneiss_chiseled")));
        for (String block : List.of("gneiss_tile", "gneiss_brick", "gneiss_chiseled",
                "reinforced_light", "reinforced_sand", "depth_brick", "depth_tiles",
                "depth_nether_brick", "depth_nether_tiles")) {
            writes.add(save(output, legacyStandardBlockItemModel(block), itemModels, hbm(block)));
            writes.add(save(output, simpleBlockState(block), blockStates, hbm(block)));
            writes.add(save(output, selfDropLoot(block), lootTables, hbm(block)));
            mineableBlocks.add("hbm:" + block);
        }
        writes.add(save(output, shapedRecipe(List.of("##", "##"), "stone_gneiss",
                "gneiss_tile", 4), recipes, hbm("gneiss_tile")));
        writes.add(save(output, shapedRecipe(List.of("##", "##"), "gneiss_tile",
                "gneiss_brick", 4), recipes, hbm("gneiss_brick")));
        writes.add(save(output, shapelessRecipe(List.of("gneiss_tile"), "gneiss_chiseled", 1),
                recipes, hbm("gneiss_chiseled")));
        writes.add(save(output, shapedRecipe(List.of("##", "##"), "stone_depth",
                "depth_brick", 4), recipes, hbm("depth_brick")));
        writes.add(save(output, shapedRecipe(List.of("##", "##"), "depth_brick",
                "depth_tiles", 4), recipes, hbm("depth_tiles")));
        writes.add(save(output, shapedRecipe(List.of("##", "##"), "stone_depth_nether",
                "depth_nether_brick", 4), recipes, hbm("depth_nether_brick")));
        writes.add(save(output, shapedRecipe(List.of("##", "##"), "depth_nether_brick",
                "depth_nether_tiles", 4), recipes, hbm("depth_nether_tiles")));
        writes.add(save(output, foundryShaped(List.of("FFF", "FBF", "FFF"), Map.of(
                "F", itemIngredient("minecraft:iron_bars"),
                "B", itemIngredient("minecraft:glowstone")),
                "hbm:reinforced_light", 1), recipes, hbm("reinforced_light")));
        writes.add(save(output, foundryShaped(List.of("FBF", "BFB", "FBF"), Map.of(
                "F", itemIngredient("minecraft:iron_bars"),
                "B", itemIngredient("minecraft:sandstone")),
                "hbm:reinforced_sand", 4), recipes, hbm("reinforced_sand")));

        writes.add(save(output, cubeAllBlockModel("oil_pipe"), blockModels, hbm("oil_pipe")));
        writes.add(save(output, simpleBlockState("oil_pipe"), blockStates, hbm("oil_pipe")));
        writes.add(save(output, emptyLoot(), lootTables, hbm("oil_pipe")));
        for (int variant = 0; variant < 5; variant++) {
            String texture = switch (variant) {
                case 0 -> "plant_dead_generic";
                case 1 -> "plant_dead_grass";
                case 2 -> "plant_dead_flower";
                case 3 -> "plant_dead_bigflower";
                default -> "plant_dead_fern";
            };
            writes.add(save(output, crossBlockModel(texture), blockModels, hbm("plant_dead_" + variant)));
        }
        writes.add(save(output, plantDeadBlockState(), blockStates, hbm("plant_dead")));
        writes.add(save(output, parentItemModel("hbm:block/plant_dead_0"), itemModels, hbm("plant_dead")));
        writes.add(save(output, emptyLoot(), lootTables, hbm("plant_dead")));
        writes.add(save(output, boxBlockModel("oil_spill", 0, 0, 0, 16, 2, 16), blockModels, hbm("oil_spill")));
        writes.add(save(output, legacyBlockItemModel("oil_spill", List.of(30, 315, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels, hbm("oil_spill")));
        writes.add(save(output, simpleBlockState("oil_spill"), blockStates, hbm("oil_spill")));
        writes.add(save(output, emptyLoot(), lootTables, hbm("oil_spill")));
        writes.add(save(output, generatedItemModel("trinitite_new"), itemModels, hbm("trinitite")));
        writes.add(save(output, generatedItemModel("burnt_bark"), itemModels, hbm("burnt_bark")));

        writes.add(save(output, cubeBottomTopBlockModel("waste_earth", "waste_grass_side",
                "waste_earth_bottom", "waste_grass_top"), blockModels, hbm("waste_earth")));
        writes.add(save(output, cubeBottomTopBlockModel("waste_mycelium", "waste_mycelium_side",
                "waste_earth_bottom", "waste_mycelium_top"), blockModels, hbm("waste_mycelium")));
        writes.add(save(output, cubeColumnBlockModel("waste_log", "waste_log_side", "waste_log_top"),
                blockModels, hbm("waste_log")));
        for (String block : List.of("waste_trinitite", "waste_trinitite_red", "waste_planks",
                "block_trinitite", "block_waste")) {
            writes.add(save(output, cubeAllBlockModel(block), blockModels, hbm(block)));
        }
        for (String block : List.of("waste_earth", "waste_mycelium", "waste_trinitite",
                "waste_trinitite_red", "waste_log", "waste_planks", "block_trinitite", "block_waste")) {
            writes.add(save(output, blockItemModel(block), itemModels, hbm(block)));
            writes.add(save(output, simpleBlockState(block), blockStates, hbm(block)));
        }
        writes.add(save(output, silkElseItemLoot("waste_earth", "minecraft:dirt"),
                lootTables, hbm("waste_earth")));
        writes.add(save(output, silkElseItemLoot("waste_mycelium", "minecraft:dirt"),
                lootTables, hbm("waste_mycelium")));
        writes.add(save(output, silkElseItemLoot("waste_trinitite", "hbm:trinitite"),
                lootTables, hbm("waste_trinitite")));
        writes.add(save(output, silkElseItemLoot("waste_trinitite_red", "hbm:trinitite"),
                lootTables, hbm("waste_trinitite_red")));
        writes.add(save(output, wasteLogLoot(), lootTables, hbm("waste_log")));
        writes.add(save(output, silkElseItemLoot("waste_planks", "minecraft:charcoal"),
                lootTables, hbm("waste_planks")));
        writes.add(save(output, selfDropLoot("block_trinitite"), lootTables, hbm("block_trinitite")));
        writes.add(save(output, selfDropLoot("block_waste"), lootTables, hbm("block_waste")));

        // Frozen blocks drop snowballs unless Silk Touch catches them lying.
        writes.add(save(output, cubeAllBlockModel("frozen_dirt"), blockModels, hbm("frozen_dirt")));
        writes.add(save(output, cubeAllBlockModel("frozen_planks"), blockModels, hbm("frozen_planks")));
        writes.add(save(output, cubeBottomTopBlockModel("frozen_grass", "frozen_grass_side",
                "frozen_dirt", "frozen_grass_top"), blockModels, hbm("frozen_grass")));
        writes.add(save(output, cubeColumnBlockModel("frozen_log", "frozen_log", "frozen_log_top"),
                blockModels, hbm("frozen_log")));
        for (String block : List.of("frozen_dirt", "frozen_grass", "frozen_log", "frozen_planks")) {
            writes.add(save(output, blockItemModel(block), itemModels, hbm(block)));
            writes.add(save(output, simpleBlockState(block), blockStates, hbm(block)));
        }
        writes.add(save(output, silkElseItemLoot("frozen_dirt", "minecraft:snowball"),
                lootTables, hbm("frozen_dirt")));
        writes.add(save(output, silkElseItemLoot("frozen_grass", "minecraft:snowball"),
                lootTables, hbm("frozen_grass")));
        writes.add(save(output, silkElseItemLoot("frozen_planks", "minecraft:snowball"),
                lootTables, hbm("frozen_planks")));
        writes.add(save(output, frozenLogLoot(), lootTables, hbm("frozen_log")));
        shovelBlocks.add("hbm:frozen_dirt");
        shovelBlocks.add("hbm:frozen_grass");
        axeBlocks.add("hbm:frozen_log");
        axeBlocks.add("hbm:frozen_planks");

        for (int level = 0; level < 6; level++) {
            for (int variant = 0; variant < 4; variant++) {
                String model = "sellafield_" + level + "_" + variant;
                writes.add(save(output, cubeAllBlockModel(model, model), blockModels, hbm(model)));
            }
            writes.add(save(output, parentItemModel("hbm:block/sellafield_" + level + "_0"),
                    itemModels, hbm("sellafield_" + level)));
        }
        for (int variant = 0; variant < 4; variant++) {
            String model = "sellafield_slaked_" + variant;
            String texture = variant == 0 ? "sellafield_slaked" : "sellafield_slaked_" + variant;
            writes.add(save(output, cubeAllBlockModel(model, texture), blockModels, hbm(model)));
        }
        writes.add(save(output, sellafieldBlockState(), blockStates, hbm("sellafield")));
        writes.add(save(output, sellafieldSlakedBlockState(), blockStates, hbm("sellafield_slaked")));
        writes.add(save(output, sellafieldItemModel(), itemModels, hbm("sellafield")));
        writes.add(save(output, parentItemModel("hbm:block/sellafield_slaked_0"), itemModels,
                hbm("sellafield_slaked")));
        writes.add(save(output, sellafieldLoot(), lootTables, hbm("sellafield")));
        writes.add(save(output, selfDropLoot("sellafield_slaked"), lootTables, hbm("sellafield_slaked")));

        shovelBlocks.add("hbm:waste_earth");
        shovelBlocks.add("hbm:waste_mycelium");
        shovelBlocks.add("hbm:waste_trinitite");
        shovelBlocks.add("hbm:waste_trinitite_red");
        axeBlocks.add("hbm:waste_log");
        axeBlocks.add("hbm:waste_planks");
        mineableBlocks.add("hbm:block_trinitite");
        mineableBlocks.add("hbm:block_waste");
        mineableBlocks.add("hbm:sellafield");
        mineableBlocks.add("hbm:sellafield_slaked");
        beaconBlocks.add("hbm:block_trinitite");
        beaconBlocks.add("hbm:block_waste");

        JsonArray caveSoils = new JsonArray();
        caveSoils.add("hbm:waste_earth");
        caveSoils.add("hbm:waste_mycelium");
        writes.add(save(output, tag(caveSoils), blockTags, minecraft("mushroom_grow_block")));
        writes.add(save(output, cubeAllBlockModel("gas_coal"), blockModels, hbm("gas_coal")));
        writes.add(save(output, blockItemModel("gas_coal"), itemModels, hbm("gas_coal")));
        writes.add(save(output, simpleBlockState("gas_coal"), blockStates, hbm("gas_coal")));
        writes.add(save(output, emptyLoot(), lootTables, hbm("gas_coal")));
        writes.add(save(output, cubeAllBlockModel("gas_asbestos"), blockModels, hbm("gas_asbestos")));
        writes.add(save(output, blockItemModel("gas_asbestos"), itemModels, hbm("gas_asbestos")));
        writes.add(save(output, simpleBlockState("gas_asbestos"), blockStates, hbm("gas_asbestos")));
        writes.add(save(output, emptyLoot(), lootTables, hbm("gas_asbestos")));
        writes.add(save(output, cubeAllBlockModel("gas_monoxide"), blockModels, hbm("gas_monoxide")));
        writes.add(save(output, blockItemModel("gas_monoxide"), itemModels, hbm("gas_monoxide")));
        writes.add(save(output, simpleBlockState("gas_monoxide"), blockStates, hbm("gas_monoxide")));
        writes.add(save(output, emptyLoot(), lootTables, hbm("gas_monoxide")));
        writes.add(save(output, generatedItemModel("coal_infernal"), itemModels, hbm("coal_infernal")));
        writes.add(save(output, cubeAllBlockModel("gas_radon"), blockModels, hbm("gas_radon")));
        writes.add(save(output, blockItemModel("gas_radon"), itemModels, hbm("gas_radon")));
        writes.add(save(output, simpleBlockState("gas_radon"), blockStates, hbm("gas_radon")));
        writes.add(save(output, emptyLoot(), lootTables, hbm("gas_radon")));
        writes.add(save(output, generatedItemModel("pellet_gas"), itemModels, hbm("pellet_gas")));
        writes.add(save(output, chlorinePelletRecipe(), recipes, hbm("pellet_gas")));
        writes.add(save(output, cubeBottomTopBlockModel("vent_chlorine",
                "vent_chlorine", "vent_blank", "vent_blank"), blockModels, hbm("vent_chlorine")));
        writes.add(save(output, legacyStandardBlockItemModel("vent_chlorine"), itemModels,
                hbm("vent_chlorine")));
        writes.add(save(output, simpleBlockState("vent_chlorine"), blockStates, hbm("vent_chlorine")));
        writes.add(save(output, emptyLoot(), lootTables, hbm("vent_chlorine")));
        mineableBlocks.add("hbm:vent_chlorine");
        writes.add(save(output, foundryShaped(List.of("IGI", "ICI", "IDI"), Map.of(
                "I", tagIngredient("c:plates/iron"),
                "G", itemIngredient("minecraft:iron_bars"),
                "C", itemIngredient("hbm:pellet_gas"),
                "D", itemIngredient("minecraft:dispenser")),
                "hbm:vent_chlorine", 1), recipes, hbm("vent_chlorine")));
        writes.add(save(output, translucentCubeAllBlockModel("chlorine_gas"), blockModels,
                hbm("chlorine_gas")));
        writes.add(save(output, legacyStandardBlockItemModel("chlorine_gas"), itemModels,
                hbm("chlorine_gas")));
        writes.add(save(output, simpleBlockState("chlorine_gas"), blockStates, hbm("chlorine_gas")));
        writes.add(save(output, emptyLoot(), lootTables, hbm("chlorine_gas")));
        writes.add(save(output, cubeBottomTopBlockModel("vent_chlorine_seal",
                "vent_chlorine_seal_side", "vent_chlorine_seal_side", "vent_chlorine_seal_top"),
                blockModels, hbm("vent_chlorine_seal")));
        writes.add(save(output, legacyStandardBlockItemModel("vent_chlorine_seal"), itemModels,
                hbm("vent_chlorine_seal")));
        writes.add(save(output, simpleBlockState("vent_chlorine_seal"), blockStates,
                hbm("vent_chlorine_seal")));
        writes.add(save(output, selfDropLoot("vent_chlorine_seal"), lootTables,
                hbm("vent_chlorine_seal")));
        writes.add(save(output, emptyModel("derrick"), blockModels, hbm("machine_well")));
        writes.add(save(output, directionalEmptyBlockState("machine_well"), blockStates, hbm("machine_well")));
        writes.add(save(output, derrickItemModel(), itemModels, hbm("machine_well")));
        writes.add(save(output, selfDropLoot("machine_well"), lootTables, hbm("machine_well")));
        writes.add(save(output, emptyModel("dieselgen"), blockModels, hbm("machine_diesel")));
        writes.add(save(output, directionalEmptyBlockState("machine_diesel"), blockStates,
                hbm("machine_diesel")));
        writes.add(save(output, parentItemModel("hbm:block/dieselgen_item"), itemModels,
                hbm("machine_diesel")));
        writes.add(save(output, selfDropLoot("machine_diesel"), lootTables, hbm("machine_diesel")));
        writes.add(save(output, selfDropLoot("machine_fluidtank"), lootTables, hbm("machine_fluidtank")));
        for (String barrel : List.of("barrel_plastic", "barrel_corroded", "barrel_steel",
                "barrel_tcalloy", "barrel_antimatter")) {
            writes.add(save(output, selfDropLoot(barrel), lootTables, hbm(barrel)));
        }
        writes.add(save(output, barrelRecipe(itemIngredient("hbm:plate_polymer"),
                        itemIngredient("hbm:plate_aluminium"), "hbm:barrel_plastic"),
                recipes, hbm("barrel_plastic")));
        writes.add(save(output, barrelRecipe(tagIngredient("c:plates/steel"),
                        tagIngredient("c:ingots/steel"), "hbm:barrel_steel"),
                recipes, hbm("barrel_steel")));
        writes.add(save(output, barrelRecipe(itemIngredient("hbm:ingot_tcalloy"),
                        itemIngredient("hbm:plate_titanium"), "hbm:barrel_tcalloy"),
                recipes, hbm("barrel_tcalloy")));
        writes.add(save(output, barrelRecipe(itemIngredient("hbm:plate_saturnite"),
                        itemIngredient("hbm:coil_gold_torus"), "hbm:barrel_antimatter"),
                recipes, hbm("barrel_antimatter")));
        writes.add(save(output, emptyModel("electric_heater"), blockModels, hbm("heater_electric")));
        writes.add(save(output, unconditionalMultipartState("heater_electric"), blockStates,
                hbm("heater_electric")));
        writes.add(save(output, parentItemModel("hbm:block/electric_heater_item_obj"), itemModels,
                hbm("heater_electric")));
        writes.add(save(output, selfDropLoot("heater_electric"), lootTables, hbm("heater_electric")));
        writes.add(save(output, emptyModel("heating_oven"), blockModels, hbm("heater_oven")));
        writes.add(save(output, unconditionalMultipartState("heater_oven"), blockStates, hbm("heater_oven")));
        writes.add(save(output, parentItemModel("hbm:block/heating_oven_item"), itemModels, hbm("heater_oven")));
        writes.add(save(output, selfDropLoot("heater_oven"), lootTables, hbm("heater_oven")));
        writes.add(save(output, emptyModel("fluid_burner"), blockModels, hbm("heater_oilburner")));
        writes.add(save(output, unconditionalMultipartState("heater_oilburner"), blockStates,
                hbm("heater_oilburner")));
        writes.add(save(output, parentItemModel("hbm:block/fluid_burner_item"), itemModels,
                hbm("heater_oilburner")));
        writes.add(save(output, selfDropLoot("heater_oilburner"), lootTables,
                hbm("heater_oilburner")));
        writes.add(save(output, emptyModel("heater_heatex"), blockModels, hbm("heater_heatex")));
        writes.add(save(output, unconditionalMultipartState("heater_heatex"), blockStates,
                hbm("heater_heatex")));
        writes.add(save(output, parentItemModel("hbm:block/heatex_item"), itemModels,
                hbm("heater_heatex")));
        writes.add(save(output, selfDropLoot("heater_heatex"), lootTables,
                hbm("heater_heatex")));
        writes.add(save(output, emptyModel("blast_furnace"), blockModels, hbm("machine_blast_furnace")));
        writes.add(save(output, unconditionalMultipartState("machine_blast_furnace"), blockStates,
                hbm("machine_blast_furnace")));
        writes.add(save(output, parentItemModel("hbm:block/blast_furnace_item"), itemModels,
                hbm("machine_blast_furnace")));
        writes.add(save(output, selfDropLoot("machine_blast_furnace"), lootTables,
                hbm("machine_blast_furnace")));
        writes.add(save(output, emptyModel("combination_oven"), blockModels, hbm("furnace_combination")));
        writes.add(save(output, unconditionalMultipartState("furnace_combination"), blockStates,
                hbm("furnace_combination")));
        writes.add(save(output, parentItemModel("hbm:block/combination_oven_item"), itemModels,
                hbm("furnace_combination")));
        writes.add(save(output, selfDropLoot("furnace_combination"), lootTables,
                hbm("furnace_combination")));
        writes.add(save(output, emptyModel("furnace_steel"), blockModels, hbm("furnace_steel")));
        writes.add(save(output, unconditionalMultipartState("furnace_steel"), blockStates,
                hbm("furnace_steel")));
        writes.add(save(output, parentItemModel("hbm:block/furnace_steel_item"), itemModels,
                hbm("furnace_steel")));
        writes.add(save(output, selfDropLoot("furnace_steel"), lootTables,
                hbm("furnace_steel")));
        writes.add(save(output, electricFurnaceBlockModel(false), blockModels,
                hbm("machine_electric_furnace_off")));
        writes.add(save(output, electricFurnaceBlockModel(true), blockModels,
                hbm("machine_electric_furnace_on")));
        writes.add(save(output, electricFurnaceBlockState(), blockStates,
                hbm("machine_electric_furnace_off")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_electric_furnace_off"), itemModels,
                hbm("machine_electric_furnace_off")));
        writes.add(save(output, selfDropLoot("machine_electric_furnace_off"), lootTables,
                hbm("machine_electric_furnace_off")));
        writes.add(save(output, electricFurnaceRecipe(), recipes,
                hbm("machine_electric_furnace_off")));
        writes.add(save(output, brickFurnaceBlockModel(false), blockModels,
                hbm("machine_furnace_brick_off")));
        writes.add(save(output, brickFurnaceBlockModel(true), blockModels,
                hbm("machine_furnace_brick_on")));
        writes.add(save(output, brickFurnaceBlockState(), blockStates,
                hbm("machine_furnace_brick_off")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_furnace_brick_off"), itemModels,
                hbm("machine_furnace_brick_off")));
        writes.add(save(output, selfDropLoot("machine_furnace_brick_off"), lootTables,
                hbm("machine_furnace_brick_off")));
        writes.add(save(output, brickFurnaceRecipe(), recipes,
                hbm("machine_furnace_brick_off")));
        writes.add(save(output, emptyModel("wood_burner"), blockModels, hbm("machine_wood_burner")));
        writes.add(save(output, unconditionalMultipartState("machine_wood_burner"), blockStates,
                hbm("machine_wood_burner")));
        writes.add(save(output, parentItemModel("hbm:block/wood_burner_item"), itemModels,
                hbm("machine_wood_burner")));
        writes.add(save(output, selfDropLoot("machine_wood_burner"), lootTables,
                hbm("machine_wood_burner")));
        writes.add(save(output, woodBurnerRecipe(), recipes, hbm("machine_wood_burner")));
        writes.add(save(output, emptyModel("microwave"), blockModels, hbm("machine_microwave")));
        writes.add(save(output, unconditionalMultipartState("machine_microwave"), blockStates,
                hbm("machine_microwave")));
        writes.add(save(output, parentItemModel("hbm:block/microwave_item"), itemModels,
                hbm("machine_microwave")));
        writes.add(save(output, selfDropLoot("machine_microwave"), lootTables,
                hbm("machine_microwave")));
        writes.add(save(output, microwaveRecipe(), recipes, hbm("machine_microwave")));
        writes.add(save(output, cubeAllBlockModel("machine_converter_he_fe"), blockModels, hbm("machine_converter_he_fe")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_converter_he_fe"), itemModels, hbm("machine_converter_he_fe")));
        writes.add(save(output, simpleBlockState("machine_converter_he_fe"), blockStates, hbm("machine_converter_he_fe")));
        writes.add(save(output, selfDropLoot("machine_converter_he_fe"), lootTables, hbm("machine_converter_he_fe")));
        writes.add(save(output, converterHEFErecipe(), recipes, hbm("machine_converter_he_fe")));
        writes.add(save(output, cubeAllBlockModel("machine_converter_fe_he"), blockModels, hbm("machine_converter_fe_he")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_converter_fe_he"), itemModels, hbm("machine_converter_fe_he")));
        writes.add(save(output, simpleBlockState("machine_converter_fe_he"), blockStates, hbm("machine_converter_fe_he")));
        writes.add(save(output, selfDropLoot("machine_converter_fe_he"), lootTables, hbm("machine_converter_fe_he")));
        writes.add(save(output, converterFEHErecipe(), recipes, hbm("machine_converter_fe_he")));
        writes.add(save(output, generatedItemModel("magnetron_alt"), itemModels, hbm("magnetron")));
        writes.add(save(output, generatedItemModel("oil_tar"), itemModels, hbm("oil_tar")));
        writes.add(save(output, generatedItemModel("powder_sawdust"), itemModels, hbm("powder_sawdust")));
        writes.add(save(output, sawbladeRecipe(), recipes, hbm("sawblade")));
        writes.add(save(output, generatedItemModel("canister_empty"), itemModels, hbm("canister_empty")));
        writes.add(save(output, canisterFullModel(), itemModels, hbm("canister_full")));
        writes.add(save(output, generatedItemModel("gas_empty"), itemModels, hbm("gas_empty")));
        writes.add(save(output, gasFullModel(), itemModels, hbm("gas_full")));
        writes.add(save(output, generatedItemModel("cell_empty"), itemModels, hbm("cell_empty")));
        writes.add(save(output, generatedItemModel("cell_tritium"), itemModels, hbm("cell_tritium")));
        writes.add(save(output, generatedItemModel("cell_sas3"), itemModels, hbm("cell_sas3")));
        writes.add(save(output, sourceContainerRecipe(true), recipes, hbm("canister_empty")));
        writes.add(save(output, sourceContainerRecipe(false), recipes, hbm("gas_empty")));
        writes.add(save(output, smeltingRecipe("hbm:ore_titanium", "hbm:ingot_titanium", 3.0F),
                recipes, hbm("ingot_titanium_from_ore")));
        writes.add(save(output, smeltingRecipe("hbm:ore_tungsten", "hbm:ingot_tungsten", 6.0F),
                recipes, hbm("ingot_tungsten_from_ore")));
        writes.add(save(output, smeltingRecipe("hbm:ore_cobalt", "hbm:ingot_cobalt", 2.0F),
                recipes, hbm("ingot_cobalt_from_ore")));
        writes.add(save(output, smeltingRecipe("hbm:powder_titanium", "hbm:ingot_titanium", 1.0F),
                recipes, hbm("ingot_titanium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_tungsten", "hbm:ingot_tungsten", 1.0F),
                recipes, hbm("ingot_tungsten_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_cobalt", "hbm:ingot_cobalt", 1.0F),
                recipes, hbm("ingot_cobalt_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_co60", "hbm:ingot_co60", 1.0F),
                recipes, hbm("ingot_co60_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_neptunium", "hbm:ingot_neptunium", 1.0F),
                recipes, hbm("ingot_neptunium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_ra226", "hbm:ingot_ra226", 1.0F),
                recipes, hbm("ingot_ra226_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_niobium", "hbm:ingot_niobium", 1.0F),
                recipes, hbm("ingot_niobium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_tantalium", "hbm:ingot_tantalium", 1.0F),
                recipes, hbm("ingot_tantalium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_aluminium", "hbm:ingot_aluminium", 1.0F),
                recipes, hbm("ingot_aluminium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_beryllium", "hbm:ingot_beryllium", 1.0F),
                recipes, hbm("ingot_beryllium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_copper", "hbm:ingot_copper", 1.0F),
                recipes, hbm("ingot_copper_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_gold", "minecraft:gold_ingot", 1.0F),
                recipes, hbm("gold_ingot_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_iron", "minecraft:iron_ingot", 1.0F),
                recipes, hbm("iron_ingot_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_uranium", "hbm:ingot_uranium", 1.0F),
                recipes, hbm("ingot_uranium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_thorium", "hbm:ingot_th232", 1.0F),
                recipes, hbm("ingot_th232_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_plutonium", "hbm:ingot_plutonium", 1.0F),
                recipes, hbm("ingot_plutonium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_lithium", "hbm:lithium", 1.0F),
                recipes, hbm("lithium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_red_copper", "hbm:ingot_red_copper", 1.0F),
                recipes, hbm("ingot_red_copper_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_steel", "hbm:ingot_steel", 1.0F),
                recipes, hbm("ingot_steel_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_asbestos", "hbm:ingot_asbestos", 1.0F),
                recipes, hbm("ingot_asbestos_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_bismuth", "hbm:ingot_bismuth", 1.0F),
                recipes, hbm("ingot_bismuth_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_calcium", "hbm:ingot_calcium", 1.0F),
                recipes, hbm("ingot_calcium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_cadmium", "hbm:ingot_cadmium", 1.0F),
                recipes, hbm("ingot_cadmium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_schrabidium", "hbm:ingot_schrabidium", 5.0F),
                recipes, hbm("ingot_schrabidium_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_tcalloy", "hbm:ingot_tcalloy", 1.0F),
                recipes, hbm("ingot_tcalloy_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_magnetized_tungsten",
                        "hbm:ingot_magnetized_tungsten", 1.0F),
                recipes, hbm("ingot_magnetized_tungsten_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_combine_steel", "hbm:ingot_combine_steel", 1.0F),
                recipes, hbm("ingot_combine_steel_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_polymer", "hbm:ingot_polymer", 1.0F),
                recipes, hbm("ingot_polymer_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_desh", "hbm:ingot_desh", 1.0F),
                recipes, hbm("ingot_desh_from_powder")));
        writes.add(save(output, smeltingRecipe("hbm:powder_dura_steel", "hbm:ingot_dura_steel", 1.0F),
                recipes, hbm("ingot_dura_steel_from_powder")));
        writes.add(save(output, powderAlloyScrapsRecipe(List.of("c:dusts/tungsten", "c:nuggets/schrabidium"),
                        "magnetized_tungsten", 38, 72),
                recipes, hbm("scraps_magnetized_tungsten")));
        writes.add(save(output, powderAlloyScrapsRecipe(List.of("c:dusts/steel", "c:nuggets/technetium_99"),
                        "technetium_steel", 36, 72),
                recipes, hbm("scraps_technetium_steel")));
        writes.add(save(output, powderAlloyScrapsRecipe(List.of("c:dusts/copper", "c:dusts/redstone"),
                        "red_copper", 31, 144),
                recipes, hbm("scraps_red_copper")));
        writes.add(save(output, powderAlloyScrapsRecipe(List.of("c:dusts/iron", "c:dusts/coal"),
                        "steel", 30, 72),
                recipes, hbm("scraps_steel")));
        writes.add(save(output, powderAlloyScrapsRecipe(List.of(
                        "c:dusts/iron", "c:dusts/iron", "c:dusts/iron", "c:dusts/iron",
                        "c:dusts/coal", "c:dusts/coal", "c:dusts/coal", "c:dusts/coal"),
                        "steel", 30, 288),
                recipes, hbm("scraps_steel_bulk")));
        writes.add(save(output, coalPowderFluxRecipe(), recipes, hbm("powder_flux_from_coal_powder")));
        writes.add(save(output, calciumPowderFluxRecipe(), recipes, hbm("powder_flux_from_calcium_powder")));
        writes.add(save(output, fluoriteFluxRecipe(), recipes, hbm("powder_flux_from_fluorite")));
        writes.add(save(output, gunpowderRecipe(false), recipes, hbm("gunpowder_from_coal")));
        writes.add(save(output, gunpowderRecipe(true), recipes, hbm("gunpowder_from_charcoal")));
        writes.add(save(output, clayUncraftingRecipe(), recipes, hbm("clay_ball_from_clay")));
        writes.add(save(output, smeltingRecipe("minecraft:gravel", "minecraft:cobblestone", 0.0F),
                recipes, hbm("cobblestone_from_gravel")));
        writes.add(save(output, smeltingRecipe("hbm:gravel_obsidian", "minecraft:obsidian", 0.0F),
                recipes, hbm("obsidian_from_gravel_obsidian")));
        writes.add(save(output, smeltingRecipe("hbm:gravel_diamond", "minecraft:diamond", 3.0F),
                recipes, hbm("diamond_from_gravel_diamond")));
        writes.add(save(output, compressionRecipe("powder_cobalt_tiny", "powder_cobalt"),
                recipes, hbm("powder_cobalt")));
        writes.add(save(output, decompressionRecipe("powder_cobalt", "powder_cobalt_tiny"),
                recipes, hbm("powder_cobalt_tiny_from_powder")));
        addCobaltBilletRecipes(writes, output);
        writes.add(save(output, compressionRecipe("powder_lithium_tiny", "powder_lithium"),
                recipes, hbm("powder_lithium")));
        writes.add(save(output, decompressionRecipe("powder_lithium", "powder_lithium_tiny"),
                recipes, hbm("powder_lithium_tiny_from_powder")));
        writes.add(save(output, compressionRecipe("nuclear_waste", "block_waste"),
                recipes, hbm("waste_block")));
        writes.add(save(output, decompressionRecipe("block_waste", "nuclear_waste"),
                recipes, hbm("nuclear_waste_from_block_waste")));
        writes.add(save(output, compressionRecipe("nugget_zirconium", "ingot_zirconium"),
                recipes, hbm("ingot_zirconium_from_nugget")));
        writes.add(save(output, decompressionRecipe("ingot_zirconium", "nugget_zirconium"),
                recipes, hbm("nugget_zirconium_from_ingot")));
        writes.add(save(output, compressionRecipe("nugget_tantalium", "ingot_tantalium"),
                recipes, hbm("ingot_tantalium_from_nugget")));
        writes.add(save(output, decompressionRecipe("ingot_tantalium", "nugget_tantalium"),
                recipes, hbm("nugget_tantalium_from_ingot")));
        writes.add(save(output, compressionRecipe("nugget_lead", "ingot_lead"),
                recipes, hbm("ingot_lead_from_nugget_lead")));
        writes.add(save(output, decompressionRecipe("ingot_lead", "nugget_lead"),
                recipes, hbm("nugget_lead_from_ingot_lead")));

        for (HazardousMaterialDefinitions.ItemDefinition ingot : HazardousMaterialDefinitions.ITEMS) {
            if (ingot.form() != HazardousMaterialDefinitions.Form.INGOT) {
                continue;
            }
            HazardousMaterialDefinitions.ItemDefinition billet = findHazardForm(
                    ingot.commonMaterial(), HazardousMaterialDefinitions.Form.BILLET);
            HazardousMaterialDefinitions.ItemDefinition nugget = findHazardForm(
                    ingot.commonMaterial(), HazardousMaterialDefinitions.Form.NUGGET);

            if (billet == null || nugget == null) {
                continue;
            }

            writes.add(save(output, compressionRecipe(nugget.id(), ingot.id()), recipes,
                    hbm(ingot.id() + "_from_" + nugget.id())));
            writes.add(save(output, decompressionRecipe(ingot.id(), nugget.id()), recipes,
                    hbm(nugget.id() + "_from_" + ingot.id())));
            writes.add(save(output, shapedRecipe(List.of("###", "###"), nugget.id(), billet.id(), 1), recipes,
                    hbm(billet.id() + "_from_" + nugget.id())));
            writes.add(save(output, shapelessRecipe(List.of(billet.id()), nugget.id(), 6), recipes,
                    hbm(nugget.id() + "_from_" + billet.id())));
            writes.add(save(output, shapelessRecipe(List.of(billet.id(), billet.id(), billet.id()), ingot.id(), 2), recipes,
                    hbm(ingot.id() + "_from_" + billet.id())));
            writes.add(save(output, shapedRecipe(List.of("##"), ingot.id(), billet.id(), 3), recipes,
                    hbm(billet.id() + "_from_" + ingot.id())));
        }

        ResourceLocation aggregateStorageTag = ResourceLocation.fromNamespaceAndPath("c", "storage_blocks");
        writes.add(save(output, tag(aggregateStorageBlocks), blockTags, aggregateStorageTag));
        writes.add(save(output, tag(aggregateStorageBlockItems), itemTags, aggregateStorageTag));
        mineableBlocks.add("hbm:red_cable");
        mineableBlocks.add("hbm:machine_battery_socket");
        mineableBlocks.add("hbm:machine_battery_redd");
        mineableBlocks.add("hbm:machine_press");
        mineableBlocks.add("hbm:machine_ammo_press");
        mineableBlocks.add("hbm:turret_chekhov");
        mineableBlocks.add("hbm:turret_friendly");
        mineableBlocks.add("hbm:turret_jeremy");
        mineableBlocks.add("hbm:turret_tauon");
        mineableBlocks.add("hbm:turret_richard");
        mineableBlocks.add("hbm:turret_howard");
        mineableBlocks.add("hbm:turret_fritz");
        mineableBlocks.add("hbm:turret_maxwell");
        mineableBlocks.add("hbm:turret_arty");
        mineableBlocks.add("hbm:turret_himars");
        mineableBlocks.add("hbm:turret_sentry");
        mineableBlocks.add("hbm:press_preheater");
        mineableBlocks.add("hbm:machine_shredder");
        mineableBlocks.add("hbm:machine_assembly_machine");
        mineableBlocks.add("hbm:machine_chemical_plant");
        mineableBlocks.add("hbm:machine_soldering_station");
        mineableBlocks.add("hbm:machine_transformer");
        mineableBlocks.add("hbm:machine_arc_welder");
        mineableBlocks.add("hbm:machine_arc_furnace");
        mineableBlocks.add("hbm:machine_refinery");
        mineableBlocks.add("hbm:machine_centrifuge");
        mineableBlocks.add("hbm:machine_catalytic_cracker");
        mineableBlocks.add("hbm:machine_fraction_tower");
        mineableBlocks.add("hbm:machine_converter_he_fe");
        mineableBlocks.add("hbm:machine_converter_fe_he");
        mineableBlocks.add("hbm:fraction_spacer");
        mineableBlocks.add("hbm:machine_crucible");
        mineableBlocks.add("hbm:foundry_mold");
        mineableBlocks.add("hbm:foundry_basin");
        mineableBlocks.add("hbm:foundry_channel");
        mineableBlocks.add("hbm:foundry_tank");
        mineableBlocks.add("hbm:foundry_outlet");
        mineableBlocks.add("hbm:foundry_slagtap");
        mineableBlocks.add("hbm:gravel_obsidian");
        mineableBlocks.add("hbm:heater_firebox");
        mineableBlocks.add("hbm:heater_oven");
        mineableBlocks.add("hbm:machine_ashpit");
        mineableBlocks.add("hbm:machine_stirling");
        mineableBlocks.add("hbm:machine_sawmill");
        mineableBlocks.add("hbm:machine_steam_engine");
        mineableBlocks.add("hbm:machine_industrial_turbine");
        mineableBlocks.add("hbm:machine_turbinegas");
        mineableBlocks.add("hbm:machine_combustion_engine");
        mineableBlocks.add("hbm:machine_turbofan");
        mineableBlocks.add("hbm:machine_turbine");
        mineableBlocks.add("hbm:machine_siren");
        mineableBlocks.add("hbm:pump_steam");
        mineableBlocks.add("hbm:pump_electric");
        mineableBlocks.add("hbm:machine_intake");
        mineableBlocks.add("hbm:machine_condenser");
        mineableBlocks.add("hbm:machine_condenser_powered");
        mineableBlocks.add("hbm:machine_boiler");
        mineableBlocks.add("hbm:heater_electric");
        mineableBlocks.add("hbm:heater_oilburner");
        mineableBlocks.add("hbm:heater_heatex");
        mineableBlocks.add("hbm:machine_blast_furnace");
        mineableBlocks.add("hbm:furnace_combination");
        mineableBlocks.add("hbm:block_coke_coal");
        mineableBlocks.add("hbm:block_coke_lignite");
        mineableBlocks.add("hbm:block_coke_petroleum");
        mineableBlocks.add("hbm:furnace_steel");
        mineableBlocks.add("hbm:machine_electric_furnace_off");
        mineableBlocks.add("hbm:machine_furnace_brick_off");
        mineableBlocks.add("hbm:machine_wood_burner");
        mineableBlocks.add("hbm:machine_microwave");
        mineableBlocks.add("hbm:geiger");
        mineableBlocks.add("hbm:machine_armor_table");
        mineableBlocks.add("hbm:machine_weapon_table");
        mineableBlocks.add("hbm:anvil_iron");
        mineableBlocks.add("hbm:anvil_lead");
        mineableBlocks.add("hbm:anvil_steel");
        mineableBlocks.add("hbm:anvil_desh");
        mineableBlocks.add("hbm:anvil_ferrouranium");
        mineableBlocks.add("hbm:anvil_saturnite");
        mineableBlocks.add("hbm:anvil_bismuth_bronze");
        mineableBlocks.add("hbm:anvil_arsenic_bronze");
        mineableBlocks.add("hbm:anvil_schrabidate");
        mineableBlocks.add("hbm:anvil_dnt");
        mineableBlocks.add("hbm:anvil_osmiridium");
        mineableBlocks.add("hbm:anvil_murky");
        mineableBlocks.add("hbm:ore_oil");
        mineableBlocks.add("hbm:ore_oil_empty");
        mineableBlocks.add("hbm:stone_cracked");
        mineableBlocks.add("hbm:vent_chlorine_seal");
        mineableBlocks.add("hbm:oil_pipe");
        mineableBlocks.add("hbm:machine_well");
        mineableBlocks.add("hbm:machine_diesel");
        mineableBlocks.add("hbm:machine_fluidtank");
        mineableBlocks.add("hbm:barrel_plastic");
        mineableBlocks.add("hbm:barrel_corroded");
        mineableBlocks.add("hbm:barrel_steel");
        mineableBlocks.add("hbm:barrel_tcalloy");
        mineableBlocks.add("hbm:barrel_antimatter");
        mineableBlocks.add("hbm:steel_scaffold");
        mineableBlocks.add("hbm:steel_beam");
        mineableBlocks.add("hbm:steel_grate");
        mineableBlocks.add("hbm:conveyor");
        mineableBlocks.add("hbm:conveyor_express");
        mineableBlocks.add("hbm:conveyor_double");
        mineableBlocks.add("hbm:conveyor_triple");
        mineableBlocks.add("hbm:conveyor_lift");
        mineableBlocks.add("hbm:conveyor_chute");
        mineableBlocks.add("hbm:crane_extractor");
        mineableBlocks.add("hbm:crane_inserter");
        mineableBlocks.add("hbm:crane_boxer");
        shovelBlocks.add("hbm:dirt_oily");
        shovelBlocks.add("hbm:dirt_dead");
        shovelBlocks.add("hbm:sand_dirty");
        shovelBlocks.add("hbm:sand_dirty_red");
        writes.add(save(output, tag(mineableBlocks), blockTags, minecraft("mineable/pickaxe")));
        writes.add(save(output, tag(shovelBlocks), blockTags, minecraft("mineable/shovel")));
        writes.add(save(output, tag(axeBlocks), blockTags, minecraft("mineable/axe")));
        writes.add(save(output, tag(beaconBlocks), blockTags, minecraft("beacon_base_blocks")));

        writes.add(save(
                output,
                compressionRecipe("powder_steel_tiny", "powder_steel"),
                recipes,
                hbm("powder_steel_from_tiny")
        ));
        writes.add(save(
                output,
                decompressionRecipe("powder_steel", "powder_steel_tiny"),
                recipes,
                hbm("powder_steel_tiny_from_powder")
        ));

        writes.add(save(output, cubeAllBlockModel("machine_press"), blockModels, hbm("machine_press")));
        writes.add(save(output, generatedItemModel("machine_press", true), itemModels, hbm("machine_press")));
        writes.add(save(output, pressBlockState(), blockStates, hbm("machine_press")));
        writes.add(save(output, selfDropLoot("machine_press"), lootTables, hbm("machine_press")));
        writes.add(save(output, cubeAllBlockModel("press_preheater"), blockModels, hbm("press_preheater")));
        writes.add(save(output, legacyStandardBlockItemModel("press_preheater"), itemModels,
                hbm("press_preheater")));
        writes.add(save(output, simpleBlockState("press_preheater"), blockStates, hbm("press_preheater")));
        writes.add(save(output, selfDropLoot("press_preheater"), lootTables, hbm("press_preheater")));
        writes.add(save(output, machinePressRecipe(), recipes, hbm("machine_press")));
        writes.add(save(output, emptyModel("ammo_press"), blockModels, hbm("machine_ammo_press")));
        writes.add(save(output, unconditionalMultipartState("machine_ammo_press"), blockStates, hbm("machine_ammo_press")));
        writes.add(save(output, selfDropLoot("machine_ammo_press"), lootTables, hbm("machine_ammo_press")));
        writes.add(save(output, ammoPressRecipe(), recipes, hbm("machine_ammo_press")));
        for (String turret : List.of("turret_chekhov", "turret_friendly", "turret_jeremy", "turret_tauon",
                "turret_richard", "turret_howard", "turret_fritz", "turret_maxwell",
                "turret_arty", "turret_himars", "turret_sentry")) {
            writes.add(save(output, emptyModel("block_steel"), blockModels, hbm(turret)));
            writes.add(save(output, unconditionalMultipartState(turret), blockStates, hbm(turret)));
            writes.add(save(output, selfDropLoot(turret), lootTables, hbm(turret)));
        }
        writes.add(save(output, generatedItemModel("ammo_dgk"), itemModels, hbm("ammo_dgk")));
        writes.add(save(output, artilleryAmmoItemModel(), itemModels, hbm("ammo_arty")));
        writes.add(save(output, himarsAmmoItemModel(), itemModels, hbm("ammo_himars")));
        for (com.hbm.ntm.weapon.ArtilleryAmmoType ammo
                : com.hbm.ntm.weapon.ArtilleryAmmoType.values()) {
            if (ammo.ordinal() == 0) continue;
            writes.add(save(output, generatedItemModel(ammo.serializedName()), itemModels,
                    hbm(ammo.serializedName())));
        }
        for (com.hbm.ntm.weapon.HimarsAmmoType ammo : com.hbm.ntm.weapon.HimarsAmmoType.values()) {
            writes.add(save(output, generatedItemModel(ammo.serializedName()), itemModels,
                    hbm(ammo.serializedName())));
        }
        writes.add(save(output, dgkAmmoRecipe(), recipes, hbm("ammo_dgk")));
        writes.add(save(output, ammoShellItemModel(), itemModels, hbm("ammo_shell")));
        for (com.hbm.ntm.weapon.TurretShellAmmoType shell
                : com.hbm.ntm.weapon.TurretShellAmmoType.values()) {
            if (shell.ordinal() == 0) continue;
            writes.add(save(output, generatedItemModel(shell.serializedName()), itemModels,
                    hbm(shell.serializedName())));
        }
        writes.add(save(output, generatedItemModel("turret_chip"), itemModels, hbm("turret_chip")));
        writes.add(save(output, pressPreheaterRecipe(), recipes, hbm("press_preheater")));
        addFlatStampRecipes(writes, output);

        writes.add(save(output, emptyModel("battery_socket"), blockModels, hbm("machine_battery_socket")));
        writes.add(save(output, unconditionalMultipartState("machine_battery_socket"), blockStates,
                hbm("machine_battery_socket")));
        writes.add(save(output, legacyBuiltinEntityItemModel(), itemModels, hbm("machine_battery_socket")));
        writes.add(save(output, legacyBuiltinEntityItemModel(), itemModels, hbm("battery_pack")));
        writes.add(save(output, selfDropLoot("machine_battery_socket"), lootTables,
                hbm("machine_battery_socket")));
        writes.add(save(output, emptyModel("block_steel"), blockModels, hbm("machine_battery_redd")));
        writes.add(save(output, unconditionalMultipartState("machine_battery_redd"), blockStates,
                hbm("machine_battery_redd")));
        writes.add(save(output, legacyBuiltinEntityItemModel(), itemModels, hbm("machine_battery_redd")));
        writes.add(save(output, selfDropLoot("machine_battery_redd"), lootTables,
                hbm("machine_battery_redd")));
        writes.add(save(output, earlyBatterySocketRecipe(), recipes,
                hbm("machine_battery_socket_from_red_copper")));
        writes.add(save(output, batteryPackRecipe("battery_redstone", "hbm:plate_iron", "minecraft:redstone_block"),
                recipes, hbm("battery_redstone")));
        writes.add(save(output, batteryPackRecipe("capacitor_copper", "hbm:plate_steel", "hbm:block_copper"),
                recipes, hbm("capacitor_copper")));

        writes.add(save(output, shredderBlockModel(), blockModels, hbm("machine_shredder")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_shredder"), itemModels,
                hbm("machine_shredder")));
        writes.add(save(output, simpleBlockState("machine_shredder"), blockStates, hbm("machine_shredder")));
        writes.add(save(output, selfDropLoot("machine_shredder"), lootTables, hbm("machine_shredder")));
        writes.add(save(output, emptyModel("industrial_turbine"), blockModels,
                hbm("machine_industrial_turbine")));
        writes.add(save(output, unconditionalMultipartState("machine_industrial_turbine"), blockStates,
                hbm("machine_industrial_turbine")));
        writes.add(save(output, industrialTurbineItemModel(), itemModels,
                hbm("machine_industrial_turbine")));
        writes.add(save(output, selfDropLoot("machine_industrial_turbine"), lootTables,
                hbm("machine_industrial_turbine")));
        writes.add(save(output, emptyModel("turbinegas"), blockModels,
                hbm("machine_turbinegas")));
        writes.add(save(output, unconditionalMultipartState("machine_turbinegas"), blockStates,
                hbm("machine_turbinegas")));
        writes.add(save(output, gasTurbineItemModel(), itemModels,
                hbm("machine_turbinegas")));
        writes.add(save(output, selfDropLoot("machine_turbinegas"), lootTables,
                hbm("machine_turbinegas")));
        writes.add(save(output, emptyModel("block_steel"), blockModels,
                hbm("machine_combustion_engine")));
        writes.add(save(output, unconditionalMultipartState("machine_combustion_engine"), blockStates,
                hbm("machine_combustion_engine")));
        writes.add(save(output, legacyBuiltinEntityItemModel(), itemModels,
                hbm("machine_combustion_engine")));
        writes.add(save(output, selfDropLoot("machine_combustion_engine"), lootTables,
                hbm("machine_combustion_engine")));
        writes.add(save(output, cubeBottomTopBlockModel("machine_siren", "machine_siren",
                "block_steel", "block_steel"), blockModels, hbm("machine_siren")));
        writes.add(save(output, simpleBlockState("machine_siren"), blockStates, hbm("machine_siren")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_siren"), itemModels,
                hbm("machine_siren")));
        writes.add(save(output, selfDropLoot("machine_siren"), lootTables, hbm("machine_siren")));
        writes.add(save(output, machineSirenRecipe(), recipes, hbm("machine_siren")));
        writes.add(save(output, sirenTrackItemModel(), itemModels, hbm("siren_track")));
        writes.add(save(output, emptyModel("block_steel"), blockModels,
                hbm("machine_turbofan")));
        writes.add(save(output, unconditionalMultipartState("machine_turbofan"), blockStates,
                hbm("machine_turbofan")));
        writes.add(save(output, turbofanItemModel(), itemModels,
                hbm("machine_turbofan")));
        writes.add(save(output, selfDropLoot("machine_turbofan"), lootTables,
                hbm("machine_turbofan")));
        writes.add(save(output, cubeBottomTopBlockModel("machine_turbine", "machine_turbine_base",
                "machine_turbine_top", "machine_turbine_top"), blockModels, hbm("machine_turbine")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_turbine"), itemModels,
                hbm("machine_turbine")));
        writes.add(save(output, simpleBlockState("machine_turbine"), blockStates, hbm("machine_turbine")));
        writes.add(save(output, selfDropLoot("machine_turbine"), lootTables, hbm("machine_turbine")));
        writes.add(save(output, selfDropLoot("machine_assembly_machine"), lootTables,
                hbm("machine_assembly_machine")));
        writes.add(save(output, selfDropLoot("machine_chemical_plant"), lootTables,
                hbm("machine_chemical_plant")));
        writes.add(save(output, selfDropLoot("machine_soldering_station"), lootTables,
                hbm("machine_soldering_station")));
        writes.add(save(output, transformerBlockModel(), blockModels, hbm("machine_transformer")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_transformer"), itemModels,
                hbm("machine_transformer")));
        writes.add(save(output, simpleBlockState("machine_transformer"), blockStates, hbm("machine_transformer")));
        writes.add(save(output, selfDropLoot("machine_transformer"), lootTables, hbm("machine_transformer")));
        writes.add(save(output, transformerRecipe(), recipes, hbm("machine_transformer")));
        writes.add(save(output, selfDropLoot("machine_arc_welder"), lootTables, hbm("machine_arc_welder")));
        writes.add(save(output, emptyModel("arc_furnace"), blockModels, hbm("machine_arc_furnace")));
        writes.add(save(output, unconditionalMultipartState("machine_arc_furnace"), blockStates,
                hbm("machine_arc_furnace")));
        writes.add(save(output, parentItemModel("hbm:block/arc_furnace_item"), itemModels,
                hbm("machine_arc_furnace")));
        writes.add(save(output, selfDropLoot("machine_arc_furnace"), lootTables,
                hbm("machine_arc_furnace")));
        writes.add(save(output, selfDropLoot("machine_refinery"), lootTables, hbm("machine_refinery")));
        writes.add(save(output, selfDropLoot("machine_centrifuge"), lootTables, hbm("machine_centrifuge")));
        writes.add(save(output, selfDropLoot("machine_catalytic_cracker"), lootTables,
                hbm("machine_catalytic_cracker")));
        writes.add(save(output, selfDropLoot("machine_fraction_tower"), lootTables,
                hbm("machine_fraction_tower")));
        writes.add(save(output, selfDropLoot("fraction_spacer"), lootTables, hbm("fraction_spacer")));
        writes.add(save(output, selfDropLoot("steel_beam"), lootTables, hbm("steel_beam")));
        writes.add(save(output, selfDropLoot("steel_grate"), lootTables, hbm("steel_grate")));
        writes.add(save(output, selfDropLoot("machine_crucible"), lootTables, hbm("machine_crucible")));
        writes.add(save(output, cubeAllBlockModel("machine_condenser", "condenser"), blockModels,
                hbm("machine_condenser")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_condenser"), itemModels,
                hbm("machine_condenser")));
        writes.add(save(output, simpleBlockState("machine_condenser"), blockStates, hbm("machine_condenser")));
        writes.add(save(output, selfDropLoot("machine_condenser"), lootTables, hbm("machine_condenser")));
        writes.add(save(output, steamCondenserRecipe(), recipes, hbm("machine_condenser")));
        writes.add(save(output, emptyModel("condenser_powered"), blockModels,
                hbm("machine_condenser_powered")));
        writes.add(save(output, unconditionalMultipartState("machine_condenser_powered"), blockStates,
                hbm("machine_condenser_powered")));
        writes.add(save(output, highPowerCondenserItemModel(), itemModels,
                hbm("machine_condenser_powered")));
        writes.add(save(output, selfDropLoot("machine_condenser_powered"), lootTables,
                hbm("machine_condenser_powered")));
        writes.add(save(output, selfDropLoot("foundry_mold"), lootTables, hbm("foundry_mold")));
        writes.add(save(output, simpleBlockState("foundry_basin"), blockStates, hbm("foundry_basin")));
        writes.add(save(output, selfDropLoot("foundry_basin"), lootTables, hbm("foundry_basin")));
        writes.add(save(output, selfDropLoot("foundry_channel"), lootTables, hbm("foundry_channel")));
        writes.add(save(output, selfDropLoot("foundry_tank"), lootTables, hbm("foundry_tank")));
        writes.add(save(output, selfDropLoot("foundry_outlet"), lootTables, hbm("foundry_outlet")));
        writes.add(save(output, selfDropLoot("foundry_slagtap"), lootTables, hbm("foundry_slagtap")));
        writes.add(save(output, crucibleItemModel(), itemModels, hbm("machine_crucible")));
        writes.add(save(output, legacyBlockItemModel("foundry_mold", List.of(30, 315, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels, hbm("foundry_mold")));
        writes.add(save(output, legacyBlockItemModel("foundry_basin", List.of(30, 315, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels, hbm("foundry_basin")));
        writes.add(save(output, legacyBlockItemModel("foundry_channel_item", List.of(30, 315, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels, hbm("foundry_channel")));
        writes.add(save(output, legacyBlockItemModel("foundry_tank", List.of(30, 315, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels, hbm("foundry_tank")));
        // This template faces backward because of course it does.
        writes.add(save(output, legacyBlockItemModel("foundry_outlet", List.of(30, 135, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels, hbm("foundry_outlet")));
        writes.add(save(output, legacyBlockItemModel("foundry_slagtap", List.of(30, 135, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels, hbm("foundry_slagtap")));
        writes.add(save(output, generatedItemModel("blueprints"), itemModels, hbm("blueprints")));
        writes.add(save(output, generatedItemModel("upgrade_muffler"), itemModels, hbm("upgrade_muffler")));
        writes.add(save(output, foundryShaped(List.of("III", "IWI", "III"), Map.of(
                "I", tagIngredient("hbm:ingots/any_rubber"),
                "W", tagIngredient("minecraft:wool")), "hbm:upgrade_muffler", 16), recipes,
                hbm("upgrade_muffler")));
        writes.add(save(output, generatedItemModel("centrifuge_element"), itemModels,
                hbm("centrifuge_element")));
        for (String type : List.of("speed", "power", "effect", "afterburn", "overdrive", "ejector", "stack")) for (int level = 1; level <= 3; level++) {
            String id = "upgrade_" + type + "_" + level;
            writes.add(save(output, generatedItemModel(id), itemModels, hbm(id)));
        }
        writes.add(save(output, generatedItemModel("upgrade_5g"), itemModels, hbm("upgrade_5g")));
        writes.add(save(output, generatedItemModel("upgrade_screm"), itemModels, hbm("upgrade_screm")));
        for (String gravel : List.of("gravel_obsidian", "gravel_diamond")) {
            writes.add(save(output, cubeAllBlockModel(gravel), blockModels, hbm(gravel)));
            writes.add(save(output, blockItemModel(gravel), itemModels, hbm(gravel)));
            writes.add(save(output, simpleBlockState(gravel), blockStates, hbm(gravel)));
            writes.add(save(output, selfDropLoot(gravel), lootTables, hbm(gravel)));
        }
        addShredderBladeRecipes(writes, output);
        for (String fuel : List.of(
                "solid_fuel",
                "solid_fuel_presto",
                "solid_fuel_presto_triplet",
                "solid_fuel_bf",
                "solid_fuel_presto_bf",
                "solid_fuel_presto_triplet_bf",
                "rocket_fuel"
        )) {
            writes.add(save(output, generatedItemModel(fuel), itemModels, hbm(fuel)));
        }
        writes.add(save(output, largeGearRecipe(), recipes, hbm("gear_large")));

        for (String explosive : List.of("dynamite", "tnt_ntm", "semtex", "c4")) {
            String texture = explosive.equals("tnt_ntm") ? "tnt" : explosive;
            writes.add(save(output, explosiveBlockModel(texture), blockModels, hbm(explosive)));
            writes.add(save(output, blockItemModel(explosive), itemModels, hbm(explosive)));
            writes.add(save(output, explosiveBlockState(explosive), blockStates, hbm(explosive)));
            writes.add(save(output, selfDropLoot(explosive), lootTables, hbm(explosive)));
        }
        for (String component : List.of(
                "safety_fuse", "ball_dynamite", "stick_dynamite", "stick_tnt", "stick_semtex", "stick_c4"
        )) {
            writes.add(save(output, generatedItemModel(component), itemModels, hbm(component)));
        }
        writes.add(save(output, safetyFuseRecipe(), recipes, hbm("safety_fuse")));
        writes.add(save(output, explosiveBlockRecipe("stick_dynamite", "dynamite"), recipes, hbm("dynamite")));
        writes.add(save(output, explosiveBlockRecipe("stick_tnt", "tnt_ntm"), recipes, hbm("tnt_ntm")));
        writes.add(save(output, explosiveBlockRecipe("stick_semtex", "semtex"), recipes, hbm("semtex")));
        writes.add(save(output, explosiveBlockRecipe("stick_c4", "c4"), recipes, hbm("c4")));

        for (String charge : List.of("charge_dynamite", "charge_miner", "charge_c4", "charge_semtex")) {
            String model = charge.equals("charge_dynamite") || charge.equals("charge_miner")
                    ? "charge_dynamite" : "charge_c4";
            writes.add(save(output, parentModel(model + "_obj"), blockModels, hbm(charge)));
            writes.add(save(output, chargeItemModel(charge), itemModels, hbm(charge)));
            writes.add(save(output, chargeBlockState(charge), blockStates, hbm(charge)));
            writes.add(save(output, emptyLoot(), lootTables, hbm(charge)));
        }
        writes.add(save(output, generatedItemModel("ducttape"), itemModels, hbm("ducttape")));
        writes.add(save(output, handheldItemModel("defuser"), itemModels, hbm("defuser")));
        writes.add(save(output, generatedItemModel("rangefinder"), itemModels, hbm("rangefinder")));
        for (String detonator : List.of("detonator", "detonator_multi", "detonator_deadman", "detonator_de")) {
            writes.add(save(output, handheldItemModel(detonator), itemModels, hbm(detonator)));
        }
        writes.add(save(output, parentItemModel("builtin/entity"), itemModels, hbm("detonator_laser")));
        writes.add(save(output, ductTapeRecipe(), recipes, hbm("ducttape")));
        writes.add(save(output, rangefinderRecipe(), recipes, hbm("rangefinder")));
        writes.add(save(output, detonatorRecipe(), recipes, hbm("detonator")));
        writes.add(save(output, multiDetonatorRecipe(), recipes, hbm("detonator_multi")));
        writes.add(save(output, laserDetonatorRecipe(), recipes, hbm("detonator_laser")));
        writes.add(save(output, deadMansDetonatorRecipe(), recipes, hbm("detonator_deadman")));
        writes.add(save(output, deadMansExplosiveRecipe(), recipes, hbm("detonator_de")));
        writes.add(save(output, chargeShapelessRecipe("stick_dynamite", "charge_dynamite"), recipes,
                hbm("charge_dynamite")));
        writes.add(save(output, chargeMinerRecipe(), recipes, hbm("charge_miner")));
        writes.add(save(output, chargeShapelessRecipe("stick_semtex", "charge_semtex"), recipes,
                hbm("charge_semtex")));
        writes.add(save(output, chargeShapelessRecipe("stick_c4", "charge_c4"), recipes,
                hbm("charge_c4")));

        for (String nuke : List.of("nuke_gadget", "nuke_boy", "nuke_mike", "nuke_tsar")) {
            writes.add(save(output, emptyModel(nuke), blockModels, hbm(nuke)));
            writes.add(save(output, directionalEmptyBlockState(nuke), blockStates, hbm(nuke)));
            writes.add(save(output, nuke.equals("nuke_boy") ? generatedItemModel(nuke)
                    : parentItemModel("hbm:block/" + nuke + "_item_obj"), itemModels, hbm(nuke)));
            writes.add(save(output, selfDropLoot(nuke), lootTables, hbm(nuke)));
        }
        writes.add(save(output, emptyModel("fat_man"), blockModels, hbm("nuke_man")));
        writes.add(save(output, nukeManBlockState(), blockStates, hbm("nuke_man")));
        writes.add(save(output, parentItemModel("hbm:block/fat_man_item_obj"), itemModels, hbm("nuke_man")));
        writes.add(save(output, selfDropLoot("nuke_man"), lootTables, hbm("nuke_man")));
        writes.add(save(output, emptyModel("fleija"), blockModels, hbm("nuke_fleija")));
        writes.add(save(output, directionalEmptyBlockState("nuke_fleija"), blockStates, hbm("nuke_fleija")));
        writes.add(save(output, parentItemModel("hbm:block/fleija_item_obj"), itemModels, hbm("nuke_fleija")));
        writes.add(save(output, selfDropLoot("nuke_fleija"), lootTables, hbm("nuke_fleija")));
        writes.add(save(output, emptyModel("nuke_solinium"), blockModels, hbm("nuke_solinium")));
        writes.add(save(output, directionalEmptyBlockState("nuke_solinium"), blockStates, hbm("nuke_solinium")));
        writes.add(save(output, parentItemModel("hbm:block/solinium_item_obj"), itemModels, hbm("nuke_solinium")));
        writes.add(save(output, selfDropLoot("nuke_solinium"), lootTables, hbm("nuke_solinium")));
        // Empty block model; the renderer supplies the large angry object.
        writes.add(save(output, emptyModel("nuke_n2"), blockModels, hbm("nuke_n2")));
        writes.add(save(output, directionalEmptyBlockState("nuke_n2"), blockStates, hbm("nuke_n2")));
        writes.add(save(output, parentItemModel("hbm:block/nuke_n2_item_obj"), itemModels, hbm("nuke_n2")));
        writes.add(save(output, selfDropLoot("nuke_n2"), lootTables, hbm("nuke_n2")));
        writes.add(save(output, emptyModel("prototype"), blockModels, hbm("nuke_prototype")));
        writes.add(save(output, directionalEmptyBlockState("nuke_prototype"), blockStates, hbm("nuke_prototype")));
        writes.add(save(output, parentItemModel("hbm:block/prototype_item_obj"), itemModels, hbm("nuke_prototype")));
        writes.add(save(output, selfDropLoot("nuke_prototype"), lootTables, hbm("nuke_prototype")));
        // Empty in-world model, flat inventory lie.
        writes.add(save(output, emptyModel("custom"), blockModels, hbm("nuke_custom")));
        writes.add(save(output, directionalEmptyBlockState("nuke_custom"), blockStates, hbm("nuke_custom")));
        writes.add(save(output, generatedItemModel("custom", true), itemModels, hbm("nuke_custom")));
        writes.add(save(output, selfDropLoot("nuke_custom"), lootTables, hbm("nuke_custom")));
        for (String rod : List.of("custom_tnt", "custom_nuke", "custom_schrab")) {
            writes.add(save(output, generatedItemModel(rod), itemModels, hbm(rod)));
        }
        // Renderer gets the bomb; inventory gets the postage stamp.
        writes.add(save(output, emptyModel("fstbmb"), blockModels, hbm("nuke_fstbmb")));
        writes.add(save(output, directionalEmptyBlockState("nuke_fstbmb"), blockStates, hbm("nuke_fstbmb")));
        writes.add(save(output, generatedItemModel("nuke_fstbmb"), itemModels, hbm("nuke_fstbmb")));
        writes.add(save(output, selfDropLoot("nuke_fstbmb"), lootTables, hbm("nuke_fstbmb")));
        // BombGeneric in-world, suspicious square of steel in the inventory.
        writes.add(save(output, emptyModel("block_steel"), blockModels, hbm("bomb_multi")));
        writes.add(save(output, directionalEmptyBlockState("bomb_multi"), blockStates, hbm("bomb_multi")));
        writes.add(save(output, generatedItemModel("block_steel", true), itemModels, hbm("bomb_multi")));
        writes.add(save(output, selfDropLoot("bomb_multi"), lootTables, hbm("bomb_multi")));
        // Levitation bomb: cube_column, because even chaos respects texture orientation.
        writes.add(save(output, cubeColumnBlockModel("float_bomb", "bomb_float", "bomb_float_top"),
                blockModels, hbm("float_bomb")));
        writes.add(save(output, simpleBlockState("float_bomb"), blockStates, hbm("float_bomb")));
        writes.add(save(output, blockItemModel("float_bomb"), itemModels, hbm("float_bomb")));
        writes.add(save(output, selfDropLoot("float_bomb"), lootTables, hbm("float_bomb")));
        // Thermobarics are cubes with matching hats and shoes.
        writes.add(save(output, cubeBottomTopBlockModel("therm_endo", "therm_endo", "therm_top",
                "therm_top"), blockModels, hbm("therm_endo")));
        writes.add(save(output, cubeBottomTopBlockModel("therm_exo", "therm_exo", "therm_top",
                "therm_top"), blockModels, hbm("therm_exo")));
        for (String bomb : List.of("therm_endo", "therm_exo")) {
            writes.add(save(output, simpleBlockState(bomb), blockStates, hbm(bomb)));
            writes.add(save(output, blockItemModel(bomb), itemModels, hbm(bomb)));
            writes.add(save(output, selfDropLoot(bomb), lootTables, hbm(bomb)));
        }
        // Mines are renderer-only, drop nothing when broken and punish curiosity.
        Map<String, String> mineParticles = new java.util.LinkedHashMap<>();
        mineParticles.put("mine_ap", "mine_ap_grass");
        mineParticles.put("mine_he", "mine_marelet");
        mineParticles.put("mine_shrap", "mine_shrapnel");
        mineParticles.put("mine_fat", "mine_fat");
        mineParticles.put("mine_naval", "nmine");
        for (Map.Entry<String, String> mine : mineParticles.entrySet()) {
            writes.add(save(output, emptyModel(mine.getValue()), blockModels, hbm(mine.getKey())));
            writes.add(save(output, simpleBlockState(mine.getKey()), blockStates, hbm(mine.getKey())));
            writes.add(save(output, generatedItemModel(mine.getKey()), itemModels, hbm(mine.getKey())));
            writes.add(save(output, emptyLoot(), lootTables, hbm(mine.getKey())));
        }
        // Ballistite is late, so "any smokeless" currently means Cordite.
        writes.add(save(output, foundryShaped(List.of("I", "C", "S"), Map.of(
                "I", itemIngredient("hbm:plate_polymer"),
                "C", tagIngredient("hbm:smokeless"),
                "S", tagIngredient("c:ingots/steel")),
                "hbm:mine_ap", 4), recipes, hbm("mine_ap")));
        JsonArray smokeless = new JsonArray();
        smokeless.add("hbm:cordite");
        writes.add(save(output, tag(smokeless), itemTags, hbm("smokeless")));
        for (String component : List.of("egg_balefire", "egg_balefire_shard", "battery_spark", "battery_trixite")) {
            writes.add(save(output, generatedItemModel(component), itemModels, hbm(component)));
        }
        // Two rods share a recipe shape. The others are waiting for their periodic table.
        writes.add(save(output, foundryShaped(List.of(" C ", "LUL", "LUL"), Map.of(
                "C", itemIngredient("hbm:plate_copper"),
                "L", itemIngredient("hbm:plate_lead"),
                "U", itemIngredient("hbm:ingot_u235")),
                "hbm:custom_nuke", 1), recipes, hbm("custom_nuke")));
        writes.add(save(output, foundryShaped(List.of(" C ", "LUL", "LUL"), Map.of(
                "C", itemIngredient("hbm:plate_copper"),
                "L", itemIngredient("hbm:plate_lead"),
                "U", itemIngredient("hbm:ingot_schrabidium")),
                "hbm:custom_schrab", 1), recipes, hbm("custom_schrab")));
        // Full 3D became handheld. Close enough for government work.
        writes.add(save(output, handheldItemModel("igniter"), itemModels, hbm("igniter")));
        writes.add(save(output, falloutBlockModel(), blockModels, hbm("fallout")));
        writes.add(save(output, simpleBlockState("fallout"), blockStates, hbm("fallout")));
        writes.add(save(output, generatedItemModel("fallout"), itemModels, hbm("fallout")));
        writes.add(save(output, selfDropLoot("fallout"), lootTables, hbm("fallout")));
        for (String component : List.of(
                "early_explosive_lenses", "explosive_lenses", "gadget_wireing", "gadget_core",
                "boy_shielding", "boy_target", "boy_bullet", "boy_propellant", "boy_igniter",
                "man_igniter", "man_core", "mike_core", "mike_deut", "mike_cooling_unit", "tsar_core",
                "n2_charge",
                "fleija_igniter", "fleija_propellant", "fleija_core",
                "solinium_igniter", "solinium_propellant", "solinium_core"
        )) {
            writes.add(save(output, generatedItemModel(component), itemModels, hbm(component)));
        }
        writes.add(save(output, generatedItemModel("geiger_counter"), itemModels, hbm("geiger_counter")));
        writes.add(save(output, generatedItemModel("dosimeter"), itemModels, hbm("dosimeter")));
        writes.add(save(output, handheldItemModel("reacher"), itemModels, hbm("reacher")));
        for (String medicine : List.of(
                "iv_empty", "iv_blood", "radaway", "radaway_strong", "radaway_flush", "radx", "pill_herbal"
        )) {
            writes.add(save(output, generatedItemModel(medicine), itemModels, hbm(medicine)));
        }
        for (String protection : List.of(
                "hazmat_cloth", "rag", "rag_damp", "rag_piss", "filter_coal", "catalyst_clay",
                "gas_mask_filter", "gas_mask_filter_mono", "gas_mask_filter_combo",
                "gas_mask_filter_rag", "gas_mask_filter_piss", "hazmat_helmet", "hazmat_plate",
                "hazmat_legs", "hazmat_boots", "goggles", "gas_mask", "gas_mask_m65",
                "gas_mask_mono", "gas_mask_olde", "mask_rag", "mask_piss"
        )) {
            writes.add(save(output, generatedItemModel(protection), itemModels, hbm(protection)));
        }
        for (String piece : List.of("envsuit_helmet", "envsuit_plate", "envsuit_legs", "envsuit_boots")) {
            // Every context gets the full M1TTY fashion show.
            writes.add(save(output, parentItemModel("builtin/entity"), itemModels, hbm(piece)));
        }
        for (String piece : List.of("dns_helmet", "dns_plate", "dns_legs", "dns_boots")) {
            // DNT also refuses to become a flat icon.
            writes.add(save(output, parentItemModel("builtin/entity"), itemModels, hbm(piece)));
        }
        writes.add(save(output, hazmatHelmetRecipe(), recipes, hbm("hazmat_helmet")));
        writes.add(save(output, armorRecipe(List.of("E E", "EEE", "EEE"), "hazmat_plate"), recipes,
                hbm("hazmat_plate")));
        writes.add(save(output, armorRecipe(List.of("EEE", "E E", "E E"), "hazmat_legs"), recipes,
                hbm("hazmat_legs")));
        writes.add(save(output, armorRecipe(List.of("E E", "E E"), "hazmat_boots"), recipes,
                hbm("hazmat_boots")));
        addProtectiveMaskRecipes(writes, output);
        addEnvsuitRecipes(writes, output);
        writes.add(save(output, geigerBlockModel(), blockModels, hbm("geiger")));
        writes.add(save(output, geigerBlockState(), blockStates, hbm("geiger")));
        writes.add(save(output, selfDropLoot("geiger"), lootTables, hbm("geiger")));
        writes.add(save(output, shapelessRecipe(List.of("geiger_counter"), "geiger", 1), recipes,
                hbm("geiger_from_geiger_counter")));
        writes.add(save(output, shapelessRecipe(List.of("geiger"), "geiger_counter", 1), recipes,
                hbm("geiger_counter_from_geiger")));
        writes.add(save(output, armorTableBlockModel(), blockModels, hbm("machine_armor_table")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_armor_table"), itemModels,
                hbm("machine_armor_table")));
        writes.add(save(output, simpleBlockState("machine_armor_table"), blockStates, hbm("machine_armor_table")));
        writes.add(save(output, selfDropLoot("machine_armor_table"), lootTables, hbm("machine_armor_table")));
        writes.add(save(output, armorTableRecipe(), recipes, hbm("machine_armor_table")));
        writes.add(save(output, weaponTableBlockModel(), blockModels, hbm("machine_weapon_table")));
        writes.add(save(output, legacyStandardBlockItemModel("machine_weapon_table"), itemModels,
                hbm("machine_weapon_table")));
        writes.add(save(output, simpleBlockState("machine_weapon_table"), blockStates,
                hbm("machine_weapon_table")));
        writes.add(save(output, selfDropLoot("machine_weapon_table"), lootTables,
                hbm("machine_weapon_table")));
        writes.add(save(output, weaponTableRecipe(), recipes, hbm("machine_weapon_table")));
        writes.add(save(output, generatedItemModel("weapon_mod_silencer"), itemModels,
                hbm("weapon_mod_silencer")));
        writes.add(save(output, silencerRecipe(), recipes, hbm("weapon_mod_silencer")));
        writes.add(save(output, generatedItemModel("weapon_mod_speedloader"), itemModels,
                hbm("weapon_mod_speedloader")));
        writes.add(save(output, speedloaderRecipe(), recipes, hbm("weapon_mod_speedloader")));
        writes.add(save(output, generatedItemModel("weapon_mod_scope"), itemModels,
                hbm("weapon_mod_scope")));
        writes.add(save(output, scopeRecipe(), recipes, hbm("weapon_mod_scope")));
        writes.add(save(output, generatedItemModel("weapon_mod_saw"), itemModels,
                hbm("weapon_mod_saw")));
        writes.add(save(output, sawRecipe(), recipes, hbm("weapon_mod_saw")));
        writes.add(save(output, generatedItemModel("weapon_mod_grease_gun"), itemModels,
                hbm("weapon_mod_grease_gun")));
        writes.add(save(output, greaseGunKitRecipe(), recipes, hbm("weapon_mod_grease_gun")));
        writes.add(save(output, generatedItemModel("weapon_mod_slowdown"), itemModels,
                hbm("weapon_mod_slowdown")));
        writes.add(save(output, slowdownRecipe(), recipes, hbm("weapon_mod_slowdown")));
        for (String material : List.of("iron", "lead", "steel", "desh", "ferrouranium", "saturnite",
                "bismuth_bronze", "arsenic_bronze", "schrabidate", "dnt", "osmiridium", "murky")) {
            writes.add(save(output, selfDropLoot("anvil_" + material), lootTables, hbm("anvil_" + material)));
        }
        writes.add(save(output, baseAnvilRecipe("iron"), recipes, hbm("anvil_iron")));
        writes.add(save(output, baseAnvilRecipe("lead"), recipes, hbm("anvil_lead")));
        writes.add(save(output, obsidianCladdingRecipe(), recipes, hbm("cladding_obsidian")));
        writes.add(save(output, rubberCladdingRecipe(), recipes, hbm("cladding_rubber")));
        writes.add(save(output, leadCladdingRecipe(), recipes, hbm("cladding_lead")));
        writes.add(save(output, deshCladdingRecipe(), recipes, hbm("cladding_desh")));
        for (String cladding : List.of("cladding_paint", "cladding_rubber", "cladding_lead",
                "cladding_desh", "cladding_ghiorsium", "cladding_iron", "cladding_obsidian")) {
            writes.add(save(output, generatedItemModel(cladding), itemModels, hbm(cladding)));
        }
        writes.add(save(output, generatedItemModel("plate_polymer"), itemModels, hbm("plate_polymer")));
        writes.add(save(output, insulatorStringWoolRecipe(), recipes, hbm("plate_polymer_from_wool")));
        writes.add(save(output, insulatorTwoItemRecipe("minecraft:brick", 4), recipes,
                hbm("plate_polymer_from_brick")));
        writes.add(save(output, insulatorTwoItemRecipe("minecraft:nether_brick", 4), recipes,
                hbm("plate_polymer_from_nether_brick")));
        writes.add(save(output, insulatorTwoItemRecipe("hbm:ingot_asbestos", 16), recipes,
                hbm("plate_polymer_from_asbestos")));
        writes.add(save(output, insulatorTwoTagRecipe("hbm:ingots/any_rubber", 8), recipes,
                hbm("plate_polymer_from_rubber")));
        writes.add(save(output, latexRecipe(), recipes, hbm("ball_resin")));
        writes.add(save(output, latexSmeltingRecipe(), recipes, hbm("ingot_biorubber")));
        writes.add(save(output, ironCladdingRecipe(), recipes, hbm("cladding_iron")));
        writes.add(save(output, wireFineModel(), itemModels, hbm("wire_fine")));
        writes.add(save(output, boltModel(), itemModels, hbm("bolt")));
        writes.add(save(output, generatedItemModel("bolt_steel"), itemModels, hbm("bolt_steel")));
        writes.add(save(output, generatedItemModel("bolt_lead"), itemModels, hbm("bolt_lead")));
        writes.add(save(output, generatedItemModel("bolt_tungsten"), itemModels, hbm("bolt_tungsten")));
        writes.add(save(output, boltRecipe("steel", 30), recipes, hbm("bolt_steel")));
        writes.add(save(output, boltRecipe("lead", 8200), recipes, hbm("bolt_lead")));
        writes.add(save(output, boltRecipe("tungsten", 7400), recipes, hbm("bolt_tungsten")));
        writes.add(save(output, castPlateModel(), itemModels, hbm("plate_cast")));
        writes.add(save(output, generatedItemModel("plate_cast_iron"), itemModels, hbm("plate_cast_iron")));
        writes.add(save(output, generatedItemModel("plate_cast_titanium"), itemModels,
                hbm("plate_cast_titanium")));
        writes.add(save(output, generatedItemModel("plate_cast_steel"), itemModels, hbm("plate_cast_steel")));
        writes.add(save(output, generatedItemModel("plate_cast_copper"), itemModels, hbm("plate_cast_copper")));
        writes.add(save(output, generatedItemModel("plate_cast_lead"), itemModels, hbm("plate_cast_lead")));
        writes.add(save(output, generatedItemModel("plate_cast_dura_steel"), itemModels, hbm("plate_cast_dura_steel")));
        writes.add(save(output, generatedItemModel("plate_cast_technetium_steel"), itemModels,
                hbm("plate_cast_technetium_steel")));
        writes.add(save(output, generatedItemModel("plate_cast_cadmium_steel"), itemModels,
                hbm("plate_cast_cadmium_steel")));
        writes.add(save(output, ironCastPlateRecipe(), recipes, hbm("plate_cast_iron")));
        writes.add(save(output, weldedPlateModel(), itemModels, hbm("plate_welded")));
        writes.add(save(output, generatedItemModel("plate_welded_steel"), itemModels,
                hbm("plate_welded_steel")));
        writes.add(save(output, generatedItemModel("plate_welded_technetium_steel"), itemModels,
                hbm("plate_welded_technetium_steel")));
        writes.add(save(output, generatedItemModel("plate_welded_cadmium_steel"), itemModels,
                hbm("plate_welded_cadmium_steel")));
        writes.add(save(output, generatedItemModel("arc_electrode.graphite"), itemModels, hbm("arc_electrode")));
        writes.add(save(output, generatedItemModel("arc_electrode_burnt.graphite"), itemModels,
                hbm("arc_electrode_burnt")));
        writes.add(save(output, graphiteElectrodeRecipe(), recipes, hbm("arc_electrode_graphite")));
        writes.add(save(output, smeltingRecipe("hbm:arc_electrode_burnt", "hbm:ingot_graphite", 3F),
                recipes, hbm("arc_electrode_burnt_graphite")));
        writes.add(save(output, generatedItemModel("powder_flux"), itemModels, hbm("powder_flux")));
        writes.add(save(output, generatedItemModel("ball_fireclay"), itemModels, hbm("ball_fireclay")));
        writes.add(save(output, generatedItemModel("mold_base"), itemModels, hbm("mold_base")));
        writes.add(save(output, foundryMoldItemModel(), itemModels, hbm("mold")));
        for (FoundryMoldItem.Mold mold : FoundryMoldItem.Mold.values()) {
            writes.add(save(output, generatedItemModel("mold_" + mold.texture()), itemModels,
                    hbm("mold_" + mold.texture())));
        }
        writes.add(save(output, generatedItemModel("blade_titanium"), itemModels, hbm("blade_titanium")));
        writes.add(save(output, generatedItemModel("turbine_titanium"), itemModels,
                hbm("turbine_titanium")));
        writes.add(save(output, turbineTitaniumRecipe(), recipes, hbm("turbine_titanium")));
        writes.add(save(output, generatedItemModel("blade_tungsten"), itemModels, hbm("blade_tungsten")));
        writes.add(save(output, generatedItemModel("wire_dense"), itemModels, hbm("wire_dense")));
        writes.add(save(output, casingItemModel(), itemModels, hbm("casing")));
        for (String casing : List.of("small", "large", "small_steel", "large_steel", "shotshell", "buckshot", "buckshot_advanced")) {
            String texture = switch (casing) {
                case "shotshell", "buckshot", "buckshot_advanced" -> "casing_" + casing;
                default -> "casing." + casing;
            };
            writes.add(save(output, generatedItemModel(texture), itemModels,
                    hbm("casing_" + casing)));
        }
        for (String part : List.of("part_barrel_light", "part_barrel_heavy", "part_receiver_light",
                "part_receiver_heavy", "part_mechanism", "part_stock", "part_grip")) {
            writes.add(save(output, generatedItemModel(part), itemModels, hbm(part)));
        }
        writes.add(save(output, foundryScrapsModel(), itemModels, hbm("scraps")));
        writes.add(save(output, generatedItemModel("ingot_raw"), itemModels, hbm("ingot_raw")));
        for (String material : List.of("iron", "copper", "tungsten", "cobalt", "carbon", "flux", "steel",
                "dura_steel", "red_copper", "magnetized_tungsten")) {
            writes.add(save(output, generatedItemModel("scraps_" + material), itemModels, hbm("scraps_" + material)));
        }
        writes.add(save(output, generatedItemModel("scraps"), itemModels, hbm("scraps_technetium_steel")));
        writes.add(save(output, handheldItemModel("screwdriver"), itemModels, hbm("screwdriver")));
        writes.add(save(output, handheldItemModel("blowtorch"), itemModels, hbm("blowtorch")));
        writes.add(save(output, generatedItemModel("book_guide"), itemModels, hbm("book_guide")));
        writes.add(save(output, guideBookRecipe(), recipes, hbm("book_guide")));
        writes.add(save(output, generatedItemModel("piston_pneumatic"), itemModels, hbm("part_generic")));
        writes.add(save(output, craneInventoryBlockModel("boxer"), blockModels,
                hbm("crane_boxer_inventory")));
        writes.add(save(output, craneInventoryBlockModel("extractor"), blockModels,
                hbm("crane_extractor_inventory")));
        writes.add(save(output, craneInventoryBlockModel("inserter"), blockModels,
                hbm("crane_inserter_inventory")));
        writes.add(save(output, legacyBlockItemModel("crane_boxer_inventory", List.of(30, 45, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels,
                hbm("crane_boxer")));
        writes.add(save(output, legacyBlockItemModel("crane_extractor_inventory", List.of(30, 45, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels,
                hbm("crane_extractor")));
        writes.add(save(output, legacyBlockItemModel("crane_inserter_inventory", List.of(30, 45, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels,
                hbm("crane_inserter")));
        writes.add(save(output, fluidIdentifierModel(), itemModels, hbm("fluid_identifier_multi")));
        writes.add(save(output, fluidIdentifierRecipe(), recipes, hbm("fluid_identifier_multi")));
        writes.add(save(output, fluidDuctBaseRecipe(), recipes, hbm("fluid_duct_neo")));
        writes.add(save(output, fluidDuctUntypingRecipe(), recipes, hbm("fluid_duct_neo_from_typed")));
        writes.add(save(output, pipeModel(), itemModels, hbm("pipe")));
        writes.add(save(output, generatedItemModel("pipe_copper"), itemModels, hbm("pipe_copper")));
        writes.add(save(output, generatedItemModel("pipe_steel"), itemModels, hbm("pipe_steel")));
        writes.add(save(output, generatedItemModel("pipe_steel"), itemModels, hbm("pipe_dura_steel")));
        writes.add(save(output, generatedItemModel("pipe_lead"), itemModels, hbm("pipe_lead")));
        writes.add(save(output, generatedItemModel("fluid_tank"), itemModels, hbm("fluid_tank_empty")));
        writes.add(save(output, fluidTankFullModel(), itemModels, hbm("fluid_tank_full")));
        writes.add(save(output, universalFluidTankRecipe(), recipes, hbm("fluid_tank_empty")));
        writes.add(save(output, fluxRecipe(), recipes, hbm("powder_flux")));
        writes.add(save(output, fireclayFromAluminumDustRecipe(), recipes,
                hbm("ball_fireclay_from_aluminum_dust")));
        writes.add(save(output, fireclayFromAluminumOreRecipe(), recipes,
                hbm("ball_fireclay_from_aluminum_ore")));
        writes.add(save(output, fireclayFromLimestoneRecipe(), recipes,
                hbm("ball_fireclay_from_limestone")));
        writes.add(save(output, smeltingRecipe("hbm:ball_fireclay", "hbm:ingot_firebrick", 0.1F),
                recipes, hbm("ingot_firebrick_from_fireclay")));
        writes.add(save(output, moldBaseRecipe(), recipes, hbm("mold_base")));
        writes.add(save(output, foundryMoldRecipe(), recipes, hbm("foundry_mold")));
        writes.add(save(output, foundryBasinRecipe(), recipes, hbm("foundry_basin")));
        writes.add(save(output, foundryChannelRecipe(), recipes, hbm("foundry_channel")));
        writes.add(save(output, foundryTankRecipe(), recipes, hbm("foundry_tank")));
        writes.add(save(output, foundryOutletRecipe(false), recipes, hbm("foundry_outlet")));
        writes.add(save(output, foundryOutletRecipe(true), recipes, hbm("foundry_slagtap")));
        writes.add(save(output, screwdriverRecipe(), recipes, hbm("screwdriver")));
        writes.add(save(output, pneumaticPistonRecipe(), recipes, hbm("part_generic")));
        writes.add(save(output, conveyorBoxerRecipe(), recipes, hbm("crane_boxer")));
        writes.add(save(output, craneEndpointRecipe("crane_extractor", true,
                itemIngredient("minecraft:stone_bricks"), 1), recipes, hbm("crane_extractor_from_stone")));
        writes.add(save(output, craneEndpointRecipe("crane_extractor", true,
                tagIngredient("c:ingots/iron"), 2), recipes, hbm("crane_extractor_from_iron")));
        writes.add(save(output, craneEndpointRecipe("crane_extractor", true,
                tagIngredient("c:ingots/steel"), 4), recipes, hbm("crane_extractor_from_steel")));
        writes.add(save(output, craneEndpointRecipe("crane_inserter", false,
                itemIngredient("minecraft:stone_bricks"), 1), recipes, hbm("crane_inserter_from_stone")));
        writes.add(save(output, craneEndpointRecipe("crane_inserter", false,
                tagIngredient("c:ingots/iron"), 2), recipes, hbm("crane_inserter_from_iron")));
        writes.add(save(output, craneEndpointRecipe("crane_inserter", false,
                tagIngredient("c:ingots/steel"), 4), recipes, hbm("crane_inserter_from_steel")));
        writes.add(save(output, regularConveyorRecipe(), recipes, hbm("conveyor_wand")));
        writes.add(save(output, regularConveyorRubberRecipe(), recipes, hbm("conveyor_wand_rubber")));
        writes.add(save(output, expressConveyorRecipe(), recipes, hbm("conveyor_wand_express")));
        writes.add(save(output, doubleConveyorRecipe(), recipes, hbm("conveyor_wand_double")));
        writes.add(save(output, tripleConveyorRecipe(), recipes, hbm("conveyor_wand_triple")));
        writes.add(save(output, selfDropLoot("conveyor_wand"), lootTables, hbm("conveyor")));
        writes.add(save(output, selfDropLoot("conveyor_wand_express"), lootTables, hbm("conveyor_express")));
        writes.add(save(output, selfDropLoot("conveyor_wand_double"), lootTables, hbm("conveyor_double")));
        writes.add(save(output, selfDropLoot("conveyor_wand_triple"), lootTables, hbm("conveyor_triple")));
        writes.add(save(output, selfDropLoot("conveyor_wand"), lootTables, hbm("conveyor_lift")));
        writes.add(save(output, selfDropLoot("conveyor_wand"), lootTables, hbm("conveyor_chute")));
        writes.add(save(output, selfDropLoot("crane_extractor"), lootTables, hbm("crane_extractor")));
        writes.add(save(output, selfDropLoot("crane_inserter"), lootTables, hbm("crane_inserter")));
        writes.add(save(output, selfDropLoot("crane_boxer"), lootTables, hbm("crane_boxer")));
        for (String material : List.of("steel", "red_copper", "carbon", "aluminium", "copper", "tungsten", "gold",
                "lead", "zirconium")) {
            writes.add(save(output, generatedItemModel("wire_" + material), itemModels,
                    hbm("wire_fine_" + material)));
        }
        for (String component : List.of("coil_copper", "coil_copper_torus", "coil_gold",
                "coil_gold_torus", "coil_tungsten", "tank_steel", "motor", "motor_desh", "drill_titanium")) {
            writes.add(save(output, generatedItemModel(component), itemModels, hbm(component)));
        }
        writes.add(save(output, generatedItemModel("plate_saturnite"), itemModels, hbm("plate_saturnite")));
        addFineWireAndComponentRecipes(writes, output);
        addSiliconConversionRecipes(writes, output);
        writes.add(save(output, circuitModel(), itemModels, hbm("circuit")));
        for (String circuit : List.of("numitron", "capacitor", "capacitor_tantalium", "pcb", "silicon", "chip",
                "analog", "basic", "advanced", "capacitor_board")) {
            writes.add(save(output, generatedItemModel("circuit." + circuit), itemModels, hbm("circuit_" + circuit)));
        }
        addEarlyCircuitRecipes(writes, output);
        writes.add(save(output, insulatorBlockModel(false), blockModels, hbm("block_insulator")));
        writes.add(save(output, insulatorBlockModel(true), blockModels, hbm("block_insulator_horizontal")));
        writes.add(save(output, rotatedPillarBlockState("block_insulator"), blockStates, hbm("block_insulator")));
        writes.add(save(output, blockItemModel("block_insulator"), itemModels, hbm("block_insulator")));
        writes.add(save(output, selfDropLoot("block_insulator"), lootTables, hbm("block_insulator")));
        writes.add(save(output, insulatorBlockCompressionRecipe(), recipes, hbm("block_insulator")));
        writes.add(save(output, insulatorBlockDecompressionRecipe(), recipes, hbm("plate_polymer_from_block")));
        writes.add(save(output, boxBlockModel("sandbags", 4, 0, 4, 12, 16, 12), blockModels,
                hbm("sandbags_core")));
        writes.add(save(output, boxBlockModel("sandbags", 4, 0, 0, 12, 16, 4), blockModels,
                hbm("sandbags_north")));
        writes.add(save(output, boxBlockModel("sandbags", 12, 0, 4, 16, 16, 12), blockModels,
                hbm("sandbags_east")));
        writes.add(save(output, boxBlockModel("sandbags", 4, 0, 12, 12, 16, 16), blockModels,
                hbm("sandbags_south")));
        writes.add(save(output, boxBlockModel("sandbags", 0, 0, 4, 4, 16, 12), blockModels,
                hbm("sandbags_west")));
        writes.add(save(output, boxBlockModel("sandbags", 2, 0, 2, 14, 16, 14), blockModels,
                hbm("sandbags_inventory")));
        writes.add(save(output, sandbagsBlockState(), blockStates, hbm("sandbags")));
        writes.add(save(output, legacyBlockItemModel("sandbags_inventory", List.of(30, 45, 0),
                List.of(0, 0, 0), List.of(0.625, 0.625, 0.625)), itemModels, hbm("sandbags")));
        writes.add(save(output, selfDropLoot("sandbags"), lootTables, hbm("sandbags")));
        writes.add(save(output, sandbagsRecipe(), recipes, hbm("sandbags")));

        writes.add(save(output, radiationDamageType(), damageTypes, hbm("radiation")));
        writes.add(save(output, nuclearBlastDamageType(), damageTypes, hbm("nuclear_blast")));
        writes.add(save(output, rubbleDamageType(), damageTypes, hbm("rubble")));
        writes.add(save(output, blenderDamageType(), damageTypes, hbm("blender")));
        writes.add(save(output, bulletDamageType(), damageTypes, hbm("bullet")));
        writes.add(save(output, shrapnelDamageType(), damageTypes, hbm("shrapnel")));
        writes.add(save(output, absoluteDamageType("laser"), damageTypes, hbm("laser")));
        // Plasma respects armor. Laser has no such manners.
        writes.add(save(output, damageType("plasma"), damageTypes, hbm("plasma")));
        writes.add(save(output, absoluteDamageType("bang"), damageTypes, hbm("bang")));
        writes.add(save(output, absoluteDamageType("blackhole"), damageTypes, hbm("blackhole")));
        writes.add(save(output, absoluteDamageType("monoxide"), damageTypes, hbm("monoxide")));
        writes.add(save(output, damageType("electric"), damageTypes, hbm("electric")));
        writes.add(save(output, absoluteDamageType("mku", 0.0F), damageTypes, hbm("mku")));
        writes.add(save(output, absoluteDamageType("ams"), damageTypes, hbm("ams")));
        writes.add(save(output, absoluteDamageType("amsCore"), damageTypes, hbm("ams_core")));
        writes.add(save(output, damageType("hard_landing"), damageTypes, hbm("hard_landing")));
        writes.add(save(output, damageType("flamethrower"), damageTypes, hbm("flamethrower")));
        writes.add(save(output, damageType("subatomic"), damageTypes, hbm("subatomic")));
        writes.add(save(output, damageType("tauBlast"), damageTypes, hbm("tau_blast")));
        JsonArray armorBypassingDamage = new JsonArray();
        armorBypassingDamage.add("hbm:radiation");
        armorBypassingDamage.add("hbm:blender");
        armorBypassingDamage.add("hbm:laser");
        armorBypassingDamage.add("hbm:bang");
        armorBypassingDamage.add("hbm:blackhole");
        armorBypassingDamage.add("hbm:monoxide");
        armorBypassingDamage.add("hbm:mku");
        armorBypassingDamage.add("hbm:ams");
        armorBypassingDamage.add("hbm:ams_core");
        armorBypassingDamage.add("hbm:hard_landing");
        armorBypassingDamage.add("hbm:tau_blast");
        writes.add(save(output, tag(armorBypassingDamage), damageTypeTags, minecraft("bypasses_armor")));
        JsonArray effectBypassingDamage = new JsonArray();
        effectBypassingDamage.add("hbm:monoxide");
        effectBypassingDamage.add("hbm:mku");
        writes.add(save(output, tag(effectBypassingDamage), damageTypeTags, minecraft("bypasses_effects")));
        JsonArray projectileDamage = new JsonArray();
        projectileDamage.add("hbm:rubble");
        projectileDamage.add("hbm:bullet");
        projectileDamage.add("hbm:shrapnel");
        projectileDamage.add("hbm:flamethrower");
        writes.add(save(output, tag(projectileDamage), damageTypeTags, minecraft("is_projectile")));
        writes.add(save(output, tag("hbm:flamethrower", false), damageTypeTags, minecraft("is_fire")));
        JsonArray explosionDamage = new JsonArray();
        explosionDamage.add("hbm:nuclear_blast");
        explosionDamage.add("hbm:tau_blast");
        writes.add(save(output, tag(explosionDamage), damageTypeTags, minecraft("is_explosion")));
        writes.add(save(output, pepperboxRecipe(), recipes, hbm("gun_pepperbox")));
        writes.add(save(output, blackPowderAmmoRecipe("stone", tagIngredient("c:cobblestones/normal")), recipes,
                hbm("ammo_standard_stone")));
        writes.add(save(output, blackPowderAmmoRecipe("stone_ap", itemIngredient("minecraft:flint")), recipes,
                hbm("ammo_standard_stone_ap")));
        writes.add(save(output, blackPowderAmmoRecipe("stone_iron", tagIngredient("c:ingots/iron")), recipes,
                hbm("ammo_standard_stone_iron")));
        writes.add(save(output, blackPowderAmmoRecipe("stone_shot", itemIngredient("minecraft:gravel")), recipes,
                hbm("ammo_standard_stone_shot")));
        writes.add(save(output, language, languages, hbm("en_us")));
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(
            CachedOutput output,
            JsonObject json,
            PackOutput.PathProvider paths,
            ResourceLocation id
    ) {
        return DataProvider.saveStable(output, json, paths.json(id));
    }

    private JsonObject explosiveBlockModel(String texture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/" + texture + "_side");
        textures.addProperty("down", "hbm:block/" + texture + "_bottom");
        textures.addProperty("up", "hbm:block/" + texture + "_top");
        textures.addProperty("north", "hbm:block/" + texture + "_side");
        textures.addProperty("south", "hbm:block/" + texture + "_side");
        textures.addProperty("west", "hbm:block/" + texture + "_side");
        textures.addProperty("east", "hbm:block/" + texture + "_side");
        root.add("textures", textures);
        return root;
    }

    private JsonObject geigerBlockModel() {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/geiger");
        root.add("textures", textures);
        root.add("elements", new JsonArray());
        return root;
    }

    private JsonObject chunkOreModel() {
        JsonObject root = generatedItemModel("chunk_ore.rare");
        JsonObject predicate = new JsonObject();
        predicate.addProperty("custom_model_data", 1);
        JsonObject override = new JsonObject();
        override.add("predicate", predicate);
        override.addProperty("model", "hbm:item/chunk_ore_malachite");
        JsonArray overrides = new JsonArray();
        overrides.add(override);
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject stoneResourceBlockState() {
        JsonObject variants = new JsonObject();
        for (String type : List.of("sulfur", "asbestos", "hematite", "malachite", "limestone", "bauxite")) {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", "hbm:block/stone_resource_" + type);
            variants.add("type=" + type, variant);
        }
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    private JsonObject stoneResourceItemModel() {
        JsonObject root = parentItemModel("hbm:block/stone_resource_hematite");
        JsonArray overrides = new JsonArray();
        List<String> types = List.of("sulfur", "asbestos", "hematite", "malachite", "limestone", "bauxite");
        for (int metadata = 0; metadata < types.size(); metadata++) {
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", metadata);
            JsonObject override = new JsonObject();
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/stone_resource_" + types.get(metadata));
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject sellafieldBlockState() {
        JsonObject variants = new JsonObject();
        for (int level = 0; level < 6; level++) {
            JsonArray choices = new JsonArray();
            for (int variant = 0; variant < 4; variant++) {
                JsonObject choice = new JsonObject();
                choice.addProperty("model", "hbm:block/sellafield_" + level + "_" + variant);
                choices.add(choice);
            }
            variants.add("level=" + level, choices);
        }
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    private JsonObject sellafieldSlakedBlockState() {
        JsonArray choices = new JsonArray();
        for (int variant = 0; variant < 4; variant++) {
            JsonObject choice = new JsonObject();
            choice.addProperty("model", "hbm:block/sellafield_slaked_" + variant);
            choices.add(choice);
        }
        JsonObject variants = new JsonObject();
        variants.add("", choices);
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    private JsonObject sellafieldItemModel() {
        JsonObject root = parentItemModel("hbm:block/sellafield_0_0");
        JsonArray overrides = new JsonArray();
        for (int level = 0; level < 6; level++) {
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", level);
            JsonObject override = new JsonObject();
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/sellafield_" + level);
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject geigerBlockState() {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (String facing : List.of("north", "south", "west", "east")) {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", "hbm:block/geiger");
            variants.add("facing=" + facing, variant);
        }
        root.add("variants", variants);
        return root;
    }

    private JsonObject explosiveBlockState(String id) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (boolean unstable : List.of(false, true)) {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", "hbm:block/" + id);
            variants.add("unstable=" + unstable, variant);
        }
        root.add("variants", variants);
        return root;
    }

    private JsonObject hazmatHelmetRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "equipment");
        JsonArray pattern = new JsonArray();
        pattern.add("EEE");
        pattern.add("EIE");
        pattern.add(" P ");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("E", itemIngredient("hbm:hazmat_cloth"));
        key.add("I", tagIngredient("c:glass_panes"));
        key.add("P", itemIngredient("hbm:plate_iron"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:hazmat_helmet");
        recipe.add("result", result);
        return recipe;
    }

    private void addProtectiveMaskRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        writes.add(save(output, foundryShaped(List.of("SW", "WS"), Map.of(
                "S", itemIngredient("minecraft:string"),
                "W", tagIngredient("minecraft:wool")), "hbm:rag", 4), recipes, hbm("rag")));
        writes.add(save(output, catalystClayRecipe(), recipes, hbm("catalyst_clay")));
        writes.add(save(output, smeltingRecipe("hbm:rag_damp", "hbm:rag", 0.1F),
                recipes, hbm("rag_from_damp_smelting")));
        writes.add(save(output, smeltingRecipe("hbm:rag_piss", "hbm:rag", 0.1F),
                recipes, hbm("rag_from_piss_smelting")));

        writes.add(save(output, foundryShaped(List.of("P P", "GPG"), Map.of(
                "P", tagIngredient("c:plates/steel"),
                "G", tagIngredient("c:glass_panes")), "hbm:goggles", 1), recipes, hbm("goggles")));
        writes.add(save(output, foundryShaped(List.of("PPP", "GPG", " F "), Map.of(
                "P", tagIngredient("c:plates/steel"),
                "G", tagIngredient("c:glass_panes"),
                "F", tagIngredient("c:plates/iron")), "hbm:gas_mask", 1), recipes, hbm("gas_mask")));
        writes.add(save(output, foundryShaped(List.of("PPP", "GPG", " F "), Map.of(
                "P", tagIngredient("hbm:ingots/any_rubber"),
                "G", tagIngredient("c:glass_panes"),
                "F", tagIngredient("c:plates/iron")), "hbm:gas_mask_m65", 1),
                recipes, hbm("gas_mask_m65")));
        writes.add(save(output, foundryShaped(List.of("PPP", "GPG", " F "), Map.of(
                "P", itemIngredient("minecraft:leather"),
                "G", tagIngredient("c:glass_panes"),
                "F", tagIngredient("c:ingots/iron")), "hbm:gas_mask_olde", 1),
                recipes, hbm("gas_mask_olde")));
        writes.add(save(output, foundryShaped(List.of(" P ", "PPP", " F "), Map.of(
                "P", tagIngredient("hbm:ingots/any_rubber"),
                "F", tagIngredient("c:plates/iron")), "hbm:gas_mask_mono", 1),
                recipes, hbm("gas_mask_mono")));
        writes.add(save(output, foundryShaped(List.of("RRR"), Map.of(
                "R", itemIngredient("hbm:rag_damp")), "hbm:mask_rag", 1), recipes, hbm("mask_rag")));
        writes.add(save(output, foundryShaped(List.of("RRR"), Map.of(
                "R", itemIngredient("hbm:rag_piss")), "hbm:mask_piss", 1), recipes, hbm("mask_piss")));

        writes.add(save(output, foundryShaped(List.of("I", "F"), Map.of(
                "I", tagIngredient("c:plates/iron"),
                "F", itemIngredient("hbm:filter_coal")), "hbm:gas_mask_filter", 1),
                recipes, hbm("gas_mask_filter")));
        writes.add(save(output, foundryShaped(List.of("ZZZ", "ZCZ", "ZZZ"), Map.of(
                "Z", tagIngredient("c:nuggets/zirconium"),
                "C", itemIngredient("hbm:catalyst_clay")), "hbm:gas_mask_filter_mono", 1),
                recipes, hbm("gas_mask_filter_mono")));
        writes.add(save(output, foundryShaped(List.of("ZCZ", "CFC", "ZCZ"), Map.of(
                "Z", tagIngredient("c:ingots/zirconium"),
                "C", itemIngredient("hbm:catalyst_clay"),
                "F", itemIngredient("hbm:gas_mask_filter")), "hbm:gas_mask_filter_combo", 1),
                recipes, hbm("gas_mask_filter_combo")));
        writes.add(save(output, foundryShaped(List.of("I", "F"), Map.of(
                "I", tagIngredient("c:ingots/iron"),
                "F", itemIngredient("hbm:rag_damp")), "hbm:gas_mask_filter_rag", 1),
                recipes, hbm("gas_mask_filter_rag")));
        writes.add(save(output, foundryShaped(List.of("I", "F"), Map.of(
                "I", tagIngredient("c:ingots/iron"),
                "F", itemIngredient("hbm:rag_piss")), "hbm:gas_mask_filter_piss", 1),
                recipes, hbm("gas_mask_filter_piss")));
    }

    /** Four expensive ways to become M1TTY. */
    private void addEnvsuitRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        JsonObject titaniumPlate = tagIngredient("c:plates/titanium");
        JsonObject rubber = tagIngredient("c:ingots/rubber");
        JsonObject titaniumCastPlate = materialComponentIngredient("hbm:plate_cast", "titanium", 2200);

        writes.add(save(output, foundryShaped(List.of("TCT", "TGT", "RRR"), Map.of(
                "T", titaniumPlate,
                "C", customComponentIngredient("hbm:circuit", "type", "chip", 5),
                "G", tagIngredient("c:glass_panes"),
                "R", rubber), "hbm:envsuit_helmet", 1), recipes, hbm("envsuit_helmet")));
        writes.add(save(output, foundryShaped(List.of("T T", "TCT", "RRR"), Map.of(
                "T", titaniumPlate,
                "C", titaniumCastPlate,
                "R", rubber), "hbm:envsuit_plate", 1), recipes, hbm("envsuit_plate")));
        writes.add(save(output, foundryShaped(List.of("TCT", "R R", "T T"), Map.of(
                "T", titaniumPlate,
                "C", titaniumCastPlate,
                "R", rubber), "hbm:envsuit_legs", 1), recipes, hbm("envsuit_legs")));
        writes.add(save(output, foundryShaped(List.of("R R", "T T"), Map.of(
                "T", titaniumPlate,
                "R", rubber), "hbm:envsuit_boots", 1), recipes, hbm("envsuit_boots")));
    }

    private JsonObject catalystClayRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(tagIngredient("c:dusts/iron"));
        ingredients.add(itemIngredient("minecraft:clay_ball"));
        recipe.add("ingredients", ingredients);
        recipe.add("result", recipeResult("hbm:catalyst_clay", 1));
        return recipe;
    }

    private JsonObject armorRecipe(List<String> rows, String resultId) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "equipment");
        JsonArray pattern = new JsonArray();
        rows.forEach(pattern::add);
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("E", itemIngredient("hbm:hazmat_cloth"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:" + resultId);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject safetyFuseRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("SSS");
        pattern.add("SGS");
        pattern.add("SSS");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("S", itemIngredient("minecraft:string"));
        key.add("G", itemIngredient("minecraft:gunpowder"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 8);
        result.addProperty("id", "hbm:safety_fuse");
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject explosiveBlockRecipe(String stick, String resultId) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("DDD");
        pattern.add("DSD");
        pattern.add("DDD");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("D", itemIngredient("hbm:" + stick));
        key.add("S", itemIngredient("hbm:safety_fuse"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:" + resultId);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject parentModel(String parent) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "hbm:block/" + parent);
        return root;
    }

    private JsonObject chargeBlockState(String id) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        addChargeVariant(variants, "up", id, 0, 0);
        addChargeVariant(variants, "down", id, 180, 0);
        addChargeVariant(variants, "north", id, 90, 0);
        addChargeVariant(variants, "south", id, 270, 0);
        addChargeVariant(variants, "west", id, 90, 270);
        addChargeVariant(variants, "east", id, 90, 90);
        root.add("variants", variants);
        return root;
    }

    private void addChargeVariant(JsonObject variants, String facing, String id, int x, int y) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", "hbm:block/" + id);
        if (x != 0) variant.addProperty("x", x);
        if (y != 0) variant.addProperty("y", y);
        variants.add("facing=" + facing, variant);
    }

    private JsonObject emptyLoot() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        root.add("pools", new JsonArray());
        return root;
    }

    private JsonObject handheldItemModel(String texture) {
        JsonObject root = generatedItemModel(texture);
        root.addProperty("parent", "minecraft:item/handheld");
        return root;
    }

    private JsonObject ductTapeRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("F");
        pattern.add("P");
        pattern.add("S");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("F", itemIngredient("minecraft:string"));
        key.add("P", itemIngredient("minecraft:paper"));
        JsonObject slime = new JsonObject();
        slime.addProperty("tag", "c:slime_balls");
        key.add("S", slime);
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 4);
        result.addProperty("id", "hbm:ducttape");
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject chargeShapelessRecipe(String stick, String resultId) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("hbm:" + stick));
        ingredients.add(itemIngredient("hbm:" + stick));
        ingredients.add(itemIngredient("hbm:" + stick));
        ingredients.add(itemIngredient("hbm:ducttape"));
        recipe.add("ingredients", ingredients);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:" + resultId);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject chargeMinerRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add(" F ");
        pattern.add("FCF");
        pattern.add(" F ");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("F", itemIngredient("minecraft:flint"));
        key.add("C", itemIngredient("hbm:charge_dynamite"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:charge_miner");
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject rangefinderRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("G", tagIngredient("c:glass_panes"));
        key.put("R", tagIngredient("c:dusts/redstone"));
        key.put("C", customComponentIngredient("hbm:circuit", "type", "basic", 8));
        key.put("S", tagIngredient("c:plates/steel"));
        return shapedItemRecipe(List.of("GRC", "  S"), key, "hbm:rangefinder");
    }

    private JsonObject detonatorRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("C", customComponentIngredient("hbm:circuit", "type", "basic", 8));
        key.put("S", tagIngredient("c:plates/steel"));
        return shapedItemRecipe(List.of("C", "S"), key, "hbm:detonator");
    }

    private JsonObject multiDetonatorRecipe() {
        return shapelessItemRecipe(List.of(
                itemIngredient("hbm:detonator"),
                customComponentIngredient("hbm:circuit", "type", "advanced", 9)
        ), "hbm:detonator_multi");
    }

    private JsonObject laserDetonatorRecipe() {
        return shapelessItemRecipe(List.of(
                itemIngredient("hbm:rangefinder"),
                customComponentIngredient("hbm:circuit", "type", "advanced", 9),
                tagIngredient("c:ingots/rubber"),
                materialComponentIngredient("hbm:wire_dense", "gold", 7900)
        ), "hbm:detonator_laser");
    }

    private JsonObject deadMansDetonatorRecipe() {
        return shapelessItemRecipe(List.of(
                itemIngredient("hbm:detonator"),
                itemIngredient("hbm:defuser"),
                itemIngredient("hbm:ducttape")
        ), "hbm:detonator_deadman");
    }

    private JsonObject deadMansExplosiveRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("T", itemIngredient("minecraft:tnt"));
        key.put("D", itemIngredient("hbm:detonator_deadman"));
        return shapedItemRecipe(List.of("T", "D", "T"), key, "hbm:detonator_de");
    }

    private JsonObject shapedItemRecipe(List<String> rows, Map<String, JsonObject> keys, String resultId) {
        return shapedItemRecipe(rows, keys, resultId, 1);
    }

    private JsonObject dgkAmmoRecipe() {
        return shapedItemRecipe(List.of("LLL", "GGG", "CCC"), Map.of(
                "L", tagIngredient("c:plates/lead"),
                "G", itemIngredient("hbm:cordite"),
                "C", tagIngredient("c:ingots/copper")), "hbm:ammo_dgk");
    }

    private JsonObject barrelRecipe(JsonObject wall, JsonObject cap, String resultId) {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("I", wall);
        key.put("P", cap);
        return shapedItemRecipe(List.of("IPI", "I I", "IPI"), key, resultId);
    }

    private JsonObject shapedItemRecipe(List<String> rows, Map<String, JsonObject> keys, String resultId,
                                        int count) {
        JsonObject recipe = shapedBase(rows);
        JsonObject key = new JsonObject();
        keys.forEach(key::add);
        recipe.add("key", key);
        recipe.add("result", recipeResult(resultId, count));
        return recipe;
    }

    private JsonObject shapelessItemRecipe(List<JsonObject> inputs, String resultId) {
        return shapelessItemRecipe(inputs, resultId, 1);
    }

    private JsonObject shapelessItemRecipe(List<JsonObject> inputs, String resultId, int count) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        inputs.forEach(ingredients::add);
        recipe.add("ingredients", ingredients);
        recipe.add("result", recipeResult(resultId, count));
        return recipe;
    }

    private JsonObject chlorinePelletRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("minecraft:water_bucket"));
        ingredients.add(tagIngredient("c:dusts/glowstone"));
        ingredients.add(tagIngredient("c:plates/steel"));
        recipe.add("ingredients", ingredients);
        recipe.add("result", recipeResult("hbm:pellet_gas", 2));
        return recipe;
    }

    private JsonObject simpleItemResult(String id) {
        JsonObject result = new JsonObject();
        result.addProperty("id", id);
        result.addProperty("count", 1);
        return result;
    }

    private JsonObject earlyBatterySocketRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "redstone");
        JsonArray pattern = new JsonArray();
        pattern.add("PRP");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("P", itemIngredient("hbm:plate_steel"));
        key.add("R", itemIngredient("hbm:ingot_red_copper"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:machine_battery_socket");
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject transformerRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "redstone");
        JsonArray pattern = new JsonArray();
        pattern.add("SCS"); pattern.add("MDM"); pattern.add("SCS");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("S", tagIngredient("c:ingots/iron"));
        key.add("C", customComponentIngredient("hbm:circuit", "type", "capacitor", 1));
        key.add("M", itemIngredient("hbm:coil_copper"));
        key.add("D", tagIngredient("c:ingots/mingrade"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:machine_transformer"); result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject graphiteElectrodeRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("C"); pattern.add("T"); pattern.add("C");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("C", tagIngredient("c:ingots/graphite"));
        key.add("T", materialComponentOrExternalTagIngredient(
                "hbm:bolt", "steel", 30, "c:bolts/steel"));
        recipe.add("key", key);
        JsonObject customData = new JsonObject(); customData.addProperty("type", "graphite");
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", 0);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:arc_electrode"); result.addProperty("count", 1);
        result.add("components", components);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject batteryPackRecipe(String type, String plate, String core) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "redstone");
        JsonArray pattern = new JsonArray();
        pattern.add("IRI");
        pattern.add("PRP");
        pattern.add("IRI");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("I", itemIngredient(plate));
        key.add("R", itemIngredient(core));
        key.add("P", itemIngredient("hbm:plate_polymer"));
        recipe.add("key", key);
        JsonObject customData = new JsonObject();
        customData.addProperty("type", type);
        customData.addProperty("charge", 0L);
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:battery_pack");
        result.addProperty("count", 1);
        result.add("components", components);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject armorTableRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("PPP");
        pattern.add("TCT");
        pattern.add("TST");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("P", itemIngredient("hbm:plate_steel"));
        key.add("T", itemIngredient("hbm:ingot_tungsten"));
        key.add("C", itemIngredient("minecraft:crafting_table"));
        key.add("S", itemIngredient("hbm:block_steel"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:machine_armor_table");
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject weaponTableRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("P", tagIngredient("c:plates/gunmetal"));
        key.put("T", tagIngredient("c:ingots/steel"));
        key.put("C", itemIngredient("minecraft:crafting_table"));
        key.put("S", tagIngredient("c:storage_blocks/steel"));
        return shapedItemRecipe(List.of("PPP", "TCT", "TST"), key, "hbm:machine_weapon_table");
    }

    private JsonObject silencerRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("P", tagIngredient("c:ingots/polymer"));
        key.put("B", foundryPart("part_barrel_light", "steel", 30));
        return shapedItemRecipe(List.of("P", "B", "P"), key, "hbm:weapon_mod_silencer");
    }

    private JsonObject speedloaderRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("B", materialComponentIngredient("hbm:bolt", "steel", 30));
        key.put("S", itemIngredient("hbm:plate_weaponsteel"));
        return shapedItemRecipe(List.of(" B ", "BSB", " B "), key, "hbm:weapon_mod_speedloader");
    }

    private JsonObject scopeRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("P", tagIngredient("c:ingots/polymer"));
        key.put("S", tagIngredient("c:plates/steel"));
        key.put("G", tagIngredient("c:glass_panes"));
        return shapedItemRecipe(List.of("SPS", "G G", "SPS"), key, "hbm:weapon_mod_scope");
    }

    private JsonObject sawRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("B", materialComponentIngredient("hbm:bolt", "steel", 30));
        key.put("S", tagIngredient("c:rods/wooden"));
        key.put("H", tagIngredient("c:plates/dura_steel"));
        return shapedItemRecipe(List.of("BBS", "BHS"), key, "hbm:weapon_mod_saw");
    }

    private JsonObject greaseGunKitRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("B", foundryPart("part_barrel_light", "weapon_steel", 50));
        key.put("R", foundryPart("part_receiver_light", "weapon_steel", 50));
        key.put("M", foundryPart("part_mechanism", "weapon_steel", 50));
        key.put("P", tagIngredient("c:plates/dura_steel"));
        key.put("G", foundryPart("part_grip", "polymer", 20_001));
        return shapedItemRecipe(List.of("BRM", "P G"), key, "hbm:weapon_mod_grease_gun");
    }

    private JsonObject slowdownRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("I", tagIngredient("c:ingots/weapon_steel"));
        key.put("M", foundryPart("part_mechanism", "weapon_steel", 50));
        return shapedItemRecipe(List.of(" I ", " M ", "I I"), key, "hbm:weapon_mod_slowdown");
    }

    private JsonObject baseAnvilRecipe(String material) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("III");
        pattern.add(" B ");
        pattern.add("III");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("I", tagIngredient("c:ingots/" + material));
        key.add("B", tagIngredient("c:storage_blocks/" + material));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:anvil_" + material);
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject obsidianCladdingRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("OOO");
        pattern.add("PDP");
        pattern.add("OOO");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("O", itemIngredient("minecraft:obsidian"));
        key.add("P", itemIngredient("hbm:plate_steel"));
        key.add("D", itemIngredient("hbm:ducttape"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:cladding_obsidian");
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject rubberCladdingRecipe() {
        JsonObject recipe = shapedBase(List.of("RCR", "CDC", "RCR"));
        JsonObject key = new JsonObject();
        key.add("R", tagIngredient("hbm:ingots/any_rubber"));
        key.add("C", tagIngredient("c:dusts/coal"));
        key.add("D", itemIngredient("hbm:ducttape"));
        recipe.add("key", key);
        recipe.add("result", itemResult("hbm:cladding_rubber", 1));
        return recipe;
    }

    private JsonObject leadCladdingRecipe() {
        return layeredCladdingRecipe("hbm:cladding_rubber", tagIngredient("c:plates/lead"),
                "hbm:cladding_lead");
    }

    private JsonObject deshCladdingRecipe() {
        return layeredCladdingRecipe("hbm:cladding_lead", itemIngredient("hbm:plate_desh"),
                "hbm:cladding_desh");
    }

    private JsonObject layeredCladdingRecipe(String core, JsonObject plate, String output) {
        JsonObject recipe = shapedBase(List.of("DPD", "PRP", "DPD"));
        JsonObject key = new JsonObject();
        key.add("D", itemIngredient("hbm:ducttape"));
        key.add("P", plate);
        key.add("R", itemIngredient(core));
        recipe.add("key", key);
        recipe.add("result", itemResult(output, 1));
        return recipe;
    }

    private JsonObject insulatorStringWoolRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("SWS");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("S", itemIngredient("minecraft:string"));
        key.add("W", tagIngredient("minecraft:wool"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:plate_polymer");
        result.addProperty("count", 4);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject insulatorTwoItemRecipe(String ingredient, int count) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("BB");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("B", itemIngredient(ingredient));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:plate_polymer");
        result.addProperty("count", count);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject insulatorTwoTagRecipe(String ingredient, int count) {
        JsonObject recipe = insulatorTwoItemRecipe("minecraft:air", count);
        recipe.getAsJsonObject("key").add("B", tagIngredient(ingredient));
        return recipe;
    }

    private JsonObject latexRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("DD");
        pattern.add("DD");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("D", itemIngredient("minecraft:dandelion"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:ball_resin");
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject latexSmeltingRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:smelting");
        recipe.addProperty("category", "misc");
        recipe.add("ingredient", itemIngredient("hbm:ball_resin"));
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:ingot_biorubber");
        recipe.add("result", result);
        recipe.addProperty("experience", 0.1F);
        recipe.addProperty("cookingtime", 200);
        return recipe;
    }

    private JsonObject ironCladdingRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("OOO");
        pattern.add("PDP");
        pattern.add("OOO");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("O", itemIngredient("hbm:plate_iron"));
        key.add("P", itemIngredient("hbm:plate_polymer"));
        key.add("D", itemIngredient("hbm:ducttape"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:cladding_iron");
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject insulatorBlockCompressionRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "building");
        JsonArray pattern = new JsonArray();
        pattern.add("###");
        pattern.add("###");
        pattern.add("###");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("#", itemIngredient("hbm:plate_polymer"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:block_insulator");
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject insulatorBlockDecompressionRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("#");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("#", itemIngredient("hbm:block_insulator"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:plate_polymer");
        result.addProperty("count", 9);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject sandbagsRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "building");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("hbm:plate_polymer"));
        ingredients.add(tagIngredient("minecraft:sand"));
        ingredients.add(tagIngredient("minecraft:sand"));
        ingredients.add(tagIngredient("minecraft:sand"));
        recipe.add("ingredients", ingredients);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:sandbags");
        result.addProperty("count", 4);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject circuitModel() {
        JsonObject root = generatedItemModel("circuit.vacuum_tube");
        JsonArray overrides = new JsonArray();
        int[] metadata = {1, 2, 3, 4, 5, 7, 8, 9, 10, 12, 19};
        String[] names = {"capacitor", "capacitor_tantalium", "pcb", "silicon", "chip", "analog", "basic",
                "advanced", "capacitor_board", "controller_chassis", "numitron"};
        for (int i = 0; i < metadata.length; i++) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", metadata[i]);
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/circuit_" + names[i]);
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject wireFineModel() {
        JsonObject root = generatedItemModel("wire_fine");
        JsonArray overrides = new JsonArray();
        List<WireData> variants = List.of(
                new WireData("steel", 30, "", "", false),
                new WireData("red_copper", 31, "", "", false),
                new WireData("carbon", 699, "", "", false),
                new WireData("aluminium", 1300, "", "", false),
                new WireData("copper", 2900, "", "", false),
                new WireData("zirconium", 4000, "", "", false),
                new WireData("tungsten", 7400, "", "", false),
                new WireData("gold", 7900, "", "", false),
                new WireData("lead", 8200, "", "", false)
        );
        for (WireData variant : variants) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", variant.metadata());
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/wire_fine_" + variant.id());
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject crossBlockModel(String texture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cross");
        root.addProperty("render_type", "cutout");
        JsonObject textures = new JsonObject();
        textures.addProperty("cross", "hbm:block/" + texture);
        root.add("textures", textures);
        return root;
    }

    private JsonObject plantDeadBlockState() {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (int variant = 0; variant < 5; variant++) {
            JsonObject model = new JsonObject();
            model.addProperty("model", "hbm:block/plant_dead_" + variant);
            variants.add("variant=" + variant, model);
        }
        root.add("variants", variants);
        return root;
    }

    private JsonObject canisterFullModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "hbm:item/canister_empty");
        textures.addProperty("layer1", "hbm:item/canister_overlay");
        root.add("textures", textures);
        return root;
    }

    private JsonObject gasFullModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "hbm:item/gas_empty");
        textures.addProperty("layer1", "hbm:item/gas_bottle");
        textures.addProperty("layer2", "hbm:item/gas_label");
        root.add("textures", textures);
        return root;
    }

    private JsonObject derrickItemModel() {
        JsonObject root = parentItemModel("hbm:block/derrick");
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(List.of(30, 315, 0),
                List.of(0, -5.344913, -0.706441),
                List.of(0.09375, 0.09375, 0.09375)));
        display.add("ground", displayTransform(List.of(0, 0, 0), List.of(0, 2, 0),
                List.of(0.09375, 0.09375, 0.09375)));
        display.add("fixed", displayTransform(List.of(0, 180, 0), List.of(0, 0, 0),
                List.of(0.125, 0.125, 0.125)));
        display.add("firstperson_righthand", displayTransform(List.of(0, 45, 0), List.of(0, 0, 0),
                List.of(0.125, 0.125, 0.125)));
        display.add("thirdperson_righthand", displayTransform(List.of(75, 45, 0), List.of(0, 0, 0),
                List.of(0.125, 0.125, 0.125)));
        root.add("display", display);
        return root;
    }

    private JsonObject fluidStorageTankItemModel() {
        JsonObject root = parentItemModel("builtin/entity");
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(List.of(30, 225, 0), List.of(0, -2, 0),
                List.of(0.0625, 0.0625, 0.0625)));
        display.add("ground", displayTransform(List.of(0, 0, 0), List.of(0, 2, 0),
                List.of(0.16, 0.16, 0.16)));
        display.add("fixed", displayTransform(List.of(0, 90, 0), List.of(0, 0, 0),
                List.of(0.16, 0.16, 0.16)));
        display.add("firstperson_righthand", displayTransform(List.of(0, 135, 0), List.of(0, 1, 0),
                List.of(0.16, 0.16, 0.16)));
        display.add("thirdperson_righthand", displayTransform(List.of(75, 135, 0), List.of(0, 2, 0),
                List.of(0.16, 0.16, 0.16)));
        root.add("display", display);
        return root;
    }

    private JsonObject industrialTurbineItemModel() {
        JsonObject root = parentItemModel("builtin/entity");
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(List.of(30, 225, 0), List.of(0, -2, 0),
                List.of(0.0625, 0.0625, 0.0625)));
        display.add("ground", displayTransform(List.of(0, 0, 0), List.of(0, 2, 0),
                List.of(0.14, 0.14, 0.14)));
        display.add("fixed", displayTransform(List.of(0, 90, 0), List.of(0, 0, 0),
                List.of(0.14, 0.14, 0.14)));
        display.add("thirdperson_righthand", displayTransform(List.of(75, 135, 0), List.of(0, 2.5, 0),
                List.of(0.14, 0.14, 0.14)));
        display.add("firstperson_righthand", displayTransform(List.of(0, 135, 0), List.of(0, 1, 0),
                List.of(0.14, 0.14, 0.14)));
        root.add("display", display);
        return root;
    }

    private JsonObject highPowerCondenserItemModel() {
        JsonObject root = parentItemModel("builtin/entity");
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(List.of(30, 225, 0), List.of(0, -2, 0),
                List.of(0.0625, 0.0625, 0.0625)));
        display.add("ground", displayTransform(List.of(0, 90, 0), List.of(0, 2, 0),
                List.of(0.25, 0.25, 0.25)));
        display.add("fixed", displayTransform(List.of(0, 90, 0), List.of(0, 0, 0),
                List.of(0.25, 0.25, 0.25)));
        display.add("thirdperson_righthand", displayTransform(List.of(75, 135, 0), List.of(0, 2.5, 0),
                List.of(0.25, 0.25, 0.25)));
        display.add("firstperson_righthand", displayTransform(List.of(0, 135, 0), List.of(0, 1, 0),
                List.of(0.25, 0.25, 0.25)));
        root.add("display", display);
        return root;
    }

    /** Turbofan inventory yoga. */
    private JsonObject turbofanItemModel() {
        JsonObject root = parentItemModel("builtin/entity");
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(List.of(30, 225, 0), List.of(0, -2, 0),
                List.of(0.0625, 0.0625, 0.0625)));
        display.add("ground", displayTransform(List.of(0, 90, 0), List.of(0, 0, 0),
                List.of(0.375, 0.375, 0.375)));
        display.add("fixed", displayTransform(List.of(0, 90, 0), List.of(0, 0, 0),
                List.of(0.25, 0.25, 0.25)));
        display.add("thirdperson_righthand", displayTransform(List.of(0, 0, 0), List.of(8, 4, 0),
                List.of(0.25, 0.25, 0.25)));
        display.add("firstperson_righthand", displayTransform(List.of(0, 90, 0), List.of(8, 4, 0),
                List.of(0.25, 0.25, 0.25)));
        root.add("display", display);
        return root;
    }

    /** Same ancient item camera, now with 75% more turbine. */
    private JsonObject gasTurbineItemModel() {
        JsonObject root = parentItemModel("builtin/entity");
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(List.of(30, 225, 0), List.of(0, -2, 0),
                List.of(0.0625, 0.0625, 0.0625)));
        display.add("ground", displayTransform(List.of(0, 90, 0), List.of(0, 0, 0),
                List.of(0.375, 0.375, 0.375)));
        display.add("fixed", displayTransform(List.of(0, 90, 0), List.of(0, 0, 0),
                List.of(0.25, 0.25, 0.25)));
        display.add("thirdperson_righthand", displayTransform(List.of(0, 0, 0), List.of(8, 4, 0),
                List.of(0.125, 0.125, 0.125)));
        display.add("firstperson_righthand", displayTransform(List.of(0, 90, 0), List.of(8, 4, 0),
                List.of(0.125, 0.125, 0.125)));
        root.add("display", display);
        return root;
    }

    /** Shared camera; renderers bring their own bad posture. */
    private JsonObject legacyBuiltinEntityItemModel() {
        JsonObject root = parentItemModel("builtin/entity");
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(List.of(30, 225, 0), List.of(0, -2, 0),
                List.of(0.0625, 0.0625, 0.0625)));
        root.add("display", display);
        return root;
    }

    private JsonObject ammoShellItemModel() {
        JsonObject root = generatedItemModel("ammo_shell");
        JsonArray overrides = new JsonArray();
        for (com.hbm.ntm.weapon.TurretShellAmmoType shell
                : com.hbm.ntm.weapon.TurretShellAmmoType.values()) {
            if (shell.ordinal() == 0) continue;
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", shell.ordinal());
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/" + shell.serializedName());
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject artilleryAmmoItemModel() {
        JsonObject root = generatedItemModel("ammo_arty");
        JsonArray overrides = new JsonArray();
        for (com.hbm.ntm.weapon.ArtilleryAmmoType ammo
                : com.hbm.ntm.weapon.ArtilleryAmmoType.values()) {
            if (ammo.ordinal() == 0) continue;
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", ammo.legacyMetadata());
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/" + ammo.serializedName());
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject himarsAmmoItemModel() {
        JsonObject root = generatedItemModel("ammo_himars_standard");
        JsonArray overrides = new JsonArray();
        for (com.hbm.ntm.weapon.HimarsAmmoType ammo : com.hbm.ntm.weapon.HimarsAmmoType.values()) {
            if (ammo.ordinal() == 0) continue;
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", ammo.legacyMetadata());
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/" + ammo.serializedName());
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject fluidStorageTankEmptyModel() {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/machine_fluidtank");
        root.add("textures", textures);
        return root;
    }

    private JsonObject crucibleItemModel() {
        JsonObject root = parentItemModel("hbm:block/crucible");
        JsonObject display = new JsonObject();
        // Machine crucible, not sword crucible. Naming was invented yesterday.
        display.add("gui", displayTransform(List.of(30, 225, 0),
                List.of(-2.298097, -1.891747, 0.0625),
                List.of(0.203125, 0.203125, 0.203125)));
        display.add("ground", displayTransform(List.of(0, 0, 0), List.of(0, 3, 0),
                List.of(0.25, 0.25, 0.25)));
        display.add("fixed", displayTransform(List.of(0, 180, 0), List.of(0, 0, 0),
                List.of(0.25, 0.25, 0.25)));
        display.add("firstperson_righthand", displayTransform(List.of(0, 45, 0), List.of(0, 0, 0),
                List.of(0.25, 0.25, 0.25)));
        display.add("thirdperson_righthand", displayTransform(List.of(75, 45, 0), List.of(0, 0, 0),
                List.of(0.25, 0.25, 0.25)));
        root.add("display", display);
        return root;
    }

    private JsonObject displayTransform(List<? extends Number> rotation, List<? extends Number> translation,
                                        List<? extends Number> scale) {
        JsonObject transform = new JsonObject();
        JsonArray rotationArray = new JsonArray(); rotation.forEach(rotationArray::add);
        JsonArray translationArray = new JsonArray(); translation.forEach(translationArray::add);
        JsonArray scaleArray = new JsonArray(); scale.forEach(scaleArray::add);
        transform.add("rotation", rotationArray);
        transform.add("translation", translationArray);
        transform.add("scale", scaleArray);
        return transform;
    }

    /** The camera angle explosives demand. */
    private JsonObject chargeItemModel(String id) {
        JsonObject root = blockItemModel(id);
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(List.of(30, 225, -90),
                List.of(-2.651650, -1.325825, 2.296397),
                List.of(0.625, 0.625, 0.625)));
        root.add("display", display);
        return root;
    }

    private JsonObject sourceContainerRecipe(boolean canister) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("S "); pattern.add("AA"); pattern.add("AA");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("S", tagIngredient(canister ? "c:plates/steel" : "c:plates/copper"));
        key.add("A", tagIngredient(canister ? "c:plates/aluminium" : "c:plates/steel"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", canister ? "hbm:canister_empty" : "hbm:gas_empty");
        result.addProperty("count", 2);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject generatedItemModel(String texture) {
        return generatedItemModel(texture, false);
    }

    private JsonObject generatedItemModel(String texture, boolean blockTexture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "hbm:" + (blockTexture ? "block/" : "item/") + texture);
        root.add("textures", textures);
        return root;
    }

    private JsonObject sirenTrackItemModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "hbm:item/cassette");
        textures.addProperty("layer1", "hbm:item/cassette_overlay");
        root.add("textures", textures);
        return root;
    }

    private JsonObject guideBookRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("minecraft:book"));
        ingredients.add(itemIngredient("minecraft:iron_ingot"));
        recipe.add("ingredients", ingredients);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:book_guide");
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    /** Eight titanium blades bullying one steel ingot. */
    private JsonObject turbineTitaniumRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("BBB");
        pattern.add("BSB");
        pattern.add("BBB");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("B", itemIngredient("hbm:blade_titanium"));
        key.add("S", tagIngredient("c:ingots/steel"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:turbine_titanium");
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject cubeAllBlockModel(String id) {
        return cubeAllBlockModel(id, id);
    }

    private JsonObject cubeAllBlockModel(String id, String texture) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube_all");
        JsonObject textures = new JsonObject();
        textures.addProperty("all", "hbm:block/" + texture);
        root.add("textures", textures);
        return root;
    }

    private JsonObject cutoutCubeAllBlockModel(String id) {
        JsonObject root = cubeAllBlockModel(id);
        root.addProperty("render_type", "cutout");
        return root;
    }

    private JsonObject reinforcedGlassPaneModel(String part) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/template_glass_pane_" + part);
        root.addProperty("render_type", "cutout");
        JsonObject textures = new JsonObject();
        textures.addProperty("pane", "hbm:block/reinforced_glass_pane");
        textures.addProperty("edge", "hbm:block/reinforced_glass_pane_edge");
        root.add("textures", textures);
        return root;
    }

    private JsonObject translucentCubeAllBlockModel(String id) {
        JsonObject root = cubeAllBlockModel(id);
        root.addProperty("render_type", "translucent");
        return root;
    }

    private JsonObject cubeColumnBlockModel(String id, String side, String end) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube_column");
        JsonObject textures = new JsonObject();
        textures.addProperty("side", "hbm:block/" + side);
        textures.addProperty("end", "hbm:block/" + end);
        root.add("textures", textures);
        return root;
    }

    private JsonObject cubeBottomTopBlockModel(String id, String side, String bottom, String top) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube_bottom_top");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/" + side);
        textures.addProperty("side", "hbm:block/" + side);
        textures.addProperty("bottom", "hbm:block/" + bottom);
        textures.addProperty("top", "hbm:block/" + top);
        root.add("textures", textures);
        return root;
    }

    private JsonObject steamCondenserRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("SIS"); pattern.add("ICI"); pattern.add("SIS");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("S", tagIngredient("c:ingots/steel"));
        key.add("I", tagIngredient("c:plates/iron"));
        key.add("C", materialComponentIngredient("hbm:plate_cast", "copper", 2900));
        root.add("key", key);
        root.add("result", recipeResult("hbm:machine_condenser", 1));
        return root;
    }

    private JsonObject electricFurnaceBlockModel(boolean lit) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/orientable_with_bottom");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/machine_electric_furnace_side");
        textures.addProperty("bottom", "hbm:block/machine_electric_furnace_bottom");
        textures.addProperty("top", "hbm:block/machine_electric_furnace_top");
        textures.addProperty("side", "hbm:block/machine_electric_furnace_side");
        textures.addProperty("front", "hbm:block/machine_electric_furnace_front_" + (lit ? "on" : "off"));
        root.add("textures", textures);
        return root;
    }

    private JsonObject electricFurnaceBlockState() {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        Map<String, Integer> rotations = Map.of("north", 0, "east", 90, "south", 180, "west", 270);
        for (boolean lit : List.of(false, true)) for (String facing : List.of("north", "east", "south", "west")) {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", "hbm:block/machine_electric_furnace_" + (lit ? "on" : "off"));
            int rotation = rotations.get(facing);
            if (rotation != 0) variant.addProperty("y", rotation);
            variants.add("facing=" + facing + ",lit=" + lit, variant);
        }
        root.add("variants", variants);
        return root;
    }

    private JsonObject electricFurnaceRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("BBB"); pattern.add("WFW"); pattern.add("RRR");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("B", tagIngredient("c:ingots/beryllium"));
        key.add("W", materialComponentIngredient("hbm:plate_cast", "copper", 2900));
        key.add("F", itemIngredient("minecraft:furnace"));
        key.add("R", itemIngredient("hbm:coil_tungsten"));
        root.add("key", key);
        root.add("result", recipeResult("hbm:machine_electric_furnace_off", 1));
        return root;
    }

    private JsonObject brickFurnaceBlockModel(boolean lit) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/orientable_with_bottom");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/machine_furnace_brick_side");
        textures.addProperty("bottom", "hbm:block/machine_furnace_brick_bottom");
        textures.addProperty("top", "hbm:block/machine_furnace_brick_top");
        textures.addProperty("side", "hbm:block/machine_furnace_brick_side");
        textures.addProperty("front", "hbm:block/machine_furnace_brick_front_" + (lit ? "on" : "off"));
        root.add("textures", textures);
        return root;
    }

    private JsonObject brickFurnaceBlockState() {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        Map<String, Integer> rotations = Map.of("north", 0, "east", 90, "south", 180, "west", 270);
        for (boolean lit : List.of(false, true)) for (String facing : List.of("north", "east", "south", "west")) {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", "hbm:block/machine_furnace_brick_" + (lit ? "on" : "off"));
            int rotation = rotations.get(facing);
            if (rotation != 0) variant.addProperty("y", rotation);
            variants.add("facing=" + facing + ",lit=" + lit, variant);
        }
        root.add("variants", variants);
        return root;
    }

    private JsonObject brickFurnaceRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("III"); pattern.add("I I"); pattern.add("BBB");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("I", itemIngredient("minecraft:brick"));
        key.add("B", itemIngredient("minecraft:stone"));
        root.add("key", key);
        root.add("result", recipeResult("hbm:machine_furnace_brick_off", 1));
        return root;
    }

    private JsonObject woodBurnerRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("PPP"); pattern.add("CFC"); pattern.add("I I");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("P", tagIngredient("c:plates/steel"));
        key.add("C", itemIngredient("hbm:coil_copper"));
        key.add("F", itemIngredient("minecraft:furnace"));
        key.add("I", tagIngredient("c:ingots/iron"));
        root.add("key", key);
        root.add("result", recipeResult("hbm:machine_wood_burner", 1));
        return root;
    }

    private JsonObject microwaveRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("III"); pattern.add("SGM"); pattern.add("IDI");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("I", itemIngredient("hbm:plate_polymer"));
        key.add("S", tagIngredient("c:plates/steel"));
        key.add("G", tagIngredient("c:glass_panes"));
        key.add("M", itemIngredient("hbm:magnetron"));
        key.add("D", itemIngredient("hbm:motor"));
        root.add("key", key);
        root.add("result", recipeResult("hbm:machine_microwave", 1));
        return root;
    }

    private JsonObject converterHEFErecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("RRR"); pattern.add("WWW"); pattern.add("III");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("R", customComponentIngredient("hbm:circuit", "type", "capacitor", 1));
        key.add("W",  itemIngredient("minecraft:redstone"));
        key.add("I", tagIngredient("c:ingots/steel"));
        root.add("key", key);
        root.add("result", recipeResult("hbm:machine_converter_he_fe", 1));
        return root;
    }

    private JsonObject converterFEHErecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("RRR"); pattern.add("WWW"); pattern.add("III");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("R", itemIngredient("minecraft:redstone"));
        key.add("W", materialComponentOrExternalTagIngredient(
                "hbm:wire_fine", "red_copper", 31, "c:wires/fine/red_copper"));
        key.add("I", tagIngredient("c:ingots/steel"));
        root.add("key", key);
        root.add("result", recipeResult("hbm:machine_converter_fe_he", 1));
        return root;
    }

    private JsonObject fireclayFromAluminumDustRecipe() {
        return fireclayRecipe(List.of(
                itemIngredient("minecraft:clay_ball"),
                itemIngredient("minecraft:clay_ball"),
                itemIngredient("minecraft:clay_ball"),
                tagIngredient("c:dusts/aluminum")));
    }

    private JsonObject fireclayFromAluminumOreRecipe() {
        return fireclayRecipe(List.of(
                itemIngredient("minecraft:clay_ball"),
                itemIngredient("minecraft:clay_ball"),
                itemIngredient("minecraft:clay_ball"),
                tagIngredient("c:ores/aluminum")));
    }

    private JsonObject fireclayFromLimestoneRecipe() {
        return fireclayRecipe(List.of(
                itemIngredient("minecraft:clay_ball"),
                itemIngredient("minecraft:clay_ball"),
                stoneResourceIngredient("limestone", 4),
                tagIngredient("minecraft:sand")));
    }

    private JsonObject fireclayRecipe(List<JsonObject> sourceIngredients) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        sourceIngredients.forEach(ingredients::add);
        root.add("ingredients", ingredients);
        root.add("result", recipeResult("hbm:ball_fireclay", 4));
        return root;
    }

    private JsonObject stoneResourceIngredient(String type, int metadata) {
        JsonObject state = new JsonObject();
        state.addProperty("type", type);
        JsonObject components = new JsonObject();
        components.add("minecraft:block_state", state);
        components.addProperty("minecraft:custom_model_data", metadata);
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("type", "neoforge:components");
        ingredient.addProperty("items", "hbm:stone_resource");
        ingredient.add("components", components);
        ingredient.addProperty("strict", true);
        return ingredient;
    }

    private JsonObject transformerBlockModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/machine_transformer_iron");
        textures.addProperty("north", "hbm:block/machine_transformer_iron");
        textures.addProperty("south", "hbm:block/machine_transformer_iron");
        textures.addProperty("east", "hbm:block/machine_transformer_iron");
        textures.addProperty("west", "hbm:block/machine_transformer_iron");
        textures.addProperty("up", "hbm:block/machine_transformer_top_iron");
        textures.addProperty("down", "hbm:block/machine_transformer_top_iron");
        root.add("textures", textures);
        return root;
    }

    /** Crane inventory face, rescued from metadata zero. */
    private JsonObject craneInventoryBlockModel(String type) {
        String output = type.equals("boxer") ? "crane_box" : "crane_out";
        String side = switch (type) {
            case "extractor" -> "crane_out_side_down";
            case "inserter" -> "crane_in_side_up";
            case "boxer" -> "crane_boxer_side_up";
            default -> throw new IllegalArgumentException("Unknown crane inventory model: " + type);
        };

        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/crane_side");
        textures.addProperty("down", "hbm:block/" + output);
        textures.addProperty("up", "hbm:block/crane_in");
        for (String face : List.of("north", "south", "west", "east")) {
            textures.addProperty(face, "hbm:block/" + side);
        }
        root.add("textures", textures);
        return root;
    }

    private JsonObject boxBlockModel(String texture, int minX, int minY, int minZ,
                                     int maxX, int maxY, int maxZ) {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/" + texture);
        textures.addProperty("all", "hbm:block/" + texture);
        root.add("textures", textures);
        JsonObject element = new JsonObject();
        JsonArray from = new JsonArray();
        from.add(minX); from.add(minY); from.add(minZ);
        JsonArray to = new JsonArray();
        to.add(maxX); to.add(maxY); to.add(maxZ);
        element.add("from", from);
        element.add("to", to);
        JsonObject faces = new JsonObject();
        for (String face : List.of("down", "up", "north", "south", "west", "east")) {
            JsonObject faceData = new JsonObject();
            faceData.addProperty("texture", "#all");
            faces.add(face, faceData);
        }
        element.add("faces", faces);
        JsonArray elements = new JsonArray();
        elements.add(element);
        root.add("elements", elements);
        return root;
    }

    private JsonObject sandbagsBlockState() {
        JsonObject root = new JsonObject();
        JsonArray multipart = new JsonArray();
        JsonObject core = new JsonObject();
        JsonObject coreApply = new JsonObject();
        coreApply.addProperty("model", "hbm:block/sandbags_core");
        core.add("apply", coreApply);
        multipart.add(core);
        for (String direction : List.of("north", "east", "south", "west")) {
            JsonObject part = new JsonObject();
            JsonObject when = new JsonObject();
            when.addProperty(direction, "true");
            part.add("when", when);
            JsonObject apply = new JsonObject();
            apply.addProperty("model", "hbm:block/sandbags_" + direction);
            part.add("apply", apply);
            multipart.add(part);
        }
        root.add("multipart", multipart);
        return root;
    }

    private JsonObject insulatorBlockModel(boolean horizontal) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", horizontal ? "minecraft:block/cube_column_horizontal" : "minecraft:block/cube_column");
        JsonObject textures = new JsonObject();
        textures.addProperty("end", "hbm:block/block_insulator_top");
        textures.addProperty("side", "hbm:block/block_insulator_side");
        root.add("textures", textures);
        return root;
    }

    private JsonObject rotatedPillarBlockState(String id) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject vertical = new JsonObject();
        vertical.addProperty("model", "hbm:block/" + id);
        variants.add("axis=y", vertical);
        JsonObject eastWest = new JsonObject();
        eastWest.addProperty("model", "hbm:block/" + id + "_horizontal");
        eastWest.addProperty("x", 90);
        eastWest.addProperty("y", 90);
        variants.add("axis=x", eastWest);
        JsonObject northSouth = new JsonObject();
        northSouth.addProperty("model", "hbm:block/" + id + "_horizontal");
        northSouth.addProperty("x", 90);
        variants.add("axis=z", northSouth);
        root.add("variants", variants);
        return root;
    }

    private JsonObject armorTableBlockModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube");
        JsonObject textures = new JsonObject();
        textures.addProperty("down", "hbm:block/armor_table_bottom");
        textures.addProperty("up", "hbm:block/armor_table_top");
        textures.addProperty("north", "hbm:block/armor_table_side");
        textures.addProperty("south", "hbm:block/armor_table_side");
        textures.addProperty("west", "hbm:block/armor_table_side");
        textures.addProperty("east", "hbm:block/armor_table_side");
        textures.addProperty("particle", "hbm:block/armor_table_side");
        root.add("textures", textures);
        return root;
    }

    private JsonObject weaponTableBlockModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube");
        JsonObject textures = new JsonObject();
        textures.addProperty("down", "hbm:block/gun_table_bottom");
        textures.addProperty("up", "hbm:block/gun_table_top");
        for (String face : List.of("north", "south", "west", "east")) {
            textures.addProperty(face, "hbm:block/gun_table_side");
        }
        textures.addProperty("particle", "hbm:block/gun_table_side");
        root.add("textures", textures);
        return root;
    }

    private JsonObject emptyModel(String particleTexture) {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/" + particleTexture);
        root.add("textures", textures);
        return root;
    }

    private JsonObject parentItemModel(String parent) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", parent);
        return root;
    }

    private JsonObject falloutBlockModel() {
        JsonObject root = new JsonObject();
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/fallout");
        textures.addProperty("all", "hbm:block/fallout");
        root.add("textures", textures);
        JsonObject element = new JsonObject();
        JsonArray from = new JsonArray(); from.add(0); from.add(0); from.add(0);
        JsonArray to = new JsonArray(); to.add(16); to.add(2); to.add(16);
        element.add("from", from); element.add("to", to);
        JsonObject faces = new JsonObject();
        for (String face : List.of("down", "up", "north", "south", "west", "east")) {
            JsonObject data = new JsonObject();
            data.addProperty("texture", "#all");
            faces.add(face, data);
        }
        element.add("faces", faces);
        JsonArray elements = new JsonArray(); elements.add(element);
        root.add("elements", elements);
        return root;
    }

    private JsonObject blockItemModel(String id) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "hbm:block/" + id);
        return root;
    }

    private JsonObject legacyBlockItemModel(String id, List<? extends Number> rotation,
                                            List<? extends Number> translation,
                                            List<? extends Number> scale) {
        JsonObject root = blockItemModel(id);
        JsonObject display = new JsonObject();
        display.add("gui", displayTransform(rotation, translation, scale));
        root.add("display", display);
        return root;
    }

    /** Old inventory camera. New defaults point the machines the wrong damn way. */
    private JsonObject legacyStandardBlockItemModel(String id) {
        return legacyBlockItemModel(id, List.of(30, 315, 0), List.of(0, 0, 0),
                List.of(0.625, 0.625, 0.625));
    }

    private JsonObject nukeManBlockState() {
        return directionalEmptyBlockState("nuke_man");
    }

    private JsonObject directionalEmptyBlockState(String id) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (String facing : List.of("north", "south", "west", "east")) {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", "hbm:block/" + id);
            variants.add("facing=" + facing, variant);
        }
        root.add("variants", variants);
        return root;
    }

    private JsonObject unconditionalMultipartState(String id) {
        JsonObject root = new JsonObject();
        JsonArray multipart = new JsonArray();
        JsonObject entry = new JsonObject();
        JsonObject apply = new JsonObject();
        apply.addProperty("model", "hbm:block/" + id);
        entry.add("apply", apply);
        multipart.add(entry);
        root.add("multipart", multipart);
        return root;
    }

    private JsonObject simpleBlockState(String id) {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject defaultVariant = new JsonObject();
        defaultVariant.addProperty("model", "hbm:block/" + id);
        variants.add("", defaultVariant);
        root.add("variants", variants);
        return root;
    }

    private JsonObject reinforcedGlassPaneBlockState() {
        JsonObject root = new JsonObject();
        JsonArray multipart = new JsonArray();
        JsonObject post = new JsonObject();
        JsonObject postModel = new JsonObject();
        postModel.addProperty("model", "hbm:block/reinforced_glass_pane_post");
        post.add("apply", postModel);
        multipart.add(post);

        addPaneStatePart(multipart, "north", true, "side", 0);
        addPaneStatePart(multipart, "east", true, "side", 90);
        addPaneStatePart(multipart, "south", true, "side_alt", 0);
        addPaneStatePart(multipart, "west", true, "side_alt", 90);
        addPaneStatePart(multipart, "north", false, "noside", 0);
        addPaneStatePart(multipart, "east", false, "noside_alt", 0);
        addPaneStatePart(multipart, "south", false, "noside_alt", 90);
        addPaneStatePart(multipart, "west", false, "noside", 270);
        root.add("multipart", multipart);
        return root;
    }

    private void addPaneStatePart(JsonArray multipart, String direction, boolean connected,
                                  String modelPart, int rotation) {
        JsonObject part = new JsonObject();
        JsonObject when = new JsonObject();
        when.addProperty(direction, Boolean.toString(connected));
        part.add("when", when);
        JsonObject apply = new JsonObject();
        apply.addProperty("model", "hbm:block/reinforced_glass_pane_" + modelPart);
        if (rotation != 0) {
            apply.addProperty("y", rotation);
        }
        part.add("apply", apply);
        multipart.add(part);
    }

    private JsonObject selfDropLoot(String id) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");

        JsonObject survivesExplosion = new JsonObject();
        survivesExplosion.addProperty("condition", "minecraft:survives_explosion");
        JsonArray conditions = new JsonArray();
        conditions.add(survivesExplosion);

        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", "hbm:" + id);
        JsonArray entries = new JsonArray();
        entries.add(entry);

        JsonObject pool = new JsonObject();
        pool.addProperty("bonus_rolls", 0.0F);
        pool.add("conditions", conditions);
        pool.add("entries", entries);
        pool.addProperty("rolls", 1.0F);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        root.add("pools", pools);
        root.addProperty("random_sequence", "hbm:blocks/" + id);
        return root;
    }

    private JsonObject silkOnlyLoot(String block) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", "hbm:" + block);

        JsonObject enchantment = new JsonObject();
        enchantment.addProperty("enchantments", "minecraft:silk_touch");
        JsonObject levels = new JsonObject();
        levels.addProperty("min", 1);
        enchantment.add("levels", levels);
        JsonArray enchantments = new JsonArray();
        enchantments.add(enchantment);
        JsonObject predicates = new JsonObject();
        predicates.add("minecraft:enchantments", enchantments);
        JsonObject predicate = new JsonObject();
        predicate.add("predicates", predicates);
        JsonObject matchTool = new JsonObject();
        matchTool.addProperty("condition", "minecraft:match_tool");
        matchTool.add("predicate", predicate);
        JsonArray entryConditions = new JsonArray();
        entryConditions.add(matchTool);
        entry.add("conditions", entryConditions);

        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonObject survives = new JsonObject();
        survives.addProperty("condition", "minecraft:survives_explosion");
        JsonArray poolConditions = new JsonArray();
        poolConditions.add(survives);
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1.0F);
        pool.addProperty("bonus_rolls", 0.0F);
        pool.add("conditions", poolConditions);
        pool.add("entries", entries);
        JsonArray pools = new JsonArray();
        pools.add(pool);

        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        root.add("pools", pools);
        root.addProperty("random_sequence", "hbm:blocks/" + block);
        return root;
    }

    private JsonObject silkElseItemLoot(String block, String ordinaryDrop) {
        JsonObject silk = new JsonObject();
        silk.addProperty("type", "minecraft:item");
        silk.addProperty("name", "hbm:" + block);
        JsonObject enchantment = new JsonObject();
        enchantment.addProperty("enchantments", "minecraft:silk_touch");
        JsonObject levels = new JsonObject();
        levels.addProperty("min", 1);
        enchantment.add("levels", levels);
        JsonArray enchantments = new JsonArray();
        enchantments.add(enchantment);
        JsonObject predicates = new JsonObject();
        predicates.add("minecraft:enchantments", enchantments);
        JsonObject predicate = new JsonObject();
        predicate.add("predicates", predicates);
        JsonObject matchTool = new JsonObject();
        matchTool.addProperty("condition", "minecraft:match_tool");
        matchTool.add("predicate", predicate);
        JsonArray silkConditions = new JsonArray();
        silkConditions.add(matchTool);
        silk.add("conditions", silkConditions);

        JsonObject ordinary = new JsonObject();
        ordinary.addProperty("type", "minecraft:item");
        ordinary.addProperty("name", ordinaryDrop);
        JsonObject alternatives = new JsonObject();
        alternatives.addProperty("type", "minecraft:alternatives");
        JsonArray children = new JsonArray();
        children.add(silk);
        children.add(ordinary);
        alternatives.add("children", children);

        JsonObject survives = new JsonObject();
        survives.addProperty("condition", "minecraft:survives_explosion");
        JsonArray conditions = new JsonArray();
        conditions.add(survives);
        JsonArray entries = new JsonArray();
        entries.add(alternatives);
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1.0F);
        pool.addProperty("bonus_rolls", 0.0F);
        pool.add("conditions", conditions);
        pool.add("entries", entries);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        root.add("pools", pools);
        root.addProperty("random_sequence", "hbm:blocks/" + block);
        return root;
    }

    private JsonObject wasteLogLoot() {
        JsonObject root = silkElseItemLoot("waste_log", "minecraft:charcoal");
        JsonArray children = root.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject().getAsJsonArray("children");

        JsonObject bark = new JsonObject();
        bark.addProperty("type", "minecraft:item");
        bark.addProperty("name", "hbm:burnt_bark");
        JsonObject chance = new JsonObject();
        chance.addProperty("condition", "minecraft:random_chance");
        chance.addProperty("chance", 0.001F);
        JsonArray barkConditions = new JsonArray();
        barkConditions.add(chance);
        bark.add("conditions", barkConditions);
        JsonObject charcoal = children.remove(1).getAsJsonObject();
        children.add(bark);
        children.add(charcoal);
        JsonObject count = new JsonObject();
        count.addProperty("type", "minecraft:uniform");
        count.addProperty("min", 2.0F);
        count.addProperty("max", 4.0F);
        JsonObject setCount = new JsonObject();
        setCount.addProperty("function", "minecraft:set_count");
        setCount.add("count", count);
        JsonArray functions = new JsonArray();
        functions.add(setCount);
        charcoal.add("functions", functions);
        return root;
    }

    /** Frozen logs contain two to four legally distinct snowballs. */
    private JsonObject frozenLogLoot() {
        JsonObject root = silkElseItemLoot("frozen_log", "minecraft:snowball");
        JsonObject snowball = root.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject()
                .getAsJsonArray("children").get(1).getAsJsonObject();
        JsonObject count = new JsonObject();
        count.addProperty("type", "minecraft:uniform");
        count.addProperty("min", 2.0F);
        count.addProperty("max", 4.0F);
        JsonObject setCount = new JsonObject();
        setCount.addProperty("function", "minecraft:set_count");
        setCount.add("count", count);
        JsonArray functions = new JsonArray();
        functions.add(setCount);
        snowball.add("functions", functions);
        return root;
    }

    private JsonObject sellafieldLoot() {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", "hbm:sellafield");
        JsonArray functions = new JsonArray();
        JsonObject copyState = new JsonObject();
        copyState.addProperty("function", "minecraft:copy_state");
        copyState.addProperty("block", "hbm:sellafield");
        JsonArray properties = new JsonArray();
        properties.add("level");
        copyState.add("properties", properties);
        functions.add(copyState);
        for (int level = 0; level < 6; level++) {
            JsonObject blockCondition = new JsonObject();
            blockCondition.addProperty("condition", "minecraft:block_state_property");
            blockCondition.addProperty("block", "hbm:sellafield");
            JsonObject expected = new JsonObject();
            expected.addProperty("level", Integer.toString(level));
            blockCondition.add("properties", expected);
            JsonArray conditions = new JsonArray();
            conditions.add(blockCondition);
            JsonObject components = new JsonObject();
            components.addProperty("minecraft:custom_model_data", level);
            JsonObject setComponents = new JsonObject();
            setComponents.addProperty("function", "minecraft:set_components");
            setComponents.add("conditions", conditions);
            setComponents.add("components", components);
            functions.add(setComponents);
        }
        entry.add("functions", functions);
        JsonArray entries = new JsonArray();
        entries.add(entry);
        JsonObject survives = new JsonObject();
        survives.addProperty("condition", "minecraft:survives_explosion");
        JsonArray conditions = new JsonArray();
        conditions.add(survives);
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1.0F);
        pool.addProperty("bonus_rolls", 0.0F);
        pool.add("conditions", conditions);
        pool.add("entries", entries);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        root.add("pools", pools);
        root.addProperty("random_sequence", "hbm:blocks/sellafield");
        return root;
    }

    private JsonObject oilDepositLoot() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", "hbm:oil_tar");
        JsonObject fortune = new JsonObject();
        fortune.addProperty("function", "minecraft:apply_bonus");
        fortune.addProperty("enchantment", "minecraft:fortune");
        fortune.addProperty("formula", "minecraft:ore_drops");
        JsonObject explosion = new JsonObject();
        explosion.addProperty("function", "minecraft:explosion_decay");
        JsonArray functions = new JsonArray();
        functions.add(fortune); functions.add(explosion);
        entry.add("functions", functions);
        JsonArray entries = new JsonArray(); entries.add(entry);
        JsonObject pool = new JsonObject();
        pool.addProperty("bonus_rolls", 0.0F);
        pool.addProperty("rolls", 1.0F);
        pool.add("entries", entries);
        JsonArray pools = new JsonArray(); pools.add(pool);
        root.add("pools", pools);
        root.addProperty("random_sequence", "hbm:blocks/ore_oil");
        return root;
    }

    private JsonObject cobaltOreLoot() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");

        JsonObject silkEntry = new JsonObject();
        silkEntry.addProperty("type", "minecraft:item");
        silkEntry.addProperty("name", "hbm:ore_cobalt");
        JsonObject enchantment = new JsonObject();
        enchantment.addProperty("enchantments", "minecraft:silk_touch");
        JsonObject levels = new JsonObject(); levels.addProperty("min", 1);
        enchantment.add("levels", levels);
        JsonArray enchantments = new JsonArray(); enchantments.add(enchantment);
        JsonObject predicates = new JsonObject(); predicates.add("minecraft:enchantments", enchantments);
        JsonObject predicate = new JsonObject(); predicate.add("predicates", predicates);
        JsonObject matchTool = new JsonObject(); matchTool.addProperty("condition", "minecraft:match_tool");
        matchTool.add("predicate", predicate);
        JsonArray silkConditions = new JsonArray(); silkConditions.add(matchTool);
        silkEntry.add("conditions", silkConditions);

        JsonObject fragmentEntry = new JsonObject();
        fragmentEntry.addProperty("type", "minecraft:item");
        fragmentEntry.addProperty("name", "hbm:fragment_cobalt");
        JsonObject count = new JsonObject(); count.addProperty("type", "minecraft:uniform");
        count.addProperty("min", 4.0F); count.addProperty("max", 9.0F);
        JsonObject setCount = new JsonObject(); setCount.addProperty("function", "minecraft:set_count");
        setCount.addProperty("add", false); setCount.add("count", count);
        JsonObject fortune = new JsonObject(); fortune.addProperty("function", "minecraft:apply_bonus");
        fortune.addProperty("enchantment", "minecraft:fortune");
        fortune.addProperty("formula", "minecraft:ore_drops");
        JsonObject explosion = new JsonObject(); explosion.addProperty("function", "minecraft:explosion_decay");
        JsonArray functions = new JsonArray(); functions.add(setCount); functions.add(fortune); functions.add(explosion);
        fragmentEntry.add("functions", functions);

        JsonObject alternatives = new JsonObject(); alternatives.addProperty("type", "minecraft:alternatives");
        JsonArray children = new JsonArray(); children.add(silkEntry); children.add(fragmentEntry);
        alternatives.add("children", children);
        JsonArray entries = new JsonArray(); entries.add(alternatives);
        JsonObject pool = new JsonObject(); pool.addProperty("bonus_rolls", 0.0F);
        pool.add("entries", entries); pool.addProperty("rolls", 1.0F);
        JsonArray pools = new JsonArray(); pools.add(pool);
        root.add("pools", pools);
        root.addProperty("random_sequence", "hbm:blocks/ore_cobalt");
        return root;
    }

    private JsonObject coltanOreLoot() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");

        JsonObject silkEntry = new JsonObject();
        silkEntry.addProperty("type", "minecraft:item");
        silkEntry.addProperty("name", "hbm:ore_coltan");
        JsonObject enchantment = new JsonObject();
        enchantment.addProperty("enchantments", "minecraft:silk_touch");
        JsonObject levels = new JsonObject();
        levels.addProperty("min", 1);
        enchantment.add("levels", levels);
        JsonArray enchantments = new JsonArray();
        enchantments.add(enchantment);
        JsonObject predicates = new JsonObject();
        predicates.add("minecraft:enchantments", enchantments);
        JsonObject predicate = new JsonObject();
        predicate.add("predicates", predicates);
        JsonObject matchTool = new JsonObject();
        matchTool.addProperty("condition", "minecraft:match_tool");
        matchTool.add("predicate", predicate);
        JsonArray silkConditions = new JsonArray();
        silkConditions.add(matchTool);
        silkEntry.add("conditions", silkConditions);

        JsonObject fragment = new JsonObject();
        fragment.addProperty("type", "minecraft:item");
        fragment.addProperty("name", "hbm:fragment_coltan");
        JsonObject fortune = new JsonObject();
        fortune.addProperty("function", "minecraft:apply_bonus");
        fortune.addProperty("enchantment", "minecraft:fortune");
        fortune.addProperty("formula", "minecraft:ore_drops");
        JsonObject explosion = new JsonObject();
        explosion.addProperty("function", "minecraft:explosion_decay");
        JsonArray functions = new JsonArray();
        functions.add(fortune);
        functions.add(explosion);
        fragment.add("functions", functions);

        JsonObject alternatives = new JsonObject();
        alternatives.addProperty("type", "minecraft:alternatives");
        JsonArray children = new JsonArray();
        children.add(silkEntry);
        children.add(fragment);
        alternatives.add("children", children);
        JsonArray entries = new JsonArray();
        entries.add(alternatives);
        JsonObject pool = new JsonObject();
        pool.addProperty("bonus_rolls", 0.0F);
        pool.addProperty("rolls", 1.0F);
        pool.add("entries", entries);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        root.add("pools", pools);
        root.addProperty("random_sequence", "hbm:blocks/ore_coltan");
        return root;
    }

    private JsonObject rareEarthOreLoot() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");

        JsonObject silkEntry = new JsonObject();
        silkEntry.addProperty("type", "minecraft:item");
        silkEntry.addProperty("name", "hbm:ore_rare");
        JsonObject enchantment = new JsonObject();
        enchantment.addProperty("enchantments", "minecraft:silk_touch");
        JsonObject levels = new JsonObject();
        levels.addProperty("min", 1);
        enchantment.add("levels", levels);
        JsonArray enchantments = new JsonArray();
        enchantments.add(enchantment);
        JsonObject predicates = new JsonObject();
        predicates.add("minecraft:enchantments", enchantments);
        JsonObject predicate = new JsonObject();
        predicate.add("predicates", predicates);
        JsonObject matchTool = new JsonObject();
        matchTool.addProperty("condition", "minecraft:match_tool");
        matchTool.add("predicate", predicate);
        JsonArray silkConditions = new JsonArray();
        silkConditions.add(matchTool);
        silkEntry.add("conditions", silkConditions);

        JsonObject chunk = new JsonObject();
        chunk.addProperty("type", "minecraft:item");
        chunk.addProperty("name", "hbm:chunk_ore");
        JsonObject fortune = new JsonObject();
        fortune.addProperty("function", "minecraft:apply_bonus");
        fortune.addProperty("enchantment", "minecraft:fortune");
        fortune.addProperty("formula", "minecraft:ore_drops");
        JsonObject explosion = new JsonObject();
        explosion.addProperty("function", "minecraft:explosion_decay");
        JsonArray functions = new JsonArray();
        functions.add(fortune);
        functions.add(explosion);
        chunk.add("functions", functions);

        JsonObject alternatives = new JsonObject();
        alternatives.addProperty("type", "minecraft:alternatives");
        JsonArray children = new JsonArray();
        children.add(silkEntry);
        children.add(chunk);
        alternatives.add("children", children);
        JsonArray entries = new JsonArray();
        entries.add(alternatives);
        JsonObject pool = new JsonObject();
        pool.addProperty("bonus_rolls", 0.0F);
        pool.addProperty("rolls", 1.0F);
        pool.add("entries", entries);
        JsonArray pools = new JsonArray();
        pools.add(pool);
        root.add("pools", pools);
        root.addProperty("random_sequence", "hbm:blocks/ore_rare");
        return root;
    }

    private JsonObject smeltingRecipe(String ingredient, String result, float experience) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:smelting");
        recipe.addProperty("category", "misc");
        recipe.add("ingredient", itemIngredient(ingredient));
        JsonObject output = new JsonObject(); output.addProperty("id", result);
        recipe.add("result", output);
        recipe.addProperty("experience", experience);
        recipe.addProperty("cookingtime", 200);
        return recipe;
    }

    private JsonObject compressionRecipe(String ingredient, String result) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "building");

        JsonArray pattern = new JsonArray();
        pattern.add("###");
        pattern.add("###");
        pattern.add("###");
        root.add("pattern", pattern);

        JsonObject exactIngredient = new JsonObject();
        exactIngredient.addProperty("item", "hbm:" + ingredient);
        JsonObject key = new JsonObject();
        key.add("#", exactIngredient);
        root.add("key", key);

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("count", 1);
        resultObject.addProperty("id", "hbm:" + result);
        root.add("result", resultObject);
        return root;
    }

    private JsonObject decompressionRecipe(String ingredient, String result) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");

        JsonObject exactIngredient = new JsonObject();
        exactIngredient.addProperty("item", "hbm:" + ingredient);
        JsonArray ingredients = new JsonArray();
        ingredients.add(exactIngredient);
        root.add("ingredients", ingredients);

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("count", 9);
        resultObject.addProperty("id", "hbm:" + result);
        root.add("result", resultObject);
        return root;
    }

    private void addSiliconConversionRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        writes.add(save(output, shapedRecipe(List.of("###", "###", "###"), "nugget_silicon",
                "ingot_silicon", 1), recipes, hbm("ingot_silicon_from_nuggets")));
        writes.add(save(output, shapelessRecipe(List.of("ingot_silicon"), "nugget_silicon", 9),
                recipes, hbm("nugget_silicon_from_boule")));
        writes.add(save(output, shapedRecipe(List.of("###", "###"), "nugget_silicon",
                "billet_silicon", 1), recipes, hbm("billet_silicon_from_nuggets")));
        writes.add(save(output, shapelessRecipe(List.of("billet_silicon"), "nugget_silicon", 6),
                recipes, hbm("nugget_silicon_from_wafer")));
        writes.add(save(output, shapelessRecipe(List.of("billet_silicon", "billet_silicon", "billet_silicon"),
                "ingot_silicon", 2), recipes, hbm("ingot_silicon_from_wafers")));
        writes.add(save(output, shapedRecipe(List.of("##"), "ingot_silicon", "billet_silicon", 3),
                recipes, hbm("billet_silicon_from_boules")));
    }

    private JsonObject foundryMoldItemModel() {
        JsonObject root = generatedItemModel("mold_nugget");
        JsonArray overrides = new JsonArray();
        for (FoundryMoldItem.Mold mold : FoundryMoldItem.Mold.values()) {
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", mold.id());
            JsonObject override = new JsonObject();
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/mold_" + mold.texture());
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject casingItemModel() {
        JsonObject root = generatedItemModel("casing.small");
        JsonArray overrides = new JsonArray();
        String[] variants = {"small", "large", "small_steel", "large_steel", "shotshell", "buckshot", "buckshot_advanced"};
        for (int metadata = 0; metadata < variants.length; metadata++) {
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", metadata);
            JsonObject override = new JsonObject();
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/casing_" + variants[metadata]);
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject foundryScrapsModel() {
        JsonObject root = generatedItemModel("scraps_iron");
        JsonArray overrides = new JsonArray();
        int[] metadata = {2600, 2900, 7400, 2700, 699, 40, 30, 33, 31, 38, 36};
        String[] materials = {"iron", "copper", "tungsten", "cobalt", "carbon", "flux", "steel", "dura_steel",
                "red_copper", "magnetized_tungsten", "technetium_steel"};
        for (int i = 0; i < metadata.length; i++) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", metadata[i]);
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/scraps_" + materials[i]);
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject powderAlloyScrapsRecipe(List<String> ingredientTags,
                                                String material, int metadata, int amount) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredientTags.forEach(tag -> ingredients.add(tagIngredient(tag)));
        root.add("ingredients", ingredients);

        JsonObject customData = new JsonObject();
        customData.addProperty("material", material);
        customData.addProperty("amount", amount);
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", metadata);
        JsonObject result = recipeResult("hbm:scraps", 1);
        result.add("components", components);
        root.add("result", result);
        return root;
    }

    private JsonObject coalPowderFluxRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(tagIngredient("c:dusts/coal"));
        ingredients.add(tagIngredient("minecraft:sand"));
        root.add("ingredients", ingredients);
        root.add("result", recipeResult("hbm:powder_flux", 2));
        return root;
    }

    private JsonObject calciumPowderFluxRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(tagIngredient("c:dusts/calcium"));
        ingredients.add(tagIngredient("minecraft:sand"));
        root.add("ingredients", ingredients);
        root.add("result", recipeResult("hbm:powder_flux", 12));
        return root;
    }

    private JsonObject fluoriteFluxRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(tagIngredient("c:dusts/fluorite"));
        ingredients.add(tagIngredient("minecraft:sand"));
        root.add("ingredients", ingredients);
        root.add("result", recipeResult("hbm:powder_flux", 4));
        return root;
    }

    private JsonObject gunpowderRecipe(boolean charcoal) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(tagIngredient("c:dusts/sulfur"));
        ingredients.add(tagIngredient("c:dusts/saltpeter"));
        ingredients.add(charcoal ? itemIngredient("minecraft:charcoal") : tagIngredient("c:gems/coal"));
        root.add("ingredients", ingredients);
        root.add("result", recipeResult("minecraft:gunpowder", 3));
        return root;
    }

    private JsonObject clayUncraftingRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("minecraft:clay"));
        root.add("ingredients", ingredients);
        root.add("result", recipeResult("minecraft:clay_ball", 4));
        return root;
    }

    private JsonObject fluxRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("minecraft:charcoal"));
        ingredients.add(tagIngredient("minecraft:sand"));
        root.add("ingredients", ingredients);
        root.add("result", recipeResult("hbm:powder_flux", 1));
        return root;
    }

    private JsonObject moldBaseRecipe() {
        return foundryShaped(List.of(" B ", "BIB", " B "), Map.of(
                "B", itemIngredient("hbm:ingot_firebrick"),
                "I", tagIngredient("c:ingots/iron")), "hbm:mold_base", 1);
    }

    private JsonObject foundryMoldRecipe() {
        return foundryShaped(List.of("B B", "BSB"), Map.of(
                "B", itemIngredient("hbm:ingot_firebrick"),
                "S", itemIngredient("minecraft:stone_slab")), "hbm:foundry_mold", 1);
    }

    private JsonObject foundryBasinRecipe() {
        return foundryShaped(List.of("B B", "B B", "BSB"), Map.of(
                "B", itemIngredient("hbm:ingot_firebrick"),
                "S", itemIngredient("minecraft:stone_slab")), "hbm:foundry_basin", 1);
    }

    private JsonObject foundryChannelRecipe() {
        return foundryShaped(List.of("B B", " S "), Map.of(
                "B", itemIngredient("hbm:ingot_firebrick"),
                "S", itemIngredient("minecraft:stone_slab")), "hbm:foundry_channel", 4);
    }

    private JsonObject foundryTankRecipe() {
        return foundryShaped(List.of("B B", "I I", "BSB"), Map.of(
                "B", itemIngredient("hbm:ingot_firebrick"),
                "I", tagIngredient("c:ingots/steel"),
                "S", itemIngredient("minecraft:stone_slab")), "hbm:foundry_tank", 1);
    }

    private JsonObject foundryOutletRecipe(boolean slagTap) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("hbm:foundry_channel"));
        ingredients.add(slagTap ? itemIngredient("minecraft:stone_bricks") : tagIngredient("c:plates/steel"));
        root.add("ingredients", ingredients);
        root.add("result", recipeResult(slagTap ? "hbm:foundry_slagtap" : "hbm:foundry_outlet", 1));
        return root;
    }

    private JsonObject screwdriverRecipe() {
        return foundryShaped(List.of("  I", " I ", "S  "), Map.of(
                "I", tagIngredient("c:ingots/iron"),
                "S", tagIngredient("c:ingots/steel")), "hbm:screwdriver", 1);
    }

    private JsonObject pneumaticPistonRecipe() {
        return foundryShaped(List.of(" I ", "CPC", " I "), Map.of(
                "I", tagIngredient("c:ingots/iron"),
                "C", tagIngredient("c:ingots/copper"),
                "P", tagIngredient("c:plates/iron")), "hbm:part_generic", 4);
    }

    private JsonObject conveyorBoxerRecipe() {
        return foundryShaped(List.of("WWW", "WPW", "CCC"), Map.of(
                "W", tagIngredient("minecraft:planks"),
                "P", itemIngredient("hbm:part_generic"),
                "C", itemIngredient("hbm:conveyor_wand")), "hbm:crane_boxer", 1);
    }

    private JsonObject craneEndpointRecipe(String endpoint, boolean extractor, JsonObject casing, int count) {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("C", casing);
        key.put("B", itemIngredient("hbm:conveyor_wand"));
        if (extractor) key.put("P", itemIngredient("hbm:part_generic"));
        return foundryShaped(extractor ? List.of("CCC", "CPC", "CBC")
                : List.of("CCC", "C C", "CBC"), key, "hbm:" + endpoint, count);
    }

    private JsonObject regularConveyorRecipe() {
        return foundryShaped(List.of("LLL", "I I", "LLL"), Map.of(
                "L", itemIngredient("minecraft:leather"),
                "I", tagIngredient("c:ingots/iron")), "hbm:conveyor_wand", 16);
    }

    private JsonObject regularConveyorRubberRecipe() {
        return foundryShaped(List.of("LLL", "I I", "LLL"), Map.of(
                "L", itemIngredient("hbm:ingot_biorubber"),
                "I", tagIngredient("c:ingots/iron")), "hbm:conveyor_wand", 64);
    }

    private JsonObject expressConveyorRecipe() {
        // Slime is bargain-bin lubricant until the real goo arrives.
        return foundryShaped(List.of("CCC", "CSC", "CCC"), Map.of(
                "C", itemIngredient("hbm:conveyor_wand"),
                "S", itemIngredient("minecraft:slime_ball")), "hbm:conveyor_wand_express", 8);
    }

    private JsonObject doubleConveyorRecipe() {
        return foundryShaped(List.of("CPC"), Map.of(
                "C", itemIngredient("hbm:conveyor_wand"),
                "P", itemIngredient("hbm:plate_iron")), "hbm:conveyor_wand_double", 1);
    }

    private JsonObject tripleConveyorRecipe() {
        return foundryShaped(List.of("DPC"), Map.of(
                "D", itemIngredient("hbm:conveyor_wand_double"),
                "P", itemIngredient("hbm:plate_steel"),
                "C", itemIngredient("hbm:conveyor_wand")), "hbm:conveyor_wand_triple", 1);
    }

    private JsonObject fluidIdentifierModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "hbm:item/fluid_identifier_multi");
        textures.addProperty("layer1", "hbm:item/fluid_identifier_overlay");
        root.add("textures", textures);
        return root;
    }

    private JsonObject fluidTankFullModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "hbm:item/fluid_tank");
        textures.addProperty("layer1", "hbm:item/fluid_tank_overlay");
        root.add("textures", textures);
        return root;
    }

    private JsonObject universalFluidTankRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("AIA"); pattern.add("AGA"); pattern.add("AIA");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("A", tagIngredient("c:plates/aluminum"));
        key.add("I", tagIngredient("c:plates/iron"));
        key.add("G", tagIngredient("c:glass_panes"));
        recipe.add("key", key);
        recipe.add("result", recipeResult("hbm:fluid_tank_empty", 8));
        return recipe;
    }

    private JsonObject fluidDuctBaseRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("SAS"); pattern.add("   "); pattern.add("SAS");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("S", tagIngredient("c:plates/steel"));
        key.add("A", tagIngredient("c:plates/aluminum"));
        recipe.add("key", key);
        recipe.add("result", recipeResult("hbm:fluid_duct_neo", 8));
        return recipe;
    }

    private JsonObject fluidDuctUntypingRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        JsonObject duct = new JsonObject();
        duct.addProperty("item", "hbm:fluid_duct");
        ingredients.add(duct);
        recipe.add("ingredients", ingredients);
        recipe.add("result", recipeResult("hbm:fluid_duct_neo", 1));
        return recipe;
    }

    private JsonObject fluidIdentifierRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("D"); pattern.add("C"); pattern.add("P");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("D", tagIngredient("c:dyes"));
        JsonObject customData = new JsonObject();
        customData.addProperty("type", "vacuum_tube");
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", 0);
        JsonObject vacuumTube = new JsonObject();
        vacuumTube.addProperty("type", "neoforge:components");
        vacuumTube.addProperty("items", "hbm:circuit");
        vacuumTube.add("components", components);
        vacuumTube.addProperty("strict", false);
        key.add("C", vacuumTube);
        key.add("P", tagIngredient("c:plates/iron"));
        recipe.add("key", key);
        recipe.add("result", recipeResult("hbm:fluid_identifier_multi", 1));
        return recipe;
    }

    private JsonObject foundryShaped(List<String> lines, Map<String, JsonObject> ingredients,
                                     String resultId, int count) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        lines.forEach(pattern::add);
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        ingredients.forEach(key::add);
        root.add("key", key);
        root.add("result", recipeResult(resultId, count));
        return root;
    }

    private JsonObject recipeResult(String id, int count) {
        JsonObject result = new JsonObject();
        result.addProperty("id", id);
        result.addProperty("count", count);
        return result;
    }

    private JsonObject shapedRecipe(List<String> patternLines, String ingredient, String result, int count) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        patternLines.forEach(pattern::add);
        root.add("pattern", pattern);
        JsonObject ingredientObject = new JsonObject();
        ingredientObject.addProperty("item", "hbm:" + ingredient);
        JsonObject key = new JsonObject();
        key.add("#", ingredientObject);
        root.add("key", key);
        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("count", count);
        resultObject.addProperty("id", "hbm:" + result);
        root.add("result", resultObject);
        return root;
    }

    private JsonObject shapelessRecipe(List<String> ingredientsList, String result, int count) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        for (String ingredient : ingredientsList) {
            JsonObject ingredientObject = new JsonObject();
            ingredientObject.addProperty("item", "hbm:" + ingredient);
            ingredients.add(ingredientObject);
        }
        root.add("ingredients", ingredients);
        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("count", count);
        resultObject.addProperty("id", "hbm:" + result);
        root.add("result", resultObject);
        return root;
    }

    private JsonObject cementRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("hbm:powder_limestone"));
        ingredients.add(itemIngredient("minecraft:clay_ball"));
        ingredients.add(itemIngredient("minecraft:clay_ball"));
        ingredients.add(itemIngredient("minecraft:clay_ball"));
        root.add("ingredients", ingredients);
        root.add("result", recipeResult("hbm:powder_cement", 4));
        return root;
    }

    private JsonObject largeGearRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("III");
        pattern.add("ICI");
        pattern.add("III");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("I", tagIngredient("c:plates/iron"));
        key.add("C", tagIngredient("c:ingots/copper"));
        recipe.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:gear_large");
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject shredderBlockModel() {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/cube");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", "hbm:block/machine_shredder_front_alt");
        textures.addProperty("down", "hbm:block/machine_shredder_bottom_alt");
        textures.addProperty("up", "hbm:block/machine_shredder_top_alt");
        textures.addProperty("north", "hbm:block/machine_shredder_front_alt");
        textures.addProperty("south", "hbm:block/machine_shredder_front_alt");
        textures.addProperty("west", "hbm:block/machine_shredder_side_alt");
        textures.addProperty("east", "hbm:block/machine_shredder_side_alt");
        root.add("textures", textures);
        return root;
    }

    private void addShredderBladeRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        addShredderBladeRecipe(writes, output, "steel", "c:plates/steel", "c:ingots/steel");
        addShredderBladeRecipe(writes, output, "titanium", "c:plates/titanium", "c:ingots/titanium");
    }

    private void addShredderBladeRecipe(
            List<CompletableFuture<?>> writes,
            CachedOutput output,
            String material,
            String plateTag,
            String ingotTag
    ) {
        JsonObject crafting = new JsonObject();
        crafting.addProperty("type", "minecraft:crafting_shaped");
        crafting.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add(" P ");
        pattern.add("PIP");
        pattern.add(" P ");
        crafting.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("P", tagIngredient(plateTag));
        key.add("I", tagIngredient(ingotTag));
        crafting.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:blades_" + material);
        crafting.add("result", result);
        writes.add(save(output, crafting, recipes, hbm("blades_" + material)));

        JsonObject repair = new JsonObject();
        repair.addProperty("type", "minecraft:crafting_shaped");
        repair.addProperty("category", "misc");
        JsonArray repairPattern = new JsonArray();
        repairPattern.add("PIP");
        repair.add("pattern", repairPattern);
        JsonObject repairKey = new JsonObject();
        repairKey.add("P", tagIngredient(plateTag));
        repairKey.add("I", itemIngredient("hbm:blades_" + material));
        repair.add("key", repairKey);
        JsonObject repairResult = new JsonObject();
        repairResult.addProperty("count", 1);
        repairResult.addProperty("id", "hbm:blades_" + material);
        repair.add("result", repairResult);
        writes.add(save(output, repair, recipes, hbm("blades_" + material + "_repair")));
    }

    private JsonObject pressBlockState() {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        for (String part : List.of("lower", "middle", "upper")) {
            JsonObject variant = new JsonObject();
            variant.addProperty("model", "hbm:block/machine_press");
            variants.add("part=" + part, variant);
        }
        root.add("variants", variants);
        return root;
    }

    private JsonObject machinePressRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("IRI");
        pattern.add("IPI");
        pattern.add("IBI");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("I", tagIngredient("c:ingots/iron"));
        key.add("R", itemIngredient("minecraft:furnace"));
        key.add("P", itemIngredient("minecraft:piston"));
        key.add("B", tagIngredient("c:storage_blocks/iron"));
        root.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:machine_press");
        root.add("result", result);
        return root;
    }

    private JsonObject ammoPressRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("IPI");
        pattern.add("C C");
        pattern.add("SSS");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("I", tagIngredient("c:ingots/iron"));
        key.add("P", itemIngredient("minecraft:piston"));
        key.add("C", tagIngredient("c:ingots/copper"));
        key.add("S", itemIngredient("minecraft:stone"));
        root.add("key", key);
        root.add("result", recipeResult("hbm:machine_ammo_press", 1));
        return root;
    }

    private JsonObject machineSirenRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "redstone");
        JsonArray pattern = new JsonArray();
        pattern.add("SIS");
        pattern.add("ICI");
        pattern.add("SRS");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("S", tagIngredient("c:plates/steel"));
        key.add("I", tagIngredient("hbm:ingots/any_rubber"));
        key.add("C", customComponentIngredient("hbm:circuit", "type", "vacuum_tube", 0));
        key.add("R", itemIngredient("minecraft:redstone"));
        recipe.add("key", key);
        recipe.add("result", recipeResult("hbm:machine_siren", 1));
        return recipe;
    }

    private JsonObject pressPreheaterRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("CCC");
        pattern.add("SLS");
        pattern.add("TST");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("C", tagIngredient("c:plates/copper"));
        key.add("S", itemIngredient("minecraft:stone"));
        key.add("L", itemIngredient("minecraft:lava_bucket"));
        key.add("T", tagIngredient("c:ingots/tungsten"));
        root.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:press_preheater");
        root.add("result", result);
        return root;
    }

    private void addEarlyCircuitRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        writes.add(save(output, vacuumTubeRecipe("tungsten", 7400), recipes, hbm("circuit_vacuum_tube_from_tungsten")));
        writes.add(save(output, vacuumTubeRecipe("carbon", 699), recipes, hbm("circuit_vacuum_tube_from_carbon")));
        writes.add(save(output, numitronRecipe(), recipes, hbm("circuit_numitron")));
        writes.add(save(output, capacitorBulkRecipe("aluminium", 1300), recipes,
                hbm("circuit_capacitor_from_aluminium")));
        writes.add(save(output, capacitorBulkRecipe("copper", 2900), recipes,
                hbm("circuit_capacitor_from_copper")));
        writes.add(save(output, capacitorTantaliumRecipe("aluminium", 1300), recipes,
                hbm("circuit_capacitor_tantalium_from_aluminium")));
        writes.add(save(output, capacitorTantaliumRecipe("copper", 2900), recipes,
                hbm("circuit_capacitor_tantalium_from_copper")));
        writes.add(save(output, pcbRecipe("c:plates/copper", 1), recipes, hbm("circuit_pcb_from_copper")));
        writes.add(save(output, pcbRecipe("c:plates/gold", 4), recipes, hbm("circuit_pcb_from_gold")));
    }

    private JsonObject vacuumTubeRecipe(String wire, int metadata) {
        JsonObject recipe = shapedBase(List.of("G", "W", "I"));
        JsonObject key = new JsonObject();
        key.add("G", tagIngredient("c:glass_panes"));
        key.add("W", componentIngredient(wire, metadata));
        key.add("I", itemIngredient("hbm:plate_polymer"));
        recipe.add("key", key);
        recipe.add("result", circuitResult("vacuum_tube", 0, 1));
        return recipe;
    }

    private JsonObject numitronRecipe() {
        JsonObject recipe = shapedBase(List.of("G", "W", "I"));
        JsonObject key = new JsonObject();
        key.add("G", tagIngredient("c:glass_panes"));
        key.add("W", itemIngredient("hbm:coil_tungsten"));
        key.add("I", tagIngredient("c:plates/copper"));
        recipe.add("key", key);
        recipe.add("result", circuitResult("numitron", 19, 3));
        return recipe;
    }

    private JsonObject capacitorBulkRecipe(String wire, int metadata) {
        JsonObject recipe = shapedBase(List.of("IAI", "W W"));
        JsonObject key = new JsonObject();
        key.add("I", itemIngredient("hbm:plate_polymer"));
        key.add("A", tagIngredient("c:dusts/aluminum"));
        key.add("W", componentIngredient(wire, metadata));
        recipe.add("key", key);
        recipe.add("result", circuitResult("capacitor", 1, 2));
        return recipe;
    }

    private JsonObject capacitorTantaliumRecipe(String wire, int metadata) {
        JsonObject recipe = shapedBase(List.of("I", "N", "W"));
        JsonObject key = new JsonObject();
        key.add("I", itemIngredient("hbm:plate_polymer"));
        key.add("N", tagIngredient("c:nuggets/tantalum"));
        key.add("W", materialComponentOrExternalTagIngredient(
                "hbm:wire_fine", wire, metadata, "c:wires/fine/" + wire));
        recipe.add("key", key);
        recipe.add("result", circuitResult("capacitor_tantalium", 2, 1));
        return recipe;
    }

    private JsonObject pcbRecipe(String plateTag, int count) {
        JsonObject recipe = shapedBase(List.of("I", "P"));
        JsonObject key = new JsonObject();
        key.add("I", itemIngredient("hbm:plate_polymer"));
        key.add("P", tagIngredient(plateTag));
        recipe.add("key", key);
        recipe.add("result", circuitResult("pcb", 3, count));
        return recipe;
    }

    private JsonObject shapedBase(List<String> rows) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        rows.forEach(pattern::add);
        recipe.add("pattern", pattern);
        return recipe;
    }

    private JsonObject circuitResult(String type, int metadata, int count) {
        JsonObject customData = new JsonObject();
        customData.addProperty("type", type);
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", metadata);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:circuit");
        result.addProperty("count", count);
        result.add("components", components);
        return result;
    }

    private void addFineWireAndComponentRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        List<WireData> wires = List.of(
                new WireData("carbon", 699, "c:ingots/graphite", "hbm:ingot_graphite", true),
                new WireData("gold", 7900, "c:ingots/gold", "minecraft:gold_ingot", true),
                new WireData("copper", 2900, "c:ingots/copper", "hbm:ingot_copper", true),
                new WireData("tungsten", 7400, "c:ingots/tungsten", "hbm:ingot_tungsten", true),
                new WireData("aluminium", 1300, "c:ingots/aluminum", "hbm:ingot_aluminium", true),
                new WireData("lead", 8200, "c:ingots/lead", "hbm:ingot_lead", false),
                new WireData("zirconium", 4000, "c:ingots/zirconium", "hbm:ingot_zirconium", false),
                new WireData("steel", 30, "c:ingots/steel", "hbm:ingot_steel", false),
                new WireData("red_copper", 31, "c:ingots/red_copper", "hbm:ingot_red_copper", true)
        );
        for (WireData wire : wires) {
            writes.add(save(output, wireCraftingRecipe(wire), recipes, hbm("wire_fine_" + wire.id())));
            if (wire.recompress()) {
                writes.add(save(output, wireRecompressionRecipe(wire), recipes,
                        hbm(wire.id() + "_ingot_from_wire_fine")));
            }
        }
        for (String core : List.of("iron", "steel")) {
            writes.add(save(output, coilRecipe("coil_copper", "red_copper", 31, core), recipes,
                    hbm("coil_copper_from_" + core)));
            writes.add(save(output, coilRecipe("coil_gold", "gold", 7900, core), recipes,
                    hbm("coil_gold_from_" + core)));
            writes.add(save(output, coilRecipe("coil_tungsten", "tungsten", 7400, core), recipes,
                    hbm("coil_tungsten_from_" + core)));
            writes.add(save(output, ringCoilRecipe("coil_copper_torus", "coil_copper", core), recipes,
                    hbm("coil_copper_torus_from_" + core)));
            writes.add(save(output, ringCoilRecipe("coil_gold_torus", "coil_gold", core), recipes,
                    hbm("coil_gold_torus_from_" + core)));
        }
        writes.add(save(output, steelTankRecipe(), recipes, hbm("tank_steel")));
        writes.add(save(output, motorRecipe(false), recipes, hbm("motor_from_iron")));
        writes.add(save(output, motorRecipe(true), recipes, hbm("motor_from_steel")));
        writes.add(save(output, craftedPartRecipe(true, tagIngredient("c:ingots/polymer"),
                "polymer", 20_001), recipes, hbm("part_stock_polymer")));
        writes.add(save(output, craftedPartRecipe(false, tagIngredient("c:ingots/polymer"),
                "polymer", 20_001), recipes, hbm("part_grip_polymer")));
        writes.add(save(output, craftedPartRecipe(true, tagIngredient("minecraft:planks"),
                "wood", 3), recipes, hbm("part_stock_wood")));
        writes.add(save(output, craftedPartRecipe(false, tagIngredient("minecraft:planks"),
                "wood", 3), recipes, hbm("part_grip_wood")));
        writes.add(save(output, craftedPartRecipe(false, tagIngredient("c:ingots/rubber"),
                "rubber", 20_003), recipes, hbm("part_grip_rubber")));
        writes.add(save(output, craftedPartRecipe(false, itemIngredient("minecraft:bone"),
                "ivory", 4), recipes, hbm("part_grip_ivory")));
        writes.add(save(output, deshMotorRecipe(), recipes, hbm("motor_desh")));
        writes.add(save(output, deshBladesRecipe(), recipes, hbm("blades_desh")));
        addUnlockedWeaponRecipes(writes, output);
        addCoreProgressionRecipes(writes, output);
        writes.add(save(output, coilBatterySocketRecipe(), recipes, hbm("machine_battery_socket_from_coil")));
    }

    private void addCoreProgressionRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        writes.add(save(output, shapedItemRecipe(List.of("FBF", "BFB", "FBF"), Map.of(
                "F", itemIngredient("minecraft:cobblestone"),
                "B", itemIngredient("minecraft:stone")), "hbm:reinforced_stone", 4),
                recipes, hbm("reinforced_stone")));
        writes.add(save(output, shapedItemRecipe(List.of("FBF", "BFB", "FBF"), Map.of(
                "F", itemIngredient("minecraft:iron_bars"),
                "B", itemIngredient("minecraft:glass")), "hbm:reinforced_glass", 4),
                recipes, hbm("reinforced_glass")));
        writes.add(save(output, shapedItemRecipe(List.of("GGG", "GGG"), Map.of(
                "G", itemIngredient("hbm:reinforced_glass")), "hbm:reinforced_glass_pane", 16),
                recipes, hbm("reinforced_glass_pane")));
        writes.add(save(output, shapedItemRecipe(List.of(" S ", "G G", " S "), Map.of(
                "S", tagIngredient("c:plates/steel"),
                "G", tagIngredient("c:glass_panes")), "hbm:cell_empty", 6),
                recipes, hbm("cell_empty")));

        writes.add(save(output, shapedItemRecipe(List.of("SSS", "L L", "SSS"), Map.of(
                "S", tagIngredient("c:plates/steel"),
                "L", tagIngredient("c:plates/lead")), "hbm:rod_empty", 16),
                recipes, hbm("rod_empty")));
        writes.add(save(output, shapelessItemRecipe(List.of(
                itemIngredient("hbm:rod_dual_empty")), "hbm:rod_empty", 2),
                recipes, hbm("rod_empty_from_dual")));
        writes.add(save(output, shapelessItemRecipe(List.of(
                itemIngredient("hbm:rod_empty"), itemIngredient("hbm:rod_empty")),
                "hbm:rod_dual_empty"), recipes, hbm("rod_dual_empty_from_rods")));
        writes.add(save(output, shapelessItemRecipe(List.of(
                itemIngredient("hbm:rod_quad_empty")), "hbm:rod_empty", 4),
                recipes, hbm("rod_empty_from_quad")));
        writes.add(save(output, shapelessItemRecipe(List.of(
                itemIngredient("hbm:rod_empty"), itemIngredient("hbm:rod_empty"),
                itemIngredient("hbm:rod_empty"), itemIngredient("hbm:rod_empty")),
                "hbm:rod_quad_empty"), recipes, hbm("rod_quad_empty_from_rods")));
        writes.add(save(output, shapelessItemRecipe(List.of(
                itemIngredient("hbm:rod_dual_empty"), itemIngredient("hbm:rod_dual_empty")),
                "hbm:rod_quad_empty"), recipes, hbm("rod_quad_empty_from_duals")));

        writes.add(save(output, shapedItemRecipe(List.of("LRL", "BRB", "LRL"), Map.of(
                "L", tagIngredient("c:ingots/lead"),
                "B", itemIngredient("minecraft:iron_bars"),
                "R", itemIngredient("hbm:rod_quad_empty")), "hbm:machine_waste_drum"),
                recipes, hbm("machine_waste_drum")));

        addReactorFuelBlendRecipes(writes, output);
        addZirnoxRodRecipes(writes, output);
        addBreedingRodRecipes(writes, output);
        addLeadBreedingRodRecipes(writes, output);
        addTritiumCellRecipes(writes, output);
    }

    private void addReactorFuelBlendRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        List<JsonObject> billetBlend = new ArrayList<>();
        for (int i = 0; i < 5; i++) billetBlend.add(itemIngredient("hbm:billet_th232"));
        billetBlend.add(itemIngredient("hbm:billet_u233"));
        writes.add(save(output, shapelessItemRecipe(billetBlend, "hbm:billet_thorium_fuel", 6),
                recipes, hbm("billet_thorium_fuel_from_billets")));

        List<JsonObject> nuggetBlend = new ArrayList<>();
        for (int i = 0; i < 5; i++) nuggetBlend.add(tagIngredient("c:nuggets/thorium_232"));
        nuggetBlend.add(tagIngredient("c:nuggets/uranium_233"));
        writes.add(save(output, shapelessItemRecipe(nuggetBlend, "hbm:billet_thorium_fuel", 1),
                recipes, hbm("billet_thorium_fuel_from_nuggets")));

        addSixPartFuelBlend(writes, output, "uranium_fuel", "billet_u238", "billet_u235",
                "c:nuggets/uranium_238", "c:nuggets/uranium_235");

        writes.add(save(output, shapelessItemRecipe(List.of(
                itemIngredient("hbm:billet_u238"), itemIngredient("hbm:billet_u238"),
                itemIngredient("hbm:billet_pu_mix")), "hbm:billet_plutonium_fuel", 3),
                recipes, hbm("billet_plutonium_fuel_from_billets")));
        writes.add(save(output, shapelessItemRecipe(List.of(
                tagIngredient("c:nuggets/plutonium_rg"), tagIngredient("c:nuggets/plutonium_rg"),
                tagIngredient("c:nuggets/uranium_238"), tagIngredient("c:nuggets/uranium_238"),
                tagIngredient("c:nuggets/uranium_238"), tagIngredient("c:nuggets/uranium_238")),
                "hbm:billet_plutonium_fuel", 1),
                recipes, hbm("billet_plutonium_fuel_from_nuggets")));

        writes.add(save(output, shapelessItemRecipe(List.of(
                itemIngredient("hbm:billet_uranium_fuel"), itemIngredient("hbm:billet_uranium_fuel"),
                tagIngredient("c:billets/plutonium_239")), "hbm:billet_mox_fuel", 3),
                recipes, hbm("billet_mox_fuel_from_billets")));
        writes.add(save(output, shapelessItemRecipe(List.of(
                tagIngredient("c:nuggets/plutonium_239"), tagIngredient("c:nuggets/plutonium_239"),
                itemIngredient("hbm:nugget_uranium_fuel"), itemIngredient("hbm:nugget_uranium_fuel"),
                itemIngredient("hbm:nugget_uranium_fuel"), itemIngredient("hbm:nugget_uranium_fuel")),
                "hbm:billet_mox_fuel", 1),
                recipes, hbm("billet_mox_fuel_from_nuggets")));
    }

    private void addSixPartFuelBlend(
            List<CompletableFuture<?>> writes,
            CachedOutput output,
            String fuel,
            String majorityBillet,
            String fissileBillet,
            String majorityNuggetTag,
            String fissileNuggetTag
    ) {
        List<JsonObject> billets = new ArrayList<>();
        for (int i = 0; i < 5; i++) billets.add(itemIngredient("hbm:" + majorityBillet));
        billets.add(itemIngredient("hbm:" + fissileBillet));
        writes.add(save(output, shapelessItemRecipe(billets, "hbm:billet_" + fuel, 6),
                recipes, hbm("billet_" + fuel + "_from_billets")));

        List<JsonObject> nuggets = new ArrayList<>();
        for (int i = 0; i < 5; i++) nuggets.add(tagIngredient(majorityNuggetTag));
        nuggets.add(tagIngredient(fissileNuggetTag));
        writes.add(save(output, shapelessItemRecipe(nuggets, "hbm:billet_" + fuel, 1),
                recipes, hbm("billet_" + fuel + "_from_nuggets")));
    }

    private void addZirnoxRodRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        writes.add(save(output, shapedItemRecipe(List.of("Z Z", "ZBZ", "Z Z"), Map.of(
                "Z", tagIngredient("c:nuggets/zirconium"),
                "B", tagIngredient("c:ingots/beryllium")), "hbm:rod_zirnox_empty", 4),
                recipes, hbm("rod_zirnox_empty")));

        addZirnoxFuelRodRecipe(writes, output, "natural_uranium_fuel", "c:billets/uranium");
        addZirnoxFuelRodRecipe(writes, output, "uranium_fuel", "c:billets/uranium_fuel");
        addZirnoxFuelRodRecipe(writes, output, "th232", "c:billets/thorium_232");
        addZirnoxFuelRodRecipe(writes, output, "thorium_fuel", "c:billets/thorium_fuel");
        addZirnoxFuelRodRecipe(writes, output, "mox_fuel", "c:billets/mox_fuel");
        addZirnoxFuelRodRecipe(writes, output, "plutonium_fuel", "c:billets/plutonium_fuel");
        addZirnoxFuelRodRecipe(writes, output, "u233_fuel", "c:billets/uranium_233");
        addZirnoxFuelRodRecipe(writes, output, "u235_fuel", "c:billets/uranium_235");

        writes.add(save(output, shapelessItemRecipe(List.of(
                itemIngredient("hbm:rod_zirnox_empty"),
                tagIngredient("c:ingots/lithium"),
                tagIngredient("c:ingots/lithium")), "hbm:rod_zirnox_lithium", 1),
                recipes, hbm("rod_zirnox_lithium")));
    }

    private void addZirnoxFuelRodRecipe(
            List<CompletableFuture<?>> writes,
            CachedOutput output,
            String rod,
            String billetTag
    ) {
        writes.add(save(output, shapelessItemRecipe(List.of(
                itemIngredient("hbm:rod_zirnox_empty"),
                tagIngredient(billetTag),
                tagIngredient(billetTag)), "hbm:rod_zirnox_" + rod, 1),
                recipes, hbm("rod_zirnox_" + rod)));
    }

    private void addTritiumCellRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        for (BreedingRodItem.Form form : BreedingRodItem.Form.values()) {
            int amount = switch (form) {
                case SINGLE -> 1;
                case DUAL -> 2;
                case QUAD -> 4;
            };
            String formSuffix = switch (form) {
                case SINGLE -> "";
                case DUAL -> "_dual";
                case QUAD -> "_quad";
            };
            List<JsonObject> inputs = new ArrayList<>();
            inputs.add(breedingRodIngredient(form, BreedingRodItem.Type.TRITIUM));
            for (int i = 0; i < amount; i++) inputs.add(itemIngredient("hbm:cell_empty"));
            writes.add(save(output,
                    shapelessRecipe(inputs, recipeResult("hbm:cell_tritium", amount)),
                    recipes, hbm("cell_tritium_from_rod" + formSuffix)));
        }
    }

    private void addBreedingRodRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        List<BreedingRodMaterial> materials = List.of(
                new BreedingRodMaterial(BreedingRodItem.Type.LITHIUM,
                        "c:ingots/lithium", "hbm:lithium"),
                new BreedingRodMaterial(BreedingRodItem.Type.CO,
                        "c:billets/cobalt", "hbm:billet_cobalt"),
                new BreedingRodMaterial(BreedingRodItem.Type.CO60,
                        "c:billets/cobalt_60", "hbm:billet_co60"),
                new BreedingRodMaterial(BreedingRodItem.Type.RA226,
                        "c:billets/radium_226", "hbm:billet_ra226"),
                new BreedingRodMaterial(BreedingRodItem.Type.AC227,
                        "c:billets/actinium_227", "hbm:billet_actinium"),
                new BreedingRodMaterial(BreedingRodItem.Type.TH232,
                        "c:billets/thorium_232", "hbm:billet_th232"),
                new BreedingRodMaterial(BreedingRodItem.Type.THF,
                        "c:billets/thorium_fuel", "hbm:billet_thorium_fuel"),
                new BreedingRodMaterial(BreedingRodItem.Type.U235,
                        "c:billets/uranium_235", "hbm:billet_u235"),
                new BreedingRodMaterial(BreedingRodItem.Type.NP237,
                        "c:billets/neptunium_237", "hbm:billet_neptunium"),
                new BreedingRodMaterial(BreedingRodItem.Type.U238,
                        "c:billets/uranium_238", "hbm:billet_u238"),
                new BreedingRodMaterial(BreedingRodItem.Type.PU238,
                        "c:billets/plutonium_238", "hbm:billet_pu238"),
                new BreedingRodMaterial(BreedingRodItem.Type.PU239,
                        "c:billets/plutonium_239", "hbm:billet_pu239"),
                new BreedingRodMaterial(BreedingRodItem.Type.RGP,
                        "c:billets/plutonium_rg", "hbm:billet_pu_mix"),
                new BreedingRodMaterial(BreedingRodItem.Type.WASTE,
                        "c:billets/nuclear_waste", "hbm:billet_nuclear_waste"),
                new BreedingRodMaterial(BreedingRodItem.Type.URANIUM,
                        "c:billets/uranium", "hbm:billet_uranium")
        );

        for (BreedingRodMaterial material : materials) {
            for (BreedingRodItem.Form form : BreedingRodItem.Form.values()) {
                int amount = switch (form) {
                    case SINGLE -> 1;
                    case DUAL -> 2;
                    case QUAD -> 4;
                };
                String formSuffix = switch (form) {
                    case SINGLE -> "";
                    case DUAL -> "_dual";
                    case QUAD -> "_quad";
                };

                List<JsonObject> loadingInputs = new ArrayList<>();
                loadingInputs.add(itemIngredient(form.emptyId().toString()));
                for (int i = 0; i < amount; i++) {
                    loadingInputs.add(tagIngredient(material.ingredientTag()));
                }
                writes.add(save(output,
                        shapelessRecipe(loadingInputs, breedingRodResult(form, material.type())),
                        recipes, hbm(form.id() + "_" + material.type().id())));

                writes.add(save(output,
                        shapelessRecipe(List.of(breedingRodIngredient(form, material.type())),
                                recipeResult(material.returnedItem(), amount)),
                        recipes, hbm(material.returnedItem().substring("hbm:".length())
                                + "_from_rod" + formSuffix)));
            }
        }
    }

    private void addLeadBreedingRodRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        List<JsonObject> single = new ArrayList<>();
        single.add(itemIngredient("hbm:rod_empty"));
        for (int i = 0; i < 6; i++) single.add(tagIngredient("c:nuggets/lead"));
        writes.add(save(output, shapelessRecipe(single,
                        breedingRodResult(BreedingRodItem.Form.SINGLE, BreedingRodItem.Type.LEAD)),
                recipes, hbm("rod_lead")));

        List<JsonObject> dual = new ArrayList<>();
        dual.add(itemIngredient("hbm:rod_dual_empty"));
        dual.add(tagIngredient("c:ingots/lead"));
        for (int i = 0; i < 3; i++) dual.add(tagIngredient("c:nuggets/lead"));
        writes.add(save(output, shapelessRecipe(dual,
                        breedingRodResult(BreedingRodItem.Form.DUAL, BreedingRodItem.Type.LEAD)),
                recipes, hbm("rod_dual_lead")));

        List<JsonObject> quad = new ArrayList<>();
        quad.add(itemIngredient("hbm:rod_quad_empty"));
        quad.add(tagIngredient("c:ingots/lead"));
        quad.add(tagIngredient("c:ingots/lead"));
        for (int i = 0; i < 6; i++) quad.add(tagIngredient("c:nuggets/lead"));
        writes.add(save(output, shapelessRecipe(quad,
                        breedingRodResult(BreedingRodItem.Form.QUAD, BreedingRodItem.Type.LEAD)),
                recipes, hbm("rod_quad_lead")));

        for (BreedingRodItem.Form form : BreedingRodItem.Form.values()) {
            int nuggets = switch (form) {
                case SINGLE -> 6;
                case DUAL -> 12;
                case QUAD -> 24;
            };
            String suffix = switch (form) {
                case SINGLE -> "";
                case DUAL -> "_dual";
                case QUAD -> "_quad";
            };
            writes.add(save(output, shapelessRecipe(
                            List.of(breedingRodIngredient(form, BreedingRodItem.Type.LEAD)),
                            recipeResult("hbm:nugget_lead", nuggets)),
                    recipes, hbm("nugget_lead_from_rod" + suffix)));
        }
    }

    private void addCobaltBilletRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        writes.add(save(output, compressionRecipe("nugget_cobalt", "ingot_cobalt"),
                recipes, hbm("ingot_cobalt_from_nugget_cobalt")));
        writes.add(save(output, decompressionRecipe("ingot_cobalt", "nugget_cobalt"),
                recipes, hbm("nugget_cobalt_from_ingot_cobalt")));
        writes.add(save(output, shapedRecipe(List.of("###", "###"), "nugget_cobalt", "billet_cobalt", 1),
                recipes, hbm("billet_cobalt_from_nugget_cobalt")));
        writes.add(save(output, shapelessRecipe(List.of("billet_cobalt"), "nugget_cobalt", 6),
                recipes, hbm("nugget_cobalt_from_billet_cobalt")));
        writes.add(save(output, shapelessRecipe(List.of("billet_cobalt", "billet_cobalt", "billet_cobalt"),
                "ingot_cobalt", 2), recipes, hbm("ingot_cobalt_from_billet_cobalt")));
        writes.add(save(output, shapedRecipe(List.of("##"), "ingot_cobalt", "billet_cobalt", 3),
                recipes, hbm("billet_cobalt_from_ingot_cobalt")));
    }

    private JsonObject shapelessRecipe(List<JsonObject> inputs, JsonObject result) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shapeless");
        recipe.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        inputs.forEach(ingredients::add);
        recipe.add("ingredients", ingredients);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject breedingRodResult(BreedingRodItem.Form form, BreedingRodItem.Type type) {
        JsonObject customData = new JsonObject();
        customData.addProperty("hbmBreedingRodType", type.id());
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", type.ordinal());
        JsonObject result = recipeResult("hbm:" + form.id(), 1);
        result.add("components", components);
        return result;
    }

    private JsonObject breedingRodIngredient(BreedingRodItem.Form form, BreedingRodItem.Type type) {
        JsonObject customData = new JsonObject();
        customData.addProperty("hbmBreedingRodType", type.id());
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", type.ordinal());
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("type", "neoforge:components");
        ingredient.addProperty("items", "hbm:" + form.id());
        ingredient.add("components", components);
        ingredient.addProperty("strict", false);
        return ingredient;
    }

    private record BreedingRodMaterial(BreedingRodItem.Type type, String ingredientTag,
                                       String returnedItem) { }

    private JsonObject craftedPartRecipe(boolean stock, JsonObject ingredient, String material, int metadata) {
        JsonObject recipe = shapedBase(stock ? List.of("WWW", "  W") : List.of("W ", " W", " W"));
        JsonObject key = new JsonObject();
        key.add("W", ingredient);
        recipe.add("key", key);
        String part = stock ? "part_stock" : "part_grip";
        recipe.add("result", materialComponentResult("hbm:" + part, material, metadata, 1));
        return recipe;
    }

    private JsonObject deshMotorRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("P", tagIngredient("c:ingots/polymer"));
        key.put("C", materialComponentIngredient("hbm:wire_dense", "gold", 7900));
        key.put("D", tagIngredient("c:ingots/desh"));
        key.put("M", itemIngredient("hbm:motor"));
        return shapedItemRecipe(List.of("PCP", "DMD", "PCP"), key, "hbm:motor_desh");
    }

    private JsonObject deshBladesRecipe() {
        Map<String, JsonObject> key = new LinkedHashMap<>();
        key.put("P", itemIngredient("hbm:plate_desh"));
        key.put("B", itemIngredient("hbm:blades_titanium"));
        return shapedItemRecipe(List.of(" P ", "PBP", " P "), key, "hbm:blades_desh");
    }

    private void addUnlockedWeaponRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        writes.add(save(output, weaponRecipe(List.of("BRM", "  G"), Map.of(
                "B", foundryPart("part_barrel_light", "steel", 30),
                "R", foundryPart("part_receiver_light", "steel", 30),
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "G", foundryPart("part_grip", "wood", 3)), "gun_light_revolver"),
                recipes, hbm("gun_light_revolver")));
        writes.add(save(output, weaponRecipe(List.of(" M ", "MAM", " M "), Map.of(
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "A", itemIngredient("hbm:gun_light_revolver")), "gun_light_revolver_atlas"),
                recipes, hbm("gun_light_revolver_atlas")));
        writes.add(save(output, weaponRecipe(List.of("BRP", "BMS"), Map.of(
                "B", foundryPart("part_barrel_light", "steel", 30),
                "R", foundryPart("part_receiver_light", "gunmetal", 49),
                "P", tagIngredient("c:plates/gunmetal"),
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "S", foundryPart("part_stock", "wood", 3)), "gun_henry"),
                recipes, hbm("gun_henry")));
        writes.add(save(output, weaponRecipe(List.of(" M ", "PGP", " M "), Map.of(
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "P", tagIngredient("c:plates/gold"),
                "G", itemIngredient("hbm:gun_henry")), "gun_henry_lincoln"),
                recipes, hbm("gun_henry_lincoln")));
        writes.add(save(output, weaponRecipe(List.of("BRM", "BGS"), Map.of(
                "B", foundryPart("part_barrel_light", "steel", 30),
                "R", foundryPart("part_receiver_light", "steel", 30),
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "G", materialComponentIngredient("hbm:bolt", "steel", 30),
                "S", foundryPart("part_stock", "wood", 3)), "gun_maresleg"),
                recipes, hbm("gun_maresleg")));
        writes.add(save(output, weaponRecipe(List.of("SMS"), Map.of(
                "S", itemIngredient("hbm:gun_maresleg"),
                "M", foundryPart("part_mechanism", "weapon_steel", 50)), "gun_maresleg_akimbo"),
                recipes, hbm("gun_maresleg_akimbo")));
        writes.add(save(output, weaponRecipe(List.of("IPI", "PGP", "IPI"), Map.of(
                "I", itemIngredient("minecraft:iron_bars"),
                "P", itemIngredient("hbm:plate_weaponsteel"),
                "G", itemIngredient("hbm:gun_maresleg")), "gun_maresleg_broken"),
                recipes, hbm("gun_maresleg_broken")));
        writes.add(save(output, weaponRecipe(List.of("BRM", "  G"), Map.of(
                "B", foundryPart("part_barrel_heavy", "steel", 30),
                "R", foundryPart("part_receiver_light", "steel", 30),
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "G", foundryPart("part_grip", "steel", 30)), "gun_flaregun"),
                recipes, hbm("gun_flaregun")));
        writes.add(save(output, weaponRecipe(List.of("BRS", "GMG"), Map.of(
                "B", foundryPart("part_barrel_light", "dura_steel", 33),
                "R", foundryPart("part_receiver_light", "dura_steel", 33),
                "S", foundryPart("part_stock", "wood", 3),
                "G", foundryPart("part_grip", "wood", 3),
                "M", foundryPart("part_mechanism", "gunmetal", 49)), "gun_am180"),
                recipes, hbm("gun_am180")));
        writes.add(save(output, weaponRecipe(List.of("BB ", "BBM", "G G"), Map.of(
                "B", foundryPart("part_barrel_light", "dura_steel", 33),
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "G", foundryPart("part_grip", "wood", 3)), "gun_liberator"),
                recipes, hbm("gun_liberator")));
        writes.add(save(output, weaponRecipe(List.of("BM ", "BRS", "G  "), Map.of(
                "B", foundryPart("part_barrel_heavy", "dura_steel", 33),
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "R", foundryPart("part_receiver_light", "dura_steel", 33),
                "S", foundryPart("part_stock", "wood", 3),
                "G", foundryPart("part_grip", "wood", 3)), "gun_congolake"),
                recipes, hbm("gun_congolake")));
        writes.add(save(output, weaponRecipe(List.of(" MG", "BBR", " GM"), Map.of(
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "G", foundryPart("part_grip", "dura_steel", 33),
                "B", foundryPart("part_barrel_heavy", "dura_steel", 33),
                "R", foundryPart("part_receiver_heavy", "dura_steel", 33)), "gun_flamer"),
                recipes, hbm("gun_flamer")));
        writes.add(save(output, weaponRecipe(List.of(" M ", "MFM", " M "), Map.of(
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "F", itemIngredient("hbm:gun_flamer")), "gun_flamer_topaz"),
                recipes, hbm("gun_flamer_topaz")));
        writes.add(save(output, weaponRecipe(List.of("GSG", "PFP", "GDG"), Map.of(
                "G", tagIngredient("c:plates/gold"),
                "S", itemIngredient("minecraft:slime_ball"),
                "P", itemIngredient("minecraft:blaze_powder"),
                "F", itemIngredient("hbm:gun_flamer"),
                "D", itemIngredient("hbm:stick_dynamite")), "gun_flamer_daybreaker"),
                recipes, hbm("gun_flamer_daybreaker")));
        writes.add(save(output, weaponRecipe(List.of("BRM", "  G"), Map.of(
                "B", foundryPart("part_barrel_light", "desh", 42),
                "R", foundryPart("part_receiver_light", "desh", 42),
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "G", foundryPart("part_grip", "wood", 3)), "gun_heavy_revolver"),
                recipes, hbm("gun_heavy_revolver")));
        writes.add(save(output, weaponRecipe(List.of("BRM", "G S"), Map.of(
                "B", foundryPart("part_barrel_light", "desh", 42),
                "R", foundryPart("part_receiver_light", "desh", 42),
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "G", foundryPart("part_grip", "wood", 3),
                "S", foundryPart("part_stock", "wood", 3)), "gun_carbine"),
                recipes, hbm("gun_carbine")));
        writes.add(save(output, weaponRecipe(List.of(" M ", "MCM", " M "), Map.of(
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "C", itemIngredient("hbm:gun_carbine")), "gun_mas36"),
                recipes, hbm("gun_mas36")));
        writes.add(save(output, weaponRecipe(List.of("BRS", " GM"), Map.of(
                "B", foundryPart("part_barrel_light", "desh", 42),
                "R", foundryPart("part_receiver_light", "desh", 42),
                "S", foundryPart("part_stock", "polymer", 20_001),
                "G", foundryPart("part_grip", "polymer", 20_001),
                "M", foundryPart("part_mechanism", "gunmetal", 49)), "gun_uzi"),
                recipes, hbm("gun_uzi")));
        writes.add(save(output, weaponRecipe(List.of("UMU"), Map.of(
                "U", itemIngredient("hbm:gun_uzi"),
                "M", foundryPart("part_mechanism", "weapon_steel", 50)), "gun_uzi_akimbo"),
                recipes, hbm("gun_uzi_akimbo")));
        writes.add(save(output, weaponRecipe(List.of("BRM", "BGS"), Map.of(
                "B", foundryPart("part_barrel_light", "desh", 42),
                "R", foundryPart("part_receiver_light", "desh", 42),
                "M", foundryPart("part_mechanism", "gunmetal", 49),
                "G", foundryPart("part_grip", "polymer", 20_001),
                "S", foundryPart("part_stock", "desh", 42)), "gun_spas12"),
                recipes, hbm("gun_spas12")));
        writes.add(save(output, weaponRecipe(List.of("BBB", "PGM"), Map.of(
                "B", foundryPart("part_barrel_heavy", "desh", 42),
                "P", materialComponentIngredient("hbm:plate_cast", "steel", 30),
                "G", foundryPart("part_grip", "desh", 42),
                "M", foundryPart("part_mechanism", "gunmetal", 49)), "gun_panzerschreck"),
                recipes, hbm("gun_panzerschreck")));
        writes.add(save(output, weaponRecipe(List.of("BRM", "  G"), Map.of(
                "B", foundryPart("part_barrel_light", "weapon_steel", 50),
                "R", foundryPart("part_receiver_light", "weapon_steel", 50),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "G", foundryPart("part_grip", "polymer", 20_001)), "gun_star_f"),
                recipes, hbm("gun_star_f")));
        writes.add(save(output, weaponRecipe(List.of("UMU"), Map.of(
                "U", itemIngredient("hbm:gun_star_f"),
                "M", foundryPart("part_mechanism", "weapon_steel", 50)), "gun_star_f_akimbo"),
                recipes, hbm("gun_star_f_akimbo")));
        writes.add(save(output, weaponRecipe(List.of("BRM", "WGS"), Map.of(
                "B", foundryPart("part_barrel_light", "weapon_steel", 50),
                "R", foundryPart("part_receiver_light", "weapon_steel", 50),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "W", foundryPart("part_grip", "wood", 3),
                "G", foundryPart("part_grip", "rubber", 20_003),
                "S", foundryPart("part_stock", "wood", 3)), "gun_g3"),
                recipes, hbm("gun_g3")));
        writes.add(save(output, weaponRecipe(List.of(" M ", "MPM", " M "), Map.of(
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "P", itemIngredient("hbm:gun_g3")), "gun_g3_zebra"),
                recipes, hbm("gun_g3_zebra")));
        writes.add(save(output, weaponRecipe(List.of("BRM", "G G"), Map.of(
                "B", foundryPart("part_barrel_heavy", "ferrouranium", 37),
                "R", foundryPart("part_receiver_heavy", "ferrouranium", 37),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "G", foundryPart("part_grip", "polymer", 20_001)), "gun_autoshotgun"),
                recipes, hbm("gun_autoshotgun")));
        writes.add(save(output, weaponRecipe(List.of(" M ", "MAM", " M "), Map.of(
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "A", itemIngredient("hbm:gun_autoshotgun")), "gun_autoshotgun_shredder"),
                recipes, hbm("gun_autoshotgun_shredder")));
        writes.add(save(output, weaponRecipe(List.of("BNB", "CAC", "BSB"), Map.of(
                "B", materialComponentIngredient("hbm:bolt", "steel", 30),
                "N", itemIngredient("minecraft:nether_star"),
                "C", customComponentIngredient("hbm:circuit", "type", "advanced", 9),
                "A", itemIngredient("hbm:gun_autoshotgun"),
                "S", tagIngredient("c:ingots/schrabidium")), "gun_autoshotgun_sexy"),
                recipes, hbm("gun_autoshotgun_sexy")));
        writes.add(save(output, weaponRecipe(List.of("BCB", "BMB", "GG "), Map.of(
                "B", foundryPart("part_barrel_heavy", "ferrouranium", 37),
                "C", customComponentIngredient("hbm:circuit", "type", "advanced", 9),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "G", foundryPart("part_grip", "polymer", 20_001)), "gun_quadro"),
                recipes, hbm("gun_quadro")));
        writes.add(save(output, weaponRecipe(List.of("BRM", "  G"), Map.of(
                "B", resistantPart("part_barrel_light"),
                "R", resistantPart("part_receiver_light"),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "G", foundryPart("part_grip", "polymer", 20_001)), "gun_lag"),
                recipes, hbm("gun_lag")));
        writes.add(save(output, weaponRecipe(List.of("BMG", "BRE", "BGM"), Map.of(
                "B", resistantPart("part_barrel_light"),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "G", foundryPart("part_grip", "polymer", 20_001),
                "R", resistantPart("part_receiver_heavy"),
                "E", itemIngredient("hbm:motor_desh")), "gun_minigun"),
                recipes, hbm("gun_minigun")));
        writes.add(save(output, weaponRecipe(List.of(" CM", "BBB", "G  "), Map.of(
                "C", customComponentIngredient("hbm:circuit", "type", "advanced", 9),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "B", resistantPart("part_barrel_heavy"),
                "G", foundryPart("part_grip", "polymer", 20_001)), "gun_missile_launcher"),
                recipes, hbm("gun_missile_launcher")));
        writes.add(save(output, weaponRecipe(List.of(" GG", "BRM", " D "), Map.of(
                "G", foundryPart("part_grip", "polymer", 20_001),
                "B", foundryPart("part_barrel_heavy", "weapon_steel", 50),
                "R", foundryPart("part_receiver_heavy", "weapon_steel", 50),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                // Source wants a Weapon Steel shell. The foundry threw it back, so Steel gets the job.
                "D", materialComponentIngredient("hbm:shell", "steel", 30)), "gun_mk108"),
                recipes, hbm("gun_mk108")));
        writes.add(save(output, weaponRecipe(List.of(" D ", "BRS", "GGM"), Map.of(
                "D", customComponentIngredient("hbm:circuit", "type", "advanced", 9),
                "B", foundryPart("part_barrel_light", "weapon_steel", 50),
                "R", foundryPart("part_receiver_light", "weapon_steel", 50),
                "S", foundryPart("part_stock", "polymer", 20_001),
                "G", foundryPart("part_grip", "polymer", 20_001),
                "M", foundryPart("part_mechanism", "weapon_steel", 50)), "gun_stg77"),
                recipes, hbm("gun_stg77")));
        writes.add(save(output, weaponRecipe(List.of("  G", "BRM", "  G"), Map.of(
                "G", foundryPart("part_grip", "wood", 3),
                "B", foundryPart("part_barrel_heavy", "ferrouranium", 37),
                "R", foundryPart("part_receiver_heavy", "ferrouranium", 37),
                "M", foundryPart("part_mechanism", "weapon_steel", 50)), "gun_m2"),
                recipes, hbm("gun_m2")));
        writes.add(save(output, weaponRecipe(List.of("CCC", "BRB", "MGE"), Map.of(
                "C", itemIngredient("hbm:coil_copper"),
                "B", resistantPart("part_barrel_heavy"),
                "R", resistantPart("part_receiver_heavy"),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "G", foundryPart("part_grip", "polymer", 20_001),
                "E", customComponentIngredient("hbm:circuit", "type", "advanced", 9)),
                "gun_tesla_cannon"), recipes, hbm("gun_tesla_cannon")));
        writes.add(save(output, weaponRecipe(List.of(" C ", "BRS", " MG"), Map.of(
                "C", customComponentIngredient("hbm:circuit", "type", "advanced", 9),
                "B", foundryPart("part_barrel_heavy", "ferrouranium", 37),
                "R", foundryPart("part_receiver_heavy", "ferrouranium", 37),
                "S", foundryPart("part_stock", "wood", 3),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "G", foundryPart("part_grip", "wood", 3)), "gun_amat"),
                recipes, hbm("gun_amat")));
        writes.add(save(output, weaponRecipe(List.of("SAS", "AGA", "SAS"), Map.of(
                "S", tagIngredient("c:ingots/schrabidium"),
                "A", tagIngredient("c:plates/aluminum"),
                "G", itemIngredient("hbm:gun_amat")), "gun_amat_subtlety"),
                recipes, hbm("gun_amat_subtlety")));
        writes.add(save(output, weaponRecipe(List.of("SDS", "MAG", "SDS"), Map.of(
                "S", tagIngredient("c:ingots/schrabidium"),
                "D", materialComponentIngredient("hbm:plate_cast", "dura_steel", 33),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "A", itemIngredient("hbm:gun_amat"),
                "G", foundryPart("part_stock", "polymer", 20_001)), "gun_amat_penance"),
                recipes, hbm("gun_amat_penance")));
        writes.add(save(output, weaponRecipe(List.of("NMN", "MHM", "NMN"), Map.of(
                "N", itemIngredient("minecraft:nether_star"),
                "M", foundryPart("part_mechanism", "weapon_steel", 50),
                "H", itemIngredient("hbm:gun_heavy_revolver")), "gun_hangman"),
                recipes, hbm("gun_hangman")));
    }

    private JsonObject weaponRecipe(List<String> pattern, Map<String, JsonObject> key, String output) {
        return shapedItemRecipe(pattern, key, "hbm:" + output);
    }

    /** Ammo press inputs flattened into shapeless crafting until that machine arrives. */
    private JsonObject capacitorRecipe(String type, int model, int silicon, boolean niobium) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shapeless");
        root.addProperty("category", "combat");
        JsonArray ingredients = new JsonArray();
        ingredients.add(itemIngredient("hbm:ingot_polymer"));
        ingredients.add(itemIngredient("hbm:ingot_polymer"));
        for (int i = 0; i < silicon; i++) ingredients.add(itemIngredient("hbm:billet_silicon"));
        if (niobium) ingredients.add(itemIngredient("hbm:ingot_niobium"));
        root.add("ingredients", ingredients);
        JsonObject customData = new JsonObject(); customData.addProperty("hbm_ammo_type", type);
        JsonObject components = new JsonObject(); components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", model);
        JsonObject result = new JsonObject(); result.addProperty("id", "hbm:ammo_standard");
        result.addProperty("count", 4); result.add("components", components); root.add("result", result);
        return root;
    }

    private JsonObject foundryPart(String part, String material, int metadata) {
        return materialComponentIngredient("hbm:" + part, material, metadata);
    }

    private JsonObject resistantPart(String part) {
        JsonArray children = new JsonArray();
        children.add(foundryPart(part, "technetium_steel", 36));
        children.add(foundryPart(part, "cadmium_steel", 43));
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("type", "neoforge:compound");
        ingredient.add("children", children);
        return ingredient;
    }

    private JsonObject wireCraftingRecipe(WireData wire) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray(); pattern.add("###"); recipe.add("pattern", pattern);
        JsonObject key = new JsonObject(); key.add("#", tagIngredient(wire.ingotTag())); recipe.add("key", key);
        recipe.add("result", wireResult(wire.id(), wire.metadata(), 24));
        return recipe;
    }

    private JsonObject wireRecompressionRecipe(WireData wire) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray(); pattern.add("###"); pattern.add("###"); pattern.add("###");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject(); key.add("#", componentIngredient(wire.id(), wire.metadata()));
        recipe.add("key", key);
        JsonObject result = new JsonObject(); result.addProperty("id", wire.ingotItem()); result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject coilRecipe(String output, String wire, int metadata, String core) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray(); pattern.add("WWW"); pattern.add("WIW"); pattern.add("WWW");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("W", componentIngredient(wire, metadata));
        key.add("I", tagIngredient("c:ingots/" + core));
        recipe.add("key", key);
        JsonObject result = new JsonObject(); result.addProperty("id", "hbm:" + output); result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject ringCoilRecipe(String output, String coil, String plate) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray(); pattern.add(" C "); pattern.add("CPC"); pattern.add(" C ");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject(); key.add("C", itemIngredient("hbm:" + coil));
        key.add("P", tagIngredient("c:plates/" + plate)); recipe.add("key", key);
        JsonObject result = new JsonObject(); result.addProperty("id", "hbm:" + output); result.addProperty("count", 2);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject steelTankRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray(); pattern.add("STS"); pattern.add("S S"); pattern.add("STS");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject(); key.add("S", tagIngredient("c:plates/steel"));
        key.add("T", tagIngredient("c:plates/titanium")); recipe.add("key", key);
        JsonObject result = new JsonObject(); result.addProperty("id", "hbm:tank_steel"); result.addProperty("count", 2);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject motorRecipe(boolean steel) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add(" R "); pattern.add("ICI"); pattern.add(steel ? " T " : "ITI");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("R", componentIngredient("red_copper", 31));
        key.add("T", itemIngredient("hbm:coil_copper_torus"));
        key.add("I", tagIngredient("c:plates/" + (steel ? "steel" : "iron")));
        key.add("C", itemIngredient("hbm:coil_copper"));
        recipe.add("key", key);
        JsonObject result = new JsonObject(); result.addProperty("id", "hbm:motor"); result.addProperty("count", 2);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject coilBatterySocketRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "redstone");
        JsonArray pattern = new JsonArray(); pattern.add("I I"); pattern.add("I I"); pattern.add("IRI");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject(); key.add("I", itemIngredient("hbm:plate_polymer"));
        key.add("R", itemIngredient("hbm:coil_copper")); recipe.add("key", key);
        JsonObject result = new JsonObject(); result.addProperty("id", "hbm:machine_battery_socket"); result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject boltModel() {
        JsonObject root = generatedItemModel("bolt_steel");
        JsonArray overrides = new JsonArray();
        for (int metadata : List.of(30, 8200, 7400)) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject(); predicate.addProperty("custom_model_data", metadata);
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/bolt_" + (metadata == 30 ? "steel" : metadata == 8200 ? "lead" : "tungsten"));
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject boltRecipe(String material, int metadata) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray(); pattern.add("#"); pattern.add("#");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject(); key.add("#", tagIngredient("c:ingots/" + material));
        recipe.add("key", key);
        JsonObject customData = new JsonObject(); customData.addProperty("material", material);
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", metadata);
        JsonObject result = new JsonObject(); result.addProperty("id", "hbm:bolt");
        result.addProperty("count", 16); result.add("components", components);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject castPlateModel() {
        JsonObject root = generatedItemModel("plate_cast_iron");
        JsonArray overrides = new JsonArray();
        int[] metadata = {30, 33, 36, 43, 2200, 2600, 2900, 8200};
        String[] materials = {"steel", "dura_steel", "technetium_steel", "cadmium_steel",
                "titanium", "iron", "copper", "lead"};
        for (int i = 0; i < metadata.length; i++) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject(); predicate.addProperty("custom_model_data", metadata[i]);
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/plate_cast_" + materials[i]);
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject weldedPlateModel() {
        JsonObject root = generatedItemModel("plate_welded_steel");
        JsonArray overrides = new JsonArray();
        int[] metadata = {30, 36, 43};
        String[] materials = {"steel", "technetium_steel", "cadmium_steel"};
        for (int i = 0; i < metadata.length; i++) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", metadata[i]);
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/plate_welded_" + materials[i]);
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject pipeModel() {
        JsonObject root = generatedItemModel("pipe_copper");
        JsonArray overrides = new JsonArray();
        int[] metadata = {30, 33, 2900, 8200};
        String[] materials = {"steel", "dura_steel", "copper", "lead"};
        for (int i = 0; i < metadata.length; i++) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject(); predicate.addProperty("custom_model_data", metadata[i]);
            override.add("predicate", predicate);
            override.addProperty("model", "hbm:item/pipe_" + materials[i]);
            overrides.add(override);
        }
        root.add("overrides", overrides);
        return root;
    }

    private JsonObject ironCastPlateRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray(); pattern.add("BPB"); pattern.add("BPB"); pattern.add("BPB");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("B", materialComponentIngredient("hbm:bolt", "steel", 30));
        key.add("P", tagIngredient("c:plates/iron"));
        recipe.add("key", key);
        JsonObject customData = new JsonObject(); customData.addProperty("material", "iron");
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", 2600);
        JsonObject result = new JsonObject(); result.addProperty("id", "hbm:plate_cast");
        result.addProperty("count", 1); result.add("components", components);
        recipe.add("result", result);
        return recipe;
    }

    private JsonObject materialComponentIngredient(String item, String material, int metadata) {
        JsonObject customData = new JsonObject(); customData.addProperty("material", material);
        JsonObject components = new JsonObject(); components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", metadata);
        JsonObject ingredient = new JsonObject(); ingredient.addProperty("type", "neoforge:components");
        ingredient.addProperty("items", item); ingredient.add("components", components);
        ingredient.addProperty("strict", false);
        return ingredient;
    }

    private JsonObject customComponentIngredient(String item, String key, String value, int metadata) {
        JsonObject customData = new JsonObject(); customData.addProperty(key, value);
        JsonObject components = new JsonObject(); components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", metadata);
        JsonObject ingredient = new JsonObject(); ingredient.addProperty("type", "neoforge:components");
        ingredient.addProperty("items", item); ingredient.add("components", components);
        ingredient.addProperty("strict", false);
        return ingredient;
    }

    private JsonObject materialComponentOrExternalTagIngredient(String item, String material, int metadata,
                                                                 String externalTag) {
        JsonObject difference = new JsonObject();
        difference.addProperty("type", "neoforge:difference");
        difference.add("base", tagIngredient(externalTag));
        difference.add("subtracted", itemIngredient(item));
        JsonArray children = new JsonArray();
        children.add(materialComponentIngredient(item, material, metadata));
        children.add(difference);
        JsonObject compound = new JsonObject();
        compound.addProperty("type", "neoforge:compound");
        compound.add("children", children);
        return compound;
    }

    private JsonObject wireResult(String material, int metadata, int count) {
        return materialComponentResult("hbm:wire_fine", material, metadata, count);
    }

    private JsonObject materialComponentResult(String item, String material, int metadata, int count) {
        JsonObject customData = new JsonObject(); customData.addProperty("material", material);
        JsonObject components = new JsonObject();
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", metadata);
        JsonObject result = new JsonObject(); result.addProperty("id", item);
        result.addProperty("count", count); result.add("components", components);
        return result;
    }

    private JsonObject componentIngredient(String material, int metadata) {
        JsonObject customData = new JsonObject(); customData.addProperty("material", material);
        JsonObject components = new JsonObject(); components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", metadata);
        JsonObject ingredient = new JsonObject(); ingredient.addProperty("type", "neoforge:components");
        ingredient.addProperty("items", "hbm:wire_fine"); ingredient.add("components", components);
        ingredient.addProperty("strict", true);
        return ingredient;
    }

    private record WireData(String id, int metadata, String ingotTag, String ingotItem, boolean recompress) { }

    private void addFlatStampRecipes(List<CompletableFuture<?>> writes, CachedOutput output) {
        List<String> bricks = List.of("minecraft:brick", "minecraft:nether_brick");
        for (String brick : bricks) {
            String suffix = brick.endsWith("nether_brick") ? "nether_brick" : "brick";
            addFlatStampRecipe(writes, output, "stone", tagIngredient("minecraft:stone_crafting_materials"), brick, suffix);
            addFlatStampRecipe(writes, output, "iron", tagIngredient("c:ingots/iron"), brick, suffix);
            addFlatStampRecipe(writes, output, "steel", tagIngredient("c:ingots/steel"), brick, suffix);
            addFlatStampRecipe(writes, output, "titanium", tagIngredient("c:ingots/titanium"), brick, suffix);
            addFlatStampRecipe(writes, output, "obsidian", itemIngredient("minecraft:obsidian"), brick, suffix);
        }
    }

    private void addFlatStampRecipe(
            List<CompletableFuture<?>> writes,
            CachedOutput output,
            String tier,
            JsonObject lowerIngredient,
            String brick,
            String suffix
    ) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("III");
        pattern.add("SSS");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("I", itemIngredient(brick));
        key.add("S", lowerIngredient);
        root.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "hbm:stamp_" + tier + "_flat");
        root.add("result", result);
        writes.add(save(output, root, recipes, hbm("stamp_" + tier + "_flat_from_" + suffix)));
    }

    private JsonObject itemIngredient(String id) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", id);
        return ingredient;
    }

    private JsonObject itemResult(String id, int count) {
        JsonObject result = new JsonObject();
        result.addProperty("id", id);
        result.addProperty("count", count);
        return result;
    }

    private JsonObject tagIngredient(String id) {
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("tag", id);
        return ingredient;
    }

    private List<String> stampIds() {
        List<String> ids = new ArrayList<>();
        for (String tier : List.of("stone", "iron", "steel", "titanium", "obsidian", "desh")) {
            for (String shape : List.of("flat", "plate", "wire", "circuit")) {
                ids.add("stamp_" + tier + "_" + shape);
            }
        }
        ids.addAll(List.of("stamp_357", "stamp_44", "stamp_9", "stamp_50",
                "stamp_desh_357", "stamp_desh_44", "stamp_desh_9", "stamp_desh_50"));
        return ids;
    }

    private String stampEnglishName(String id) {
        return switch (id) {
            case "stamp_357" -> ".357 Magnum Stamp";
            case "stamp_44" -> ".44 Magnum Stamp";
            case "stamp_9" -> "Small Caliber Stamp";
            case "stamp_50" -> "Large Caliber Stamp";
            case "stamp_desh_357" -> ".357 Magnum Stamp (Desh)";
            case "stamp_desh_44" -> ".44 Magnum Stamp (Desh)";
            case "stamp_desh_9" -> "Small Caliber Stamp (Desh)";
            case "stamp_desh_50" -> "Large Caliber Stamp (Desh)";
            default -> {
                String[] parts = id.substring("stamp_".length()).split("_");
                String tier = Character.toUpperCase(parts[0].charAt(0)) + parts[0].substring(1);
                String shape = Character.toUpperCase(parts[1].charAt(0)) + parts[1].substring(1);
                yield shape + " Stamp (" + tier + ")";
            }
        };
    }

    private String stampTexture(String id) {
        return switch (id) {
            case "stamp_desh_357" -> "stamp_357_desh";
            case "stamp_desh_44" -> "stamp_44_desh";
            case "stamp_desh_9" -> "stamp_9_desh";
            case "stamp_desh_50" -> "stamp_50_desh";
            default -> id;
        };
    }

    private HazardousMaterialDefinitions.ItemDefinition findHazardForm(
            String material,
            HazardousMaterialDefinitions.Form form
    ) {
        return HazardousMaterialDefinitions.ITEMS.stream()
                .filter(definition -> definition.commonMaterial() != null
                        && definition.commonMaterial().equals(material) && definition.form() == form)
                .findFirst()
                .orElse(null);
    }

    private JsonObject pepperboxRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "combat");
        JsonArray pattern = new JsonArray();
        pattern.add("IIW");
        pattern.add("  C");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("I", tagIngredient("c:ingots/iron"));
        key.add("W", tagIngredient("minecraft:planks"));
        key.add("C", tagIngredient("c:ingots/copper"));
        root.add("key", key);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:gun_pepperbox");
        result.addProperty("count", 1);
        root.add("result", result);
        return root;
    }

    private JsonObject blackPowderAmmoRecipe(String type, JsonObject projectile) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "combat");
        JsonArray pattern = new JsonArray();
        pattern.add("C");
        pattern.add("P");
        pattern.add("G");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("C", projectile);
        key.add("P", itemIngredient("minecraft:paper"));
        key.add("G", itemIngredient("minecraft:gunpowder"));
        root.add("key", key);

        int model = switch (type) {
            case "stone_ap" -> 1;
            case "stone_iron" -> 2;
            case "stone_shot" -> 3;
            default -> 0;
        };
        JsonObject components = new JsonObject();
        JsonObject customData = new JsonObject();
        customData.addProperty("hbm_ammo_type", type);
        components.add("minecraft:custom_data", customData);
        components.addProperty("minecraft:custom_model_data", model);
        JsonObject result = new JsonObject();
        result.addProperty("id", "hbm:ammo_standard");
        result.addProperty("count", 6);
        result.add("components", components);
        root.add("result", result);
        return root;
    }

    private JsonObject radiationDamageType() {
        JsonObject root = new JsonObject();
        root.addProperty("exhaustion", 0.1F);
        root.addProperty("message_id", "radiation");
        root.addProperty("scaling", "never");
        return root;
    }

    private JsonObject nuclearBlastDamageType() {
        JsonObject root = new JsonObject();
        root.addProperty("exhaustion", 0.1F);
        root.addProperty("message_id", "nuclear_blast");
        root.addProperty("scaling", "never");
        root.addProperty("effects", "burning");
        return root;
    }

    private JsonObject rubbleDamageType() {
        JsonObject root = new JsonObject();
        root.addProperty("exhaustion", 0.1F);
        root.addProperty("message_id", "rubble");
        root.addProperty("scaling", "never");
        return root;
    }

    // Shrapnel is a projectile and respects armor, grudgingly.
    private JsonObject shrapnelDamageType() {
        JsonObject root = new JsonObject();
        root.addProperty("exhaustion", 0.1F);
        root.addProperty("message_id", "shrapnel");
        root.addProperty("scaling", "never");
        return root;
    }

    private JsonObject blenderDamageType() {
        JsonObject root = new JsonObject();
        root.addProperty("exhaustion", 0.1F);
        root.addProperty("message_id", "blender");
        root.addProperty("scaling", "never");
        return root;
    }

    private JsonObject sawbladeRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:crafting_shaped");
        root.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("PPP"); pattern.add("PIP"); pattern.add("PPP");
        root.add("pattern", pattern);
        JsonObject key = new JsonObject();
        key.add("P", tagIngredient("c:plates/steel"));
        key.add("I", tagIngredient("c:ingots/iron"));
        root.add("key", key);
        root.add("result", recipeResult("hbm:sawblade", 1));
        return root;
    }

    private JsonObject bulletDamageType() {
        JsonObject root = new JsonObject();
        root.addProperty("exhaustion", 0.1F);
        root.addProperty("message_id", "bullet");
        root.addProperty("scaling", "never");
        return root;
    }

    private JsonObject damageType(String messageId) {
        JsonObject root = new JsonObject();
        root.addProperty("exhaustion", 0.1F);
        root.addProperty("message_id", messageId);
        root.addProperty("scaling", "never");
        return root;
    }

    private JsonObject absoluteDamageType(String messageId) {
        return absoluteDamageType(messageId, 0.1F);
    }

    private JsonObject absoluteDamageType(String messageId, float exhaustion) {
        JsonObject root = new JsonObject();
        root.addProperty("exhaustion", exhaustion);
        root.addProperty("message_id", messageId);
        root.addProperty("scaling", "never");
        return root;
    }

    private JsonObject tag(String value) {
        return tag(value, true);
    }

    private JsonObject tag(String value, boolean addHbmNamespace) {
        JsonArray values = new JsonArray();
        values.add(addHbmNamespace ? "hbm:" + value : value);
        return tag(values);
    }

    private JsonObject tag(JsonArray values) {
        JsonObject root = new JsonObject();
        root.addProperty("replace", false);
        root.add("values", values);
        return root;
    }

    private List<String> hazardousMaterialAliases(String commonMaterial) {
        return switch (commonMaterial) {
            case "uranium_233" -> List.of("uranium233", "u233");
            case "uranium_235" -> List.of("uranium235", "u235");
            case "uranium_238" -> List.of("uranium238", "u238");
            case "thorium_232" -> List.of("thorium232", "th232", "thorium");
            case "radium_226" -> List.of("radium226", "ra226");
            case "actinium_227" -> List.of("actinium227", "ac227");
            default -> List.of();
        };
    }

    private String legacyMaterialAlias(String commonMaterial) {
        return switch (commonMaterial) {
            case "red_copper" -> "mingrade";
            case "technetium_steel" -> "tc_alloy";
            case "cadmium_steel" -> "cd_alloy";
            case "combine_steel" -> "cmb_steel";
            case "lanthanium" -> "lanthanum";
            default -> null;
        };
    }

    private ResourceLocation hbm(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private ResourceLocation minecraft(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }

    @Override
    public String getName() {
        return "HBM material resources";
    }
}
