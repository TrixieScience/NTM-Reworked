package com.hbm.ntm.conveyor;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.AbstractConveyorBlock;
import com.hbm.ntm.block.ConveyorBlock;
import com.hbm.ntm.block.ConveyorBoxerBlock;
import com.hbm.ntm.block.ConveyorChuteBlock;
import com.hbm.ntm.block.ConveyorLiftBlock;
import com.hbm.ntm.block.AbstractCraneBlock;
import com.hbm.ntm.blockentity.ConveyorBoxerBlockEntity;
import com.hbm.ntm.blockentity.CraneExtractorBlockEntity;
import com.hbm.ntm.blockentity.CraneGrabberBlockEntity;
import com.hbm.ntm.blockentity.CraneInserterBlockEntity;
import com.hbm.ntm.entity.MovingConveyorItemEntity;
import com.hbm.ntm.entity.MovingConveyorPackageEntity;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConveyorGameTests {
    private ConveyorGameTests() {
    }

    @GameTest(template = "empty")
    public static void flatVariantsPreserveSpeedCurvesBoundsAndLaneSnapping(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        var level = helper.getLevel();
        ConveyorBlock regular = ModBlocks.CONVEYOR.get();
        BlockState straight = regular.defaultBlockState().setValue(ConveyorBlock.FACING, Direction.NORTH);
        check(helper, straight.getCollisionShape(level, pos).bounds().maxY == 0.25D,
                "Flat conveyors must retain the source quarter-block collision height");
        check(helper, regular.inputDirection(level, pos, straight) == Direction.NORTH
                        && regular.outputDirection(level, pos, straight) == Direction.SOUTH,
                "Flat conveyor facing must describe source input while movement goes to its opposite");

        Vec3 directionalCenter = Vec3.atLowerCornerOf(pos).add(0.5D, 0.25D, 0.5D);
        for (Direction input : Direction.Plane.HORIZONTAL) {
            BlockState directional = regular.defaultBlockState().setValue(ConveyorBlock.FACING, input);
            Vec3 moved = regular.travelLocation(level, pos, directional, directionalCenter, 0.0625D);
            check(helper, regular.inputDirection(level, pos, directional) == input
                            && regular.outputDirection(level, pos, directional) == input.getOpposite()
                            && close(moved.x - directionalCenter.x, -input.getStepX() * 0.0625D)
                            && close(moved.z - directionalCenter.z, -input.getStepZ() * 0.0625D),
                    "Straight conveyors must move in all four horizontal orientations (input=" + input + ")");

            BlockState directionalLeft = directional.setValue(ConveyorBlock.CURVE, ConveyorCurve.LEFT);
            BlockState directionalRight = directional.setValue(ConveyorBlock.CURVE, ConveyorCurve.RIGHT);
            check(helper, regular.outputDirection(level, pos, directionalLeft)
                            == input.getOpposite().getCounterClockWise()
                            && regular.outputDirection(level, pos, directionalRight)
                            == input.getOpposite().getClockWise(),
                    "Left/right bends must expose correct exits for all four facings (input=" + input + ")");
        }

        BlockState left = straight.setValue(ConveyorBlock.CURVE, ConveyorCurve.LEFT);
        BlockState right = straight.setValue(ConveyorBlock.CURVE, ConveyorCurve.RIGHT);
        check(helper, regular.outputDirection(level, pos, left) == Direction.EAST
                        && regular.outputDirection(level, pos, right) == Direction.WEST,
                "Left and right curve exits must be perpendicular to the incoming path");

        Vec3 center = Vec3.atLowerCornerOf(pos).add(0.5D, 0.25D, 0.5D);
        Vec3 normalTravel = regular.travelLocation(level, pos, straight, center, 0.0625D);
        Vec3 expressTravel = ModBlocks.CONVEYOR_EXPRESS.get().travelLocation(level, pos,
                ModBlocks.CONVEYOR_EXPRESS.get().defaultBlockState().setValue(ConveyorBlock.FACING,
                        Direction.NORTH), center, 0.0625D);
        check(helper, close(normalTravel.z - center.z, 0.0625D)
                        && close(expressTravel.z - center.z, 0.1875D),
                "Express conveyors must move items at exactly three times regular speed");

        Vec3 offCenter = Vec3.atLowerCornerOf(pos).add(0.8D, 0.25D, 0.5D);
        Vec3 doubleLane = ModBlocks.CONVEYOR_DOUBLE.get().closestSnappingPosition(level, pos,
                ModBlocks.CONVEYOR_DOUBLE.get().defaultBlockState(), offCenter);
        Vec3 tripleLane = ModBlocks.CONVEYOR_TRIPLE.get().closestSnappingPosition(level, pos,
                ModBlocks.CONVEYOR_TRIPLE.get().defaultBlockState(), offCenter);
        Vec3 tripleCenter = ModBlocks.CONVEYOR_TRIPLE.get().closestSnappingPosition(level, pos,
                ModBlocks.CONVEYOR_TRIPLE.get().defaultBlockState(),
                Vec3.atLowerCornerOf(pos).add(0.51D, 0.25D, 0.5D));
        check(helper, close(doubleLane.x, pos.getX() + 0.75D)
                        && close(tripleLane.x, pos.getX() + 0.8125D)
                        && close(tripleCenter.x, pos.getX() + 0.5D),
                "Double and triple belts must preserve their source lane offsets and center threshold");

        Vec3 innerLaneBefore = Vec3.atLowerCornerOf(pos).add(0.25D, 0.25D, 0.70D);
        Vec3 innerLaneAfter = Vec3.atLowerCornerOf(pos).add(0.25D, 0.25D, 0.80D);
        Vec3 outerLaneAfter = Vec3.atLowerCornerOf(pos).add(0.75D, 0.25D, 0.30D);
        check(helper, regular.travelDirection(level, pos, left, innerLaneBefore) == Direction.NORTH
                        && regular.travelDirection(level, pos, left, innerLaneAfter) == Direction.WEST
                        && regular.travelDirection(level, pos, left, outerLaneAfter) == Direction.WEST,
                "Curved multi-lane belts must switch each lane at the source corner-diamond boundary");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void flatConveyorsCarryPlayersAndMobsUsingBeltDirectionAndSpeed(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        Vec3 center = Vec3.atLowerCornerOf(pos).add(0.5D, 0.25D, 0.5D);
        BlockState regular = ModBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.NORTH);

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setPos(center.x, center.y, center.z);
        player.setDeltaMovement(0.0D, 0.1D, 0.0D);
        ModBlocks.CONVEYOR.get().stepOn(level, pos, regular, player);
        check(helper, close(player.getDeltaMovement().x, 0.0D)
                        && close(player.getDeltaMovement().y, 0.1D)
                        && close(player.getDeltaMovement().z, 0.1D),
                "A regular straight conveyor must carry a standing player toward its output without changing Y");

        Zombie zombie = EntityType.ZOMBIE.create(level);
        check(helper, zombie != null, "The conveyor mob transport test must create a living mob");
        if (zombie == null) return;
        zombie.setPos(center.x, center.y, center.z);
        zombie.setDeltaMovement(Vec3.ZERO);
        BlockState express = ModBlocks.CONVEYOR_EXPRESS.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.EAST);
        ModBlocks.CONVEYOR_EXPRESS.get().stepOn(level, pos, express, zombie);
        check(helper, close(zombie.getDeltaMovement().x, -0.3D)
                        && close(zombie.getDeltaMovement().z, 0.0D),
                "An Express conveyor must carry mobs toward its output at three times regular speed");

        player.setDeltaMovement(Vec3.ZERO);
        BlockState leftCurve = regular.setValue(ConveyorBlock.CURVE, ConveyorCurve.LEFT);
        ModBlocks.CONVEYOR.get().stepOn(level, pos, leftCurve, player);
        check(helper, close(player.getDeltaMovement().x, 0.1D)
                        && close(player.getDeltaMovement().z, 0.0D),
                "Living entities must follow the same resolved output direction as conveyor corners");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void movingItemsWaitThenTravelAndFallOffAsVanillaDrops(GameTestHelper helper) {
        BlockPos beltPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockState belt = ModBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.NORTH);
        helper.getLevel().setBlock(beltPos, belt, Block.UPDATE_ALL);

        MovingConveyorItemEntity moving = MovingConveyorItemEntity.create(helper.getLevel(),
                new ItemStack(Items.IRON_INGOT, 3));
        moving.setPos(beltPos.getX() + 0.5D, beltPos.getY() + 0.25D, beltPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(moving);
        double startZ = moving.getZ();
        for (int tick = 0; tick < 5; tick++) {
            moving.tickCount++;
            moving.tick();
        }
        check(helper, close(moving.getZ(), startZ), "Conveyed items must retain the source five-tick spawn grace");
        moving.tickCount++;
        moving.tick();
        check(helper, close(moving.getZ(), startZ + 0.0625D),
                "A regular belt must advance a conveyed item by one sixteenth per tick");

        moving.setPos(beltPos.getX() + 0.5D, beltPos.getY() + 0.25D, beltPos.getZ() + 1.05D);
        moving.setDeltaMovement(0, 0, 0.0625D);
        moving.tick();
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                moving.getBoundingBox().inflate(2.0D));
        check(helper, !moving.isAlive() && drops.stream().anyMatch(drop -> drop.getItem().is(Items.IRON_INGOT)
                        && drop.getItem().getCount() == 3 && drop.lifespan == 60 * 20),
                "Leaving a conveyor must restore the complete stack with the source 60-second lifespan");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void agedVanillaDropsConvertToMovingConveyorItems(GameTestHelper helper) {
        BlockPos beltPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockState belt = ModBlocks.CONVEYOR_DOUBLE.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.NORTH);
        helper.getLevel().setBlock(beltPos, belt, Block.UPDATE_ALL);
        ItemEntity dropped = new ItemEntity(helper.getLevel(), beltPos.getX() + 0.76D,
                beltPos.getY() + 0.25D, beltPos.getZ() + 0.5D,
                new ItemStack(Items.GOLD_INGOT, 5));
        dropped.tickCount = 11;
        helper.getLevel().addFreshEntity(dropped);

        belt.entityInside(helper.getLevel(), beltPos, dropped);
        List<MovingConveyorItemEntity> moving = helper.getLevel().getEntitiesOfClass(
                MovingConveyorItemEntity.class, dropped.getBoundingBox().inflate(2.0D));
        check(helper, !dropped.isAlive() && moving.size() == 1
                        && moving.getFirst().getItemStack().is(Items.GOLD_INGOT)
                        && moving.getFirst().getItemStack().getCount() == 5
                        && close(moving.getFirst().getX(), beltPos.getX() + 0.75D),
                "Aged vanilla drops must convert intact and snap to the nearest conveyor lane");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void packagesPreserveSparseSlotsAcrossEntityPersistence(GameTestHelper helper) {
        MovingConveyorPackageEntity original = MovingConveyorPackageEntity.create(helper.getLevel(), List.of(
                new ItemStack(Items.IRON_INGOT, 2), ItemStack.EMPTY,
                new ItemStack(Items.COPPER_INGOT, 4), ItemStack.EMPTY));
        CompoundTag saved = original.saveWithoutId(new CompoundTag());
        MovingConveyorPackageEntity loaded = MovingConveyorPackageEntity.create(helper.getLevel(), List.of());
        loaded.load(saved);
        List<ItemStack> contents = loaded.getItemStacks();
        check(helper, saved.getInt("Count") == 4 && contents.size() == 4
                        && contents.get(0).is(Items.IRON_INGOT) && contents.get(0).getCount() == 2
                        && contents.get(1).isEmpty()
                        && contents.get(2).is(Items.COPPER_INGOT) && contents.get(2).getCount() == 4
                        && contents.get(3).isEmpty(),
                "Conveyor packages must retain their exact array length and sparse slot indices in NBT");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void screwdriverCyclesFlatLiftAndChuteSourceStates(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockState initial = ModBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.NORTH);
        helper.getLevel().setBlock(pos, initial, Block.UPDATE_ALL);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack screwdriver = new ItemStack(ModItems.SCREWDRIVER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, screwdriver);
        player.setShiftKeyDown(true);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);

        useScrewdriver(helper, pos, player, screwdriver, hit);
        check(helper, helper.getLevel().getBlockState(pos).getValue(ConveyorBlock.CURVE) == ConveyorCurve.LEFT,
                "Sneaking screwdriver use must cycle straight belts to a left bend");
        useScrewdriver(helper, pos, player, screwdriver, hit);
        check(helper, helper.getLevel().getBlockState(pos).getValue(ConveyorBlock.CURVE) == ConveyorCurve.RIGHT,
                "Sneaking screwdriver use must cycle left bends to right bends");
        useScrewdriver(helper, pos, player, screwdriver, hit);
        check(helper, helper.getLevel().getBlockState(pos).is(ModBlocks.CONVEYOR_LIFT.get()),
                "A regular right bend must cycle into a chain lift");
        useScrewdriver(helper, pos, player, screwdriver, hit);
        check(helper, helper.getLevel().getBlockState(pos).is(ModBlocks.CONVEYOR_CHUTE.get()),
                "A chain lift must cycle into a chute");
        useScrewdriver(helper, pos, player, screwdriver, hit);
        check(helper, helper.getLevel().getBlockState(pos).is(ModBlocks.CONVEYOR.get())
                        && helper.getLevel().getBlockState(pos).getValue(ConveyorBlock.CURVE)
                        == ConveyorCurve.STRAIGHT,
                "A chute must complete the cycle back to a straight regular belt");

        player.setShiftKeyDown(false);
        useScrewdriver(helper, pos, player, screwdriver, hit);
        check(helper, helper.getLevel().getBlockState(pos).getValue(ConveyorBlock.FACING) == Direction.EAST,
                "Normal screwdriver use must rotate conveyor facing clockwise");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void conveyorEndpointInsertsIntoModernInventoryCapability(GameTestHelper helper) {
        BlockPos beltPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos chestPos = beltPos.south();
        helper.getLevel().setBlock(beltPos, ModBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);

        MovingConveyorItemEntity moving = MovingConveyorItemEntity.create(helper.getLevel(),
                new ItemStack(Items.COPPER_INGOT, 7));
        moving.setPos(beltPos.getX() + 0.5D, beltPos.getY() + 0.25D, beltPos.getZ() + 0.98D);
        helper.getLevel().addFreshEntity(moving);
        for (int tick = 0; tick < 6; tick++) {
            moving.tickCount++;
            moving.tick();
        }
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos);
        int inserted = chest == null ? 0 : chest.countItem(Items.COPPER_INGOT);
        check(helper, !moving.isAlive() && inserted == 7,
                "Conveyed stacks must enter current NeoForge item-handler inventories without cranes");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void liftAndChutePreserveVerticalMiddleAndHorizontalExitMotion(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        ConveyorLiftBlock lift = ModBlocks.CONVEYOR_LIFT.get();
        BlockState middleLift = lift.defaultBlockState().setValue(ConveyorLiftBlock.BOTTOM, false)
                .setValue(ConveyorLiftBlock.TOP, false).setValue(AbstractConveyorBlock.FACING, Direction.NORTH);
        BlockState topLift = middleLift.setValue(ConveyorLiftBlock.TOP, true);
        Vec3 middle = Vec3.atLowerCornerOf(pos).add(0.3D, 0.5D, 0.8D);
        Vec3 liftVertical = lift.travelLocation(level, pos, middleLift, middle, 0.0625D);
        Vec3 liftExit = lift.travelLocation(level, pos, topLift,
                Vec3.atLowerCornerOf(pos).add(0.5D, 0.25D, 0.5D), 0.0625D);
        check(helper, liftVertical.y > middle.y && liftVertical.x > middle.x && liftVertical.z < middle.z
                        && liftExit.z > pos.getZ() + 0.5D,
                "Lift middles must rise toward center while top segments exit opposite their facing");
        check(helper, topLift.getCollisionShape(level, pos).bounds().maxY == 0.5D
                        && middleLift.getCollisionShape(level, pos).bounds().maxY == 1.0D,
                "Lift top and middle collision heights must remain one-half and one block");

        ConveyorChuteBlock chute = ModBlocks.CONVEYOR_CHUTE.get();
        BlockState middleChute = chute.defaultBlockState().setValue(ConveyorChuteBlock.BOTTOM, false)
                .setValue(AbstractConveyorBlock.FACING, Direction.NORTH);
        BlockState bottomChute = middleChute.setValue(ConveyorChuteBlock.BOTTOM, true);
        Vec3 centeredMiddle = Vec3.atLowerCornerOf(pos).add(0.5D, 0.5D, 0.5D);
        Vec3 chuteVertical = chute.travelLocation(level, pos, middleChute, centeredMiddle, 0.0625D);
        Vec3 chuteExit = chute.travelLocation(level, pos, bottomChute,
                Vec3.atLowerCornerOf(pos).add(0.5D, 0.25D, 0.5D), 0.0625D);
        check(helper, chuteVertical.y < centeredMiddle.y
                        && close(centeredMiddle.y - chuteVertical.y, 0.3125D)
                        && chuteExit.z > pos.getZ() + 0.5D,
                "Chutes must descend at fivefold speed when connected and exit horizontally at the bottom");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void chuteTopologyTracksAllSourceSideOpenings(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 3, 2));
        ConveyorChuteBlock chute = ModBlocks.CONVEYOR_CHUTE.get();
        BlockState belt = ModBlocks.CONVEYOR.get().defaultBlockState();

        level.setBlock(pos.below(), belt, Block.UPDATE_ALL);
        level.setBlock(pos.north(), belt, Block.UPDATE_ALL);
        level.setBlock(pos.east(), belt, Block.UPDATE_ALL);
        BlockState vertical = chute.recalculateState(chute.defaultBlockState()
                .setValue(AbstractConveyorBlock.FACING, Direction.NORTH), level, pos);
        level.setBlock(pos, vertical, Block.UPDATE_ALL);

        check(helper, !vertical.getValue(ConveyorChuteBlock.BOTTOM)
                        && vertical.getValue(ConveyorChuteBlock.NORTH)
                        && vertical.getValue(ConveyorChuteBlock.EAST)
                        && !vertical.getValue(ConveyorChuteBlock.SOUTH)
                        && !vertical.getValue(ConveyorChuteBlock.WEST),
                "A descending chute must open only the walls with horizontal conveyor neighbors");

        level.removeBlock(pos.below(), false);
        BlockState bottom = chute.recalculateState(level.getBlockState(pos), level, pos);
        check(helper, bottom.getValue(ConveyorChuteBlock.BOTTOM)
                        && chute.travelDirection(level, pos, bottom,
                        Vec3.atLowerCornerOf(pos).add(0.5D, 0.25D, 0.5D)) == Direction.NORTH
                        && chute.outputDirection(level, pos, bottom) == Direction.DOWN,
                "A source bottom chute must keep its vertical API output while its floor exits opposite facing");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "conveyor_route_isolated")
    public static void routeBuilderCreatesTurnsVerticalRunsAndHonorsLimits(GameTestHelper helper) {
        var level = helper.getLevel();
        // The shared empty template is intentionally only 1x1x1 and GameTest
        // surrounds it with barriers. Clear the exact route volume so this
        // spatial test exercises the builder instead of the test enclosure.
        for (int z = 1; z <= 4; z++) helper.setBlock(new BlockPos(1, 2, z), Blocks.AIR);
        for (int x = 1; x <= 4; x++) helper.setBlock(new BlockPos(x, 2, 4), Blocks.AIR);
        for (int y = 2; y <= 6; y++) helper.setBlock(new BlockPos(1, y, 1), Blocks.AIR);
        BlockPos start = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockPos end = helper.absolutePos(new BlockPos(4, 1, 4));
        ConveyorWandItem.Route corner = ConveyorWandItem.planRoute(level, ConveyorType.REGULAR,
                Direction.SOUTH, start, Direction.UP, end, Direction.UP, 32);
        check(helper, corner.status() == ConveyorWandItem.RouteStatus.SUCCESS
                        && corner.placements().stream().anyMatch(placement -> placement.state().getBlock()
                        instanceof ConveyorBlock && placement.state().getValue(ConveyorBlock.CURVE)
                        != ConveyorCurve.STRAIGHT),
                "Two-click routing must construct an actual bend between offset endpoints (status="
                        + corner.status() + ", segments=" + corner.placements().size() + ", curves="
                        + corner.placements().stream().filter(placement -> placement.state().getBlock()
                        instanceof ConveyorBlock && placement.state().getValue(ConveyorBlock.CURVE)
                        != ConveyorCurve.STRAIGHT).count() + ", obstruction=" + corner.obstruction()
                        + (corner.obstruction() == null ? "" : ", state="
                        + level.getBlockState(corner.obstruction())) + ")");

        BlockPos verticalEnd = start.above(4);
        ConveyorWandItem.Route lift = ConveyorWandItem.planRoute(level, ConveyorType.REGULAR,
                Direction.SOUTH, start, Direction.UP, verticalEnd, Direction.UP, 16);
        check(helper, lift.status() == ConveyorWandItem.RouteStatus.SUCCESS
                        && lift.placements().stream().allMatch(placement -> placement.state().is(
                        ModBlocks.CONVEYOR_LIFT.get())),
                "Regular conveyor routing must build a chain-lift run between vertically aligned endpoints");

        ConveyorWandItem.Route tooShort = ConveyorWandItem.planRoute(level, ConveyorType.REGULAR,
                Direction.SOUTH, start, Direction.UP, end, Direction.UP, 2);
        check(helper, tooShort.status() == ConveyorWandItem.RouteStatus.INSUFFICIENT
                        && tooShort.placements().size() == 2
                        && tooShort.placements().stream().allMatch(placement ->
                        level.getBlockState(placement.pos()).isAir()),
                "Insufficient routes must expose a red partial preview without mutating the world");

        BlockPos blocked = start.relative(Direction.UP);
        level.setBlock(blocked, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
        ConveyorWandItem.Route obstruction = ConveyorWandItem.planRoute(level, ConveyorType.DOUBLE,
                Direction.SOUTH, start, Direction.UP, end, Direction.UP, 32);
        check(helper, obstruction.status() == ConveyorWandItem.RouteStatus.OBSTRUCTED,
                "A non-replaceable first segment must cancel the complete route");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void conveyorRecipesAndInventoryRepresentationsMatchFamilies(GameTestHelper helper) {
        checkRecipe(helper, "conveyor_wand", ModItems.CONVEYOR_WAND.get(), 16);
        checkRecipe(helper, "conveyor_wand_rubber", ModItems.CONVEYOR_WAND.get(), 64);
        checkRecipe(helper, "conveyor_wand_express", ModItems.CONVEYOR_WAND_EXPRESS.get(), 8);
        checkRecipe(helper, "conveyor_wand_double", ModItems.CONVEYOR_WAND_DOUBLE.get(), 1);
        checkRecipe(helper, "conveyor_wand_triple", ModItems.CONVEYOR_WAND_TRIPLE.get(), 1);
        checkRecipe(helper, "part_generic", ModItems.PART_GENERIC.get(), 4);
        checkRecipe(helper, "crane_extractor_from_stone", ModItems.CRANE_EXTRACTOR_ITEM.get(), 1);
        checkRecipe(helper, "crane_inserter_from_iron", ModItems.CRANE_INSERTER_ITEM.get(), 2);
        checkRecipe(helper, "crane_grabber_from_steel", ModItems.CRANE_GRABBER_ITEM.get(), 4);
        checkRecipe(helper, "crane_boxer", ModItems.CRANE_BOXER_ITEM.get(), 1);
        check(helper, ConveyorWandItem.stackFor(ConveyorType.REGULAR, 3).getCount() == 3
                        && ConveyorWandItem.stackFor(ConveyorType.EXPRESS, 1).is(ModItems.CONVEYOR_WAND_EXPRESS.get())
                        && ConveyorWandItem.stackFor(ConveyorType.DOUBLE, 1).is(ModItems.CONVEYOR_WAND_DOUBLE.get())
                        && ConveyorWandItem.stackFor(ConveyorType.TRIPLE, 1).is(ModItems.CONVEYOR_WAND_TRIPLE.get()),
                "All four source conveyor subtypes must have distinct modern inventory identities");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void boxerPreservesSourceDirectionConfigurationRules(GameTestHelper helper) {
        BlockState initial = ModBlocks.CRANE_BOXER.get().defaultBlockState()
                .setValue(ConveyorBoxerBlock.INPUT, Direction.NORTH)
                .setValue(ConveyorBoxerBlock.OUTPUT, Direction.SOUTH);
        BlockState inputTwice = ConveyorBoxerBlock.setInput(initial, Direction.NORTH);
        check(helper, inputTwice.getValue(ConveyorBoxerBlock.INPUT) == Direction.SOUTH
                        && inputTwice.getValue(ConveyorBoxerBlock.OUTPUT) == Direction.NORTH,
                "Clicking the current boxer input twice must reverse it and swap the output");

        BlockState outputOntoInput = ConveyorBoxerBlock.setOutput(initial, Direction.NORTH);
        check(helper, outputOntoInput.getValue(ConveyorBoxerBlock.INPUT) == Direction.SOUTH
                        && outputOntoInput.getValue(ConveyorBoxerBlock.OUTPUT) == Direction.NORTH,
                "Setting boxer output onto its input must swap the two sides");

        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlock(pos, initial, Block.UPDATE_ALL);
        ConveyorBoxerBlock boxer = ModBlocks.CRANE_BOXER.get();
        MovingConveyorItemEntity item = MovingConveyorItemEntity.create(helper.getLevel(),
                new ItemStack(Items.IRON_INGOT));
        check(helper, boxer.canConveyorItemEnter(helper.getLevel(), pos, Direction.NORTH, item)
                        && !boxer.canConveyorItemEnter(helper.getLevel(), pos, Direction.SOUTH, item),
                "Loose conveyor items must only enter the source-configured boxer input side");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void boxerRedstoneModePackagesEveryOccupiedSlotOncePerRisingEdge(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockState state = ModBlocks.CRANE_BOXER.get().defaultBlockState()
                .setValue(ConveyorBoxerBlock.INPUT, Direction.NORTH)
                .setValue(ConveyorBoxerBlock.OUTPUT, Direction.SOUTH);
        helper.getLevel().setBlock(pos, state, Block.UPDATE_ALL);
        helper.getLevel().setBlock(pos.south(), ModBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        ConveyorBoxerBlockEntity boxer = (ConveyorBoxerBlockEntity) helper.getLevel().getBlockEntity(pos);
        boxer.setMode(ConveyorBoxerBlockEntity.MODE_REDSTONE);
        boxer.setItem(0, new ItemStack(Items.IRON_INGOT, 3));
        boxer.setItem(3, new ItemStack(Items.GOLD_INGOT, 5));
        helper.getLevel().setBlock(pos.west(), Blocks.REDSTONE_BLOCK.defaultBlockState(), Block.UPDATE_ALL);

        ConveyorBoxerBlockEntity.tick(helper.getLevel(), pos, state, boxer);
        List<MovingConveyorPackageEntity> packages = helper.getLevel().getEntitiesOfClass(
                MovingConveyorPackageEntity.class, new AABB(pos).inflate(3.0D));
        check(helper, packages.size() == 1 && boxer.isEmpty(),
                "A redstone rising edge must package all occupied boxer slots when an output belt exists");
        List<ItemStack> contents = packages.getFirst().getItemStacks();
        check(helper, contents.size() == 2 && contents.get(0).is(Items.GOLD_INGOT)
                        && contents.get(0).getCount() == 5 && contents.get(1).is(Items.IRON_INGOT)
                        && contents.get(1).getCount() == 3,
                "Redstone packages must retain the source reverse-slot array order and partial counts");

        ConveyorBoxerBlockEntity.tick(helper.getLevel(), pos, state, boxer);
        check(helper, helper.getLevel().getEntitiesOfClass(MovingConveyorPackageEntity.class,
                        new AABB(pos).inflate(3.0D)).size() == 1,
                "A held redstone signal must not produce more than one package");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 10)
    public static void boxerAutomaticModeWaitsForFourCompletelyFullStacks(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockState state = ModBlocks.CRANE_BOXER.get().defaultBlockState()
                .setValue(ConveyorBoxerBlock.INPUT, Direction.NORTH)
                .setValue(ConveyorBoxerBlock.OUTPUT, Direction.SOUTH);
        helper.getLevel().setBlock(pos, state, Block.UPDATE_ALL);
        helper.getLevel().setBlock(pos.south(), ModBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        ConveyorBoxerBlockEntity boxer = (ConveyorBoxerBlockEntity) helper.getLevel().getBlockEntity(pos);
        boxer.setMode(ConveyorBoxerBlockEntity.MODE_4);
        for (int slot = 0; slot < 3; slot++) boxer.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        boxer.setItem(3, new ItemStack(Items.COBBLESTONE, 63));

        helper.runAfterDelay(2, () -> {
            check(helper, helper.getLevel().getEntitiesOfClass(MovingConveyorPackageEntity.class,
                            new AABB(pos).inflate(3.0D)).isEmpty(),
                    "Automatic mode must reject a nearly-full fourth stack");
            boxer.setItem(3, new ItemStack(Items.COBBLESTONE, 64));
            helper.runAfterDelay(2, () -> {
                List<MovingConveyorPackageEntity> packages = helper.getLevel().getEntitiesOfClass(
                        MovingConveyorPackageEntity.class, new AABB(pos).inflate(3.0D));
                check(helper, packages.size() == 1 && packages.getFirst().getItemStacks().size() == 4,
                        "Mode 4 must emit exactly four completely full stacks on its even-tick check");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void extractorPullsMachineOutputOntoConfiguredConveyor(GameTestHelper helper) {
        BlockPos extractorPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos chestPos = extractorPos.north();
        BlockPos beltPos = extractorPos.south();
        BlockState extractorState = ModBlocks.CRANE_EXTRACTOR.get().defaultBlockState()
                .setValue(AbstractCraneBlock.INPUT, Direction.SOUTH)
                .setValue(AbstractCraneBlock.OUTPUT, Direction.NORTH);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(beltPos, ModBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
        helper.getLevel().setBlock(extractorPos, extractorState, Block.UPDATE_ALL);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.IRON_INGOT, 3));

        helper.runAfterDelay(22, () -> {
            List<MovingConveyorItemEntity> moving = helper.getLevel().getEntitiesOfClass(
                    MovingConveyorItemEntity.class, new AABB(beltPos).inflate(2D));
            check(helper, chest.countItem(Items.IRON_INGOT) == 2 && moving.size() == 1
                            && moving.getFirst().getItemStack().is(Items.IRON_INGOT)
                            && moving.getFirst().getItemStack().getCount() == 1,
                    "The source Conveyor Ejector must pull one permitted output every 20 ticks onto its belt side");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void inserterAcceptsConveyorItemAndUsesConfiguredMachineFace(GameTestHelper helper) {
        BlockPos inserterPos = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos chestPos = inserterPos.south();
        BlockState inserterState = ModBlocks.CRANE_INSERTER.get().defaultBlockState()
                .setValue(AbstractCraneBlock.INPUT, Direction.NORTH)
                .setValue(AbstractCraneBlock.OUTPUT, Direction.SOUTH);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(inserterPos, inserterState, Block.UPDATE_ALL);
        CraneInserterBlockEntity inserter = (CraneInserterBlockEntity)
                helper.getLevel().getBlockEntity(inserterPos);
        inserter.accept(new ItemStack(Items.COPPER_INGOT, 7));
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos);
        check(helper, chest.countItem(Items.COPPER_INGOT) == 7 && inserter.isEmpty(),
                "The Conveyor Inserter must place an incoming moving stack into its configured output inventory");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 30)
    public static void grabberFiltersPassingItemsAndHonorsStackUpgrade(GameTestHelper helper) {
        BlockPos grabberPos = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos chestPos = grabberPos.north();
        BlockState grabberState = ModBlocks.CRANE_GRABBER.get().defaultBlockState()
                .setValue(AbstractCraneBlock.INPUT, Direction.SOUTH)
                .setValue(AbstractCraneBlock.OUTPUT, Direction.NORTH);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(grabberPos, grabberState, Block.UPDATE_ALL);
        CraneGrabberBlockEntity grabber = (CraneGrabberBlockEntity)
                helper.getLevel().getBlockEntity(grabberPos);
        grabber.setFilter(0, new ItemStack(Items.IRON_INGOT));
        grabber.toggleWhitelist();
        grabber.setItem(CraneGrabberBlockEntity.STACK_UPGRADE,
                new ItemStack(ModItems.MACHINE_UPGRADES.get("upgrade_stack_3").get()));

        helper.runAfterDelay(21, () -> {
            MovingConveyorItemEntity item = MovingConveyorItemEntity.create(helper.getLevel(),
                    new ItemStack(Items.IRON_INGOT, 8));
            item.setPos(grabberPos.getX() + 0.5D, grabberPos.getY() + 0.25D, grabberPos.getZ() + 1.5D);
            helper.getLevel().addFreshEntity(item);
            CraneGrabberBlockEntity.tick(helper.getLevel(), grabberPos, grabberState, grabber);
            ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos);
            check(helper, !item.isAlive() && chest.countItem(Items.IRON_INGOT) == 8,
                    "A whitelisted Grabber with the tier-three stack upgrade must insert up to 64 passing items");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 30)
    public static void grabberMovesWholeParcelOntoOutputConveyor(GameTestHelper helper) {
        BlockPos grabberPos = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos outputPos = grabberPos.north();
        BlockState grabberState = ModBlocks.CRANE_GRABBER.get().defaultBlockState()
                .setValue(AbstractCraneBlock.INPUT, Direction.SOUTH)
                .setValue(AbstractCraneBlock.OUTPUT, Direction.NORTH);
        BlockState beltState = ModBlocks.CONVEYOR.get().defaultBlockState()
                .setValue(ConveyorBlock.FACING, Direction.SOUTH);
        helper.getLevel().setBlock(outputPos, beltState, Block.UPDATE_ALL);
        helper.getLevel().setBlock(grabberPos, grabberState, Block.UPDATE_ALL);
        CraneGrabberBlockEntity grabber = (CraneGrabberBlockEntity)
                helper.getLevel().getBlockEntity(grabberPos);

        helper.runAfterDelay(21, () -> {
            MovingConveyorItemEntity item = MovingConveyorItemEntity.create(helper.getLevel(),
                    new ItemStack(Items.COPPER_INGOT, 17));
            item.setPos(grabberPos.getX() + 0.5D, grabberPos.getY() + 0.25D, grabberPos.getZ() + 1.5D);
            helper.getLevel().addFreshEntity(item);
            CraneGrabberBlockEntity.tick(helper.getLevel(), grabberPos, grabberState, grabber);
            List<MovingConveyorItemEntity> output = helper.getLevel().getEntitiesOfClass(
                    MovingConveyorItemEntity.class, new AABB(outputPos).inflate(0.5D),
                    moving -> moving.isAlive() && moving.getItemStack().is(Items.COPPER_INGOT));
            check(helper, !item.isAlive() && output.size() == 1
                            && output.getFirst().getItemStack().getCount() == 17,
                    "Belt-to-belt grabbing must move the complete parcel regardless of the stack upgrade");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 30)
    public static void grabberOnlyReachesNearestTripleConveyorLane(GameTestHelper helper) {
        BlockPos grabberPos = helper.absolutePos(new BlockPos(3, 2, 3));
        BlockPos chestPos = grabberPos.north();
        BlockState grabberState = ModBlocks.CRANE_GRABBER.get().defaultBlockState()
                .setValue(AbstractCraneBlock.INPUT, Direction.SOUTH)
                .setValue(AbstractCraneBlock.OUTPUT, Direction.NORTH);
        helper.getLevel().setBlock(grabberPos.south(), ModBlocks.CONVEYOR_TRIPLE.get().defaultBlockState(),
                Block.UPDATE_ALL);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(grabberPos, grabberState, Block.UPDATE_ALL);
        CraneGrabberBlockEntity grabber = (CraneGrabberBlockEntity)
                helper.getLevel().getBlockEntity(grabberPos);
        grabber.setItem(CraneGrabberBlockEntity.STACK_UPGRADE,
                new ItemStack(ModItems.MACHINE_UPGRADES.get("upgrade_stack_3").get()));

        helper.runAfterDelay(21, () -> {
            MovingConveyorItemEntity near = MovingConveyorItemEntity.create(helper.getLevel(),
                    new ItemStack(Items.GOLD_INGOT, 3));
            near.setPos(grabberPos.getX() + 0.5D, grabberPos.getY() + 0.25D, grabberPos.getZ() + 0.8D);
            MovingConveyorItemEntity far = MovingConveyorItemEntity.create(helper.getLevel(),
                    new ItemStack(Items.IRON_INGOT, 3));
            far.setPos(grabberPos.getX() + 0.5D, grabberPos.getY() + 0.25D, grabberPos.getZ() + 1.5D);
            helper.getLevel().addFreshEntity(near);
            helper.getLevel().addFreshEntity(far);
            CraneGrabberBlockEntity.tick(helper.getLevel(), grabberPos, grabberState, grabber);
            ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos);
            check(helper, !near.isAlive() && far.isAlive()
                            && chest.countItem(Items.GOLD_INGOT) == 3
                            && chest.countItem(Items.IRON_INGOT) == 0,
                    "The Grabber must preserve the source contracted AABB for the nearest triple-belt lane");
            helper.succeed();
        });
    }

    private static void checkRecipe(GameTestHelper helper, String id, net.minecraft.world.item.Item item, int count) {
        ItemStack result = helper.getLevel().getRecipeManager().byKey(
                        ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, id))
                .map(holder -> holder.value().getResultItem(helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        check(helper, result.is(item) && result.getCount() == count,
                id + " must produce " + count + " of its conveyor family");
    }

    private static void useScrewdriver(GameTestHelper helper, BlockPos pos,
                                       net.minecraft.world.entity.player.Player player,
                                       ItemStack screwdriver, BlockHitResult hit) {
        helper.getLevel().getBlockState(pos).useItemOn(screwdriver, helper.getLevel(), player,
                InteractionHand.MAIN_HAND, hit);
    }

    private static boolean close(double first, double second) {
        return Math.abs(first - second) < 1.0E-6D;
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
