package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import com.ponyo.thewitchslegacy.block.entity.ModBlockEntities;
import com.ponyo.thewitchslegacy.block.entity.WitchOvenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class WitchOven extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<WitchOven> CODEC = simpleCodec(WitchOven::new);
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    // Box args are (minX, minY, minZ, maxX, maxY, maxZ) in 1/16th-block units.
    // Edit this SOUTH-facing base shape by hand, then the other facings are derived by rotation.
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(
            // Main body: rectangular oven body.
            Block.box(1.0, 1.0, 2.0, 15.0, 10.0, 14.0),
            // Top lip: slight overhang near the top.
            Block.box(0.0, 10.0, 1.0, 16.0, 11.0, 15.0),
            // Chimney: centered toward the back half.
            Block.box(3.0, 11.0, 3.0, 13.0, 12.0, 13.0),
            // Front lip/opening: small protrusion on the front face.
            Block.box(6.0, 3.0, 1.0, 10.0, 16.0, 5.0)
    );
    private static final Map<Direction, VoxelShape> SHAPES = VoxelShapeUtils.horizontalFromSouth(SOUTH_SHAPE);

    public WitchOven(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, net.minecraft.core.Direction.NORTH)
                .setValue(BlockStateProperties.LIT, false)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public MapCodec<WitchOven> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BlockStateProperties.LIT, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.LIT, false)
                .setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState,
                                     RandomSource random) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
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

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WitchOvenBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WitchOvenBlockEntity blockEntity && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(blockEntity, pos);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BlockStateProperties.LIT)) {
            return;
        }

        double centerX = pos.getX() + 0.5;
        double centerY = pos.getY();
        double centerZ = pos.getZ() + 0.5;

        if (random.nextDouble() < 0.1) {
            level.playLocalSound(
                    centerX,
                    centerY,
                    centerZ,
                    SoundEvents.FURNACE_FIRE_CRACKLE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F,
                    false
            );
        }

        Direction direction = state.getValue(FACING);
        Direction.Axis axis = direction.getAxis();
        double frontOffset = 0.4575;
        double sideOffset = random.nextDouble() * 0.6 - 0.3;
        double particleY = centerY + 3.0 / 16.0 + random.nextDouble() * 6.0 / 16.0;
        double particleX = centerX + (axis == Direction.Axis.X ? direction.getStepX() * frontOffset : sideOffset);
        double particleZ = centerZ + (axis == Direction.Axis.Z ? direction.getStepZ() * frontOffset : sideOffset);

        level.addParticle(ParticleTypes.SMOKE, particleX, particleY, particleZ, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, particleX, particleY, particleZ, 0.0, 0.0, 0.0);

        // Spawn a lower-rate chimney plume while the oven is lit.
        int chimneyParticles = 1 + (random.nextFloat() < 0.5F ? 1 : 0);
        for (int i = 0; i < chimneyParticles; i++) {
            double chimneyX = pos.getX() + 0.5;
            double chimneyY = pos.getY() + 1.0;
            double chimneyZ = pos.getZ() + 13.0 / 16.0;
            double chimneyXSpeed = random.nextDouble() * 0.02 - 0.01;
            double chimneyYSpeed = 0.03 + random.nextDouble() * 0.01;
            double chimneyZSpeed = random.nextDouble() * 0.02 - 0.01;

            level.addParticle(
                    ParticleTypes.SMOKE,
                    chimneyX,
                    chimneyY,
                    chimneyZ,
                    chimneyXSpeed,
                    chimneyYSpeed,
                    chimneyZSpeed
            );
        }
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level instanceof net.minecraft.server.level.ServerLevel serverLevel
                ? createTickerHelper(blockEntityType, ModBlockEntities.WITCH_OVEN.get(),
                (tickerLevel, tickerPos, tickerState, blockEntity) ->
                        AbstractFurnaceBlockEntity.serverTick(serverLevel, tickerPos, tickerState, blockEntity))
                : null;
    }

    private static VoxelShape getFacingShape(Direction direction) {
        return VoxelShapeUtils.getHorizontalShape(SHAPES, direction, Direction.SOUTH);
    }
}
