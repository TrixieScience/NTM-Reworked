package com.hbm.ntm.client.screen;

import com.hbm.ntm.blockentity.FluidPumpBlockEntity;
import com.hbm.ntm.inventory.FluidPumpMenu;
import com.hbm.ntm.network.FluidPumpConfigPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FluidPumpScreen extends AbstractContainerScreen<FluidPumpMenu> {
    private static final String[] PRIORITIES = {"LOWEST", "LOW", "NORMAL", "HIGH", "HIGHEST"};
    private EditBox throughput;
    private Button pressure;
    private Button priority;
    private int pressureValue;
    private int priorityValue;

    public FluidPumpScreen(FluidPumpMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 300;
        imageHeight = 80;
    }

    @Override protected void init() {
        super.init();
        pressureValue = menu.pressure();
        priorityValue = menu.priority();
        throughput = new EditBox(font, leftPos, topPos + 40, 90, 20, Component.empty());
        throughput.setMaxLength(5);
        throughput.setValue(Integer.toString(menu.throughput()));
        addRenderableWidget(throughput);
        pressure = addRenderableWidget(Button.builder(Component.literal(pressureValue + " PU"), button -> {
            pressureValue = (pressureValue + 1) % 6;
            button.setMessage(Component.literal(pressureValue + " PU"));
        }).bounds(leftPos + 100, topPos + 40, 90, 20).build());
        priority = addRenderableWidget(Button.builder(Component.literal(PRIORITIES[priorityValue]), button -> {
            priorityValue = (priorityValue + 1) % FluidPumpBlockEntity.PRIORITY_COUNT;
            button.setMessage(Component.literal(PRIORITIES[priorityValue]));
        }).bounds(leftPos + 200, topPos + 40, 90, 20).build());
        setInitialFocus(throughput);
    }

    @Override public void removed() {
        int value = menu.throughput();
        try {
            value = Integer.parseInt(throughput.getValue());
        } catch (NumberFormatException ignored) {
        }
        PacketDistributor.sendToServer(new FluidPumpConfigPayload(value, pressureValue, priorityValue));
        super.removed();
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.drawString(font, "Throughput:", leftPos, topPos + 20, 0xA0A0A0, false);
        graphics.drawString(font, "(max. 10,000mB)", leftPos, topPos + 30, 0xA0A0A0, false);
        graphics.drawString(font, "Pressure:", leftPos + 100, topPos + 20, 0xA0A0A0, false);
        graphics.drawString(font, "Priority:", leftPos + 200, topPos + 20, 0xA0A0A0, false);
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
