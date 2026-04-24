package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

@FunctionalInterface
public interface RitualRingMatcher {
    boolean matches(ServerLevel level, BlockPos centerPos);

    static RitualRingMatcher allRequired(List<RitualRingRequirement> requirements) {
        return (level, centerPos) -> {
            for (RitualRingRequirement requirement : requirements) {
                for (var offset : RitualPatterns.positionsFor(requirement.size())) {
                    if (!level.getBlockState(centerPos.offset(offset)).is(requirement.glyphBlock())) {
                        return false;
                    }
                }
            }
            return true;
        };
    }
}
