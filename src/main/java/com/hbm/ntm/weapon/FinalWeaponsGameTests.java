package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.entity.LaserPistolBeamEntity;
import com.hbm.ntm.item.DebugGunItem;
import com.hbm.ntm.item.DualMinigunItem;
import com.hbm.ntm.item.LacunaeItem;
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
public final class FinalWeaponsGameTests {
    private FinalWeaponsGameTests() { }

    @GameTest(template = "empty")
    public static void debugGunKeepsBothReceiversAndSixShotSecondary(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModItems.GUN_DEBUG.get());
        DebugGunItem.setTestState(stack, 0, DebugGunItem.GunState.IDLE, 0, 1,
                DebugAmmoType.BULLET, 0.0F);
        DebugGunItem.setTestState(stack, 1, DebugGunItem.GunState.IDLE, 0, 1,
                DebugAmmoType.SHOT, 0.0F);
        Player player = arm(helper, stack);

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<BulletEntity> primary = bullets(helper, player);
        helper.assertTrue(primary.size() == 1 && primary.getFirst().ammoType() == DebugAmmoType.BULLET,
                "Debug Gun primary must fire one ten-damage bullet");
        primary.forEach(BulletEntity::discard);
        SednaGunItem.handleInput(player, GunInput.PRIMARY_RELEASE);
        tickDebug(player, 14);
        SednaGunItem.handleInput(player, GunInput.SECONDARY);
        List<BulletEntity> shots = bullets(helper, player);
        helper.assertTrue(DebugGunItem.rounds(stack, 0) == 0
                        && DebugGunItem.rounds(stack, 1) == 0
                        && shots.stream().filter(shot -> shot.ammoType() == DebugAmmoType.SHOT).count() == 6,
                "Debug Gun secondary must fire six five-damage pellets after the shared action cycles");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lacunaeLoadsFortyShotsPerCapacitorAndFiresBeam(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModItems.GUN_MINIGUN_LACUNAE.get());
        LacunaeItem.setTestState(stack, LacunaeItem.GunState.IDLE, 0, 0,
                EnergyAmmoType.OVERCHARGE, 0.0F);
        Player player = arm(helper, stack);
        player.getInventory().add(EnergyAmmoType.OVERCHARGE.createStack(ModItems.AMMO_STANDARD.get(), 5));

        SednaGunItem.handleInput(player, GunInput.RELOAD);
        tickLacunae(player, 15);
        helper.assertTrue(LacunaeItem.rounds(stack) == 200
                        && countAmmo(player, EnergyAmmoType.OVERCHARGE) == 0,
                "Lacunae must load two hundred shots from five forty-shot capacitors");
        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<LaserPistolBeamEntity> beams = helper.getLevel().getEntitiesOfClass(
                LaserPistolBeamEntity.class, new AABB(player.position(), player.position()).inflate(300.0D),
                beam -> beam.getOwner() == player);
        helper.assertTrue(LacunaeItem.rounds(stack) == 199 && beams.size() == 1
                        && beams.getFirst().ammoType() == EnergyAmmoType.OVERCHARGE
                        && beams.getFirst().beamDamage() == 18.0F,
                "Lacunae overcharge must fire one 12 x 1.5 beam on its one-tick action");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dualMinigunReceiversFireIndependentlyFromOneBelt(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModItems.GUN_MINIGUN_DUAL.get());
        DualMinigunItem.setTestState(stack, 0, DualMinigunItem.GunState.IDLE, 0, 0,
                SevenSixTwoAmmoType.SOFT_POINT, 0.0F);
        DualMinigunItem.setTestState(stack, 1, DualMinigunItem.GunState.IDLE, 0, 0,
                SevenSixTwoAmmoType.SOFT_POINT, 0.0F);
        Player player = arm(helper, stack);
        player.getInventory().add(SevenSixTwoAmmoType.SOFT_POINT.createStack(ModItems.AMMO_STANDARD.get(), 4));

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        SednaGunItem.handleInput(player, GunInput.SECONDARY);
        helper.assertTrue(bullets(helper, player).size() == 2
                        && countAmmo(player, SevenSixTwoAmmoType.SOFT_POINT) == 2
                        && DualMinigunItem.state(stack, 0) == DualMinigunItem.GunState.COOLDOWN
                        && DualMinigunItem.state(stack, 1) == DualMinigunItem.GunState.COOLDOWN
                        && DualMinigunItem.wear(stack, 0) == 1.0F
                        && DualMinigunItem.wear(stack, 1) == 1.0F,
                "Dual Minigun must keep independent automatic receivers over one inventory belt");
        helper.succeed();
    }

    private static Player arm(GameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        return player;
    }

    private static void tickLacunae(Player player, int ticks) {
        LacunaeItem gun = (LacunaeItem) player.getMainHandItem().getItem();
        for (int tick = 0; tick < ticks; tick++) {
            gun.inventoryTick(player.getMainHandItem(), player.level(), player,
                    player.getInventory().selected, true);
        }
    }

    private static void tickDebug(Player player, int ticks) {
        DebugGunItem gun = (DebugGunItem) player.getMainHandItem().getItem();
        for (int tick = 0; tick < ticks; tick++) {
            gun.inventoryTick(player.getMainHandItem(), player.level(), player,
                    player.getInventory().selected, true);
        }
    }

    private static List<BulletEntity> bullets(GameTestHelper helper, Player owner) {
        return helper.getLevel().getEntitiesOfClass(BulletEntity.class,
                new AABB(owner.position(), owner.position()).inflate(64.0D),
                bullet -> bullet.getOwner() == owner);
    }

    private static int countAmmo(Player player, SednaAmmoType type) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.AMMO_STANDARD.get()) && StandardAmmoTypes.fromStack(stack) == type) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
