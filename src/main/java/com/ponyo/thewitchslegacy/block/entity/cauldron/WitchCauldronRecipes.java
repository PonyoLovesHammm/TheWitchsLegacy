package com.ponyo.thewitchslegacy.block.entity.cauldron;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class WitchCauldronRecipes {
    private static final List<WitchCauldronRecipe> RECIPES = List.of(
            new WitchCauldronRecipe(
                    Map.of(
                            modId("mandrake_root"), 1,
                            Identifier.fromNamespaceAndPath("minecraft", "egg"), 1,
                            modId("bark_of_the_ancient"), 1
                    ),
                    modId("mutandis"),
                    1,
                    0x7ED267
            )
    );

    private WitchCauldronRecipes() {
    }

    public static Optional<WitchCauldronRecipe> findExactMatch(Map<Identifier, Integer> ingredientCounts) {
        if (ingredientCounts.isEmpty()) {
            return Optional.empty();
        }

        return RECIPES.stream()
                .filter(recipe -> recipe.matches(ingredientCounts))
                .findFirst();
    }

    public static boolean hasViableContinuation(Map<Identifier, Integer> currentIngredientCounts) {
        if (currentIngredientCounts.isEmpty()) {
            return false;
        }

        return RECIPES.stream().anyMatch(recipe -> recipe.canStillMatch(currentIngredientCounts));
    }

    private static Identifier modId(String path) {
        return Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, path);
    }
}
