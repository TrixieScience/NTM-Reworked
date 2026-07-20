package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.foundry.FoundryMaterial;
import com.hbm.ntm.item.BoltItem;
import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.item.FoundryPartItem;
import com.hbm.ntm.item.ShellItem;
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
import java.util.List;
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

    @GameTest(template = "empty")
    public static void remainingWeaponRecipesResolveAndCraft(GameTestHelper helper) {
        ItemStack steelLightBarrel = part(FoundryPartItem.PartType.LIGHT_BARREL, FoundryMaterial.STEEL);
        ItemStack steelLightReceiver = part(FoundryPartItem.PartType.LIGHT_RECEIVER, FoundryMaterial.STEEL);
        ItemStack steelGrip = part(FoundryPartItem.PartType.GRIP, FoundryMaterial.STEEL);
        ItemStack steelBolt = BoltItem.create(ModItems.BOLT.get(), BoltItem.BoltMaterial.STEEL, 1);
        ItemStack gunmetalMechanism = part(FoundryPartItem.PartType.MECHANISM, FoundryMaterial.GUNMETAL);
        ItemStack weaponMechanism = part(FoundryPartItem.PartType.MECHANISM, FoundryMaterial.WEAPON_STEEL);
        ItemStack weaponLightBarrel = part(FoundryPartItem.PartType.LIGHT_BARREL, FoundryMaterial.WEAPON_STEEL);
        ItemStack weaponHeavyBarrel = part(FoundryPartItem.PartType.HEAVY_BARREL, FoundryMaterial.WEAPON_STEEL);
        ItemStack weaponLightReceiver = part(FoundryPartItem.PartType.LIGHT_RECEIVER, FoundryMaterial.WEAPON_STEEL);
        ItemStack weaponHeavyReceiver = part(FoundryPartItem.PartType.HEAVY_RECEIVER, FoundryMaterial.WEAPON_STEEL);
        ItemStack weaponGrip = part(FoundryPartItem.PartType.GRIP, FoundryMaterial.WEAPON_STEEL);
        ItemStack woodStock = part(FoundryPartItem.PartType.STOCK, FoundryMaterial.WOOD);
        ItemStack woodGrip = part(FoundryPartItem.PartType.GRIP, FoundryMaterial.WOOD);
        ItemStack rubberGrip = part(FoundryPartItem.PartType.GRIP, FoundryMaterial.RUBBER);
        ItemStack polymerStock = part(FoundryPartItem.PartType.STOCK, FoundryMaterial.POLYMER);
        ItemStack polymerGrip = part(FoundryPartItem.PartType.GRIP, FoundryMaterial.POLYMER);
        ItemStack ferroHeavyBarrel = part(FoundryPartItem.PartType.HEAVY_BARREL, FoundryMaterial.FERROURANIUM);
        ItemStack ferroHeavyReceiver = part(FoundryPartItem.PartType.HEAVY_RECEIVER, FoundryMaterial.FERROURANIUM);
        ItemStack advancedCircuit = CircuitItem.create(ModItems.CIRCUIT.get(), CircuitItem.CircuitType.ADVANCED, 1);

        craft(helper, "gun_light_revolver_atlas", ModItems.GUN_LIGHT_REVOLVER_ATLAS.get(), Map.of(
                'M', weaponMechanism, 'A', new ItemStack(ModItems.GUN_LIGHT_REVOLVER.get())),
                " M ", "MAM", " M ");
        craft(helper, "gun_henry_lincoln", ModItems.GUN_HENRY_LINCOLN.get(), Map.of(
                'M', weaponMechanism, 'P', new ItemStack(ModItems.get("plate_gold").get()),
                'G', new ItemStack(ModItems.GUN_HENRY.get())), " M ", "PGP", " M ");
        craft(helper, "gun_greasegun", ModItems.GUN_GREASEGUN.get(), Map.of(
                'B', steelLightBarrel, 'R', steelLightReceiver, 'S', steelBolt,
                'M', gunmetalMechanism, 'G', steelGrip), "BRS", "SMG");
        craft(helper, "gun_maresleg_akimbo", ModItems.GUN_MARESLEG_AKIMBO.get(), Map.of(
                'S', new ItemStack(ModItems.GUN_MARESLEG.get()), 'M', weaponMechanism), "SMS");
        craft(helper, "gun_maresleg_broken", ModItems.GUN_MARESLEG_BROKEN.get(), Map.of(
                'I', new ItemStack(Items.IRON_BARS),
                'P', new ItemStack(ModItems.get("plate_weaponsteel").get()),
                'G', new ItemStack(ModItems.GUN_MARESLEG.get())), "IPI", "PGP", "IPI");
        craft(helper, "gun_flamer_topaz", ModItems.GUN_FLAMER_TOPAZ.get(), Map.of(
                'M', weaponMechanism, 'F', new ItemStack(ModItems.GUN_FLAMER.get())),
                " M ", "MFM", " M ");
        craft(helper, "gun_flamer_daybreaker", ModItems.GUN_FLAMER_DAYBREAKER.get(), Map.of(
                'G', new ItemStack(ModItems.get("plate_gold").get()), 'S', new ItemStack(Items.SLIME_BALL),
                'P', new ItemStack(Items.BLAZE_POWDER), 'F', new ItemStack(ModItems.GUN_FLAMER.get()),
                'D', new ItemStack(ModItems.STICK_DYNAMITE.get())), "GSG", "PFP", "GDG");
        craft(helper, "gun_mas36", ModItems.GUN_MAS36.get(), Map.of(
                'M', weaponMechanism, 'C', new ItemStack(ModItems.GUN_CARBINE.get())),
                " M ", "MCM", " M ");
        craft(helper, "gun_star_f_akimbo", ModItems.GUN_STAR_F_AKIMBO.get(), Map.of(
                'U', new ItemStack(ModItems.GUN_STAR_F.get()), 'M', weaponMechanism), "UMU");
        craft(helper, "gun_g3", ModItems.GUN_G3.get(), Map.of(
                'B', weaponLightBarrel, 'R', weaponLightReceiver, 'M', weaponMechanism,
                'W', woodGrip, 'G', rubberGrip, 'S', woodStock), "BRM", "WGS");
        craft(helper, "gun_g3_zebra", ModItems.GUN_G3_ZEBRA.get(), Map.of(
                'M', weaponMechanism, 'P', new ItemStack(ModItems.GUN_G3.get())),
                " M ", "MPM", " M ");
        craft(helper, "gun_autoshotgun_shredder", ModItems.GUN_AUTOSHOTGUN_SHREDDER.get(), Map.of(
                'M', weaponMechanism, 'A', new ItemStack(ModItems.GUN_AUTOSHOTGUN.get())),
                " M ", "MAM", " M ");
        craft(helper, "gun_autoshotgun_sexy", ModItems.GUN_AUTOSHOTGUN_SEXY.get(), Map.of(
                'B', steelBolt, 'N', new ItemStack(Items.NETHER_STAR), 'C', advancedCircuit,
                'A', new ItemStack(ModItems.GUN_AUTOSHOTGUN.get()),
                'S', new ItemStack(ModItems.get("ingot_schrabidium").get())), "BNB", "CAC", "BSB");
        craft(helper, "gun_stinger", ModItems.GUN_STINGER.get(), Map.of(
                'B', weaponHeavyBarrel, 'P', advancedCircuit, 'G', weaponGrip, 'M', weaponMechanism),
                "BBB", "PGM");
        craft(helper, "gun_mk108", ModItems.GUN_MK108.get(), Map.of(
                'G', polymerGrip, 'B', weaponHeavyBarrel, 'R', weaponHeavyReceiver,
                'M', weaponMechanism, 'D', ShellItem.steel(ModItems.SHELL.get(), 1)),
                " GG", "BRM", " D ");
        craft(helper, "gun_stg77", ModItems.GUN_STG77.get(), Map.of(
                'D', advancedCircuit, 'B', weaponLightBarrel, 'R', weaponLightReceiver,
                'S', polymerStock, 'G', polymerGrip, 'M', weaponMechanism), " D ", "BRS", "GGM");
        craft(helper, "gun_m2", ModItems.GUN_M2.get(), Map.of(
                'G', woodGrip, 'B', ferroHeavyBarrel, 'R', ferroHeavyReceiver, 'M', weaponMechanism),
                "  G", "BRM", "  G");
        craft(helper, "gun_amat", ModItems.GUN_AMAT.get(), Map.of(
                'C', advancedCircuit, 'B', ferroHeavyBarrel, 'R', ferroHeavyReceiver,
                'S', woodStock, 'M', weaponMechanism, 'G', woodGrip), " C ", "BRS", " MG");
        craft(helper, "gun_amat_subtlety", ModItems.GUN_AMAT_SUBTLETY.get(), Map.of(
                'S', new ItemStack(ModItems.get("ingot_schrabidium").get()),
                'A', new ItemStack(ModItems.get("plate_aluminium").get()),
                'G', new ItemStack(ModItems.GUN_AMAT.get())), "SAS", "AGA", "SAS");
        craft(helper, "gun_amat_penance", ModItems.GUN_AMAT_PENANCE.get(), Map.of(
                'S', new ItemStack(ModItems.get("ingot_schrabidium").get()),
                'D', CastPlateItem.create(ModItems.PLATE_CAST.get(),
                        CastPlateItem.CastPlateMaterial.DURA_STEEL, 1),
                'M', weaponMechanism, 'A', new ItemStack(ModItems.GUN_AMAT.get()),
                'G', polymerStock), "SDS", "MAG", "SDS");
        craft(helper, "gun_hangman", ModItems.GUN_HANGMAN.get(), Map.of(
                'N', new ItemStack(Items.NETHER_STAR), 'M', weaponMechanism,
                'H', new ItemStack(ModItems.GUN_HEAVY_REVOLVER.get())), "NMN", "MHM", "NMN");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theB92TwinsAreTheOnlyGunsWithoutCraftingRecipes(GameTestHelper helper) {
        List<String> craftable = List.of(
                "gun_am180", "gun_amat", "gun_amat_penance", "gun_amat_subtlety", "gun_autoshotgun", "gun_autoshotgun_sexy",
                "gun_autoshotgun_shredder", "gun_carbine", "gun_congolake", "gun_flamer", "gun_flamer_daybreaker", "gun_flamer_topaz",
                "gun_flaregun", "gun_g3", "gun_g3_zebra", "gun_greasegun", "gun_hangman", "gun_heavy_revolver",
                "gun_henry", "gun_henry_lincoln", "gun_lag", "gun_liberator", "gun_light_revolver", "gun_light_revolver_atlas",
                "gun_m2", "gun_maresleg", "gun_maresleg_akimbo", "gun_maresleg_broken", "gun_mas36", "gun_minigun",
                "gun_missile_launcher", "gun_mk108", "gun_panzerschreck", "gun_pepperbox", "gun_quadro", "gun_spas12",
                "gun_star_f", "gun_star_f_akimbo", "gun_stg77", "gun_stinger", "gun_uzi", "gun_uzi_akimbo");
        for (String weapon : craftable) {
            check(helper, helper.getLevel().getRecipeManager().byKey(id(weapon)).isPresent(),
                    "hbm:" + weapon + " must have a normal crafting recipe");
        }
        check(helper, helper.getLevel().getRecipeManager().byKey(id("gun_b92")).isEmpty()
                        && helper.getLevel().getRecipeManager().byKey(id("gun_b93")).isEmpty(),
                "B92 and B93 must remain the two recipe exceptions");
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
                RecipeType.CRAFTING, input, helper.getLevel())
                .orElseThrow(() -> new AssertionError("No crafting recipe matched hbm:" + recipeName));
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
