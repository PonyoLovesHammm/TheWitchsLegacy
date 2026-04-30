package com.ponyo.thewitchslegacy.ritual.definitions.transposition;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import com.ponyo.thewitchslegacy.ritual.SustainingRitualManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Supplier;

public final class RiteOfBeastialCall {
    private static final int ALTAR_POWER_COST = 4000;
    private static final int SUMMON_COUNT = 6;
    private static final double ATTRACT_RADIUS = 128.0D;
    private static final double ARRIVED_RADIUS = 6.0D;
    private static final int SUMMON_DISTANCE = 32;
    private static final int ACTIVE_CALL_TICKS = 30 * 20;
    private static final int ALTAR_POWER_PER_SECOND = 0;
    private static final int ATTRACTION_INTERVAL_TICKS = 10;

    private static final List<WeightedAnimal> ANIMALS = List.of(
            animal(EntityType.COW, 39),
            animal(EntityType.SHEEP, 39),
            animal(EntityType.PIG, 37),
            animal(EntityType.CHICKEN, 37),
            animal(EntityType.RABBIT, 5),
            animal(EntityType.HORSE, 3),
            animal(EntityType.DONKEY, 3),
            animal(EntityType.GOAT, 3),
            animal(EntityType.WOLF, 1),
            animal(EntityType.CAT, 1),
            animal(EntityType.FOX, 1),
            animal(EntityType.BEE, 3),
            animal(EntityType.LLAMA, 2),
            animal(EntityType.CAMEL, 1),
            animal(EntityType.MULE, 1),
            animal(EntityType.OCELOT, 1),
            animal(EntityType.PARROT, 2),
            animal(EntityType.TURTLE, 1),
            animal(EntityType.FROG, 1),
            animal(EntityType.ARMADILLO, 1),
            animal(EntityType.MOOSHROOM, 1),
            animal(EntityType.PANDA, 1),
            animal(EntityType.POLAR_BEAR, 1),
            animal(EntityType.AXOLOTL, 1)
            /*
            animal(EntityType.DOLPHIN, 1),
            animal(EntityType.SQUID, 1),
            animal(EntityType.GLOW_SQUID, 1),
            animal(EntityType.COD, 1),
            animal(EntityType.SALMON, 1),
            animal(EntityType.TROPICAL_FISH, 1),
            animal(EntityType.PUFFERFISH, 1)
             */
    );

    private RiteOfBeastialCall() {
    }

    public static RitualDefinition create() {
        List<RitualItemRequirement> itemRequirements = List.of(
                new RitualItemRequirement(Items.MILK_BUCKET, 1, true),
                new RitualItemRequirement(Items.HAY_BLOCK, 1, true),
                new RitualItemRequirement(Items.APPLE, 1, true),
                new RitualItemRequirement(Items.BEEF, 1, true),
                rawFishRequirement(),
                mushroomRequirement(),
                new RitualItemRequirement(Items.CARROT, 1, true),
                seedsRequirement()
        );

        return new RitualDefinition(
                "rite_of_beastial_call",
                "ritual.thewitchslegacy.rite_of_beastial_call",
                List.of(RitualRingRequirement.large(ModBlocks.OTHERWHERE_GLYPH.get())),
                itemRequirements,
                ALTAR_POWER_COST,
                (level, centerPos, player, consumedItems) -> {
                    summonAnimals(level, centerPos);
                    SustainingRitualManager.start(
                            level,
                            centerPos,
                            "rite_of_beastial_call",
                            ACTIVE_CALL_TICKS,
                            ALTAR_POWER_PER_SECOND,
                            RiteOfBeastialCall::tickActiveCall
                    );
                    attractNearbyAnimals(level, centerPos);
                    RitualEffects.playCompletionEffects(level, centerPos.above(1));
                    return null;
                }
        );
    }

    private static RitualItemRequirement rawFishRequirement() {
        return new RitualItemRequirement(Items.COD, 1, true, stack -> stack.is(ItemTags.FISHES));
    }

