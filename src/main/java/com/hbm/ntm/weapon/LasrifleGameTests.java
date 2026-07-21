package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.LaserPistolBeamEntity;
import com.hbm.ntm.item.LasrifleItem;
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
public final class LasrifleGameTests {
    private LasrifleGameTests() { }

    @GameTest(template = "empty")
    public static void receiverKeepsTheSourceLasrifleContract(GameTestHelper helper) {
        LasrifleItem gun = ModItems.GUN_LASRIFLE.get();
        ItemStack stack = new ItemStack(gun);
        helper.assertTrue(LasrifleItem.DURABILITY == 2_000 && LasrifleItem.CAPACITY == 24
                        && LasrifleItem.DRAW_TICKS == 10 && LasrifleItem.INSPECT_TICKS == 26
                        && LasrifleItem.FIRE_DELAY == 8 && LasrifleItem.RELOAD_TICKS == 44
                        && LasrifleItem.JAM_TICKS == 36 && LasrifleItem.BASE_DAMAGE == 50.0F
                        && LasrifleItem.INNATE_SPREAD == 0.0F && LasrifleItem.HIP_SPREAD == 1.0F
                        && LasrifleItem.AIM_FOV_MULTIPLIER == 0.25F
                        && !gun.gunAutomatic() && gun.gunCrosshair() == SednaCrosshair.CIRCLE
                        && LasrifleItem.SCOPE.equals(gun.gunScopeTexture()),
                "Lasrifle timing, damage, spread, capacity, scope, and reticle must match XFactoryEnergy");
        helper.assertTrue(LasrifleItem.rounds(stack) == 0
                        && LasrifleItem.loadedAmmo(stack) == EnergyAmmoType.STANDARD,
                "a fresh Lasrifle is empty but remembers the standard capacitor identity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void partialMagazineLocksItsCapacitorType(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(ModItems.GUN_LASRIFLE.get());
        LasrifleItem.setTestState(gun, LasrifleItem.GunState.IDLE, 0, 4,
                EnergyAmmoType.OVERCHARGE, 0.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.getInventory().add(EnergyAmmoType.STANDARD.createStack(ModItems.AMMO_STANDARD.get(), 20));
        player.getInventory().add(EnergyAmmoType.OVERCHARGE.createStack(ModItems.AMMO_STANDARD.get(), 20));

        SednaGunItem.handleInput(player, GunInput.RELOAD);
        for (int tick = 0; tick < LasrifleItem.RELOAD_TICKS; tick++) {
            gun.getItem().inventoryTick(gun, helper.getLevel(), player, 0, true);
        }

        helper.assertTrue(LasrifleItem.rounds(gun) == 24
                        && LasrifleItem.loadedAmmo(gun) == EnergyAmmoType.OVERCHARGE
                        && count(player, EnergyAmmoType.OVERCHARGE) == 0
                        && count(player, EnergyAmmoType.STANDARD) == 20,
                "a partial Lasrifle battery must fill only from its existing capacitor profile");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void overchargeShotConsumesOneRoundAndSpawnsItsBeam(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(ModItems.GUN_LASRIFLE.get());
        LasrifleItem.setTestState(gun, LasrifleItem.GunState.IDLE, 0, 2,
                EnergyAmmoType.OVERCHARGE, 0.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<LaserPistolBeamEntity> beams = helper.getLevel().getEntitiesOfClass(
                LaserPistolBeamEntity.class, new AABB(player.position(), player.position()).inflate(2.0D));
        helper.assertTrue(beams.size() == 1
                        && beams.getFirst().ammoType() == EnergyAmmoType.OVERCHARGE
                        && beams.getFirst().beamDamage() == 75.0F && !beams.getFirst().emerald(),
                "overcharge must spawn one red 50 x 1.5 Lasrifle beam");
        helper.assertTrue(LasrifleItem.rounds(gun) == 1
                        && LasrifleItem.state(gun) == LasrifleItem.GunState.COOLDOWN
                        && LasrifleItem.timer(gun) == 8,
                "one round is consumed and the semiauto action enters its eight tick cooldown");
        helper.succeed();
    }

    private static int count(Player player, EnergyAmmoType type) {
        int amount = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.AMMO_STANDARD.get()) && StandardAmmoTypes.fromStack(stack) == type) {
                amount += stack.getCount();
            }
        }
        return amount;
    }
}
