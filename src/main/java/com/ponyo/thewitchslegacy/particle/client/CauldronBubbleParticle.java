package com.ponyo.thewitchslegacy.particle.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class CauldronBubbleParticle extends SingleQuadParticle {
    protected CauldronBubbleParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xd,
            double yd,
            double zd,
            TextureAtlasSprite sprite
    ) {
        super(level, x, y, z, sprite);
        this.setSize(0.02F, 0.02F);
        this.quadSize = this.quadSize * (this.random.nextFloat() * 0.35F + 0.2F);
        this.xd = xd * 0.15F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.004F;
        this.yd = yd * 0.15F + 0.004F + this.random.nextFloat() * 0.004F;
        this.zd = zd * 0.15F + (this.random.nextFloat() * 2.0F - 1.0F) * 0.004F;
        this.lifetime = 6 + this.random.nextInt(5);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.yd += 0.0008;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.8F;
            this.yd *= 0.8F;
            this.zd *= 0.8F;
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xd,
                double yd,
                double zd,
                RandomSource random
        ) {
            return new CauldronBubbleParticle(level, x, y, z, xd, yd, zd, this.sprites.get(random));
        }
    }
}
