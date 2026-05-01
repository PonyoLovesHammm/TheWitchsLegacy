package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class RitualMatcher {
    private static final double ITEM_RADIUS = 7.0;
    private static final Comparator<RitualDefinition> MATCH_PRIORITY = Comparator
            .comparingInt(RitualMatcher::totalRequiredItemCount)
            .thenComparingInt(ritual -> ritual.ringRequirements().size())
            .thenComparingInt(RitualDefinition::altarPowerCost);

    private RitualMatcher() {
    }

    static RitualDefinition findMatchingRitual(ServerLevel level, BlockPos centerPos, List<ItemEntity> nearbyItems) {
        return ModRituals.ALL.stream()
                .filter(ritual -> ritual.ringMatcher().matches(level, centerPos))
                .filter(ritual -> hasRequiredItems(nearbyItems, ritual.itemRequirements()))
                .max(MATCH_PRIORITY)
                .orElse(null);
    }

    static List<RitualDefinition> findMatchingRitualsInCastOrder(ServerLevel level, BlockPos centerPos, List<ItemEntity> nearbyItems) {
        List<ItemStack> availableItems = new ArrayList<>();
        for (ItemEntity itemEntity : nearbyItems) {
            availableItems.add(itemEntity.getItem().copy());
        }

        Map<String, RitualDefinition> bestVariantByGroup = new HashMap<>();
        ModRituals.ALL.stream()
                .filter(ritual -> ritual.ringMatcher().matches(level, centerPos))
                .filter(ritual -> hasRequiredItems(nearbyItems, ritual.itemRequirements()))
                .forEach(ritual -> bestVariantByGroup.merge(
                        ritualGroupId(ritual),
                        ritual,
                        (existing, candidate) -> MATCH_PRIORITY.compare(candidate, existing) > 0 ? candidate : existing
                ));

        List<RitualDefinition> matches = new ArrayList<>();
        List<RitualDefinition> candidates = bestVariantByGroup.values().stream()
                .sorted(Comparator
                        .comparingInt(RitualMatcher::ringCastOrder)
                        .thenComparing(MATCH_PRIORITY.reversed()))
                .toList();

        for (RitualDefinition ritual : candidates) {
            if (canAllocateItems(availableItems, ritual.itemRequirements())) {
                matches.add(ritual);
                allocateConsumableItems(availableItems, ritual.itemRequirements());
            }
        }
        return List.copyOf(matches);
    }

    static List<ItemEntity> getNearbyItems(ServerLevel level, BlockPos centerPos) {
        return level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(centerPos).inflate(ITEM_RADIUS),
                itemEntity -> itemEntity.distanceToSqr(centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5) <= ITEM_RADIUS * ITEM_RADIUS
        );
    }

    static boolean hasRequiredItems(List<ItemEntity> itemEntities, List<RitualItemRequirement> requirements) {
        for (RitualItemRequirement requirement : requirements) {
            int matchedCount = 0;
            for (ItemEntity itemEntity : itemEntities) {
                ItemStack stack = itemEntity.getItem();
                if (!requirement.matcher().test(stack)) {
                    continue;
                }
                matchedCount += stack.getCount();
            }

            if (matchedCount < requirement.count()) {
                return false;
            }
        }
        return true;
    }

    private static int totalRequiredItemCount(RitualDefinition ritual) {
        int total = 0;
        for (RitualItemRequirement requirement : ritual.itemRequirements()) {
            total += requirement.count();
        }
        return total;
    }

    private static int ringCastOrder(RitualDefinition ritual) {
        int order = 0;
        for (RitualRingRequirement requirement : ritual.ringRequirements()) {
            order = Math.max(order, requirement.size().ordinal());
        }
        return order;
    }

    private static String ritualGroupId(RitualDefinition ritual) {
        String id = ritual.id();
        id = id.replace("_waystone", "");
        id = id.replace("_small", "");
        id = id.replace("_medium", "");
        id = id.replace("_large", "");
        return id;
    }

    private static boolean canAllocateItems(List<ItemStack> availableItems, List<RitualItemRequirement> requirements) {
        for (RitualItemRequirement requirement : requirements) {
            int matchedCount = 0;
            for (ItemStack stack : availableItems) {
                if (requirement.matcher().test(stack)) {
                    matchedCount += stack.getCount();
                }
            }
            if (matchedCount < requirement.count()) {
                return false;
            }
        }
        return true;
    }

    private static void allocateConsumableItems(List<ItemStack> availableItems, List<RitualItemRequirement> requirements) {
        for (RitualItemRequirement requirement : requirements) {
            if (!requirement.consume()) {
                continue;
            }

            int remaining = requirement.count();
            for (ItemStack stack : availableItems) {
                if (remaining <= 0) {
                    break;
                }
                if (!requirement.matcher().test(stack)) {
                    continue;
                }

                int consumed = Math.min(remaining, stack.getCount());
                stack.shrink(consumed);
                remaining -= consumed;
            }
        }
        availableItems.removeIf(ItemStack::isEmpty);
    }
}
