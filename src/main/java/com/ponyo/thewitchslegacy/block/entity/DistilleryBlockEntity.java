package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.block.entity.distillery.DistilleryRecipe;
import com.ponyo.thewitchslegacy.block.entity.distillery.DistilleryRecipeOutput;
import com.ponyo.thewitchslegacy.block.entity.distillery.DistilleryRecipes;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.menu.DistilleryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class DistilleryBlockEntity extends BaseContainerBlockEntity {
    public static final int INPUT_SLOT_A = 0;
    public static final int INPUT_SLOT_B = 1;
    public static final int JAR_SLOT = 2;
    public static final int OUTPUT_SLOT_START = 3;
    public static final int OUTPUT_SLOT_END = 6;
    public static final int SLOT_COUNT = 7;
    private static final int DATA_COUNT = 2;
    private static final int DRIP_SOUND_INTERVAL = 50;
    private static final float DRIP_SOUND_VOLUME = 0.35F;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int brewingTicksRemaining;
    private int totalBrewingTicks;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> DistilleryBlockEntity.this.brewingTicksRemaining;
                case 1 -> DistilleryBlockEntity.this.totalBrewingTicks;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> DistilleryBlockEntity.this.brewingTicksRemaining = value;
                case 1 -> DistilleryBlockEntity.this.totalBrewingTicks = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public DistilleryBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DISTILLERY.get(), pos, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thewitchslegacy.distillery");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new DistilleryMenu(containerId, inventory, this, this.dataAccess);
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
        if (slot == JAR_SLOT) {
            return stack.is(ModItems.CLAY_JAR.get());
        }

        return slot == INPUT_SLOT_A || slot == INPUT_SLOT_B;
    }

    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, DistilleryBlockEntity blockEntity) {
        Optional<DistilleryRecipe> recipe = DistilleryRecipes.findMatch(blockEntity.getIngredientCounts());
        if (recipe.isEmpty() || !blockEntity.canApplyRecipe(recipe.get())) {
            blockEntity.resetProgressIfNeeded();
            return;
        }

        DistilleryRecipe currentRecipe = recipe.get();
        if (blockEntity.totalBrewingTicks != currentRecipe.distillTime()) {
            blockEntity.totalBrewingTicks = currentRecipe.distillTime();
            blockEntity.brewingTicksRemaining = currentRecipe.distillTime();
            blockEntity.setChanged();
        }

        if (blockEntity.brewingTicksRemaining > 0) {
            blockEntity.brewingTicksRemaining--;
            blockEntity.playInProgressDripSound(level, pos);
            blockEntity.setChanged();
        }

        if (blockEntity.brewingTicksRemaining > 0) {
            return;
        }

        blockEntity.applyRecipe(currentRecipe);
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
        blockEntity.totalBrewingTicks = 0;
        blockEntity.brewingTicksRemaining = 0;
        blockEntity.setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("brewing_ticks_remaining", this.brewingTicksRemaining);
        output.putInt("total_brewing_ticks", this.totalBrewingTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.brewingTicksRemaining = input.getIntOr("brewing_ticks_remaining", 0);
        this.totalBrewingTicks = input.getIntOr("total_brewing_ticks", 0);
    }

    private Map<Identifier, Integer> getIngredientCounts() {
        Map<Identifier, Integer> ingredientCounts = new TreeMap<>(Identifier::compareNamespaced);
        accumulateIngredient(ingredientCounts, this.items.get(INPUT_SLOT_A));
        accumulateIngredient(ingredientCounts, this.items.get(INPUT_SLOT_B));
        return ingredientCounts;
    }

    private boolean canApplyRecipe(DistilleryRecipe recipe) {
        ItemStack jarStack = this.items.get(JAR_SLOT);
        if (!jarStack.is(ModItems.CLAY_JAR.get()) || jarStack.getCount() < recipe.jarCost()) {
            return false;
        }

        return simulateDistributedOutputs(recipe).isPresent();
    }

    private void applyRecipe(DistilleryRecipe recipe) {
        consumeIngredients(recipe);
        this.items.get(JAR_SLOT).shrink(recipe.jarCost());
        NonNullList<ItemStack> newOutputs = simulateDistributedOutputs(recipe)
                .orElseThrow(() -> new IllegalStateException("Distillery recipe no longer fits outputs after validation"));

        for (int slot = OUTPUT_SLOT_START; slot <= OUTPUT_SLOT_END; slot++) {
            this.items.set(slot, newOutputs.get(slot - OUTPUT_SLOT_START));
        }
    }

    private void consumeIngredients(DistilleryRecipe recipe) {
        Map<Identifier, Integer> remainingCounts = new TreeMap<>(recipe.ingredients());
        consumeIngredientStack(INPUT_SLOT_A, remainingCounts);
        consumeIngredientStack(INPUT_SLOT_B, remainingCounts);
    }

    private void consumeIngredientStack(int slot, Map<Identifier, Integer> remainingCounts) {
        ItemStack stack = this.items.get(slot);
        if (stack.isEmpty()) {
            return;
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        int remaining = remainingCounts.getOrDefault(itemId, 0);
        if (remaining <= 0) {
            return;
        }

        int amountToShrink = Math.min(remaining, stack.getCount());
        stack.shrink(amountToShrink);
        remaining -= amountToShrink;
        if (remaining > 0) {
            remainingCounts.put(itemId, remaining);
        } else {
            remainingCounts.remove(itemId);
        }
    }

    private Optional<NonNullList<ItemStack>> simulateDistributedOutputs(DistilleryRecipe recipe) {
        NonNullList<ItemStack> simulatedOutputs = NonNullList.withSize(getOutputSlotCount(), ItemStack.EMPTY);
        for (int index = 0; index < simulatedOutputs.size(); index++) {
            simulatedOutputs.set(index, this.items.get(OUTPUT_SLOT_START + index).copy());
        }

        for (int outputIndex = 0; outputIndex < recipe.outputs().size(); outputIndex++) {
            DistilleryRecipeOutput output = recipe.outputs().get(outputIndex);
            if (!placeOutput(output, recipe.outputs().size() - outputIndex - 1, simulatedOutputs)) {
                return Optional.empty();
            }
        }

        return Optional.of(simulatedOutputs);
    }

    private static boolean placeOutput(DistilleryRecipeOutput output, int remainingOutputTypes, NonNullList<ItemStack> simulatedOutputs) {
        if (output.count() <= 0 || output.item() == Items.AIR) {
            return true;
        }

        List<Integer> compatibleSlots = new ArrayList<>();
        List<Integer> emptySlots = new ArrayList<>();
        for (int slot = 0; slot < simulatedOutputs.size(); slot++) {
            ItemStack current = simulatedOutputs.get(slot);
            if (current.isEmpty()) {
                emptySlots.add(slot);
            } else if (current.is(output.item()) && current.getCount() < current.getMaxStackSize()) {
                compatibleSlots.add(slot);
            }
        }

        int usableEmptySlots = Math.max(0, emptySlots.size() - remainingOutputTypes);
        List<Integer> targetSlots = new ArrayList<>(compatibleSlots);
        for (int i = 0; i < usableEmptySlots && i < output.count(); i++) {
            targetSlots.add(emptySlots.get(i));
        }

        if (targetSlots.isEmpty()) {
            return false;
        }

        int remainingToPlace = output.count();
        int slotPointer = 0;
        while (remainingToPlace > 0) {
            boolean placedAny = false;
            for (int attempt = 0; attempt < targetSlots.size() && remainingToPlace > 0; attempt++) {
                int targetSlot = targetSlots.get(slotPointer);
                ItemStack targetStack = simulatedOutputs.get(targetSlot);
                if (targetStack.isEmpty()) {
                    simulatedOutputs.set(targetSlot, new ItemStack(output.item(), 1));
                    remainingToPlace--;
                    placedAny = true;
                } else if (targetStack.is(output.item()) && targetStack.getCount() < targetStack.getMaxStackSize()) {
                    targetStack.grow(1);
                    remainingToPlace--;
                    placedAny = true;
                }

                slotPointer = (slotPointer + 1) % targetSlots.size();
            }

            if (!placedAny) {
                return false;
            }
        }

        return true;
    }

    private void resetProgressIfNeeded() {
        if (this.brewingTicksRemaining == 0 && this.totalBrewingTicks == 0) {
            return;
        }

        this.brewingTicksRemaining = 0;
        this.totalBrewingTicks = 0;
        this.setChanged();
    }

    private static void accumulateIngredient(Map<Identifier, Integer> ingredientCounts, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ingredientCounts.merge(itemId, stack.getCount(), Integer::sum);
    }

    private static int getOutputSlotCount() {
        return OUTPUT_SLOT_END - OUTPUT_SLOT_START + 1;
    }

    private void playInProgressDripSound(Level level, BlockPos pos) {
        if (this.brewingTicksRemaining <= 0 || this.brewingTicksRemaining % DRIP_SOUND_INTERVAL != 0) {
            return;
        }

        float pitch = level.random.nextFloat() * 0.1F + 0.9F;
        level.playSound(null, pos, SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON, SoundSource.BLOCKS, DRIP_SOUND_VOLUME, pitch);
    }
}
