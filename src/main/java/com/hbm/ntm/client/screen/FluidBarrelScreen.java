package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.inventory.FluidBarrelMenu;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.network.FluidStorageTankModePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public final class FluidBarrelScreen extends AbstractContainerScreen<FluidBarrelMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/storage/gui_barrel.png");
    private static final Component[] MODES = {
            Component.translatable("gui.hbm.fluidtank.mode.input"),
            Component.translatable("gui.hbm.fluidtank.mode.buffer"),
            Component.translatable("gui.hbm.fluidtank.mode.output"),
            Component.translatable("gui.hbm.fluidtank.mode.locked")
    };

    public FluidBarrelScreen(FluidBarrelMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= leftPos + 151 && mouseX < leftPos + 169
                && mouseY >= topPos + 34 && mouseY < topPos + 52) {
            PacketDistributor.sendToServer(new FluidStorageTankModePayload());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(71, 17, 34, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(
                            Component.translatable(menu.selection().translationKey()),
                            Component.literal(menu.amount() + "/" + menu.capacity() + " mB")),
                    Optional.empty(), mouseX, mouseY);
        } else if (isHovering(151, 34, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, MODES[menu.mode()], mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        drawFluid(graphics, menu.selection(), menu.amount(), menu.capacity(), leftPos + 71, topPos + 17);
        graphics.blit(TEXTURE, leftPos + 151, topPos + 34, 176, menu.mode() * 18,
                18, 18, 256, 256);
    }

    private static void drawFluid(GuiGraphics graphics, FluidIdentifierItem.Selection selection,
                                  int amount, int capacity, int x, int y) {
        if (selection == FluidIdentifierItem.Selection.NONE || amount <= 0) return;
        ResourceLocation fluid = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID,
                "textures/gui/fluids/" + selection.id() + ".png");
        int height = Math.clamp(amount * 52 / capacity, 1, 52);
        int bottom = y + 52;
        while (height > 0) {
            int strip = Math.min(16, height);
            bottom -= strip;
            for (int column = 0; column < 34; column += 16) {
                int width = Math.min(16, 34 - column);
                graphics.blit(fluid, x + column, bottom, 0, 16 - strip,
                        width, strip, 16, 16);
            }
            height -= strip;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2,
                6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY,
                0x404040, false);
    }
}
