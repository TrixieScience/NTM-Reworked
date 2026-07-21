package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.LaserPistolBeamEntity;
import com.hbm.ntm.item.LaserPistolItem;
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
public final class LaserPistolGameTests {
    private LaserPistolGameTests() { }

    @GameTest(template = "empty")
    public static void receiverKeepsTheSourceLaserPistolContract(GameTestHelper helper) {
        LaserPistolItem gun = ModItems.GUN_LASER_PISTOL.get();
        ItemStack stack = new ItemStack(gun);
        helper.assertTrue(LaserPistolItem.DURABILITY == 500 && LaserPistolItem.CAPACITY == 30
                        && LaserPistolItem.DRAW_TICKS == 10 && LaserPistolItem.INSPECT_TICKS == 26
                        && LaserPistolItem.FIRE_DELAY == 5 && LaserPistolItem.RELOAD_TICKS == 45
                        && LaserPistolItem.JAM_TICKS == 37 && LaserPistolItem.BASE_DAMAGE == 25.0F
                        && LaserPistolItem.INNATE_SPREAD == 1.0F && LaserPistolItem.HIP_SPREAD == 1.0F
                        && !gun.gunAutomatic() && gun.gunCrosshair() == SednaCrosshair.CIRCLE,
                "Laser Pistol timing, damage, spread, capacity, and reticle must match XFactoryEnergy");
        helper.assertTrue(LaserPistolItem.rounds(stack) == 0
                        && LaserPistolItem.loadedAmmo(stack) == EnergyAmmoType.STANDARD,
                "a fresh Laser Pistol is empty but remembers the standard capacitor identity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pewPewReceiverKeepsItsBsideContract(GameTestHelper helper) {
        LaserPistolItem gun = ModItems.GUN_LASER_PISTOL_PEW_PEW.get();
        ItemStack stack = new ItemStack(gun);
        helper.assertTrue(gun.variant() == LaserPistolItem.Variant.PEW_PEW
                        && gun.gunCapacity() == 10 && gun.roundsPerCycle() == 5
                        && gun.baseDamage() == 30.0F
                        && gun.variant().fireDelay() == 10
                        && gun.variant().innateSpread() == 0.25F
                        && gun.variant().firePitch() == 0.8F
                        && !gun.gunAutomatic() && gun.gunCrosshair() == SednaCrosshair.CIRCLE,
                "Pew Pew damage, volley, timing, spread, capacity, pitch, and reticle must match XFactoryEnergy");
        helper.assertTrue(LaserPistolItem.rounds(stack) == 0
                        && LaserPistolItem.loadedAmmo(stack) == EnergyAmmoType.OVERCHARGE,
                "a fresh Pew Pew is empty but remembers the overcharge capacitor identity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void morningGloryReceiverKeepsItsLegendaryContract(GameTestHelper helper) {
        LaserPistolItem gun = ModItems.GUN_LASER_PISTOL_MORNING_GLORY.get();
        ItemStack stack = new ItemStack(gun);
        helper.assertTrue(gun.variant() == LaserPistolItem.Variant.MORNING_GLORY
                        && gun.gunDurability() == 1500.0F && gun.gunCapacity() == 20
                        && gun.roundsPerCycle() == 1 && gun.baseDamage() == 20.0F
                        && gun.variant().fireDelay() == 7
                        && gun.variant().innateSpread() == 0.0F
                        && gun.variant().hipSpread() == 0.5F
                        && gun.variant().firePitch() == 1.1F
                        && gun.variant().emeraldBeam()
                        && !gun.gunAutomatic() && gun.gunCrosshair() == SednaCrosshair.CIRCLE,
                "Morning Glory durability, damage, timing, spread, capacity, pitch, and reticle must match XFactoryEnergy");
        helper.assertTrue(LaserPistolItem.rounds(stack) == 0
                        && LaserPistolItem.loadedAmmo(stack) == EnergyAmmoType.OVERCHARGE,
                "a fresh Morning Glory is empty but remembers the overcharge capacitor identity");
        helper.assertTrue(gun.armorPiercing() == 0.5F
                        && gun.armorThresholdNegation(EnergyAmmoType.STANDARD) == 10.0F
                        && gun.armorThresholdNegation(EnergyAmmoType.OVERCHARGE) == 15.0F
                        && gun.armorThresholdNegation(EnergyAmmoType.LOW_WAVELENGTH) == 10.0F,
                "Morning Glory's emerald profiles must preserve their armor piercing and threshold negation");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void laserProfilesKeepTheirOwnImpactRules(GameTestHelper helper) {
        helper.assertTrue(!EnergyAmmoType.STANDARD.laserPenetrates()
                        && EnergyAmmoType.OVERCHARGE.laserPenetrates()
                        && !EnergyAmmoType.LOW_WAVELENGTH.laserPenetrates()
                        && !EnergyAmmoType.STANDARD.laserFire()
                        && !EnergyAmmoType.OVERCHARGE.laserFire()
                        && EnergyAmmoType.LOW_WAVELENGTH.laserFire(),
                "only overcharge pierces and only low wavelength applies the source fire impact");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void partialMagazineLocksItsCapacitorType(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(ModItems.GUN_LASER_PISTOL.get());
        LaserPistolItem.setTestState(gun, LaserPistolItem.GunState.IDLE, 0, 5,
                EnergyAmmoType.OVERCHARGE, 0.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.getInventory().add(EnergyAmmoType.STANDARD.createStack(ModItems.AMMO_STANDARD.get(), 30));
        player.getInventory().add(EnergyAmmoType.OVERCHARGE.createStack(ModItems.AMMO_STANDARD.get(), 25));

        SednaGunItem.handleInput(player, GunInput.RELOAD);
        for (int tick = 0; tick < LaserPistolItem.RELOAD_TICKS; tick++) {
            gun.getItem().inventoryTick(gun, helper.getLevel(), player, 0, true);
        }

        helper.assertTrue(LaserPistolItem.rounds(gun) == 30
                        && LaserPistolItem.loadedAmmo(gun) == EnergyAmmoType.OVERCHARGE
                        && count(player, EnergyAmmoType.OVERCHARGE) == 0
                        && count(player, EnergyAmmoType.STANDARD) == 30,
                "a partial magazine must fill only from its existing capacitor profile");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void overchargeShotConsumesOneRoundAndSpawnsItsBeam(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(ModItems.GUN_LASER_PISTOL.get());
        LaserPistolItem.setTestState(gun, LaserPistolItem.GunState.IDLE, 0, 2,
                EnergyAmmoType.OVERCHARGE, 0.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<LaserPistolBeamEntity> beams = helper.getLevel().getEntitiesOfClass(
                LaserPistolBeamEntity.class, new AABB(player.position(), player.position()).inflate(260.0D));
        helper.assertTrue(beams.size() == 1
                        && beams.getFirst().ammoType() == EnergyAmmoType.OVERCHARGE
                        && beams.getFirst().beamDamage() == 37.5F,
                "overcharge must spawn one 25 x 1.5 Laser Pistol beam");
        helper.assertTrue(LaserPistolItem.rounds(gun) == 1
                        && LaserPistolItem.state(gun) == LaserPistolItem.GunState.COOLDOWN
                        && LaserPistolItem.timer(gun) == 5,
                "one round is consumed and the semiauto action enters its five tick cooldown");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void pewPewTriggerConsumesFiveRoundsAndSpawnsFiveBeams(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(ModItems.GUN_LASER_PISTOL_PEW_PEW.get());
        LaserPistolItem.setTestState(gun, LaserPistolItem.GunState.IDLE, 0, 10,
                EnergyAmmoType.OVERCHARGE, 0.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<LaserPistolBeamEntity> beams = helper.getLevel().getEntitiesOfClass(
                LaserPistolBeamEntity.class, new AABB(player.position(), player.position()).inflate(260.0D));
        helper.assertTrue(beams.size() == 5
                        && beams.stream().allMatch(beam -> beam.ammoType() == EnergyAmmoType.OVERCHARGE
                        && beam.beamDamage() == 45.0F),
                "Pew Pew must fire five independent 30 x 1.5 overcharge beams per trigger pull");
        helper.assertTrue(LaserPistolItem.rounds(gun) == 5
                        && LaserPistolItem.state(gun) == LaserPistolItem.GunState.COOLDOWN
                        && LaserPistolItem.timer(gun) == 10,
                "one Pew Pew volley must consume five rounds and enter its ten tick cooldown");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void morningGloryFiresItsEmeraldOverchargeBeam(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(ModItems.GUN_LASER_PISTOL_MORNING_GLORY.get());
        LaserPistolItem.setTestState(gun, LaserPistolItem.GunState.IDLE, 0, 2,
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
                        && beams.getFirst().beamDamage() == 30.0F
                        && beams.getFirst().emerald()
                        && beams.getFirst().armorPiercing() == 0.5F
                        && beams.getFirst().armorThresholdNegation() == 15.0F,
                "Morning Glory must fire one emerald 20 x 1.5 beam with its overcharge armor modifiers");
        helper.assertTrue(LaserPistolItem.rounds(gun) == 1
                        && LaserPistolItem.state(gun) == LaserPistolItem.GunState.COOLDOWN
                        && LaserPistolItem.timer(gun) == 7,
                "one Morning Glory shot consumes one round and enters its seven tick cooldown");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void beamDirectionFollowsTheShootersView(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(117.0F);
        player.setXRot(-38.0F);
        LaserPistolBeamEntity beam = new LaserPistolBeamEntity(helper.getLevel(), player,
                EnergyAmmoType.STANDARD, 25.0F, 0.0F, new Vec3(-0.1875D, -0.09375D, 0.75D));
        helper.assertTrue(beam.beamDirection().distanceToSqr(player.getLookAngle()) < 1.0E-6D,
                "the synced Laser Pistol beam direction must follow the shooter's camera");
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
