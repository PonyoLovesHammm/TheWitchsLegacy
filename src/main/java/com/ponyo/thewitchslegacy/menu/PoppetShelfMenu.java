package com.ponyo.thewitchslegacy.menu;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.entity.PoppetShelfBlockEntity;
import com.ponyo.thewitchslegacy.item.custom.Poppet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PoppetShelfMenu extends AbstractContainerMenu {
    private static final int INV_SLOT_START = PoppetShelfBlockEntity.SLOT_COUNT;
    private static final int INV_SLOT_END = INV_SLOT_START + 27;
    private static final int HOTBAR_SLOT_START = INV_SLOT_END;
    private static final int HOTBAR_SLOT_END = HOTBAR_SLOT_START + 9;

    private final ContainerLevelAccess access;

    public PoppetShelfMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, getClientDependencies(inventory, extraData));
    }

    public PoppetShelfMenu(int containerId, Inventory inventory, Container container, BlockPos pos) {
        this(containerId, inventory, container, ContainerLevelAccess.create(inventory.player.level(), pos));
    }

    private PoppetShelfMenu(int containerId, Inventory inventory, ClientDependencies dependencies) {
        this(containerId, inventory, dependencies.container(), dependencies.access());
    }

    private PoppetShelfMenu(int containerId, Inventory inventory, Container container, ContainerLevelAccess access) {
        super(ModMenuTypes.POPPET_SHELF.get(), containerId);
        checkContainerSize(container, PoppetShelfBlockEntity.SLOT_COUNT);
        this.access = access;

        for (int slot = 0; slot < PoppetShelfBlockEntity.SLOT_COUNT; slot++) {
            this.addSlot(new Slot(container, slot, 8 + slot * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return Poppet.isAnyPoppet(stack);
                }
            });
        }

        addPlayerInventory(inventory);
    }

    private static ClientDependencies getClientDependencies(Inventory inventory, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof PoppetShelfBlockEntity blockEntity) {
            return new ClientDependencies(blockEntity, ContainerLevelAccess.create(inventory.player.level(), pos));
        }

        return new ClientDependencies(new SimpleContainer(PoppetShelfBlockEntity.SLOT_COUNT), ContainerLevelAccess.create(inventory.player.level(), pos));
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 50 + row * 18));
            }
        }

        for (int slot = 0; slot < 9; slot++) {
            this.addSlot(new Slot(inventory, slot, 8 + slot * 18, 108));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.POPPET_SHELF.get());
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

        if (index < PoppetShelfBlockEntity.SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, INV_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (Poppet.isAnyPoppet(stack)) {
            if (!this.moveItemStackTo(stack, 0, PoppetShelfBlockEntity.SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < INV_SLOT_END) {
            if (!this.moveItemStackTo(stack, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < HOTBAR_SLOT_END && !this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, false)) {
            return ItemStack.EMPTY;
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

    private record ClientDependencies(Container container, ContainerLevelAccess access) {
    }
}
