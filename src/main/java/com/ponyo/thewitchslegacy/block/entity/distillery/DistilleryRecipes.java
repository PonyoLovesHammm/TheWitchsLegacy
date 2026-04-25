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
                            new DistilleryRecipeOutput(modId("ender_dew"), 5, 5),
                            new DistilleryRecipeOutput(modId("whiff_of_magic"), 1, 1)
                    ),
                    400,
                    0,
                    0
            ),
            new DistilleryRecipe(
                    Map.of(
                            modId("quicklime"), 1,
                            Identifier.fromNamespaceAndPath("minecraft", "gunpowder"), 1
                    ),
                    List.of(
                            new DistilleryRecipeOutput(modId("oil_of_vitriol"), 1, 1),
                            new DistilleryRecipeOutput(modId("selenite_shard"), 1),
                            new DistilleryRecipeOutput(Identifier.fromNamespaceAndPath("minecraft", "bone_meal"), 1)
                    ),
                    400,
                    0,
                    0
            ),
            new DistilleryRecipe(
                    Map.of(
                            modId("breath_of_the_goddess"), 1,
                            Identifier.fromNamespaceAndPath("minecraft", "lapis_lazuli"), 1
                    ),
                    List.of(
                            new DistilleryRecipeOutput(modId("tear_of_the_goddess"), 1, 1),
                            new DistilleryRecipeOutput(modId("whiff_of_magic"), 1, 1),
                            new DistilleryRecipeOutput(Identifier.fromNamespaceAndPath("minecraft", "slime_ball"), 1),
                            new DistilleryRecipeOutput(Identifier.fromNamespaceAndPath("minecraft", "clay_ball"), 1)
                    ),
                    400,
                    0,
                    0
            ),
            new DistilleryRecipe(
                    Map.of(
                            Identifier.fromNamespaceAndPath("minecraft", "diamond"), 1,
                            modId("oil_of_vitriol"), 1
                    ),
                    List.of(
                            new DistilleryRecipeOutput(modId("diamond_vapor"), 2, 2),
                            new DistilleryRecipeOutput(modId("odor_of_purity"), 1, 1)
                    ),
                    400,
                    0,
                    0
            ),
            new DistilleryRecipe(
                    Map.of(
                            modId("diamond_vapor"), 1,
                            Identifier.fromNamespaceAndPath("minecraft", "ghast_tear"), 1
                    ),
                    List.of(
                            new DistilleryRecipeOutput(modId("odor_of_purity"), 1, 1),
                            new DistilleryRecipeOutput(modId("reek_of_misfortune"), 1, 1),
                            new DistilleryRecipeOutput(modId("refined_evil"), 1),
                            new DistilleryRecipeOutput(Identifier.fromNamespaceAndPath("minecraft", "blaze_powder"), 1)
                    ),
                    400,
                    0,
                    0
            ),
            new DistilleryRecipe(
                    Map.of(
                            modId("demon_heart"), 1,
                            modId("diamond_vapor"), 1
                    ),
                    List.of(
                            new DistilleryRecipeOutput(modId("demonic_blood"), 4, 4),
                            new DistilleryRecipeOutput(modId("refined_evil"), 1)
                    ),
                    400,
                    0,
                    0
            ),
            new DistilleryRecipe(
                    Map.of(
                            modId("demon_heart"), 1,
                            Identifier.fromNamespaceAndPath("minecraft", "netherrack"), 1
                    ),
                    List.of(
                            new DistilleryRecipeOutput(Identifier.fromNamespaceAndPath("minecraft", "soul_sand"), 1),
                            new DistilleryRecipeOutput(modId("demonic_blood"), 2, 2)
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
