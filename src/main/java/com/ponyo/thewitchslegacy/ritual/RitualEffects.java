package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class RitualEffects {
    private RitualEffects() {
    }

    public static void playCompletionEffects(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 35, 0.35, 0.35, 0.35, 0.08);
        level.playSound(null, pos, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public static void spawnItem(ServerLevel level, BlockPos pos, Item item) {
        spawnItem(level, pos, new ItemStack(item));
    }

    public static void spawnItem(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                stack
        );
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }
}
