package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.item.custom.Poppet;
import com.ponyo.thewitchslegacy.menu.PoppetShelfMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PoppetShelfBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_COUNT = 9;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private boolean syncingFromSavedData;

    public PoppetShelfBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.POPPET_SHELF.get(), pos, blockState);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        syncFromSavedDataOrPublish();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thewitchslegacy.poppet_shelf");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new PoppetShelfMenu(containerId, inventory, this, this.worldPosition);
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return Poppet.isAnyPoppet(stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!this.syncingFromSavedData) {
            syncToSavedData();
        }
    }

    public void setItemFromSavedData(int slot, ItemStack stack) {
        this.syncingFromSavedData = true;
        try {
            super.setItem(slot, stack.copy());
        } finally {
            this.syncingFromSavedData = false;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        syncFromSavedDataOrPublish();
    }

    private void syncFromSavedDataOrPublish() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }

        PoppetShelfSavedData savedData = PoppetShelfSavedData.get(serverLevel);
        savedData.getShelfItems(serverLevel, this.worldPosition).ifPresentOrElse(
                savedItems -> {
                    this.syncingFromSavedData = true;
                    try {
                        this.items = savedItems;
                        super.setChanged();
                    } finally {
                        this.syncingFromSavedData = false;
                    }
                },
                this::syncToSavedData
        );
    }

    private void syncToSavedData() {
        if (this.level instanceof ServerLevel serverLevel) {
            PoppetShelfSavedData.get(serverLevel).updateShelf(serverLevel, this.worldPosition, this.items);
        }
    }
}
