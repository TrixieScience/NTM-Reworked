package com.hbm.ntm.client.screen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.inventory.AmmoPressMenu;
import com.hbm.ntm.recipe.AmmoPressRecipes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AmmoPressScreen extends AbstractContainerScreen<AmmoPressMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            HbmNtm.MOD_ID, "textures/gui/processing/gui_ammo_press.png");
    private final List<AmmoPressRecipes.Recipe> recipes = new ArrayList<>();
    private EditBox search;
    private int page;

    public AmmoPressScreen(AmmoPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 200;
        inventoryLabelX = 8;
        inventoryLabelY = 106;
    }

    @Override protected void init() {
        super.init();
        search = new EditBox(font, leftPos + 10, topPos + 75, 66, 12, Component.translatable("gui.recipe.search"));
        search.setBordered(false);
        search.setTextColor(0xFFFFFF);
        search.setMaxLength(25);
        search.setResponder(this::filter);
        addRenderableWidget(search);
        filter("");
    }

    private void filter(String value) {
        recipes.clear();
        String needle = value.toLowerCase(Locale.ROOT);
        for (AmmoPressRecipes.Recipe recipe : AmmoPressRecipes.all()) {
            if (needle.isEmpty() || recipe.output().getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle)) {
                recipes.add(recipe);
            }
        }
        page = 0;
    }

    private int maxPage() { return Math.max(0, (recipes.size() - 12 + 2) / 3); }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderGhostInputs(graphics);
        renderTooltip(graphics, mouseX, mouseY);
        for (int i = page * 3; i < Math.min(recipes.size(), page * 3 + 12); i++) {
            int visible = i - page * 3;
            int x = 16 + 18 * (visible / 3);
            int y = 17 + 18 * (visible % 3);
            if (isHovering(x, y, 18, 18, mouseX, mouseY)) {
                graphics.renderTooltip(font, recipes.get(i).output(), mouseX, mouseY);
                break;
            }
        }
    }

    private void renderGhostInputs(GuiGraphics graphics) {
        AmmoPressRecipes.Recipe recipe = AmmoPressRecipes.get(menu.selectedRecipe());
        if (recipe == null) return;
        for (int slot = 0; slot < 9; slot++) {
            if (menu.getSlot(slot).hasItem() || recipe.input(slot) == null) continue;
            ItemStack ghost = recipe.input(slot).displayStack();
            int x = leftPos + 116 + 18 * (slot % 3);
            int y = topPos + 18 + 18 * (slot / 3);
            graphics.pose().pushPose();
            graphics.setColor(1F, 1F, 1F, 0.45F);
            graphics.renderFakeItem(ghost, x, y);
            graphics.renderItemDecorations(font, ghost, x, y, ghost.getCount() > 1 ? Integer.toString(ghost.getCount()) : null);
            graphics.setColor(1F, 1F, 1F, 1F);
            graphics.pose().popPose();
        }
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (isHovering(7, 17, 9, 54, mouseX, mouseY)) graphics.blit(TEXTURE, leftPos + 7, topPos + 17, 176, 0, 9, 54, 256, 256);
        if (isHovering(88, 17, 9, 54, mouseX, mouseY)) graphics.blit(TEXTURE, leftPos + 88, topPos + 17, 185, 0, 9, 54, 256, 256);
        if (search != null && search.isFocused()) graphics.blit(TEXTURE, leftPos + 8, topPos + 72, 176, 54, 70, 16, 256, 256);
        for (int i = page * 3; i < Math.min(recipes.size(), page * 3 + 12); i++) {
            int visible = i - page * 3;
            int x = leftPos + 16 + 18 * (visible / 3);
            int y = topPos + 17 + 18 * (visible % 3);
            ItemStack output = recipes.get(i).output();
            graphics.renderFakeItem(output, x + 1, y + 1);
            graphics.blit(TEXTURE, x, y, menu.selectedRecipe() == recipes.get(i).index() ? 194 : 212, 0, 18, 18, 256, 256);
            graphics.renderItemDecorations(font, output, x + 1, y + 1, Integer.toString(output.getCount()));
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, imageWidth / 2 - font.width(title) / 2, 6, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovering(7, 17, 9, 54, mouseX, mouseY) && page > 0) { page--; click(); return true; }
        if (isHovering(88, 17, 9, 54, mouseX, mouseY) && page < maxPage()) { page++; click(); return true; }
        for (int i = page * 3; i < Math.min(recipes.size(), page * 3 + 12); i++) {
            int visible = i - page * 3;
            if (isHovering(16 + 18 * (visible / 3), 17 + 18 * (visible % 3), 18, 18, mouseX, mouseY)) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, recipes.get(i).index());
                click();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && page > 0) page--;
        else if (scrollY < 0 && page < maxPage()) page++;
        else return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        return true;
    }

    private void click() {
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1F));
    }
}
