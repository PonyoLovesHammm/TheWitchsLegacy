package com.ponyo.thewitchslegacy.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MandrakeEntity extends PathfinderMob {
    private static final int INITIAL_FRENZY_TICKS = 200;

    private int frenzyTicks;

    public MandrakeEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 1.15D, 1.45D));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 8.0F, 1.15D, 1.4D));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.frenzyTicks > 0) {
            this.frenzyTicks--;

            if (this.level().isClientSide()) {
                return;
            }

            if (this.tickCount % 20 == 0) {
                this.playMandrakeScream();
            }

            if (this.tickCount % 10 == 0) {
                Vec3 target = DefaultRandomPos.getPos(this, 10, 4);
                if (target != null) {
                    this.getNavigation().moveTo(target.x, target.y, target.z, 1.45D);
                }
            }
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnType,
            @Nullable SpawnGroupData spawnGroupData
    ) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        this.frenzyTicks = INITIAL_FRENZY_TICKS;
        this.playMandrakeScream();
        return data;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FOX_SCREECH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GOAT_SCREAMING_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GOAT_SCREAMING_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.GRASS_STEP, 0.15F, 1.3F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 100;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public float getVoicePitch() {
        return 1.5F + (this.random.nextFloat() - 0.5F) * 0.3F;
    }

    private void playMandrakeScream() {
        this.playSound(SoundEvents.FOX_SCREECH, 1.0F, this.getVoicePitch());
    }
}
