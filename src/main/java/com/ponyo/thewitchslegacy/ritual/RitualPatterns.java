package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;

import java.util.List;

public final class RitualPatterns {
    private static final List<BlockPos> SMALL_RING = List.of(
            offset(0, 3), offset(1, 3), offset(2, 2), offset(3, 1), offset(3, 0), offset(3, -1), offset(2, -2), offset(1, -3),
            offset(0, -3), offset(-1, -3), offset(-2, -2), offset(-3, -1), offset(-3, 0), offset(-3, 1), offset(-2, 2), offset(-1, 3)
    );
    private static final List<BlockPos> MEDIUM_RING = List.of(
            offset(0, 5), offset(1, 5), offset(2, 5), offset(3, 4), offset(4, 3), offset(5, 2), offset(5, 1), offset(5, 0),
            offset(5, -1), offset(5, -2), offset(4, -3), offset(3, -4), offset(2, -5), offset(1, -5), offset(0, -5), offset(-1, -5),
            offset(-2, -5), offset(-3, -4), offset(-4, -3), offset(-5, -2), offset(-5, -1), offset(-5, 0), offset(-5, 1), offset(-5, 2),
            offset(-4, 3), offset(-3, 4), offset(-2, 5), offset(-1, 5)
    );
    private static final List<BlockPos> LARGE_RING = List.of(
            offset(0, 7), offset(1, 7), offset(2, 7), offset(3, 7), offset(4, 6), offset(5, 5), offset(6, 4), offset(7, 3),
            offset(7, 2), offset(7, 1), offset(7, 0), offset(7, -1), offset(7, -2), offset(7, -3), offset(6, -4), offset(5, -5),
            offset(4, -6), offset(3, -7), offset(2, -7), offset(1, -7), offset(0, -7), offset(-1, -7), offset(-2, -7), offset(-3, -7),
            offset(-4, -6), offset(-5, -5), offset(-6, -4), offset(-7, -3), offset(-7, -2), offset(-7, -1), offset(-7, 0), offset(-7, 1),
            offset(-7, 2), offset(-7, 3), offset(-6, 4), offset(-5, 5), offset(-4, 6), offset(-3, 7), offset(-2, 7), offset(-1, 7)
    );

    private RitualPatterns() {
    }

    public static List<BlockPos> positionsFor(RitualRingSize size) {
        return switch (size) {
            case SMALL -> SMALL_RING;
            case MEDIUM -> MEDIUM_RING;
            case LARGE -> LARGE_RING;
        };
    }

    private static BlockPos offset(int x, int z) {
        return new BlockPos(x, 0, z);
    }
}
