package com.ponyo.thewitchslegacy.block.entity;

import com.ponyo.thewitchslegacy.block.custom.WitchCauldron;
import com.ponyo.thewitchslegacy.block.entity.cauldron.WitchCauldronRecipe;
import com.ponyo.thewitchslegacy.block.entity.cauldron.WitchCauldronRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WitchCauldronBlockEntity extends BlockEntity {
    public static final int DEFAULT_LIQUID_COLOR = 0x3F76E4;

    private static final int HEAT_UP_TICKS = 100;
    private static final int BREW_FAIL_TICKS = 200;
    private static final int MIN_BREW_LEVEL = 1;
    private static final int FAILED_BREW_COLOR = 0x5A462F;
    private static final int SUCCESS_CELEBRATION_TICKS_TOTAL = 32;
    private static final int SUCCESS_PARTICLES_PER_TICK = 10;
    // Swap these values to quickly retheme the success celebration particle.
    private static final float SUCCESS_PARTICLE_RED = 0.66F;
    private static final float SUCCESS_PARTICLE_GREEN = 0.28F;
    private static final float SUCCESS_PARTICLE_BLUE = 0.94F;
    private static final AABB ITEM_INPUT_BOUNDS = new AABB(3.0 / 16.0, 4.0 / 16.0, 3.0 / 16.0, 13.0 / 16.0, 13.0 / 16.0, 13.0 / 16.0);
    private static final String HEATING_TICKS_TAG = "HeatingTicks";
    private static final String LIQUID_COLOR_TAG = "LiquidColor";
    private static final String BREW_FAILED_TAG = "BrewFailed";
    private static final String SUCCESS_TICKS_TAG = "SuccessCelebrationTicks";
    private static final String SUCCESS_ANGLE_TAG = "SuccessCelebrationAngle";
    private static final String PENDING_RESULT_ID_TAG = "PendingResultId";
    private static final String PENDING_RESULT_COUNT_TAG = "PendingResultCount";

    private int heatingTicks;
    private int liquidColor = DEFAULT_LIQUID_COLOR;
    private int ticksSinceLastIngredient = -1;
    private boolean brewFailed;
    private int successCelebrationTicks;
    private float successCelebrationAngle;
    private ItemStack pendingResult = ItemStack.EMPTY;
    private final Map<Identifier, Integer> ingredientCounts = new HashMap<>();

    public WitchCauldronBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WITCH_CAULDRON.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WitchCauldronBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        blockEntity.tickHeatState(level, pos, state);

        int fluidLevel = state.getValue(WitchCauldron.LEVEL);
        if (fluidLevel < MIN_BREW_LEVEL) {
            blockEntity.clearBrewingDataAndResetColor();
            return;
        }

        if (blockEntity.successCelebrationTicks > 0 && level instanceof ServerLevel serverLevel) {
            blockEntity.tickSuccessCelebration(serverLevel, pos, state);
            return;
        }

        if (!state.getValue(WitchCauldron.BUBBLING)) {
            return;
        }

        if (!blockEntity.brewFailed && blockEntity.tryConsumeDroppedIngredient(level, pos)) {
            Optional<WitchCauldronRecipe> recipeMatch = WitchCauldronRecipes.findExactMatch(blockEntity.ingredientCounts);
            if (recipeMatch.isPresent() && level instanceof ServerLevel serverLevel) {
                blockEntity.beginSuccessCelebration(serverLevel, pos, recipeMatch.get());
                return;
            }

            if (!WitchCauldronRecipes.hasViableContinuation(blockEntity.ingredientCounts) && level instanceof ServerLevel serverLevel) {
                blockEntity.failBrew(serverLevel, pos);
                return;
            }
        }

        if (!blockEntity.ingredientCounts.isEmpty()) {
            blockEntity.ticksSinceLastIngredient++;
            if (blockEntity.ticksSinceLastIngredient >= BREW_FAIL_TICKS && level instanceof ServerLevel serverLevel) {
                blockEntity.failBrew(serverLevel, pos);
            } else {
                blockEntity.setChanged();
            }
        }
    }

    private void tickHeatState(Level level, BlockPos pos, BlockState state) {
        boolean shouldHeat = !this.brewFailed && state.getValue(WitchCauldron.LEVEL) == 3 && isHeatSource(level.getBlockState(pos.below()));
        int nextHeatingTicks = shouldHeat ? Math.min(this.heatingTicks + 1, HEAT_UP_TICKS) : 0;
        boolean nextBubbling = shouldHeat && nextHeatingTicks >= HEAT_UP_TICKS;
        boolean bubbling = state.getValue(WitchCauldron.BUBBLING);

        if (nextHeatingTicks != this.heatingTicks) {
            this.heatingTicks = nextHeatingTicks;
            this.setChanged();
        }

        if (nextBubbling != bubbling) {
            BlockState newState = state.setValue(WitchCauldron.BUBBLING, nextBubbling);
            level.setBlock(pos, newState, 3);
            level.sendBlockUpdated(pos, state, newState, 3);
        }
    }

    private boolean tryConsumeDroppedIngredient(Level level, BlockPos pos) {
        AABB inputBounds = ITEM_INPUT_BOUNDS.move(pos);
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, inputBounds, entity -> entity.isAlive() && !entity.getItem().isEmpty())) {
            ItemStack stack = itemEntity.getItem();
            ItemStack consumedUnit = stack.copyWithCount(1);
            Identifier itemId = BuiltInRegistries.ITEM.getKey(consumedUnit.getItem());
            this.ingredientCounts.merge(itemId, 1, Integer::sum);
            this.ticksSinceLastIngredient = 0;

            stack.shrink(1);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }

            this.setLiquidColor(mixColors(this.liquidColor, colorForItem(itemId), 0.35F));

            if (level instanceof ServerLevel serverLevel) {
                double particleX = pos.getX() + 0.5D;
                double particleY = pos.getY() + 0.75D;
                double particleZ = pos.getZ() + 0.5D;
                serverLevel.sendParticles(ParticleTypes.SPLASH, particleX, particleY, particleZ, 9, 0.12D, 0.02D, 0.12D, 0.02D);
                serverLevel.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.35F, 1.1F);
            }

            this.setChanged();
            return true;
        }

        return false;
    }

    private void beginSuccessCelebration(ServerLevel level, BlockPos pos, WitchCauldronRecipe recipe) {
        this.pendingResult = recipe.createResultStack();
        this.setLiquidColor(recipe.resultColor());
        this.ingredientCounts.clear();
        this.ticksSinceLastIngredient = -1;
        this.brewFailed = false;
        this.successCelebrationTicks = SUCCESS_CELEBRATION_TICKS_TOTAL;
        this.successCelebrationAngle = 0.0F;
        this.syncForClient();
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.9F, 0.92F);
    }

    private void tickSuccessCelebration(ServerLevel level, BlockPos pos, BlockState state) {
        float progress = 1.0F - (float) this.successCelebrationTicks / (float) SUCCESS_CELEBRATION_TICKS_TOTAL;
        float easedProgress = progress * progress * (3.0F - 2.0F * progress);
        float pulse = 0.82F + 0.18F * (0.5F + 0.5F * Mth.sin(this.successCelebrationAngle * 0.5F));
        float spiralSpeed = 0.16F + easedProgress * 1.35F + Mth.sin(this.successCelebrationAngle * 0.22F) * 0.03F;
        float radius = Mth.lerp(easedProgress, 0.36F, 0.045F)
                + (Mth.sin(this.successCelebrationAngle * 0.27F) * 0.04F + Mth.sin(this.successCelebrationAngle * 0.13F + 1.2F) * 0.02F)
                * (1.0F - easedProgress * 0.65F);
        radius = Math.max(radius, 0.02F);
        float height = Mth.lerp(easedProgress, 0.60F, 1.15F);
        float angleStep = (float) ((Math.PI * 2.0D) / SUCCESS_PARTICLES_PER_TICK);
        ColorParticleOption successSpell = ColorParticleOption.create(
                ParticleTypes.ENTITY_EFFECT,
                SUCCESS_PARTICLE_RED * pulse,
                SUCCESS_PARTICLE_GREEN * pulse,
                SUCCESS_PARTICLE_BLUE
        );

        this.successCelebrationAngle += spiralSpeed;
        for (int i = 0; i < SUCCESS_PARTICLES_PER_TICK; i++) {
            float phase = i * angleStep;
            float armOffset = (i & 1) == 0 ? 0.0F : (float) Math.PI;
            float localRadius = radius * (0.82F + 0.18F * Mth.sin(this.successCelebrationAngle * 0.18F + phase * 2.0F));
            float angle = this.successCelebrationAngle + phase * 0.35F + armOffset;
            double x = pos.getX() + 0.5D + Math.cos(angle) * localRadius;
            double y = pos.getY() + height + Mth.cos(this.successCelebrationAngle * 0.31F + phase) * 0.03F + ((i & 1) == 0 ? 0.0D : 0.015D);
            double z = pos.getZ() + 0.5D + Math.sin(angle) * localRadius;
            level.sendParticles(successSpell, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        if ((this.successCelebrationTicks & 7) == 0) {
            level.sendParticles(successSpell, pos.getX() + 0.5D, pos.getY() + height + 0.05D, pos.getZ() + 0.5D, 6, 0.03D, 0.02D, 0.03D, 0.0D);
        }

        if ((this.successCelebrationTicks % 6) == 0) {
            float pitch = 0.8F + easedProgress * 0.55F + level.random.nextFloat() * 0.08F;
            level.playSound(null, pos, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.20F, pitch);
        }

        this.successCelebrationTicks--;
        this.setChanged();
        if (this.successCelebrationTicks <= 0) {
            this.completeSuccessfulBrew(level, pos, state);
        }
    }

    private void completeSuccessfulBrew(ServerLevel level, BlockPos pos, BlockState state) {
        if (!this.pendingResult.isEmpty()) {
            ItemEntity resultEntity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, this.pendingResult.copy());
            resultEntity.setDeltaMovement(0.0D, 0.22D, 0.0D);
            level.addFreshEntity(resultEntity);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.55F, 1.35F);
        }

        this.pendingResult = ItemStack.EMPTY;
        this.successCelebrationTicks = 0;
        this.successCelebrationAngle = 0.0F;
        this.heatingTicks = 0;
        this.liquidColor = DEFAULT_LIQUID_COLOR;
        this.brewFailed = false;
        this.setChanged();
        BlockState clearedState = state.setValue(WitchCauldron.LEVEL, 0).setValue(WitchCauldron.BUBBLING, false);
        level.setBlock(pos, clearedState, 3);
        level.sendBlockUpdated(pos, state, clearedState, 3);
    }

    private void failBrew(ServerLevel level, BlockPos pos) {
        this.ingredientCounts.clear();
        this.ticksSinceLastIngredient = -1;
        this.brewFailed = true;
        this.heatingTicks = 0;
        this.setLiquidColor(FAILED_BREW_COLOR);
        ColorParticleOption failSpell = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.36F, 0.25F, 0.16F);
        for (int i = 0; i < 12; i++) {
            double particleX = pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.45D;
            double particleY = pos.getY() + 0.82D + level.random.nextDouble() * 0.08D;
            double particleZ = pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.45D;
            level.sendParticles(failSpell, particleX, particleY, particleZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.9F);

        BlockState state = level.getBlockState(pos);
        if (state.getValue(WitchCauldron.BUBBLING)) {
            BlockState stillState = state.setValue(WitchCauldron.BUBBLING, false);
            level.setBlock(pos, stillState, 3);
            level.sendBlockUpdated(pos, state, stillState, 3);
        } else {
            this.syncForClient();
        }
    }

    private void clearBrewingDataAndResetColor() {
        if (!this.ingredientCounts.isEmpty() || this.ticksSinceLastIngredient != -1 || this.brewFailed || this.successCelebrationTicks > 0 || !this.pendingResult.isEmpty()) {
            this.ingredientCounts.clear();
            this.ticksSinceLastIngredient = -1;
            this.brewFailed = false;
            this.successCelebrationTicks = 0;
            this.successCelebrationAngle = 0.0F;
            this.pendingResult = ItemStack.EMPTY;
            this.setChanged();
        }

        if (this.liquidColor != DEFAULT_LIQUID_COLOR) {
            this.setLiquidColor(DEFAULT_LIQUID_COLOR);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(HEATING_TICKS_TAG, this.heatingTicks);
        output.putInt(LIQUID_COLOR_TAG, this.liquidColor);
        output.putBoolean(BREW_FAILED_TAG, this.brewFailed);
        output.putInt(SUCCESS_TICKS_TAG, this.successCelebrationTicks);
        output.putFloat(SUCCESS_ANGLE_TAG, this.successCelebrationAngle);
        if (!this.pendingResult.isEmpty()) {
            Identifier resultId = BuiltInRegistries.ITEM.getKey(this.pendingResult.getItem());
            output.store(PENDING_RESULT_ID_TAG, Identifier.CODEC, resultId);
            output.putInt(PENDING_RESULT_COUNT_TAG, this.pendingResult.getCount());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.heatingTicks = input.getIntOr(HEATING_TICKS_TAG, 0);
        this.liquidColor = input.getIntOr(LIQUID_COLOR_TAG, DEFAULT_LIQUID_COLOR);
        this.brewFailed = input.getBooleanOr(BREW_FAILED_TAG, false);
        this.successCelebrationTicks = input.getIntOr(SUCCESS_TICKS_TAG, 0);
        this.successCelebrationAngle = input.getFloatOr(SUCCESS_ANGLE_TAG, 0.0F);
        this.pendingResult = input.read(PENDING_RESULT_ID_TAG, Identifier.CODEC)
                .map(BuiltInRegistries.ITEM::getValue)
                .filter(item -> item != Items.AIR)
                .map(item -> new ItemStack(item, Math.max(1, input.getIntOr(PENDING_RESULT_COUNT_TAG, 1))))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public int getLiquidColor() {
        return this.liquidColor;
    }

    public void setLiquidColor(int liquidColor) {
        if (this.liquidColor != liquidColor) {
            this.liquidColor = liquidColor;
            this.syncForClient();
        }
    }

    private void syncForClient() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    private static int colorForItem(Identifier itemId) {
        float hue = (itemId.hashCode() & 0xFFFF) / 65535.0F;
        return Mth.hsvToRgb(hue, 0.62F, 0.86F);
    }

    private static int mixColors(int colorA, int colorB, float blendB) {
        float blendA = 1.0F - blendB;
        int red = (int) (((colorA >> 16) & 0xFF) * blendA + ((colorB >> 16) & 0xFF) * blendB);
        int green = (int) (((colorA >> 8) & 0xFF) * blendA + ((colorB >> 8) & 0xFF) * blendB);
        int blue = (int) ((colorA & 0xFF) * blendA + (colorB & 0xFF) * blendB);
        return (red << 16) | (green << 8) | blue;
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
