package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.TurretFriendlyBlockEntity;
import com.hbm.ntm.inventory.TurretFriendlyMenu;
import com.hbm.ntm.network.TurretWhitelistPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public final class TurretFriendlyScreen extends AbstractContainerScreen<TurretFriendlyMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/weapon/gui_turret_friendly.png");
    private EditBox field;
    private int whitelistIndex;

    public TurretFriendlyScreen(TurretFriendlyMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 222;
        inventoryLabelX = 8;
        inventoryLabelY = 128;
    }

    @Override protected void init() {
        super.init();
        field = new EditBox(font, leftPos + 10, topPos + 65, 50, 14, Component.empty());
        field.setMaxLength(25);
        field.setBordered(false);
        field.setTextColor(0x00FF00);
        addWidget(field);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(152, 45, 16, 53, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.literal(menu.power() + " / "
                    + TurretFriendlyBlockEntity.MAX_POWER + " HE"), mouseX, mouseY);
        } else {
            String[] keys = {"turret.players", "turret.animals", "turret.mobs", "turret.machines"};
            for (int id = 1; id <= 4; id++) if (isHovering(8 + (id - 1) * 14, 30, 10, 10, mouseX, mouseY)) {
                Component state = Component.translatable(menu.mode(id) ? "turret.on" : "turret.off");
                graphics.renderTooltip(font, Component.translatable(keys[id - 1], state), mouseX, mouseY);
                break;
            }
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.active()) graphics.blit(TEXTURE, leftPos + 115, topPos + 26, 176, 40, 18, 18, 256, 256);
        for (int id = 1; id <= 4; id++) {
            if (menu.mode(id)) graphics.blit(TEXTURE, leftPos + 8 + (id - 1) * 14, topPos + 30,
                    176, (id - 1) * 10, 10, 10, 256, 256);
        }
        int power = Math.clamp(menu.power() * 53 / (int) TurretFriendlyBlockEntity.MAX_POWER, 0, 53);
        if (power > 0) graphics.blit(TEXTURE, leftPos + 152, topPos + 97 - power,
                194, 52 - power, 16, power, 256, 256);
        int tallies = menu.stattrak();
        if (tallies >= 36) {
            graphics.blit(TEXTURE, leftPos + 77, topPos + 50, 176, 120, 63, 6, 256, 256);
        } else {
            int steps = (int) Math.ceil(tallies / 5D);
            for (int step = 0; step < steps; step++) {
                int remainder = tallies % 5;
                if (step < steps - 1 || remainder == 0) {
                    graphics.blit(TEXTURE, leftPos + 77 + 9 * step, topPos + 50,
                            194, 94, 9, 6, 256, 256);
                } else {
                    graphics.blit(TEXTURE, leftPos + 77 + 9 * step, topPos + 50,
                            176, 94, remainder * 2, 6, 256, 256);
                }
            }
        }
        if (inside(mouseX, mouseY, 7, 80, 18, 18)) {
            graphics.blit(TEXTURE, leftPos + 7, topPos + 80, 176, 58, 18, 18, 256, 256);
        }
        if (inside(mouseX, mouseY, 43, 80, 18, 18)) {
            graphics.blit(TEXTURE, leftPos + 43, topPos + 80, 194, 58, 18, 18, 256, 256);
        }
        if (inside(mouseX, mouseY, 7, 98, 18, 18)) {
            graphics.blit(TEXTURE, leftPos + 7, topPos + 98, 176, 76, 18, 18, 256, 256);
        }
        if (inside(mouseX, mouseY, 43, 98, 18, 18)) {
            graphics.blit(TEXTURE, leftPos + 43, topPos + 98, 194, 76, 18, 18, 256, 256);
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inside(mouseX, mouseY, 115, 26, 18, 18)) return press(0);
        for (int id = 1; id <= 4; id++) {
            if (inside(mouseX, mouseY, 8 + (id - 1) * 14, 30, 10, 10)) return press(id);
        }
        List<String> names = menu.whitelist();
        if (inside(mouseX, mouseY, 7, 80, 18, 18) && !names.isEmpty()) {
            whitelistIndex = Math.floorMod(whitelistIndex - 1, names.size());
            return true;
        }
        if (inside(mouseX, mouseY, 43, 80, 18, 18) && !names.isEmpty()) {
            whitelistIndex = (whitelistIndex + 1) % names.size();
            return true;
        }
        if (inside(mouseX, mouseY, 7, 98, 18, 18)) {
            String name = field.getValue().strip();
            if (!name.isEmpty()) {
                PacketDistributor.sendToServer(new TurretWhitelistPayload(0, name, 0));
                field.setValue("");
            }
            return true;
        }
        if (inside(mouseX, mouseY, 43, 98, 18, 18) && !names.isEmpty()) {
            whitelistIndex = Math.min(whitelistIndex, names.size() - 1);
            PacketDistributor.sendToServer(new TurretWhitelistPayload(1, "", whitelistIndex));
            whitelistIndex = Math.max(0, whitelistIndex - 1);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean press(int id) {
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        return true;
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        double relativeX = mouseX - leftPos;
        double relativeY = mouseY - topPos;
        return relativeX >= x && relativeX < x + width && relativeY >= y && relativeY < y + height;
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
        List<String> names = menu.whitelist();
        if (names.isEmpty()) whitelistIndex = 0;
        else whitelistIndex = Math.min(whitelistIndex, names.size() - 1);
        String shown = names.isEmpty() ? Component.translatable("turret.none").getString()
                : names.get(whitelistIndex);
        String typed = field == null ? "" : field.getValue();
        if (field != null && field.isFocused() && System.currentTimeMillis() % 1000L < 500L) {
            int cursor = field.getCursorPosition();
            typed = typed.substring(0, cursor) + "||" + typed.substring(cursor);
        }
        graphics.pose().pushPose();
        graphics.pose().scale(0.5F, 0.5F, 1F);
        graphics.drawString(font, shown, 24, 102, 0x00FF00, false);
        graphics.drawString(font, typed, 24, 138, 0x00FF00, false);
        graphics.pose().popPose();
    }
}
