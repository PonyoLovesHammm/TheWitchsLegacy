package com.ponyo.thewitchslegacy.ritual.definitions.transposition;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.WaystoneItem;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public final class WaystoneTransposition {
    private WaystoneTransposition() {
    }

    public static RitualDefinition create() {
        List<RitualItemRequirement> itemRequirements = List.of(
                boundOrBloodedWaystoneRequirement(),
                new RitualItemRequirement(ModItems.INFUSED_STONE_CHARGED.get(), 1, true)
        );

        return new RitualDefinition(
                "waystone_transposition",
                "ritual.thewitchslegacy.waystone_transposition",
                List.of(RitualRingRequirement.small(ModBlocks.OTHERWHERE_GLYPH.get())),
                itemRequirements,
                0,
                (level, centerPos, player, consumedItems) -> {
                    TeleportTarget target = resolveTeleportTarget(level, consumedItems);
                    if (target == null) {
                        return Component.translatable("message.thewitchslegacy.waystone_target_unavailable");
                    }

                    boolean teleported = player.teleportTo(
                            target.level(),
                            target.x(),
                            target.y(),
                            target.z(),
                            Set.of(),
                            player.getYRot(),
                            player.getXRot(),
                            true
                    );
                    if (!teleported) {
                        return Component.translatable("message.thewitchslegacy.waystone_target_unavailable");
                    }

                    RitualEffects.playCompletionEffects(level, centerPos.above(1));
                    RitualEffects.playCompletionEffects(target.level(), BlockPos.containing(target.x(), target.y(), target.z()));
                    RitualEffects.spawnChargedStoneRemainderIfNeeded(level, centerPos.above(1), itemRequirements);
                    return null;
                }
        );
    }

    private static RitualItemRequirement boundOrBloodedWaystoneRequirement() {
        return new RitualItemRequirement(
                ModItems.BOUND_WAYSTONE.get(),
                1,
                true,
                stack -> stack.is(ModItems.BOUND_WAYSTONE.get()) || stack.is(ModItems.BLOODED_WAYSTONE.get())
        );
    }

    private static ItemStack findBoundOrBloodedWaystone(List<ItemStack> consumedItems) {
        for (ItemStack stack : consumedItems) {
            if (stack.is(ModItems.BOUND_WAYSTONE.get()) || stack.is(ModItems.BLOODED_WAYSTONE.get())) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static TeleportTarget resolveTeleportTarget(ServerLevel level, List<ItemStack> consumedItems) {
        ItemStack sourceWaystone = findBoundOrBloodedWaystone(consumedItems);
        if (sourceWaystone.isEmpty()) {
            return null;
        }

        if (sourceWaystone.is(ModItems.BOUND_WAYSTONE.get())) {
            WaystoneItem.StoredLocation location = WaystoneItem.getStoredLocation(sourceWaystone).orElse(null);
            if (location == null) {
                return null;
            }

            Identifier dimensionId = parseDimensionIdentifier(location.dimensionId());
            if (dimensionId == null) {
                return null;
            }

            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
            ServerLevel targetLevel = level.getServer().getLevel(dimensionKey);
            if (targetLevel == null) {
                return null;
            }

            return new TeleportTarget(
                    targetLevel,
                    location.x() + 0.5D,
                    location.y() + 1.0D,
                    location.z() + 0.5D
            );
        }

        WaystoneItem.BloodTarget bloodTarget = WaystoneItem.getBloodTarget(sourceWaystone).orElse(null);
        if (bloodTarget == null) {
            return null;
        }

        ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(bloodTarget.entityUuid());
        if (targetPlayer == null) {
            return null;
        }

        return new TeleportTarget(
                (ServerLevel) targetPlayer.level(),
                targetPlayer.getX(),
                targetPlayer.getY(),
                targetPlayer.getZ()
        );
    }

    private static Identifier parseDimensionIdentifier(String rawDimensionId) {
        try {
            return Identifier.parse(rawDimensionId);
        } catch (RuntimeException ignored) {
            int slashIndex = rawDimensionId.indexOf('/');
            int endIndex = rawDimensionId.lastIndexOf(']');
            if (slashIndex < 0 || endIndex <= slashIndex) {
                return null;
            }

            String candidate = rawDimensionId.substring(slashIndex + 1, endIndex).trim();
            try {
                return Identifier.parse(candidate);
            } catch (RuntimeException ignoredAgain) {
                return null;
            }
        }
    }

    private record TeleportTarget(ServerLevel level, double x, double y, double z) {
    }
}
