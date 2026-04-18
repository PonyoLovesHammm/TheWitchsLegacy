package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MagicMirrorBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<MagicMirrorBlock> CODEC = simpleCodec(MagicMirrorBlock::new);

    private static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
    private static final VoxelShape WEST_SHAPE = rotateShape(SOUTH_SHAPE, 1);
    private static final VoxelShape NORTH_SHAPE = rotateShape(SOUTH_SHAPE, 2);
    private static final VoxelShape EAST_SHAPE = rotateShape(SOUTH_SHAPE, 3);

    public MagicMirrorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.SOUTH)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public MapCodec<MagicMirrorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.DOUBLE_BLOCK_HALF);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (!clickedFace.getAxis().isHorizontal()) {
            return null;
        }

        Direction attachment = clickedFace.getOpposite();
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();

        if (canPlacePair(level, clickedPos, clickedPos.above(), attachment)) {
            return this.defaultBlockState()
                    .setValue(FACING, attachment)
                    .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        }

        if (canPlacePair(level, clickedPos.below(), clickedPos, attachment)) {
            return this.defaultBlockState()
                    .setValue(FACING, attachment)
                    .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        }

        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        BlockPos otherPos = getOtherHalfPos(pos, state);
        BlockState otherState = state.setValue(
                BlockStateProperties.DOUBLE_BLOCK_HALF,
                state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER
        );
        level.setBlock(otherPos, otherState, 3);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction attachment = state.getValue(FACING);
        BlockPos wallPos = pos.relative(attachment);
        return level.getBlockState(wallPos).isFaceSturdy(level, wallPos, attachment.getOpposite());
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            net.minecraft.world.level.ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            net.minecraft.util.RandomSource random
    ) {
        if (!state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }

        if (direction.getAxis().isVertical() && !isMatchingOtherHalf(state, neighborState)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide()) {
            BlockPos otherPos = getOtherHalfPos(pos, state);
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this)) {
                level.destroyBlock(otherPos, false);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getFacingShape(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getFacingShape(state.getValue(FACING));
    }

    private static boolean canPlacePair(Level level, BlockPos lowerPos, BlockPos upperPos, Direction attachment) {
        return level.getBlockState(lowerPos).canBeReplaced()
                && level.getBlockState(upperPos).canBeReplaced()
                && isWallSturdy(level, lowerPos, attachment)
                && isWallSturdy(level, upperPos, attachment);
    }

    private static boolean isWallSturdy(LevelAccessor level, BlockPos pos, Direction attachment) {
        BlockPos wallPos = pos.relative(attachment);
        return level.getBlockState(wallPos).isFaceSturdy(level, wallPos, attachment.getOpposite());
    }

    private static BlockPos getOtherHalfPos(BlockPos pos, BlockState state) {
        return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
    }

    private static boolean isMatchingOtherHalf(BlockState state, BlockState otherState) {
        return otherState.is(state.getBlock())
                && otherState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)
                && otherState.getValue(FACING) == state.getValue(FACING);
    }

    private static VoxelShape getFacingShape(Direction direction) {
        return switch (direction) {
            case WEST -> WEST_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> SOUTH_SHAPE;
        };
    }

    private static VoxelShape rotateShape(VoxelShape shape, int quarterTurns) {
        VoxelShape rotated = shape;
        for (int i = 0; i < quarterTurns; i++) {
            VoxelShape current = rotated;
            final VoxelShape[] next = {Shapes.empty()};
            current.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    next[0] = Shapes.or(
                            next[0],
                            Shapes.box(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX)
                    )
            );
            rotated = next[0];
        }
        return rotated;
    }
}
