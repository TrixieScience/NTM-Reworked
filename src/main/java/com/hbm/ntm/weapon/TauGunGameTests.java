package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.TauBeamEntity;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.item.TauGunItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TauGunGameTests {
    private TauGunGameTests() { }

    @GameTest(template = "empty")
    public static void uraniumBoxAndReceiverKeepTheirSourceContract(GameTestHelper helper) {
        ItemStack ammo = TauAmmoType.DEPLETED_URANIUM.createStack(ModItems.AMMO_STANDARD.get(), 16);
        TauGunItem gun = ModItems.GUN_TAU.get();
        ItemStack stack = new ItemStack(gun);
        helper.assertTrue(TauAmmoType.DEPLETED_URANIUM.legacyMetadata() == 70
                        && StandardAmmoTypes.fromStack(ammo) == TauAmmoType.DEPLETED_URANIUM,
                "Tau uranium must keep source EnumAmmo ordinal 70 and survive the shared carrier");
        helper.assertTrue(TauGunItem.DURABILITY == 6_400 && TauGunItem.DRAW_TICKS == 10
                        && TauGunItem.INSPECT_TICKS == 10 && TauGunItem.FIRE_DELAY == 4
                        && TauGunItem.BASE_DAMAGE == 25.0F && gun.gunAutomatic()
                        && gun.gunSecondaryAutomatic() && gun.gunBeltFed()
                        && gun.gunCrosshair() == SednaCrosshair.CIRCLE,
                "Tau receiver timing, damage, automatic inputs, belt, and reticle must match XFactoryAccelerator");
        helper.assertTrue(TauGunItem.beltCount(stack) == 0,
                "setDefaultAmmo remembers uranium identity but MagazineBelt stores no rounds in a fresh gun");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void primaryConsumesOneBoxAndFiresAStandardBeam(GameTestHelper helper) {
        Player player = armedPlayer(helper, 3);
        ItemStack gun = player.getMainHandItem();

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<TauBeamEntity> beams = beams(helper, player);
        helper.assertTrue(beams.size() == 1 && !beams.getFirst().charged()
                        && beams.getFirst().beamDamage() == 25.0F,
                "primary fire must create one ordinary 25-damage Tau beam");
        helper.assertTrue(countAmmo(player) == 2
                        && TauGunItem.state(gun) == TauGunItem.GunState.COOLDOWN
                        && TauGunItem.timer(gun) == 4 && TauGunItem.wear(gun) == 1.0F,
                "primary fire must consume one inventory box and enter the four-tick auto cooldown");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chargeBurnsBoxesAndScalesItsSpectralShot(GameTestHelper helper) {
        Player player = armedPlayer(helper, 5);
        ItemStack gun = player.getMainHandItem();

        SednaGunItem.handleInput(player, GunInput.SECONDARY);
        for (int tick = 0; tick < 20; tick++) {
            gun.getItem().inventoryTick(gun, helper.getLevel(), player, 0, true);
        }
        SednaGunItem.handleInput(player, GunInput.SECONDARY_RELEASE);

        List<TauBeamEntity> beams = beams(helper, player);
        helper.assertTrue(beams.size() == 1 && beams.getFirst().charged()
                        && beams.getFirst().beamDamage() == 375.0F,
                "twenty charge ticks must release a spectral 25 x 3 x 5 beam");
        helper.assertTrue(countAmmo(player) == 3 && TauGunItem.wear(gun) == 3.0F
                        && TauGunItem.animation(gun) == TauGunItem.GunAnimation.ALT_CYCLE,
                "timer zero and timer ten each consume a box while shot wear uses three charge units");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chargedBeamIgnoresBlocksButPrimaryStops(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        helper.setBlock(new BlockPos(2, 5, 6), Blocks.STONE);

        TauBeamEntity primary = new TauBeamEntity(helper.getLevel(), player, 25.0F, 0.0F,
                Vec3.ZERO, false);
        primary.performHitscan();
        TauBeamEntity charged = new TauBeamEntity(helper.getLevel(), player, 250.0F, 0.0F,
                Vec3.ZERO, true);
        charged.performHitscan();

        helper.assertTrue(primary.beamLength() < TauBeamEntity.RANGE - 1.0D,
                "the ordinary Tau beam must stop at the first solid block");
        helper.assertTrue(Math.abs(charged.beamLength() - TauBeamEntity.RANGE) < 0.01D,
                "the charged spectral beam must ignore blocks for its full 250-block range");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 240)
    public static void holdingChargeTooLongRuinsTheGun(GameTestHelper helper) {
        Player player = armedPlayer(helper, 13, GameType.CREATIVE);
        ItemStack gun = player.getMainHandItem();

        SednaGunItem.handleInput(player, GunInput.SECONDARY);
        for (int tick = 0; tick <= TauGunItem.OVERCHARGE_TICK; tick++) {
            gun.getItem().inventoryTick(gun, helper.getLevel(), player, 0, true);
        }

        helper.assertTrue(countAmmo(player) == 0,
                "the charge must consume all thirteen uranium boxes through timer 120");
        helper.assertTrue(TauGunItem.wear(gun) == TauGunItem.DURABILITY
                        && TauGunItem.animation(gun) == TauGunItem.GunAnimation.CYCLE_DRY,
                "timer 201 must trigger tauBlast, max the wear, and abort the charge");
        helper.succeed();
    }

    private static Player armedPlayer(GameTestHelper helper, int ammo) {
        return armedPlayer(helper, ammo, GameType.SURVIVAL);
    }

    private static Player armedPlayer(GameTestHelper helper, int ammo, GameType gameType) {
        Player player = helper.makeMockPlayer(gameType);
        ItemStack gun = new ItemStack(ModItems.GUN_TAU.get());
        TauGunItem.setTestState(gun, TauGunItem.GunState.IDLE, 0, 0.0F,
                TauGunItem.GunAnimation.CYCLE_DRY, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        player.getInventory().add(TauAmmoType.DEPLETED_URANIUM.createStack(
                ModItems.AMMO_STANDARD.get(), ammo));
        return player;
    }

    private static List<TauBeamEntity> beams(GameTestHelper helper, Player player) {
        return helper.getLevel().getEntitiesOfClass(TauBeamEntity.class,
                new AABB(player.position(), player.position()).inflate(260.0D));
    }

    private static int countAmmo(Player player) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.AMMO_STANDARD.get())
                    && StandardAmmoTypes.fromStack(stack) == TauAmmoType.DEPLETED_URANIUM) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
