package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.inventory.CombustionEngineMenu;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.network.CombustionEngineControlPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class CombustionEngineScreen extends AbstractContainerScreen<CombustionEngineMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/generators/gui_combustion.png");
    private boolean dragging;

    public CombustionEngineScreen(CombustionEngineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 203;
        inventoryLabelX = 8;
        inventoryLabelY = 109;
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(35, 17, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(Component.translatable(menu.selectedFluid().translationKey()),
                    Component.literal(menu.fuel() + "/" + CombustionEngineBlockEntity.FUEL_CAPACITY + "mB")),
                    Optional.empty(), mouseX, mouseY);
        } else if (isHovering(143, 17, 16, 52, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.power() + "/" + CombustionEngineBlockEntity.MAX_POWER + " HE"), mouseX, mouseY);
        } else if (dragging || isHovering(80, 38, 34, 8, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(String.format(Locale.US, "%.1fmB/t", menu.throttle() * 0.2D)), mouseX, mouseY);
        } else if (isHovering(79, 50, 35, 14, mouseX, mouseY) && menu.pistonType() != null) {
            long output = (long) (menu.throttle() * 0.2D
                    * com.hbm.ntm.recipe.CombustionEngineFuels.fuel(menu.selectedFluid()).combustionEnergyPerBucket()
                    / 1_000D * menu.efficiency());
            graphics.renderTooltip(font, List.of(
                    Component.literal(String.format(Locale.US, "%,d HE/t", output)).withStyle(ChatFormatting.YELLOW),
                    Component.literal(String.format(Locale.US, "%,d HE/s", output * 20L)).withStyle(ChatFormatting.YELLOW)),
                    Optional.empty(), mouseX, mouseY);
        } else if (isHovering(79, 13, 35, 15, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal("Ignition"), mouseX, mouseY);
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.pistonType() != null) graphics.blit(TEXTURE, leftPos + 80, topPos + 51,
                176, 52 + menu.pistonType().ordinal() * 12, 25, 12, 256, 256);
        graphics.blit(TEXTURE, leftPos + 79 + menu.throttle() * 32 / CombustionEngineBlockEntity.MAX_THROTTLE,
                topPos + 38, 192, 15, 4, 8, 256, 256);
        if (menu.on()) graphics.blit(TEXTURE, leftPos + 79, topPos + 13, 192, 0, 35, 15, 256, 256);
        int powerHeight = (int) (menu.power() * 53L / CombustionEngineBlockEntity.MAX_POWER);
        if (powerHeight > 0) graphics.blit(TEXTURE, leftPos + 143, topPos + 69 - powerHeight,
                176, 52 - powerHeight, 16, powerHeight, 256, 256);
        drawFluid(graphics, menu.selectedFluid(), menu.fuel());
    }

    private void drawFluid(GuiGraphics graphics, FluidIdentifierItem.Selection selection, int amount) {
        if (selection == FluidIdentifierItem.Selection.NONE || amount <= 0) return;
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID,
                "textures/gui/fluids/" + selection.id() + ".png");
        int remaining = amount * 52 / CombustionEngineBlockEntity.FUEL_CAPACITY;
        int bottom = topPos + 69;
        while (remaining > 0) {
            int strip = Math.min(16, remaining);
            bottom -= strip;
            graphics.blit(texture, leftPos + 35, bottom, 0, 16 - strip, 16, strip, 16, 16);
            remaining -= strip;
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && inside(mouseX, mouseY, 89, 13, 16, 14)) {
            click();
            PacketDistributor.sendToServer(new CombustionEngineControlPayload(
                    CombustionEngineBlockEntity.Control.TOGGLE, 0));
            return true;
        }
        if (button == 0 && inside(mouseX, mouseY, 79, 38, 36, 8)) {
            click();
            dragging = true;
            sendThrottle(mouseX);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (button == 0 && dragging) { sendThrottle(mouseX); return true; }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void sendThrottle(double mouseX) {
        int value = Math.clamp((int) ((mouseX - leftPos - 81D) * CombustionEngineBlockEntity.MAX_THROTTLE / 32D),
                0, CombustionEngineBlockEntity.MAX_THROTTLE);
        PacketDistributor.sendToServer(new CombustionEngineControlPayload(
                CombustionEngineBlockEntity.Control.THROTTLE, value));
    }
    private void click() {
        if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
    }
    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= leftPos + x && mouseX < leftPos + x + width
                && mouseY >= topPos + y && mouseY < topPos + y + height;
    }
}
