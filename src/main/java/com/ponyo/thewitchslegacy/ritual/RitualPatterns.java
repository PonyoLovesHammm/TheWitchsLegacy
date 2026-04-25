package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static List<BlockPos> filledPositionsFor(RitualRingSize size) {
        Map<Integer, Integer> minXByZ = new HashMap<>();
        Map<Integer, Integer> maxXByZ = new HashMap<>();
        for (BlockPos offset : positionsFor(size)) {
            int z = offset.getZ();
            minXByZ.merge(z, offset.getX(), Math::min);
            maxXByZ.merge(z, offset.getX(), Math::max);
        }

        List<BlockPos> filled = new ArrayList<>();
        minXByZ.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .forEach(entry -> {
                    int z = entry.getKey();
                    int minX = entry.getValue();
                    int maxX = maxXByZ.get(z);
                    for (int x = minX; x <= maxX; x++) {
                        filled.add(offset(x, z));
                    }
                });

        return List.copyOf(filled);
    }

    private static BlockPos offset(int x, int z) {
        return new BlockPos(x, 0, z);
    }
}
