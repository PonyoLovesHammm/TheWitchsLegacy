package com.ponyo.thewitchslegacy.ritual.definitions.summoning;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.custom.Glyph;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import com.ponyo.thewitchslegacy.ritual.WaitingRitualManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;

public final class DemonSummon {
    private static final String DEMON_SUMMON_1_ID = "demon_summon_1";
    private static final String DEMON_SUMMON_2_ID = "demon_summon_2";
    private static final String DISPLAY_NAME_KEY = "ritual.thewitchslegacy.demon_summon";
    private static final int ALTAR_POWER_COST = 3000;
    private static final int CLEAR_RADIUS = 3;
    private static final int CLEAR_HEIGHT = 4;
    private static final double SACRIFICE_RADIUS = 7.5D;

    private DemonSummon() {
    }

    public static RitualDefinition create() {
        return create(DEMON_SUMMON_1_ID, List.of(
                new RitualItemRequirement(ModItems.PRICKLE_THROUGH_THE_VEIL.get(), 1, true)
        ));
    }

    public static RitualDefinition createInfusedStone() {
        List<RitualItemRequirement> itemRequirements = List.of(
                new RitualItemRequirement(ModItems.PRICKLE_THROUGH_THE_VEIL.get(), 1, true),
                new RitualItemRequirement(ModItems.INFUSED_STONE.get(), 1, true)
        );

        return new RitualDefinition(
                DEMON_SUMMON_2_ID,
                DISPLAY_NAME_KEY,
                List.of(RitualRingRequirement.large(ModBlocks.FIERY_GLYPH.get())),
                itemRequirements,
                ALTAR_POWER_COST,
                (level, centerPos, player, consumedItems) -> {
                    summonPlaceholderDemon(level, centerPos);
                    RitualEffects.playCompletionEffects(level, centerPos.above(1));
                    return null;
                },
                (level, centerPos) -> true,
                (level, centerPos, player) -> isInteriorClear(level, centerPos)
                        ? null
                        : Component.translatable("message.thewitchslegacy.interior_must_be_clear")
        );
    }

    private static RitualDefinition create(String ritualId, List<RitualItemRequirement> itemRequirements) {
        return new RitualDefinition(
                ritualId,
                DISPLAY_NAME_KEY,
                List.of(RitualRingRequirement.large(ModBlocks.FIERY_GLYPH.get())),
                itemRequirements,
                ALTAR_POWER_COST,
                (level, centerPos, player, consumedItems) -> {
                    WaitingRitualManager.start(
                            level,
                            centerPos,
                            ritualId,
                            DemonSummon::tickWaiting,
                            WaitingRitualManager.WaitingStop.NOOP
                    );
                    return null;
                },
                (level, centerPos) -> !isWaiting(level, centerPos),
                (level, centerPos, player) -> isInteriorClear(level, centerPos)
                        ? null
                        : Component.translatable("message.thewitchslegacy.interior_must_be_clear")
        );
    }

    public static boolean isWaiting(ServerLevel level, BlockPos centerPos) {
        return WaitingRitualManager.isActive(level, centerPos);
    }

    public static boolean cancelWaiting(ServerLevel level, BlockPos centerPos) {
        return WaitingRitualManager.cancelActiveFromBrokenGlyph(level, centerPos);
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Villager villager) || !(villager.level() instanceof ServerLevel level)) {
            return;
        }

        WaitingRitualManager.completeFirst(level, new WaitingRitualManager.WaitingCompletion() {
            @Override
            public boolean matches(String ritualId, ServerLevel activeLevel, BlockPos centerPos) {
                return isDemonSummon(ritualId) && isSacrificeInsideCircle(villager.position(), centerPos);
            }

            @Override
            public void complete(String ritualId, ServerLevel activeLevel, BlockPos centerPos) {
                if (!isInteriorClear(activeLevel, centerPos)) {
                    WaitingRitualManager.playFailureSmoke(activeLevel, centerPos);
                    return;
                }

                summonPlaceholderDemon(activeLevel, centerPos);
                RitualEffects.playCompletionEffects(activeLevel, centerPos.above(1));
            }
        });
    }

    private static boolean isDemonSummon(String ritualId) {
        return DEMON_SUMMON_1_ID.equals(ritualId);
    }

    private static boolean isInteriorClear(ServerLevel level, BlockPos centerPos) {
        for (BlockPos pos : BlockPos.betweenClosed(
                centerPos.offset(-CLEAR_RADIUS, 0, -CLEAR_RADIUS),
                centerPos.offset(CLEAR_RADIUS, CLEAR_HEIGHT - 1, CLEAR_RADIUS))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getBlock() instanceof Glyph) {
                continue;
            }
            if (Block.isShapeFullBlock(state.getCollisionShape(level, pos))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSacrificeInsideCircle(Vec3 position, BlockPos centerPos) {
        double centerX = centerPos.getX() + 0.5D;
        double centerZ = centerPos.getZ() + 0.5D;
        double dx = position.x() - centerX;
        double dz = position.z() - centerZ;
        return dx * dx + dz * dz <= SACRIFICE_RADIUS * SACRIFICE_RADIUS
                && position.y() >= centerPos.getY()
                && position.y() <= centerPos.getY() + CLEAR_HEIGHT + 1.0D;
    }

    private static void summonPlaceholderDemon(ServerLevel level, BlockPos centerPos) {
        Cow cow = EntityType.COW.create(level, EntitySpawnReason.TRIGGERED);
        if (cow == null) {
            return;
        }

        cow.setCustomName(Component.literal("DEMON"));
        cow.setCustomNameVisible(true);
        cow.snapTo(centerPos.getX() + 0.5D, centerPos.getY() + 1.0D, centerPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(cow);
    }

    private static void spawnWaitingParticles(ServerLevel level, BlockPos centerPos) {
        level.sendParticles(
                ParticleTypes.PORTAL,
                centerPos.getX() + 0.5D,
                centerPos.getY() + 0.35D,
                centerPos.getZ() + 0.5D,
                12,
                0.35D,
                0.18D,
                0.35D,
                0.01D
        );
    }

    private static void tickWaiting(ServerLevel level, BlockPos centerPos, long gameTime) {
        if (gameTime % 4L == 0L) {
            spawnWaitingParticles(level, centerPos);
        }
    }
}
