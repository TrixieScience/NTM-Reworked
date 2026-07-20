package com.hbm.ntm.recipe;

import com.hbm.ntm.content.MaterialDefinitions;
import com.hbm.ntm.item.OreChunkItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Shredder defaults go in before generated material conversions. First recipe wins.
 */
public final class ShredderRecipes {
    private static final List<ShredderRecipe> RECIPES = buildRecipes();

    private ShredderRecipes() {
    }

    public static ItemStack getResult(ItemStack input) {
        if (input.isEmpty()) {
            return new ItemStack(ModItems.get("scrap").get());
        }
        for (ShredderRecipe recipe : RECIPES) {
            if (recipe.input().test(input)) {
                return recipe.output().get().copy();
            }
        }
        return new ItemStack(ModItems.get("scrap").get());
    }

    public static int recipeCount() {
        return RECIPES.size();
    }

    private static List<ShredderRecipe> buildRecipes() {
        List<ShredderRecipe> recipes = new ArrayList<>();

        exact(recipes, () -> ModItems.get("scrap").get(), item("dust"));
        exact(recipes, () -> ModItems.get("dust").get(), item("dust"));
        exact(recipes, () -> ModItems.get("dust_tiny").get(), item("dust_tiny"));
        exact(recipes, () -> Blocks.GLOWSTONE, vanilla(Items.GLOWSTONE_DUST, 4));
        exact(recipes, () -> Blocks.QUARTZ_BLOCK, item("powder_quartz", 4));
        exact(recipes, () -> Blocks.CHISELED_QUARTZ_BLOCK, item("powder_quartz", 4));
        exact(recipes, () -> Blocks.QUARTZ_PILLAR, item("powder_quartz", 4));
        exact(recipes, () -> Blocks.QUARTZ_STAIRS, item("powder_quartz", 3));
        exact(recipes, () -> Blocks.QUARTZ_SLAB, item("powder_quartz", 2));
        exact(recipes, () -> Items.QUARTZ, item("powder_quartz"));
        exact(recipes, () -> Items.COAL, item("powder_coal"));
        exact(recipes, () -> ModItems.legacyOreResourceItem("coal_infernal").get(), item("powder_coal", 2));
        exact(recipes, () -> Blocks.NETHER_QUARTZ_ORE, item("powder_quartz", 2));
        exact(recipes, () -> ModItems.get("fragment_cobalt").get(), item("powder_cobalt_tiny"));
        exact(recipes, () -> ModBlocks.ORE_RARE.get(), item("powder_desh_mix"));
        recipes.add(new ShredderRecipe(
                stack -> stack.is(ModItems.CHUNK_ORE.get())
                        && OreChunkItem.type(stack) == OreChunkItem.ChunkType.RARE,
                item("powder_desh_mix")
        ));
        exact(recipes, () -> ModBlocks.ORE_COLTAN.get(), item("powder_coltan_ore", 2));
        exact(recipes, () -> Blocks.OBSIDIAN, block(ModBlocks.GRAVEL_OBSIDIAN, 1));
        exact(recipes, () -> Blocks.STONE, vanilla(Blocks.GRAVEL, 1));
        exact(recipes, () -> ModBlocks.ORE_OIL_EMPTY.get(), vanilla(Blocks.GRAVEL, 1));
        exact(recipes, () -> ModBlocks.STEEL_SCAFFOLD.get(), item("powder_steel_tiny", 4));
        exact(recipes, () -> Blocks.COBBLESTONE, vanilla(Blocks.GRAVEL, 1));
        exact(recipes, () -> Blocks.STONE_BRICKS, vanilla(Blocks.GRAVEL, 1));
        exact(recipes, () -> Blocks.GRAVEL, vanilla(Blocks.SAND, 1));
        exact(recipes, () -> Blocks.BRICKS, vanilla(Items.CLAY_BALL, 4));
        exact(recipes, () -> Blocks.BRICK_STAIRS, vanilla(Items.CLAY_BALL, 3));
        exact(recipes, () -> Items.FLOWER_POT, vanilla(Items.CLAY_BALL, 3));
        exact(recipes, () -> Items.BRICK, vanilla(Items.CLAY_BALL, 1));
        exact(recipes, () -> Blocks.SANDSTONE, vanilla(Blocks.SAND, 4));
        exact(recipes, () -> Blocks.SANDSTONE_STAIRS, vanilla(Blocks.SAND, 6));
        exact(recipes, () -> Blocks.CLAY, vanilla(Items.CLAY_BALL, 4));
        exact(recipes, () -> Blocks.TERRACOTTA, vanilla(Items.CLAY_BALL, 4));
        exact(recipes, () -> Blocks.TNT, vanilla(Items.GUNPOWDER, 5));
        exact(recipes, () -> Blocks.DIAMOND_ORE, block(ModBlocks.GRAVEL_DIAMOND, 2));
        exact(recipes, () -> Blocks.DEEPSLATE_DIAMOND_ORE, block(ModBlocks.GRAVEL_DIAMOND, 2));
        exact(recipes, () -> Items.SUGAR_CANE, vanilla(Items.SUGAR, 3));
        exact(recipes, () -> Items.APPLE, vanilla(Items.SUGAR, 1));
        exact(recipes, () -> Items.CARROT, vanilla(Items.SUGAR, 1));
        tag(recipes, ItemTags.SAPLINGS, vanilla(Items.STICK, 1));
        exact(recipes, () -> Blocks.ANVIL, item("powder_iron", 31));
        coloredTerracotta(recipes);
        tag(recipes, ItemTags.WOOL, vanilla(Items.STRING, 4));

        generatedItem(recipes, Items.IRON_INGOT, "powder_iron", 1);
        generatedItem(recipes, Items.GOLD_INGOT, "powder_gold", 1);
        generatedItem(recipes, Items.COPPER_INGOT, "powder_copper", 1);
        generatedHbm(recipes, "ingot_titanium", "powder_titanium", 1);
        generatedHbm(recipes, "ingot_copper", "powder_copper", 1);
        generatedHbm(recipes, "ingot_red_copper", "powder_red_copper", 1);
        generatedHbm(recipes, "ingot_tungsten", "powder_tungsten", 1);
        generatedHbm(recipes, "ingot_aluminium", "powder_aluminium", 1);
        generatedHbm(recipes, "ingot_steel", "powder_steel", 1);
        generatedHbm(recipes, "ingot_tcalloy", "powder_tcalloy", 1);
        generatedHbm(recipes, "ingot_polymer", "powder_polymer", 1);
        generatedHbm(recipes, "ingot_calcium", "powder_calcium", 1);
        generatedHbm(recipes, "ingot_cadmium", "powder_cadmium", 1);
        generatedHbm(recipes, "ingot_beryllium", "powder_beryllium", 1);
        generatedHbm(recipes, "ingot_cobalt", "powder_cobalt", 1);
        generatedHbm(recipes, "fragment_coltan", "powder_coltan_ore", 1);

        generatedHbm(recipes, "plate_iron", "powder_iron", 1);
        generatedHbm(recipes, "plate_gold", "powder_gold", 1);
        generatedHbm(recipes, "plate_titanium", "powder_titanium", 1);
        generatedHbm(recipes, "plate_aluminium", "powder_aluminium", 1);
        generatedHbm(recipes, "plate_steel", "powder_steel", 1);
        generatedHbm(recipes, "plate_copper", "powder_copper", 1);

        generatedItem(recipes, Items.LAPIS_LAZULI, "powder_lapis", 1);
        generatedItem(recipes, Items.DIAMOND, "powder_diamond", 1);
        generatedItem(recipes, Items.EMERALD, "powder_emerald", 1);
        generatedHbm(recipes, "gem_tantalium", "powder_tantalium", 1);

        ore(recipes, Blocks.IRON_ORE, "powder_iron");
        ore(recipes, Blocks.DEEPSLATE_IRON_ORE, "powder_iron");
        ore(recipes, Blocks.GOLD_ORE, "powder_gold");
        ore(recipes, Blocks.DEEPSLATE_GOLD_ORE, "powder_gold");
        ore(recipes, Blocks.NETHER_GOLD_ORE, "powder_gold");
        ore(recipes, Blocks.COPPER_ORE, "powder_copper");
        ore(recipes, Blocks.DEEPSLATE_COPPER_ORE, "powder_copper");
        ore(recipes, Blocks.LAPIS_ORE, "powder_lapis");
        ore(recipes, Blocks.DEEPSLATE_LAPIS_ORE, "powder_lapis");
        exact(recipes, () -> Blocks.REDSTONE_ORE, vanilla(Items.REDSTONE, 2));
        exact(recipes, () -> Blocks.DEEPSLATE_REDSTONE_ORE, vanilla(Items.REDSTONE, 2));
        ore(recipes, Blocks.EMERALD_ORE, "powder_emerald");
        ore(recipes, Blocks.DEEPSLATE_EMERALD_ORE, "powder_emerald");

        storage(recipes, Blocks.IRON_BLOCK, "powder_iron", 9);
        storage(recipes, Blocks.GOLD_BLOCK, "powder_gold", 9);
        storage(recipes, Blocks.COPPER_BLOCK, "powder_copper", 9);
        storage(recipes, Blocks.LAPIS_BLOCK, "powder_lapis", 9);
        storage(recipes, Blocks.DIAMOND_BLOCK, "powder_diamond", 9);
        storage(recipes, Blocks.EMERALD_BLOCK, "powder_emerald", 9);
        exact(recipes, () -> Blocks.REDSTONE_BLOCK, vanilla(Items.REDSTONE, 9));
        storageHbm(recipes, "block_titanium", "powder_titanium", 9);
        storageHbm(recipes, "block_sulfur", "sulfur", 4);
        storageHbm(recipes, "block_niter", "niter", 4);
        storageHbm(recipes, "block_copper", "powder_copper", 9);
        storageHbm(recipes, "block_red_copper", "powder_red_copper", 9);
        storageHbm(recipes, "block_tungsten", "powder_tungsten", 9);
        storageHbm(recipes, "block_aluminium", "powder_aluminium", 9);
        storageHbm(recipes, "block_fluorite", "fluorite", 4);
        storageHbm(recipes, "block_beryllium", "powder_beryllium", 9);
        storageHbm(recipes, "block_steel", "powder_steel", 9);
        storageHbm(recipes, "block_tcalloy", "powder_tcalloy", 9);
        storageHbm(recipes, "block_bismuth", "powder_bismuth", 9);
        storageHbm(recipes, "block_cadmium", "powder_cadmium", 9);
        storageHbm(recipes, "block_coltan", "powder_coltan_ore", 9);

        exact(recipes, () -> ModItems.get("powder_steel_tiny").get(), item("dust_tiny"));
        exact(recipes, () -> ModItems.get("powder_coltan_ore").get(), item("dust"));
        for (MaterialDefinitions.ItemDefinition definition : MaterialDefinitions.ITEMS) {
            if (definition.form() == MaterialDefinitions.ItemForm.DUST) {
                exact(recipes, () -> ModItems.get(definition.id()).get(), item("dust"));
            }
        }
        return List.copyOf(recipes);
    }

