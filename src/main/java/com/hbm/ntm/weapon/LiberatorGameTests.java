package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.item.LiberatorItem;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LiberatorGameTests {
    private LiberatorGameTests() { }

    @GameTest(template = "empty")
    public static void liberatorUsesSourceConfigurationAndDefaultIdentity(GameTestHelper helper) {
        LiberatorItem item = ModItems.GUN_LIBERATOR.get();
        ItemStack fresh = new ItemStack(item);
        helper.assertTrue(LiberatorItem.DURABILITY == 200
                        && LiberatorItem.DRAW_TICKS == 20
                        && LiberatorItem.INSPECT_TICKS == 21
                        && LiberatorItem.FIRE_DELAY == 20
                        && LiberatorItem.ROUNDS_PER_CYCLE == 4
                        && LiberatorItem.CAPACITY == 4
                        && LiberatorItem.RELOAD_BEGIN_TICKS == 25
                        && LiberatorItem.RELOAD_CYCLE_TICKS == 15
                        && LiberatorItem.RELOAD_END_TICKS == 7
                        && LiberatorItem.JAM_TICKS == 45,
                "Liberator durability, four-round burst, tube, and action timing must match XFactory12ga");
        helper.assertTrue(item.baseDamage() == 16.0F && item.gunCapacity() == 4
                        && item.recoilVertical() == 5.0F
                        && item.recoilHorizontalSigma() == 1.5F
                        && item.gunCrosshair() == SednaCrosshair.L_CIRCLE,
                "Liberator damage, capacity, 5/1.5 recoil, and large-circle reticle must match the source");
        // setDefaultAmmo(G12, 12) sets only the BUCKSHOT type identity: the fresh magazine stays empty.
        helper.assertTrue(LiberatorItem.rounds(fresh) == 0
                        && LiberatorItem.loadedAmmo(fresh) == Shotgun12GaugeAmmoType.BUCKSHOT,
                "A fresh gun must be empty with the source default buckshot identity (no preloaded shells)");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void oneTriggerPullFiresTheWholeFourShellBurst(GameTestHelper helper) {
        Player player = armedPlayer(helper, Shotgun12GaugeAmmoType.BUCKSHOT, 4, 0.0F);
        ItemStack gun = player.getMainHandItem();
        double playerX = player.getX();

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<BulletEntity> pellets = bullets(helper, player);
        helper.assertTrue(pellets.size() == 32,
                "One primary press must fire all four buckshot shells at once (4 x 8 = 32 pellets)");
        helper.assertTrue(pellets.stream().allMatch(pellet -> pellet.damage() == 2.0F),
                "Every pellet must deal 2.0 damage (16 base x 0.125 buckshot multiplier)");
        helper.assertTrue(pellets.stream().allMatch(pellet -> Math.abs(pellet.getX() - (playerX - 0.1875D)) < 0.0001D),
                "Every hip-fired pellet must originate at the exact side offset");
        helper.assertTrue(LiberatorItem.rounds(gun) == 0
                        && LiberatorItem.state(gun) == LiberatorItem.GunState.COOLDOWN
                        && LiberatorItem.timer(gun) == 20,
                "The burst must empty the four-shell magazine and enter the 20-tick cooldown");
        helper.assertTrue(LiberatorItem.wear(gun) == 4.0F,
                "Four shells fired must add four wear steps (one per shell)");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void partialLoadFiresOnlyTheLoadedShells(GameTestHelper helper) {
        Player player = armedPlayer(helper, Shotgun12GaugeAmmoType.BUCKSHOT, 2, 0.0F);
        ItemStack gun = player.getMainHandItem();

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<BulletEntity> pellets = bullets(helper, player);
        helper.assertTrue(pellets.size() == 16,
                "Two loaded shells must fire exactly two bursts (2 x 8 = 16 pellets), not a full four");
        helper.assertTrue(LiberatorItem.rounds(gun) == 0
                        && LiberatorItem.state(gun) == LiberatorItem.GunState.COOLDOWN
                        && LiberatorItem.timer(gun) == 20,
                "A partial burst must still consume all loaded shells and enter cooldown");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dryFireSpawnsNoBulletsAndPlaysTheDryAnimation(GameTestHelper helper) {
        Player player = armedPlayer(helper, Shotgun12GaugeAmmoType.BUCKSHOT, 0, 0.0F);
        ItemStack gun = player.getMainHandItem();

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        helper.assertTrue(bullets(helper, player).isEmpty(),
                "An empty Liberator must not spawn any pellets on a dry fire");
        helper.assertTrue(LiberatorItem.state(gun) == LiberatorItem.GunState.DRAWING
                        && LiberatorItem.timer(gun) == 20
                        && LiberatorItem.animation(gun) == LiberatorItem.GunAnimation.CYCLE_DRY,
                "A dry fire must enter the 20-tick draw and play the CYCLE_DRY animation");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakActionReloadLoadsOneShellPerPhase(GameTestHelper helper) {
        Player player = armedPlayer(helper, Shotgun12GaugeAmmoType.BUCKSHOT, 0, 0.0F);
        ItemStack gun = player.getMainHandItem();
        player.getInventory().add(Shotgun12GaugeAmmoType.FLECHETTE.createStack(ModItems.AMMO_STANDARD.get(), 8));

        SednaGunItem.handleInput(player, GunInput.RELOAD);
        helper.assertTrue(LiberatorItem.state(gun) == LiberatorItem.GunState.RELOADING
                        && LiberatorItem.timer(gun) == 25 && LiberatorItem.amountBeforeReload(gun) == 0,
                "The break-action reload must begin with the source 25-tick opening phase");
        tickHeld(helper, player, 24);
        helper.assertTrue(LiberatorItem.rounds(gun) == 0 && LiberatorItem.timer(gun) == 1,
                "No shell may transfer before the opening phase ends");
        tickHeld(helper, player, 1);
        helper.assertTrue(LiberatorItem.rounds(gun) == 1
                        && LiberatorItem.timer(gun) == 15
                        && LiberatorItem.loadedAmmo(gun) == Shotgun12GaugeAmmoType.FLECHETTE
                        && LiberatorItem.animation(gun) == LiberatorItem.GunAnimation.RELOAD_CYCLE,
                "The first transfer must lock the tube type and enter a 15-tick cycle");
        tickHeld(helper, player, 15);
        helper.assertTrue(LiberatorItem.rounds(gun) == 2 && LiberatorItem.timer(gun) == 15,
                "The second phase must load one more shell");
        tickHeld(helper, player, 15);
        helper.assertTrue(LiberatorItem.rounds(gun) == 3 && LiberatorItem.timer(gun) == 15,
                "The third phase must load one more shell");
        tickHeld(helper, player, 15);
        helper.assertTrue(LiberatorItem.rounds(gun) == 4
                        && LiberatorItem.state(gun) == LiberatorItem.GunState.DRAWING
                        && LiberatorItem.timer(gun) == 7
                        && LiberatorItem.animation(gun) == LiberatorItem.GunAnimation.RELOAD_END,
                "Loading the fourth shell must fill the tube and enter the 7-tick close");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void speedloaderFillsAllFourShellsInOpeningPhase(GameTestHelper helper) {
        Player player = armedPlayer(helper, Shotgun12GaugeAmmoType.BUCKSHOT, 0, 0.0F);
        ItemStack gun = player.getMainHandItem();
        WeaponModManager.install(gun, 0,
                List.of(new ItemStack(ModItems.WEAPON_MOD_SPEEDLOADER.get())));
        player.getInventory().add(Shotgun12GaugeAmmoType.FLECHETTE.createStack(
                ModItems.AMMO_STANDARD.get(), 8));

        SednaGunItem.handleInput(player, GunInput.RELOAD);
        tickHeld(helper, player, 24);
        helper.assertTrue(LiberatorItem.rounds(gun) == 0,
                "Speedloader must not transfer shells before the action is fully open");
        tickHeld(helper, player, 1);
        helper.assertTrue(LiberatorItem.rounds(gun) == 4
                        && LiberatorItem.loadedAmmo(gun) == Shotgun12GaugeAmmoType.FLECHETTE
                        && LiberatorItem.state(gun) == LiberatorItem.GunState.DRAWING
                        && LiberatorItem.timer(gun) == 7
                        && LiberatorItem.animation(gun) == LiberatorItem.GunAnimation.RELOAD_END,
                "Speedloader must fill all four chambers together and immediately close the action");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reloadCancelFinishesCurrentShellAndPreservesOtherAmmo(GameTestHelper helper) {
        Player player = armedPlayer(helper, Shotgun12GaugeAmmoType.FLECHETTE, 2, 0.0F);
        ItemStack gun = player.getMainHandItem();
        player.getInventory().add(Shotgun12GaugeAmmoType.SLUG.createStack(ModItems.AMMO_STANDARD.get(), 8));
        player.getInventory().add(Shotgun12GaugeAmmoType.FLECHETTE.createStack(ModItems.AMMO_STANDARD.get(), 8));

        SednaGunItem.handleInput(player, GunInput.RELOAD);
        helper.assertTrue(LiberatorItem.state(gun) == LiberatorItem.GunState.RELOADING
                        && LiberatorItem.timer(gun) == 25,
                "The reload must open with the 25-tick begin phase");
        tickHeld(helper, player, 24);
        tickHeld(helper, player, 1);
        helper.assertTrue(LiberatorItem.rounds(gun) == 3 && LiberatorItem.timer(gun) == 15,
                "The first cycle must load the third shell");

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        tickHeld(helper, player, 15);
        helper.assertTrue(LiberatorItem.rounds(gun) == 4
                        && LiberatorItem.state(gun) == LiberatorItem.GunState.DRAWING
                        && LiberatorItem.timer(gun) == 7
                        && LiberatorItem.loadedAmmo(gun) == Shotgun12GaugeAmmoType.FLECHETTE
                        && countAmmo(player, Shotgun12GaugeAmmoType.SLUG) == 8,
                "Canceling must finish the current shell, preserve the loaded type and other loads");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void partialTubeAcceptsOnlyItsLoadedType(GameTestHelper helper) {
        Player player = armedPlayer(helper, Shotgun12GaugeAmmoType.FLECHETTE, 2, 0.0F);
        ItemStack gun = player.getMainHandItem();
        player.getInventory().add(Shotgun12GaugeAmmoType.SLUG.createStack(ModItems.AMMO_STANDARD.get(), 8));

        // A partially loaded FLECHETTE tube must reject SLUG: reload plays the cosmetic INSPECT and stays IDLE.
        SednaGunItem.handleInput(player, GunInput.RELOAD);
        helper.assertTrue(LiberatorItem.state(gun) == LiberatorItem.GunState.IDLE
                        && LiberatorItem.animation(gun) == LiberatorItem.GunAnimation.INSPECT
                        && LiberatorItem.rounds(gun) == 2
                        && countAmmo(player, Shotgun12GaugeAmmoType.SLUG) == 8,
                "A FLECHETTE tube must reject SLUG and inspect without state change");

        // Once matching FLECHETTE is available the tube reloads normally.
        player.getInventory().add(Shotgun12GaugeAmmoType.FLECHETTE.createStack(ModItems.AMMO_STANDARD.get(), 8));
        SednaGunItem.handleInput(player, GunInput.RELOAD);
        helper.assertTrue(LiberatorItem.state(gun) == LiberatorItem.GunState.RELOADING
                        && LiberatorItem.timer(gun) == 25
                        && countAmmo(player, Shotgun12GaugeAmmoType.SLUG) == 8,
                "Matching FLECHETTE must begin a reload while the mismatched SLUG stays untouched");
        helper.succeed();
    }

    private static Player armedPlayer(GameTestHelper helper, Shotgun12GaugeAmmoType ammo,
                                      int rounds, float wear) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(ModItems.GUN_LIBERATOR.get());
        LiberatorItem.setTestState(gun, LiberatorItem.GunState.IDLE, 0, rounds, ammo, wear);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 2, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        return player;
    }

    private static void tickHeld(GameTestHelper helper, Player player, int ticks) {
        LiberatorItem gun = (LiberatorItem) player.getMainHandItem().getItem();
        for (int i = 0; i < ticks; i++) {
            gun.inventoryTick(player.getMainHandItem(), helper.getLevel(), player,
                    player.getInventory().selected, true);
        }
    }

    private static List<BulletEntity> bullets(GameTestHelper helper, Player owner) {
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        return helper.getLevel().getEntitiesOfClass(BulletEntity.class,
                new AABB(origin).inflate(64.0D), bullet -> bullet.getOwner() == owner);
    }

    private static int countAmmo(Player player, SednaAmmoType type) {
        int total = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.AMMO_STANDARD.get()) && StandardAmmoTypes.fromStack(stack) == type) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
