package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.custom.Altar;
import com.ponyo.thewitchslegacy.block.entity.spinningwheel.SpinningWheelRecipe;
import com.ponyo.thewitchslegacy.block.entity.spinningwheel.SpinningWheelRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SpinningWheelBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity {
    private static final String ACTIVE_RECIPE_TAG = "active_recipe";
    private static final String CRAFTING_TICKS_TAG = "crafting_ticks";
    private static final String TOTAL_CRAFTING_TICKS_TAG = "total_crafting_ticks";
    private static final int POWER_CONSUMPTION_INTERVAL = 20;
    private static final int MAX_ALTAR_RANGE = 48;
    private static final int MAX_ALTAR_RANGE_SQUARED = MAX_ALTAR_RANGE * MAX_ALTAR_RANGE;
    private static final float IDLE_SPIN_SPEED = ((float) Math.PI * 2.0F) / 120.0F;
    private static final float ACTIVE_SPIN_SPEED = ((float) Math.PI * 2.0F) / 40.0F;

    @Nullable
    private Identifier activeRecipeId;
    private int craftingTicks;
    private int totalCraftingTicks;

    public SpinningWheelBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SPINNING_WHEEL.get(), pos, blockState);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, SpinningWheelBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        SpinningWheelRecipe recipe = blockEntity.getActiveRecipe();
        if (recipe == null) {
            if (blockEntity.craftingTicks != 0 || blockEntity.totalCraftingTicks != 0) {
                blockEntity.craftingTicks = 0;
                blockEntity.totalCraftingTicks = 0;
                blockEntity.setChanged();
            }
            return;
        }

        if (blockEntity.totalCraftingTicks != recipe.craftTimeTicks()) {
            blockEntity.totalCraftingTicks = recipe.craftTimeTicks();
            blockEntity.craftingTicks = Math.min(blockEntity.craftingTicks, recipe.craftTimeTicks());
            blockEntity.setChanged();
        }

        if (!blockEntity.tryConsumeAltarPower(serverLevel, pos, recipe)) {
            return;
        }

        if (blockEntity.craftingTicks < recipe.craftTimeTicks()) {
            blockEntity.craftingTicks++;
            blockEntity.setChanged();
        }

        if (blockEntity.craftingTicks >= recipe.craftTimeTicks()) {
            blockEntity.finishRecipe();
        }
    }

    public void startRecipe(Identifier recipeId) {
        SpinningWheelRecipe recipe = SpinningWheelRecipes.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown spinning wheel recipe: " + recipeId));
        this.activeRecipeId = recipe.id();
        this.craftingTicks = 0;
        this.totalCraftingTicks = recipe.craftTimeTicks();
        this.setChanged();
    }

    public void cancelRecipe() {
        if (this.activeRecipeId == null && this.craftingTicks == 0 && this.totalCraftingTicks == 0) {
            return;
        }

        this.activeRecipeId = null;
        this.craftingTicks = 0;
        this.totalCraftingTicks = 0;
        this.setChanged();
    }

    public boolean isCrafting() {
        return this.activeRecipeId != null;
    }

    public float getVisualSpinSpeed() {
        return this.isCrafting() ? ACTIVE_SPIN_SPEED : IDLE_SPIN_SPEED;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.activeRecipeId != null) {
            output.store(ACTIVE_RECIPE_TAG, Identifier.CODEC, this.activeRecipeId);
        }
        output.putInt(CRAFTING_TICKS_TAG, this.craftingTicks);
        output.putInt(TOTAL_CRAFTING_TICKS_TAG, this.totalCraftingTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.activeRecipeId = input.read(ACTIVE_RECIPE_TAG, Identifier.CODEC).orElse(null);
        this.craftingTicks = input.getIntOr(CRAFTING_TICKS_TAG, 0);
        this.totalCraftingTicks = input.getIntOr(TOTAL_CRAFTING_TICKS_TAG, 0);
    }

    @Nullable
    private SpinningWheelRecipe getActiveRecipe() {
        return this.activeRecipeId == null ? null : SpinningWheelRecipes.findById(this.activeRecipeId).orElse(null);
    }

    private boolean tryConsumeAltarPower(ServerLevel level, BlockPos pos, SpinningWheelRecipe recipe) {
        int powerCost = recipe.altarPowerPerSecond();
        if (powerCost <= 0) {
            return true;
        }

        if (this.craftingTicks % POWER_CONSUMPTION_INTERVAL != 0) {
            return true;
        }

        AltarBlockEntity altar = findBestSupportingAltar(level, pos, powerCost);
        return altar != null && altar.consumePower(powerCost);
    }

    private void finishRecipe() {
        this.activeRecipeId = null;
        this.craftingTicks = 0;
        this.totalCraftingTicks = 0;
        this.setChanged();
    }

    @Nullable
    private static AltarBlockEntity findBestSupportingAltar(ServerLevel level, BlockPos centerPos, int requiredPower) {
        AltarBlockEntity bestAltar = null;

        for (BlockPos scanPos : BlockPos.betweenClosed(
                centerPos.offset(-MAX_ALTAR_RANGE, -1, -MAX_ALTAR_RANGE),
                centerPos.offset(MAX_ALTAR_RANGE, 1, MAX_ALTAR_RANGE))) {
            if (scanPos.distSqr(centerPos) > MAX_ALTAR_RANGE_SQUARED) {
                continue;
            }

            if (!(level.getBlockEntity(scanPos) instanceof AltarBlockEntity altar)) {
                continue;
            }
            if (!altar.isController()) {
                continue;
            }
            if (!level.getBlockState(scanPos).is(ModBlocks.ALTAR.get())) {
                continue;
            }
            if (!level.getBlockState(scanPos).getValue(Altar.ACTIVATED)) {
                continue;
            }

            int distributionRange = altar.getDistributionRange();
            if (scanPos.distSqr(centerPos) > (long) distributionRange * distributionRange) {
                continue;
            }
            if (altar.getCurrentPower() < requiredPower) {
                continue;
            }
            if (bestAltar == null || altar.getCurrentPower() > bestAltar.getCurrentPower()) {
                bestAltar = altar;
            }
        }

        return bestAltar;
    }
}
