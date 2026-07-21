package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.BombMultiBlock;
import com.hbm.ntm.blockentity.BombMultiBlockEntity;
import com.hbm.ntm.compat.BombImpactFuseEvents;
import com.hbm.ntm.compat.BombImpactFusePhysics;
import com.hbm.ntm.explosion.DetonationResult;
import com.hbm.ntm.explosion.MultiBombExplosion;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BombMultiGameTests {
    private BombMultiGameTests() { }

    private static ItemStack gunpowder() { return new ItemStack(Items.GUNPOWDER); }
    private static ItemStack tnt() { return new ItemStack(Items.TNT); }
    private static ItemStack powderFire() { return new ItemStack(ModItems.legacyOreResourceItem("powder_fire").get()); }
    private static ItemStack pelletGas() { return new ItemStack(ModItems.PELLET_GAS.get()); }

    @GameTest(template = "empty")
    public static void aeronauticsImpactFuseArmsOnHardImpact(GameTestHelper helper) {
        check(helper, !BombImpactFusePhysics.shouldDetonate(4.99D),
                "An impact below Sable's five-speed threshold must not fire the bomb");
        check(helper, BombImpactFusePhysics.shouldDetonate(5.0D),
                "The fuse must fire at the exact five-speed source threshold");
        check(helper, BombImpactFusePhysics.shouldDetonate(20.0D),
                "A hard slam must fire regardless of how far the bomb fell or was thrown");
        check(helper, BombImpactFusePhysics.shouldDetonate(-12.0D),
                "Sable may report signed collision speed, so a fast downward impact must still fire");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aeronauticsImpactDetonationWaitsUntilPhysicsTickEnds(GameTestHelper helper) {
        AtomicBoolean detonated = new AtomicBoolean();
        BombImpactFuseEvents.queue(helper.getLevel(), helper.absolutePos(BlockPos.ZERO),
                () -> detonated.set(true));
        check(helper, !detonated.get(), "Impact callback must not detonate while Rapier is stepping");
        helper.runAfterDelay(1, () -> {
            check(helper, detonated.get(), "Queued impact must detonate at the end of the server tick");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void loadedRequiresFourCornerTnt(GameTestHelper helper) {
        BlockPos pos = placeBomb(helper);
        BombMultiBlockEntity bomb = bomb(helper, pos);
        check(helper, !bomb.isLoaded(), "Empty Multi Purpose Bomb must not be loaded");
        loadCorners(bomb);
        check(helper, bomb.isLoaded(), "Four corner TNT must load the bomb");
        // Modifier slots do not affect the loaded state.
        bomb.setItem(2, gunpowder());
        bomb.setItem(5, pelletGas());
        check(helper, bomb.isLoaded(), "Modifier slots must not affect the loaded state");
        bomb.setItem(3, ItemStack.EMPTY);
        check(helper, !bomb.isLoaded(), "Missing a corner TNT must unload the bomb");
        bomb.setItem(3, new ItemStack(Blocks.DIRT));
        check(helper, !bomb.isLoaded(), "A non-TNT corner must unload the bomb");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void modifierTypeMapping(GameTestHelper helper) {
        // Item -> type mapping. pellet_cluster (3) and powder_poison
        // (5) are intentionally unreachable: their ingredients are not registered in this port.
        check(helper, BombMultiBlockEntity.typeOf(gunpowder()) == 1, "gunpowder -> 1");
        check(helper, BombMultiBlockEntity.typeOf(tnt()) == 2, "TNT -> 2");
        check(helper, BombMultiBlockEntity.typeOf(powderFire()) == 4, "powder_fire -> 4");
        check(helper, BombMultiBlockEntity.typeOf(pelletGas()) == 6, "pellet_gas -> 6");
        check(helper, BombMultiBlockEntity.typeOf(new ItemStack(Blocks.DIRT)) == 0, "other item -> 0");
        check(helper, BombMultiBlockEntity.typeOf(ItemStack.EMPTY) == 0, "empty -> 0");

        BlockPos pos = placeBomb(helper);
        BombMultiBlockEntity bomb = bomb(helper, pos);
        bomb.setItem(2, powderFire());
        bomb.setItem(5, pelletGas());
        check(helper, bomb.return2type() == 4 && bomb.return5type() == 6,
                "Slots 2 and 5 must resolve their own modifier types");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void strengthAndEffectAccumulationMatrix(GameTestHelper helper) {
        check(helper, MultiBombExplosion.config(0, 0).explosionValue() == 8.0F, "base strength 8");
        check(helper, MultiBombExplosion.config(1, 1).explosionValue() == 10.0F, "gunpowder + gunpowder = 10");
        check(helper, MultiBombExplosion.config(2, 2).explosionValue() == 16.0F, "TNT + TNT = 16");
        check(helper, MultiBombExplosion.config(1, 2).explosionValue() == 13.0F, "gunpowder + TNT = 13");
        check(helper, MultiBombExplosion.config(4, 4).explosionValue() == 8.0F,
                "non-strength modifiers must leave the base strength at 8");

        check(helper, MultiBombExplosion.config(4, 0).fireRadius() == 10
                && MultiBombExplosion.config(4, 4).fireRadius() == 20, "fire radius 10 / 20");
        check(helper, MultiBombExplosion.config(5, 0).poisonRadius() == 15
                && MultiBombExplosion.config(5, 5).poisonRadius() == 30, "poison radius 15 / 30");
        check(helper, MultiBombExplosion.config(3, 0).clusterCount() == 50
                && MultiBombExplosion.config(3, 3).clusterCount() == 100, "cluster count 50 / 100");
        check(helper, MultiBombExplosion.config(6, 0).gasCloud() == 50
                && MultiBombExplosion.config(6, 6).gasCloud() == 100, "gas cloud 50 / 100");

        MultiBombExplosion.Config mixed = MultiBombExplosion.config(3, 4);
        check(helper, mixed.explosionValue() == 8.0F && mixed.clusterCount() == 50 && mixed.fireRadius() == 10,
                "mixed cluster + fire must accumulate independently");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void detonationConsumesLoadAndFiresBaseBlast(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 3, 4);
        placeBomb(helper, pos);
        BombMultiBlockEntity bomb = bomb(helper, pos);
        loadCorners(bomb);
        helper.setBlock(pos.above(), Blocks.STONE);
        check(helper, ModBlocks.BOMB_MULTI.get().detonate(helper.getLevel(), helper.absolutePos(pos)),
                "Loaded bomb must detonate");
        check(helper, helper.getBlockState(pos).isAir(), "Detonation must remove the bomb block");
        check(helper, helper.getBlockState(pos.above()).isAir(),
                "Base griefing blast must destroy the adjacent block");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unloadedBombDoesNotDetonate(GameTestHelper helper) {
        BlockPos pos = placeBomb(helper);
        check(helper, !ModBlocks.BOMB_MULTI.get().detonate(helper.getLevel(), helper.absolutePos(pos)),
                "Unloaded bomb must not detonate");
        check(helper, helper.getBlockState(pos).is(ModBlocks.BOMB_MULTI.get()),
                "Unloaded bomb block must remain intact");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void clearForDetonationEmptiesAndFlags(GameTestHelper helper) {
        BlockPos pos = placeBomb(helper);
        BombMultiBlockEntity bomb = bomb(helper, pos);
        loadCorners(bomb);
        bomb.setItem(2, gunpowder());
        bomb.clearForDetonation();
        check(helper, bomb.detonating(), "clearForDetonation must set the detonating flag");
        for (int i = 0; i < bomb.getContainerSize(); i++) {
            check(helper, bomb.getItem(i).isEmpty(), "clearForDetonation must empty every slot");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void redstoneDetonatesLoadedBomb(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 3, 4);
        placeBomb(helper, pos);
        loadCorners(bomb(helper, pos));
        // A powered neighbor fires neighborChanged and takes the redstone path.
        helper.setBlock(pos.south(), Blocks.REDSTONE_BLOCK);
        check(helper, helper.getBlockState(pos).isAir(),
                "A redstone signal must detonate a loaded bomb");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void redstoneIgnoresUnloadedBomb(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 3, 4);
        placeBomb(helper, pos);
        helper.setBlock(pos.south(), Blocks.REDSTONE_BLOCK);
        check(helper, helper.getBlockState(pos).is(ModBlocks.BOMB_MULTI.get()),
                "A redstone signal must not detonate an unloaded bomb");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void remoteDetonationDetonatesLoadedBomb(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 3, 4);
        placeBomb(helper, pos);
        loadCorners(bomb(helper, pos));
        check(helper, ModBlocks.BOMB_MULTI.get().detonateRemotely(helper.getLevel(),
                        helper.absolutePos(pos)) == DetonationResult.DETONATED,
                "Remote detonation of a loaded bomb must report DETONATED");
        check(helper, helper.getBlockState(pos).isAir(), "Remote detonation must remove the bomb");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void remoteDetonationRejectsUnloadedBomb(GameTestHelper helper) {
        BlockPos pos = placeBomb(helper);
        check(helper, ModBlocks.BOMB_MULTI.get().detonateRemotely(helper.getLevel(),
                        helper.absolutePos(pos)) == DetonationResult.ERROR_MISSING_COMPONENT,
                "Remote detonation of an unloaded bomb must report ERROR_MISSING_COMPONENT");
        check(helper, helper.getBlockState(pos).is(ModBlocks.BOMB_MULTI.get()),
                "An unloaded remote attempt must leave the bomb intact");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingScattersAllSixStacks(GameTestHelper helper) {
        BlockPos pos = placeBomb(helper);
        BombMultiBlockEntity bomb = bomb(helper, pos);
        loadCorners(bomb);
        bomb.setItem(2, gunpowder());
        bomb.setItem(5, pelletGas());
        helper.setBlock(pos, Blocks.AIR);
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(pos)).inflate(2.0D));
        check(helper, countDrops(drops, Items.TNT) == 4, "Breaking must scatter all four corner TNT");
        check(helper, countDrops(drops, Items.GUNPOWDER) == 1, "Breaking must scatter the slot-2 modifier");
        check(helper, countDrops(drops, ModItems.PELLET_GAS.get()) == 1, "Breaking must scatter the slot-5 modifier");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fireModeIgnitesSphere(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 3, 4);
        helper.setBlock(center, Blocks.STONE);
        helper.setBlock(center.above(), Blocks.AIR);
        BlockPos abs = helper.absolutePos(center);
        MultiBombExplosion.igniteAllBlocks(helper.getLevel(), abs.getX(), abs.getY(), abs.getZ(), 3);
        check(helper, helper.getBlockState(center.above()).is(Blocks.FIRE),
                "Fire mode must set fire above a solid block inside the sphere");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wasteModeConvertsTerrain(GameTestHelper helper) {
        BlockPos center = new BlockPos(4, 3, 4);
        helper.setBlock(center, Blocks.GRASS_BLOCK);
        helper.setBlock(center.east(), Blocks.OAK_LEAVES);
        helper.setBlock(center.south(), Blocks.OAK_PLANKS);
        BlockPos abs = helper.absolutePos(center);
        MultiBombExplosion.wasteNoSchrab(helper.getLevel(), abs.getX(), abs.getY(), abs.getZ(), 5);
        check(helper, helper.getBlockState(center).is(ModBlocks.WASTE_EARTH.get()),
                "Waste mode must convert grass to waste earth");
        check(helper, helper.getBlockState(center.east()).isAir(),
                "Waste mode must vaporise leaves");
        check(helper, helper.getBlockState(center.south()).is(ModBlocks.WASTE_PLANKS.get()),
                "Waste mode must convert planks to waste planks");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gasConfiguredBombIsReachableButEmitsNoCloud(GameTestHelper helper) {
        // pellet_gas IS registered, so a gas-configured bomb is reachable. Its EntityMist area cloud
        // is not implemented; the bomb still detonates with the base blast.
        BlockPos pos = new BlockPos(4, 3, 4);
        placeBomb(helper, pos);
        BombMultiBlockEntity bomb = bomb(helper, pos);
        loadCorners(bomb);
        bomb.setItem(2, pelletGas());
        check(helper, bomb.return2type() == 6, "pellet_gas must resolve to mode 6");
        check(helper, ModBlocks.BOMB_MULTI.get().detonateRemotely(helper.getLevel(),
                        helper.absolutePos(pos)) == DetonationResult.DETONATED,
                "A gas-configured bomb must still detonate");
        check(helper, helper.getBlockState(pos).isAir(), "The gas bomb must remove its block");
        helper.succeed();
    }

    private static BlockPos placeBomb(GameTestHelper helper) {
        return placeBomb(helper, new BlockPos(3, 2, 3));
    }

    private static BlockPos placeBomb(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, ModBlocks.BOMB_MULTI.get().defaultBlockState()
                .setValue(BombMultiBlock.FACING, Direction.SOUTH));
        return pos;
    }

    private static BombMultiBlockEntity bomb(GameTestHelper helper, BlockPos pos) {
        if (helper.getBlockEntity(pos) instanceof BombMultiBlockEntity bomb) return bomb;
        helper.fail("Expected Multi Purpose Bomb block entity");
        throw new IllegalStateException();
    }

    private static void loadCorners(BombMultiBlockEntity bomb) {
        bomb.setItem(0, new ItemStack(Items.TNT));
        bomb.setItem(1, new ItemStack(Items.TNT));
        bomb.setItem(3, new ItemStack(Items.TNT));
        bomb.setItem(4, new ItemStack(Items.TNT));
    }

    private static int countDrops(List<ItemEntity> drops, Item item) {
        return drops.stream().filter(entity -> entity.getItem().is(item))
                .mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
