package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.AmmoPressBlock;
import com.hbm.ntm.blockentity.AmmoPressBlockEntity;
import com.hbm.ntm.item.CasingItem;
import com.hbm.ntm.recipe.AmmoPressRecipes;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.weapon.Magnum357AmmoType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AmmoPressGameTests {
    private AmmoPressGameTests() { }

    @GameTest(template = "empty")
    public static void pressConsumesEveryCompleteBatchAtOnce(GameTestHelper helper) {
        BlockPos position = new BlockPos(3, 1, 3);
        AmmoPressBlockEntity press = placePress(helper, position);
        press.selectRecipe(0);
        press.setItem(1, new ItemStack(ModItems.get("ingot_lead").get(), 4));
        press.setItem(4, new ItemStack(Items.GUNPOWDER, 2));
        press.setItem(7, CasingItem.create(ModItems.CASING.get(), CasingItem.CasingType.SMALL, 2));

        AmmoPressBlockEntity.tick(helper.getLevel(), helper.absolutePos(position), helper.getBlockState(position), press);

        ItemStack output = press.getItem(AmmoPressBlockEntity.OUTPUT_SLOT);
        check(helper, Magnum357AmmoType.fromStack(output) == Magnum357AmmoType.BLACK_POWDER && output.getCount() == 32,
                "Two source recipe batches should become 32 black-powder .357 rounds in one tick");
        check(helper, press.getItem(1).isEmpty() && press.getItem(4).isEmpty() && press.getItem(7).isEmpty(),
                "The Ammo Press should consume all ingredients used by those two batches");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void recipeGridIsPositionalAndAutomationFollowsSelection(GameTestHelper helper) {
        AmmoPressBlockEntity press = placePress(helper, new BlockPos(3, 1, 3));
        press.selectRecipe(0);
        ItemStack lead = new ItemStack(ModItems.get("ingot_lead").get(), 2);
        check(helper, press.canPlaceItem(1, lead), "Lead belongs in the selected recipe's top-middle slot");
        check(helper, !press.canPlaceItem(0, lead), "Automation must not flatten the recipe into any input slot");
        check(helper, !press.canPlaceItem(AmmoPressBlockEntity.OUTPUT_SLOT, lead), "Automation must never insert into output");
        check(helper, press.canTakeItemThroughFace(AmmoPressBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY, Direction.DOWN),
                "Automation should extract only the output slot");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingAnyPartRemovesTheWholePress(GameTestHelper helper) {
        BlockPos core = new BlockPos(3, 1, 3);
        placePress(helper, core);
        BlockPos topFront = core.north().above();
        helper.getLevel().setBlock(helper.absolutePos(topFront), Blocks.AIR.defaultBlockState(), 3);
        for (BlockPos part : AmmoPressBlock.partPositions(core, Direction.NORTH)) {
            check(helper, helper.getBlockState(part).isAir(), "Breaking one Ammo Press part should remove all six parts");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sourceCatalogKeepsAvailableRecipesAndLegacyComponents(GameTestHelper helper) {
        check(helper, AmmoPressRecipes.all().size() == 62,
                "The dependency-complete source catalog should contain exactly 62 recipes");
        check(helper, CasingItem.CasingType.values().length == 7,
                "All seven legacy casing variants must be registered for ammo recipes and spent shells");
        helper.succeed();
    }

    private static AmmoPressBlockEntity placePress(GameTestHelper helper, BlockPos core) {
        AmmoPressBlock block = ModBlocks.AMMO_PRESS.get();
        for (BlockPos part : AmmoPressBlock.partPositions(core, Direction.NORTH)) {
            int y = part.getY() - core.getY();
            int depth = 1 - (part.getZ() - core.getZ());
            helper.setBlock(part, block.defaultBlockState().setValue(AmmoPressBlock.FACING, Direction.NORTH)
                    .setValue(AmmoPressBlock.DEPTH, depth).setValue(AmmoPressBlock.Y, y));
        }
        return helper.getBlockEntity(core);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
