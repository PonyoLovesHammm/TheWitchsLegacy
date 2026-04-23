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

public class Chalice extends Block {
    public static final MapCodec<Chalice> CODEC = simpleCodec(Chalice::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(6.5, 0.0, 6.5, 9.5, 0.5, 9.5),
            Block.box(7.5, 0.5, 7.5, 8.5, 3.0, 8.5),
            Block.box(5.5, 3.0, 5.5, 10.5, 7.0, 10.5)
    );

    public Chalice(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<Chalice> codec() {
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
