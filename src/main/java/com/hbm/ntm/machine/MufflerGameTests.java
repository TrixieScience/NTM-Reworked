package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.ThermalMultiblockBlock;
import com.hbm.ntm.blockentity.FireboxBlockEntity;
import com.hbm.ntm.blockentity.MachineShredderBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MufflerGameTests {
    private MufflerGameTests() {
    }

    @GameTest(template = "empty")
    public static void shredderConsumesOneMufflerAndKeepsItInstalled(GameTestHelper helper) {
        BlockPos position = new BlockPos(2, 1, 2);
        helper.setBlock(position, ModBlocks.MACHINE_SHREDDER.get());
        MachineShredderBlockEntity shredder = helper.getBlockEntity(position);
        ItemStack mufflers = new ItemStack(ModItems.UPGRADE_MUFFLER.get(), 2);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, mufflers);

        ModItems.UPGRADE_MUFFLER.get().useOn(context(helper, position, player));
        check(helper, shredder.isMuffled(), "The Muffler should install on the Shredder");
        check(helper, mufflers.getCount() == 1, "Installing a Muffler should consume exactly one item");

        ModItems.UPGRADE_MUFFLER.get().useOn(context(helper, position, player));
        check(helper, mufflers.getCount() == 1, "An already muffled machine should not eat another Muffler");

        var tag = shredder.saveWithoutMetadata(helper.getLevel().registryAccess());
        MachineShredderBlockEntity loaded = new MachineShredderBlockEntity(BlockPos.ZERO,
                ModBlocks.MACHINE_SHREDDER.get().defaultBlockState());
        loaded.loadWithComponents(tag, helper.getLevel().registryAccess());
        check(helper, loaded.isMuffled(), "The Shredder should remember its Muffler after loading");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void thermalProxyInstallsMufflerOnFireboxCore(GameTestHelper helper) {
        BlockPos corePosition = new BlockPos(2, 1, 2);
        BlockPos proxyPosition = corePosition.east();
        BlockState coreState = thermalState(1);
        BlockState proxyState = thermalState(0);
        helper.setBlock(corePosition, coreState);
        helper.setBlock(proxyPosition, proxyState);
        FireboxBlockEntity firebox = helper.getBlockEntity(corePosition);
        ItemStack muffler = new ItemStack(ModItems.UPGRADE_MUFFLER.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, muffler);

        ModItems.UPGRADE_MUFFLER.get().useOn(context(helper, proxyPosition, player));
        check(helper, firebox.isMuffled(), "Using a Muffler on a Firebox proxy should reach its core");
        check(helper, muffler.isEmpty(), "Installing the Firebox Muffler should consume the item");

        var tag = firebox.saveWithoutMetadata(helper.getLevel().registryAccess());
        FireboxBlockEntity loaded = new FireboxBlockEntity(BlockPos.ZERO, coreState);
        loaded.loadWithComponents(tag, helper.getLevel().registryAccess());
        check(helper, loaded.isMuffled(), "The Firebox should remember its Muffler after loading");
        helper.succeed();
    }

    private static BlockState thermalState(int coreX) {
        return ModBlocks.HEATER_FIREBOX.get().defaultBlockState()
                .setValue(ThermalMultiblockBlock.FACING, Direction.SOUTH)
                .setValue(ThermalMultiblockBlock.CORE_X, coreX)
                .setValue(ThermalMultiblockBlock.CORE_Y, 1)
                .setValue(ThermalMultiblockBlock.CORE_Z, 1);
    }

    private static UseOnContext context(GameTestHelper helper, BlockPos relativePosition,
                                        net.minecraft.world.entity.player.Player player) {
        BlockPos absolute = helper.absolutePos(relativePosition);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false));
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
