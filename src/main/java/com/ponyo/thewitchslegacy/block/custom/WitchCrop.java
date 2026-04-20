package com.ponyo.thewitchslegacy.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class WitchCrop extends CropBlock {
    public enum SupportType {
        FARMLAND,
        WATER_SOURCE
    }

    @FunctionalInterface
    public interface MatureDestroyHandler {
        boolean onDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool, WitchCrop cropBlock);
    }

    private final Supplier<? extends ItemLike> seedItem;
    private final VoxelShape[] shapes;
    private final SupportType supportType;
    @Nullable
    private final MatureDestroyHandler matureDestroyHandler;

    public WitchCrop(Properties properties, Supplier<? extends ItemLike> seedItem, VoxelShape[] shapes) {
        this(properties, seedItem, shapes, SupportType.FARMLAND, null);
    }

    public WitchCrop(Properties properties, Supplier<? extends ItemLike> seedItem, VoxelShape[] shapes, SupportType supportType) {
        this(properties, seedItem, shapes, supportType, null);
    }

    public WitchCrop(Properties properties, Supplier<? extends ItemLike> seedItem, VoxelShape[] shapes,
                     SupportType supportType, @Nullable MatureDestroyHandler matureDestroyHandler) {
        super(properties);
        this.seedItem = seedItem;
        this.shapes = shapes;
        this.supportType = supportType;
        this.matureDestroyHandler = matureDestroyHandler;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seedItem.get();
    }

    @Override
    public int getMaxAge() {
        return shapes.length - 1;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes[getAge(state)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        if (supportType == SupportType.WATER_SOURCE) {
            FluidState fluidState = level.getFluidState(pos);
            FluidState aboveFluidState = level.getFluidState(pos.above());
            return fluidState.getType() == Fluids.WATER && fluidState.isSource() && aboveFluidState.getType() == Fluids.EMPTY;
        }

        return super.mayPlaceOn(state, level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (supportType != SupportType.WATER_SOURCE) {
            super.randomTick(state, level, pos, random);
            return;
        }

        if (!level.isAreaLoaded(pos, 1)) {
            return;
        }

        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = getAge(state);
            if (age < getMaxAge()) {
                float growthSpeed = getWaterGrowthSpeed(state, level, pos);
                if (net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state,
                        random.nextInt((int) (25.0F / growthSpeed) + 1) == 0)) {
                    level.setBlock(pos, getStateForAge(age + 1), 2);
                    net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
                }
            }
        }
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (matureDestroyHandler != null && isMaxAge(state)
                && matureDestroyHandler.onDestroy(level, player, pos, state, blockEntity, tool, this)) {
            return;
        }

        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

    public static VoxelShape[] createShapes(double... heights) {
        VoxelShape[] shapes = new VoxelShape[heights.length];
        for (int i = 0; i < heights.length; i++) {
            shapes[i] = box(0.0D, 0.0D, 0.0D, 16.0D, heights[i], 16.0D);
        }
        return shapes;
    }

    private static float getWaterGrowthSpeed(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        float growthSpeed = 4.0F;

        BlockPos north = pos.north();
        BlockPos south = pos.south();
        BlockPos west = pos.west();
        BlockPos east = pos.east();
        boolean horizontalMatch = level.getBlockState(west).is(block) || level.getBlockState(east).is(block);
        boolean verticalMatch = level.getBlockState(north).is(block) || level.getBlockState(south).is(block);

        if (horizontalMatch && verticalMatch) {
            growthSpeed /= 2.0F;
        } else {
            boolean diagonalMatch = level.getBlockState(west.north()).is(block)
                    || level.getBlockState(east.north()).is(block)
                    || level.getBlockState(east.south()).is(block)
                    || level.getBlockState(west.south()).is(block);
            if (diagonalMatch) {
                growthSpeed /= 2.0F;
            }
        }

        return growthSpeed;
    }
}
