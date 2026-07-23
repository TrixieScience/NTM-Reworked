package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.fluid.FluidTankProperties;
import com.hbm.ntm.item.FluidIdentifierItem;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public final class HbmFluidIngredientRenderer implements IIngredientRenderer<FluidStack> {
    static final HbmFluidIngredientRenderer INSTANCE = new HbmFluidIngredientRenderer();
    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "textures/item/fluid_icon.png");

    private final IIngredientRenderer<FluidStack> fallback;

    private HbmFluidIngredientRenderer() {
        this(null);
    }

    private HbmFluidIngredientRenderer(IIngredientRenderer<FluidStack> fallback) {
        this.fallback = fallback;
    }

    public static IIngredientRenderer<FluidStack> withFallback(IIngredientRenderer<FluidStack> fallback) {
        return new HbmFluidIngredientRenderer(fallback);
    }

    @Override
    public void render(GuiGraphics graphics, FluidStack stack) {
        FluidIdentifierItem.Selection fluid = FluidIdentifierItem.Selection.fromFluid(stack.getFluid());
        if (fluid == FluidIdentifierItem.Selection.NONE && fallback != null) {
            fallback.render(graphics, stack);
            return;
        }
        int color = fluid.color();
        graphics.setColor((color >> 16 & 0xFF) / 255F,
                (color >> 8 & 0xFF) / 255F,
                (color & 0xFF) / 255F,
                1F);
        graphics.blit(ICON, 0, 0, 0, 0, 16, 16, 16, 16);
        graphics.setColor(1F, 1F, 1F, 1F);
    }

    @Override
    public List<Component> getTooltip(FluidStack stack, TooltipFlag tooltipFlag) {
        FluidIdentifierItem.Selection fluid = FluidIdentifierItem.Selection.fromFluid(stack.getFluid());
        if (fluid == FluidIdentifierItem.Selection.NONE && fallback != null) {
            return fallback.getTooltip(stack, tooltipFlag);
        }

        FluidTankProperties.Profile profile = FluidTankProperties.get(fluid);
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(stack.getHoverName());
        if (stack.getAmount() > 0) {
            tooltip.add(Component.literal(stack.getAmount() + "mB").withStyle(ChatFormatting.GRAY));
        }

        int temperature = stack.getFluidType().getTemperature(stack);
        if (temperature != 293 && temperature != 300) {
            int celsius = temperature - 273;
            tooltip.add(Component.literal(celsius + "°C")
                    .withStyle(celsius < 0 ? ChatFormatting.BLUE : ChatFormatting.RED));
        }

        if (profile.flammable()) {
            tooltip.add(Component.literal("[Flammable]").withStyle(ChatFormatting.YELLOW));
        }
        if (profile.symbol() == FluidTankProperties.Symbol.RADIATION) {
            tooltip.add(Component.literal("[Radioactive]").withStyle(ChatFormatting.YELLOW));
        }
        if (profile.symbol() == FluidTankProperties.Symbol.ACID
                || profile.symbol() == FluidTankProperties.Symbol.OXIDIZER) {
            tooltip.add(Component.literal("[Corrosive]").withStyle(ChatFormatting.YELLOW));
        }
        boolean polluting = profile.burnSootPerMb() > 0F || profile.spillPoisonPerMb() > 0F;
        if (polluting) {
            tooltip.add(Component.literal("[Polluting]").withStyle(ChatFormatting.GOLD));
        }

        if (Screen.hasShiftDown()) {
            addHiddenInfo(tooltip, profile);
        } else if (hasHiddenInfo(profile)) {
            tooltip.add(Component.literal("Hold <")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
                    .append(Component.literal("LSHIFT")
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
                    .append(Component.literal("> to display more info")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
        }
        return tooltip;
    }

    private static boolean hasHiddenInfo(FluidTankProperties.Profile profile) {
        return profile.phase() != FluidTankProperties.Phase.NONE
                || profile.symbol() == FluidTankProperties.Symbol.NO_WATER
                || profile.symbol() == FluidTankProperties.Symbol.ASPHYXIANT
                || profile.symbol() == FluidTankProperties.Symbol.CRYOGENIC
                || profile.symbol() == FluidTankProperties.Symbol.OXIDIZER
                || profile.burnSootPerMb() > 0F
                || profile.spillPoisonPerMb() > 0F;
    }

    private static void addHiddenInfo(List<Component> tooltip, FluidTankProperties.Profile profile) {
        if (profile.gaseous()) {
            tooltip.add(Component.literal("[Gaseous]").withStyle(ChatFormatting.BLUE));
        } else if (profile.liquid()) {
            tooltip.add(Component.literal("[Liquid]").withStyle(ChatFormatting.BLUE));
        }

        switch (profile.symbol()) {
            case NO_WATER ->
                    tooltip.add(Component.literal("[Reacts with Water]").withStyle(ChatFormatting.DARK_RED));
            case ASPHYXIANT ->
                    tooltip.add(Component.literal("[Asphyxiant]").withStyle(ChatFormatting.GRAY));
            case CRYOGENIC ->
                    tooltip.add(Component.literal("[Cryogenic]").withStyle(ChatFormatting.AQUA));
            case OXIDIZER ->
                    tooltip.add(Component.literal("[Oxidizer]").withStyle(ChatFormatting.YELLOW));
            default -> {
            }
        }

        if (profile.spillPoisonPerMb() > 0F) {
            tooltip.add(Component.literal("When spilled:").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal(" - " + profile.spillPoisonPerMb() + " poison per mB")
                    .withStyle(ChatFormatting.GREEN));
        }
        if (profile.burnSootPerMb() > 0F) {
            tooltip.add(Component.literal("When burned:").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal(" - " + profile.burnSootPerMb() + " soot per mB")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }
}
