package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CandelabraBlock extends Block {
    public static final MapCodec<CandelabraBlock> CODEC = simpleCodec(CandelabraBlock::new);
    private static final double[][] FLAME_OFFSETS = {
            {0.5, 1.075, 0.5},
            {0.5, 0.95, 0.1875},
            {0.5, 0.95, 0.8125},
            {0.1875, 0.95, 0.5},
            {0.8125, 0.95, 0.5}
    };

    // Box args are (minX, minY, minZ, maxX, maxY, maxZ) in 1/16th-block units.
    private static final VoxelShape SHAPE = Shapes.or(
            // Base plate.
            Block.box(5.5, 0.0, 5.5, 10.5, 1.0, 10.5),
            // North-south arm.
            Block.box(7.0, 4.0, 2.0, 9.0, 5.0, 14.0),
            // East-west arm.
            Block.box(2.0, 4.0, 7.0, 14.0, 5.0, 9.0),

            // North candle.
            Block.box(7.0, 5.0, 2.0, 9.0, 13.0, 4.0),
            // South candle.
            Block.box(7.0, 5.0, 12.0, 9.0, 13.0, 14.0),
            // West candle.
            Block.box(2.0, 5.0, 7.0, 4.0, 13.0, 9.0),
            // East candle.
            Block.box(12.0, 5.0, 7.0, 14.0, 13.0, 9.0),
            // Center candle.
            Block.box(7.0, 1.0, 7.0, 9.0, 15.0, 9.0)
    );

    public CandelabraBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<CandelabraBlock> codec() {
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

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (double[] offset : FLAME_OFFSETS) {
            addCandleParticlesAndSound(level, new Vec3(
                    pos.getX() + offset[0],
                    pos.getY() + offset[1],
                    pos.getZ() + offset[2]
            ), random);
        }
    }

    private static void addCandleParticlesAndSound(Level level, Vec3 offset, RandomSource random) {
        float chance = random.nextFloat();
        if (chance < 0.3F) {
            level.addParticle(ParticleTypes.SMOKE, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
            if (chance < 0.17F) {
                level.playLocalSound(
                        offset.x + 0.5,
                        offset.y + 0.5,
                        offset.z + 0.5,
                        SoundEvents.CANDLE_AMBIENT,
                        SoundSource.BLOCKS,
                        1.0F + random.nextFloat(),
                        random.nextFloat() * 0.7F + 0.3F,
                        false
                );
            }
        }

        level.addParticle(ParticleTypes.SMALL_FLAME, offset.x, offset.y, offset.z, 0.0, 0.0, 0.0);
    }
}
