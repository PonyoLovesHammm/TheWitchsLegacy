package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.block.custom.WitchCauldron;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class WitchCauldronBlockEntity extends BlockEntity {
    private static final int HEAT_UP_TICKS = 100;
    private static final int DEFAULT_LIQUID_COLOR = 0x3F76E4;
    private static final String HEATING_TICKS_TAG = "HeatingTicks";
    private static final String LIQUID_COLOR_TAG = "LiquidColor";

    private int heatingTicks;
    private int liquidColor = DEFAULT_LIQUID_COLOR;

    public WitchCauldronBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WITCH_CAULDRON.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WitchCauldronBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        boolean shouldHeat = state.getValue(WitchCauldron.LEVEL) == 3 && isHeatSource(level.getBlockState(pos.below()));
        int nextHeatingTicks = shouldHeat ? Math.min(blockEntity.heatingTicks + 1, HEAT_UP_TICKS) : 0;
        boolean nextBubbling = shouldHeat && nextHeatingTicks >= HEAT_UP_TICKS;
        boolean bubbling = state.getValue(WitchCauldron.BUBBLING);

        if (nextHeatingTicks != blockEntity.heatingTicks) {
            blockEntity.heatingTicks = nextHeatingTicks;
            blockEntity.setChanged();
        }

        if (nextBubbling != bubbling) {
            BlockState newState = state.setValue(WitchCauldron.BUBBLING, nextBubbling);
            level.setBlock(pos, newState, 3);
            level.sendBlockUpdated(pos, state, newState, 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(HEATING_TICKS_TAG, this.heatingTicks);
        output.putInt(LIQUID_COLOR_TAG, this.liquidColor);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.heatingTicks = input.getIntOr(HEATING_TICKS_TAG, 0);
        this.liquidColor = input.getIntOr(LIQUID_COLOR_TAG, DEFAULT_LIQUID_COLOR);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int getLiquidColor() {
        return this.liquidColor;
    }

    public void setLiquidColor(int liquidColor) {
        if (this.liquidColor != liquidColor) {
            this.liquidColor = liquidColor;
            this.setChanged();
        }
    }

    private static boolean isHeatSource(BlockState state) {
        if (state.is(Blocks.LAVA) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.MAGMA_BLOCK)) {
            return true;
        }

        return isLitCampfire(state);
    }

    private static boolean isLitCampfire(BlockState state) {
        return state.is(BlockTags.CAMPFIRES) && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT);
    }
}
