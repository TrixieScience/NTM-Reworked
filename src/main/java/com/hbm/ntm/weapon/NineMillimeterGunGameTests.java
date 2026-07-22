package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.item.NineMillimeterGunItem;
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
public final class NineMillimeterGunGameTests {
    private NineMillimeterGunGameTests() { }

    @GameTest(template = "empty")
    public static void nineMillimeterLoadsPreserveSourceIdentityAndStats(GameTestHelper helper) {
        for (NineMillimeterAmmoType type : NineMillimeterAmmoType.values()) {
            ItemStack stack = type.createStack(ModItems.AMMO_STANDARD.get(), 8);
            helper.assertTrue(NineMillimeterAmmoType.fromStack(stack) == type,
                    type + " must survive component serialization");
            helper.assertTrue(StandardAmmoTypes.fromStack(stack) == type,
                    type + " must remain distinguishable through the shared ammo carrier");
            helper.assertTrue(type.legacyMetadata() >= 20 && type.legacyMetadata() <= 23,
                    type + " must preserve legacy ammo_standard metadata");
            helper.assertTrue(type.legacyBulletConfig() == type.legacyMetadata() + 2,
                    type + " must preserve its source BulletConfig index");
        }
        helper.assertTrue(NineMillimeterAmmoType.FULL_METAL_JACKET.damageMultiplier() == 0.8F
                        && NineMillimeterAmmoType.FULL_METAL_JACKET.armorThresholdNegation() == 2.0F
                        && NineMillimeterAmmoType.FULL_METAL_JACKET.armorPiercing() == 0.1F,
                "9mm FMJ must retain its source damage, DT negation, and armor piercing");
        helper.assertTrue(NineMillimeterAmmoType.HOLLOW_POINT.damageMultiplier() == 1.5F
                        && NineMillimeterAmmoType.HOLLOW_POINT.headshotMultiplier() == 1.5F
                        && NineMillimeterAmmoType.HOLLOW_POINT.armorPiercing() == -0.25F,
                "9mm JHP must retain its source damage and headshot multipliers");
        helper.assertTrue(NineMillimeterAmmoType.ARMOR_PIERCING.penetrates()
                        && !NineMillimeterAmmoType.ARMOR_PIERCING.penetrationDamageFalloff()
                        && NineMillimeterAmmoType.ARMOR_PIERCING.armorThresholdNegation() == 5.0F,
                "9mm AP must penetrate without falloff and retain five DT negation");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void greaseGunAndUziUseExactSourceReceiverValues(GameTestHelper helper) {
        NineMillimeterGunItem grease = ModItems.GUN_GREASEGUN.get();
        NineMillimeterGunItem uzi = ModItems.GUN_UZI.get();
        ItemStack greaseStack = new ItemStack(grease);
        ItemStack uziStack = new ItemStack(uzi);

        helper.assertTrue(grease.gunAutomatic() && grease.gunCapacity() == 30
                        && grease.variant().drawTicks() == 20 && grease.variant().fireDelay() == 4
                        && grease.variant().dryTicks() == 40 && grease.variant().reloadTicks() == 60
                        && grease.variant().jamTicks() == 55 && grease.variant().innateSpread() == 0.015F,
                "Grease Gun must retain the source automatic receiver timings and spread");
        helper.assertTrue(uzi.gunAutomatic() && uzi.gunCapacity() == 30
                        && uzi.variant().drawTicks() == 15 && uzi.variant().fireDelay() == 2
                        && uzi.variant().dryTicks() == 25 && uzi.variant().reloadTicks() == 55
                        && uzi.variant().jamTicks() == 50 && uzi.variant().innateSpread() == 0.005F,
                "Uzi must retain the source automatic receiver timings and spread");
        helper.assertTrue(NineMillimeterGunItem.rounds(greaseStack) == 30
                        && NineMillimeterGunItem.rounds(uziStack) == 30
                        && NineMillimeterGunItem.loadedAmmo(greaseStack) == NineMillimeterAmmoType.SOFT_POINT
                        && NineMillimeterGunItem.loadedAmmo(uziStack) == NineMillimeterAmmoType.SOFT_POINT,
                "Both new guns must spawn with the source thirty-round Soft Point magazine");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void heldUziRefiresEveryTwoTicksAndReleaseStopsIt(GameTestHelper helper) {
        Player player = armedPlayer(helper, NineMillimeterGunItem.Variant.UZI,
                NineMillimeterAmmoType.SOFT_POINT, 5, 0.0F, 2);
        ItemStack stack = player.getMainHandItem();

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        helper.assertTrue(NineMillimeterGunItem.rounds(stack) == 4
                        && NineMillimeterGunItem.timer(stack) == 2
                        && NineMillimeterGunItem.primaryHeld(stack),
                "First Uzi shot must consume one round and start its two-tick cycle");
        tickHeld(helper, player, 1);
        helper.assertTrue(NineMillimeterGunItem.rounds(stack) == 4
                        && NineMillimeterGunItem.state(stack) == NineMillimeterGunItem.GunState.COOLDOWN,
                "Uzi must remain locked through cycle tick one");
        tickHeld(helper, player, 1);
        helper.assertTrue(NineMillimeterGunItem.rounds(stack) == 3
                        && NineMillimeterGunItem.timer(stack) == 2
                        && bullets(helper, player).size() == 2,
                "Holding primary must produce one exact automatic refire on cycle tick two");

        SednaGunItem.handleInput(player, GunInput.PRIMARY_RELEASE);
        tickHeld(helper, player, 2);
        helper.assertTrue(NineMillimeterGunItem.rounds(stack) == 3
                        && NineMillimeterGunItem.state(stack) == NineMillimeterGunItem.GunState.IDLE
                        && !NineMillimeterGunItem.primaryHeld(stack),
                "Releasing primary must stop the automatic receiver after its current cycle");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fullReloadLocksLoadedTypeAndEmptyMagazineSelectsNineMillimeter(GameTestHelper helper) {
        Player partial = armedPlayer(helper, NineMillimeterGunItem.Variant.GREASE_GUN,
                NineMillimeterAmmoType.FULL_METAL_JACKET, 2, 0.0F, 2);
        partial.getInventory().add(NineMillimeterAmmoType.SOFT_POINT.createStack(ModItems.AMMO_STANDARD.get(), 8));
        partial.getInventory().add(NineMillimeterAmmoType.FULL_METAL_JACKET.createStack(ModItems.AMMO_STANDARD.get(), 28));
        ItemStack partialGun = partial.getMainHandItem();

        SednaGunItem.handleInput(partial, GunInput.RELOAD);
        helper.assertTrue(NineMillimeterGunItem.state(partialGun) == NineMillimeterGunItem.GunState.RELOADING
                        && NineMillimeterGunItem.timer(partialGun) == 60
                        && NineMillimeterGunItem.amountBeforeReload(partialGun) == 2,
                "Grease Gun full reload must begin with the source sixty-tick phase");
        tickHeld(helper, partial, 60);
        helper.assertTrue(NineMillimeterGunItem.rounds(partialGun) == 30
                        && NineMillimeterGunItem.loadedAmmo(partialGun) == NineMillimeterAmmoType.FULL_METAL_JACKET
                        && countAmmo(partial, NineMillimeterAmmoType.SOFT_POINT) == 8,
                "A partial magazine must accept only its already loaded 9mm identity");

        Player empty = armedPlayer(helper, NineMillimeterGunItem.Variant.UZI,
                NineMillimeterAmmoType.SOFT_POINT, 0, 0.0F, 8);
        empty.getInventory().add(PepperboxAmmoType.STONE.createStack(ModItems.AMMO_STANDARD.get(), 2));
        empty.getInventory().add(Magnum357AmmoType.SOFT_POINT.createStack(ModItems.AMMO_STANDARD.get(), 2));
        empty.getInventory().add(NineMillimeterAmmoType.HOLLOW_POINT.createStack(ModItems.AMMO_STANDARD.get(), 30));
        ItemStack emptyGun = empty.getMainHandItem();
        SednaGunItem.handleInput(empty, GunInput.RELOAD);
        tickHeld(helper, empty, 55);
        helper.assertTrue(NineMillimeterGunItem.rounds(emptyGun) == 30
                        && NineMillimeterGunItem.loadedAmmo(emptyGun) == NineMillimeterAmmoType.HOLLOW_POINT,
                "An empty Uzi must select the first compatible 9mm identity");
        helper.assertTrue(countAmmo(empty, PepperboxAmmoType.STONE) == 2
                        && countAmmo(empty, Magnum357AmmoType.SOFT_POINT) == 2,
                "9mm reload must not consume other ammo_standard calibers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wearCurvesAndAutomaticEmptyBehaviorMatchSource(GameTestHelper helper) {
        helper.assertTrue(NineMillimeterGunItem.wearSpread(1_500.0F) == 0.0F
                        && NineMillimeterGunItem.wearSpread(3_000.0F) == 0.125F,
                "Wear spread must begin at fifty percent and reach 0.125 at maximum wear");
        helper.assertTrue(NineMillimeterGunItem.wearDamageMultiplier(2_250.0F) == 1.0F
                        && NineMillimeterGunItem.wearDamageMultiplier(3_000.0F) == 0.5F,
                "Wear damage must remain full through seventy-five percent and halve at maximum wear");
        helper.assertTrue(NineMillimeterGunItem.jamChance(1_980.0F) < 0.0001F
                        && NineMillimeterGunItem.jamChance(2_730.0F) > 0.999F,
                "Jam chance must begin above sixty-six percent and cap at ninety-one percent wear");

        Player player = armedPlayer(helper, NineMillimeterGunItem.Variant.UZI,
                NineMillimeterAmmoType.SOFT_POINT, 0, 0.0F, 2);
        ItemStack stack = player.getMainHandItem();
        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        helper.assertTrue(NineMillimeterGunItem.animation(stack) == NineMillimeterGunItem.GunAnimation.CYCLE_DRY
                        && NineMillimeterGunItem.state(stack) == NineMillimeterGunItem.GunState.DRAWING
                        && NineMillimeterGunItem.timer(stack) == 25,
                "An empty Uzi click must use its source twenty-five-tick dry cycle");
        tickHeld(helper, player, 25);
        helper.assertTrue(NineMillimeterGunItem.state(stack) == NineMillimeterGunItem.GunState.IDLE
                        && NineMillimeterGunItem.rounds(stack) == 0,
                "Holding an empty automatic Uzi must not repeatedly dry-fire");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void modernizedGreaseGunUsesTheSourceReceiverChanges(GameTestHelper helper) {
        Player player = armedPlayer(helper, NineMillimeterGunItem.Variant.GREASE_GUN,
                NineMillimeterAmmoType.SOFT_POINT, 3, 0.0F, 2);
        ItemStack stack = player.getMainHandItem();
        WeaponModManager.install(stack, 0,
                List.of(new ItemStack(ModItems.WEAPON_MOD_GREASE_GUN.get())));

        helper.assertTrue(NineMillimeterGunItem.durability(stack) == 9_000.0F
                        && NineMillimeterGunItem.baseDamage(stack) == 5.0F
                        && NineMillimeterGunItem.innateSpread(stack) == 0.0F
                        && NineMillimeterGunItem.fireDelay(stack) == 2
                        && NineMillimeterGunItem.casingTick(stack) == 1
                        && stack.getDescriptionId().equals("item.hbm.gun_greasegun_m3"),
                "Modernized M3 must triple durability, add two damage, remove innate spread, and halve delay");
        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        helper.assertTrue(NineMillimeterGunItem.rounds(stack) == 2
                        && NineMillimeterGunItem.timer(stack) == 2
                        && bullets(helper, player).size() == 1
                        && bullets(helper, player).getFirst().damage() == 5.0F,
                "Modernized M3 shot must use its source five damage and two-tick automatic cycle");
        helper.succeed();
    }

    private static Player armedPlayer(GameTestHelper helper, NineMillimeterGunItem.Variant variant,
                                      NineMillimeterAmmoType ammo, int rounds, float wear, int x) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(variant == NineMillimeterGunItem.Variant.GREASE_GUN
                ? ModItems.GUN_GREASEGUN.get() : ModItems.GUN_UZI.get());
        NineMillimeterGunItem.setTestState(gun, NineMillimeterGunItem.GunState.IDLE,
                0, rounds, ammo, wear, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(x, 2, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        return player;
    }

    private static void tickHeld(GameTestHelper helper, Player player, int ticks) {
        NineMillimeterGunItem gun = (NineMillimeterGunItem) player.getMainHandItem().getItem();
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
