package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

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
}
