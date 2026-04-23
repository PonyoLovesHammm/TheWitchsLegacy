package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.world.item.Item;

public record RitualItemRequirement(Item item, int count, boolean consume) {
}
