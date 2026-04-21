package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WalkthroughLeaves extends TintedParticleLeavesBlock {
    public static final MapCodec<WalkthroughLeaves> CODEC = simpleCodec(WalkthroughLeaves::new);

    public WalkthroughLeaves(BlockBehaviour.Properties properties) {
        this(0.01F, properties);
    }

    public WalkthroughLeaves(float leafParticleChance, BlockBehaviour.Properties properties) {
        super(leafParticleChance, properties);
    }

    @Override
    public MapCodec<WalkthroughLeaves> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }
}
