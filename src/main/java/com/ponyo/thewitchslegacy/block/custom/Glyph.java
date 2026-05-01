package com.ponyo.thewitchslegacy.block.custom;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.Chalk;
import com.ponyo.thewitchslegacy.ritual.RitualManager;
import com.ponyo.thewitchslegacy.ritual.SustainingRitualManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Glyph extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 11);

    public Glyph(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VARIANT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        // A thin rune drawn on the top surface of the block below.
        return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        if (state.is(ModBlocks.WHITE_GLYPH.get())) {
            return new ItemStack(ModItems.WHITE_CHALK.get());
        }
        if (state.is(ModBlocks.GOLDEN_GLYPH.get())) {
            return new ItemStack(ModItems.GOLDEN_CHALK.get());
        }
        if (state.is(ModBlocks.FIERY_GLYPH.get())) {
            return new ItemStack(ModItems.FIERY_CHALK.get());
        }
        if (state.is(ModBlocks.OTHERWHERE_GLYPH.get())) {
            return new ItemStack(ModItems.OTHERWHERE_CHALK.get());
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (state.is(ModBlocks.GOLDEN_GLYPH.get())) {
            if (!RitualManager.cancelActiveRitualFromBrokenGlyph(level, pos)) {
                SustainingRitualManager.cancelActiveFromBrokenGlyph(level, pos);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, orientation, isMoving);

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        if (belowState.isAir()) {
            level.destroyBlock(pos, false);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof Chalk) {
            return InteractionResult.PASS;
        }
        return tryTriggerRitual(state, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return tryTriggerRitual(state, level, pos, player);
    }

    private InteractionResult tryTriggerRitual(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.is(ModBlocks.GOLDEN_GLYPH.get())) {
            level.playSound(player, pos, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.BLOCKS, 1f, 1f);
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
                if (RitualManager.isRitualActive(serverLevel, pos)) {
                    RitualManager.cancelActiveRitual(serverLevel, pos, serverPlayer);
                } else if (SustainingRitualManager.isActive(serverLevel, pos)) {
                    SustainingRitualManager.cancelActive(serverLevel, pos, serverPlayer);
                } else {
                    RitualManager.tryTrigger(serverLevel, pos, serverPlayer);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
