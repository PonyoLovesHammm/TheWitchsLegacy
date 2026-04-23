package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

final class RitualVisuals {
    private static final DustParticleOptions CASTING_PARTICLE = DustParticleOptions.REDSTONE;

    private RitualVisuals() {
    }

    static void playFailureSmoke(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 5, 0.2, 0.0, 0.2, 0.02);
    }

    static void playCastingParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(CASTING_PARTICLE, pos.getX() + 0.5, pos.getY() + 0.1875, pos.getZ() + 0.5, 1, 0.125, 0.04, 0.125, 0.04);
    }

    static void playItemConsumedEffects(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.DUST_PLUME, x, y, z, 18, 0.18, 0.08, 0.18, 0.02);
        level.playSound(null, x, y, z, SoundEvents.PUFFER_FISH_BLOW_OUT, SoundSource.BLOCKS, 5.0f, 1.25f);
    }
}
