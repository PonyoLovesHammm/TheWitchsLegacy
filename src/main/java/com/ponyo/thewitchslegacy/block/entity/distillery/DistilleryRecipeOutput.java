package com.ponyo.thewitchslegacy.block.entity.distillery;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record DistilleryRecipeOutput(Identifier itemId, int count) {
    public Item item() {
        Item item = BuiltInRegistries.ITEM.getValue(this.itemId);
        return item != null ? item : Items.AIR;
    }

    public int jarCost() {
        return this.count;
    }
}
