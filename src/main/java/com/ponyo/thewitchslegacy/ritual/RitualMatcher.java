package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class RitualMatcher {
    private static final double ITEM_RADIUS = 7.0;

    private RitualMatcher() {
    }

    static RitualDefinition findMatchingRitual(ServerLevel level, BlockPos centerPos) {
        for (RitualDefinition ritual : ModRituals.ALL) {
            if (matchesRings(level, centerPos, ritual.ringRequirements())) {
                return ritual;
            }
        }
        return null;
    }

    static List<ItemEntity> getNearbyItems(ServerLevel level, BlockPos centerPos) {
        return level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(centerPos).inflate(ITEM_RADIUS),
                itemEntity -> itemEntity.distanceToSqr(centerPos.getX() + 0.5, centerPos.getY() + 0.5, centerPos.getZ() + 0.5) <= ITEM_RADIUS * ITEM_RADIUS
        );
    }

    static boolean hasRequiredItems(List<ItemEntity> itemEntities, List<RitualItemRequirement> requirements) {
        Map<Item, Integer> available = countItems(itemEntities);
        for (RitualItemRequirement requirement : requirements) {
            if (available.getOrDefault(requirement.item(), 0) < requirement.count()) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesRings(ServerLevel level, BlockPos centerPos, List<RitualRingRequirement> requirements) {
        for (RitualRingRequirement requirement : requirements) {
            for (BlockPos offset : RitualPatterns.positionsFor(requirement.size())) {
                if (!level.getBlockState(centerPos.offset(offset)).is(requirement.glyphBlock())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Map<Item, Integer> countItems(List<ItemEntity> itemEntities) {
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack stack = itemEntity.getItem();
            counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        return counts;
    }
}
