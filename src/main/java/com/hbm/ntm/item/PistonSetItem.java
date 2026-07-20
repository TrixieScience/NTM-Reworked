package com.hbm.ntm.item;

import com.hbm.ntm.recipe.DieselGeneratorFuels;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** The four increasingly unreasonable piston sets from the source combustion engine. */
public final class PistonSetItem extends Item {
    private final Type type;

    public PistonSetItem(Type type) {
        super(new Properties());
        this.type = type;
    }

    public Type type() { return type; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Fuel efficiency:").withStyle(ChatFormatting.YELLOW));
        for (DieselGeneratorFuels.Grade grade : new DieselGeneratorFuels.Grade[]{
                DieselGeneratorFuels.Grade.LOW, DieselGeneratorFuels.Grade.MEDIUM,
                DieselGeneratorFuels.Grade.HIGH, DieselGeneratorFuels.Grade.AERO,
                DieselGeneratorFuels.Grade.GAS}) {
            tooltip.add(Component.literal("-" + grade.displayName() + ": ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal((int) (type.efficiency(grade) * 100D) + "%")
                            .withStyle(ChatFormatting.RED)));
        }
    }

    public enum Type {
        STEEL(1.00D, 0.75D, 0.25D, 0.00D, 0.00D),
        DURA(0.50D, 1.00D, 0.90D, 0.50D, 0.00D),
        DESH(0.00D, 0.50D, 1.00D, 0.75D, 0.00D),
        STARMETAL(0.50D, 0.75D, 1.00D, 0.90D, 0.50D);

        private final double[] efficiency;
        Type(double... efficiency) { this.efficiency = efficiency; }

        public double efficiency(DieselGeneratorFuels.Grade grade) {
            int index = grade.ordinal();
            return index >= 0 && index < efficiency.length ? efficiency[index] : 0D;
        }
    }
}
