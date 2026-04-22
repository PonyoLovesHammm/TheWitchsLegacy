package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.menu.WitchOvenMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class WitchOvenBlockEntity extends AbstractFurnaceBlockEntity {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int JAR_SLOT = 3;
    public static final int EXTRA_SLOT = 4;
    private static final float BASE_FUME_CHANCE = 0.30F;
    private static final Map<Item, Supplier<Item>> FUME_BY_INPUT = Map.ofEntries(
            Map.entry(Blocks.OAK_SAPLING.asItem(), ModItems.BARK_OF_THE_ANCIENT),
            Map.entry(Blocks.SPRUCE_SAPLING.asItem(), ModItems.TOUCH_OF_REGROWTH),
            Map.entry(Blocks.BIRCH_SAPLING.asItem(), ModItems.BREATH_OF_THE_GODDESS),
            Map.entry(Blocks.JUNGLE_SAPLING.asItem(), ModItems.HEART_OF_THE_WILD),
            Map.entry(Blocks.ACACIA_SAPLING.asItem(), ModItems.GUST_OF_THE_ARID_WINDS),
            Map.entry(Blocks.DARK_OAK_SAPLING.asItem(), ModItems.ECHOES_OF_THE_LOST),
            Map.entry(Blocks.CHERRY_SAPLING.asItem(), ModItems.SCENT_OF_SERENITY),
            Map.entry(ModBlocks.ROWAN_SAPLING.get().asItem(), ModItems.WHIFF_OF_MAGIC),
            Map.entry(ModBlocks.HAWTHORN_SAPLING.get().asItem(), ModItems.PRICKLE_THROUGH_THE_VEIL),
            Map.entry(ModBlocks.WILLOW_SAPLING.get().asItem(), ModItems.ROOT_OF_REMEMBRANCE)
    );

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
            return stack.is(ModItems.CLAY_JAR.get());
        }

        if (slot == EXTRA_SLOT) {
            return false;
        }

        return super.canPlaceItem(slot, stack);
    }

    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    public static void tickServer(ServerLevel level, BlockPos pos, BlockState state, WitchOvenBlockEntity blockEntity) {
        ItemStack inputBeforeTick = blockEntity.getItem(INPUT_SLOT).copy();
        ItemStack resultBeforeTick = blockEntity.getItem(RESULT_SLOT).copy();

        AbstractFurnaceBlockEntity.serverTick(level, pos, state, blockEntity);
        blockEntity.tryCreateFume(level, inputBeforeTick, resultBeforeTick);
    }

    private void tryCreateFume(ServerLevel level, ItemStack inputBeforeTick, ItemStack resultBeforeTick) {
        if (!didSmeltComplete(inputBeforeTick, resultBeforeTick, this.getItem(INPUT_SLOT), this.getItem(RESULT_SLOT))) {
            return;
        }

        ItemStack jarStack = this.getItem(JAR_SLOT);
        if (jarStack.isEmpty() || !jarStack.is(ModItems.CLAY_JAR.get())) {
            return;
        }

        Item fumeItem = getFumeForInput(inputBeforeTick);
        if (fumeItem == null) {
            return;
        }

        float chance = Math.min(1.0F, BASE_FUME_CHANCE + getFumeFunnelBonus(level, this.worldPosition, this.getBlockState()));
        if (level.random.nextFloat() >= chance) {
            return;
        }

        jarStack.shrink(1);

        ItemStack extraStack = this.getItem(EXTRA_SLOT);
        ItemStack fumeStack = new ItemStack(fumeItem);
        if (extraStack.isEmpty()) {
            this.setItem(EXTRA_SLOT, fumeStack);
        } else if (ItemStack.isSameItemSameComponents(extraStack, fumeStack) && extraStack.getCount() < extraStack.getMaxStackSize()) {
            extraStack.grow(1);
            this.setChanged();
        }
    }

    private static boolean didSmeltComplete(ItemStack inputBeforeTick, ItemStack resultBeforeTick, ItemStack inputAfterTick, ItemStack resultAfterTick) {
        if (inputBeforeTick.isEmpty()) {
            return false;
        }

        int inputCountDelta = inputBeforeTick.getCount() - inputAfterTick.getCount();
        boolean inputConsumed = inputAfterTick.isEmpty() ? inputBeforeTick.getCount() == 1 : inputCountDelta == 1;
        if (!inputConsumed) {
            return false;
        }

        if (resultBeforeTick.isEmpty()) {
            return !resultAfterTick.isEmpty();
        }

        if (!ItemStack.isSameItemSameComponents(resultBeforeTick, resultAfterTick)) {
            return false;
        }

        return resultAfterTick.getCount() > resultBeforeTick.getCount();
    }

    @Nullable
    private static Item getFumeForInput(ItemStack smeltedInput) {
        Supplier<Item> fumeSupplier = FUME_BY_INPUT.get(smeltedInput.getItem());
        return fumeSupplier != null ? fumeSupplier.get() : null;
    }

    private static float getFumeFunnelBonus(ServerLevel level, BlockPos pos, BlockState state) {
        return 0.0F;
    }
}
