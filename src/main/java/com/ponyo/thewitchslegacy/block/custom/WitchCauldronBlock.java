package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WitchCauldronBlock extends Block {
    public static final MapCodec<WitchCauldronBlock> CODEC = simpleCodec(WitchCauldronBlock::new);

    // Box args are (minX, minY, minZ, maxX, maxY, maxZ) in 1/16th-block units.
    // This shape uses:
    // - a 10x10 inner base, 1 voxel thick, sitting one voxel above the ground
    // - four outer walls forming the cauldron rim/body
    private static final VoxelShape SHAPE = Shapes.or(
            // Base plate: centered floor of the cauldron bowl.
            Block.box(3.0, 3.0, 3.0, 13.0, 4.0, 13.0),
            // West wall: outer left side.
            Block.box(1.0, 4.0, 1.0, 2.0, 13.0, 15.0),
            // East wall: outer right side.
            Block.box(14.0, 4.0, 1.0, 15.0, 13.0, 15.0),
            // North wall: outer back side.
            Block.box(2.0, 4.0, 1.0, 14.0, 13.0, 2.0),
            // South wall: outer front side.
            Block.box(2.0, 4.0, 14.0, 14.0, 13.0, 15.0)
    );

    public WitchCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<WitchCauldronBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
