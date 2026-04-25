package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class RitualItemCollector {
    private RitualItemCollector() {
    }

    static List<RitualItemRequirement> itemsToConsume(List<RitualItemRequirement> requirements) {
        List<RitualItemRequirement> items = new ArrayList<>();
        for (RitualItemRequirement requirement : requirements) {
            if (!requirement.consume()) {
                continue;
            }

            for (int i = 0; i < requirement.count(); i++) {
                items.add(requirement);
            }
        }
        return items;
    }

    static ItemConsumeResult consumeOneItem(ServerLevel level, BlockPos centerPos, RitualItemRequirement requirement) {
        for (ItemEntity itemEntity : RitualMatcher.getNearbyItems(level, centerPos)) {
            ItemStack stack = itemEntity.getItem();
            if (!requirement.matcher().test(stack)) {
                continue;
            }

            ItemStack consumedStack = stack.copyWithCount(1);
            double x = itemEntity.getX();
            double y = itemEntity.getY() + 0.1;
            double z = itemEntity.getZ();
            stack.shrink(1);

            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }

            return new ItemConsumeResult(x, y, z, consumedStack);
        }

        return null;
    }

    record ItemConsumeResult(double x, double y, double z, ItemStack consumedStack) {
    }
}
