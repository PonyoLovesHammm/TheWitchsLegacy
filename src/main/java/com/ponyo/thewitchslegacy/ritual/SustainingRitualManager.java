package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SustainingRitualManager {
    private static final List<ActiveSustainingRitual> ACTIVE_RITUALS = new ArrayList<>();

    private SustainingRitualManager() {
    }

    public static void start(ServerLevel level, BlockPos centerPos, String ritualId, int durationTicks,
                             int altarPowerPerSecond, SustainingTick tick) {
        start(level, centerPos, ritualId, durationTicks, altarPowerPerSecond, tick, SustainingStop.NOOP);
    }

    public static void start(ServerLevel level, BlockPos centerPos, String ritualId, int durationTicks,
                             int altarPowerPerSecond, SustainingTick tick, SustainingStop stop) {
        long endTick = durationTicks <= 0 ? Long.MAX_VALUE : level.getGameTime() + durationTicks;
        ACTIVE_RITUALS.add(new ActiveSustainingRitual(
                ritualId,
                level.dimension(),
                centerPos.immutable(),
                endTick,
                altarPowerPerSecond,
                tick,
                stop
        ));
    }

    public static boolean isActive(ServerLevel level, BlockPos centerPos) {
        for (ActiveSustainingRitual activeRitual : ACTIVE_RITUALS) {
            if (activeRitual.dimension().equals(level.dimension()) && activeRitual.centerPos().equals(centerPos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean cancelActive(ServerLevel level, BlockPos centerPos, ServerPlayer player) {
        boolean cancelled = false;
        Iterator<ActiveSustainingRitual> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            ActiveSustainingRitual activeRitual = iterator.next();
            if (!activeRitual.dimension().equals(level.dimension()) || !activeRitual.centerPos().equals(centerPos)) {
                continue;
            }

            stopRitual(activeRitual, level);
            iterator.remove();
            cancelled = true;
        }

        if (cancelled) {
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.ritual_cancelled"), true);
            RitualVisuals.playFailureSmoke(level, centerPos);
        }
        return cancelled;
    }

    public static boolean cancelActiveFromBrokenGlyph(ServerLevel level, BlockPos centerPos) {
        boolean cancelled = false;
        Iterator<ActiveSustainingRitual> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            ActiveSustainingRitual activeRitual = iterator.next();
            if (!activeRitual.dimension().equals(level.dimension()) || !activeRitual.centerPos().equals(centerPos)) {
                continue;
            }

            stopRitual(activeRitual, level);
            iterator.remove();
            cancelled = true;
        }

        if (cancelled) {
            RitualVisuals.playFailureSmoke(level, centerPos);
        }
        return cancelled;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_RITUALS.isEmpty()) {
            return;
        }

        long gameTime = event.getServer().overworld().getGameTime();
        Iterator<ActiveSustainingRitual> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            ActiveSustainingRitual activeRitual = iterator.next();
            ServerLevel level = event.getServer().getLevel(activeRitual.dimension());
            if (level == null || gameTime >= activeRitual.endTick()) {
                if (level != null) {
                    stopRitual(activeRitual, level);
                }
                iterator.remove();
                continue;
            }

            if (activeRitual.altarPowerPerSecond() > 0 && gameTime % 20L == 0L) {
                if (!consumeSustainingPower(level, activeRitual)) {
                    stopRitual(activeRitual, level);
                    iterator.remove();
                    RitualVisuals.playFailureSmoke(level, activeRitual.centerPos());
                    continue;
                }
            }

            if (gameTime % 4L == 0L) {
                spawnSustainingParticles(level, activeRitual.centerPos());
            }
            activeRitual.tick().tick(level, activeRitual.centerPos(), gameTime);
        }
    }

    private static boolean consumeSustainingPower(ServerLevel level, ActiveSustainingRitual activeRitual) {
        var altar = RitualAltarSupport.findBestSupportingAltar(
                level,
                activeRitual.centerPos(),
                activeRitual.altarPowerPerSecond()
        );
        return altar != null && altar.consumePower(activeRitual.altarPowerPerSecond());
    }

    private static void stopRitual(ActiveSustainingRitual activeRitual, ServerLevel level) {
        activeRitual.stop().stop(level, activeRitual.centerPos());
    }

    private static void spawnSustainingParticles(ServerLevel level, BlockPos centerPos) {
        level.sendParticles(
                ParticleTypes.COMPOSTER,
                centerPos.getX() + 0.5D,
                centerPos.getY() + 0.08D,
                centerPos.getZ() + 0.5D,
                8,
                0.18D,
                0.03D,
                0.18D,
                0.02D
        );
    }

    @FunctionalInterface
    public interface SustainingTick {
        void tick(ServerLevel level, BlockPos centerPos, long gameTime);
    }

    @FunctionalInterface
    public interface SustainingStop {
        SustainingStop NOOP = (level, centerPos) -> {
        };

        void stop(ServerLevel level, BlockPos centerPos);
    }

    private record ActiveSustainingRitual(String ritualId, ResourceKey<Level> dimension, BlockPos centerPos,
                                          long endTick, int altarPowerPerSecond, SustainingTick tick,
                                          SustainingStop stop) {
    }
}
