package com.ponyo.thewitchslegacy.recipe;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, TheWitchsLegacy.MODID);

    public static final Supplier<RecipeSerializer<HungerProtectionPoppetFeedingRecipe>> HUNGER_PROTECTION_POPPET_FEEDING =
            RECIPE_SERIALIZERS.register("hunger_protection_poppet_feeding",
                    () -> new CustomRecipe.Serializer<>(HungerProtectionPoppetFeedingRecipe::new));
    public static final Supplier<RecipeSerializer<PoppetBindingRecipe>> POPPET_BINDING =
            RECIPE_SERIALIZERS.register("poppet_binding",
                    () -> new CustomRecipe.Serializer<>(PoppetBindingRecipe::new));

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
