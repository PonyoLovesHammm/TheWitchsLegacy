package com.ponyo.thewitchslegacy.client.gui;

import com.ponyo.thewitchslegacy.menu.AltarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class AltarScreen extends AbstractContainerScreen<AltarMenu> {
    private static final int PANEL_WIDTH = 176;
    private static final int PANEL_HEIGHT = 88;
    private static final int TEXT_COLOR = 0xFFF7F0DE;
    private static final int SUBTEXT_COLOR = 0xFF2B1B14;

    public AltarScreen(AltarMenu menu, Inventory inventory, Component title) {
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
        guiGraphics.fill(left + 1, top + 1, left + this.imageWidth - 1, top + this.imageHeight - 1, 0xFF4E3C2D);
        guiGraphics.fill(left + 5, top + 18, left + this.imageWidth - 5, top + this.imageHeight - 6, 0xFFCCB790);

        int barLeft = left + 12;
        int barTop = top + 34;
        int barWidth = 152;
        int fillWidth = this.menu.getMaxPower() <= 0
                ? 0
                : Mth.clamp((this.menu.getCurrentPower() * barWidth) / this.menu.getMaxPower(), 0, barWidth);

        guiGraphics.fill(barLeft, barTop, barLeft + barWidth, barTop + 10, 0xFF3B2B22);
        guiGraphics.fill(barLeft + 1, barTop + 1, barLeft + fillWidth, barTop + 9, 0xFF7CC96B);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component powerText = Component.translatable("gui.thewitchslegacy.altar.power", this.menu.getCurrentPower(), this.menu.getMaxPower());

        guiGraphics.drawString(this.font, this.title, this.titleLabelX, 6, SUBTEXT_COLOR, false);
        guiGraphics.drawString(
                this.font,
                powerText,
                (this.imageWidth - this.font.width(powerText)) / 2,
                22,
                TEXT_COLOR,
                false
        );
        guiGraphics.drawString(
                this.font,
                Component.translatable("gui.thewitchslegacy.altar.rate", this.menu.getAccumulationRate()),
                12,
                50,
                SUBTEXT_COLOR,
                false
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
