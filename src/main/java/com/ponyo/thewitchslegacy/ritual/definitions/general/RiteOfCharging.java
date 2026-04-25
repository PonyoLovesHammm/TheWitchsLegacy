package com.ponyo.thewitchslegacy.ritual.definitions.general;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;

import java.util.List;

public final class RiteOfCharging {
    private RiteOfCharging() {
    }

    public static RitualDefinition create() {
        return new RitualDefinition(
                "charged_infused_stone",
                List.of(
                        RitualRingRequirement.small(ModBlocks.WHITE_GLYPH.get()),
                        RitualRingRequirement.medium(ModBlocks.WHITE_GLYPH.get())
                ),
                List.of(
                        new RitualItemRequirement(ModItems.INFUSED_STONE.get(), 1, true),
                        new RitualItemRequirement(Items.GLOWSTONE_DUST, 1, true),
                        new RitualItemRequirement(Items.REDSTONE, 1, true),
                        new RitualItemRequirement(ModItems.WOOD_ASH.get(), 1, true),
                        new RitualItemRequirement(ModItems.TOUCH_OF_REGROWTH.get(), 1, true)
                ),
                2000,
                (level, centerPos, player, consumedItems) -> {
                    BlockPos outputPos = centerPos.above(1);
                    RitualEffects.playCompletionEffects(level, outputPos);
                    RitualEffects.spawnOutputItem(level, outputPos, ModItems.INFUSED_STONE_CHARGED.get());
                    return null;
                }
        );
    }
}
