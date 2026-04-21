package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.menu.WitchOvenMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WitchOvenBlockEntity extends AbstractFurnaceBlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int JAR_SLOT = 3;
    public static final int EXTRA_SLOT = 4;

    public WitchOvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WITCH_OVEN.get(), pos, state, RecipeType.SMELTING);
        this.items = NonNullList.withSize(5, ItemStack.EMPTY);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thewitchslegacy.witch_oven");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new WitchOvenMenu(containerId, inventory, this, this.dataAccess);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == JAR_SLOT) {
            return stack.is(com.ponyo.thewitchslegacy.item.ModItems.CLAY_JAR.get());
        }

        if (slot == EXTRA_SLOT) {
            return false;
        }

        return super.canPlaceItem(slot, stack);
    }

    public ContainerData getDataAccess() {
        return this.dataAccess;
    }
}
