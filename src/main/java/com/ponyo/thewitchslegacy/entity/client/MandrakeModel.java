package com.ponyo.thewitchslegacy.entity.client;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class MandrakeModel extends EntityModel<LivingEntityRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "mandrake"),
            "main"
    );

    private final ModelPart head;
    private final ModelPart eyes;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart flower;
    private final ModelPart leaves;

    public MandrakeModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.eyes = this.head.getChild("eyes");
        this.body = root.getChild("body");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.flower = root.getChild("flower");
        this.leaves = root.getChild("leaves");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30).addBox(-1.5F, -4.0F, -1.6F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 16.0F, 0.0F)
        );

        PartDefinition eyes = head.addOrReplaceChild("eyes", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        eyes.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                .texOffs(0, 0).addBox(1.0631F, 0.1275F, -1.1F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2.4315F, -1.1F, 0.0F, 0.0F, 0.3054F)
        );
        eyes.addOrReplaceChild("cube_r2", CubeListBuilder.create()
                .texOffs(0, 0).mirror().addBox(-2.0631F, 0.1275F, -1.1F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, -2.4315F, -1.1F, 0.0F, 0.0F, -0.3054F)
        );

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 9).addBox(-2.5F, -3.0F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 19.0F, 0.0F)
        );

        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 41).addBox(-3.0F, -0.6667F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(-4, 43).addBox(-5.5F, -0.1667F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.5F, 16.6667F, 0.0F)
        );
        leftArm.addOrReplaceChild("cube_r3", CubeListBuilder.create()
                .texOffs(-3, 47).addBox(-3.0F, 0.0F, -2.0F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.5F, -0.1667F, 0.0F, 1.5708F, 0.0F, 0.0F)
        );

        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(-4, 43).mirror().addBox(0.5F, -0.1667F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 41).mirror().addBox(0.0F, -0.6667F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(2.5F, 16.6667F, 0.0F)
        );
        rightArm.addOrReplaceChild("cube_r4", CubeListBuilder.create()
                .texOffs(-3, 47).mirror().addBox(-2.0F, 0.0F, -2.0F, 5.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(2.5F, -0.1667F, 0.0F, 1.5708F, 0.0F, 0.0F)
        );

        root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 38).addBox(0.0F, 3.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(-1.0F, 2.0F, -1.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 23).addBox(-1.5F, -1.0F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 20.0F, 0.0F)
        );

        root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 38).addBox(-1.0F, 3.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).mirror().addBox(-1.0F, 2.0F, -1.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 23).mirror().addBox(-1.5F, -1.0F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(2.0F, 20.0F, 0.0F)
        );

        PartDefinition flower = root.addOrReplaceChild("flower", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition largeFlower = flower.addOrReplaceChild("large_flower", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7373F, 0.3419F, -0.2766F));
        largeFlower.addOrReplaceChild("cube_r5", CubeListBuilder.create()
                .texOffs(25, 0).mirror().addBox(-1.0F, -0.1463F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.2343F, -2.9804F, 0.8235F, 0.0F, -0.7854F, 0.0873F)
        );
        largeFlower.addOrReplaceChild("cube_r6", CubeListBuilder.create()
                .texOffs(22, 3).addBox(-2.4577F, 0.9575F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.2343F, -2.9804F, 0.8235F, -0.6155F, -0.5236F, 1.0426F)
        );
        largeFlower.addOrReplaceChild("cube_r7", CubeListBuilder.create()
                .texOffs(22, 3).mirror().addBox(0.4577F, 0.9575F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.2343F, -2.9804F, 0.8235F, -0.6155F, 0.5236F, -0.8681F)
        );
        largeFlower.addOrReplaceChild("cube_r8", CubeListBuilder.create()
                .texOffs(22, 3).mirror().addBox(0.4577F, 0.9575F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.2343F, -2.9804F, 0.8235F, 0.6155F, -0.5236F, -0.8681F)
        );
        largeFlower.addOrReplaceChild("cube_r9", CubeListBuilder.create()
                .texOffs(22, 3).addBox(-2.4577F, 0.9575F, -1.5F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.2343F, -2.9804F, 0.8235F, 0.6155F, 0.5236F, 1.0426F)
        );

        PartDefinition smallFlower = flower.addOrReplaceChild("small_flower", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        smallFlower.addOrReplaceChild("cube_r10", CubeListBuilder.create()
                .texOffs(23, 4).mirror().addBox(-0.3186F, 0.6011F, -1.6761F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(3.0963F, -3.3536F, -0.6961F, 3.0069F, 0.8714F, -1.4848F)
        );
        smallFlower.addOrReplaceChild("cube_r11", CubeListBuilder.create()
                .texOffs(23, 4).mirror().addBox(-0.3187F, 0.6012F, -1.6762F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(3.0963F, -3.3536F, -0.6961F, -0.8306F, 0.5904F, 0.4213F)
        );
        smallFlower.addOrReplaceChild("cube_r12", CubeListBuilder.create()
                .texOffs(23, 4).mirror().addBox(-0.3186F, 0.6011F, -1.6761F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(3.0963F, -3.3536F, -0.6961F, 0.1175F, -0.6951F, 0.106F)
        );
        smallFlower.addOrReplaceChild("cube_r13", CubeListBuilder.create()
                .texOffs(26, 1).addBox(-0.4963F, -0.2464F, -0.5039F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0963F, -3.3536F, -0.6961F, 1.5911F, -1.4457F, -0.6203F)
        );
        smallFlower.addOrReplaceChild("cube_r14", CubeListBuilder.create()
                .texOffs(23, 4).mirror().addBox(-0.3187F, 0.6012F, -1.6762F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(3.0963F, -3.3536F, -0.6961F, 2.0747F, -0.4466F, -1.5032F)
        );
        smallFlower.addOrReplaceChild("cube_r15", CubeListBuilder.create()
                .texOffs(23, 4).mirror().addBox(-0.3186F, 0.6011F, -1.6761F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-1.8037F, -3.1536F, 2.5039F, -1.1837F, -0.5853F, -0.8952F)
        );
        smallFlower.addOrReplaceChild("cube_r16", CubeListBuilder.create()
                .texOffs(23, 4).mirror().addBox(-0.3187F, 0.6012F, -1.6762F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-1.8037F, -3.1536F, 2.5039F, -3.0338F, -1.2249F, 1.9436F)
        );
        smallFlower.addOrReplaceChild("cube_r17", CubeListBuilder.create()
                .texOffs(23, 4).mirror().addBox(-0.3186F, 0.6011F, -1.6761F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-1.8037F, -3.1536F, 2.5039F, -2.9715F, -0.1125F, 3.0714F)
        );
        smallFlower.addOrReplaceChild("cube_r18", CubeListBuilder.create()
                .texOffs(26, 1).addBox(-0.4963F, -0.2464F, -0.5039F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.8037F, -3.1536F, 2.5039F, -2.1417F, 0.66F, -3.0597F)
        );
        smallFlower.addOrReplaceChild("cube_r19", CubeListBuilder.create()
                .texOffs(23, 4).mirror().addBox(-0.3187F, 0.6012F, -1.6762F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-1.8037F, -3.1536F, 2.5039F, -1.9187F, 0.2797F, -1.9396F)
        );

        PartDefinition leaves = root.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));
        leaves.addOrReplaceChild("cube_r20", CubeListBuilder.create()
                .texOffs(25, 12).mirror().addBox(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-0.6F, 0.0F, -0.3F, 1.8118F, -1.147F, -2.825F)
        );
        leaves.addOrReplaceChild("cube_r21", CubeListBuilder.create()
                .texOffs(25, 9).addBox(-2.0F, -6.6955F, -1.5307F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.6F, 0.0F, -0.3F, 1.4191F, -1.147F, -2.825F)
        );
        leaves.addOrReplaceChild("cube_r22", CubeListBuilder.create()
                .texOffs(25, 7).addBox(-2.0F, -7.6001F, -3.9765F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.6F, 0.0F, -0.3F, 1.0264F, -1.147F, -2.825F)
        );
        leaves.addOrReplaceChild("cube_r23", CubeListBuilder.create()
                .texOffs(25, 7).addBox(-2.0F, -7.6001F, -3.9765F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.7F, 0.0F, -0.6F, -1.0036F, -1.3963F, -0.2182F)
        );
        leaves.addOrReplaceChild("cube_r24", CubeListBuilder.create()
                .texOffs(25, 9).addBox(-2.0F, -6.6955F, -1.5307F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.7F, 0.0F, -0.6F, -0.6109F, -1.3963F, -0.2182F)
        );
        leaves.addOrReplaceChild("cube_r25", CubeListBuilder.create()
                .texOffs(25, 12).addBox(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.7F, 0.0F, -0.6F, -0.2182F, -1.3963F, -0.2182F)
        );
        leaves.addOrReplaceChild("cube_r26", CubeListBuilder.create()
                .texOffs(25, 7).addBox(-2.0F, -7.6001F, -3.9765F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 1.0572F, 0.9614F, 3.1212F)
        );
        leaves.addOrReplaceChild("cube_r27", CubeListBuilder.create()
                .texOffs(25, 9).addBox(-2.0F, -6.6955F, -1.5307F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 1.4499F, 0.9614F, 3.1212F)
        );
        leaves.addOrReplaceChild("cube_r28", CubeListBuilder.create()
                .texOffs(25, 12).mirror().addBox(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 1.8426F, 0.9614F, 3.1212F)
        );
        leaves.addOrReplaceChild("cube_r29", CubeListBuilder.create()
                .texOffs(25, 7).addBox(-2.0F, -7.6001F, -3.9765F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 1.7231F, 0.7212F, 3.0737F)
        );
        leaves.addOrReplaceChild("cube_r30", CubeListBuilder.create()
                .texOffs(25, 9).addBox(-2.0F, -6.6955F, -1.5307F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 2.1158F, 0.7212F, 3.0737F)
        );
        leaves.addOrReplaceChild("cube_r31", CubeListBuilder.create()
                .texOffs(25, 12).addBox(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 2.5085F, 0.7212F, 3.0737F)
        );
        leaves.addOrReplaceChild("cube_r32", CubeListBuilder.create()
                .texOffs(25, 12).addBox(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 0.0F, 0.4F, -0.9845F, -0.1886F, -0.1126F)
        );
        leaves.addOrReplaceChild("cube_r33", CubeListBuilder.create()
                .texOffs(25, 9).mirror().addBox(-2.0F, -6.6955F, -1.5307F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-0.5F, 0.0F, 0.4F, -1.3772F, -0.1886F, -0.1126F)
        );
        leaves.addOrReplaceChild("cube_r34", CubeListBuilder.create()
                .texOffs(25, 7).mirror().addBox(-2.0F, -7.6001F, -3.9765F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-0.5F, 0.0F, 0.4F, -1.7699F, -0.1886F, -0.1126F)
        );
        leaves.addOrReplaceChild("cube_r35", CubeListBuilder.create()
                .texOffs(25, 12).addBox(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.4F, -1.5272F, 1.3515F, -0.5321F)
        );
        leaves.addOrReplaceChild("cube_r36", CubeListBuilder.create()
                .texOffs(25, 9).mirror().addBox(-2.0F, -6.6955F, -1.5307F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.4F, -1.9199F, 1.3515F, -0.5321F)
        );
        leaves.addOrReplaceChild("cube_r37", CubeListBuilder.create()
                .texOffs(25, 7).mirror().addBox(-2.0F, -7.6001F, -3.9765F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.4F, -2.3126F, 1.3515F, -0.5321F)
        );
        leaves.addOrReplaceChild("cube_r38", CubeListBuilder.create()
                .texOffs(25, 12).addBox(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.4F, -0.3215F, 0.5648F, 0.0754F)
        );
        leaves.addOrReplaceChild("cube_r39", CubeListBuilder.create()
                .texOffs(25, 9).addBox(-2.0F, -6.6955F, -1.5307F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.4F, -0.7142F, 0.5648F, 0.0754F)
        );
        leaves.addOrReplaceChild("cube_r40", CubeListBuilder.create()
                .texOffs(25, 7).mirror().addBox(-2.0F, -7.6001F, -3.9765F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.4F, -1.1069F, 0.5648F, 0.0754F)
        );
        leaves.addOrReplaceChild("cube_r41", CubeListBuilder.create()
                .texOffs(25, 7).mirror().addBox(-2.0F, -7.6001F, -3.9765F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, -0.7418F, 0.0F, 0.0F)
        );
        leaves.addOrReplaceChild("cube_r42", CubeListBuilder.create()
                .texOffs(25, 9).mirror().addBox(-2.0F, -6.6955F, -1.5307F, 4.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, -0.3491F, 0.0F, 0.0F)
        );
        leaves.addOrReplaceChild("cube_r43", CubeListBuilder.create()
                .texOffs(25, 12).mirror().addBox(-2.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.0436F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        float armSwingAmount = state.walkAnimationSpeed;
        float idleArmDrop = Mth.clamp(1.0F - armSwingAmount * 3.0F, 0.0F, 1.0F) * 0.35F;

        this.head.yRot = state.yRot * (Mth.PI / 180F) * 0.35F;
        this.head.xRot = state.xRot * (Mth.PI / 180F) * 0.2F;

        this.leftLeg.xRot = Mth.cos(state.walkAnimationPos * 1.1F) * armSwingAmount;
        this.rightLeg.xRot = Mth.cos(state.walkAnimationPos * 1.1F + Mth.PI) * armSwingAmount;
        this.leftArm.zRot = -idleArmDrop + Mth.cos(state.walkAnimationPos * 1.75F + 0.9F) * 0.85F * armSwingAmount;
        this.rightArm.zRot = idleArmDrop + Mth.cos(state.walkAnimationPos * 1.45F + 2.6F) * 0.7F * armSwingAmount;

        this.body.xRot = Mth.cos(state.ageInTicks * 0.15F) * 0.04F;
        this.flower.zRot = Mth.cos(state.ageInTicks * 0.1F) * 0.08F;
        this.leaves.zRot = Mth.sin(state.ageInTicks * 0.08F) * 0.1F;
    }
}
