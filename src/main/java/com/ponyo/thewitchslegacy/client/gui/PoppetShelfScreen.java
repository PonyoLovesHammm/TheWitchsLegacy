package com.ponyo.thewitchslegacy.client.gui;

import com.ponyo.thewitchslegacy.menu.PoppetShelfMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PoppetShelfScreen extends AbstractContainerScreen<PoppetShelfMenu> {
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 132;

    public PoppetShelfScreen(PoppetShelfMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PANEL_WIDTH;
        this.imageHeight = PANEL_HEIGHT;
        this.inventoryLabelY = 1000;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF221A16);
        guiGraphics.fill(left + 1, top + 1, left + this.imageWidth - 1, top + this.imageHeight - 1, 0xFF6F5434);
        guiGraphics.fill(left + 5, top + 14, left + this.imageWidth - 5, top + 38, 0xFF2B2118);
        guiGraphics.fill(left + 5, top + 46, left + this.imageWidth - 5, top + 126, 0xFF2B2118);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
