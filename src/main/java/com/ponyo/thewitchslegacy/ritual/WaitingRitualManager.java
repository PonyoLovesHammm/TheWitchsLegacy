package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class WaitingRitualManager {
    private static final List<ActiveWaitingRitual> ACTIVE_RITUALS = new ArrayList<>();

    private WaitingRitualManager() {
    }

    public static void start(ServerLevel level, BlockPos centerPos, String ritualId, WaitingTick tick, WaitingStop stop) {
        ACTIVE_RITUALS.add(new ActiveWaitingRitual(
                ritualId,
                level.dimension(),
                centerPos.immutable(),
                tick,
                stop
        ));
    }

    public static boolean isActive(ServerLevel level, BlockPos centerPos) {
        for (ActiveWaitingRitual activeRitual : ACTIVE_RITUALS) {
            if (activeRitual.dimension().equals(level.dimension()) && activeRitual.centerPos().equals(centerPos)) {
                return true;
            }
        }
        return false;
    }

    public static boolean cancelActive(ServerLevel level, BlockPos centerPos, ServerPlayer player) {
        boolean cancelled = cancelActive(level, centerPos);
        if (cancelled) {
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.ritual_cancelled"), true);
        }
        return cancelled;
    }

    public static boolean cancelActiveFromBrokenGlyph(ServerLevel level, BlockPos centerPos) {
        return cancelActive(level, centerPos);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_RITUALS.isEmpty()) {
            return;
        }

        long gameTime = event.getServer().overworld().getGameTime();
        Iterator<ActiveWaitingRitual> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            ActiveWaitingRitual activeRitual = iterator.next();
            ServerLevel level = event.getServer().getLevel(activeRitual.dimension());
            if (level == null) {
                iterator.remove();
                continue;
            }

            activeRitual.tick().tick(level, activeRitual.centerPos(), gameTime);
        }
    }

    private static boolean cancelActive(ServerLevel level, BlockPos centerPos) {
        boolean cancelled = false;
        Iterator<ActiveWaitingRitual> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            ActiveWaitingRitual activeRitual = iterator.next();
            if (!activeRitual.dimension().equals(level.dimension()) || !activeRitual.centerPos().equals(centerPos)) {
                continue;
            }

            activeRitual.stop().stop(level, activeRitual.centerPos());
            iterator.remove();
            cancelled = true;
        }

        if (cancelled) {
            playFailureSmoke(level, centerPos);
        }
        return cancelled;
    }

    public static boolean completeFirst(ServerLevel level, WaitingCompletion completion) {
        Iterator<ActiveWaitingRitual> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            ActiveWaitingRitual activeRitual = iterator.next();
            if (!activeRitual.dimension().equals(level.dimension()) || !completion.matches(activeRitual.ritualId(), level, activeRitual.centerPos())) {
                continue;
            }

            completion.complete(activeRitual.ritualId(), level, activeRitual.centerPos());
            iterator.remove();
            return true;
        }
        return false;
    }

    public static void playFailureSmoke(ServerLevel level, BlockPos centerPos) {
        level.sendParticles(
                ParticleTypes.SMOKE,
                centerPos.getX() + 0.5D,
                centerPos.getY() + 0.5D,
                centerPos.getZ() + 0.5D,
                18,
                0.35D,
                0.2D,
                0.35D,
                0.02D
        );
        level.playSound(null, centerPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.0F);
    }

    @FunctionalInterface
    public interface WaitingTick {
        void tick(ServerLevel level, BlockPos centerPos, long gameTime);
    }

    @FunctionalInterface
    public interface WaitingStop {
        WaitingStop NOOP = (level, centerPos) -> {
        };

        void stop(ServerLevel level, BlockPos centerPos);
    }

    public interface WaitingCompletion {
        boolean matches(String ritualId, ServerLevel level, BlockPos centerPos);

        void complete(String ritualId, ServerLevel level, BlockPos centerPos);
    }

    private record ActiveWaitingRitual(String ritualId, ResourceKey<Level> dimension, BlockPos centerPos,
                                       WaitingTick tick, WaitingStop stop) {
    }
}
