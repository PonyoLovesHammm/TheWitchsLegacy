package com.ponyo.thewitchslegacy.block.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.custom.SpinningWheel;
import com.ponyo.thewitchslegacy.block.entity.SpinningWheelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;
import org.joml.Quaternionf;

public class SpinningWheelRenderer implements BlockEntityRenderer<SpinningWheelBlockEntity, SpinningWheelRenderState> {
    private static final float WHEEL_SPEED_MULTIPLIER = 1.3F;
    private static final float BOBBIN_SPEED_MULTIPLIER = 6.5F;

    public static final StandaloneModelKey<BlockStateModel> WHEEL_MODEL = new StandaloneModelKey<>(debugName("thewitchslegacy:spinning_wheel_wheel"));
    public static final StandaloneModelKey<BlockStateModel> BOBBIN_MODEL = new StandaloneModelKey<>(debugName("thewitchslegacy:spinning_wheel_bobbin"));
    public static final Identifier WHEEL_MODEL_ID = TheWitchsLegacy.id("block/spinning_wheel_wheel");
    public static final Identifier BOBBIN_MODEL_ID = TheWitchsLegacy.id("block/spinning_wheel_bobbin");

    public SpinningWheelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SpinningWheelRenderState createRenderState() {
        return new SpinningWheelRenderState();
    }

    @Override
    public void extractRenderState(
            SpinningWheelBlockEntity blockEntity,
            SpinningWheelRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);
        renderState.facing = blockEntity.getBlockState().getValue(SpinningWheel.FACING);

        double time = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() + partialTick : partialTick;
        float baseRotation = (float) (time * blockEntity.getVisualSpinSpeed());
        renderState.wheelRotation = baseRotation * WHEEL_SPEED_MULTIPLIER;
        renderState.bobbinRotation = baseRotation * BOBBIN_SPEED_MULTIPLIER;
    }

    @Override
    public void submit(
            SpinningWheelRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        BlockStateModel wheelModel = Minecraft.getInstance().getModelManager().getStandaloneModel(WHEEL_MODEL);
        BlockStateModel bobbinModel = Minecraft.getInstance().getModelManager().getStandaloneModel(BOBBIN_MODEL);
        if (wheelModel == null || bobbinModel == null) {
            return;
        }

        submitRotatingModel(submitNodeCollector, poseStack, wheelModel, renderState, renderState.wheelRotation, 8.0F / 16.0F, 10.5F / 16.0F, 11.5F / 16.0F);
        submitRotatingModel(submitNodeCollector, poseStack, bobbinModel, renderState, renderState.bobbinRotation, 8.0F / 16.0F, 8.75F / 16.0F, 3.1F / 16.0F);
    }

    private static void submitRotatingModel(
            SubmitNodeCollector submitNodeCollector,
            PoseStack poseStack,
            BlockStateModel model,
            SpinningWheelRenderState renderState,
            float xRotation,
            float originX,
            float originY,
            float originZ
    ) {
        poseStack.pushPose();
        applyFacingRotation(poseStack, renderState.facing);
        poseStack.translate(originX, originY, originZ);
        poseStack.mulPose(Axis.XP.rotation(xRotation));
        poseStack.translate(-originX, -originY, -originZ);
        submitNodeCollector.submitBlockModel(poseStack, RenderTypes.cutoutMovingBlock(), model, 1.0F, 1.0F, 1.0F, renderState.lightCoords, 0, 0);
        poseStack.popPose();
    }

    private static void applyFacingRotation(PoseStack poseStack, Direction facing) {
        Quaternionf rotation = switch (facing) {
            case SOUTH -> Axis.YP.rotationDegrees(180.0F);
            case WEST -> Axis.YP.rotationDegrees(90.0F);
            case EAST -> Axis.YP.rotationDegrees(-90.0F);
            default -> Axis.YP.rotationDegrees(0.0F);
        };
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(rotation);
        poseStack.translate(-0.5D, 0.0D, -0.5D);
    }

    private static ModelDebugName debugName(String name) {
        return () -> name;
    }
}
