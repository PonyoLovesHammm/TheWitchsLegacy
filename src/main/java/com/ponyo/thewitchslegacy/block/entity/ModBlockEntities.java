package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheWitchsLegacy.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WitchOvenBlockEntity>> WITCH_OVEN =
            BLOCK_ENTITIES.register("witch_oven",
                    () -> new BlockEntityType<>(WitchOvenBlockEntity::new, java.util.Set.of(ModBlocks.WITCH_OVEN.get())));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
