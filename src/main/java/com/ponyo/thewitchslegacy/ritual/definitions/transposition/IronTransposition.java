package com.ponyo.thewitchslegacy.ritual.definitions.transposition;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualPatterns;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public final class IronTransposition {
    private IronTransposition() {
    }

    public static RitualDefinition create() {
        List<RitualItemRequirement> itemRequirements = List.of(
                new RitualItemRequirement(ModItems.ENDER_DEW.get(), 1, true),
                new RitualItemRequirement(Items.IRON_INGOT, 1, true),
                new RitualItemRequirement(Items.BLAZE_POWDER, 1, true),
                new RitualItemRequirement(ModItems.DIAMOND_VAPOR.get(), 1, true),
                new RitualItemRequirement(ModItems.INFUSED_STONE_CHARGED.get(), 1, true)
        );

        return new RitualDefinition(
                "iron_transposition",
                "ritual.thewitchslegacy.iron_transposition",
                List.of(RitualRingRequirement.large(ModBlocks.OTHERWHERE_GLYPH.get())),
                itemRequirements,
                0,
                (level, centerPos, player, consumedItems) -> {
                    boolean movedIron = performTransposition(level, centerPos);
                    BlockPos outputPos = centerPos.above(1);
                    RitualEffects.playCompletionEffects(level, outputPos);
                    RitualEffects.spawnChargedStoneRemainderIfNeeded(level, outputPos, itemRequirements);
                    if (!movedIron) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.thewitchslegacy.no_iron_found_below"), true);
                    }
                    return null;
                }
        );
    }

    private static boolean performTransposition(net.minecraft.server.level.ServerLevel level, BlockPos centerPos) {
        boolean movedAny = false;
        for (BlockPos offset : RitualPatterns.filledPositionsFor(RitualRingSize.LARGE)) {
            BlockPos columnTop = centerPos.offset(offset);
            movedAny |= scanColumn(level, centerPos, columnTop);
        }
        return movedAny;
    }

    private static boolean scanColumn(net.minecraft.server.level.ServerLevel level, BlockPos centerPos, BlockPos columnTop) {
        boolean movedAny = false;
        for (int y = centerPos.getY() - 1; y >= level.getMinY(); y--) {
            BlockPos scanPos = new BlockPos(columnTop.getX(), y, columnTop.getZ());
            Block block = level.getBlockState(scanPos).getBlock();
            if (!isTransposableIronBlock(block)) {
                continue;
            }

            Block replacement = y >= 2 ? Blocks.STONE : Blocks.DEEPSLATE;
            level.setBlock(scanPos, replacement.defaultBlockState(), 3);
            RitualEffects.spawnItem(level, new BlockPos(scanPos.getX(), centerPos.getY(), scanPos.getZ()), block.asItem());
            movedAny = true;
        }
        return movedAny;
    }

    private static boolean isTransposableIronBlock(Block block) {
        return block == Blocks.IRON_ORE
                || block == Blocks.DEEPSLATE_IRON_ORE
                || block == Blocks.RAW_IRON_BLOCK;
    }
}
