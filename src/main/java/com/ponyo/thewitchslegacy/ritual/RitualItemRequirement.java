package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public record RitualItemRequirement(Item item, int count, boolean consume, Predicate<ItemStack> matcher) {
    public RitualItemRequirement(Item item, int count, boolean consume) {
        this(item, count, consume, stack -> stack.is(item));
    }
}
