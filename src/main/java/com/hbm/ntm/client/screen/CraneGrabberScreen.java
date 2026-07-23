package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.inventory.CraneGrabberMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public final class CraneGrabberScreen extends AbstractContainerScreen<CraneGrabberMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/storage/gui_crane_grabber.png");

    public CraneGrabberScreen(CraneGrabberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 185;
        inventoryLabelX = 8;
        inventoryLabelY = 91;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (!menu.getCarried().isEmpty()) return;
        for (int slot = 0; slot < 9; slot++) {
            if (menu.getSlot(slot).hasItem() && isHovering(menu.getSlot(slot).x, menu.getSlot(slot).y,
                    16, 16, mouseX, mouseY)) {
                graphics.renderTooltip(font, List.of(
                        Component.literal("Right click to change").withStyle(ChatFormatting.RED)
                                .getVisualOrderText(),
                        Component.literal(menu.filterModeLabel(slot)).withStyle(ChatFormatting.YELLOW)
                                .getVisualOrderText()), mouseX, mouseY - 30);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        graphics.blit(TEXTURE, leftPos + 108, topPos + (menu.whitelist() ? 33 : 47),
                176, 0, 3, 6, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && mouseX >= leftPos + 97 && mouseX < leftPos + 111
                && mouseY > topPos + 30 && mouseY <= topPos + 56) {
            if (minecraft != null && minecraft.gameMode != null) {
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
