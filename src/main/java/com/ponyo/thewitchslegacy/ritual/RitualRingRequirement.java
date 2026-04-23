package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.world.level.block.Block;

public record RitualRingRequirement(RitualRingSize size, Block glyphBlock) {
    public static RitualRingRequirement small(Block glyphBlock) {
        return new RitualRingRequirement(RitualRingSize.SMALL, glyphBlock);
    }

    public static RitualRingRequirement medium(Block glyphBlock) {
        return new RitualRingRequirement(RitualRingSize.MEDIUM, glyphBlock);
    }

    public static RitualRingRequirement large(Block glyphBlock) {
        return new RitualRingRequirement(RitualRingSize.LARGE, glyphBlock);
    }
}
