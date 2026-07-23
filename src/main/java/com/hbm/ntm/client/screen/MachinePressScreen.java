package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.MachinePressBlockEntity;
import com.hbm.ntm.inventory.MachinePressMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Matrix4f;

import java.util.List;

public final class MachinePressScreen extends AbstractContainerScreen<MachinePressMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "textures/gui/gui_press.png");

    public MachinePressScreen(MachinePressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 214;
        inventoryLabelX = 8;
        inventoryLabelY = 120;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(25, 16, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(Component.literal(menu.speed() * 100 / MachinePressBlockEntity.MAX_SPEED + "%")),
                    java.util.Optional.empty(), mouseX, mouseY);
        } else if (isHovering(25, 34, 18, 18, mouseX, mouseY)) {
            graphics.renderTooltip(font, List.of(Component.translatable("gui.hbm.press.operations_left", menu.burnTime() / 200)),
                    java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = imageWidth / 2 - font.width(title) / 2;
        graphics.drawString(font, title, titleX, 5, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.burnTime() >= 20) {
            graphics.blit(TEXTURE, leftPos + 27, topPos + 36, 0, 214, 14, 14, 256, 256);
        }

        int progress = (int) (menu.displayPress() * 16.0D / MachinePressBlockEntity.MAX_PRESS);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + 79, topPos + 35, 15, 214, 18, progress, 256, 256);
        }
        drawGaugeNeedle(graphics, Mth.clamp(menu.speed() / (double) MachinePressBlockEntity.MAX_SPEED, 0.0D, 1.0D));
    }

    private void drawGaugeNeedle(GuiGraphics graphics, double progress) {
        float angle = (float) Math.toRadians(-Mth.clamp(progress, 0.0D, 1.0D) * 270.0D - 45.0D);
        float sin = Mth.sin(angle);
        float cos = Mth.cos(angle);
        float centerX = leftPos + 34.0F;
        float centerY = topPos + 25.0F;
        Matrix4f pose = graphics.pose().last().pose();

        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        triangle(builder, pose, centerX, centerY, sin, cos, 1.5F, 0x000000);
        triangle(builder, pose, centerX, centerY, sin, cos, 1.0F, 0x7F0000);
        BufferUploader.drawWithShader(builder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private static void triangle(BufferBuilder builder, Matrix4f pose, float centerX, float centerY,
                                 float sin, float cos, float scale, int color) {
        vertex(builder, pose, centerX, centerY, 0.0F, 5.0F, sin, cos, scale, color);
        vertex(builder, pose, centerX, centerY, 1.0F, -2.0F, sin, cos, scale, color);
        vertex(builder, pose, centerX, centerY, -1.0F, -2.0F, sin, cos, scale, color);
    }

    private static void vertex(BufferBuilder builder, Matrix4f pose, float centerX, float centerY,
                               float x, float y, float sin, float cos, float scale, int color) {
        float rotatedX = (x * cos + y * sin) * scale;
        float rotatedY = (y * cos - x * sin) * scale;
        builder.addVertex(pose, centerX + rotatedX, centerY + rotatedY, 200.0F)
                .setColor(color | 0xFF000000);
    }
}
