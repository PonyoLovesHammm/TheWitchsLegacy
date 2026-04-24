package com.ponyo.thewitchslegacy.block.entity.distillery;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DistilleryRecipes {
    private static final List<DistilleryRecipe> RECIPES = List.of(
            new DistilleryRecipe(
                    Map.of(
                            Identifier.fromNamespaceAndPath("minecraft", "ender_pearl"), 1
                    ),
                    List.of(
                            new DistilleryRecipeOutput(modId("ender_dew"), 5),
                            new DistilleryRecipeOutput(modId("whiff_of_magic"), 1)
                    ),
                    400,
                    0,
                    0
            )
    );

    private DistilleryRecipes() {
    }

    public static Optional<DistilleryRecipe> findMatch(Map<Identifier, Integer> ingredientCounts) {
        if (ingredientCounts.isEmpty()) {
            return Optional.empty();
        }

        return RECIPES.stream()
                .filter(recipe -> recipe.matches(ingredientCounts))
                .findFirst();
    }

    private static Identifier modId(String path) {
        return Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, path);
    }
}
