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
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR =
            BLOCK_ENTITIES.register("altar",
                    () -> new BlockEntityType<>(AltarBlockEntity::new, java.util.Set.of(ModBlocks.ALTAR.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WitchCauldronBlockEntity>> WITCH_CAULDRON =
            BLOCK_ENTITIES.register("witch_cauldron",
                    () -> new BlockEntityType<>(WitchCauldronBlockEntity::new, java.util.Set.of(ModBlocks.WITCH_CAULDRON.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistilleryBlockEntity>> DISTILLERY =
            BLOCK_ENTITIES.register("distillery",
                    () -> new BlockEntityType<>(DistilleryBlockEntity::new, java.util.Set.of(ModBlocks.DISTILLERY.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpinningWheelBlockEntity>> SPINNING_WHEEL =
            BLOCK_ENTITIES.register("spinning_wheel",
                    () -> new BlockEntityType<>(SpinningWheelBlockEntity::new, java.util.Set.of(ModBlocks.SPINNING_WHEEL.get())));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
