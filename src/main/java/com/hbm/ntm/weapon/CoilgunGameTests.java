package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.item.CoilgunItem;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
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
public final class CoilgunGameTests {
    private CoilgunGameTests() { }

    @GameTest(template = "empty")
    public static void receiverAndBallsKeepTheirSourceContract(GameTestHelper helper) {
        CoilgunItem gun = ModItems.GUN_COILGUN.get();
        ItemStack stack = new ItemStack(gun);
        ItemStack tungsten = CoilAmmoType.TUNGSTEN.createStack(ModItems.AMMO_STANDARD.get(), 5);
        ItemStack ferro = CoilAmmoType.FERROURANIUM.createStack(ModItems.AMMO_STANDARD.get(), 4);

        helper.assertTrue(CoilAmmoType.TUNGSTEN.legacyMetadata() == 71
                        && CoilAmmoType.FERROURANIUM.legacyMetadata() == 72
                        && StandardAmmoTypes.fromStack(tungsten) == CoilAmmoType.TUNGSTEN
                        && StandardAmmoTypes.fromStack(ferro) == CoilAmmoType.FERROURANIUM,
                "coil ammo ids");
        helper.assertTrue(CoilAmmoType.TUNGSTEN.projectileSpeed() == 7.5D
                        && CoilAmmoType.TUNGSTEN.projectileLifetime() == 50
                        && CoilAmmoType.TUNGSTEN.spectral()
                        && CoilAmmoType.TUNGSTEN.penetrates()
                        && !CoilAmmoType.TUNGSTEN.penetrationDamageFalloff()
                        && CoilAmmoType.TUNGSTEN.blockBreakHardness() == 1.25F
                        && CoilAmmoType.FERROURANIUM.blockBreakHardness() == 2.5F,
                "coil projectile profiles");
        helper.assertTrue(gun.gunDurability() == 400.0F && gun.gunCapacity() == 1
                        && CoilgunItem.DRAW_TICKS == 5 && CoilgunItem.INSPECT_TICKS == 39
                        && CoilgunItem.FIRE_DELAY == 5 && CoilgunItem.RELOAD_TICKS == 20
                        && CoilgunItem.JAM_TICKS == 33 && CoilgunItem.BASE_DAMAGE == 35.0F
                        && gun.gunCrosshair() == SednaCrosshair.L_CIRCUMFLEX
                        && gun.recoilVertical() == 10.0F && gun.recoilHorizontalSigma() == 1.5F,
                "coil receiver profile");
        helper.assertTrue(CoilgunItem.rounds(stack) == 0
                        && CoilgunItem.loadedAmmo(stack) == CoilAmmoType.TUNGSTEN,
                "fresh coilgun state");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reloadTakesTwentyTicksAndUsesInventoryFirst(GameTestHelper helper) {
        Player player = armedPlayer(helper, 0, CoilAmmoType.TUNGSTEN);
        player.getInventory().add(CoilAmmoType.FERROURANIUM.createStack(
                ModItems.AMMO_STANDARD.get(), 2));
        player.getInventory().add(CoilAmmoType.TUNGSTEN.createStack(
                ModItems.AMMO_STANDARD.get(), 2));

        SednaGunItem.handleInput(player, GunInput.RELOAD);
        helper.assertTrue(CoilgunItem.state(player.getMainHandItem())
                        == CoilgunItem.GunState.RELOADING
                        && CoilgunItem.timer(player.getMainHandItem()) == 20,
                "coil reload start");
        tickHeld(player, 19);
        helper.assertTrue(CoilgunItem.rounds(player.getMainHandItem()) == 0,
                "coil reload timing");
        tickHeld(player, 1);
        helper.assertTrue(CoilgunItem.rounds(player.getMainHandItem()) == 1
                        && CoilgunItem.loadedAmmo(player.getMainHandItem())
                        == CoilAmmoType.FERROURANIUM
                        && countAmmo(player, CoilAmmoType.FERROURANIUM) == 1
                        && countAmmo(player, CoilAmmoType.TUNGSTEN) == 2,
                "coil reload result");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "coilgun_receiver_isolated")
    public static void ferrouraniumShotUsesTheLiveReceiver(GameTestHelper helper) {
        Player player = armedPlayer(helper, 1, CoilAmmoType.FERROURANIUM);
        ItemStack stack = player.getMainHandItem();

        SednaGunItem.handleInput(player, GunInput.PRIMARY);
        List<BulletEntity> bullets = bullets(helper, player);
        helper.assertTrue(bullets.size() == 1
                        && bullets.getFirst().ammoType() == CoilAmmoType.FERROURANIUM
                        && bullets.getFirst().damage() == 35.0F
                        && Math.abs(bullets.getFirst().getDeltaMovement().length() - 7.5D) < 0.01D,
                "coil shot projectile");
        helper.assertTrue(CoilgunItem.rounds(stack) == 0
                        && CoilgunItem.wear(stack) == 1.0F
                        && CoilgunItem.state(stack) == CoilgunItem.GunState.COOLDOWN
                        && CoilgunItem.timer(stack) == 5,
                "coil shot state");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void coilBallsBreakDifferentHardnesses(GameTestHelper helper) {
        BlockPos tungstenStart = new BlockPos(2, 3, 2);
        BlockPos ferroStart = new BlockPos(5, 3, 2);
        helper.setBlock(new BlockPos(2, 3, 4), Blocks.DIRT);
        helper.setBlock(new BlockPos(2, 3, 6), Blocks.STONE);
        helper.setBlock(new BlockPos(5, 3, 4), Blocks.DIRT);
        helper.setBlock(new BlockPos(5, 3, 6), Blocks.STONE);

        BulletEntity tungsten = projectile(helper, tungstenStart, CoilAmmoType.TUNGSTEN);
        BulletEntity ferro = projectile(helper, ferroStart, CoilAmmoType.FERROURANIUM);
        tungsten.tick();
        ferro.tick();

        helper.assertBlockPresent(Blocks.AIR, new BlockPos(2, 3, 4));
        helper.assertBlockPresent(Blocks.STONE, new BlockPos(2, 3, 6));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(5, 3, 4));
        helper.assertBlockPresent(Blocks.AIR, new BlockPos(5, 3, 6));
        helper.assertTrue(tungsten.getZ() > helper.absolutePos(tungstenStart).getZ() + 7.0D
                        && ferro.getZ() > helper.absolutePos(ferroStart).getZ() + 7.0D,
                "spectral travel");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "coilgun_penetration_isolated")
    public static void coilBallPenetratesWithoutDamageLoss(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        Zombie first = target(helper, new BlockPos(2, 3, 5));
        Zombie second = target(helper, new BlockPos(2, 3, 8));
        Vec3 origin = new Vec3(player.getX(), player.getY() + 0.9D, player.getZ());
        BulletEntity bullet = new BulletEntity(helper.getLevel(), player, CoilAmmoType.TUNGSTEN,
                35.0F, 0.0F, origin, new Vec3(0.0D, 0.0D, 1.0D));
        bullet.tick();

        helper.assertTrue(first.getHealth() < 100.0F
                        && Math.abs(first.getHealth() - second.getHealth()) < 0.001F
                        && bullet.damage() == 35.0F && !bullet.isRemoved(),
                "coil penetration damage");
        helper.succeed();
    }

    private static Player armedPlayer(GameTestHelper helper, int rounds, CoilAmmoType ammo) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack gun = new ItemStack(ModItems.GUN_COILGUN.get());
        CoilgunItem.setTestState(gun, CoilgunItem.GunState.IDLE, 0, rounds, ammo,
                0.0F, CoilgunItem.GunAnimation.CYCLE);
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.setPos(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 3, 2))));
        player.setYRot(0.0F);
        player.setXRot(0.0F);
        return player;
    }

    private static BulletEntity projectile(GameTestHelper helper, BlockPos start, CoilAmmoType ammo) {
        Vec3 origin = Vec3.atCenterOf(helper.absolutePos(start));
        return new BulletEntity(helper.getLevel(), null, ammo, 35.0F, 0.0F,
                origin, new Vec3(0.0D, 0.0D, 1.0D));
    }

    private static Zombie target(GameTestHelper helper, BlockPos position) {
        Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
        helper.assertTrue(zombie != null, "zombie");
        zombie.setNoAi(true);
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0D);
        zombie.setHealth(100.0F);
        zombie.setPos(Vec3.atBottomCenterOf(helper.absolutePos(position)));
        helper.getLevel().addFreshEntity(zombie);
        return zombie;
    }

    private static void tickHeld(Player player, int ticks) {
        CoilgunItem item = (CoilgunItem) player.getMainHandItem().getItem();
        for (int i = 0; i < ticks; i++) {
            item.inventoryTick(player.getMainHandItem(), player.level(), player,
                    player.getInventory().selected, true);
        }
    }

    private static List<BulletEntity> bullets(GameTestHelper helper, Player owner) {
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        return helper.getLevel().getEntitiesOfClass(BulletEntity.class,
                new AABB(origin).inflate(64.0D), bullet -> bullet.getOwner() == owner);
    }

    private static int countAmmo(Player player, CoilAmmoType ammo) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.AMMO_STANDARD.get())
                    && StandardAmmoTypes.fromStack(stack) == ammo) count += stack.getCount();
        }
        return count;
    }
}
