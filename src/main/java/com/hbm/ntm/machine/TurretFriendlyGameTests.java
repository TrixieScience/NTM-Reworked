package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.TurretFriendlyBlock;
import com.hbm.ntm.blockentity.TurretFriendlyBlockEntity;
import com.hbm.ntm.item.TurretChipItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.weapon.FiveFiveSixAmmoType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TurretFriendlyGameTests {
    private TurretFriendlyGameTests() { }

    @GameTest(template = "empty")
    public static void sourceFootprintAndInventoryRulesStayIntact(GameTestHelper helper) {
        BlockPos[] parts = TurretFriendlyBlock.parts(BlockPos.ZERO, Direction.NORTH);
        helper.assertTrue(parts.length == 4 && java.util.Set.of(parts).size() == 4,
                "Mister Friendly must keep its source 2x2 mount");
        assertOffset(helper, Direction.NORTH, 1D, 0D);
        assertOffset(helper, Direction.SOUTH, 0D, 1D);
        assertOffset(helper, Direction.EAST, 1D, 1D);
        assertOffset(helper, Direction.WEST, 0D, 0D);

        Vec3 east = TurretFriendlyBlockEntity.barrelDirection(0F, -90F);
        Vec3 west = TurretFriendlyBlockEntity.barrelDirection(0F, 90F);
        helper.assertTrue(east.x > 0.999D && Math.abs(east.z) < 0.001D
                        && west.x < -0.999D && Math.abs(west.z) < 0.001D,
                "The barrel must fire toward its target instead of through the rear of the model");

        Vec3 compensated = TurretFriendlyBlockEntity.compensatedDirection(
                new Vec3(0D, 0D, 20D), new Vec3(2D, 0D, 0D), 10D);
        Vec3 impact = compensated.scale(10D * 2.041241452D)
                .add(new Vec3(2D, 0D, 0D).scale(2.041241452D));
        helper.assertTrue(Math.abs(impact.x) < 0.001D && Math.abs(impact.z - 20D) < 0.001D,
                "A moving aircraft turret must cancel inherited sideways velocity when aiming");

        AABB pigSizedTarget = AABB.ofSize(new Vec3(0D, 1D, 20D), 0.9D, 1.0D, 0.9D);
        helper.assertTrue(TurretFriendlyBlockEntity.shotIntersects(pigSizedTarget, Vec3.ZERO,
                        new Vec3(0D, 0.5D, 10D))
                        && !TurretFriendlyBlockEntity.shotIntersects(pigSizedTarget, Vec3.ZERO,
                        new Vec3(2D, 0.5D, 10D)),
                "Mister Friendly must only fire when the actual shot crosses its target");

        TurretFriendlyBlockEntity turret = new TurretFriendlyBlockEntity(BlockPos.ZERO,
                ModBlocks.TURRET_FRIENDLY.get().defaultBlockState());
        ItemStack chip = new ItemStack(ModItems.TURRET_CHIP.get());
        ItemStack ammo = FiveFiveSixAmmoType.ARMOR_PIERCING.createStack(ModItems.AMMO_STANDARD.get(), 4);
        helper.assertTrue(turret.getContainerSize() == 11 && turret.canPlaceItem(0, chip)
                        && turret.canPlaceItem(1, ammo) && !turret.canPlaceItem(10, ammo),
                "The source chip, nine ammo, and battery slots must remain distinct");
        helper.succeed();
    }

    private static void assertOffset(GameTestHelper helper, Direction facing,
                                     double expectedX, double expectedZ) {
        Vec3 offset = TurretFriendlyBlock.horizontalOffset(facing);
        helper.assertTrue(Math.abs(offset.x - expectedX) < 0.001D
                        && Math.abs(offset.z - expectedZ) < 0.001D,
                "Mister Friendly's model origin must follow its rotated 2x2 footprint");
    }

    @GameTest(template = "empty")
    public static void chipKeepsNamesAndTurretConsumesPowerAndAmmo(GameTestHelper helper) {
        ItemStack chip = new ItemStack(ModItems.TURRET_CHIP.get());
        TurretChipItem.add(chip, "TwilightSparkle");
        helper.assertTrue(TurretChipItem.contains(chip, "twilightsparkle"),
                "Turret chip names must survive in component data");

        BlockPos turretPos = new BlockPos(2, 1, 2);
        helper.setBlock(turretPos, ModBlocks.TURRET_FRIENDLY.get().defaultBlockState());
        TurretFriendlyBlockEntity turret = helper.getBlockEntity(turretPos);
        ItemStack ammo = FiveFiveSixAmmoType.SOFT_POINT.createStack(ModItems.AMMO_STANDARD.get(), 8);
        turret.setItem(1, ammo);
        turret.setPower(TurretFriendlyBlockEntity.MAX_POWER);
        turret.clickButton(0);
        var zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 12));
        zombie.setNoAi(true);

        helper.runAfterDelay(40, () -> {
            helper.assertTrue(turret.getPower() < TurretFriendlyBlockEntity.MAX_POWER,
                    "An enabled turret must spend 100 HE/t");
            helper.assertTrue(turret.getItem(1).getCount() < 8,
                    "Mister Friendly must fire one of its accepted 5.56 rounds");
            helper.succeed();
        });
    }
}
