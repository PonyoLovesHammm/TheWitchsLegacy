package com.ponyo.thewitchslegacy.worldgen.tree;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public final class ModTreeGrowers {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATORS =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, TheWitchsLegacy.MODID);

    public static final ResourceKey<ConfiguredFeature<?, ?>> ROWAN_TREE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "rowan")
    );
    public static final ResourceKey<ConfiguredFeature<?, ?>> HAWTHORN_TREE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "hawthorn")
    );
    public static final ResourceKey<ConfiguredFeature<?, ?>> WILLOW_TREE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "willow")
    );

    public static final TreeGrower ROWAN = new TreeGrower(
            "rowan",
            Optional.empty(),
            Optional.of(ROWAN_TREE),
            Optional.empty()
    );
    public static final TreeGrower HAWTHORN = new TreeGrower(
            "hawthorn",
            Optional.empty(),
            Optional.of(HAWTHORN_TREE),
            Optional.empty()
    );
    public static final TreeGrower WILLOW = new TreeGrower(
            "willow",
            Optional.empty(),
            Optional.of(WILLOW_TREE),
            Optional.empty()
    );

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<HangingLeavesDecorator>> WILLOW_HANGING_LEAVES =
            TREE_DECORATORS.register("willow_hanging_leaves", () -> new TreeDecoratorType<>(HangingLeavesDecorator.CODEC));

    private ModTreeGrowers() {
    }

    public static void register(IEventBus eventBus) {
        TREE_DECORATORS.register(eventBus);
    }
}
