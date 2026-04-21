package com.ponyo.thewitchslegacy.entity.client;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.entity.MandrakeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class MandrakeRenderer extends MobRenderer<MandrakeEntity, LivingEntityRenderState, MandrakeModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "textures/entities/mandrake.png");

    public MandrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new MandrakeModel(context.bakeLayer(MandrakeModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
