package com.ponyo.thewitchslegacy.item;

import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    public static final FoodProperties GARLIC = new FoodProperties.Builder().nutrition(1).saturationModifier(.25f)
            .alwaysEdible().build();
    public static final FoodProperties ROWAN_BERRY_PIE = new FoodProperties.Builder().nutrition(6).saturationModifier(1.0f)
            .build();
    public static final FoodProperties ROWAN_BERRIES = new FoodProperties.Builder().nutrition(1).saturationModifier(.25f)
            .alwaysEdible().build();
}