    private static void coloredTerracotta(List<ShredderRecipe> recipes) {
        List<ItemLike> blocks = List.of(
                Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA,
                Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA,
                Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA,
                Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA,
                Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA,
                Blocks.BLACK_TERRACOTTA
        );
        for (ItemLike block : blocks) {
            exact(recipes, () -> block, vanilla(Items.CLAY_BALL, 4));
        }
    }

    private static void generatedItem(List<ShredderRecipe> recipes, ItemLike input, String output, int count) {
        exact(recipes, () -> input, item(output, count));
    }

    private static void generatedHbm(List<ShredderRecipe> recipes, String input, String output, int count) {
        exact(recipes, () -> ModItems.get(input).get(), item(output, count));
    }

    private static void ore(List<ShredderRecipe> recipes, ItemLike input, String output) {
        generatedItem(recipes, input, output, 2);
    }

    private static void storage(List<ShredderRecipe> recipes, ItemLike input, String output, int count) {
        generatedItem(recipes, input, output, count);
    }

    private static void storageHbm(List<ShredderRecipe> recipes, String input, String output, int count) {
        exact(recipes, () -> ModBlocks.get(input).get(), item(output, count));
    }

    private static void exact(
            List<ShredderRecipe> recipes,
            Supplier<? extends ItemLike> input,
            Supplier<ItemStack> output
    ) {
        recipes.add(new ShredderRecipe(stack -> stack.is(input.get().asItem()), output));
    }

    private static void tag(List<ShredderRecipe> recipes, TagKey<Item> input, Supplier<ItemStack> output) {
        recipes.add(new ShredderRecipe(stack -> stack.is(input), output));
    }

    private static Supplier<ItemStack> item(String id) {
        return item(id, 1);
    }

    private static Supplier<ItemStack> item(String id, int count) {
        return () -> new ItemStack(ModItems.get(id).get(), count);
    }

    private static Supplier<ItemStack> block(Supplier<? extends ItemLike> block, int count) {
        return () -> new ItemStack(block.get(), count);
    }

    private static Supplier<ItemStack> vanilla(ItemLike item, int count) {
        return () -> new ItemStack(item, count);
    }

    public record ShredderRecipe(Predicate<ItemStack> input, Supplier<ItemStack> output) {
    }
}
