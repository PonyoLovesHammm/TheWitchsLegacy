package com.ponyo.thewitchslegacy.recipe;

import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.Poppet;
import com.ponyo.thewitchslegacy.item.custom.Waystone;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class HungerProtectionPoppetFeedingRecipe extends CustomRecipe {
    public HungerProtectionPoppetFeedingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        Ingredients ingredients = findIngredients(input);
        return ingredients != null && canFeed(ingredients.poppet(), ingredients.food());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Ingredients ingredients = findIngredients(input);
        if (ingredients == null || !canFeed(ingredients.poppet(), ingredients.food())) {
            return ItemStack.EMPTY;
        }

        ItemStack result = ingredients.poppet().copyWithCount(1);
        int addedDurability = foodDurabilityValue(ingredients.food());
        int currentMax = result.getMaxDamage();
        int currentRemaining = currentMax - result.getDamageValue();
        int newMax = Math.min(Poppet.HUNGER_PROTECTION_MAX_DURABILITY, currentMax + addedDurability);
        int newRemaining = Math.min(Poppet.HUNGER_PROTECTION_MAX_DURABILITY, currentRemaining + addedDurability);

        result.set(DataComponents.MAX_DAMAGE, newMax);
        result.setDamageValue(Math.max(0, newMax - newRemaining));
        return result;
    }

    @Override
    public RecipeSerializer<HungerProtectionPoppetFeedingRecipe> getSerializer() {
        return ModRecipeSerializers.HUNGER_PROTECTION_POPPET_FEEDING.get();
    }

    private static Ingredients findIngredients(CraftingInput input) {
        if (input.ingredientCount() != 2) {
            return null;
        }

        ItemStack poppet = ItemStack.EMPTY;
        ItemStack food = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.HUNGER_PROTECTION_POPPET.get())) {
                if (!poppet.isEmpty()) {
                    return null;
                }
                poppet = stack;
            } else if (stack.has(DataComponents.FOOD)) {
                if (!food.isEmpty()) {
                    return null;
                }
                food = stack;
            } else {
                return null;
            }
        }

        return poppet.isEmpty() || food.isEmpty() ? null : new Ingredients(poppet, food);
    }

    private static boolean canFeed(ItemStack poppet, ItemStack food) {
        return Waystone.getBloodTarget(poppet).isPresent()
                && poppet.getMaxDamage() - poppet.getDamageValue() < Poppet.HUNGER_PROTECTION_MAX_DURABILITY
                && foodDurabilityValue(food) > 0;
    }

    private static int foodDurabilityValue(ItemStack food) {
        FoodProperties properties = food.get(DataComponents.FOOD);
        if (properties == null) {
            return 0;
        }
        return (int) Math.ceil(properties.nutrition() + properties.saturation());
    }

    private record Ingredients(ItemStack poppet, ItemStack food) {
    }
}
