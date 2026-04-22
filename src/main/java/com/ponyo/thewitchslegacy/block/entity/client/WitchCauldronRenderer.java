package com.ponyo.thewitchslegacy.block.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ponyo.thewitchslegacy.block.custom.WitchCauldron;
import com.ponyo.thewitchslegacy.block.entity.WitchCauldronBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WitchCauldronRenderer implements BlockEntityRenderer<WitchCauldronBlockEntity, WitchCauldronRenderState> {
    private static final Material WATER_MATERIAL = new Material(
            TextureAtlas.LOCATION_BLOCKS,
            Identifier.withDefaultNamespace("block/water_still")
    );
    private static final float LIQUID_MIN = 3.0F / 16.0F;
    private static final float LIQUID_MAX = 13.0F / 16.0F;
    private static final float SURFACE_OFFSET = 0.001F;

    private final MaterialSet materials;

    public WitchCauldronRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
    }

    @Override
    public WitchCauldronRenderState createRenderState() {
        return new WitchCauldronRenderState();
    }

    @Override
    public void extractRenderState(
            WitchCauldronBlockEntity blockEntity,
            WitchCauldronRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
        renderState.level = blockEntity.getBlockState().getValue(WitchCauldron.LEVEL);
        renderState.liquidColor = blockEntity.getLiquidColor();
    }

    @Override
    public void submit(
            WitchCauldronRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        if (renderState.level <= 0) {
            return;
        }

        TextureAtlasSprite waterSprite = this.materials.get(WATER_MATERIAL);
        float surfaceY = getSurfaceHeight(renderState.level) + SURFACE_OFFSET;
        int color = ARGB.color(150, renderState.liquidColor);
        float minU = waterSprite.getU0();
        float maxU = waterSprite.getU1();
        float minV = waterSprite.getV0();
        float maxV = waterSprite.getV1();

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityNoOutline(TextureAtlas.LOCATION_BLOCKS), (pose, vertexConsumer) -> {
            addVertex(vertexConsumer, pose, LIQUID_MIN, surfaceY, LIQUID_MAX, minU, maxV, renderState.lightCoords, color);
            addVertex(vertexConsumer, pose, LIQUID_MAX, surfaceY, LIQUID_MAX, maxU, maxV, renderState.lightCoords, color);
            addVertex(vertexConsumer, pose, LIQUID_MAX, surfaceY, LIQUID_MIN, maxU, minV, renderState.lightCoords, color);
            addVertex(vertexConsumer, pose, LIQUID_MIN, surfaceY, LIQUID_MIN, minU, minV, renderState.lightCoords, color);

            // Draw the underside as well so the surface stays readable through transparency from low view angles.
            addVertex(vertexConsumer, pose, LIQUID_MIN, surfaceY, LIQUID_MIN, minU, minV, renderState.lightCoords, color);
            addVertex(vertexConsumer, pose, LIQUID_MAX, surfaceY, LIQUID_MIN, maxU, minV, renderState.lightCoords, color);
            addVertex(vertexConsumer, pose, LIQUID_MAX, surfaceY, LIQUID_MAX, maxU, maxV, renderState.lightCoords, color);
            addVertex(vertexConsumer, pose, LIQUID_MIN, surfaceY, LIQUID_MAX, minU, maxV, renderState.lightCoords, color);
        });
    }

    private static float getSurfaceHeight(int level) {
        return switch (level) {
            case 1 -> 7.0F / 16.0F;
            case 2 -> 9.0F / 16.0F;
            default -> 11.0F / 16.0F;
        };
    }

    private static void addVertex(
            VertexConsumer vertexConsumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int lightCoords,
            int color
    ) {
        vertexConsumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightCoords)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
