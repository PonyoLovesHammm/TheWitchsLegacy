package com.ponyo.thewitchslegacy.ritual;

import com.ponyo.thewitchslegacy.block.entity.AltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public final class RitualManager {
    private static final int ITEM_CONSUME_INTERVAL_TICKS = 20;
    private static final List<ActiveRitual> ACTIVE_RITUALS = new ArrayList<>();

    private RitualManager() {
    }

    public static boolean tryTrigger(ServerLevel level, BlockPos centerPos, ServerPlayer player) {
        List<ItemEntity> nearbyItems = RitualMatcher.getNearbyItems(level, centerPos);
        RitualDefinition ritual = RitualMatcher.findMatchingRitual(level, centerPos, nearbyItems);
        if (ritual == null) {
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.unknown_ritual"), true);
            RitualVisuals.playFailureSmoke(level, centerPos);
            return true;
        }
        if (isRitualActive(level, centerPos)) {
            return true;
        }

        Component startFailure = ritual.startValidator().validate(level, centerPos, player);
        if (startFailure != null) {
            player.displayClientMessage(startFailure, true);
            RitualVisuals.playFailureSmoke(level, centerPos);
            return true;
        }

        AltarBlockEntity altar = RitualAltarSupport.findBestSupportingAltar(level, centerPos, ritual.altarPowerCost());
        if (ritual.altarPowerCost() > 0 && altar == null) {
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.altar_power_insufficient"), true);
            RitualVisuals.playFailureSmoke(level, centerPos);
            return true;
        }

        if (altar != null && !altar.consumePower(ritual.altarPowerCost())) {
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.altar_power_insufficient"), true);
            RitualVisuals.playFailureSmoke(level, centerPos);
            return true;
        }

        startRitual(level, centerPos, ritual, player);
        return true;
    }

    public static boolean cancelActiveRitual(ServerLevel level, BlockPos centerPos, ServerPlayer player) {
        Iterator<ActiveRitual> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            ActiveRitual activeRitual = iterator.next();
            if (!activeRitual.dimension().equals(level.dimension()) || !activeRitual.centerPos().equals(centerPos)) {
                continue;
            }

            iterator.remove();
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.ritual_cancelled"), true);
            RitualVisuals.playFailureSmoke(level, centerPos);
            returnConsumedItems(level, activeRitual);
            return true;
        }

        return false;
    }

    public static boolean cancelActiveRitualFromBrokenGlyph(ServerLevel level, BlockPos centerPos) {
        Iterator<ActiveRitual> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            ActiveRitual activeRitual = iterator.next();
            if (!activeRitual.dimension().equals(level.dimension()) || !activeRitual.centerPos().equals(centerPos)) {
                continue;
            }

            iterator.remove();
            RitualVisuals.playFailureSmoke(level, centerPos);
            returnConsumedItems(level, activeRitual);
            return true;
        }

        return false;
    }

    public static boolean isRitualActive(ServerLevel level, BlockPos centerPos) {
        for (ActiveRitual activeRitual : ACTIVE_RITUALS) {
            if (activeRitual.dimension().equals(level.dimension()) && activeRitual.centerPos().equals(centerPos)) {
                return true;
            }
        }
        return false;
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_RITUALS.isEmpty()) {
            return;
        }

        long gameTime = event.getServer().overworld().getGameTime();
        Iterator<ActiveRitual> iterator = ACTIVE_RITUALS.iterator();
        while (iterator.hasNext()) {
            ActiveRitual activeRitual = iterator.next();
            ServerLevel level = event.getServer().getLevel(activeRitual.dimension());
            if (level == null) {
                iterator.remove();
                continue;
            }

            if (gameTime % 2L == 0L) {
                RitualVisuals.playCastingParticles(level, activeRitual.centerPos());
            }

            if (!activeRitual.ritual().ringMatcher().matches(level, activeRitual.centerPos())) {
                failRitual(event, level, activeRitual);
                iterator.remove();
                continue;
            }

            if (gameTime < activeRitual.nextConsumeTick()) {
                continue;
            }

            RitualItemRequirement itemToConsume = activeRitual.nextItemToConsume();
            if (itemToConsume == null) {
                ServerPlayer player = event.getServer().getPlayerList().getPlayer(activeRitual.playerId());
                if (player != null) {
                    Component completionFailure = activeRitual.ritual().startValidator().validate(level, activeRitual.centerPos(), player);
                    if (completionFailure != null) {
                        failRitual(event, level, activeRitual, completionFailure);
                        iterator.remove();
                        continue;
                    }
                    Component effectMessage = activeRitual.ritual().effect().execute(
                            level,
                            activeRitual.centerPos(),
                            player,
                            List.copyOf(activeRitual.consumedItems())
                    );
                    if (effectMessage != null) {
                        player.displayClientMessage(effectMessage, true);
                    }
                }
                iterator.remove();
                continue;
            }

            RitualItemCollector.ItemConsumeResult result = RitualItemCollector.consumeOneItem(level, activeRitual.centerPos(), itemToConsume);
            if (result == null) {
                failRitual(event, level, activeRitual);
                iterator.remove();
                continue;
            }

            RitualVisuals.playItemConsumedEffects(level, result.x(), result.y(), result.z());
            activeRitual.advance(result.consumedStack(), gameTime + ITEM_CONSUME_INTERVAL_TICKS);
        }
    }

    private static void startRitual(ServerLevel level, BlockPos centerPos, RitualDefinition ritual, ServerPlayer player) {
        ACTIVE_RITUALS.add(new ActiveRitual(
                player.getUUID(),
                level.dimension(),
                centerPos.immutable(),
                ritual,
                RitualItemCollector.itemsToConsume(ritual.itemRequirements()),
                level.getGameTime()
        ));
    }

    private static void failRitual(ServerTickEvent.Post event, ServerLevel level, ActiveRitual activeRitual) {
        failRitual(event, level, activeRitual, null);
    }

    private static void failRitual(ServerTickEvent.Post event, ServerLevel level, ActiveRitual activeRitual, Component failureMessage) {
        ServerPlayer player = event.getServer().getPlayerList().getPlayer(activeRitual.playerId());
        if (player == null) {
            return;
        }

        if (failureMessage != null) {
            player.displayClientMessage(failureMessage, true);
        } else {
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.ritual_failed_items_picked_up"), true);
        }
        RitualVisuals.playFailureSmoke(level, activeRitual.centerPos());
        returnConsumedItems(level, activeRitual);
    }

    private static void returnConsumedItems(ServerLevel level, ActiveRitual activeRitual) {
        for (ItemStack stack : activeRitual.consumedItems()) {
            RitualEffects.spawnItem(level, activeRitual.centerPos().above(1), stack);
        }
    }
}
