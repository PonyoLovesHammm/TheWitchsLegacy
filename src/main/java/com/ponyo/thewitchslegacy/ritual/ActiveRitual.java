package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class ActiveRitual {
    private final UUID playerId;
    private final ResourceKey<Level> dimension;
    private final BlockPos centerPos;
    private final RitualDefinition ritual;
    private final List<RitualItemRequirement> itemsToConsume;
    private final List<ItemStack> consumedItems = new ArrayList<>();
    private int nextItemIndex;
    private long nextConsumeTick;

    ActiveRitual(UUID playerId, ResourceKey<Level> dimension, BlockPos centerPos, RitualDefinition ritual,
                 List<RitualItemRequirement> itemsToConsume, long nextConsumeTick) {
        this.playerId = playerId;
        this.dimension = dimension;
        this.centerPos = centerPos;
        this.ritual = ritual;
        this.itemsToConsume = itemsToConsume;
        this.nextConsumeTick = nextConsumeTick;
    }

    UUID playerId() {
        return this.playerId;
    }

    ResourceKey<Level> dimension() {
        return this.dimension;
    }

    BlockPos centerPos() {
        return this.centerPos;
    }

    RitualDefinition ritual() {
        return this.ritual;
    }

    long nextConsumeTick() {
        return this.nextConsumeTick;
    }

    RitualItemRequirement nextItemToConsume() {
        return this.nextItemIndex >= this.itemsToConsume.size() ? null : this.itemsToConsume.get(this.nextItemIndex);
    }

    List<ItemStack> consumedItems() {
        return this.consumedItems;
    }

    void advance(ItemStack consumedItem, long nextConsumeTick) {
        this.consumedItems.add(consumedItem);
        this.nextItemIndex++;
        this.nextConsumeTick = nextConsumeTick;
    }
}
