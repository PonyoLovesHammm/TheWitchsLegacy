package com.ponyo.thewitchslegacy.entity;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, TheWitchsLegacy.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MandrakeEntity>> MANDRAKE = ENTITY_TYPES.register(
            "mandrake",
            () -> EntityType.Builder.of(MandrakeEntity::new, MobCategory.CREATURE)
                    .sized(0.8F, 1.0F)
                    .eyeHeight(0.65F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "mandrake")))
    );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @EventBusSubscriber(modid = TheWitchsLegacy.MODID)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(MANDRAKE.get(), MandrakeEntity.createAttributes().build());
        }
    }
}
