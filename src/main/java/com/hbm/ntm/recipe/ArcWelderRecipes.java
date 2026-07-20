package com.hbm.ntm.recipe;

import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.item.WeldedPlateItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Arc Welder operations whose ingredients and outputs are registered. */
public final class ArcWelderRecipes {
    private static final List<ArcWelderRecipe> RECIPES = List.of(
            new ArcWelderRecipe(WeldedPlateItem.steel(ModItems.PLATE_WELDED.get(), 1), 100, 500L, null,
                    List.of(new Input(castPlate(CastPlateItem.CastPlateMaterial.STEEL,
                            "steel"), 2))),
            new ArcWelderRecipe(WeldedPlateItem.create(ModItems.PLATE_WELDED.get(),
                    WeldedPlateItem.WeldedPlateMaterial.TECHNETIUM_STEEL, 1), 1_200, 1_000_000L,
                    new FluidStack(ModFluids.OXYGEN.get(), 1_000),
                    List.of(new Input(castPlate(CastPlateItem.CastPlateMaterial.TECHNETIUM_STEEL,
                            "technetium_steel"), 2))),
            new ArcWelderRecipe(WeldedPlateItem.create(ModItems.PLATE_WELDED.get(),
                    WeldedPlateItem.WeldedPlateMaterial.CADMIUM_STEEL, 1), 1_200, 1_000_000L,
                    new FluidStack(ModFluids.OXYGEN.get(), 1_000),
                    List.of(new Input(castPlate(CastPlateItem.CastPlateMaterial.CADMIUM_STEEL,
                            "cadmium_steel"), 2)))
    );

    private ArcWelderRecipes() { }

    public static List<ArcWelderRecipe> all() { return RECIPES; }

    public static @Nullable ArcWelderRecipe find(ItemStack... inputs) {
        outer:
        for (ArcWelderRecipe recipe : RECIPES) {
            List<Input> remaining = new ArrayList<>(recipe.ingredients());
            for (ItemStack stack : inputs) {
                if (stack.isEmpty()) continue;
                int match = -1;
                for (int index = 0; index < remaining.size(); index++) {
                    if (remaining.get(index).matches(stack)) { match = index; break; }
                }
                if (match < 0) continue outer;
                remaining.remove(match);
            }
            if (remaining.isEmpty()) return recipe;
        }
        return null;
    }

    public static boolean isValidInput(ItemStack stack) {
        for (ArcWelderRecipe recipe : RECIPES) {
            for (Input input : recipe.ingredients()) if (input.ingredient().test(stack)) return true;
        }
        return false;
    }

    private static Ingredient castPlate(CastPlateItem.CastPlateMaterial material, String tagName) {
        ItemStack plate = CastPlateItem.create(ModItems.PLATE_CAST.get(), material, 1);
        Ingredient hbmSubtype = DataComponentIngredient.of(false, plate);
        TagKey<Item> compatibilityTag = TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("c", "plates/cast/" + tagName));
        Ingredient externalCompatibility = DifferenceIngredient.of(
                Ingredient.of(compatibilityTag), Ingredient.of(ModItems.PLATE_CAST.get()));
        return CompoundIngredient.of(hbmSubtype, externalCompatibility);
    }

    public record Input(Ingredient ingredient, int count) {
        public boolean matches(ItemStack stack) { return ingredient.test(stack) && stack.getCount() >= count; }
    }

    public record ArcWelderRecipe(ItemStack output, int duration, long consumption,
                                  @Nullable FluidStack fluid, List<Input> ingredients) {
        /** Source output checks compare item and metadata while ignoring unrelated NBT. */
        public boolean matchesOutput(ItemStack stack) {
            if (!stack.is(output.getItem())) return false;
            if (output.is(ModItems.PLATE_WELDED.get())) {
                return WeldedPlateItem.material(stack) == WeldedPlateItem.material(output);
            }
            return ItemStack.isSameItemSameComponents(stack, output);
        }
    }
}
