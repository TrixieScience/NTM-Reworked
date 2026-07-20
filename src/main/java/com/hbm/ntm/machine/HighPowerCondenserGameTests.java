package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HighPowerCondenserBlock;
import com.hbm.ntm.blockentity.HighPowerCondenserBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.List;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HighPowerCondenserGameTests {
    private HighPowerCondenserGameTests() { }

    @GameTest(template = "empty")
    public static void sourceGeometryHasSixtyThreePartsAndSixOutwardPorts(GameTestHelper helper) {
        BlockPos core = new BlockPos(4, 2, 4);
        HighPowerCondenserBlock block = ModBlocks.MACHINE_CONDENSER_POWERED.get();
        List<BlockPos> parts = HighPowerCondenserBlock.partPositions(core, Direction.NORTH);
        List<HighPowerCondenserBlock.Connection> connections =
                HighPowerCondenserBlock.connections(core, Direction.NORTH);

        check(helper, parts.size() == 63 && new HashSet<>(parts).size() == 63,
                "Source dimensions must remain a full 3x7x3 shell");
        check(helper, connections.size() == 6
                        && connections.stream().map(HighPowerCondenserBlock.Connection::port).distinct().count() == 6,
                "Powered Condenser must have exactly six unique combination ports");

        for (BlockPos part : parts) {
            BlockState state = block.stateForPart(part, core, Direction.NORTH);
            boolean expectedPort = connections.stream().anyMatch(connection -> connection.port().equals(part));
            check(helper, HighPowerCondenserBlock.isPort(state) == expectedPort,
                    "Only the six source extra cells may become proxies");
            if (expectedPort) {
                Direction expected = connections.stream().filter(connection -> connection.port().equals(part))
                        .findFirst().orElseThrow().outward();
                check(helper, HighPowerCondenserBlock.outwardDirection(state) == expected,
                        "Every proxy must point through the outer wall");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void conversionKeepsOriginalAllOrNothingPowerCheck(GameTestHelper helper) {
        BlockPos position = new BlockPos(4, 2, 4);
        HighPowerCondenserBlock block = ModBlocks.MACHINE_CONDENSER_POWERED.get();
        helper.setBlock(position, block.stateForPart(position, position, Direction.NORTH));
        HighPowerCondenserBlockEntity condenser = helper.getBlockEntity(position);
        check(helper, condenser.fluidHandler().fill(new FluidStack(ModFluids.SPENTSTEAM.get(), 100),
                IFluidHandler.FluidAction.EXECUTE) == 100, "Input port must accept 100mB LPS");

        condenser.setPower(999L);
        HighPowerCondenserBlockEntity.tick(helper.getLevel(), helper.absolutePos(position),
                helper.getBlockState(position), condenser);
        check(helper, condenser.spentSteamTank().getFluidAmount() == 100
                        && condenser.waterTank().isEmpty() && condenser.getPower() == 999L,
                "999HE must not partially process a 1,000HE batch");

        condenser.setPower(1_000L);
        HighPowerCondenserBlockEntity.tick(helper.getLevel(), helper.absolutePos(position),
                helper.getBlockState(position), condenser);
        check(helper, condenser.spentSteamTank().isEmpty()
                        && condenser.waterTank().getFluidAmount() == 100 && condenser.getPower() == 0L,
                "1,000HE must convert 100mB LPS into exactly 100mB Water");
        check(helper, condenser.throughput() == 100 && condenser.waterTimer() == 20,
                "Successful conversion must start the source fan and steam timer");
        helper.succeed();
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
