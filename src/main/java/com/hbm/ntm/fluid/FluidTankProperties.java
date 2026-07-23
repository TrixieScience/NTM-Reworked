package com.hbm.ntm.fluid;

import com.hbm.ntm.item.FluidIdentifierItem;

/** Numbers and tiny warning pictures for liquids best kept inside the tank. */
public final class FluidTankProperties {
    private static final float SOOT_UNREFINED_OIL = 0.004F;
    private static final float SOOT_REFINED_OIL = 0.001F;
    private static final float SOOT_GAS = 0.0002F;
    private static final float POISON_OIL = 0.00005F;

    private FluidTankProperties() { }

    public static Profile get(FluidIdentifierItem.Selection fluid) {
        if (fluid == null) fluid = FluidIdentifierItem.Selection.NONE;
        return switch (fluid) {
            case NONE -> profile(0, 0, 0, Symbol.NONE, Phase.NONE, false);
            case SMOKE -> profile(1, 0, 0, Symbol.NONE, Phase.GAS, false);
            case AIR -> gas(0, 0, 0, false, false);
            case AIRBLAST -> gas(0, 3, 0, false, false);
            case WATER -> profile(0, 0, 0, Symbol.NONE, Phase.LIQUID, false);
            case STEAM -> profile(3, 0, 0, Symbol.NONE, Phase.GAS, false);
            case HOTSTEAM, SUPERHOTSTEAM, ULTRAHOTSTEAM ->
                    profile(4, 0, 0, Symbol.NONE, Phase.GAS, false);
            case COOLANT -> profile(1, 0, 0, Symbol.NONE, Phase.LIQUID, false);
            case COOLANT_HOT -> profile(1, 0, 0, Symbol.NONE, Phase.LIQUID, false);
            case LAVA -> profile(4, 0, 0, Symbol.NO_WATER, Phase.LIQUID, false);
            case PEROXIDE -> profile(3, 0, 3, Symbol.OXIDIZER, Phase.LIQUID, false);
            case SULFURIC_ACID -> profile(3, 0, 2, Symbol.ACID, Phase.LIQUID, false);
            case OIL -> oil(2, 1, true);
            case HOTOIL -> oil(2, 3, false);
            case HEAVYOIL -> oil(2, 1, true);
            case NAPHTHA -> fuel(2, 1, true);
            case LIGHTOIL -> fuel(1, 2, true);
            case BITUMEN -> oil(2, 0, false);
            case SMEAR -> oil(2, 1, true);
            case HEATINGOIL -> oil(2, 2, true);
            case WOODOIL -> oil(2, 2, false);
            case COALCREOSOTE -> oil(3, 2, true);
            case LUBRICANT -> oil(2, 1, false);
            case DIESEL -> fuel(1, 2, true);
            case KEROSENE -> fuel(1, 2, true);
            case GASOLINE, GASOLINE_LEADED, COALGAS, COALGAS_LEADED -> fuel(1, 2, true);
            case PETROLEUM -> gas(1, 4, 1, true, true);
            case GAS -> gas(1, 4, 1, true, true);
            case CARBONDIOXIDE -> gas(1, 0, 0, false, false);
            case HYDROGEN -> profile(3, 4, 0, Symbol.CRYOGENIC, Phase.LIQUID, true);
            case DEUTERIUM -> gas(3, 4, 0, true, false);
            case TRITIUM -> profile(3, 4, 0, Symbol.RADIATION, Phase.GAS, true);
            case CRYOGEL -> profile(2, 0, 0, Symbol.CRYOGENIC, Phase.LIQUID, false);
            case UNSATURATEDS -> gas(1, 4, 1, true, true);
            case SPENTSTEAM -> gas(2, 0, 0, false, false);
            case FLUE -> gas(1, 4, 1, true, true);
            case MERCURY -> profile(2, 0, 0, Symbol.NONE, Phase.LIQUID, false);
            case BLOOD -> profile(0, 0, 0, Symbol.NONE, Phase.LIQUID, false);
            case OXYGEN -> profile(3, 0, 0, Symbol.CRYOGENIC, Phase.LIQUID, false);
            case PAIN -> profile(2, 0, 1, Symbol.ACID, Phase.LIQUID, false);
            case SAS3 -> profile(5, 0, 4, Symbol.RADIATION, Phase.LIQUID, false);
        };
    }

    private static Profile oil(int health, int fire, boolean flammable) {
        return new Profile(health, fire, 0, Symbol.NONE, Phase.LIQUID, flammable,
                SOOT_UNREFINED_OIL, POISON_OIL);
    }

    private static Profile fuel(int health, int fire, boolean flammable) {
        return new Profile(health, fire, 0, Symbol.NONE, Phase.LIQUID, flammable,
                SOOT_REFINED_OIL, POISON_OIL);
    }

    private static Profile gas(int health, int fire, int reactivity, boolean flammable, boolean polluting) {
        return new Profile(health, fire, reactivity, Symbol.NONE, Phase.GAS, flammable,
                polluting ? SOOT_GAS : 0F, polluting ? POISON_OIL : 0F);
    }

    private static Profile profile(int health, int fire, int reactivity, Symbol symbol,
                                   Phase phase, boolean flammable) {
        return new Profile(health, fire, reactivity, symbol, phase, flammable, 0F, 0F);
    }

    public record Profile(int health, int flammability, int reactivity, Symbol symbol,
                          Phase phase, boolean flammable, float burnSootPerMb,
                          float spillPoisonPerMb) {
        public boolean gaseous() { return phase == Phase.GAS; }
        public boolean liquid() { return phase == Phase.LIQUID; }
    }

    public enum Phase { NONE, LIQUID, GAS }

    /** Where each tiny omen lives in the warning atlas. */
    public enum Symbol {
        NONE(0, 0),
        RADIATION(195, 2),
        NO_WATER(195, 63),
        ACID(195, 124),
        ASPHYXIANT(195, 185),
        CRYOGENIC(134, 185),
        ANTIMATTER(73, 185),
        OXIDIZER(12, 185);

        private final int x;
        private final int y;

        Symbol(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x() { return x; }
        public int y() { return y; }
    }
}
