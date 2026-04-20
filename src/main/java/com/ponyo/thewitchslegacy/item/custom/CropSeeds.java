package com.ponyo.thewitchslegacy.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class CropSeeds extends Item {
    private final Supplier<Block> cropBlock;
    private final boolean placeOnWater;

    public CropSeeds(Supplier<Block> cropBlock, Properties properties) {
        this(cropBlock, properties, false);
    }

    public CropSeeds(Supplier<Block> cropBlock, Properties properties, boolean placeOnWater) {
        super(properties);
        this.cropBlock = cropBlock;
        this.placeOnWater = placeOnWater;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        return placeCrop(context, context.getClickedPos().above());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!placeOnWater) {
            return super.use(level, player, hand);
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != BlockHitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        return placeCrop(new UseOnContext(player, hand, hitResult), hitResult.getBlockPos().above());
    }

    private InteractionResult placeCrop(UseOnContext context, BlockPos placePos) {
        Level level = context.getLevel();
        BlockState cropState = cropBlock.get().defaultBlockState();

        if (!level.getBlockState(placePos).canBeReplaced()) {
            return InteractionResult.PASS;
        }

        if (!cropState.canSurvive(level, placePos)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            level.setBlock(placePos, cropState, 3);
            level.playSound(null, placePos, cropState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);

            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
