package com.ponyo.thewitchslegacy.ritual.definitions.transposition;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.Waystone;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class CreatureTeleportation {
    private static final int PENDING_TIMEOUT_TICKS = 100;
    private static final List<PendingTeleport> PENDING_TELEPORTS = new ArrayList<>();

    private CreatureTeleportation() {
    }

    public static RitualDefinition create() {
        List<RitualItemRequirement> itemRequirements = List.of(
                new RitualItemRequirement(ModItems.WAYSTONE.get(), 1, true),
                filledWitchsClaimRequirement(),
                new RitualItemRequirement(ModItems.ENDER_DEW.get(), 1, true),
                new RitualItemRequirement(ModItems.ROOT_OF_REMEMBRANCE.get(), 1, true)
        );

        return new RitualDefinition(
                "creature_teleportation",
                "ritual.thewitchslegacy.creature_teleportation",
                List.of(RitualRingRequirement.medium(ModBlocks.OTHERWHERE_GLYPH.get())),
                itemRequirements,
                3000,
                (level, centerPos, player, consumedItems) -> {
                    Waystone.BloodTarget target = findClaimTarget(consumedItems);
                    if (target == null) {
                        return Component.translatable("message.thewitchslegacy.creature_target_unavailable");
                    }

                    Entity entity = findLoadedTargetEntity(level, target);
                    if (entity == null || !entity.isAlive()) {
                        if (target.lastKnownLocation() != null) {
                            startPendingTeleport(level, centerPos, player, target);
                            return null;
                        }
                        if (level.getServer().getPlayerList().getPlayer(target.entityUuid()) == null) {
                            return Component.translatable("message.thewitchslegacy.creature_target_unavailable");
                        }
                        return Component.translatable("message.thewitchslegacy.player_offline");
                    }

                    if (!teleportEntity(level, centerPos, entity)) {
                        return Component.translatable("message.thewitchslegacy.creature_target_unavailable");
                    }
                    return null;
                }
        );
    }

    private static RitualItemRequirement filledWitchsClaimRequirement() {
        return new RitualItemRequirement(
                ModItems.WITCHS_CLAIM_FILLED.get(),
                1,
                true,
                stack -> stack.is(ModItems.WITCHS_CLAIM_FILLED.get()) && Waystone.getBloodTarget(stack).isPresent()
        );
    }

    private static Waystone.BloodTarget findClaimTarget(List<ItemStack> consumedItems) {
        for (ItemStack stack : consumedItems) {
            if (stack.is(ModItems.WITCHS_CLAIM_FILLED.get())) {
                return Waystone.getBloodTarget(stack).orElse(null);
            }
        }
        return null;
    }

    private static Entity findLoadedTargetEntity(ServerLevel level, Waystone.BloodTarget target) {
        ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(target.entityUuid());
        if (targetPlayer != null) {
            return targetPlayer;
        }

        Entity indexedEntity = level.getEntityInAnyDimension(target.entityUuid());
        if (indexedEntity != null) {
            return indexedEntity;
        }

        for (ServerLevel candidateLevel : level.getServer().getAllLevels()) {
            for (Entity entity : candidateLevel.getAllEntities()) {
                if (entity.getUUID().equals(target.entityUuid())) {
                    return entity;
                }
            }
        }

        return null;
    }

    private static void startPendingTeleport(ServerLevel castingLevel, BlockPos centerPos, ServerPlayer player, Waystone.BloodTarget target) {
        ServerLevel targetLevel = resolveTargetLevel(castingLevel, target.lastKnownLocation());
        if (targetLevel == null) {
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.creature_target_unavailable"), true);
            return;
        }

        ChunkPos chunkPos = new ChunkPos(target.lastKnownLocation().blockPos());
        targetLevel.getChunkSource().addTicketWithRadius(TicketType.FORCED, chunkPos, 0);
        targetLevel.getChunk(target.lastKnownLocation().blockPos());
        PENDING_TELEPORTS.add(new PendingTeleport(
                target.entityUuid(),
                player.getUUID(),
                castingLevel.dimension(),
                centerPos.immutable(),
                targetLevel.dimension(),
                chunkPos,
                castingLevel.getGameTime() + PENDING_TIMEOUT_TICKS
        ));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (PENDING_TELEPORTS.isEmpty()) {
            return;
        }

        long gameTime = event.getServer().overworld().getGameTime();
        Iterator<PendingTeleport> iterator = PENDING_TELEPORTS.iterator();
        while (iterator.hasNext()) {
            PendingTeleport pending = iterator.next();
            ServerLevel castingLevel = event.getServer().getLevel(pending.castingDimension());
            ServerLevel targetLevel = event.getServer().getLevel(pending.targetDimension());
            if (castingLevel == null || targetLevel == null) {
                iterator.remove();
                releaseTicket(targetLevel, pending);
                continue;
            }

            Entity entity = targetLevel.getEntity(pending.targetId());
            if (entity == null) {
                for (Entity loadedEntity : targetLevel.getAllEntities()) {
                    if (loadedEntity.getUUID().equals(pending.targetId())) {
                        entity = loadedEntity;
                        break;
                    }
                }
            }

            if (entity != null && entity.isAlive()) {
                teleportEntity(castingLevel, pending.centerPos(), entity);
                releaseTicket(targetLevel, pending);
                iterator.remove();
                continue;
            }

            if (gameTime >= pending.timeoutTick()) {
                ServerPlayer player = event.getServer().getPlayerList().getPlayer(pending.playerId());
                if (player != null) {
                    player.displayClientMessage(Component.translatable("message.thewitchslegacy.creature_target_unavailable"), true);
                }
                releaseTicket(targetLevel, pending);
                iterator.remove();
            }
        }
    }

    private static ServerLevel resolveTargetLevel(ServerLevel castingLevel, Waystone.StoredLocation location) {
        if (location == null) {
            return null;
        }

        Identifier dimensionId;
        try {
            dimensionId = Identifier.parse(location.dimensionId());
        } catch (RuntimeException ignored) {
            return null;
        }

        return castingLevel.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
    }

    private static boolean teleportEntity(ServerLevel level, BlockPos centerPos, Entity entity) {
        boolean teleported = entity.teleportTo(
                level,
                centerPos.getX() + 0.5D,
                centerPos.getY() + 1.0D,
                centerPos.getZ() + 0.5D,
                Set.of(),
                entity.getYRot(),
                entity.getXRot(),
                true
        );
        if (teleported) {
            RitualEffects.playCompletionEffects(level, centerPos.above(1));
        }
        return teleported;
    }

    private static void releaseTicket(ServerLevel targetLevel, PendingTeleport pending) {
        if (targetLevel != null) {
            targetLevel.getChunkSource().removeTicketWithRadius(TicketType.FORCED, pending.chunkPos(), 0);
        }
    }

    private record PendingTeleport(UUID targetId, UUID playerId, ResourceKey<Level> castingDimension,
                                   BlockPos centerPos, ResourceKey<Level> targetDimension, ChunkPos chunkPos,
                                   long timeoutTick) {
    }
}
