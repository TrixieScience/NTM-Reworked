package com.hbm.ntm.recipe;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.function.Supplier;

/** Centrifuge recipes whose ingredients and outputs are registered. */
public final class CentrifugeRecipes {
    private static final List<CentrifugeRecipe> RECIPES = List.of(
            recipe(Ingredient.of(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE),
                    item("powder_coal", 2), item("powder_coal", 2), item("powder_coal", 2),
                    () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE),
                    item("powder_iron", 1), item("powder_iron", 1), item("powder_iron", 1),
                    () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE),
                    item("powder_gold", 1), item("powder_gold", 1), item("powder_gold", 1),
                    () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE),
                    item("powder_diamond", 1), item("powder_diamond", 1), item("powder_diamond", 1),
                    () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE),
                    item("powder_emerald", 1), item("powder_emerald", 1), item("powder_emerald", 1),
                    () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(ModBlocks.ORE_TITANIUM.get()),
                    item("powder_titanium", 1), item("powder_titanium", 1), item("powder_iron", 1),
                    () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(ModBlocks.ORE_TUNGSTEN.get()),
                    item("powder_tungsten", 1), item("powder_tungsten", 1), item("powder_iron", 1),
                    () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE),
                    item("powder_copper", 1), item("powder_copper", 1), item("powder_gold", 1),
                    () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(ModBlocks.ORE_COBALT.get()),
                    item("powder_cobalt", 2), item("powder_iron", 1), item("powder_copper", 1),
                    () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(ModBlocks.ORE_RARE.get()),
                    item("powder_desh_mix", 1), item("nugget_zirconium", 1),
                    item("nugget_zirconium", 1), () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(ModItems.legacyOreBlockItem("ore_uranium").get(),
                            ModItems.legacyOreBlockItem("ore_gneiss_uranium").get(),
                            ModItems.legacyOreBlockItem("ore_nether_uranium").get()),
                    item("powder_uranium", 1), item("powder_uranium", 1),
                    item("nugget_ra226", 1), () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(ModItems.legacyOreBlockItem("ore_thorium").get()),
                    item("powder_thorium", 1), item("powder_thorium", 1),
                    item("powder_uranium", 1), () -> new ItemStack(Items.GRAVEL)),
            recipe(Ingredient.of(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE),
                    () -> new ItemStack(Items.REDSTONE, 3), () -> new ItemStack(Items.REDSTONE, 3),
                    item("nugget_mercury", 1), () -> new ItemStack(Items.GRAVEL))
    );

    private CentrifugeRecipes() {
    }

    public static ItemStack[] getOutput(ItemStack input) {
        if (input.isEmpty()) return null;
        for (CentrifugeRecipe recipe : RECIPES) {
            if (recipe.input().test(input)) {
                ItemStack[] result = new ItemStack[recipe.outputs().size()];
                for (int i = 0; i < result.length; i++) result[i] = recipe.outputs().get(i).get().copy();
                return result;
            }
        }
        return null;
    }

    public static boolean hasRecipe(ItemStack input) {
        return getOutput(input) != null;
    }

    public static int recipeCount() {
        return RECIPES.size();
    }

    private static CentrifugeRecipe recipe(Ingredient input,
                                           Supplier<ItemStack> first,
                                           Supplier<ItemStack> second,
                                           Supplier<ItemStack> third,
                                           Supplier<ItemStack> fourth) {
        return new CentrifugeRecipe(input, List.of(first, second, third, fourth));
    }

    private static Supplier<ItemStack> item(String id, int count) {
        return () -> new ItemStack(ModItems.get(id).get(), count);
    }

    private record CentrifugeRecipe(Ingredient input, List<Supplier<ItemStack>> outputs) {
    }
}
