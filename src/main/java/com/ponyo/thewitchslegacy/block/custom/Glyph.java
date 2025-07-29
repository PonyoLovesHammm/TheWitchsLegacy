package com.ponyo.thewitchslegacy.block.custom;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

//Defines a custom block class for A Glyph Block
public class Glyph extends Block {
    //property used to store different variants of a block, in this case different textures
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 11);

    //constructor, sets the default state of the block to variant 0
    public Glyph(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, 0));
    }

    //spawns particles at the blocks position
    private void spawnSmokeParticles(Level level, BlockPos pos) {
        //determines how many particles will be spawned
        for (int i = 0; i < 5; i++) {
            //determines the offset from the center that the particles will spawn at
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;
            double y = pos.getY() + 0.1;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4;

            //determines the type of particles that will spawn (smoke) and gives them a velocity, upwards at .02
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    //registers the VARIANT property so the blockstate system knows about it
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VARIANT);
    }

    //Defines the visual shape (outline) of the block when rendered
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        //Will render a block one pixel high and missing a border of pixels
        return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);
    }

    //Defines the shape used for collisions
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        //makes the block have no collision (empty=no collision)
        return Shapes.empty();
    }

    //Indicate that this block does not behave like a solid full cube for collision
    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    //called when the player right-clicks the block with an empty hand
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        //Only trigger if this is the specific GOLDEN_GLYPH block
        if (player.getMainHandItem().isEmpty() && state.is(ModBlocks.GOLDEN_GLYPH.get())) {
            //play a sound to provide feedback
            level.playSound(player, pos, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.BLOCKS, 1f, 1f);
            //If on CLIENT SIDE show particles
            if (level.isClientSide()) {
                spawnSmokeParticles(level, pos);
            }
            //returns the interaction as a success to indicate the interaction was handled
            return InteractionResult.SUCCESS;
        }
        //otherwise, pass the interaction to other potential handlers
        return InteractionResult.PASS;
    }
}
