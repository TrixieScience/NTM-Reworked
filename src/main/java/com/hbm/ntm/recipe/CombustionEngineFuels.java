package com.hbm.ntm.recipe;

import com.hbm.ntm.item.FluidIdentifierItem;

/** Source FT_Combustible grades and bucket energies used by the industrial engine. */
public final class CombustionEngineFuels {
    private CombustionEngineFuels() { }

    public static Fuel fuel(FluidIdentifierItem.Selection selection) {
        return switch (selection) {
            case HEAVYOIL -> new Fuel(DieselGeneratorFuels.Grade.LOW, 25_000L, true);
            case HEATINGOIL -> new Fuel(DieselGeneratorFuels.Grade.LOW, 100_000L, true);
            case NAPHTHA -> new Fuel(DieselGeneratorFuels.Grade.MEDIUM, 200_000L, true);
            case LIGHTOIL -> new Fuel(DieselGeneratorFuels.Grade.MEDIUM, 500_000L, true);
            case DIESEL -> new Fuel(DieselGeneratorFuels.Grade.HIGH, 500_000L, true);
            case KEROSENE -> new Fuel(DieselGeneratorFuels.Grade.AERO, 1_250_000L, true);
            case HYDROGEN, DEUTERIUM, TRITIUM ->
                    new Fuel(DieselGeneratorFuels.Grade.HIGH, 10_000L, false);
            default -> Fuel.NONE;
        };
    }

    public record Fuel(DieselGeneratorFuels.Grade grade, long combustionEnergyPerBucket,
                       boolean polluting) {
        private static final Fuel NONE = new Fuel(DieselGeneratorFuels.Grade.NONE, 0L, false);
        public boolean accepted() { return combustionEnergyPerBucket > 0L; }
    }
}
