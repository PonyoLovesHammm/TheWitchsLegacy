package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import com.ponyo.thewitchslegacy.block.entity.AltarBlockEntity;
import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Altar extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<Altar> CODEC = simpleCodec(Altar::new);

    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Box args are (minX, minY, minZ, maxX, maxY, maxZ) in 1/16th-block units.
    private static final VoxelShape BASE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
    private static final VoxelShape TOP_SHAPE = Block.box(0.0, 7.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape CENTER_SHAPE = Block.box(3.0, 3.0, 3.0, 13.0, 7.0, 13.0);
    private static final VoxelShape NORTH_ARM = Block.box(3.0, 3.0, 0.0, 13.0, 7.0, 3.0);
    private static final VoxelShape EAST_ARM = Block.box(13.0, 3.0, 3.0, 16.0, 7.0, 13.0);
    private static final VoxelShape SOUTH_ARM = Block.box(3.0, 3.0, 13.0, 13.0, 7.0, 16.0);
    private static final VoxelShape WEST_ARM = Block.box(0.0, 3.0, 3.0, 3.0, 7.0, 13.0);

    public Altar(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ACTIVATED, false)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public MapCodec<Altar> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED, NORTH, EAST, SOUTH, WEST, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return updateConnections(context.getLevel(), context.getClickedPos(), this.defaultBlockState()
                .setValue(ACTIVATED, false)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level instanceof net.minecraft.server.level.ServerLevel serverLevel
                ? createTickerHelper(blockEntityType, com.ponyo.thewitchslegacy.block.entity.ModBlockEntities.ALTAR.get(),
                (tickerLevel, tickerPos, tickerState, blockEntity) ->
                        AltarBlockEntity.serverTick(serverLevel, tickerPos, tickerState, blockEntity))
                : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModItems.INFUSED_STONE.get())) {
            return state.getValue(ACTIVATED) ? openAltar(level, pos, player) : InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (state.getValue(ACTIVATED)) {
            return openAltar(level, pos, player);
        }

        List<BlockPos> altarPattern = findValidAltarPattern(level, pos);
        if (altarPattern == null) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            BlockPos controllerPos = altarPattern.getFirst();
            for (BlockPos altarPos : altarPattern) {
                BlockState altarState = level.getBlockState(altarPos);
                level.setBlock(altarPos, altarState.setValue(ACTIVATED, true), 3);
                if (level.getBlockEntity(altarPos) instanceof AltarBlockEntity altarBlockEntity) {
                    altarBlockEntity.setControllerPos(controllerPos);
                }
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return state.getValue(ACTIVATED) ? openAltar(level, pos, player) : InteractionResult.PASS;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos currentPos,
            Direction facing,
            BlockPos facingPos,
            BlockState facingState,
            RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return facing.getAxis().isHorizontal()
                ? updateConnections(level, currentPos, state)
                : super.updateShape(state, level, scheduledTickAccess, currentPos, facing, facingPos, facingState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (state.getValue(ACTIVATED)) {
            deactivateLinkedAltars(level, pos);
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> state.setValue(NORTH, state.getValue(SOUTH))
                    .setValue(EAST, state.getValue(WEST))
                    .setValue(SOUTH, state.getValue(NORTH))
                    .setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> state.setValue(NORTH, state.getValue(EAST))
                    .setValue(EAST, state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(WEST))
                    .setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90 -> state.setValue(NORTH, state.getValue(WEST))
                    .setValue(EAST, state.getValue(NORTH))
                    .setValue(SOUTH, state.getValue(EAST))
                    .setValue(WEST, state.getValue(SOUTH));
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.setValue(NORTH, state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state.setValue(EAST, state.getValue(WEST))
                    .setValue(WEST, state.getValue(EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return buildShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return buildShape(state);
    }

    private static BlockState updateConnections(BlockGetter level, BlockPos pos, BlockState state) {
        return state.setValue(NORTH, connectsTo(level.getBlockState(pos.north())))
                .setValue(EAST, connectsTo(level.getBlockState(pos.east())))
                .setValue(SOUTH, connectsTo(level.getBlockState(pos.south())))
                .setValue(WEST, connectsTo(level.getBlockState(pos.west())));
    }

    private static boolean connectsTo(BlockState state) {
        return state.getBlock() instanceof Altar;
    }

    private static void deactivateLinkedAltars(Level level, BlockPos brokenPos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(
                brokenPos.offset(-2, 0, -2),
                brokenPos.offset(2, 0, 2))) {
            if (!(level.getBlockEntity(checkPos) instanceof AltarBlockEntity altarBlockEntity)) {
                continue;
            }
            if (!altarBlockEntity.isController()) {
                continue;
            }

            deactivateControlledAltars(level, altarBlockEntity.getBlockPos());
        }
    }

    private static void deactivateControlledAltars(Level level, BlockPos controllerPos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(
                controllerPos.offset(-2, 0, -2),
                controllerPos.offset(2, 0, 2))) {
            if (!(level.getBlockEntity(checkPos) instanceof AltarBlockEntity altarBlockEntity)) {
                continue;
            }
            if (!altarBlockEntity.getControllerPos().equals(controllerPos)) {
                continue;
            }

            BlockState altarState = level.getBlockState(checkPos);
            if (altarState.getBlock() instanceof Altar && altarState.getValue(ACTIVATED)) {
                level.setBlock(checkPos, altarState.setValue(ACTIVATED, false), 3);
                altarBlockEntity.setControllerPos(checkPos);
            }
        }
    }

    private static InteractionResult openAltar(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AltarBlockEntity blockEntity && player instanceof ServerPlayer serverPlayer) {
            BlockPos controllerPos = blockEntity.getControllerPos();
            if (level.getBlockEntity(controllerPos) instanceof AltarBlockEntity controllerEntity) {
                controllerEntity.openMenu(serverPlayer);
            } else {
                blockEntity.openMenu(serverPlayer);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static @Nullable List<BlockPos> findValidAltarPattern(Level level, BlockPos clickedPos) {
        List<BlockPos> pattern = findValidAltarPattern(level, clickedPos, 2, 3);
        if (pattern != null) {
            return pattern;
        }

        return findValidAltarPattern(level, clickedPos, 3, 2);
    }

    private static @Nullable List<BlockPos> findValidAltarPattern(Level level, BlockPos clickedPos, int width, int depth) {
        for (int offsetX = 0; offsetX < width; offsetX++) {
            for (int offsetZ = 0; offsetZ < depth; offsetZ++) {
                BlockPos origin = clickedPos.offset(-offsetX, 0, -offsetZ);
                List<BlockPos> positions = new ArrayList<>(width * depth);
                boolean valid = true;

                for (int dx = 0; dx < width && valid; dx++) {
                    for (int dz = 0; dz < depth; dz++) {
                        BlockPos checkPos = origin.offset(dx, 0, dz);
                        if (!(level.getBlockState(checkPos).getBlock() instanceof Altar)) {
                            valid = false;
                            break;
                        }
                        positions.add(checkPos);
                    }
                }

                if (valid) {
                    return positions;
                }
            }
        }

        return null;
    }

    private static VoxelShape buildShape(BlockState state) {
        VoxelShape shape = Shapes.or(BASE_SHAPE, TOP_SHAPE, CENTER_SHAPE);
        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_ARM);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_ARM);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_ARM);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_ARM);
        }
        return shape;
    }
}
