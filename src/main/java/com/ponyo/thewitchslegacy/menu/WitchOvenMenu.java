package com.ponyo.thewitchslegacy.menu;

import com.ponyo.thewitchslegacy.block.entity.WitchOvenBlockEntity;
import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class WitchOvenMenu extends AbstractContainerMenu {
    private static final int INPUT_SLOT = 0;
    private static final int FUEL_SLOT = 1;
    private static final int RESULT_SLOT = 2;
    private static final int JAR_SLOT = 3;
    private static final int EXTRA_SLOT = 4;
    private static final int DATA_COUNT = 4;
    private static final int MACHINE_SLOT_COUNT = 5;
    private static final int INV_SLOT_START = MACHINE_SLOT_COUNT;
    private static final int INV_SLOT_END = INV_SLOT_START + 27;
    private static final int HOTBAR_SLOT_START = INV_SLOT_END;
    private static final int HOTBAR_SLOT_END = HOTBAR_SLOT_START + 9;

    private final Container container;
    private final ContainerData data;
    private final Level level;
    private final net.minecraft.world.item.crafting.RecipePropertySet acceptedInputs;

    public WitchOvenMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, getClientDependencies(inventory, extraData));
    }

    public WitchOvenMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenuTypes.WITCH_OVEN.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.level = inventory.player.level();
        this.acceptedInputs = this.level.recipeAccess().propertySet(RecipePropertySet.FURNACE_INPUT);

        this.addSlot(new Slot(container, INPUT_SLOT, 56, 17));
        this.addSlot(new Slot(container, FUEL_SLOT, 56, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return WitchOvenMenu.this.isFuel(stack) || FurnaceFuelSlot.isBucket(stack);
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return FurnaceFuelSlot.isBucket(stack) ? 1 : super.getMaxStackSize(stack);
            }
        });
        this.addSlot(new FurnaceResultSlot(inventory.player, container, RESULT_SLOT, 116, 35));
        this.addSlot(new Slot(container, JAR_SLOT, 83, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.CLAY_JAR.get());
            }
        });
        this.addSlot(new Slot(container, EXTRA_SLOT, 142, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(inventory);
        this.addDataSlots(data);
    }

    private WitchOvenMenu(int containerId, Inventory inventory, ClientDependencies dependencies) {
        this(containerId, inventory, dependencies.container(), dependencies.data());
    }

    private static ClientDependencies getClientDependencies(Inventory inventory, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof WitchOvenBlockEntity blockEntity) {
            return new ClientDependencies(blockEntity, blockEntity.getDataAccess());
        }

        return new ClientDependencies(new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
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

        if (index == RESULT_SLOT) {
            if (!this.moveItemStackTo(stack, INV_SLOT_START, HOTBAR_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copied);
        } else if (index < MACHINE_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, INV_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.CLAY_JAR.get())) {
            if (!this.moveItemStackTo(stack, JAR_SLOT, JAR_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (this.canSmelt(stack)) {
            if (!this.moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (this.isFuel(stack)) {
            if (!this.moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < INV_SLOT_END) {
            if (!this.moveItemStackTo(stack, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < HOTBAR_SLOT_END) {
            if (!this.moveItemStackTo(stack, INV_SLOT_START, INV_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
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

    private boolean canSmelt(ItemStack stack) {
        return this.acceptedInputs.test(stack);
    }

    private boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING, this.level.fuelValues()) > 0;
    }

    public float getBurnProgress() {
        int cookingTime = this.data.get(2);
        int totalCookingTime = this.data.get(3);
        return totalCookingTime != 0 && cookingTime != 0 ? Mth.clamp((float) cookingTime / totalCookingTime, 0.0F, 1.0F) : 0.0F;
    }

    public float getLitProgress() {
        int litTotalTime = this.data.get(1);
        if (litTotalTime == 0) {
            litTotalTime = 200;
        }

        return Mth.clamp((float) this.data.get(0) / litTotalTime, 0.0F, 1.0F);
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    private record ClientDependencies(Container container, ContainerData data) {
    }
}
