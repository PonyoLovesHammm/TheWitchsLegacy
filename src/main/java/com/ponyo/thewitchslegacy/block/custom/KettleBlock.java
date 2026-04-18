package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KettleBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<KettleBlock> CODEC = simpleCodec(KettleBlock::new);

    // Box args are (minX, minY, minZ, maxX, maxY, maxZ) in 1/16th-block units.
    // Edit this NORTH-facing base shape by hand, then the other facings are derived by rotation.
    // Current parts:
    // - kettle body
    // - lid/top
    // - top bar
    // - left and right side handles
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            // Main kettle body.
            Block.box(3.0, 2.0, 3.0, 13.0, 7.0, 13.0),
            // Hanging bar across the top.
            Block.box(0.0, 14.0, 7.0, 16.0, 16.0, 9.0),
            // Left side handle.
            Block.box(1.0, 3.0, 6.0, 3.0, 6.0, 10.0),
            // Right side handle.
            Block.box(13.0, 3.0, 6.0, 15.0, 6.0, 10.0)
    );
    private static final VoxelShape EAST_SHAPE = rotateShape(NORTH_SHAPE, 1);
    private static final VoxelShape SOUTH_SHAPE = rotateShape(NORTH_SHAPE, 2);
    private static final VoxelShape WEST_SHAPE = rotateShape(NORTH_SHAPE, 3);

    public KettleBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<KettleBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
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

    private static VoxelShape getFacingShape(Direction direction) {
        return switch (direction) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
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
