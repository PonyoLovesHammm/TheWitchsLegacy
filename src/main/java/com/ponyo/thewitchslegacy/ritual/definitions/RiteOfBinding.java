package com.ponyo.thewitchslegacy.ritual.definitions;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.CircleTalisman;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualPatterns;
import com.ponyo.thewitchslegacy.ritual.RitualRingSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public final class RiteOfBinding {
    private RiteOfBinding() {
    }

    public static RitualDefinition create() {
        return new RitualDefinition(
                "rite_of_binding",
                List.of(),
                List.of(
                        new RitualItemRequirement(ModItems.CIRCLE_TALISMAN.get(), 1, true, CircleTalisman::isEmpty),
                        new RitualItemRequirement(Items.REDSTONE, 1, true)
                ),
                1000,
                (level, centerPos) -> {
                    List<CircleTalisman.RingData> rings = detectBindableRings(level, centerPos);
                    if (rings.isEmpty()) {
                        return;
                    }

                    clearMatchedRings(level, centerPos, rings);

                    BlockPos outputPos = centerPos.above(1);
                    RitualEffects.playCompletionEffects(level, outputPos);
                    RitualEffects.spawnItem(level, outputPos, CircleTalisman.createWithRings(ModItems.CIRCLE_TALISMAN.get(), rings));
                },
                (level, centerPos) -> !detectBindableRings(level, centerPos).isEmpty()
        );
    }

    private static List<CircleTalisman.RingData> detectBindableRings(net.minecraft.server.level.ServerLevel level, BlockPos centerPos) {
        List<CircleTalisman.RingData> rings = new ArrayList<>();

        for (RitualRingSize size : RitualRingSize.values()) {
            for (CircleTalisman.RingColor color : CircleTalisman.RingColor.values()) {
                if (matchesRing(level, centerPos, size, color.glyphBlock())) {
                    rings.add(new CircleTalisman.RingData(size, color));
                    break;
                }
            }
        }

        return rings;
    }

    private static boolean matchesRing(net.minecraft.server.level.ServerLevel level, BlockPos centerPos, RitualRingSize size, Block glyphBlock) {
        for (BlockPos offset : RitualPatterns.positionsFor(size)) {
            if (!level.getBlockState(centerPos.offset(offset)).is(glyphBlock)) {
                return false;
            }
        }
        return true;
    }

    private static void clearMatchedRings(net.minecraft.server.level.ServerLevel level, BlockPos centerPos, List<CircleTalisman.RingData> rings) {
        if (level.getBlockState(centerPos).is(ModBlocks.GOLDEN_GLYPH.get())) {
            level.removeBlock(centerPos, false);
        }

        for (CircleTalisman.RingData ring : rings) {
            for (BlockPos offset : RitualPatterns.positionsFor(ring.size())) {
                BlockPos glyphPos = centerPos.offset(offset);
                level.removeBlock(glyphPos, false);
            }
        }
    }
}
