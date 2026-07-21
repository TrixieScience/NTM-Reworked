package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.MiniNukeProjectileEntity;
import com.hbm.ntm.item.FatManItem;
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
public final class FatManLauncherGameTests {
    private FatManLauncherGameTests() { }

    @GameTest(template = "empty")
    public static void receiverAndMiniNukesKeepTheirSourceNumbers(GameTestHelper helper) {
        FatManItem gun = ModItems.GUN_FATMAN.get();
        ItemStack stack = new ItemStack(gun);
        for (MiniNukeAmmoType ammo : MiniNukeAmmoType.values()) {
            ItemStack round = ammo.createStack(ModItems.AMMO_STANDARD.get(), 1);
            helper.assertTrue(StandardAmmoTypes.fromStack(round) == ammo
                            && StandardAmmoTypes.fromLegacyMetadata(ammo.legacyMetadata()) == ammo,
                    ammo + " identity");
        }
        helper.assertTrue(MiniNukeAmmoType.STANDARD.legacyMetadata() == 73
                        && MiniNukeAmmoType.HIVE.legacyMetadata() == 77
                        && MiniNukeAmmoType.BALEFIRE.legacyMetadata() == 93
                        && MiniNukeAmmoType.TINY_TOTS.projectiles() == 8
                        && MiniNukeAmmoType.TINY_TOTS.damageMultiplier() == 0.35F
                        && MiniNukeAmmoType.HIVE.projectiles() == 12
                        && MiniNukeAmmoType.HIVE.damageMultiplier() == 0.25F
                        && MiniNukeAmmoType.BALEFIRE.damageMultiplier() == 2.5F
                        && MiniNukeAmmoType.GRAVITY == 0.025F,
                "mini nuke profiles");
        helper.assertTrue(gun.gunDurability() == 300.0F && gun.gunCapacity() == 1
                        && FatManItem.DRAW_TICKS == 20 && FatManItem.INSPECT_TICKS == 30
                        && FatManItem.FIRE_DELAY == 10 && FatManItem.RELOAD_TICKS == 57
                        && FatManItem.JAM_TICKS == 40 && FatManItem.BASE_DAMAGE == 100.0F
                        && gun.gunCrosshair() == SednaCrosshair.L_CIRCUMFLEX
                        && !gun.gunHideCrosshairWhenAimed()
                        && gun.recoilVertical() == 0.0F && gun.recoilHorizontalSigma() == 0.0F,
                "fat man receiver");
        helper.assertTrue(FatManItem.rounds(stack) == 0
                        && FatManItem.loadedAmmo(stack) == MiniNukeAmmoType.STANDARD,
                "fresh fat man state");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reloadTakesFiftySevenTicksAndChangesToInventoryAmmo(GameTestHelper helper) {
        Player player = armed(helper, MiniNukeAmmoType.STANDARD, 0);
        player.getInventory().add(MiniNukeAmmoType.DEMOLITION.createStack(
                ModItems.AMMO_STANDARD.get(), 2));
        player.getInventory().add(MiniNukeAmmoType.STANDARD.createStack(
                ModItems.AMMO_STANDARD.get(), 2));

        SednaGunItem.handleInput(player, GunInput.RELOAD);
        helper.assertTrue(FatManItem.state(player.getMainHandItem()) == FatManItem.GunState.RELOADING
                        && FatManItem.timer(player.getMainHandItem()) == 57,
                "fat man reload start");
        tickHeld(player, 56);
        helper.assertTrue(FatManItem.rounds(player.getMainHandItem()) == 0,
                "fat man reload timing");
        tickHeld(player, 1);
        helper.assertTrue(FatManItem.rounds(player.getMainHandItem()) == 1
                        && FatManItem.loadedAmmo(player.getMainHandItem()) == MiniNukeAmmoType.DEMOLITION
                        && countAmmo(player, MiniNukeAmmoType.DEMOLITION) == 1
                        && countAmmo(player, MiniNukeAmmoType.STANDARD) == 2,
                "fat man reload result");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tinyTotsAndHiveLaunchEveryProjectile(GameTestHelper helper) {
        Player totsPlayer = armed(helper, MiniNukeAmmoType.TINY_TOTS, 1);
        SednaGunItem.handleInput(totsPlayer, GunInput.PRIMARY);
        List<MiniNukeProjectileEntity> tots = projectiles(helper, totsPlayer);
        helper.assertTrue(tots.size() == 8 && tots.stream().allMatch(shot ->
                        shot.ammoType() == MiniNukeAmmoType.TINY_TOTS && shot.damage() == 35.0F),
                "tiny tots volley");

        Player hivePlayer = armed(helper, MiniNukeAmmoType.HIVE, 1);
        hivePlayer.setPos(hivePlayer.position().add(4.0D, 0.0D, 0.0D));
        SednaGunItem.handleInput(hivePlayer, GunInput.PRIMARY);
        List<MiniNukeProjectileEntity> hive = projectiles(helper, hivePlayer);
        helper.assertTrue(hive.size() == 12 && hive.stream().allMatch(shot ->
                        shot.ammoType() == MiniNukeAmmoType.HIVE && shot.damage() == 25.0F),
                "rocket hive volley");
        helper.assertTrue(FatManItem.rounds(totsPlayer.getMainHandItem()) == 0
                        && FatManItem.timer(totsPlayer.getMainHandItem()) == 10
                        && FatManItem.rounds(hivePlayer.getMainHandItem()) == 0,
                "fat man shot state");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void miniNukeUsesAuthoredSpeedAndGravity(GameTestHelper helper) {
        Player player = armed(helper, MiniNukeAmmoType.STANDARD, 1);
        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        MiniNukeProjectileEntity shot = projectiles(helper, player).getFirst();
        Vec3 start = shot.position();
        shot.tick();
        helper.assertTrue(Math.abs(shot.position().distanceTo(start) - 3.0D) < 0.01D
                        && Math.abs(shot.getDeltaMovement().y + 0.025D) < 0.001D,
                "mini nuke flight");
        helper.succeed();
    }

    private static Player armed(GameTestHelper helper, MiniNukeAmmoType ammo, int rounds) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = new ItemStack(ModItems.GUN_FATMAN.get());
        FatManItem.setTestState(stack, FatManItem.GunState.IDLE, 0, rounds, ammo,
                0.0F, false, FatManItem.GunAnimation.CYCLE);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        return player;
    }

    private static void tickHeld(Player player, int ticks) {
        FatManItem item = (FatManItem) player.getMainHandItem().getItem();
        for (int i = 0; i < ticks; i++) {
            item.inventoryTick(player.getMainHandItem(), player.level(), player,
                    player.getInventory().selected, true);
        }
    }

    private static List<MiniNukeProjectileEntity> projectiles(GameTestHelper helper, Player owner) {
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        return helper.getLevel().getEntitiesOfClass(MiniNukeProjectileEntity.class,
                new AABB(origin).inflate(64.0D), shot -> shot.getOwner() == owner);
    }

    private static int countAmmo(Player player, MiniNukeAmmoType ammo) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.AMMO_STANDARD.get()) && StandardAmmoTypes.fromStack(stack) == ammo) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
