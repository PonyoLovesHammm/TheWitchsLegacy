package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import com.ponyo.thewitchslegacy.block.entity.ModBlockEntities;
import com.ponyo.thewitchslegacy.block.entity.WitchCauldronBlockEntity;
import com.ponyo.thewitchslegacy.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class WitchCauldron extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<WitchCauldron> CODEC = simpleCodec(WitchCauldron::new);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final BooleanProperty BUBBLING = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final int MAX_LEVEL = 3;
    private static final double HIT_EPSILON = 1.0E-4;
    private static final double PARTICLE_MIN = 4.0;
    private static final double PARTICLE_RANGE = 8.0;
    private static final double PARTICLE_Y = 9.85 / 16.0;

    // Box args are (minX, minY, minZ, maxX, maxY, maxZ) in 1/16th-block units.
    // This shape uses:
    // - a 10x10 inner base, 1 voxel thick, extending two voxels lower than before
    // - four outer walls forming the cauldron rim/body
    private static final VoxelShape SHAPE = Shapes.or(
            // Base plate: centered floor of the cauldron bowl.
            Block.box(1.0, 2.0, 1.0, 15.0, 4.0, 15.0),
            // West wall: outer left side.
            Block.box(1.0, 4.0, 1.0, 2.0, 13.0, 15.0),
            // East wall: outer right side.
            Block.box(14.0, 4.0, 1.0, 15.0, 13.0, 15.0),
            // North wall: outer back side.
            Block.box(2.0, 4.0, 1.0, 14.0, 13.0, 2.0),
            // South wall: outer front side.
            Block.box(2.0, 4.0, 14.0, 14.0, 13.0, 15.0)
    );
    private static final List<AABB> SHAPE_BOXES = SHAPE.toAabbs();

    public WitchCauldron(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LEVEL, 0)
                .setValue(BUBBLING, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public MapCodec<WitchCauldron> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, BUBBLING, WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WitchCauldronBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.WITCH_CAULDRON.get(), WitchCauldronBlockEntity::serverTick);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        int currentLevel = state.getValue(LEVEL);

        if (stack.is(Items.WATER_BUCKET)) {
            if (!hitCauldronGeometry(hit, pos)) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }

            return currentLevel == MAX_LEVEL ? InteractionResult.SUCCESS : fillFromWaterBucket(level, pos, player, hand, stack);
        }

        if (stack.is(Items.BUCKET)) {
            return currentLevel == MAX_LEVEL ? takeWaterWithBucket(level, pos, state, player, hand, stack) : InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (stack.is(Items.GLASS_BOTTLE)) {
            return currentLevel > 0 ? fillBottle(level, pos, state, player, hand, stack) : InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (isWaterPotion(stack)) {
            return currentLevel < MAX_LEVEL ? pourWaterBottle(level, pos, state, player, hand, stack) : InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LEVEL) != MAX_LEVEL || !state.getValue(BUBBLING)) {
            return;
        }

        int particleCount = 1 + random.nextInt(2);
        for (int i = 0; i < particleCount; i++) {
            double particleX = pos.getX() + (PARTICLE_MIN + random.nextDouble() * PARTICLE_RANGE) / 16.0;
            double particleY = pos.getY() + PARTICLE_Y;
            double particleZ = pos.getZ() + (PARTICLE_MIN + random.nextDouble() * PARTICLE_RANGE) / 16.0;

            level.addParticle(ModParticles.CAULDRON_BUBBLE.get(), particleX, particleY, particleZ, 0.0, 0.0, 0.0);

            if (random.nextFloat() < 0.18F) {
                level.addParticle(ParticleTypes.BUBBLE_POP, particleX, particleY + 0.03, particleZ, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction side) {
        return state.getValue(LEVEL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    private static InteractionResult fillFromWaterBucket(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
        player.awardStat(Stats.FILL_CAULDRON);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        setLevel(level, pos, level.getBlockState(pos), MAX_LEVEL, GameEvent.FLUID_PLACE);
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult takeWaterWithBucket(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, ItemStack stack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.WATER_BUCKET)));
        player.awardStat(Stats.USE_CAULDRON);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        setLevel(level, pos, state, 0, GameEvent.FLUID_PICKUP);
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult fillBottle(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, ItemStack stack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
        player.awardStat(Stats.USE_CAULDRON);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        setLevel(level, pos, state, state.getValue(LEVEL) - 1, GameEvent.FLUID_PICKUP);
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult pourWaterBottle(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, ItemStack stack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
        player.awardStat(Stats.USE_CAULDRON);
        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        setLevel(level, pos, state, state.getValue(LEVEL) + 1, GameEvent.FLUID_PLACE);
        level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }

    private static void setLevel(Level level, BlockPos pos, BlockState state, int newLevel, Holder<GameEvent> gameEvent) {
        BlockState newState = state.setValue(LEVEL, newLevel);
        level.setBlockAndUpdate(pos, newState);
        level.gameEvent(null, gameEvent, pos);
    }

    private static boolean hitCauldronGeometry(BlockHitResult hit, BlockPos pos) {
        Vec3 localHit = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());

        for (AABB box : SHAPE_BOXES) {
            if (localHit.x >= box.minX - HIT_EPSILON && localHit.x <= box.maxX + HIT_EPSILON
                    && localHit.y >= box.minY - HIT_EPSILON && localHit.y <= box.maxY + HIT_EPSILON
                    && localHit.z >= box.minZ - HIT_EPSILON && localHit.z <= box.maxZ + HIT_EPSILON) {
                return true;
            }
        }

        return false;
    }

    private static boolean isWaterPotion(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }

        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        return potionContents != null && potionContents.is(Potions.WATER);
    }
}
