package com.ponyo.thewitchslegacy.client.gui;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.menu.WitchOvenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

public class WitchOvenScreen extends AbstractContainerScreen<WitchOvenMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier LIT_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier BURN_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private static final Identifier CUSTOM_SLOT_TEXTURE = Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "textures/gui/jar_slot_overlay.png");

    public WitchOvenScreen(WitchOvenMenu menu, Inventory inventory, Component title) {
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
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, left, top, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        if (this.menu.isLit()) {
            int litHeight = Mth.ceil(this.menu.getLitProgress() * 13.0F) + 1;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - litHeight, left + 56, top + 36 + 14 - litHeight, 14, litHeight);
        }

        int burnWidth = Mth.ceil(this.menu.getBurnProgress() * 24.0F);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_SPRITE, 24, 16, 0, 0, left + 79, top + 34, burnWidth, 16);

        renderCustomExtraSlot(guiGraphics, left + 82, top + 52);
        renderPlainExtraSlot(guiGraphics, left + 141, top + 34);
    }

    private static void renderCustomExtraSlot(GuiGraphics guiGraphics, int x, int y) {
        renderVanillaStyleSlot(guiGraphics, x, y);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CUSTOM_SLOT_TEXTURE, x, y, 0.0F, 0.0F, 18, 18, 18, 18);
    }

    private static void renderPlainExtraSlot(GuiGraphics guiGraphics, int x, int y) {
        renderVanillaStyleSlot(guiGraphics, x, y);
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
