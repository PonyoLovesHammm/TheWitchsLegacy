package com.ponyo.thewitchslegacy.ritual.definitions.summoning;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.Items;

import java.util.List;

public final class WitchSummon {
    private static final String RITUAL_ID = "witch_summon";
    private static final String DISPLAY_NAME_KEY = "ritual.thewitchslegacy.witch_summon";
    private static final int ALTAR_POWER_COST = 2000;

    private WitchSummon() {
    }

    public static RitualDefinition create() {
        List<RitualItemRequirement> itemRequirements = List.of(
                new RitualItemRequirement(ModItems.DIAMOND_VAPOR.get(), 1, true),
                new RitualItemRequirement(Items.FERMENTED_SPIDER_EYE, 1, true)
        );

        return new RitualDefinition(
                RITUAL_ID,
                DISPLAY_NAME_KEY,
                List.of(RitualRingRequirement.small(ModBlocks.FIERY_GLYPH.get())),
                itemRequirements,
                ALTAR_POWER_COST,
                (level, centerPos, player, consumedItems) -> {
                    summonWitch(level, centerPos);
                    RitualEffects.playCompletionEffects(level, centerPos.above(1));
                    return null;
                }
        );
    }

    private static void summonWitch(ServerLevel level, BlockPos centerPos) {
        Witch witch = EntityType.WITCH.create(level, EntitySpawnReason.TRIGGERED);
        if (witch == null) {
            return;
        }

        witch.snapTo(centerPos.getX() + 0.5D, centerPos.getY() + 1.0D, centerPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(witch);
    }
}
