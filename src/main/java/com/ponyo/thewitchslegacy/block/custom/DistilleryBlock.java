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

import java.util.Map;

public class DistilleryBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<DistilleryBlock> CODEC = simpleCodec(DistilleryBlock::new);

    // Box args are (minX, minY, minZ, maxX, maxY, maxZ) in 1/16th-block units.
    // Edit this SOUTH-facing base shape by hand, then the other facings are derived by rotation.
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
            // Front tray/platform.
            Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 5.0),
            // Front jar/burner row.
            Block.box(0.0, 1.0, 1.0, 16.0, 8.0, 4.0),

            // Rear vat/body.
            Block.box(3.0, 0.0, 6.0, 13.0, 7.0, 16.0),
            // Top vat body
            Block.box(5.0, 7.0, 8.0, 11.0, 13.0, 14.0),
            // Rear top fitting.
            Block.box(6.5, 13.0, 9.5, 9.5, 15.0, 12.5)
    );
    private static final Map<Direction, VoxelShape> SHAPES = VoxelShapeUtils.horizontalFromSouth(SOUTH_SHAPE);

    public DistilleryBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<DistilleryBlock> codec() {
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
        return VoxelShapeUtils.getHorizontalShape(SHAPES, direction, Direction.SOUTH);
    }
}
