package com.ponyo.thewitchslegacy.ritual.definitions.general;

import com.ponyo.thewitchslegacy.block.custom.Glyph;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.Chalk;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualPatterns;
import com.ponyo.thewitchslegacy.ritual.RitualRingMatcher;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingSize;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class GlyphicTransformation {
    private static final String DISPLAY_NAME_KEY = "ritual.thewitchslegacy.glyphic_transformation";

    private GlyphicTransformation() {
    }

    public static RitualDefinition createSmall() {
        return create("glyphic_transformation_small", RitualRingSize.SMALL, 1, 16);
    }

    public static RitualDefinition createMedium() {
        return create("glyphic_transformation_medium", RitualRingSize.MEDIUM, 2, 28);
    }

    public static RitualDefinition createLarge() {
        return create("glyphic_transformation_large", RitualRingSize.LARGE, 3, 40);
    }

    public static List<RitualDefinition> createAll() {
        return List.of(
                createSmall(),
                createMedium(),
                createLarge()
        );
    }

    private static RitualDefinition create(String id, RitualRingSize ringSize, int seleniteCount, int chalkDamage) {
        List<RitualItemRequirement> itemRequirements = List.of(
                transformChalkRequirement(),
                new RitualItemRequirement(ModItems.SELENITE_SHARD.get(), seleniteCount, true)
        );

        return new RitualDefinition(
                id,
                DISPLAY_NAME_KEY,
                List.of(),
                itemRequirements,
                0,
                (level, centerPos, player, consumedItems) -> {
                    ItemStack chalkStack = findTransformChalk(consumedItems);
                    if (chalkStack.isEmpty()) {
                        return null;
                    }

                    transformRing(level, centerPos, ringSize, chalkStack);

                    BlockPos outputPos = centerPos.above(1);
                    RitualEffects.playCompletionEffects(level, outputPos);
                    ItemStack returnedChalk = damageChalk(chalkStack, chalkDamage);
                    if (!returnedChalk.isEmpty()) {
                        RitualEffects.spawnOutputItem(level, outputPos, returnedChalk);
                    }
                    return null;
                },
                createRingMatcher(ringSize)
        );
    }

    private static RitualItemRequirement transformChalkRequirement() {
        return new RitualItemRequirement(
                ModItems.WHITE_CHALK.get(),
                1,
                true,
                Chalk::isTransformChalk
        );
    }

    private static RitualRingMatcher createRingMatcher(RitualRingSize ringSize) {
        return (level, centerPos) -> {
            for (BlockPos offset : RitualPatterns.positionsFor(ringSize)) {
                if (!(level.getBlockState(centerPos.offset(offset)).getBlock() instanceof Glyph)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static void transformRing(net.minecraft.server.level.ServerLevel level, BlockPos centerPos, RitualRingSize ringSize, ItemStack chalkStack) {
        for (BlockPos offset : RitualPatterns.positionsFor(ringSize)) {
            BlockPos glyphPos = centerPos.offset(offset);
            BlockState existingState = level.getBlockState(glyphPos);
            int variant = existingState.hasProperty(Glyph.VARIANT) ? existingState.getValue(Glyph.VARIANT) : 0;
            BlockState transformedState = Chalk.glyphStateForStack(chalkStack, variant);
            if (transformedState != null) {
                level.setBlock(glyphPos, transformedState, 3);
            }
        }
    }

    private static ItemStack findTransformChalk(List<ItemStack> consumedItems) {
        for (ItemStack stack : consumedItems) {
            if (Chalk.isTransformChalk(stack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack damageChalk(ItemStack chalkStack, int amount) {
        ItemStack damaged = chalkStack.copy();
        int newDamage = damaged.getDamageValue() + amount;
        if (newDamage >= damaged.getMaxDamage()) {
            return ItemStack.EMPTY;
        }

        damaged.setDamageValue(newDamage);
        return damaged;
    }
}
