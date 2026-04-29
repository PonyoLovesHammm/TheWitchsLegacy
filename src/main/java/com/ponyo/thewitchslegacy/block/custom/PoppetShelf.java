package com.ponyo.thewitchslegacy.block.custom;

import com.mojang.serialization.MapCodec;
import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.entity.PoppetShelfBlockEntity;
import com.ponyo.thewitchslegacy.block.entity.PoppetShelfSavedData;
import com.ponyo.thewitchslegacy.menu.PoppetShelfMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jspecify.annotations.Nullable;

public class PoppetShelf extends BaseEntityBlock {
    public static final MapCodec<PoppetShelf> CODEC = simpleCodec(PoppetShelf::new);
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public PoppetShelf(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<PoppetShelf> codec() {
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
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PoppetShelfBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        openShelf(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        openShelf(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, net.minecraft.core.Direction side) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity blockEntity) {
            Containers.dropContents(level, pos, blockEntity);
            blockEntity.clearContent();
            PoppetShelfSavedData.get(serverLevel).removeShelf(serverLevel, pos);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity blockEntity) {
            Containers.dropContents(level, pos, blockEntity);
        }
        PoppetShelfSavedData.get(level).removeShelf(level, pos);
        Containers.updateNeighboursAfterDestroy(state, level, pos);
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !event.getLevel().getBlockState(event.getPos()).is(ModBlocks.POPPET_SHELF.get())) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }

        openShelf(event.getLevel(), event.getPos(), event.getEntity());
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void openShelf(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            MenuConstructor menu = (containerId, inventory, menuPlayer) -> {
                if (level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity blockEntity) {
                    return new PoppetShelfMenu(containerId, inventory, blockEntity, pos);
                }
                return new PoppetShelfMenu(containerId, inventory, new SimpleContainer(PoppetShelfBlockEntity.SLOT_COUNT), pos);
            };
            serverPlayer.openMenu(new SimpleMenuProvider(
                    menu,
                    Component.translatable("block.thewitchslegacy.poppet_shelf")
            ), buffer -> buffer.writeBlockPos(pos));
        }
    }
}
