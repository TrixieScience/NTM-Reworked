package com.hbm.ntm.machine;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.ConverterFEtoHEBlockEntity;
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
public final class ConverterFEHEGameTests {
    private ConverterFEHEGameTests() { }

    @GameTest(template = "empty")
    public static void checkConversionSingleUnit(GameTestHelper helper) {
        ConverterFEtoHEBlockEntity converter = bareConverter(helper, new BlockPos(3, 1, 3));

        converter.getEnergy().receiveEnergy(2, false);
        tick(helper, converter);
        check(helper, converter.getPower() == 5 && converter.getEnergy().getEnergyStored() == 0,
                "Converter must convert 2 FE to 5 HE and have 0 FE left");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void checkConversionFullEnergy(GameTestHelper helper) {
        ConverterFEtoHEBlockEntity converter = bareConverter(helper, new BlockPos(3, 1, 3));

        converter.setPower(5_000_000);
        converter.getEnergy().receiveEnergy(40, false);
        tick(helper, converter);
        check(helper, converter.getPower() == 5_000_000 && converter.getEnergy().getEnergyStored() == 40,
                "Converter must not convert FE when the HE buffer is full");

        converter.setPower(5_000_000-50);
        tick(helper, converter);
        check(helper, converter.getPower() == 5_000_000 && converter.getEnergy().getEnergyStored() == 20,
                "Converter must convert 20 FE to remaining 50 HE");

        converter.setPower(5_000_000-100);
        tick(helper, converter);
        check(helper, converter.getPower() == 5_000_000-50 && converter.getEnergy().getEnergyStored() == 0,
                "Converter must convert 20 FE to remaining 50 HE and other 0 FE can't be converted into remaining 50 HE");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void checkUnevenConversion(GameTestHelper helper) {
        ConverterFEtoHEBlockEntity converter = bareConverter(helper, new BlockPos(3, 1, 3));

        converter.getEnergy().receiveEnergy(3, false);
        tick(helper, converter);
        check(helper, converter.getPower() == 5 && converter.getEnergy().getEnergyStored() == 1,
                "Converter must convert 2 FE to 5 HE and have 1 HE left");

        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void checkEnergyFlow(GameTestHelper helper) {
        ConverterFEtoHEBlockEntity converter = bareConverter(helper, new BlockPos(3, 1, 3));

        converter.setPower(5_000_000);
        converter.getEnergy().receiveEnergy(6, false);
        for (Direction direction : Direction.values()) {
            IEnergyStorage energyStorage = helper.getLevel().getCapability(
                    net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                    converter.getBlockPos(),
                    direction
            );

            if (energyStorage != null && energyStorage.canExtract()) {
                energyStorage.extractEnergy(1, false);
            }
        }
        tick(helper, converter);
        check(helper, converter.getEnergy().getEnergyStored() == 6,
                "External sources aren't supposed to receive energy");

        helper.succeed();
    }

    private static ConverterFEtoHEBlockEntity bareConverter(GameTestHelper helper, BlockPos position) {
        helper.setBlock(position, ModBlocks.MACHINE_CONVERTER_FE_HE.get().defaultBlockState());
        return helper.getBlockEntity(position);
    }

    private static void tick(GameTestHelper helper, ConverterFEtoHEBlockEntity converter) {
        ConverterFEtoHEBlockEntity.tick(helper.getLevel(), converter.getBlockPos(), converter.getBlockState(), converter);
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) helper.fail(message);
    }
}
