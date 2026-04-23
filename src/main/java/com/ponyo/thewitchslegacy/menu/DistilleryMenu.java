package com.ponyo.thewitchslegacy.menu;

import com.ponyo.thewitchslegacy.block.entity.DistilleryBlockEntity;
import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DistilleryMenu extends AbstractContainerMenu {
    private static final int INV_SLOT_START = DistilleryBlockEntity.SLOT_COUNT;
    private static final int INV_SLOT_END = INV_SLOT_START + 27;
    private static final int HOTBAR_SLOT_START = INV_SLOT_END;
    private static final int HOTBAR_SLOT_END = HOTBAR_SLOT_START + 9;

    private final Container container;

    public DistilleryMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, getContainer(inventory, extraData));
    }

    public DistilleryMenu(int containerId, Inventory inventory, Container container) {
        super(ModMenuTypes.DISTILLERY.get(), containerId);
        checkContainerSize(container, DistilleryBlockEntity.SLOT_COUNT);
        this.container = container;

        this.addSlot(new Slot(container, DistilleryBlockEntity.INPUT_SLOT_A, 91, 16));
        this.addSlot(new Slot(container, DistilleryBlockEntity.INPUT_SLOT_B, 91, 34));
        this.addSlot(new Slot(container, DistilleryBlockEntity.JAR_SLOT, 91, 56) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.CLAY_JAR.get());
            }
        });

        this.addSlot(new OutputSlot(container, DistilleryBlockEntity.OUTPUT_SLOT_START, 129, 25));
        this.addSlot(new OutputSlot(container, DistilleryBlockEntity.OUTPUT_SLOT_START + 1, 147, 25));
        this.addSlot(new OutputSlot(container, DistilleryBlockEntity.OUTPUT_SLOT_START + 2, 129, 43));
        this.addSlot(new OutputSlot(container, DistilleryBlockEntity.OUTPUT_SLOT_START + 3, 147, 43));

        addPlayerInventory(inventory);
    }

    private static Container getContainer(Inventory inventory, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof DistilleryBlockEntity blockEntity) {
            return blockEntity;
        }

        return new SimpleContainer(DistilleryBlockEntity.SLOT_COUNT);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int slot = 0; slot < 9; slot++) {
            this.addSlot(new Slot(inventory, slot, 8 + slot * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        copied = stack.copy();

        if (index < DistilleryBlockEntity.SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, INV_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.CLAY_JAR.get())) {
            if (!this.moveItemStackTo(stack, DistilleryBlockEntity.JAR_SLOT, DistilleryBlockEntity.JAR_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, DistilleryBlockEntity.INPUT_SLOT_A, DistilleryBlockEntity.INPUT_SLOT_B + 1, false)) {
            if (index < INV_SLOT_END) {
                if (!this.moveItemStackTo(stack, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < HOTBAR_SLOT_END && !this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == copied.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return copied;
    }

    private static class OutputSlot extends Slot {
        private OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
