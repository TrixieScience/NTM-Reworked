package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.BoxcarEntity;
import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.entity.TorpedoEntity;
import com.hbm.ntm.item.DaniItem;
import com.hbm.ntm.item.LegendaryHeavyRevolverItem;
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

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LegendaryRevolverGameTests {
    private LegendaryRevolverGameTests() { }

    @GameTest(template = "empty")
    public static void dayAndNightKeepsTwoIndependentCylinders(GameTestHelper helper) {
        DaniItem gun = ModItems.GUN_LIGHT_REVOLVER_DANI.get();
        ItemStack stack = new ItemStack(gun);
        DaniItem.setTestState(stack, 0, DaniItem.GunState.IDLE, 0, 2,
                Magnum357AmmoType.EXPRESS, 0.0F);
        DaniItem.setTestState(stack, 1, DaniItem.GunState.IDLE, 0, 3,
                Magnum357AmmoType.HOLLOW_POINT, 0.0F);
        Player player = arm(helper, stack);

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        SednaGunItem.handleInput(player, GunInput.SECONDARY);
        helper.assertTrue(DaniItem.rounds(stack, 0) == 1 && DaniItem.rounds(stack, 1) == 2
                        && bullets(helper, player) == 2 && gun.gunHasMirroredHud()
                        && gun.gunDurability() == 30_000.0F && gun.gunCapacity() == 6,
                "Day And Night must fire its two source receivers independently");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void headExploderKeepsItsSecretIdentity(GameTestHelper helper) {
        ItemStack ammo = Equestrian44AmmoType.BOXCAR.createStack(ModItems.AMMO_SECRET.get(), 1);
        LegendaryHeavyRevolverItem lilMac = ModItems.GUN_HEAVY_REVOLVER_LILMAC.get();
        LegendaryHeavyRevolverItem protege = ModItems.GUN_HEAVY_REVOLVER_PROTEGE.get();
        helper.assertTrue(SecretAmmoTypes.fromStack(ammo) == Equestrian44AmmoType.BOXCAR
                        && Equestrian44AmmoType.BOXCAR.legacyMetadata() == 2
                        && lilMac.gunDurability() == 31_000.0F && lilMac.gunCapacity() == 6
                        && lilMac.gunAimFovMultiplier() == 0.34F && lilMac.gunScopeTexture() != null
                        && protege.gunAimFovMultiplier() == 0.67F && protege.gunScopeTexture() == null,
                "the Head-Exploder and both legendary receivers must keep their source profiles");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "legendary_revolver_payloads")
    public static void eachRevolverCallsItsOwnFallingProblem(GameTestHelper helper) {
        Vec3 impact = Vec3.atCenterOf(helper.absolutePos(new BlockPos(5, 3, 5)));
        BoxcarEntity boxcar = BoxcarEntity.spawn(helper.getLevel(), impact);
        TorpedoEntity torpedo = TorpedoEntity.spawn(helper.getLevel(), impact);
        BulletEntity boxcarRound = new BulletEntity(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL),
                Equestrian44AmmoType.BOXCAR, 0.0F, 0.0F, impact, new Vec3(1.0D, 0.0D, 0.0D));
        BulletEntity torpedoRound = new BulletEntity(helper.getLevel(), helper.makeMockPlayer(GameType.SURVIVAL),
                Equestrian44AmmoType.TORPEDO, 0.0F, 0.0F, impact, new Vec3(1.0D, 0.0D, 0.0D));
        helper.assertTrue(boxcarRound.ammoType() == Equestrian44AmmoType.BOXCAR
                        && torpedoRound.ammoType() == Equestrian44AmmoType.TORPEDO
                        && boxcar.getY() == impact.y + 50.0D && torpedo.getY() == impact.y + 50.0D,
                "each Head-Exploder config must retain its payload and fifty-block delivery height");
        helper.succeed();
    }

    private static Player arm(GameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        return player;
    }

    private static int bullets(GameTestHelper helper, Player player) {
        return helper.getLevel().getEntitiesOfClass(BulletEntity.class,
                new AABB(player.position(), player.position()).inflate(32.0D), shot -> shot.getOwner() == player).size();
    }
}
