package com.ponyo.thewitchslegacy.worldgen.tree;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.Optional;

public final class ModTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ROWAN_TREE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, "rowan")
    );

    public static final TreeGrower ROWAN = new TreeGrower(
            "rowan",
            Optional.empty(),
            Optional.of(ROWAN_TREE),
            Optional.empty()
    );

    private ModTreeGrowers() {
    }
}
