package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.TeslaBeamEntity;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.item.TeslaCannonItem;
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
public final class TeslaCannonGameTests {
    private TeslaCannonGameTests() { }

    @GameTest(template = "empty")
    public static void capacitorProfilesKeepTheirSourceIdentity(GameTestHelper helper) {
        helper.assertTrue(EnergyAmmoType.STANDARD.legacyMetadata() == 67
                        && EnergyAmmoType.OVERCHARGE.legacyMetadata() == 68
                        && EnergyAmmoType.LOW_WAVELENGTH.legacyMetadata() == 69,
                "Tesla capacitors must keep the source EnumAmmo ordinals");
        helper.assertTrue(EnergyAmmoType.STANDARD.penetrates()
                        && EnergyAmmoType.OVERCHARGE.penetrates()
                        && !EnergyAmmoType.LOW_WAVELENGTH.penetrates()
                        && EnergyAmmoType.LOW_WAVELENGTH.chainLightning(),
                "standard and overcharge penetrate while low wavelength stops and splits");
        for (EnergyAmmoType type : EnergyAmmoType.values()) {
            ItemStack stack = type.createStack(ModItems.AMMO_STANDARD.get(), 4);
            helper.assertTrue(StandardAmmoTypes.fromStack(stack) == type,
                    type + " must survive its shared ammo carrier");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void receiverRetainsTheOldTeslaContract(GameTestHelper helper) {
        TeslaCannonItem gun = ModItems.GUN_TESLA_CANNON.get();
        ItemStack stack = new ItemStack(gun);
        helper.assertTrue(TeslaCannonItem.DURABILITY == 1_000 && TeslaCannonItem.DRAW_TICKS == 10
                        && TeslaCannonItem.INSPECT_TICKS == 33 && TeslaCannonItem.JAM_TICKS == 19
                        && TeslaCannonItem.FIRE_DELAY == 20 && TeslaCannonItem.BASE_DAMAGE == 35.0F
                        && TeslaCannonItem.HIP_SPREAD == 1.5F && gun.gunBeltFed()
                        && !gun.gunAutomatic() && gun.gunCrosshair() == SednaCrosshair.CIRCLE,
                "Tesla receiver timing, damage, spread, belt, and reticle must match XFactoryEnergy");
        helper.assertTrue(TeslaCannonItem.beltCount(stack) == 0
                        && TeslaCannonItem.loadedAmmo(stack) == EnergyAmmoType.STANDARD,
                "a fresh Tesla belt is empty but remembers the standard capacitor identity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void beamDirectionFollowsTheShootersView(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(117.0F);
        player.setXRot(-38.0F);
        TeslaBeamEntity beam = new TeslaBeamEntity(helper.getLevel(), player, EnergyAmmoType.STANDARD,
                35.0F, 0.0F, new Vec3(-0.375D, 0.0D, 0.75D));
        helper.assertTrue(beam.beamDirection().distanceToSqr(player.getLookAngle()) < 1.0E-6D,
                "the synced Tesla bolt direction must follow the shooter's camera");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void beltFeedsFirstCapacitorAndFiresTheRightBeam(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(ModItems.GUN_TESLA_CANNON.get());
        TeslaCannonItem.setTestState(gun, TeslaCannonItem.GunState.IDLE, EnergyAmmoType.STANDARD, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F); player.setXRot(0.0F);
        player.getInventory().add(FiftyCalAmmoType.FULL_METAL_JACKET.createStack(ModItems.AMMO_STANDARD.get(), 3));
        player.getInventory().add(EnergyAmmoType.OVERCHARGE.createStack(ModItems.AMMO_STANDARD.get(), 2));
        player.getInventory().add(EnergyAmmoType.STANDARD.createStack(ModItems.AMMO_STANDARD.get(), 4));

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<TeslaBeamEntity> beams = helper.getLevel().getEntitiesOfClass(TeslaBeamEntity.class,
                new AABB(player.position(), player.position()).inflate(260.0D));
        helper.assertTrue(beams.size() == 1 && beams.getFirst().ammoType() == EnergyAmmoType.OVERCHARGE
                        && beams.getFirst().beamDamage() == 52.5F,
                "the earliest compatible stack must fire a 35 x 1.5 overcharge beam");
        helper.assertTrue(count(player, EnergyAmmoType.OVERCHARGE) == 1
                        && count(player, EnergyAmmoType.STANDARD) == 4
                        && TeslaCannonItem.state(gun) == TeslaCannonItem.GunState.COOLDOWN
                        && TeslaCannonItem.timer(gun) == 20,
                "one capacitor is consumed and the semiauto action enters its twenty tick cooldown");
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
