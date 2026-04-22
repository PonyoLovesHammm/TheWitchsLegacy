package com.ponyo.thewitchslegacy.menu;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.entity.AltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class AltarMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 4;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    public AltarMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, getClientDependencies(inventory, extraData));
    }

    public AltarMenu(int containerId, Inventory inventory, ContainerData data, BlockPos pos) {
        this(containerId, inventory, data, ContainerLevelAccess.create(inventory.player.level(), pos));
    }

    private AltarMenu(int containerId, Inventory inventory, ClientDependencies dependencies) {
        this(containerId, inventory, dependencies.data(), dependencies.access());
    }

    private AltarMenu(int containerId, Inventory inventory, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.ALTAR.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.data = data;
        this.access = access;
        this.addDataSlots(data);
    }

    private static ClientDependencies getClientDependencies(Inventory inventory, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        if (inventory.player.level().getBlockEntity(pos) instanceof AltarBlockEntity blockEntity) {
            return new ClientDependencies(blockEntity.getDataAccess(), ContainerLevelAccess.create(inventory.player.level(), pos));
        }

        return new ClientDependencies(new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.create(inventory.player.level(), pos));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ALTAR.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public int getCurrentPower() {
        return this.data.get(0);
    }

    public int getMaxPower() {
        return this.data.get(1);
    }

    public int getAccumulationRate() {
        return this.data.get(2);
    }

    public int getDistributionRange() {
        return this.data.get(3);
    }

    private record ClientDependencies(ContainerData data, ContainerLevelAccess access) {
    }
}
