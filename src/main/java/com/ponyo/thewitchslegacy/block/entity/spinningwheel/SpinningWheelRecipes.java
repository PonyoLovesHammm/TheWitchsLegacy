package com.ponyo.thewitchslegacy.block.entity.spinningwheel;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SpinningWheelRecipes {
    private static final Map<Identifier, SpinningWheelRecipe> RECIPES = new LinkedHashMap<>();

    private SpinningWheelRecipes() {
    }

    public static Optional<SpinningWheelRecipe> findById(Identifier id) {
        return Optional.ofNullable(RECIPES.get(id));
    }

    public static Collection<SpinningWheelRecipe> all() {
        return RECIPES.values();
    }

    public static void register(SpinningWheelRecipe recipe) {
        RECIPES.put(recipe.id(), recipe);
    }
}
