package com.ponyo.thewitchslegacy.ritual.definitions.summoning;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.custom.Glyph;
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
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class ImpSummon {
    private static final String RITUAL_ID = "imp_summon";
    private static final String DISPLAY_NAME_KEY = "ritual.thewitchslegacy.imp_summon";
    private static final int ALTAR_POWER_COST = 5000;
    private static final int CLEAR_RADIUS = 3;
    private static final int CLEAR_HEIGHT = 4;

    private ImpSummon() {
    }

    public static RitualDefinition create() {
        List<RitualItemRequirement> itemRequirements = List.of(
                new RitualItemRequirement(ModItems.DEMONIC_BLOOD.get(), 1, true),
                new RitualItemRequirement(ModItems.ENDER_DEW.get(), 1, true),
                new RitualItemRequirement(ModItems.INFUSED_STONE.get(), 1, true)
        );

        return new RitualDefinition(
                RITUAL_ID,
                DISPLAY_NAME_KEY,
                List.of(RitualRingRequirement.medium(ModBlocks.FIERY_GLYPH.get())),
                itemRequirements,
                ALTAR_POWER_COST,
                (level, centerPos, player, consumedItems) -> {
                    summonPlaceholderImp(level, centerPos);
                    RitualEffects.playCompletionEffects(level, centerPos.above(1));
                    return null;
                },
                (level, centerPos) -> true,
                (level, centerPos, player) -> isInteriorClear(level, centerPos)
                        ? null
                        : Component.translatable("message.thewitchslegacy.interior_must_be_clear")
        );
    }

    private static boolean isInteriorClear(ServerLevel level, BlockPos centerPos) {
        for (BlockPos pos : BlockPos.betweenClosed(
                centerPos.offset(-CLEAR_RADIUS, 0, -CLEAR_RADIUS),
                centerPos.offset(CLEAR_RADIUS, CLEAR_HEIGHT - 1, CLEAR_RADIUS))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.getBlock() instanceof Glyph) {
                continue;
            }
            if (Block.isShapeFullBlock(state.getCollisionShape(level, pos))) {
                return false;
            }
        }
        return true;
    }

    private static void summonPlaceholderImp(ServerLevel level, BlockPos centerPos) {
        Pig pig = EntityType.PIG.create(level, EntitySpawnReason.TRIGGERED);
        if (pig == null) {
            return;
        }

        pig.setCustomName(Component.literal("IMP"));
        pig.setCustomNameVisible(true);
        pig.snapTo(centerPos.getX() + 0.5D, centerPos.getY() + 1.0D, centerPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(pig);
    }
}
