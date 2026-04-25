package com.ponyo.thewitchslegacy.ritual.definitions.general;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.familiar.FamiliarManager;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualPatterns;
import com.ponyo.thewitchslegacy.ritual.RitualRingMatcher;
import com.ponyo.thewitchslegacy.ritual.RitualRingSize;
import com.ponyo.thewitchslegacy.ritual.RitualStartValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

public final class FamiliarBinding {
    private static final double FAMILIAR_RADIUS = 4.0D;

    private FamiliarBinding() {
    }

    public static RitualDefinition create() {
        List<RitualItemRequirement> itemRequirements = List.of(
                new RitualItemRequirement(ModItems.BREATH_OF_THE_GODDESS.get(), 1, true),
                new RitualItemRequirement(Items.COD, 1, true, stack -> stack.is(ItemTags.FISHES))
        );

        return new RitualDefinition(
                "familiar_binding",
                "ritual.thewitchslegacy.familiar_binding",
                List.of(),
                itemRequirements,
                8000,
                (level, centerPos, player, consumedItems) -> {
                    Cat cat = findBindableCat(level, centerPos, player);
                    if (cat == null) {
                        return null;
                    }

                    cat.setCustomName(Component.literal("steve"));
                    FamiliarManager.bindCatFamiliar(player, cat);

                    RitualEffects.playCompletionEffects(level, centerPos.above());
                    return null;
                },
                createRingMatcher(),
                createStartValidator()
        );
    }

    private static RitualRingMatcher createRingMatcher() {
        return FamiliarBinding::matchesMediumWhiteRing;
    }

    private static RitualStartValidator createStartValidator() {
        return (level, centerPos, player) -> {
            if (FamiliarManager.playerHasFamiliar(level, player)) {
                return Component.translatable("message.thewitchslegacy.one_familiar_only");
            }
            if (findBindableCat(level, centerPos, player) == null) {
                return Component.translatable("message.thewitchslegacy.no_tamed_cat_for_ritual");
            }
            return null;
        };
    }

    private static boolean matchesMediumWhiteRing(ServerLevel level, BlockPos centerPos) {
        return matchesRing(level, centerPos, ModBlocks.WHITE_GLYPH.get());
    }

    private static boolean matchesRing(ServerLevel level, BlockPos centerPos, net.minecraft.world.level.block.Block glyphBlock) {
        for (BlockPos offset : RitualPatterns.positionsFor(RitualRingSize.MEDIUM)) {
            if (!level.getBlockState(centerPos.offset(offset)).is(glyphBlock)) {
                return false;
            }
        }
        return true;
    }

    private static Cat findBindableCat(ServerLevel level, BlockPos centerPos, ServerPlayer player) {
        return level.getEntitiesOfClass(
                        Cat.class,
                        new AABB(centerPos).inflate(FAMILIAR_RADIUS),
                        cat -> cat.isAlive()
                                && cat.isTame()
                                && player.equals(cat.getOwner())
                                && !FamiliarManager.isFamiliar(level, cat)
                ).stream()
                .min(Comparator.comparingDouble(cat -> cat.distanceToSqr(centerPos.getX() + 0.5D, centerPos.getY() + 0.5D, centerPos.getZ() + 0.5D)))
                .orElse(null);
    }
}
