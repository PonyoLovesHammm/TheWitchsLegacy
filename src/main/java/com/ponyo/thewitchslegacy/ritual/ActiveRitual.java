package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class ActiveRitual {
    private final UUID playerId;
    private final ResourceKey<Level> dimension;
    private final BlockPos centerPos;
    private final RitualDefinition ritual;
    private final List<Item> itemsToConsume;
    private final List<Item> consumedItems = new ArrayList<>();
    private int nextItemIndex;
    private long nextConsumeTick;

    ActiveRitual(UUID playerId, ResourceKey<Level> dimension, BlockPos centerPos, RitualDefinition ritual,
                 List<Item> itemsToConsume, long nextConsumeTick) {
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

    Item nextItemToConsume() {
        return this.nextItemIndex >= this.itemsToConsume.size() ? null : this.itemsToConsume.get(this.nextItemIndex);
    }

    List<Item> consumedItems() {
        return this.consumedItems;
    }

    void advance(Item consumedItem, long nextConsumeTick) {
        this.consumedItems.add(consumedItem);
        this.nextItemIndex++;
        this.nextConsumeTick = nextConsumeTick;
    }
}
