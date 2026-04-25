package com.ponyo.thewitchslegacy.ritual;

import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class RitualEffects {
    private static final double OUTPUT_VERTICAL_VELOCITY = 0.5D;
    private static final double OUTPUT_HORIZONTAL_OFFSET_MAX = 1.0D;
    private static final double OUTPUT_HORIZONTAL_VELOCITY_MAX = 0.08D;

    private RitualEffects() {
    }

    public static void playCompletionEffects(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 35, 0.35, 0.35, 0.35, 0.08);
        level.playSound(null, pos, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    public static void spawnItem(ServerLevel level, BlockPos pos, Item item) {
        spawnItem(level, pos, new ItemStack(item));
    }

    public static void spawnOutputItem(ServerLevel level, BlockPos pos, Item item) {
        spawnOutputItem(level, pos, new ItemStack(item));
    }

    public static void spawnOutputItem(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemEntity itemEntity = createItemEntity(level, pos, stack);
        double xOffset = randomHorizontalOffset(level);
        double zOffset = randomHorizontalOffset(level);
        itemEntity.setPos(itemEntity.getX() + xOffset, itemEntity.getY() - 1.5D, itemEntity.getZ() + zOffset);
        itemEntity.setDeltaMovement(randomHorizontalVelocity(level), OUTPUT_VERTICAL_VELOCITY, randomHorizontalVelocity(level));
        level.addFreshEntity(itemEntity);
    }

    public static void spawnItem(ServerLevel level, BlockPos pos, ItemStack stack) {
        level.addFreshEntity(createItemEntity(level, pos, stack));
    }

    public static void spawnChargedStoneRemainderIfNeeded(ServerLevel level, BlockPos pos, List<RitualItemRequirement> itemRequirements) {
        boolean usesChargedStone = itemRequirements.stream()
                .anyMatch(requirement -> requirement.item() == ModItems.INFUSED_STONE_CHARGED.get() && requirement.consume());
        if (usesChargedStone) {
            spawnOutputItem(level, pos, ModItems.INFUSED_STONE.get());
        }
    }

    private static ItemEntity createItemEntity(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(
                level,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                stack
        );
        itemEntity.setDefaultPickUpDelay();
        return itemEntity;
    }

    private static double randomHorizontalVelocity(ServerLevel level) {
        return (level.random.nextDouble() * 2.0D - 1.0D) * OUTPUT_HORIZONTAL_VELOCITY_MAX;
    }

    private static double randomHorizontalOffset(ServerLevel level) {
        return (level.random.nextDouble() * 2.0D - 1.0D) * OUTPUT_HORIZONTAL_OFFSET_MAX;
    }
}
