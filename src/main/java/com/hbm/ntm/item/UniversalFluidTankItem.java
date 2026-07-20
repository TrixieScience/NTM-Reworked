package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModFluids;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.function.Supplier;

/** Universal tank whose fluid identity lives in components. */
public final class UniversalFluidTankItem extends Item {
    private static final String FLUID = "fluid";

    public UniversalFluidTankItem() {
        super(new Properties());
    }

    public static ItemStack create(Item item, ContainedFluid fluid, int count) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = new CompoundTag();
        tag.putString(FLUID, fluid.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static ContainedFluid fluid(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(FLUID) ? ContainedFluid.byId(tag.getString(FLUID)) : ContainedFluid.NONE;
    }

    @Override public String getDescriptionId(ItemStack stack) {
        return "item.hbm.fluid_tank_full";
    }

    @Override public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack), Component.translatable(fluid(stack).translationKey()));
    }

    @Override public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return new ItemStack(com.hbm.ntm.registry.ModItems.FLUID_TANK_EMPTY.get());
    }

    @Override public boolean hasCraftingRemainingItem(ItemStack stack) { return true; }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                                          List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("1,000 mB"));
    }

    public enum ContainedFluid {
        NONE("none", "hbmfluid.none", 0xFFFFFF, () -> Fluids.EMPTY),
        WATER("water", "block.minecraft.water", 0x3333FF, () -> Fluids.WATER),
        STEAM("steam", "hbmfluid.steam", 0xE5E5E5, () -> ModFluids.STEAM.get()),
        HOTSTEAM("hotsteam", "hbmfluid.hotsteam", 0xE7D6D6, () -> ModFluids.HOTSTEAM.get()),
        SUPERHOTSTEAM("superhotsteam", "hbmfluid.superhotsteam", 0xE7B7B7,
                () -> ModFluids.SUPERHOTSTEAM.get()),
        ULTRAHOTSTEAM("ultrahotsteam", "hbmfluid.ultrahotsteam", 0xE39393,
                () -> ModFluids.ULTRAHOTSTEAM.get()),
        COOLANT("coolant", "hbmfluid.coolant", 0xD8FCFF, () -> ModFluids.COOLANT.get()),
        COOLANT_HOT("coolant_hot", "hbmfluid.coolant_hot", 0x99525E, () -> ModFluids.COOLANT_HOT.get()),
        LAVA("lava", "block.minecraft.lava", 0xFF3300, () -> Fluids.LAVA),
        PEROXIDE("peroxide", "hbmfluid.peroxide", 0xFFF7AA, () -> ModFluids.PEROXIDE.get()),
        SULFURIC_ACID("sulfuric_acid", "hbmfluid.sulfuric_acid", 0xB0AA64,
                () -> ModFluids.SULFURIC_ACID.get()),
        OIL("oil", "hbmfluid.oil", 0x020202, () -> ModFluids.OIL.get()),
        HOTOIL("hotoil", "hbmfluid.hotoil", 0x300900, () -> ModFluids.HOTOIL.get()),
        HEAVYOIL("heavyoil", "hbmfluid.heavyoil", 0x141312, () -> ModFluids.HEAVYOIL.get()),
        NAPHTHA("naphtha", "hbmfluid.naphtha", 0x595744, () -> ModFluids.NAPHTHA.get()),
        LIGHTOIL("lightoil", "hbmfluid.lightoil", 0x8C7451, () -> ModFluids.LIGHTOIL.get()),
        BITUMEN("bitumen", "hbmfluid.bitumen", 0x1F2426, () -> ModFluids.BITUMEN.get()),
        SMEAR("smear", "hbmfluid.smear", 0x190F01, () -> ModFluids.SMEAR.get()),
        HEATINGOIL("heatingoil", "hbmfluid.heatingoil", 0x211806, () -> ModFluids.HEATINGOIL.get()),
        WOODOIL("woodoil", "hbmfluid.woodoil", 0x847D54, () -> ModFluids.WOODOIL.get()),
        COALCREOSOTE("coalcreosote", "hbmfluid.coalcreosote", 0x51694F,
                () -> ModFluids.COALCREOSOTE.get()),
        LUBRICANT("lubricant", "hbmfluid.lubricant", 0x606060, () -> ModFluids.LUBRICANT.get()),
        DIESEL("diesel", "hbmfluid.diesel", 0xF2EED5, () -> ModFluids.DIESEL.get()),
        KEROSENE("kerosene", "hbmfluid.kerosene", 0xFFA5D2, () -> ModFluids.KEROSENE.get()),
        PETROLEUM("petroleum", "hbmfluid.petroleum", 0x7CB7C9, () -> ModFluids.PETROLEUM.get()),
        GAS("gas", "hbmfluid.gas", 0xFFFEED, () -> ModFluids.GAS.get()),
        CARBONDIOXIDE("carbondioxide", "hbmfluid.carbondioxide", 0xB0B0B0,
                () -> ModFluids.CARBONDIOXIDE.get()),
        DEUTERIUM("deuterium", "hbmfluid.deuterium", 0x3A6EA5, () -> ModFluids.DEUTERIUM.get()),
        TRITIUM("tritium", "hbmfluid.tritium", 0x2FB24C, () -> ModFluids.TRITIUM.get()),
        CRYOGEL("cryogel", "hbmfluid.cryogel", 0x7DE7FF, () -> ModFluids.CRYOGEL.get()),
        UNSATURATEDS("unsaturateds", "hbmfluid.unsaturateds", 0x628FAE,
                () -> ModFluids.UNSATURATEDS.get()),
        AIRBLAST("airblast", "hbmfluid.airblast", 0xFFDADA, () -> ModFluids.AIRBLAST.get()),
        FLUE("flue", "hbmfluid.flue", 0x131313, () -> ModFluids.FLUE.get()),
        MERCURY("mercury", "hbmfluid.mercury", 0x808080, () -> ModFluids.MERCURY.get()),
        BLOOD("blood", "hbmfluid.blood", 0xB22424, () -> ModFluids.BLOOD.get()),
        PAIN("pain", "hbmfluid.pain", 0x938541, () -> ModFluids.PAIN.get()),
        SAS3("sas3", "hbmfluid.sas3", 0x4FFFFC, () -> ModFluids.SAS3.get());

        private final String id;
        private final String translationKey;
        private final int color;
        private final Supplier<Fluid> fluid;

        ContainedFluid(String id, String translationKey, int color, Supplier<Fluid> fluid) {
            this.id = id;
            this.translationKey = translationKey;
            this.color = color;
            this.fluid = fluid;
        }

        public String id() { return id; }
        public String translationKey() { return translationKey; }
        public int color() { return color; }
        public Fluid fluid() { return fluid.get(); }

        public static ContainedFluid byId(String id) {
            for (ContainedFluid fluid : values()) if (fluid.id.equals(id)) return fluid;
            return NONE;
        }

        public static ContainedFluid fromFluid(Fluid fluid) {
            for (ContainedFluid candidate : values()) if (fluid.isSame(candidate.fluid())) return candidate;
            return null;
        }
    }
}
