package com.ponyo.thewitchslegacy.entity;

import com.ponyo.thewitchslegacy.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MandrakeEntity extends PathfinderMob {
    private static final int INITIAL_FRENZY_TICKS = 200;
    private static final int NAUSEA_DURATION_TICKS = 200;
    private static final int NAUSEA_AMPLIFIER = 1;
    private static final int NAUSEA_REAPPLY_INTERVAL_TICKS = 200;
    private static final double SCREAM_EFFECT_RADIUS = 12.0D;

    private int frenzyTicks;

    public MandrakeEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static void spawnFromHarvest(Level level, BlockPos pos, @Nullable Player triggeringPlayer) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        MandrakeEntity mandrake = ModEntities.MANDRAKE.get().create(serverLevel, EntitySpawnReason.TRIGGERED);
        if (mandrake == null) {
            return;
        }

        Vec3 spawnPos = Vec3.atBottomCenterOf(pos);
        mandrake.snapTo(spawnPos.x(), spawnPos.y(), spawnPos.z(), serverLevel.random.nextFloat() * 360.0F, 0.0F);
        mandrake.startHarvestFrenzy(triggeringPlayer);
        serverLevel.addFreshEntity(mandrake);
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

        if (!this.level().isClientSide() && this.tickCount % NAUSEA_REAPPLY_INTERVAL_TICKS == 0) {
            this.applyScreamNausea();
        }

        if (this.frenzyTicks > 0) {
            this.frenzyTicks--;

            if (this.level().isClientSide()) {
                return;
            }

            if (this.tickCount % 20 == 0) {
                this.playMandrakeScream(false);
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
        this.playMandrakeScream(false);
        return data;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return getRandomMandrakeScream();
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
    protected void actuallyHurt(ServerLevel level, DamageSource source, float amount) {
        super.actuallyHurt(level, source, amount);
        if (amount > 0.0F && this.isAlive()) {
            this.startHarvestFrenzy(source.getEntity() instanceof LivingEntity livingEntity ? livingEntity : null);
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.GRASS_STEP, 0.15F, 1.3F);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public float getVoicePitch() {
        return 1.5F + (this.random.nextFloat() - 0.5F) * 0.3F;
    }

    public void startHarvestFrenzy(@Nullable LivingEntity triggeringEntity) {
        this.frenzyTicks = INITIAL_FRENZY_TICKS;
        this.playMandrakeScream(true);
        this.applyScreamNausea();

        if (triggeringEntity != null) {
            this.setTarget(triggeringEntity);
        }
    }

    private void applyScreamNausea() {
        AABB effectBounds = this.getBoundingBox().inflate(SCREAM_EFFECT_RADIUS);
        for (Player player : this.level().getEntitiesOfClass(Player.class, effectBounds)) {
            if (player.distanceToSqr(this) <= SCREAM_EFFECT_RADIUS * SCREAM_EFFECT_RADIUS) {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, NAUSEA_DURATION_TICKS, NAUSEA_AMPLIFIER));
            }
        }
    }

    private void playMandrakeScream(boolean harvestScream) {
        SoundEvent screamSound = harvestScream ? ModSounds.MANDRAKE_SCREAM_ON_PLANT_BREAK.get() : getRandomMandrakeScream();
        this.level().playSound(null, this.blockPosition(), screamSound, SoundSource.HOSTILE, 1.5F, this.getVoicePitch());
    }

    private SoundEvent getRandomMandrakeScream() {
        return switch (this.random.nextInt(3)) {
            case 0 -> ModSounds.MANDRAKE_SCREAM_1.get();
            case 1 -> ModSounds.MANDRAKE_SCREAM_2.get();
            default -> ModSounds.MANDRAKE_SCREAM_3.get();
        };
    }
}
