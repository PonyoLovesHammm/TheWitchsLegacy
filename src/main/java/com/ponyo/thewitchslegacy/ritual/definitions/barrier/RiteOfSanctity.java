package com.ponyo.thewitchslegacy.ritual.definitions.barrier;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualPatterns;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingSize;
import com.ponyo.thewitchslegacy.ritual.SustainingRitualManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RiteOfSanctity {
    private static final int ALTAR_POWER_COST = 1000;
    private static final String DISPLAY_NAME_KEY = "ritual.thewitchslegacy.rite_of_sanctity";
    private static final String RITUAL_ID = "rite_of_sanctity";
    private static final double VERTICAL_RADIUS = 6.0D;
    private static final double OUTSIDE_PADDING = 0.75D;
    private static final int RING_PARTICLE_INTERVAL_TICKS = 20;
    private static final int RING_PARTICLES_PER_PULSE = 8;

    private RiteOfSanctity() {
    }

    public static List<RitualDefinition> createAll() {
        return List.of(
                createSmall(),
                createMedium(),
                createLarge()
        );
    }

    private static RitualDefinition createSmall() {
        return create("rite_of_sanctity_small", RitualRingRequirement.small(ModBlocks.WHITE_GLYPH.get()), RitualRingSize.SMALL, 1, 3.0D, 20);
    }

    private static RitualDefinition createMedium() {
        return create("rite_of_sanctity_medium", RitualRingRequirement.medium(ModBlocks.WHITE_GLYPH.get()), RitualRingSize.MEDIUM, 2, 5.0D, 25);
    }

    private static RitualDefinition createLarge() {
        return create("rite_of_sanctity_large", RitualRingRequirement.large(ModBlocks.WHITE_GLYPH.get()), RitualRingSize.LARGE, 3, 7.0D, 30);
    }

    private static RitualDefinition create(String id, RitualRingRequirement ringRequirement, RitualRingSize ringSize,
                                           int redstoneCount, double radius, int altarPowerPerSecond) {
        List<RitualItemRequirement> itemRequirements = List.of(
                new RitualItemRequirement(Items.FEATHER, 1, true),
                new RitualItemRequirement(Items.REDSTONE, redstoneCount, true)
        );

        return new RitualDefinition(
                id,
                DISPLAY_NAME_KEY,
                List.of(ringRequirement),
                itemRequirements,
                ALTAR_POWER_COST,
                (level, centerPos, player, consumedItems) -> {
                    SustainingRitualManager.start(
                            level,
                            centerPos,
                            RITUAL_ID,
                            0,
                            altarPowerPerSecond,
                            (activeLevel, activeCenterPos, gameTime) -> tickBarrier(activeLevel, activeCenterPos, radius, ringSize, gameTime)
                    );
                    RitualEffects.playCompletionEffects(level, centerPos.above(1));
                    return null;
                }
        );
    }

    private static void tickBarrier(ServerLevel level, BlockPos centerPos, double radius, RitualRingSize ringSize, long gameTime) {
        for (Mob mob : getNearbyHostiles(level, centerPos, radius)) {
            if (isInsideBarrier(mob.position(), centerPos, radius)) {
                repelFromBarrier(mob, centerPos, radius);
            }
        }

        if (gameTime % RING_PARTICLE_INTERVAL_TICKS == 0L) {
            spawnRingParticles(level, centerPos, ringSize);
        }
    }

    private static List<Mob> getNearbyHostiles(ServerLevel level, BlockPos centerPos, double radius) {
        AABB bounds = new AABB(centerPos).inflate(radius + OUTSIDE_PADDING + 1.0D, VERTICAL_RADIUS, radius + OUTSIDE_PADDING + 1.0D);
        return level.getEntitiesOfClass(Mob.class, bounds, mob -> mob instanceof Enemy);
    }

    private static boolean isInsideBarrier(Vec3 position, BlockPos centerPos, double radius) {
        double centerX = centerPos.getX() + 0.5D;
        double centerZ = centerPos.getZ() + 0.5D;
        double dx = position.x() - centerX;
        double dz = position.z() - centerZ;
        return dx * dx + dz * dz <= radius * radius
                && Math.abs(position.y() - (centerPos.getY() + 0.5D)) <= VERTICAL_RADIUS;
    }

    private static void repelFromBarrier(Mob mob, BlockPos centerPos, double radius) {
        double centerX = centerPos.getX() + 0.5D;
        double centerZ = centerPos.getZ() + 0.5D;
        double dx = mob.getX() - centerX;
        double dz = mob.getZ() - centerZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 0.001D) {
            float angle = mob.level().random.nextFloat() * Mth.TWO_PI;
            dx = Mth.cos(angle);
            dz = Mth.sin(angle);
            distance = 1.0D;
        }

        double targetDistance = radius + OUTSIDE_PADDING;
        double targetX = centerX + dx / distance * targetDistance;
        double targetZ = centerZ + dz / distance * targetDistance;
        mob.getNavigation().stop();
        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y(), 0.0D);
        mob.teleportTo(targetX, mob.getY(), targetZ);
    }

    private static void spawnRingParticles(ServerLevel level, BlockPos centerPos, RitualRingSize ringSize) {
        List<BlockPos> ringOffsets = RitualPatterns.positionsFor(ringSize);
        for (int i = 0; i < RING_PARTICLES_PER_PULSE; i++) {
            BlockPos glyphPos = centerPos.offset(ringOffsets.get(level.random.nextInt(ringOffsets.size())));
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    glyphPos.getX() + 0.5D,
                    glyphPos.getY() + 0.35D,
                    glyphPos.getZ() + 0.5D,
                    2,
                    0.18D,
                    0.04D,
                    0.18D,
                    0.015D
            );
        }
    }

}
