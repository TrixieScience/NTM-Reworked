package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.inventory.WeaponModifierMenu;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WeaponModifierGameTests {
    private WeaponModifierGameTests() {}

    @GameTest(template = "empty")
    public static void silencerOnlyFitsPortedReceivers(GameTestHelper helper) {
        ItemStack silencer = new ItemStack(ModItems.WEAPON_MOD_SILENCER.get());
        helper.assertTrue(WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_AM180.get()), silencer, 0),
                "Silencer must fit the American-180 receiver");
        helper.assertTrue(WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_STAR_F.get()), silencer, 0),
                "Silencer must fit the Star F receiver");
        helper.assertTrue(WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_UZI.get()), silencer, 0),
                "Silencer must fit the single Uzi receiver");
        ItemStack dual = new ItemStack(ModItems.GUN_UZI_AKIMBO.get());
        helper.assertTrue(WeaponModManager.configCount(dual) == 2
                        && WeaponModManager.isApplicable(dual, silencer, 0)
                        && WeaponModManager.isApplicable(dual, silencer, 1),
                "Silencer must fit both independent dual Uzi receivers");
        ItemStack dualStar = new ItemStack(ModItems.GUN_STAR_F_AKIMBO.get());
        helper.assertTrue(WeaponModManager.configCount(dualStar) == 2
                        && WeaponModManager.isApplicable(dualStar, silencer, 0)
                        && WeaponModManager.isApplicable(dualStar, silencer, 1),
                "Silencer must fit both independent dual Star F receivers");
        helper.assertTrue(WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_G3.get()), silencer, 0),
                "Silencer must fit the standard G3 receiver");
        helper.assertTrue(!WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_G3_ZEBRA.get()), silencer, 0),
                "Zebra must keep its built-in suppressor instead of accepting another one");
        helper.assertTrue(WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_AMAT.get()), silencer, 0),
                "Silencer must fit the ordinary AMAT receiver");
        helper.assertTrue(!WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_AMAT_SUBTLETY.get()), silencer, 0)
                        && !WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_AMAT_PENANCE.get()), silencer, 0),
                "Subtlety and permanently silenced Penance must stay outside the source definition");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void installedIdsRoundTripInSourceOrder(GameTestHelper helper) {
        ItemStack gun = new ItemStack(ModItems.GUN_AM180.get());
        WeaponModManager.install(gun, 0, List.of(new ItemStack(ModItems.WEAPON_MOD_SILENCER.get())));
        helper.assertTrue(WeaponModManager.hasMod(gun, 0, WeaponModManager.SILENCER),
                "Installed silencer must use source ID 201");
        helper.assertTrue(WeaponModManager.installedMods(gun, 0).getFirst()
                        .is(ModItems.WEAPON_MOD_SILENCER.get()),
                "Installed silencer must rebuild its table stack");
        WeaponModManager.uninstall(gun, 0);
        helper.assertTrue(!WeaponModManager.hasMod(gun, 0, WeaponModManager.SILENCER),
                "Uninstall must clear the receiver list");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void speedloaderOnlyFitsLiberator(GameTestHelper helper) {
        ItemStack speedloader = new ItemStack(ModItems.WEAPON_MOD_SPEEDLOADER.get());
        helper.assertTrue(WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_LIBERATOR.get()), speedloader, 0),
                "Speedloader must fit the Liberator");
        helper.assertTrue(!WeaponModManager.isApplicable(
                        new ItemStack(ModItems.GUN_AMAT.get()), speedloader, 0),
                "Speedloader must not fit unrelated magazine-fed guns");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dualReceiverStorageStaysIndependent(GameTestHelper helper) {
        ItemStack dual = new ItemStack(ModItems.GUN_UZI_AKIMBO.get());
        ItemStack silencer = new ItemStack(ModItems.WEAPON_MOD_SILENCER.get());
        WeaponModManager.install(dual, 1, List.of(silencer));
        helper.assertTrue(!WeaponModManager.hasMod(dual, 0, WeaponModManager.SILENCER)
                        && WeaponModManager.hasMod(dual, 1, WeaponModManager.SILENCER),
                "Dual Uzi receiver one must not mutate receiver zero");
        WeaponModManager.install(dual, 0, List.of(silencer));
        WeaponModManager.uninstall(dual, 1);
        helper.assertTrue(WeaponModManager.hasMod(dual, 0, WeaponModManager.SILENCER)
                        && !WeaponModManager.hasMod(dual, 1, WeaponModManager.SILENCER),
                "Dual Uzi receiver zero must survive receiver one removal");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tableSwitchCommitsTheSelectedDualReceiver(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        WeaponModifierMenu menu = new WeaponModifierMenu(0, player.getInventory());
        ItemStack dual = new ItemStack(ModItems.GUN_UZI_AKIMBO.get());
        menu.getSlot(0).set(dual);
        helper.assertTrue(menu.configCount() == 2 && menu.clickMenuButton(player, 1),
                "Dual Uzi must expose its second table configuration");
        menu.getSlot(1).set(new ItemStack(ModItems.WEAPON_MOD_SILENCER.get()));
        ItemStack taken = menu.getSlot(0).remove(1);
        menu.getSlot(0).onTake(player, taken);
        helper.assertTrue(!WeaponModManager.hasMod(taken, 0, WeaponModManager.SILENCER)
                        && WeaponModManager.hasMod(taken, 1, WeaponModManager.SILENCER),
                "Taking the dual Uzi must commit only the selected receiver");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dualStarStorageStaysIndependent(GameTestHelper helper) {
        ItemStack dual = new ItemStack(ModItems.GUN_STAR_F_AKIMBO.get());
        ItemStack silencer = new ItemStack(ModItems.WEAPON_MOD_SILENCER.get());
        WeaponModManager.install(dual, 0, List.of(silencer));
        helper.assertTrue(WeaponModManager.hasMod(dual, 0, WeaponModManager.SILENCER)
                        && !WeaponModManager.hasMod(dual, 1, WeaponModManager.SILENCER),
                "Elite Star F receiver zero must not mutate receiver one");
        WeaponModManager.install(dual, 1, List.of(silencer));
        WeaponModManager.uninstall(dual, 0);
        helper.assertTrue(!WeaponModManager.hasMod(dual, 0, WeaponModManager.SILENCER)
                        && WeaponModManager.hasMod(dual, 1, WeaponModManager.SILENCER),
                "Elite Star F receiver one must survive receiver zero removal");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void takingGunCommitsWhileClosingCancels(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        WeaponModifierMenu commit = new WeaponModifierMenu(0, player.getInventory());
        commit.getSlot(0).set(new ItemStack(ModItems.GUN_AM180.get()));
        commit.getSlot(1).set(new ItemStack(ModItems.WEAPON_MOD_SILENCER.get()));
        ItemStack taken = commit.getSlot(0).remove(1);
        commit.getSlot(0).onTake(player, taken);
        helper.assertTrue(WeaponModManager.hasMod(taken, 0, WeaponModManager.SILENCER),
                "Taking the gun must commit displayed mods");
        helper.assertTrue(commit.getSlot(1).getItem().isEmpty(),
                "Committed mods must be absorbed by the gun");

        WeaponModifierMenu cancel = new WeaponModifierMenu(1, player.getInventory());
        ItemStack canceledGun = new ItemStack(ModItems.GUN_AM180.get());
        cancel.getSlot(0).set(canceledGun);
        cancel.getSlot(1).set(new ItemStack(ModItems.WEAPON_MOD_SILENCER.get()));
        cancel.removed(player);
        helper.assertTrue(!WeaponModManager.hasMod(canceledGun, 0, WeaponModManager.SILENCER),
                "Closing the table must cancel preview mods");
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                AABB.ofSize(player.position(), 4.0D, 4.0D, 4.0D));
        helper.assertTrue(drops.stream().anyMatch(item -> item.getItem().is(ModItems.GUN_AM180.get()))
                        && drops.stream().anyMatch(item -> item.getItem().is(ModItems.WEAPON_MOD_SILENCER.get())),
                "Closing the table must drop the gun and loose mod back to the player");
        helper.succeed();
    }
}