    private static RitualItemRequirement mushroomRequirement() {
        return new RitualItemRequirement(
                Items.BROWN_MUSHROOM,
                1,
                true,
                stack -> stack.is(Items.BROWN_MUSHROOM) || stack.is(Items.RED_MUSHROOM)
        );
    }

    private static RitualItemRequirement seedsRequirement() {
        return new RitualItemRequirement(
                Items.WHEAT_SEEDS,
                1,
                true,
                stack -> stack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)
        );
    }

    private static void summonAnimals(ServerLevel level, BlockPos centerPos) {
        double startingAngle = level.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < SUMMON_COUNT; i++) {
            WeightedAnimal weightedAnimal = chooseAnimal(level.random);
            if (weightedAnimal == null) {
                return;
            }

            Mob mob = weightedAnimal.type().get().create(level, EntitySpawnReason.TRIGGERED);
            if (mob == null) {
                continue;
            }

            double angle = startingAngle + (Math.PI * 2.0D * i / SUMMON_COUNT);
            BlockPos spawnPos = findSpawnPosAtDistance(level, centerPos, angle);
            mob.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
            level.addFreshEntity(mob);
        }
    }

    private static WeightedAnimal chooseAnimal(RandomSource random) {
        int totalWeight = 0;
        for (WeightedAnimal animal : ANIMALS) {
            totalWeight += animal.weight();
        }
        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        for (WeightedAnimal animal : ANIMALS) {
            roll -= animal.weight();
            if (roll < 0) {
                return animal;
            }
        }
        return ANIMALS.getLast();
    }

    private static BlockPos findSpawnPosAtDistance(ServerLevel level, BlockPos centerPos, double angle) {
        int baseX = centerPos.getX() + (int) Math.round(Math.cos(angle) * SUMMON_DISTANCE);
        int baseZ = centerPos.getZ() + (int) Math.round(Math.sin(angle) * SUMMON_DISTANCE);
        BlockPos fallbackPos = surfacePos(level, baseX, baseZ, centerPos.getY());
        for (int attempts = 0; attempts < 24; attempts++) {
            int x = baseX + level.random.nextInt(13) - 6;
            int z = baseZ + level.random.nextInt(13) - 6;
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, centerPos.getY(), z));
            if (level.getBlockState(surfacePos.below()).isFaceSturdy(level, surfacePos.below(), net.minecraft.core.Direction.UP)) {
                return surfacePos;
            }
        }
        return fallbackPos;
    }

    private static BlockPos surfacePos(ServerLevel level, int x, int z, int yHint) {
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, yHint, z));
    }

    private static void attractNearbyAnimals(ServerLevel level, BlockPos centerPos) {
        AABB bounds = new AABB(centerPos).inflate(ATTRACT_RADIUS);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, bounds, RiteOfBeastialCall::isCallableAnimal)) {
            if (mob.distanceToSqr(centerPos.getX() + 0.5D, centerPos.getY() + 0.5D, centerPos.getZ() + 0.5D) > ATTRACT_RADIUS * ATTRACT_RADIUS) {
                continue;
            }
            if (mob.distanceToSqr(centerPos.getX() + 0.5D, centerPos.getY() + 0.5D, centerPos.getZ() + 0.5D) <= ARRIVED_RADIUS * ARRIVED_RADIUS) {
                continue;
            }

            mob.getNavigation().moveTo(centerPos.getX() + 0.5D, centerPos.getY(), centerPos.getZ() + 0.5D, 1.15D);
        }
    }

    private static void tickActiveCall(ServerLevel level, BlockPos centerPos, long gameTime) {
        if (gameTime % ATTRACTION_INTERVAL_TICKS == 0L) {
            attractNearbyAnimals(level, centerPos);
        }
    }

    private static boolean isCallableAnimal(Mob mob) {
        for (WeightedAnimal animal : ANIMALS) {
            if (animal.type().get() == mob.getType()) {
                return true;
            }
        }
        return false;
    }

    private static WeightedAnimal animal(EntityType<? extends Mob> type, int weight) {
        return new WeightedAnimal(() -> type, weight);
    }

    private record WeightedAnimal(Supplier<EntityType<? extends Mob>> type, int weight) {
    }
}
