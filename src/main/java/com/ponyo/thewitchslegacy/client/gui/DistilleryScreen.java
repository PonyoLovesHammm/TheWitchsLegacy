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
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/brewing_stand.png");
    private static final Identifier BUBBLES_SPRITE = Identifier.withDefaultNamespace("container/brewing_stand/bubbles");
    private static final Identifier JAR_SLOT_OVERLAY = Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "textures/gui/witch_oven_slot.png");
    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int PANEL_DARK = 0xFF555555;
    private static final int[] BUBBLE_LENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};
    private static final int SLOT_X_INPUT = 90;
    private static final int SLOT_Y_INPUT_A = 15;
    private static final int SLOT_Y_INPUT_B = 33;
    private static final int SLOT_Y_JAR = 55;
    private static final int SLOT_X_OUTPUT_A = 128;
    private static final int SLOT_X_OUTPUT_B = 146;
    private static final int SLOT_Y_OUTPUT_A = 24;
    private static final int SLOT_Y_OUTPUT_B = 42;

    public DistilleryScreen(DistilleryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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

        // Keep the vanilla inventory region, but replace the brewing stand machine area.
        guiGraphics.fill(left + 8, top + 14, left + 168, top + 78, PANEL_COLOR);
        renderBrewingBubbles(guiGraphics, left + 54, top + 19);

        renderVanillaStyleSlot(guiGraphics, left + SLOT_X_INPUT, top + SLOT_Y_INPUT_A);
        renderVanillaStyleSlot(guiGraphics, left + SLOT_X_INPUT, top + SLOT_Y_INPUT_B);
        renderJarSlot(guiGraphics, left + SLOT_X_INPUT, top + SLOT_Y_JAR);

        renderArrow(guiGraphics, left + 113, top + 37);

        renderVanillaStyleSlot(guiGraphics, left + SLOT_X_OUTPUT_A, top + SLOT_Y_OUTPUT_A);
        renderVanillaStyleSlot(guiGraphics, left + SLOT_X_OUTPUT_B, top + SLOT_Y_OUTPUT_A);
        renderVanillaStyleSlot(guiGraphics, left + SLOT_X_OUTPUT_A, top + SLOT_Y_OUTPUT_B);
        renderVanillaStyleSlot(guiGraphics, left + SLOT_X_OUTPUT_B, top + SLOT_Y_OUTPUT_B);
    }

    private void renderBrewingBubbles(GuiGraphics guiGraphics, int x, int y) {
        int frame = (int) (this.minecraft.level.getGameTime() / 2L % BUBBLE_LENGTHS.length);
        int bubbleHeight = BUBBLE_LENGTHS[frame];
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

    private static void renderJarSlot(GuiGraphics guiGraphics, int x, int y) {
        renderVanillaStyleSlot(guiGraphics, x, y);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, JAR_SLOT_OVERLAY, x, y, 0.0F, 0.0F, 18, 18, 18, 18);
    }

    private static void renderArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y + 6, x + 11, y + 10, 0xFF8B8B8B);
        guiGraphics.fill(x + 11, y + 3, x + 14, y + 13, 0xFF8B8B8B);
        guiGraphics.fill(x + 14, y + 5, x + 17, y + 11, 0xFF8B8B8B);
        guiGraphics.fill(x + 17, y + 7, x + 19, y + 9, 0xFF8B8B8B);
        guiGraphics.fill(x, y + 10, x + 12, y + 11, PANEL_DARK);
    }

    private static void renderVanillaStyleSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
        guiGraphics.fill(x, y, x + 18, y + 1, 0xFF373737);
        guiGraphics.fill(x, y + 1, x + 1, y + 18, 0xFF373737);
        guiGraphics.fill(x + 17, y, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x, y + 17, x + 17, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x + 17, y, x + 18, y + 1, 0xFF8B8B8B);
        guiGraphics.fill(x, y + 17, x + 1, y + 18, 0xFF8B8B8B);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
