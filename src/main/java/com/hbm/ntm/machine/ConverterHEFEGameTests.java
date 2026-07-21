package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ConverterHEtoFEBlockEntity;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConverterHEFEGameTests {
    private ConverterHEFEGameTests() { }

    @GameTest(template = "empty")
    public static void checkConversionSingleUnit(GameTestHelper helper) {
        ConverterHEtoFEBlockEntity converter = bareConverter(helper, new BlockPos(3, 1, 3));

        converter.setPower(5);
        tick(helper, converter);
        check(helper, converter.getPower() == 0 && converter.getEnergy().getEnergyStored() == 1,
                "Converter must convert 5 HE to 1 FE and have 0 HE left");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void checkConversionFullEnergy(GameTestHelper helper) {
        ConverterHEtoFEBlockEntity converter = bareConverter(helper, new BlockPos(3, 1, 3));

        converter.setPower(100);
        converter.getEnergy().receiveEnergy(1_000_000, false);
        tick(helper, converter);
        check(helper, converter.getPower() == 100 && converter.getEnergy().getEnergyStored() == 1_000_000,
                "Converter must not convert HE when the FE buffer is full");

        converter.getEnergy().extractEnergy(10, false);
        tick(helper, converter);
        check(helper, converter.getPower() == 50 && converter.getEnergy().getEnergyStored() == 1_000_000,
                "Converter must convert 50 HE to remaining 10 FE");

        converter.getEnergy().extractEnergy(20, false);
        tick(helper, converter);
        check(helper, converter.getPower() == 0 && converter.getEnergy().getEnergyStored() == 1_000_000-10,
                "Converter must convert 50 HE to remaining 20 FE and other 0 HE can't be converted into remaining 20 FE");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void checkUnevenConversion(GameTestHelper helper) {
        ConverterHEtoFEBlockEntity converter = bareConverter(helper, new BlockPos(3, 1, 3));

        converter.setPower(7);
        tick(helper, converter);
        check(helper, converter.getPower() == 2 && converter.getEnergy().getEnergyStored() == 1,
                "Converter must convert 5 HE to 1 FE and have 2 HE left");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void checkEnergyFlow(GameTestHelper helper) {
        ConverterHEtoFEBlockEntity converter = bareConverter(helper, new BlockPos(3, 1, 3));

        for (Direction direction : Direction.values()) {
            IEnergyStorage energyStorage = helper.getLevel().getCapability(
                    net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                    converter.getBlockPos(),
                    direction
            );

            if (energyStorage != null && energyStorage.canReceive()) {
                energyStorage.receiveEnergy(1, false);
            }
        }
        tick(helper, converter);
        check(helper, converter.getEnergy().getEnergyStored() == 0,
                "External sources aren't supposed to extract into energy");

        helper.succeed();
    }

    private static ConverterHEtoFEBlockEntity bareConverter(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, ModBlocks.MACHINE_CONVERTER_HE_FE.get().defaultBlockState());
        return helper.getBlockEntity(position);
    }

    private static void tick(GameTestHelper helper, ConverterHEtoFEBlockEntity converter) {
        ConverterHEtoFEBlockEntity.tick(helper.getLevel(), converter.getBlockPos(), converter.getBlockState(), converter);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}