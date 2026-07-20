package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.GasTurbineBlockEntity;
import com.hbm.ntm.inventory.GasTurbineMenu;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.network.GasTurbineControlPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

/** The 2010s called. Their 4,848-pixel-wide RPM gauge still works. */
public final class GasTurbineScreen extends AbstractContainerScreen<GasTurbineMenu> {
    private static final ResourceLocation TEXTURE = texture("gui/generators/gui_turbinegas.png");
    private static final ResourceLocation GAUGE = texture("gui/gauges/button_big.png");
    private static final ResourceLocation UTILITY = texture("gui/gui_utility.png");
    private int numberToDisplay;
    private int digitNumber;
    private int exponent;
    private boolean draggingSlider;

    public GasTurbineScreen(GasTurbineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 223;
        inventoryLabelX = 8;
        inventoryLabelY = 129;
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        tankTooltip(graphics, mouseX, mouseY, 8, 16, 16, 48,
                menu.selectedFuel(), menu.fuel(), GasTurbineBlockEntity.FUEL_CAPACITY);
        tankTooltip(graphics, mouseX, mouseY, 8, 70, 16, 32,
                FluidIdentifierItem.Selection.LUBRICANT, menu.lubricant(), GasTurbineBlockEntity.LUBRICANT_CAPACITY);
        tankTooltip(graphics, mouseX, mouseY, 147, 61, 16, 36,
                FluidIdentifierItem.Selection.WATER, menu.water(), GasTurbineBlockEntity.WATER_CAPACITY);
        tankTooltip(graphics, mouseX, mouseY, 147, 21, 16, 36,
                FluidIdentifierItem.Selection.HOTSTEAM, menu.steam(), GasTurbineBlockEntity.STEAM_CAPACITY);

        if (isHovering(26, 108, 142, 16, mouseX, mouseY)) tooltip(graphics, mouseX, mouseY,
                menu.power() + "/" + GasTurbineBlockEntity.MAX_POWER + " HE");
        if (isHovering(36, 36, 16, 66, mouseX, mouseY)) {
            if (menu.state() == 1) {
                double perTick = 2.5D + 50D * menu.throttle() / 100D;
                tooltip(graphics, mouseX, mouseY, "Fuel consumption: " + perTick * 20D + " mB/s");
            } else tooltip(graphics, mouseX, mouseY, "Generator offline");
        }
        if (isHovering(133, 23, 8, 72, mouseX, mouseY)) tooltip(graphics, mouseX, mouseY,
                "Temperature: " + Math.max(menu.temperature(), 20) + "°C");
        if (mouseX >= leftPos - 16 && mouseX < leftPos && mouseY >= topPos + 34 && mouseY < topPos + 50) {
            graphics.renderTooltip(font, List.of(Component.literal("Automatic control:"),
                            Component.literal("Matches throttle to the power buffer.")),
                    Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= leftPos - 16 && mouseX < leftPos
                && mouseY >= topPos + 50 && mouseY < topPos + 66) {
            graphics.renderTooltip(font, List.of(Component.literal("Accepted gaseous fuel:"),
                            Component.literal("  Natural Gas")), Optional.empty(), mouseX, mouseY);
        } else if (mouseX >= leftPos - 16 && mouseX < leftPos
                && mouseY >= topPos + 66 && mouseY < topPos + 82
                && (menu.fuel() < 5_000 || menu.lubricant() < 1_000)) {
            graphics.renderTooltip(font, List.of(Component.literal("Low fuel or lubricant."),
                            Component.literal("The turbine will stop when either runs dry.")),
                    Optional.empty(), mouseX, mouseY);
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        graphics.blit(TEXTURE, leftPos + 74, topPos + 86, 194, menu.autoMode() ? 11 : 24,
                29, 13, 256, 256);

        int stateU = menu.state() == 0 ? 178 : menu.state() == -1 ? 194 : 210;
        graphics.blit(TEXTURE, leftPos + 80, topPos + 32, stateU, 38, 16, 16, 256, 256);
        if (menu.state() == -1) displayStartup(graphics);
        else if (menu.state() == 1) drawPowerMeter(graphics, menu.output() * 20);

        graphics.blit(TEXTURE, leftPos + 36, topPos + 97 - menu.slider(), 178, 0,
                16, 6, 256, 256);
        int powerWidth = (int) (Math.max(menu.power(), 0L) * 142L / GasTurbineBlockEntity.MAX_POWER);
        if (powerWidth > 0) graphics.blit(TEXTURE, leftPos + 26, topPos + 109,
                0, 223, Math.min(powerWidth, 142), 16, 256, 256);

        int rpm = Math.clamp(menu.rpm(), 0, 100);
        graphics.blit(GAUGE, leftPos + 64, topPos + 16, rpm * 48, 0,
                48, 48, 4_848, 48);
        int temperatureHeight = Math.clamp(menu.temperature(), 0, 800) * 64 / 800;
        if (temperatureHeight > 0) graphics.blit(TEXTURE, leftPos + 136,
                topPos + 28 + 64 - temperatureHeight, 176, 64 - temperatureHeight,
                2, temperatureHeight, 256, 256);

        drawFluid(graphics, menu.selectedFuel(), menu.fuel(), 8, 16, 48, GasTurbineBlockEntity.FUEL_CAPACITY);
        drawFluid(graphics, FluidIdentifierItem.Selection.LUBRICANT, menu.lubricant(),
                8, 70, 32, GasTurbineBlockEntity.LUBRICANT_CAPACITY);
        drawFluid(graphics, FluidIdentifierItem.Selection.WATER, menu.water(),
                147, 61, 36, GasTurbineBlockEntity.WATER_CAPACITY);
        drawFluid(graphics, FluidIdentifierItem.Selection.HOTSTEAM, menu.steam(),
                147, 21, 36, GasTurbineBlockEntity.STEAM_CAPACITY);

        graphics.blit(UTILITY, leftPos - 16, topPos + 34, 8, 0, 16, 16, 256, 256);
        graphics.blit(UTILITY, leftPos - 16, topPos + 50, 24, 0, 16, 16, 256, 256);
        if (menu.fuel() < 5_000 || menu.lubricant() < 1_000) {
            int warningU = menu.fuel() == 0 || menu.lubricant() == 0 ? 8 : 24;
            graphics.blit(UTILITY, leftPos - 16, topPos + 66, warningU, 16, 16, 16, 256, 256);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double dx = mouseX - leftPos - 88D;
            double dy = mouseY - topPos - 40D;
            if (dx * dx + dy * dy <= 64D) {
                if (menu.state() == 0 && menu.counter() == 0) send(GasTurbineBlockEntity.Control.STATE, 1);
                else if (menu.state() == 1) send(GasTurbineBlockEntity.Control.STATE, 0);
                return true;
            }
            if (menu.state() == 1 && inside(mouseX, mouseY, 74, 86, 29, 13)) {
                send(GasTurbineBlockEntity.Control.AUTO, menu.autoMode() ? 0 : 1);
                return true;
            }
            if (menu.state() == 1 && inside(mouseX, mouseY, 36, 97 - menu.slider(), 16, 6)) {
                draggingSlider = true;
                send(GasTurbineBlockEntity.Control.AUTO, 0);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button,
                                           double dragX, double dragY) {
        if (button == 0 && draggingSlider && menu.state() == 1 && !menu.autoMode()) {
            int slider = Math.clamp((int) Math.round(topPos + 100D - mouseY), 0,
                    GasTurbineBlockEntity.MAX_SLIDER);
            sendQuiet(GasTurbineBlockEntity.Control.THROTTLE, slider);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void displayStartup(GuiGraphics graphics) {
        if (numberToDisplay < 8_888_888 && menu.counter() < 60) {
            digitNumber++;
            if (digitNumber == 9) {
                digitNumber = 1;
                exponent++;
            }
            numberToDisplay += (int) Math.pow(10, exponent);
        }
        if (menu.counter() > 50) numberToDisplay = 0;
        drawPowerMeter(graphics, numberToDisplay);
    }

    private void drawPowerMeter(GuiGraphics graphics, int number) {
        int[] digits = new int[7];
        for (int index = 6; index >= 0; index--) {
            digits[index] = Math.floorMod(number, 10);
            number /= 10;
            graphics.blit(TEXTURE, leftPos + 65 + index * 7, topPos + 71,
                    194 + digits[index] * 5, 0, 5, 11, 256, 256);
        }
        int zeroes = 0;
        while (zeroes < 6 && digits[zeroes] == 0) zeroes++;
        for (int index = 0; index < zeroes; index++) {
            graphics.blit(TEXTURE, leftPos + 65 + index * 7, topPos + 71,
                    244, 0, 5, 11, 256, 256);
        }
    }

    private void drawFluid(GuiGraphics graphics, FluidIdentifierItem.Selection fluid, int amount,
                           int x, int y, int height, int capacity) {
        if (fluid == FluidIdentifierItem.Selection.NONE || amount <= 0) return;
        ResourceLocation texture = texture("gui/fluids/" + fluid.id() + ".png");
        int remaining = Math.clamp(amount * height / capacity, 0, height);
        int bottom = topPos + y + height;
        while (remaining > 0) {
            int strip = Math.min(16, remaining);
            bottom -= strip;
            graphics.blit(texture, leftPos + x, bottom, 0, 16 - strip,
                    16, strip, 16, 16);
            remaining -= strip;
        }
    }

    private void tankTooltip(GuiGraphics graphics, int mouseX, int mouseY,
                             int x, int y, int width, int height,
                             FluidIdentifierItem.Selection fluid, int amount, int capacity) {
        if (!isHovering(x, y, width, height, mouseX, mouseY)) return;
        graphics.renderTooltip(font, List.of(Component.translatable(fluid.translationKey()),
                        Component.literal(amount + "/" + capacity + "mB")),
                Optional.empty(), mouseX, mouseY);
    }

    private void tooltip(GuiGraphics graphics, int mouseX, int mouseY, String text) {
        graphics.renderTooltip(font, Component.literal(text), mouseX, mouseY);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width
                && mouseY >= topPos + y && mouseY < topPos + y + height;
    }

    private void send(GasTurbineBlockEntity.Control control, int value) {
        if (minecraft != null) minecraft.getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
        sendQuiet(control, value);
    }

    private void sendQuiet(GasTurbineBlockEntity.Control control, int value) {
        PacketDistributor.sendToServer(new GasTurbineControlPayload(control, value));
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "textures/" + path);
    }
}
