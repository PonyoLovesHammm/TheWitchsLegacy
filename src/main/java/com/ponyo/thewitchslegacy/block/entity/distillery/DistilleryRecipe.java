package com.ponyo.thewitchslegacy.block.entity.distillery;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public record DistilleryRecipe(
        Map<Identifier, Integer> ingredients,
        List<DistilleryRecipeOutput> outputs,
        int distillTime,
        int altarPowerRequirement,
        int altarPowerConsumptionRate
) {
    public boolean matches(Map<Identifier, Integer> availableIngredients) {
        if (availableIngredients.size() != this.ingredients.size()) {
            return false;
        }

        for (Map.Entry<Identifier, Integer> entry : this.ingredients.entrySet()) {
            if (availableIngredients.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    public int jarCost() {
        return this.outputs.stream()
                .mapToInt(DistilleryRecipeOutput::jarCost)
                .sum();
    }
}
