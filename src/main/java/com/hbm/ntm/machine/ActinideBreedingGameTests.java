package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RadioactiveBlock;
import com.hbm.ntm.hazard.HazardCarrier;
import com.hbm.ntm.item.BreedingRodItem;
import com.hbm.ntm.nuclear.CustomNukeExplosion;
import com.hbm.ntm.recipe.BreederRecipes;
import com.hbm.ntm.recipe.ShredderRecipes;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ActinideBreedingGameTests {
    private static final float EPSILON = 0.0001F;

    private ActinideBreedingGameTests() { }

    @GameTest(template = "empty")
    public static void uraniumBreedsThroughTheExactSourceChain(GameTestHelper helper) {
        assertBreedingStep(helper, BreedingRodItem.Type.U235, BreedingRodItem.Type.NP237, 300);
        assertBreedingStep(helper, BreedingRodItem.Type.NP237, BreedingRodItem.Type.PU238, 200);
        assertBreedingStep(helper, BreedingRodItem.Type.PU238, BreedingRodItem.Type.PU239, 1_000);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bredActinidesCanReturnToEverySourceMaterialForm(GameTestHelper helper) {
        for (Material material : MATERIALS) {
            assertRecipe(helper, "ingot_" + material.id() + "_from_nugget_" + material.id(),
                    material.ingot(), 1);
            assertRecipe(helper, "nugget_" + material.id() + "_from_ingot_" + material.id(),
                    material.nugget(), 9);
            assertRecipe(helper, "billet_" + material.id() + "_from_nugget_" + material.id(),
                    material.billet(), 1);
            assertRecipe(helper, "nugget_" + material.id() + "_from_billet_" + material.id(),
                    material.nugget(), 6);
            assertRecipe(helper, "ingot_" + material.id() + "_from_billet_" + material.id(),
                    material.ingot(), 2);
            assertRecipe(helper, "billet_" + material.id() + "_from_ingot_" + material.id(),
                    material.billet(), 3);
            assertRecipe(helper, material.blockRecipe(), material.block(), 1);
            assertRecipe(helper, material.ingot() + "_from_" + material.block(), material.ingot(), 9);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void neptuniumPowderClosesItsSourceRecoveryLoop(GameTestHelper helper) {
        Item ingot = item("ingot_neptunium");
        Item powder = item("powder_neptunium");
        ItemStack shredded = ShredderRecipes.getResult(new ItemStack(ingot));
        check(helper, shredded.is(powder) && shredded.getCount() == 1,
                "The Shredder must turn one Neptunium Ingot into one Neptunium Powder");

        ItemStack blockDust = ShredderRecipes.getResult(new ItemStack(ModBlocks.get("block_neptunium").get()));
        check(helper, blockDust.is(powder) && blockDust.getCount() == 9,
                "A Neptunium block must shred into all nine powder units");

        SingleRecipeInput input = new SingleRecipeInput(shredded);
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(
                RecipeType.SMELTING, input, helper.getLevel()).orElseThrow();
        ItemStack remelted = recipe.value().assemble(input, helper.getLevel().registryAccess());
        check(helper, recipe.id().equals(id("ingot_neptunium_from_powder")) && remelted.is(ingot),
                "Neptunium Powder must smelt back into its ingot");
        check(helper, recipe.value() instanceof AbstractCookingRecipe cooking
                        && close(cooking.getExperience(), 1F),
                "Neptunium remelting must retain the source 1.0 experience");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void actinidesKeepTheirSourceHazardsAndBlockLight(GameTestHelper helper) {
        assertHazards(helper, "ingot_neptunium", 2.5F, 0F);
        assertHazards(helper, "billet_neptunium", 1.25F, 0F);
        assertHazards(helper, "nugget_neptunium", 0.25F, 0F);
        assertHazards(helper, "powder_neptunium", 7.5F, 0F);
        assertHazards(helper, "block_neptunium", 25F, 0F);

        assertHazards(helper, "ingot_pu238", 10F, 3F);
        assertHazards(helper, "billet_pu238", 5F, 1.5F);
        assertHazards(helper, "nugget_pu238", 1F, 0.3F);
        assertHazards(helper, "block_pu238", 100F, 30F);

        assertHazards(helper, "ingot_pu239", 5F, 0F);
        assertHazards(helper, "billet_pu239", 2.5F, 0F);
        assertHazards(helper, "nugget_pu239", 0.5F, 0F);
        assertHazards(helper, "block_pu239", 50F, 0F);

        for (String blockId : new String[]{"block_neptunium", "block_pu238", "block_pu239"}) {
            check(helper, ((RadioactiveBlock) ModBlocks.get(blockId).get()).radiationFog(),
                    blockId + " must retain source RADFOG");
        }
        check(helper, ModBlocks.get("block_pu238").get().defaultBlockState().getLightEmission() == 5,
                "Pu-238 must glow at the source light level 5");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void customNukeUsesTheOldActinideNumbers(GameTestHelper helper) {
        assertNukeEntry(helper, "ingot_pu239", CustomNukeExplosion.EntryType.ADD, 25F);
        assertNukeEntry(helper, "nugget_pu239", CustomNukeExplosion.EntryType.ADD, 2.5F);
        assertNukeEntry(helper, "ingot_neptunium", CustomNukeExplosion.EntryType.ADD, 30F);
        assertNukeEntry(helper, "nugget_neptunium", CustomNukeExplosion.EntryType.ADD, 3F);
        assertNukeEntry(helper, "powder_neptunium", CustomNukeExplosion.EntryType.ADD, 30F);
        assertNukeEntry(helper, "ingot_pu238", CustomNukeExplosion.EntryType.MULT, 1.15F);
        assertNukeEntry(helper, "nugget_pu238", CustomNukeExplosion.EntryType.MULT, 1.015F);
        helper.succeed();
    }

    private static final Material[] MATERIALS = {
            new Material("neptunium", "ingot_neptunium", "billet_neptunium", "nugget_neptunium",
                    "block_neptunium", "neptunium_block"),
            new Material("pu238", "ingot_pu238", "billet_pu238", "nugget_pu238",
                    "block_pu238", "pu238_block"),
            new Material("pu239", "ingot_pu239", "billet_pu239", "nugget_pu239",
                    "block_pu239", "pu239_block")
    };

    private static void assertBreedingStep(
            GameTestHelper helper,
            BreedingRodItem.Type input,
            BreedingRodItem.Type output,
            int flux
    ) {
        BreederRecipes.Recipe recipe = BreederRecipes.get(
                BreedingRodItem.stack(ModItems.ROD.get(), input, 1));
        check(helper, recipe != null && recipe.flux() == flux
                        && BreedingRodItem.type(recipe.output()) == output,
                input.id() + " must breed into " + output.id() + " at " + flux + " flux");
    }

    private static void assertRecipe(GameTestHelper helper, String recipeId, String outputId, int count) {
        ItemStack result = helper.getLevel().getRecipeManager()
                .byKey(id(recipeId))
                .map(holder -> holder.value().getResultItem(helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        check(helper, result.is(item(outputId)) && result.getCount() == count,
                "Recipe hbm:" + recipeId + " must produce " + count + " hbm:" + outputId);
    }

    private static void assertHazards(GameTestHelper helper, String itemId, float radiation, float heat) {
        Item item = item(itemId);
        check(helper, item != Items.AIR, "Expected registered actinide form hbm:" + itemId);
        check(helper, item instanceof HazardCarrier carrier
                        && close(carrier.hbm$getHazards(new ItemStack(item)).radiation(), radiation)
                        && close(carrier.hbm$getHazards(new ItemStack(item)).heat(), heat),
                "hbm:" + itemId + " must retain source radiation " + radiation + " and heat " + heat);
    }

    private static void assertNukeEntry(
            GameTestHelper helper,
            String itemId,
            CustomNukeExplosion.EntryType type,
            float value
    ) {
        CustomNukeExplosion.Entry entry = CustomNukeExplosion.entries().get(item(itemId));
        check(helper, entry != null && entry.type() == CustomNukeExplosion.BombType.NUKE
                        && entry.entry() == type && close(entry.value(), value),
                "hbm:" + itemId + " must keep its source custom-nuke value " + value);
    }

    private static Item item(String itemId) {
        return BuiltInRegistries.ITEM.get(id(itemId));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private static boolean close(float first, float second) {
        return Math.abs(first - second) < EPSILON;
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }

    private record Material(
            String id,
            String ingot,
            String billet,
            String nugget,
            String block,
            String blockRecipe
    ) { }
}
