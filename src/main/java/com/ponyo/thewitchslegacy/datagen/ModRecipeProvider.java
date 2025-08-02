/*
package com.ponyo.thewitchslegacy.datagen;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        List<ItemLike> CLAY_JAR_SMELTABLES = List.of(ModItems.CLAY_JAR, ModItems.SOFT_CLAY_JAR);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ROWAN_BERRY_PIE.get())
                .pattern(" X ")
                .pattern("XAX")
                .pattern("BCB")
                .define('X', ModItems.ROWAN_BERRIES.get())
                .define('A', Items.EGG)
                .define('B', Items.WHEAT)
                .define('C', Items.MILK_BUCKET)
                .unlockedBy("has_rowan_berries", has(ModItems.ROWAN_BERRIES)).save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BONE_NEEDLE.get(), 8)
                .requires(Items.BONE)
                .requires(Items.FLINT)
                .unlockedBy("has_bone", has(Items.BONE)).save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.WITCHS_CLAIM.get())
                .requires(Items.GLASS_BOTTLE)
                .requires(ModItems.BONE_NEEDLE.get())
                .unlockedBy("has_bone_needle", has(ModItems.BONE_NEEDLE.get())).save(recipeOutput);

    }
}


 */