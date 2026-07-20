package com.hbm.ntm.anvil;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.NtmAnvilBlock;
import com.hbm.ntm.inventory.AnvilMenu;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AnvilGameTests {
    private AnvilGameTests() { }

    @GameTest(template = "empty")
    public static void variantsPreserveTierShapeAndGravity(GameTestHelper helper) {
        NtmAnvilBlock iron = ModBlocks.ANVIL_IRON.get();
        NtmAnvilBlock steel = ModBlocks.ANVIL_STEEL.get();
        NtmAnvilBlock[] variants = {
                iron, ModBlocks.ANVIL_LEAD.get(), steel, ModBlocks.ANVIL_DESH.get(),
                ModBlocks.ANVIL_FERROURANIUM.get(), ModBlocks.ANVIL_SATURNITE.get(),
                ModBlocks.ANVIL_BISMUTH_BRONZE.get(), ModBlocks.ANVIL_ARSENIC_BRONZE.get(),
                ModBlocks.ANVIL_SCHRABIDATE.get(), ModBlocks.ANVIL_DNT.get(),
                ModBlocks.ANVIL_OSMIRIDIUM.get(), ModBlocks.ANVIL_MURKY.get()
        };
        int[] tiers = {1, 1, 2, 3, 4, 5, 5, 5, 6, 7, 8, 1_916_169};
        for (int i = 0; i < variants.length; i++) {
            check(helper, variants[i].tier() == tiers[i],
                    "Anvil variant " + i + " must preserve source tier " + tiers[i]);
            check(helper, variants[i] instanceof FallingBlock, "Every NTM Anvil variant must remain a gravity block");
        }
        check(helper, iron instanceof FallingBlock, "NTM Anvil must remain a gravity block");
        VoxelShape north = iron.defaultBlockState().setValue(NtmAnvilBlock.FACING,
                net.minecraft.core.Direction.NORTH).getShape(helper.getLevel(), BlockPos.ZERO);
        VoxelShape east = iron.defaultBlockState().setValue(NtmAnvilBlock.FACING,
                net.minecraft.core.Direction.EAST).getShape(helper.getLevel(), BlockPos.ZERO);
        check(helper, north.bounds().maxY == 0.75D && north.bounds().minZ == 0.25D && north.bounds().maxZ == 0.75D,
                "North/south Anvil shape must be 16x12x8 source bounds");
        check(helper, east.bounds().minX == 0.25D && east.bounds().maxX == 0.75D,
                "East/west Anvil shape must rotate source bounds");
        check(helper, iron.getExplosionResistance() == 60.0F, "Legacy resistance 100 must convert to effective 60");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void baseRecipesAndActiveConstructionTableLoad(GameTestHelper helper) {
        for (String id : new String[]{"anvil_iron", "anvil_lead"}) {
            ItemStack result = helper.getLevel().getRecipeManager().byKey(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, id)).orElseThrow().value()
                    .getResultItem(helper.getLevel().registryAccess());
            check(helper, !result.isEmpty(), "Base recipe hbm:" + id + " must load");
        }
        check(helper, AnvilRecipes.construction().size() == 87,
                "All 87 currently satisfiable active Anvil recipes must be registered");
        AnvilRecipes.Construction blastFurnace = AnvilRecipes.byId(
                ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "anvil/machine_blast_furnace"));
        check(helper, blastFurnace != null && blastFurnace.validForTier(1)
                        && blastFurnace.inputs().get(0).count() == 4
                        && blastFurnace.inputs().get(1).count() == 32
                        && blastFurnace.inputs().get(2).count() == 8,
                "Blast Furnace must preserve the source Tier-1 4/32/8 construction recipe");
        AnvilRecipes.Construction combination = AnvilRecipes.byId(
                ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "anvil/furnace_combination"));
        check(helper, combination != null && combination.validForTier(2) && !combination.validForTier(1)
                        && combination.inputs().get(0).count() == 8
                        && combination.inputs().get(1).count() == 16
                        && combination.inputs().get(2).count() == 2
                        && combination.inputs().get(3).count() == 16
                        && combination.inputs().get(3).matches(new ItemStack(Items.BRICK)),
                "Combination Oven must preserve its source Tier-2 8/16/2/16 construction recipe");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void smithingUpgradeConsumesExactAmounts(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        AnvilMenu menu = new AnvilMenu(0, player.getInventory(), 1);
        menu.getSlot(0).set(new ItemStack(ModItems.ANVIL_IRON_ITEM.get()));
        menu.getSlot(1).set(new ItemStack(ModItems.get("ingot_steel").get(), 10));
        check(helper, menu.result().is(ModItems.ANVIL_STEEL_ITEM.get()),
                "Tier-1 Anvil plus ten Steel ingots must smith a Steel Anvil");
        ItemStack taken = menu.getSlot(2).remove(1);
        menu.getSlot(2).onTake(player, taken);
        check(helper, menu.input(0).isEmpty() && menu.input(1).isEmpty(),
                "Taking smithing result must consume one base Anvil and ten ingots");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dependencyCompleteHigherAnvilUpgradesMatchSource(GameTestHelper helper) {
        Item[] bases = {ModItems.ANVIL_IRON_ITEM.get(), ModItems.ANVIL_LEAD_ITEM.get()};
        Item[] materials = {
                ModItems.get("ingot_ferrouranium").get(),
                ModItems.get("ingot_bismuth_bronze").get(),
                ModItems.get("ingot_arsenic_bronze").get()
        };
        Item[] outputs = {
                ModItems.ANVIL_FERROURANIUM_ITEM.get(),
                ModItems.ANVIL_BISMUTH_BRONZE_ITEM.get(),
                ModItems.ANVIL_ARSENIC_BRONZE_ITEM.get()
        };
        for (Item base : bases) {
            for (int i = 0; i < materials.length; i++) {
                AnvilRecipes.Smithing recipe = AnvilRecipes.findSmithing(
                        new ItemStack(base), new ItemStack(materials[i], 10), 1);
                check(helper, recipe != null && recipe.output().get().is(outputs[i]),
                        "Iron/Lead plus ten active source ingots must produce the matching higher Anvil");
                check(helper, AnvilRecipes.findSmithing(
                                new ItemStack(base), new ItemStack(materials[i], 9), 1) == null,
                        "Higher Anvil upgrades must reject fewer than ten ingots");
            }
        }

        for (Item gated : new Item[]{ModItems.ANVIL_SATURNITE_ITEM.get(), ModItems.ANVIL_SCHRABIDATE_ITEM.get(),
                ModItems.ANVIL_DNT_ITEM.get(), ModItems.ANVIL_OSMIRIDIUM_ITEM.get(),
                ModItems.ANVIL_MURKY_ITEM.get()}) {
            check(helper, AnvilRecipes.smithing().stream().noneMatch(recipe -> recipe.output().get().is(gated)),
                    "Anvil upgrades must not register recipes when their source ingredient is absent");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void constructionIsTransactionalAndTierValidated(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        AnvilRecipes.Construction recipe = AnvilRecipes.byId(
                ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "anvil/heater_firebox"));
        player.getInventory().add(new ItemStack(Items.FURNACE));
        player.getInventory().add(new ItemStack(ModItems.get("plate_steel").get(), 8));
        player.getInventory().add(new ItemStack(ModItems.get("ingot_copper").get(), 7));
        check(helper, !AnvilRecipes.canCraft(player.getInventory(), recipe),
                "Firebox construction must reject one missing Copper ingot");
        check(helper, player.getInventory().countItem(ModItems.get("plate_steel").get()) == 8,
                "Failed Anvil construction must not partially consume inputs");
        check(helper, !recipe.validForTier(1) && recipe.validForTier(2), "Firebox must remain Tier 2");
        player.getInventory().add(new ItemStack(ModItems.get("ingot_copper").get()));
        check(helper, AnvilRecipes.craft(player, recipe, false), "Complete Firebox inventory must craft");
        check(helper, player.getInventory().countItem(ModItems.HEATER_FIREBOX_ITEM.get()) == 1,
                "Construction output must go directly to player inventory");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void shiftBulkRepeatsUntilInputsRunOut(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        AnvilRecipes.Construction recipe = AnvilRecipes.byId(
                ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "anvil/motor"));
        player.getInventory().add(new ItemStack(ModItems.get("plate_iron").get(), 4));
        player.getInventory().add(new ItemStack(ModItems.COIL_COPPER.get(), 2));
        player.getInventory().add(new ItemStack(ModItems.COIL_COPPER_TORUS.get(), 2));
        check(helper, AnvilRecipes.craft(player, recipe, true), "Bulk Motor construction must begin when inputs exist");
        check(helper, player.getInventory().countItem(ModItems.MOTOR.get()) == 4,
                "Shift bulk must repeat the two-Motor recipe exactly twice before inputs run out");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void undamagedStampAndMissingCogRecyclingRemainDistinct(GameTestHelper helper) {
        AnvilRecipes.Construction wireStamp = AnvilRecipes.byId(
                ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "anvil/stamp_stone_wire"));
        ItemStack worn = new ItemStack(ModItems.STAMPS.get("stamp_stone_flat").get());
        worn.setDamageValue(1);
        check(helper, !wireStamp.inputs().getFirst().matches(worn),
                "Worn flat stamps must not match source Anvil reshaping recipes");
        AnvilRecipes.Construction normal = AnvilRecipes.byId(
                ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "anvil/recycle_stirling"));
        AnvilRecipes.Construction missing = AnvilRecipes.byId(
                ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "anvil/recycle_stirling_missing_cog"));
        ItemStack missingStack = com.hbm.ntm.item.StirlingMachineBlockItem.withoutCog(ModItems.MACHINE_STIRLING_ITEM.get());
        check(helper, !normal.inputs().getFirst().matches(missingStack) && missing.inputs().getFirst().matches(missingStack),
                "Missing-cog Stirling must select its no-gear recycling recipe");
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
