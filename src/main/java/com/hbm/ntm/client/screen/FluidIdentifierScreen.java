package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.inventory.FluidIdentifierMenu;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.network.FluidIdentifierSelectPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Compact searchable selector for naming the pipe soup. */
public final class FluidIdentifierScreen extends AbstractContainerScreen<FluidIdentifierMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/machine/gui_fluid.png");
    private static final int RESULT_COUNT = 9;
    private static final int RESULT_X = 7;
    private static final int RESULT_Y = 29;
    private static final int RESULT_SIZE = 18;

    private final List<FluidIdentifierItem.Selection> results = new ArrayList<>(RESULT_COUNT);
    private FluidIdentifierItem.Selection primary;
    private FluidIdentifierItem.Selection secondary;
    private EditBox search;

    public FluidIdentifierScreen(FluidIdentifierMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 54;
        primary = menu.primary();
        secondary = menu.secondary();
    }

    @Override
    protected void init() {
        super.init();
        results.clear();
        search = new EditBox(font, leftPos + 46, topPos + 11, 86, 12, Component.empty());
        search.setBordered(false);
        search.setTextColor(0xFFFFFF);
        search.setTextColorUneditable(0xFFFFFF);
        search.setMaxLength(64);
        search.setResponder(this::updateSearch);
        addRenderableWidget(search);
        setFocused(search);
        search.setFocused(true);
    }

    private void updateSearch(String value) {
        results.clear();
        String needle = value.toLowerCase(Locale.ROOT);
        for (FluidIdentifierItem.Selection selection : FluidIdentifierItem.Selection.values()) {
            if (selection == FluidIdentifierItem.Selection.NONE) continue;
            String name = Component.translatable(selection.translationKey()).getString().toLowerCase(Locale.ROOT);
            if (name.contains(needle)) {
                results.add(selection);
                if (results.size() == RESULT_COUNT) return;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        for (int index = 0; index < results.size(); index++) {
            if (insideResult(index, mouseX, mouseY)) {
                graphics.renderTooltip(font,
                        List.of(Component.translatable(results.get(index).translationKey())),
                        Optional.empty(), mouseX, mouseY);
                return;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.setColor(1F, 1F, 1F, 1F);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (search != null && search.isFocused()) {
            graphics.blit(TEXTURE, leftPos + 43, topPos + 7, 166, 54, 90, 18, 256, 256);
        }

        for (int index = 0; index < results.size(); index++) {
            FluidIdentifierItem.Selection selection = results.get(index);
            int color = selection.color();
            graphics.setColor((color >> 16 & 0xFF) / 255F,
                    (color >> 8 & 0xFF) / 255F,
                    (color & 0xFF) / 255F, 1F);
            graphics.blit(TEXTURE, leftPos + 12 + index * RESULT_SIZE, topPos + 31,
                    12 + index * RESULT_SIZE, 56, 8, 14, 256, 256);
            graphics.setColor(1F, 1F, 1F, 1F);

            if (selection == primary && selection == secondary) {
                drawSelection(graphics, index, 36);
            } else if (selection == primary) {
                drawSelection(graphics, index, 0);
            } else if (selection == secondary) {
                drawSelection(graphics, index, 18);
            }
        }
    }

    private void drawSelection(GuiGraphics graphics, int index, int textureY) {
        graphics.blit(TEXTURE, leftPos + RESULT_X + index * RESULT_SIZE, topPos + RESULT_Y,
                176, textureY, RESULT_SIZE, RESULT_SIZE, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // No title, no inventory label, no unnecessary pleasantries.
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (search != null && search.isFocused() && minecraft != null
                && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            for (int index = 0; index < results.size(); index++) {
                if (!insideResult(index, mouseX, mouseY)) continue;
                FluidIdentifierItem.Selection selection = results.get(index);
                boolean setPrimary = button == 0;
                if (setPrimary) primary = selection;
                else secondary = selection;
                PacketDistributor.sendToServer(new FluidIdentifierSelectPayload(selection, setPrimary));
                if (minecraft != null) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean insideResult(int index, double mouseX, double mouseY) {
        int x = leftPos + RESULT_X + index * RESULT_SIZE;
        int y = topPos + RESULT_Y;
        return mouseX >= x && mouseX < x + RESULT_SIZE && mouseY >= y && mouseY < y + RESULT_SIZE;
    }
}
