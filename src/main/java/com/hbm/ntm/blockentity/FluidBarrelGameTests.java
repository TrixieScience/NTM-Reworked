package com.hbm.ntm.blockentity;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FluidBarrelGameTests {
    private FluidBarrelGameTests() {
    }

    @GameTest(template = "empty")
    public static void barrelCapacitiesAndCorrodedQuirkMatchSource(GameTestHelper helper) {
        checkCapacity(helper, new BlockPos(1, 1, 1), ModBlocks.BARREL_PLASTIC.get(), 12_000);
        checkCapacity(helper, new BlockPos(2, 1, 1), ModBlocks.BARREL_STEEL.get(), 16_000);
        checkCapacity(helper, new BlockPos(3, 1, 1), ModBlocks.BARREL_TCALLOY.get(), 24_000);
        checkCapacity(helper, new BlockPos(4, 1, 1), ModBlocks.BARREL_ANTIMATTER.get(), 16_000);

        BlockPos corroded = helper.absolutePos(new BlockPos(5, 1, 1));
        helper.getLevel().setBlock(corroded, ModBlocks.BARREL_CORRODED.get().defaultBlockState(), Block.UPDATE_ALL);
        check(helper, helper.getLevel().getBlockEntity(corroded) == null,
                "The source Corroded Barrel must remain a static worldgen prop");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void barrelModesComparatorAndStoredDropWork(GameTestHelper helper) {
        BlockPos first = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlock(first, ModBlocks.BARREL_STEEL.get().defaultBlockState(), Block.UPDATE_ALL);
        FluidBarrelBlockEntity barrel = (FluidBarrelBlockEntity) helper.getLevel().getBlockEntity(first);
        check(helper, barrel != null, "Steel Barrel must create its storage block entity");

        barrel.selectFluid(FluidIdentifierItem.Selection.WATER);
        int filled = barrel.fluidHandler().fill(new FluidStack(
                FluidIdentifierItem.Selection.WATER.fluid(), 8_000), IFluidHandler.FluidAction.EXECUTE);
        check(helper, filled == 8_000 && barrel.comparatorSignal() == 8,
                "A half-full Steel Barrel must accept 8,000 mB and emit comparator level 8");

        barrel.dataAccess().set(2, FluidBarrelBlockEntity.MODE_LOCKED);
        check(helper, barrel.fluidHandler().fill(new FluidStack(
                        FluidIdentifierItem.Selection.WATER.fluid(), 1_000),
                IFluidHandler.FluidAction.EXECUTE) == 0
                        && barrel.fluidHandler().drain(1_000, IFluidHandler.FluidAction.EXECUTE).isEmpty(),
                "Locked barrels must reject both fluid insertion and extraction");

        ItemStack stored = barrel.machineDrop();
        BlockPos second = helper.absolutePos(new BlockPos(3, 1, 2));
        helper.getLevel().setBlock(second, ModBlocks.BARREL_STEEL.get().defaultBlockState(), Block.UPDATE_ALL);
        FluidBarrelBlockEntity restored = (FluidBarrelBlockEntity) helper.getLevel().getBlockEntity(second);
        restored.restoreFromItem(stored);
        check(helper, restored.selection() == FluidIdentifierItem.Selection.WATER
                        && restored.tank().getFluidAmount() == 8_000
                        && restored.mode() == FluidBarrelBlockEntity.MODE_LOCKED,
                "Breaking and replacing a barrel must retain its fluid and transfer mode");
        helper.succeed();
    }

    private static void checkCapacity(GameTestHelper helper, BlockPos relative, Block block, int expected) {
        BlockPos position = helper.absolutePos(relative);
        helper.getLevel().setBlock(position, block.defaultBlockState(), Block.UPDATE_ALL);
        FluidBarrelBlockEntity barrel = (FluidBarrelBlockEntity) helper.getLevel().getBlockEntity(position);
        check(helper, barrel != null && barrel.capacity() == expected,
                block.getDescriptionId() + " must hold exactly " + expected + " mB");
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
