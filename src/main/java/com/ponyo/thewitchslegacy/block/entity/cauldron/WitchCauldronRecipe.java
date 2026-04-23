package com.ponyo.thewitchslegacy.block.entity.cauldron;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public record WitchCauldronRecipe(
        Map<Identifier, Integer> ingredients,
        Identifier resultItemId,
        int resultCount,
        int resultColor
) {
    public boolean matches(Map<Identifier, Integer> availableIngredients) {
        return this.ingredients.equals(availableIngredients);
    }

    public boolean canStillMatch(Map<Identifier, Integer> currentIngredients) {
        for (Map.Entry<Identifier, Integer> entry : currentIngredients.entrySet()) {
            int requiredCount = this.ingredients.getOrDefault(entry.getKey(), 0);
            if (entry.getValue() > requiredCount) {
                return false;
            }
        }

        return true;
    }

    public ItemStack createResultStack() {
        Item resultItem = BuiltInRegistries.ITEM.getValue(this.resultItemId);
        if (resultItem == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(resultItem, this.resultCount);
    }
}
