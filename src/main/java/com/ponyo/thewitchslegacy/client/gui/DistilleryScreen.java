package com.ponyo.thewitchslegacy.client.gui;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.menu.DistilleryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class DistilleryScreen extends AbstractContainerScreen<DistilleryMenu> {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "textures/gui/distillery_gui.png");
    private static final Identifier JAR_SLOT_OVERLAY = Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "textures/gui/jar_slot_overlay.png");
    private static final Identifier BREW_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/brewing_stand/brew_progress");
    private static final Identifier BUBBLES_SPRITE = Identifier.withDefaultNamespace("container/brewing_stand/bubbles");
    private static final int[] BUBBLE_LENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};
    private static final int BREW_PROGRESS_X = 123;
    private static final int BREW_PROGRESS_Y = 20;
    private static final int BUBBLES_X = 51;
    private static final int BUBBLES_Y = 14;
    private static final int JAR_SLOT_X = 25;
    private static final int JAR_SLOT_Y = 51;

    public DistilleryScreen(DistilleryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, left, top, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        renderBrewingProgress(guiGraphics, left + BREW_PROGRESS_X, top + BREW_PROGRESS_Y);
        renderBrewingBubbles(guiGraphics, left + BUBBLES_X, top + BUBBLES_Y);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, JAR_SLOT_OVERLAY, left + JAR_SLOT_X, top + JAR_SLOT_Y, 0.0F, 0.0F, 18, 18, 18, 18);
    }

    private void renderBrewingProgress(GuiGraphics guiGraphics, int x, int y) {
        if (!this.menu.isBrewing()) {
            return;
        }

        int progressHeight = (int) (28.0F * this.menu.getBrewingProgress());
        if (progressHeight > 0) {
            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    BREW_PROGRESS_SPRITE,
                    9,
                    28,
                    0,
                    0,
                    x,
                    y,
                    9,
                    progressHeight
            );
        }
    }

    private void renderBrewingBubbles(GuiGraphics guiGraphics, int x, int y) {
        if (!this.menu.isBrewing()) {
            return;
        }

        int bubbleHeight = BUBBLE_LENGTHS[this.menu.getBrewingTicksRemaining() / 2 % BUBBLE_LENGTHS.length];
        if (bubbleHeight > 0) {
            guiGraphics.blitSprite(
                    RenderPipelines.GUI_TEXTURED,
                    BUBBLES_SPRITE,
                    12,
                    29,
                    0,
                    29 - bubbleHeight,
                    x,
                    y + 29 - bubbleHeight,
                    12,
                    bubbleHeight
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
