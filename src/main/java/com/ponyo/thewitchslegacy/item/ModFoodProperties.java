package com.ponyo.thewitchslegacy.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    public static final FoodProperties GARLIC = new FoodProperties.Builder().nutrition(1).saturationModifier(.25f)
            .alwaysEdible().effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0f).build();
    public static final FoodProperties ROWAN_BERRY_PIE = new FoodProperties.Builder().nutrition(6).saturationModifier(1.0f)
            .build();
    public static final FoodProperties ROWAN_BERRIES = new FoodProperties.Builder().nutrition(1).saturationModifier(.25f)
            .alwaysEdible().effect(() -> new MobEffectInstance(MobEffects.POISON, 200, 0), 1.0f).build();
}
