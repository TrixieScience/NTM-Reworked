package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.StoneResourceBlock;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.item.PipeItem;
import com.hbm.ntm.item.ShellItem;
import com.hbm.ntm.item.StoneResourceBlockItem;
import com.hbm.ntm.recipe.AssemblyRecipes;
import com.hbm.ntm.recipe.ChemicalPlantRecipes;
import com.hbm.ntm.recipe.ShredderRecipes;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConcreteProgressionGameTests {
    private ConcreteProgressionGameTests() { }

    @GameTest(template = "empty")
    public static void limestoneBecomesCementThroughTheSourceRoute(GameTestHelper helper) {
        ItemStack limestone = StoneResourceBlockItem.create(ModItems.STONE_RESOURCE_ITEM.get(),
                StoneResourceBlock.Type.LIMESTONE, 1);
        ItemStack powder = ShredderRecipes.getResult(limestone);
        check(helper, powder.is(ModItems.get("powder_limestone").get()) && powder.getCount() == 4,
                "One Limestone block must shred into four Limestone Powder");

        CraftingInput input = CraftingInput.of(2, 2, List.of(
                powder.copyWithCount(1), new ItemStack(Items.CLAY_BALL),
                new ItemStack(Items.CLAY_BALL), new ItemStack(Items.CLAY_BALL)));
        ItemStack cement = helper.getLevel().getRecipeManager().getRecipeFor(
                        RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow().value()
                .assemble(input, helper.getLevel().registryAccess());
        check(helper, cement.is(ModItems.get("powder_cement").get()) && cement.getCount() == 4,
                "One Limestone Powder and three Clay Balls must make four Cement");
        var food = cement.get(DataComponents.FOOD);
        check(helper, food != null && food.nutrition() == 2 && food.saturation() == 2.0F,
                "Cement must remain the source's deeply questionable two-point snack");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void concreteChemicalRecipeAndTagMatchSource(GameTestHelper helper) {
        var concrete = ChemicalPlantRecipes.get(ChemicalPlantRecipes.CONCRETE);
        check(helper, concrete != null && ChemicalPlantRecipes.all().size() == 22
                        && concrete.duration() == 100 && concrete.power() == 100
                        && concrete.itemInputs().size() == 3
                        && concrete.itemInputs().get(0).count() == 1
                        && concrete.itemInputs().get(0).matches(new ItemStack(ModItems.get("powder_cement").get()))
                        && concrete.itemInputs().get(1).count() == 8
                        && concrete.itemInputs().get(1).matches(new ItemStack(Items.GRAVEL, 8))
                        && concrete.itemInputs().get(2).count() == 8
                        && concrete.itemInputs().get(2).matches(new ItemStack(Items.SAND, 8))
                        && concrete.fluidInputs().size() == 1
                        && concrete.fluidInputs().getFirst().amount() == 2_000
                        && concrete.itemOutputs().getFirst().is(ModItems.CONCRETE_SMOOTH_ITEM.get())
                        && concrete.itemOutputs().getFirst().getCount() == 16,
                "Concrete must preserve Cement + 8 Gravel + 8 Sand + 2,000mB Water -> 16 at 100x100");

        TagKey<net.minecraft.world.item.Item> anyConcrete = TagKey.create(Registries.ITEM, id("any_concrete"));
        check(helper, new ItemStack(ModItems.CONCRETE_SMOOTH_ITEM.get()).is(anyConcrete)
                        && !new ItemStack(net.minecraft.world.level.block.Blocks.WHITE_CONCRETE).is(anyConcrete),
                "ANY_CONCRETE must contain HBM Concrete without the old temporary vanilla substitute");
        check(helper, ModBlocks.CONCRETE_SMOOTH.get().defaultDestroyTime() == 15.0F
                        && ModBlocks.CONCRETE_SMOOTH.get().getExplosionResistance() == 84.0F,
                "HBM Concrete must preserve source hardness 15 and converted resistance 84");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void zirnoxAssemblyRecipeIsFinallySurvivalComplete(GameTestHelper helper) {
        var recipe = AssemblyRecipes.byName("ass.cirnox");
        check(helper, recipe != null && recipe.duration() == 600 && recipe.power() == 100
                        && recipe.inputs().size() == 7
                        && recipe.inputs().get(0).count() == 4
                        && recipe.inputs().get(0).matches(ShellItem.steel(ModItems.SHELL.get(), 4))
                        && recipe.inputs().get(1).count() == 8
                        && recipe.inputs().get(1).matches(PipeItem.steel(ModItems.PIPE.get(), 8))
                        && recipe.inputs().get(2).count() == 8
                        && recipe.inputs().get(3).count() == 16
                        && recipe.inputs().get(4).count() == 16
                        && recipe.inputs().get(5).count() == 16
                        && recipe.inputs().get(5).matches(new ItemStack(ModItems.CONCRETE_SMOOTH_ITEM.get(), 16))
                        && recipe.inputs().get(6).count() == 4
                        && recipe.inputs().get(6).matches(CircuitItem.create(
                        ModItems.CIRCUIT.get(), CircuitItem.CircuitType.BASIC, 4))
                        && recipe.output().is(ModItems.REACTOR_ZIRNOX_ITEM.get()),
                "ass.cirnox must preserve all seven exact source inputs and its 600x100 cost");
        helper.succeed();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
