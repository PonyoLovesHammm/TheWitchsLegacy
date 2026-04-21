package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class GlintWeed extends FaceAttachedHorizontalDirectionalBlock {
    public static final MapCodec<GlintWeed> CODEC = simpleCodec(GlintWeed::new);
    private static final VoxelShape FLOOR_OR_CEILING_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 13.0D, 13.0D);
    private static final VoxelShape NORTH_SHAPE = Block.box(3.0D, 3.0D, 6.0D, 13.0D, 13.0D, 16.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(3.0D, 3.0D, 0.0D, 13.0D, 13.0D, 10.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(6.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(0.0D, 3.0D, 3.0D, 10.0D, 13.0D, 13.0D);

    public GlintWeed(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACE, AttachFace.FLOOR)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<GlintWeed> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis() != Direction.Axis.Y) {
            return null;
        }

        BlockState preferredState = defaultBlockState()
                .setValue(FACE, clickedFace == Direction.UP ? AttachFace.FLOOR : AttachFace.CEILING)
                .setValue(FACING, context.getHorizontalDirection());

        if (preferredState.canSurvive(context.getLevel(), context.getClickedPos())) {
            return preferredState;
        }

        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(FACE) == AttachFace.WALL) {
            return false;
        }

        return super.canSurvive(state, level, pos);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(FACE) == AttachFace.FLOOR;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(FACE) != AttachFace.FLOOR || random.nextInt(25) != 0) {
            return;
        }

        int nearbyCount = 5;
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            BlockState checkState = level.getBlockState(checkPos);
            if (checkState.is(this) && checkState.getValue(FACE) == AttachFace.FLOOR && --nearbyCount <= 0) {
                return;
            }
        }

        BlockPos spreadPos = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        for (int i = 0; i < 4; i++) {
            if (level.isEmptyBlock(spreadPos)) {
                BlockState spreadState = defaultBlockState()
                        .setValue(FACE, AttachFace.FLOOR)
                        .setValue(FACING, randomHorizontalFacing(random));
                if (spreadState.canSurvive(level, spreadPos)) {
                    pos = spreadPos;
                }
            }

            spreadPos = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
        }

        if (level.isEmptyBlock(spreadPos)) {
            BlockState spreadState = defaultBlockState()
                    .setValue(FACE, AttachFace.FLOOR)
                    .setValue(FACING, randomHorizontalFacing(random));
            if (spreadState.canSurvive(level, spreadPos)) {
                level.setBlock(spreadPos, spreadState, 2);
            }
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACE)) {
            case FLOOR, CEILING -> FLOOR_OR_CEILING_SHAPE;
            case WALL -> FLOOR_OR_CEILING_SHAPE;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    private static Direction randomHorizontalFacing(RandomSource random) {
        return Direction.Plane.HORIZONTAL.getRandomDirection(random);
    }
}
