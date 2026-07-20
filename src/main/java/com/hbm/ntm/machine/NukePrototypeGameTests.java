package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.NukePrototypeBlock;
import com.hbm.ntm.blockentity.NukePrototypeBlockEntity;
import com.hbm.ntm.config.HbmConfig;
import com.hbm.ntm.explosion.DetonationResult;
import com.hbm.ntm.explosion.RemoteDetonation;
import com.hbm.ntm.inventory.NukePrototypeMenu;
import com.hbm.ntm.item.BreedingRodItem;
import com.hbm.ntm.nuclear.FleijaCloudEntity;
import com.hbm.ntm.nuclear.FleijaExplosionEntity;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NukePrototypeGameTests {
    private NukePrototypeGameTests() { }

    /**
     * Source render mapping (metadata 5/3/4/2 -> 90/0/270/180) combined with the source
     * placement (facing i=0/1/2/3 -> meta 5/3/4/2) reduces to 90 - facing.toYRot().
     */
    @GameTest(template = "empty")
    public static void renderRotationMatchesSourceMetadataMapping(GameTestHelper helper) {
        check(helper, NukePrototypeBlock.renderRotationDegrees(Direction.SOUTH) == 90, "SOUTH must map to meta 5 -> 90");
        check(helper, NukePrototypeBlock.renderRotationDegrees(Direction.WEST) == 0, "WEST must map to meta 3 -> 0");
        check(helper, NukePrototypeBlock.renderRotationDegrees(Direction.NORTH) == 270, "NORTH must map to meta 4 -> 270");
        check(helper, NukePrototypeBlock.renderRotationDegrees(Direction.EAST) == 180, "EAST must map to meta 2 -> 180");
        // Placement stores the facing directly; confirm the state round-trips.
        BlockPos pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, ModBlocks.NUKE_PROTOTYPE.get().defaultBlockState()
                .setValue(NukePrototypeBlock.FACING, Direction.EAST));
        check(helper, helper.getBlockState(pos).getValue(NukePrototypeBlock.FACING) == Direction.EAST,
                "The placed Prototype must keep its facing blockstate");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void radiusDefaultsToOneHundredFifty(GameTestHelper helper) {
        check(helper, HbmConfig.PROTOTYPE_RADIUS.get() == 150,
                "The Prototype's FLEIJA radius must default to 150 (source config 3.05_prototypeRadius)");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void readinessRequiresTheExactFourteenSourceComponents(GameTestHelper helper) {
        BlockPos pos = placeBomb(helper);
        NukePrototypeBlockEntity bomb = bomb(helper, pos);
        check(helper, !bomb.isReady(), "An empty Prototype must not be ready");
        for (int slot = 0; slot < NukePrototypeBlockEntity.SLOTS; slot++) {
            bomb.setItem(slot, new ItemStack(Items.IRON_INGOT));
        }
        check(helper, !bomb.isReady(), "Fourteen random items must not arm the Prototype");

        int[] cells = {0, 1, 12, 13};
        int[] uranium = {2, 3, 10, 11};
        int[] lead = {4, 5, 8, 9};
        int[] neptunium = {6, 7};
        for (int slot : cells) bomb.setItem(slot, new ItemStack(ModItems.CELL_SAS3.get()));
        for (int slot : uranium) bomb.setItem(slot, quadRod(BreedingRodItem.Type.URANIUM));
        for (int slot : lead) bomb.setItem(slot, quadRod(BreedingRodItem.Type.LEAD));
        for (int slot : neptunium) bomb.setItem(slot, quadRod(BreedingRodItem.Type.NP237));
        check(helper, bomb.isReady(), "Four SAS3 Cells and the exact U/Lead/Np rod layout must arm the Prototype");

        bomb.setItem(13, new ItemStack(ModItems.CELL_TRITIUM.get()));
        check(helper, !bomb.isReady(), "A Tritium Cell must not pass for SAS3 in the Prototype");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void armingRodsKeepTheirSourceSlotLayout(GameTestHelper helper) {
        NukePrototypeBlockEntity bomb = bomb(helper, placeBomb(helper));
        int[] uranium = {2, 3, 10, 11};
        int[] lead = {4, 5, 8, 9};
        int[] neptunium = {6, 7};
        for (int slot : uranium) bomb.setItem(slot, quadRod(BreedingRodItem.Type.URANIUM));
        for (int slot : lead) bomb.setItem(slot, quadRod(BreedingRodItem.Type.LEAD));
        for (int slot : neptunium) bomb.setItem(slot, quadRod(BreedingRodItem.Type.NP237));
        check(helper, bomb.hasCorrectRodLayout(), "The Prototype must accept its exact U/Lead/Np rod layout");

        bomb.setItem(4, quadRod(BreedingRodItem.Type.URANIUM));
        check(helper, !bomb.hasCorrectRodLayout(), "A Uranium rod must not pass for Lead in slot 4");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void menuPlacesFourteenSlotsAtSourceCoordinates(GameTestHelper helper) {
        BlockPos pos = placeBomb(helper);
        NukePrototypeBlockEntity bomb = bomb(helper, pos);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        NukePrototypeMenu menu = new NukePrototypeMenu(0, player.getInventory(), bomb);
        int[][] coords = {
                {8, 35}, {26, 35}, {44, 26}, {44, 44}, {62, 26}, {62, 44}, {80, 26}, {80, 44},
                {98, 26}, {98, 44}, {116, 26}, {116, 44}, {134, 35}, {152, 35}
        };
        for (int i = 0; i < coords.length; i++) {
            Slot slot = menu.getSlot(i);
            check(helper, slot.x == coords[i][0] && slot.y == coords[i][1],
                    "Component slot " + i + " must sit at (" + coords[i][0] + "," + coords[i][1] + ")");
        }
        // Player inventory begins directly beneath at (8,84) and the hotbar at (8,142).
        check(helper, menu.getSlot(14).x == 8 && menu.getSlot(14).y == 84,
                "Player inventory must start at (8,84)");
        check(helper, menu.getSlot(49).x == 152 && menu.getSlot(49).y == 142,
                "The hotbar must end at (152,142)");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void inventoryPersistsWithStackLimitSixtyFour(GameTestHelper helper) {
        BlockPos pos = placeBomb(helper);
        NukePrototypeBlockEntity bomb = bomb(helper, pos);
        bomb.setItem(0, new ItemStack(Items.IRON_INGOT, 64));
        check(helper, bomb.getItem(0).getCount() == 64,
                "Prototype slots must accept the source stack limit of 64");
        var tag = bomb.saveWithoutMetadata(helper.getLevel().registryAccess());
        NukePrototypeBlockEntity loaded = new NukePrototypeBlockEntity(helper.absolutePos(pos), helper.getBlockState(pos));
        loaded.loadWithComponents(tag, helper.getLevel().registryAccess());
        check(helper, loaded.getItem(0).is(Items.IRON_INGOT) && loaded.getItem(0).getCount() == 64,
                "Prototype inventory must persist through save/load");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void remoteAndRedstoneStayInertWhenUnarmed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = placeBomb(helper);
        BlockPos absolute = helper.absolutePos(pos);

        RemoteDetonation.Attempt unarmed = RemoteDetonation.trigger(level, absolute);
        check(helper, unarmed.compatible() && unarmed.result() == DetonationResult.ERROR_MISSING_COMPONENT,
                "An unarmed Prototype must report ERROR_MISSING_COMPONENT to a remote detonator");
        check(helper, !helper.getBlockState(pos).isAir(), "A failed remote trigger must leave the block intact");

        helper.setBlock(pos.above(), Blocks.REDSTONE_BLOCK);
        check(helper, !helper.getBlockState(pos).isAir(), "An unarmed powered Prototype must not detonate");
        check(helper, explosions(helper, pos).isEmpty() && clouds(helper, pos).isEmpty(),
                "An unarmed powered Prototype must spawn no blast systems");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void igniterRightClickStaysGatedWhenUnarmed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = placeBomb(helper);
        BlockPos absolute = helper.absolutePos(pos);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack igniter = new ItemStack(ModItems.IGNITER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, igniter);
        level.getBlockState(absolute).useItemOn(igniter, level, player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
        check(helper, !helper.getBlockState(pos).isAir(),
                "A right-click with the igniter on an unarmed Prototype must not detonate it");
        check(helper, explosions(helper, pos).isEmpty() && clouds(helper, pos).isEmpty(),
                "An unarmed igniter click must spawn no blast systems");
        helper.succeed();
    }

    /**
     * The source {@code igniteTestBomb} is not guarded by isReady (its callers are), so it can
     * be exercised directly: the blast is centered on the block (+0.5) while the plain cyan
     * cloud sits at the raw coordinate, both using the given radius.
     */
    @GameTest(template = "empty")
    public static void igniteTestBombSpawnsCenteredBlastAndRawCloud(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = placeBomb(helper);
        BlockPos absolute = helper.absolutePos(pos);
        NukePrototypeBlock.igniteTestBomb(level, absolute, 2);

        FleijaExplosionEntity explosion = explosions(helper, pos).getFirst();
        FleijaCloudEntity cloud = clouds(helper, pos).getFirst();
        check(helper, explosions(helper, pos).size() == 1 && clouds(helper, pos).size() == 1,
                "igniteTestBomb must spawn exactly one FLEIJA blast and one cyan cloud");
        check(helper, explosion.getX() == absolute.getX() + 0.5D
                        && explosion.getY() == absolute.getY() + 0.5D
                        && explosion.getZ() == absolute.getZ() + 0.5D,
                "The FLEIJA blast must be centered on the block");
        check(helper, cloud.getX() == absolute.getX() && cloud.getY() == absolute.getY()
                        && cloud.getZ() == absolute.getZ(),
                "The cyan cloud must sit at the raw block coordinate");
        check(helper, explosion.radius() == 2 && cloud.maxAge() == 2,
                "The blast radius and cloud max age must both equal the ignite radius");
        discardBlast(helper, pos);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingLoadedBombScattersInventory(GameTestHelper helper) {
        BlockPos pos = placeBomb(helper);
        NukePrototypeBlockEntity bomb = bomb(helper, pos);
        for (int slot = 0; slot < NukePrototypeBlockEntity.SLOTS; slot++) {
            bomb.setItem(slot, new ItemStack(Items.IRON_INGOT, 20));
        }
        helper.setBlock(pos, Blocks.AIR);
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(pos)).inflate(2.0D));
        int total = drops.stream().filter(item -> item.getItem().is(Items.IRON_INGOT))
                .mapToInt(item -> item.getItem().getCount()).sum();
        check(helper, total == 14 * 20,
                "Breaking a loaded Prototype must scatter all fourteen stacks");
        helper.succeed();
    }

    private static BlockPos placeBomb(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 2, 3);
        helper.setBlock(pos, ModBlocks.NUKE_PROTOTYPE.get().defaultBlockState()
                .setValue(NukePrototypeBlock.FACING, Direction.SOUTH));
        return pos;
    }

    private static ItemStack quadRod(BreedingRodItem.Type type) {
        return BreedingRodItem.stack(ModItems.ROD_QUAD.get(), type, 1);
    }

    private static NukePrototypeBlockEntity bomb(GameTestHelper helper, BlockPos pos) {
        if (helper.getBlockEntity(pos) instanceof NukePrototypeBlockEntity bomb) return bomb;
        helper.fail("Expected Prototype block entity");
        throw new IllegalStateException();
    }

    private static List<FleijaExplosionEntity> explosions(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getEntitiesOfClass(FleijaExplosionEntity.class,
                new AABB(helper.absolutePos(pos)).inflate(3.0D));
    }

    private static List<FleijaCloudEntity> clouds(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getEntitiesOfClass(FleijaCloudEntity.class,
                new AABB(helper.absolutePos(pos)).inflate(3.0D));
    }

    private static void discardBlast(GameTestHelper helper, BlockPos pos) {
        explosions(helper, pos).forEach(Entity::discard);
        clouds(helper, pos).forEach(Entity::discard);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
