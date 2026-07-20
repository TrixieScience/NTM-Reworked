package com.hbm.ntm.recipe;

import com.hbm.ntm.config.HbmConfig;
import com.hbm.ntm.item.FluidIdentifierItem;
import net.minecraft.world.level.material.Fluid;

/** Source fuel-grade table and integer-first HE conversion used by the Diesel Generator. */
public final class DieselGeneratorFuels {
    public enum Grade {
        LOW("Low"), MEDIUM("Medium"), HIGH("High"), AERO("Aviation"), GAS("Gaseous"), NONE("None");

        private final String displayName;
        Grade(String displayName) { this.displayName = displayName; }
        public String displayName() { return displayName; }
    }

    private DieselGeneratorFuels() { }

    public static Fuel fuel(Fluid fluid) {
        for (FluidIdentifierItem.Selection selection : FluidIdentifierItem.Selection.values()) {
            if (selection != FluidIdentifierItem.Selection.NONE && selection.accepts(fluid)) return fuel(selection);
        }
        return Fuel.NONE;
    }

    public static Fuel fuel(FluidIdentifierItem.Selection selection) {
        return switch (selection) {
            case NAPHTHA -> new Fuel(Grade.MEDIUM, 165_000L, true);
            case LIGHTOIL -> new Fuel(Grade.MEDIUM, 2_200_000L, true);
            case DIESEL -> new Fuel(Grade.HIGH, 1_370_000L, true);
            case KEROSENE -> new Fuel(Grade.AERO, 3_850_000L, true);
            case HYDROGEN -> new Fuel(Grade.HIGH, 10_000L, false);
            case HEAVYOIL, SMEAR, HEATINGOIL -> new Fuel(Grade.LOW, 0L, true);
            case GAS, PETROLEUM -> new Fuel(Grade.GAS, 0L, true);
            default -> Fuel.NONE;
        };
    }

    public static long energyPerMb(FluidIdentifierItem.Selection selection) {
        Fuel fuel = fuel(selection);
        if (!fuel.accepted()) return 0L;
        // Divide the bucket value before applying configurable efficiency. Order matters.
        return (long) (fuel.combustionEnergyPerBucket() / 1_000L * efficiency(fuel.grade()));
    }

    public static boolean accepted(FluidIdentifierItem.Selection selection) {
        return energyPerMb(selection) > 0L;
    }

    private static double efficiency(Grade grade) {
        return switch (grade) {
            case MEDIUM -> HbmConfig.DIESEL_EFFICIENCY_MEDIUM.get();
            case HIGH -> HbmConfig.DIESEL_EFFICIENCY_HIGH.get();
            case AERO -> HbmConfig.DIESEL_EFFICIENCY_AERO.get();
            default -> 0D;
        };
    }

    public record Fuel(Grade grade, long combustionEnergyPerBucket, boolean polluting) {
        private static final Fuel NONE = new Fuel(Grade.NONE, 0L, false);
        public boolean accepted() {
            return grade == Grade.MEDIUM || grade == Grade.HIGH || grade == Grade.AERO;
        }
    }
}
