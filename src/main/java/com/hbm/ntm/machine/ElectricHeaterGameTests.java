package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.anvil.AnvilRecipes;
import com.hbm.ntm.block.ElectricHeaterBlock;
import com.hbm.ntm.blockentity.ElectricHeaterBlockEntity;
import com.hbm.ntm.blockentity.ElectricHeaterProxyBlockEntity;
import com.hbm.ntm.blockentity.FireboxBlockEntity;
import com.hbm.ntm.item.CircuitItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ElectricHeaterGameTests {
    private ElectricHeaterGameTests() { }

    @GameTest(template = "empty")
    public static void placementBuildsExactSourceVolumeAndFrontPowerProxy(GameTestHelper helper) {
        BlockPos clicked = new BlockPos(4, 1, 3);
        Direction facing = Direction.SOUTH;
        ElectricHeaterBlock block = ModBlocks.HEATER_ELECTRIC.get();
        BlockState clickedState = block.defaultBlockState()
                .setValue(ElectricHeaterBlock.FACING, facing)
                .setValue(ElectricHeaterBlock.LATERAL, 1)
                .setValue(ElectricHeaterBlock.DEPTH, 3);
        helper.setBlock(clicked, clickedState);
        block.setPlacedBy(helper.getLevel(), helper.absolutePos(clicked), clickedState,
                helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(ModItems.HEATER_ELECTRIC_ITEM.get()));

        BlockPos core = clicked.relative(facing.getOpposite(), 2);
        int cells = 0;
        int cores = 0;
        int proxies = 0;
        for (BlockPos part : ElectricHeaterBlock.partPositions(core, facing)) {
            BlockState state = helper.getBlockState(part);
            check(helper, state.is(block), "Every source 3x1x4 Electric Heater cell must be present");
            check(helper, ElectricHeaterBlock.corePosition(helper.absolutePos(part), state)
                            .equals(helper.absolutePos(core)),
                    "Every part state must resolve to the same core");
            cells++;
            if (helper.getLevel().getBlockEntity(helper.absolutePos(part))
                    instanceof ElectricHeaterBlockEntity) cores++;
            if (helper.getLevel().getBlockEntity(helper.absolutePos(part))
                    instanceof ElectricHeaterProxyBlockEntity) proxies++;
        }
        check(helper, cells == 12 && cores == 1 && proxies == 1,
                "The Heater must have twelve cells, one core and one source power proxy");
        check(helper, helper.getLevel().getBlockEntity(helper.absolutePos(clicked))
                        instanceof ElectricHeaterProxyBlockEntity,
                "The originally placed cell two blocks ahead of the core must be the HE proxy");

        helper.destroyBlock(core.relative(facing.getOpposite()).relative(facing.getClockWise()));
        for (BlockPos part : ElectricHeaterBlock.partPositions(core, facing)) {
            check(helper, !helper.getBlockState(part).is(block),
                    "Breaking any Heater part must dismantle the whole structure");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allFacingsPreserveAsymmetricOneBackTwoForwardTopology(GameTestHelper helper) {
        ElectricHeaterBlock block = ModBlocks.HEATER_ELECTRIC.get();
        BlockPos core = new BlockPos(4, 1, 4);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (BlockPos part : ElectricHeaterBlock.partPositions(core, facing)) {
                BlockState state = block.stateForPart(part, core, facing);
                check(helper, ElectricHeaterBlock.corePosition(part, state).equals(core),
                        "Rotated Heater parts must preserve exact core mapping");
            }
            BlockState back = block.stateForPart(core.relative(facing.getOpposite()), core, facing);
            BlockState front = block.stateForPart(core.relative(facing, 2), core, facing);
            check(helper, back.getValue(ElectricHeaterBlock.DEPTH) == 0
                            && front.getValue(ElectricHeaterBlock.DEPTH) == 3
                            && ElectricHeaterBlock.isPowerPort(front),
                    "Each rotation must retain one rear row and two forward rows");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void settingsUseExactSourcePowerCurveAndWrap(GameTestHelper helper) {
        ElectricHeaterBlockEntity heater = bareHeater(helper, new BlockPos(2, 1, 2));
        long[] expected = {0, 200, 527, 931, 1392, 1903, 2457, 3049, 3675, 4334, 5023};
        check(helper, heater.setting() == 0 && heater.getConsumption() == 0 && heater.getMaxPower() == 0,
                "The Electric Heater must start disabled with a zero-sized dynamic buffer");
        for (int setting = 1; setting <= 10; setting++) {
            heater.toggleSetting();
            check(helper, heater.setting() == setting && heater.getConsumption() == expected[setting]
                            && heater.getMaxPower() == expected[setting] * 20L
                            && heater.getHeatGen() == setting * 100,
                    "Every screwdriver setting must use floor(pow(setting, 1.4) * 200)");
        }
        heater.toggleSetting();
        check(helper, heater.setting() == 0 && heater.getConsumption() == 0,
                "The eleventh screwdriver use must wrap the Heater back to Off");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void activeTickPullsBottomHeatAtEightyFivePercentAndConsumesPower(GameTestHelper helper) {
        BlockPos position = new BlockPos(3, 2, 3);
        ElectricHeaterBlockEntity heater = bareHeater(helper, position);
        FireboxBlockEntity source = bareFirebox(helper, position.below());
        loadHeater(helper, heater, 2, 1_054L, 1_000);
        setFireboxHeat(helper, source, 100);

        tick(helper, heater);
        check(helper, source.getHeatStored() == 0,
                "The source Heater must consume all heat below even though only 85% is recovered");
        check(helper, heater.heatEnergy() == 1_284,
                "Cooling 1000 to 999, recovering 85 TU and producing 200 TU must yield 1284 TU");
        check(helper, heater.getPower() == 527L && heater.active(),
                "Setting 2 must consume exactly 527 HE and set the active flag");

        loadHeater(helper, heater, 2, 526L, 1_000);
        tick(helper, heater);
        check(helper, heater.heatEnergy() == 999 && heater.getPower() == 526L && !heater.active(),
                "Insufficient HE must still cool the buffer without producing heat or consuming power");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void frontProxyDelegatesDynamicReceiverAndStatePersists(GameTestHelper helper) {
        BlockPos clicked = new BlockPos(4, 1, 3);
        ElectricHeaterBlockEntity heater = placeHeater(helper, clicked, Direction.SOUTH);
        heater.toggleSetting();
        ElectricHeaterProxyBlockEntity proxy = (ElectricHeaterProxyBlockEntity)
                helper.getLevel().getBlockEntity(helper.absolutePos(clicked));
        check(helper, proxy != null && proxy.getMaxPower() == 4_000L && proxy.transferPower(400L) == 0L
                        && heater.getPower() == 400L,
                "The front ProxyCombo cell must delegate direct HE reception to the core");

        CompoundTag tag = heater.saveWithoutMetadata(helper.getLevel().registryAccess());
        ElectricHeaterBlockEntity loaded = bareHeater(helper, new BlockPos(7, 1, 7));
        loaded.loadWithComponents(tag, helper.getLevel().registryAccess());
        check(helper, loaded.setting() == 1 && loaded.getPower() == 400L,
                "Power and screwdriver setting must persist with the source NBT keys");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tierThreeConstructionAndDeshAnvilUpgradeMatchSource(GameTestHelper helper) {
        AnvilRecipes.Construction recipe = AnvilRecipes.byId(ResourceLocation.fromNamespaceAndPath(
                HbmNtm.MOD_ID, "anvil/heater_electric"));
        check(helper, recipe != null && recipe.tierLower() == 3 && !recipe.validForTier(2)
                        && recipe.validForTier(3) && recipe.inputs().size() == 5,
                "The Electric Heater must remain a five-input Tier 3 construction recipe");
        check(helper, recipe.inputs().get(0).count() == 4 && recipe.inputs().get(1).count() == 8
                        && recipe.inputs().get(2).count() == 8 && recipe.inputs().get(3).count() == 8,
                "Plastic, copper, steel plate and heating-coil counts must match source");
        check(helper, recipe.inputs().get(0).matches(new ItemStack(ModItems.get("ingot_polymer").get()))
                        && !recipe.inputs().get(0).matches(new ItemStack(ModItems.PLATE_POLYMER.get())),
                "ANY_PLASTIC must use the active Polymer Bar identity, not the old Insulator stand-in");
        ItemStack basic = CircuitItem.create(ModItems.CIRCUIT.get(), CircuitItem.CircuitType.BASIC, 1);
        check(helper, recipe.inputs().get(4).matches(basic)
                        && recipe.icon().is(ModItems.HEATER_ELECTRIC_ITEM.get()),
                "The fifth input must be one exact Basic Circuit and output the Heater");

        ItemStack desh = new ItemStack(ModItems.get("ingot_desh").get(), 10);
        AnvilRecipes.Smithing upgrade = AnvilRecipes.findSmithing(
                new ItemStack(ModItems.ANVIL_IRON_ITEM.get()), desh, 1);
        check(helper, ModBlocks.ANVIL_DESH.get().tier() == 3 && upgrade != null
                        && upgrade.output().get().is(ModItems.ANVIL_DESH_ITEM.get()),
                "Ten Desh ingots on a base anvil must unlock the source Tier 3 Desh Anvil");
        helper.succeed();
    }

    private static ElectricHeaterBlockEntity placeHeater(GameTestHelper helper, BlockPos clicked,
                                                          Direction facing) {
        ElectricHeaterBlock block = ModBlocks.HEATER_ELECTRIC.get();
        BlockState state = block.defaultBlockState().setValue(ElectricHeaterBlock.FACING, facing)
                .setValue(ElectricHeaterBlock.LATERAL, 1).setValue(ElectricHeaterBlock.DEPTH, 3);
        helper.setBlock(clicked, state);
        block.setPlacedBy(helper.getLevel(), helper.absolutePos(clicked), state,
                helper.makeMockPlayer(GameType.SURVIVAL), new ItemStack(ModItems.HEATER_ELECTRIC_ITEM.get()));
        BlockPos core = clicked.relative(facing.getOpposite(), 2);
        return (ElectricHeaterBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(core));
    }

    private static ElectricHeaterBlockEntity bareHeater(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, ModBlocks.HEATER_ELECTRIC.get().defaultBlockState());
        return helper.getBlockEntity(position);
    }

    private static FireboxBlockEntity bareFirebox(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, ModBlocks.HEATER_FIREBOX.get().defaultBlockState());
        return helper.getBlockEntity(position);
    }

    private static void loadHeater(GameTestHelper helper, ElectricHeaterBlockEntity heater,
                                   int setting, long power, int heat) {
        CompoundTag tag = heater.saveWithoutMetadata(helper.getLevel().registryAccess());
        tag.putInt("setting", setting);
        tag.putLong("power", power);
        tag.putInt("heatEnergy", heat);
        heater.loadWithComponents(tag, helper.getLevel().registryAccess());
    }

    private static void setFireboxHeat(GameTestHelper helper, FireboxBlockEntity firebox, int heat) {
        CompoundTag tag = firebox.saveWithoutMetadata(helper.getLevel().registryAccess());
        tag.putInt("heatEnergy", heat);
        firebox.loadWithComponents(tag, helper.getLevel().registryAccess());
    }

    private static void tick(GameTestHelper helper, ElectricHeaterBlockEntity heater) {
        ElectricHeaterBlockEntity.tick(helper.getLevel(), heater.getBlockPos(), heater.getBlockState(), heater);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
