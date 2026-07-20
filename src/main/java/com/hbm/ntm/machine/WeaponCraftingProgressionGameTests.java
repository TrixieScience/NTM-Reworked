package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.foundry.FoundryMaterial;
import com.hbm.ntm.item.BoltItem;
import com.hbm.ntm.item.FoundryPartItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Map;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WeaponCraftingProgressionGameTests {
    private WeaponCraftingProgressionGameTests() { }

    @GameTest(template = "empty")
    public static void woodRubberAndIvoryPartsKeepTheirSourceRecipes(GameTestHelper helper) {
        ItemStack woodStock = craft(helper, "part_stock_wood", ModItems.FOUNDRY_PARTS
                        .get(FoundryPartItem.PartType.STOCK).get(),
                Map.of('W', new ItemStack(Items.OAK_PLANKS)), "WWW", "  W");
        ItemStack woodGrip = craft(helper, "part_grip_wood", ModItems.FOUNDRY_PARTS
                        .get(FoundryPartItem.PartType.GRIP).get(),
                Map.of('W', new ItemStack(Items.OAK_PLANKS)), "W ", " W", " W");
        ItemStack rubberGrip = craft(helper, "part_grip_rubber", ModItems.FOUNDRY_PARTS
                        .get(FoundryPartItem.PartType.GRIP).get(),
                Map.of('W', new ItemStack(ModItems.get("ingot_rubber").get())), "W ", " W", " W");
        ItemStack ivoryGrip = craft(helper, "part_grip_ivory", ModItems.FOUNDRY_PARTS
                        .get(FoundryPartItem.PartType.GRIP).get(),
                Map.of('W', new ItemStack(Items.BONE)), "W ", " W", " W");

        check(helper, FoundryPartItem.material(woodStock) == FoundryMaterial.WOOD
                        && FoundryPartItem.material(woodGrip) == FoundryMaterial.WOOD
                        && FoundryPartItem.material(rubberGrip) == FoundryMaterial.RUBBER
                        && FoundryPartItem.material(ivoryGrip) == FoundryMaterial.IVORY,
                "Crafted nonmetal parts must keep source material and legacy model identity");
        check(helper, FoundryMaterial.fromItem(woodStock) == null
                        && FoundryMaterial.fromItem(rubberGrip) == null
                        && FoundryMaterial.fromItem(ivoryGrip) == null,
                "Wood, Rubber and Ivory parts must remain non-smeltable like the source materials");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dependencyCompleteSourceWeaponsAreCraftable(GameTestHelper helper) {
        ItemStack steelLightBarrel = part(FoundryPartItem.PartType.LIGHT_BARREL, FoundryMaterial.STEEL);
        ItemStack steelHeavyBarrel = part(FoundryPartItem.PartType.HEAVY_BARREL, FoundryMaterial.STEEL);
        ItemStack steelLightReceiver = part(FoundryPartItem.PartType.LIGHT_RECEIVER, FoundryMaterial.STEEL);
        ItemStack steelGrip = part(FoundryPartItem.PartType.GRIP, FoundryMaterial.STEEL);
        ItemStack gunmetalLightReceiver = part(FoundryPartItem.PartType.LIGHT_RECEIVER, FoundryMaterial.GUNMETAL);
        ItemStack gunmetalMechanism = part(FoundryPartItem.PartType.MECHANISM, FoundryMaterial.GUNMETAL);
        ItemStack woodStock = part(FoundryPartItem.PartType.STOCK, FoundryMaterial.WOOD);
        ItemStack woodGrip = part(FoundryPartItem.PartType.GRIP, FoundryMaterial.WOOD);
        ItemStack duraLightBarrel = part(FoundryPartItem.PartType.LIGHT_BARREL, FoundryMaterial.DURA_STEEL);
        ItemStack duraHeavyBarrel = part(FoundryPartItem.PartType.HEAVY_BARREL, FoundryMaterial.DURA_STEEL);
        ItemStack duraLightReceiver = part(FoundryPartItem.PartType.LIGHT_RECEIVER, FoundryMaterial.DURA_STEEL);
        ItemStack duraHeavyReceiver = part(FoundryPartItem.PartType.HEAVY_RECEIVER, FoundryMaterial.DURA_STEEL);
        ItemStack duraGrip = part(FoundryPartItem.PartType.GRIP, FoundryMaterial.DURA_STEEL);
        ItemStack deshLightBarrel = part(FoundryPartItem.PartType.LIGHT_BARREL, FoundryMaterial.DESH);
        ItemStack deshLightReceiver = part(FoundryPartItem.PartType.LIGHT_RECEIVER, FoundryMaterial.DESH);

        craft(helper, "gun_light_revolver", ModItems.GUN_LIGHT_REVOLVER.get(), Map.of(
                'B', steelLightBarrel, 'R', steelLightReceiver, 'M', gunmetalMechanism, 'G', woodGrip),
                "BRM", "  G");
        craft(helper, "gun_henry", ModItems.GUN_HENRY.get(), Map.of(
                'B', steelLightBarrel, 'R', gunmetalLightReceiver,
                'P', new ItemStack(ModItems.get("plate_gunmetal").get()),
                'M', gunmetalMechanism, 'S', woodStock), "BRP", "BMS");
        craft(helper, "gun_maresleg", ModItems.GUN_MARESLEG.get(), Map.of(
                'B', steelLightBarrel, 'R', steelLightReceiver, 'M', gunmetalMechanism,
                'G', BoltItem.create(ModItems.BOLT.get(), BoltItem.BoltMaterial.STEEL, 1), 'S', woodStock),
                "BRM", "BGS");
        craft(helper, "gun_flaregun", ModItems.GUN_FLAREGUN.get(), Map.of(
                'B', steelHeavyBarrel, 'R', steelLightReceiver, 'M', gunmetalMechanism, 'G', steelGrip),
                "BRM", "  G");
        craft(helper, "gun_am180", ModItems.GUN_AM180.get(), Map.of(
                'B', duraLightBarrel, 'R', duraLightReceiver, 'S', woodStock,
                'G', woodGrip, 'M', gunmetalMechanism), "BRS", "GMG");
        craft(helper, "gun_liberator", ModItems.GUN_LIBERATOR.get(), Map.of(
                'B', duraLightBarrel, 'M', gunmetalMechanism, 'G', woodGrip),
                "BB ", "BBM", "G G");
        craft(helper, "gun_congolake", ModItems.GUN_CONGOLAKE.get(), Map.of(
                'B', duraHeavyBarrel, 'M', gunmetalMechanism, 'R', duraLightReceiver,
                'S', woodStock, 'G', woodGrip), "BM ", "BRS", "G  ");
        craft(helper, "gun_flamer", ModItems.GUN_FLAMER.get(), Map.of(
                'M', gunmetalMechanism, 'G', duraGrip,
                'B', duraHeavyBarrel, 'R', duraHeavyReceiver), " MG", "BBR", " GM");
        craft(helper, "gun_heavy_revolver", ModItems.GUN_HEAVY_REVOLVER.get(), Map.of(
                'B', deshLightBarrel, 'R', deshLightReceiver, 'M', gunmetalMechanism, 'G', woodGrip),
                "BRM", "  G");
        craft(helper, "gun_carbine", ModItems.GUN_CARBINE.get(), Map.of(
                'B', deshLightBarrel, 'R', deshLightReceiver, 'M', gunmetalMechanism,
                'G', woodGrip, 'S', woodStock), "BRM", "G S");
        helper.succeed();
    }

    private static ItemStack craft(GameTestHelper helper, String recipeName, Item expected,
                                   Map<Character, ItemStack> key, String... pattern) {
        int width = pattern[0].length();
        var slots = new ArrayList<ItemStack>(width * pattern.length);
        for (String row : pattern) {
            check(helper, row.length() == width, "Test pattern " + recipeName + " must be rectangular");
            for (int column = 0; column < width; column++) {
                char symbol = row.charAt(column);
                slots.add(symbol == ' ' ? ItemStack.EMPTY : key.get(symbol).copy());
            }
        }
        CraftingInput input = CraftingInput.of(width, pattern.length, slots);
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(
                RecipeType.CRAFTING, input, helper.getLevel()).orElseThrow();
        check(helper, recipe.id().equals(id(recipeName)), "Crafting grid must resolve to hbm:" + recipeName);
        ItemStack output = recipe.value().assemble(input, helper.getLevel().registryAccess());
        check(helper, output.is(expected) && output.getCount() == 1,
                "hbm:" + recipeName + " must produce exactly one source item");
        return output;
    }

    private static ItemStack part(FoundryPartItem.PartType type, FoundryMaterial material) {
        return FoundryPartItem.create(ModItems.FOUNDRY_PARTS.get(type).get(), material, 1);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
