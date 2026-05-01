package com.ponyo.thewitchslegacy.ritual.definitions.barrier;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.Waystone;
import com.ponyo.thewitchslegacy.network.BarrierSyncPayload;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingSize;
import com.ponyo.thewitchslegacy.ritual.SustainingRitualManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RiteOfProtection {
    private static final int ALTAR_POWER_COST = 1000;
    private static final int BARRIER_SPHERE_3_ALTAR_POWER_COST = 0;
    private static final String DISPLAY_NAME_KEY = "ritual.thewitchslegacy.rite_of_protection";
    private static final double BARRIER_PADDING = 0.75D;
    private static final double WATCH_PADDING = 2.0D;
    private static final int BOUNDARY_PARTICLE_INTERVAL_TICKS = 10;
    private static final int BOUNDARY_PARTICLES_PER_PULSE = 8;
    private static final int BARRIER_SPHERE_3_DURATION_TICKS = 30 * 20;
    private static final double SMALL_RADIUS = 3.0D;
    private static final double MEDIUM_RADIUS = 5.0D;
    private static final double LARGE_RADIUS = 7.0D;
    private static final double BOUNDARY_PARTICLE_MAX_FALL_DISTANCE = 1.0D;
    private static final double BARRIER_BLOCK_SHELL_THICKNESS = 0.75D;
    private static final int BARRIER_BLOCK_REPAIR_INTERVAL_TICKS = 10;
    private static final int BARRIER_BLOCK_REPAIR_COUNT = 24;
    private static final int BARRIER_CYLINDER_HEIGHT_BLOCKS = 7;
    private static final int BARRIER_FLOOR_Y_OFFSET = -1;
    private static final int BARRIER_ROOF_Y_OFFSET = 7;
    private static final List<ActiveProtectionSphere> ACTIVE_SPHERES = new ArrayList<>();
    private static final Map<BarrierBlockKey, ActiveProtectionSphere> BARRIER_BLOCKS = new HashMap<>();
    private static final List<ClientSyncedBarrier> CLIENT_SYNCED_BARRIERS = new ArrayList<>();

    private RiteOfProtection() {
    }

    public static List<RitualDefinition> createAll() {
        List<RitualDefinition> rituals = new ArrayList<>();
        rituals.addAll(createAllForKind(ProtectionKind.BARRIER_SPHERE_1));
        rituals.addAll(createAllForKind(ProtectionKind.BARRIER_SPHERE_2));
        rituals.add(createBarrierSphere3());
        return List.copyOf(rituals);
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_SPHERES.isEmpty()) {
            return;
        }

        Iterator<ActiveProtectionSphere> iterator = ACTIVE_SPHERES.iterator();
        while (iterator.hasNext()) {
            ActiveProtectionSphere sphere = iterator.next();
            ServerLevel level = event.getServer().getLevel(sphere.dimension());
            if (level == null) {
                iterator.remove();
                continue;
            }

            sphere.tickProjectiles(level);
        }
    }

    public static boolean shouldBarrierCollide(BlockGetter level, BlockPos pos, Entity entity) {
        if (!(level instanceof Level actualLevel)) {
            return true;
        }

        if (actualLevel.isClientSide()) {
            return shouldClientSyncedBarrierCollide(actualLevel, pos, entity);
        }

        ActiveProtectionSphere sphere = BARRIER_BLOCKS.get(new BarrierBlockKey(actualLevel.dimension(), pos.immutable()));
        if (sphere == null) {
            return false;
        }

        return sphere.blocksBarrierBlockFor(entity);
    }

    public static void replaceClientSyncedBarriers(List<SyncedBarrier> barriers) {
        CLIENT_SYNCED_BARRIERS.clear();
        for (SyncedBarrier barrier : barriers) {
            CLIENT_SYNCED_BARRIERS.add(new ClientSyncedBarrier(barrier));
        }
    }

    private static boolean shouldClientSyncedBarrierCollide(Level level, BlockPos pos, Entity entity) {
        Identifier dimensionId = level.dimension().identifier();
        for (ClientSyncedBarrier barrier : CLIENT_SYNCED_BARRIERS) {
            if (barrier.dimensionId().equals(dimensionId)
                    && barrier.isIntendedBarrierPos(pos)
                    && blocksMovementForRule(barrier.movementRuleId(), entity, barrier.casterId())) {
                return true;
            }
        }
        return false;
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            cleanupOrphanBarrierBlocksNear(level, event.getEntity().blockPosition(), 12);
            if (event.getEntity() instanceof ServerPlayer player) {
                syncBarriersToPlayer(player);
            }
        }
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (ACTIVE_SPHERES.isEmpty() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos brokenPos = event.getPos().immutable();
        for (ActiveProtectionSphere sphere : ACTIVE_SPHERES) {
            if (sphere.dimension().equals(level.dimension()) && sphere.isIntendedBarrierPos(brokenPos)) {
                sphere.markForRepair(brokenPos);
            }
        }
    }

    private static void cleanupOrphanBarrierBlocksNear(ServerLevel level, BlockPos centerPos, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(centerPos.offset(-radius, -radius, -radius), centerPos.offset(radius, radius, radius))) {
            if (!level.getBlockState(pos).is(ModBlocks.RITUAL_BARRIER.get())) {
                continue;
            }
            if (BARRIER_BLOCKS.containsKey(new BarrierBlockKey(level.dimension(), pos.immutable()))) {
                continue;
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (ACTIVE_SPHERES.isEmpty() || !(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }

        Entity victim = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        Entity direct = event.getSource().getDirectEntity();
        if (attacker == null && direct instanceof Projectile projectile) {
            attacker = projectile.getOwner();
        }
        if (attacker == null) {
            attacker = direct;
        }
        if (attacker == null || attacker == victim || attacker.level() != level) {
            return;
        }

        for (ActiveProtectionSphere sphere : ACTIVE_SPHERES) {
            if (sphere.blocksInteractionBetween(level, attacker.position(), victim.position())) {
                event.setCanceled(true);
                if (direct instanceof Projectile projectile) {
                    projectile.discard();
                }
                return;
            }
        }
    }

    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Projectile projectile = event.getProjectile();
        if (ACTIVE_SPHERES.isEmpty() || !(projectile.level() instanceof ServerLevel level)) {
            return;
        }

        Entity owner = projectile.getOwner();
        Vec3 ownerPosition = owner != null && owner.level() == level ? owner.position() : new Vec3(projectile.xOld, projectile.yOld, projectile.zOld);
        for (ActiveProtectionSphere sphere : ACTIVE_SPHERES) {
            if (sphere.blocksProjectileCrossing(level, projectile, ownerPosition)) {
                event.setCanceled(true);
                projectile.discard();
                return;
            }
        }
    }

    private static List<RitualDefinition> createAllForKind(ProtectionKind kind) {
        return List.of(
                create(kind, RitualRingSize.SMALL, 1, false),
                create(kind, RitualRingSize.MEDIUM, 2, false),
                create(kind, RitualRingSize.LARGE, 3, false),
                create(kind, RitualRingSize.SMALL, 1, true),
                create(kind, RitualRingSize.MEDIUM, 2, true),
                create(kind, RitualRingSize.LARGE, 3, true)
        );
    }

    private static RitualDefinition create(ProtectionKind kind, RitualRingSize ringSize, int powerItemCount, boolean usesWaystone) {
        List<RitualItemRequirement> itemRequirements = new ArrayList<>();
        itemRequirements.add(new RitualItemRequirement(Items.OBSIDIAN, 1, true));
        itemRequirements.add(new RitualItemRequirement(kind.powerItem(), powerItemCount, true));
        if (usesWaystone) {
            itemRequirements.add(boundOrBloodedWaystoneRequirement());
        }

        return new RitualDefinition(
                kind.id() + "_" + ringSize.name().toLowerCase() + (usesWaystone ? "_waystone" : ""),
                DISPLAY_NAME_KEY,
                List.of(ringRequirement(ringSize)),
                List.copyOf(itemRequirements),
                ALTAR_POWER_COST,
                (level, centerPos, player, consumedItems) -> {
                    BarrierTarget target = usesWaystone ? resolveBarrierTarget(level, consumedItems) : BarrierTarget.success(level, centerPos);
                    if (target.status() == BarrierTargetStatus.PLAYER_OFFLINE) {
                        return Component.translatable("message.thewitchslegacy.player_offline");
                    }
                    if (target.level() == null || target.centerPos() == null) {
                        return Component.translatable("message.thewitchslegacy.waystone_target_unavailable");
                    }

                    ActiveProtectionSphere sphere = new ActiveProtectionSphere(
                            kind,
                            target.level().dimension(),
                            target.centerPos().immutable(),
                            radiusFor(ringSize),
                            player.getUUID()
                    );
                    ACTIVE_SPHERES.add(sphere);
                    sphere.placeBarrierBlocks(target.level());
                    syncBarriersToAllPlayers();
                    SustainingRitualManager.start(
                            level,
                            centerPos,
                            kind.id(),
                            kind.durationTicks(),
                            kind.altarPowerPerSecond(powerItemCount),
                            (activeLevel, activeCenterPos, gameTime) -> sphere.tickMovement(activeLevel.getServer(), gameTime),
                            (activeLevel, activeCenterPos) -> stopSphere(activeLevel, sphere)
                    );
                    RitualEffects.playCompletionEffects(level, centerPos.above(1));
                    if (target.level() != level || !target.centerPos().equals(centerPos)) {
                        RitualEffects.playCompletionEffects(target.level(), target.centerPos().above(1));
                    }
                    return null;
                }
        );
    }

    private static RitualDefinition createBarrierSphere3() {
        List<RitualItemRequirement> itemRequirements = List.of(
                new RitualItemRequirement(Items.OBSIDIAN, 1, true),
                new RitualItemRequirement(ModItems.INFUSED_STONE_CHARGED.get(), 1, true)
        );

        ProtectionKind kind = ProtectionKind.BARRIER_SPHERE_3;
        return new RitualDefinition(
                kind.id(),
                DISPLAY_NAME_KEY,
                List.of(RitualRingRequirement.small(ModBlocks.WHITE_GLYPH.get())),
                itemRequirements,
                BARRIER_SPHERE_3_ALTAR_POWER_COST,
                (level, centerPos, player, consumedItems) -> {
                    ActiveProtectionSphere sphere = new ActiveProtectionSphere(
                            kind,
                            level.dimension(),
                            centerPos.immutable(),
                            SMALL_RADIUS,
                            player.getUUID()
                    );
                    ACTIVE_SPHERES.add(sphere);
                    sphere.placeBarrierBlocks(level);
                    syncBarriersToAllPlayers();
                    SustainingRitualManager.start(
                            level,
                            centerPos,
                            kind.id(),
                            kind.durationTicks(),
                            kind.altarPowerPerSecond(1),
                            (activeLevel, activeCenterPos, gameTime) -> sphere.tickMovement(activeLevel.getServer(), gameTime),
                            (activeLevel, activeCenterPos) -> stopSphere(activeLevel, sphere)
                    );
                    RitualEffects.playCompletionEffects(level, centerPos.above(1));
                    RitualEffects.spawnChargedStoneRemainderIfNeeded(level, centerPos.above(1), itemRequirements);
                    return null;
                }
        );
    }

    private static void stopSphere(ServerLevel activeLevel, ActiveProtectionSphere sphere) {
        ServerLevel level = activeLevel.getServer().getLevel(sphere.dimension());
        if (level != null) {
            sphere.removeBarrierBlocks(level);
        }
        ACTIVE_SPHERES.remove(sphere);
        syncBarriersToAllPlayers();
    }

    private static void syncBarriersToAllPlayers() {
        PacketDistributor.sendToAllPlayers(new BarrierSyncPayload(createSyncSnapshot()));
    }

    private static void syncBarriersToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new BarrierSyncPayload(createSyncSnapshot()));
    }

    private static List<SyncedBarrier> createSyncSnapshot() {
        List<SyncedBarrier> snapshot = new ArrayList<>();
        for (ActiveProtectionSphere sphere : ACTIVE_SPHERES) {
            snapshot.add(sphere.toSyncedBarrier());
        }
        return List.copyOf(snapshot);
    }

    private static RitualRingRequirement ringRequirement(RitualRingSize ringSize) {
        return switch (ringSize) {
            case SMALL -> RitualRingRequirement.small(ModBlocks.WHITE_GLYPH.get());
            case MEDIUM -> RitualRingRequirement.medium(ModBlocks.WHITE_GLYPH.get());
            case LARGE -> RitualRingRequirement.large(ModBlocks.WHITE_GLYPH.get());
        };
    }

    private static double radiusFor(RitualRingSize ringSize) {
        return switch (ringSize) {
            case SMALL -> SMALL_RADIUS;
            case MEDIUM -> MEDIUM_RADIUS;
            case LARGE -> LARGE_RADIUS;
        };
    }

    private static RitualItemRequirement boundOrBloodedWaystoneRequirement() {
        return new RitualItemRequirement(
                ModItems.BOUND_WAYSTONE.get(),
                1,
                true,
                stack -> stack.is(ModItems.BOUND_WAYSTONE.get()) || stack.is(ModItems.BLOODED_WAYSTONE.get())
        );
    }

    private static BarrierTarget resolveBarrierTarget(ServerLevel castingLevel, List<ItemStack> consumedItems) {
        ItemStack waystone = findWaystone(consumedItems);
        if (waystone.isEmpty()) {
            return BarrierTarget.unavailable();
        }

        if (waystone.is(ModItems.BOUND_WAYSTONE.get())) {
            Waystone.StoredLocation location = Waystone.getStoredLocation(waystone).orElse(null);
            if (location == null) {
                return BarrierTarget.unavailable();
            }

            Identifier dimensionId = parseDimensionIdentifier(location.dimensionId());
            if (dimensionId == null) {
                return BarrierTarget.unavailable();
            }

            ServerLevel targetLevel = castingLevel.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
            if (targetLevel == null) {
                return BarrierTarget.unavailable();
            }

            return BarrierTarget.success(targetLevel, location.blockPos());
        }

        Waystone.BloodTarget bloodTarget = Waystone.getBloodTarget(waystone).orElse(null);
        if (bloodTarget == null) {
            return BarrierTarget.unavailable();
        }

        ServerPlayer targetPlayer = castingLevel.getServer().getPlayerList().getPlayer(bloodTarget.entityUuid());
        if (targetPlayer == null) {
            return BarrierTarget.playerOffline();
        }

        return BarrierTarget.success((ServerLevel) targetPlayer.level(), targetPlayer.blockPosition());
    }

    private static ItemStack findWaystone(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (stack.is(ModItems.BOUND_WAYSTONE.get()) || stack.is(ModItems.BLOODED_WAYSTONE.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static Identifier parseDimensionIdentifier(String rawDimensionId) {
        try {
            return Identifier.parse(rawDimensionId);
        } catch (RuntimeException ignored) {
            int slashIndex = rawDimensionId.indexOf('/');
            int endIndex = rawDimensionId.lastIndexOf(']');
            if (slashIndex < 0 || endIndex <= slashIndex) {
                return null;
            }

            try {
                return Identifier.parse(rawDimensionId.substring(slashIndex + 1, endIndex).trim());
            } catch (RuntimeException ignoredAgain) {
                return null;
            }
        }
    }

    private enum ProtectionKind {
        BARRIER_SPHERE_1("barrier_sphere_1", Items.REDSTONE, 25, 0, MovementRule.HOSTILE_ONLY),
        BARRIER_SPHERE_2("barrier_sphere_2", Items.GLOWSTONE_DUST, 50, 0, MovementRule.ALL_EXCEPT_CASTER),
        BARRIER_SPHERE_3("barrier_sphere_3", Items.AIR, 0, BARRIER_SPHERE_3_DURATION_TICKS, MovementRule.ALL);

        private final String id;
        private final Item powerItem;
        private final int basePowerPerSecond;
        private final int durationTicks;
        private final MovementRule movementRule;

        ProtectionKind(String id, Item powerItem, int basePowerPerSecond, int durationTicks, MovementRule movementRule) {
            this.id = id;
            this.powerItem = powerItem;
            this.basePowerPerSecond = basePowerPerSecond;
            this.durationTicks = durationTicks;
            this.movementRule = movementRule;
        }

        String id() {
            return this.id;
        }

        Item powerItem() {
            return this.powerItem;
        }

        int altarPowerPerSecond(int powerItemCount) {
            return this.basePowerPerSecond + (powerItemCount - 1) * 5;
        }

        int durationTicks() {
            return this.durationTicks;
        }

        boolean blocksMovementFor(Entity entity, UUID casterId) {
            return blocksMovementForRule(this.movementRule.ordinal(), entity, casterId);
        }

        int movementRuleId() {
            return this.movementRule.ordinal();
        }
    }

    private static boolean blocksMovementForRule(int movementRuleId, Entity entity, UUID casterId) {
        MovementRule[] rules = MovementRule.values();
        if (movementRuleId < 0 || movementRuleId >= rules.length) {
            return false;
        }
        return switch (rules[movementRuleId]) {
            case HOSTILE_ONLY -> entity instanceof Mob && entity instanceof Enemy;
            case ALL_EXCEPT_CASTER -> entity instanceof LivingEntity && !entity.getUUID().equals(casterId);
            case ALL -> true;
        };
    }

    private enum MovementRule {
        HOSTILE_ONLY,
        ALL_EXCEPT_CASTER,
        ALL
    }

    private enum Side {
        INSIDE,
        OUTSIDE
    }

    private record BarrierTarget(BarrierTargetStatus status, ServerLevel level, BlockPos centerPos) {
        static BarrierTarget success(ServerLevel level, BlockPos centerPos) {
            return new BarrierTarget(BarrierTargetStatus.SUCCESS, level, centerPos);
        }

        static BarrierTarget unavailable() {
            return new BarrierTarget(BarrierTargetStatus.UNAVAILABLE, null, null);
        }

        static BarrierTarget playerOffline() {
            return new BarrierTarget(BarrierTargetStatus.PLAYER_OFFLINE, null, null);
        }
    }

    private enum BarrierTargetStatus {
        SUCCESS,
        UNAVAILABLE,
        PLAYER_OFFLINE
    }

    private record BarrierBlockKey(ResourceKey<Level> dimension, BlockPos pos) {
    }

    public record SyncedBarrier(Identifier dimensionId, BlockPos centerPos, double radius, int movementRuleId, UUID casterId) {
    }

    private record ClientSyncedBarrier(SyncedBarrier barrier, Set<BlockPos> intendedBarrierPositions) {
        private ClientSyncedBarrier(SyncedBarrier barrier) {
            this(barrier, ActiveProtectionSphere.createBarrierShellPositions(barrier.centerPos(), barrier.radius()));
        }

        private Identifier dimensionId() {
            return this.barrier.dimensionId();
        }

        private int movementRuleId() {
            return this.barrier.movementRuleId();
        }

        private UUID casterId() {
            return this.barrier.casterId();
        }

        private boolean isIntendedBarrierPos(BlockPos pos) {
            return this.intendedBarrierPositions.contains(pos);
        }
    }

    private static final class ActiveProtectionSphere {
        private final ProtectionKind kind;
        private final ResourceKey<Level> dimension;
        private final BlockPos centerPos;
        private final double radius;
        private final UUID casterId;
        private final Set<BlockPos> intendedBarrierPositions;
        private final Set<BlockPos> placedBarrierPositions = new HashSet<>();
        private final Set<BlockPos> pendingBarrierRepairs = new HashSet<>();
        private final Map<UUID, Side> entitySides = new HashMap<>();
        private final Map<UUID, Side> projectileSides = new HashMap<>();
        private int nextRepairIndex;

        private ActiveProtectionSphere(ProtectionKind kind, ResourceKey<Level> dimension, BlockPos centerPos,
                                       double radius, UUID casterId) {
            this.kind = kind;
            this.dimension = dimension;
            this.centerPos = centerPos;
            this.radius = radius;
            this.casterId = casterId;
            this.intendedBarrierPositions = createBarrierShellPositions(centerPos, radius);
        }

        private ResourceKey<Level> dimension() {
            return this.dimension;
        }

        private void tickMovement(net.minecraft.server.MinecraftServer server, long gameTime) {
            ServerLevel level = server.getLevel(this.dimension);
            if (level == null) {
                return;
            }

            Set<UUID> seen = new java.util.HashSet<>();
            for (Entity entity : level.getEntities((Entity) null, bounds(), this::isMovementCandidate)) {
                seen.add(entity.getUUID());
                Side currentSide = sideOf(entity.position());
                Side previousSide = this.entitySides.putIfAbsent(entity.getUUID(), currentSide);
                if (previousSide != null && previousSide != currentSide) {
                    moveToSide(entity, previousSide);
                }
            }
            this.entitySides.keySet().removeIf(uuid -> !seen.contains(uuid));

            if (gameTime % BOUNDARY_PARTICLE_INTERVAL_TICKS == 0L) {
                spawnBoundaryParticles(level);
            }
            if (gameTime % BARRIER_BLOCK_REPAIR_INTERVAL_TICKS == 0L) {
                repairBarrierBlocks(level);
            }
        }

        private void tickProjectiles(ServerLevel level) {
            Set<UUID> seen = new java.util.HashSet<>();
            for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, bounds())) {
                seen.add(projectile.getUUID());
                Side currentSide = sideOf(projectile.position());
                Side previousSide = this.projectileSides.get(projectile.getUUID());
                if (previousSide == null) {
                    previousSide = initialProjectileSide(projectile);
                    this.projectileSides.put(projectile.getUUID(), previousSide);
                }
                Vec3 previousPosition = new Vec3(projectile.xOld, projectile.yOld, projectile.zOld);
                if (previousSide != null && previousSide != currentSide) {
                    projectile.discard();
                    continue;
                }
                if (crossesBoundary(previousPosition, projectile.position())) {
                    projectile.discard();
                }
            }
            this.projectileSides.keySet().removeIf(uuid -> !seen.contains(uuid));
        }

        private boolean isMovementCandidate(Entity entity) {
            return !(entity instanceof Projectile) && this.kind.blocksMovementFor(entity, this.casterId) && !entity.isRemoved();
        }

        private boolean blocksBarrierBlockFor(Entity entity) {
            return this.kind.blocksMovementFor(entity, this.casterId);
        }

        private SyncedBarrier toSyncedBarrier() {
            return new SyncedBarrier(
                    this.dimension.identifier(),
                    this.centerPos,
                    this.radius,
                    this.kind.movementRuleId(),
                    this.casterId
            );
        }

        private boolean blocksInteractionBetween(ServerLevel level, Vec3 firstPosition, Vec3 secondPosition) {
            return level.dimension().equals(this.dimension)
                    && isInsideWatch(firstPosition)
                    && isInsideWatch(secondPosition)
                    && sideOf(firstPosition) != sideOf(secondPosition);
        }

        private boolean blocksProjectileCrossing(ServerLevel level, Projectile projectile, Vec3 fallbackPreviousPosition) {
            if (!level.dimension().equals(this.dimension)) {
                return false;
            }

            Entity owner = projectile.getOwner();
            Vec3 previousPosition = owner != null && owner.level().dimension().equals(this.dimension)
                    ? owner.position()
                    : fallbackPreviousPosition;
            return crossesBoundary(previousPosition, projectile.position());
        }

        private Side initialProjectileSide(Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner != null && owner.level().dimension().equals(this.dimension) && isInsideWatch(owner.position())) {
                return sideOf(owner.position());
            }
            Vec3 previousPosition = new Vec3(projectile.xOld, projectile.yOld, projectile.zOld);
            if (isInsideWatch(previousPosition)) {
                return sideOf(previousPosition);
            }
            return sideOf(projectile.position());
        }

        private AABB bounds() {
            Vec3 center = center();
            double watchRadius = this.radius + WATCH_PADDING;
            return new AABB(
                    center.x() - watchRadius,
                    this.centerPos.getY() + BARRIER_FLOOR_Y_OFFSET,
                    center.z() - watchRadius,
                    center.x() + watchRadius,
                    this.centerPos.getY() + BARRIER_ROOF_Y_OFFSET + 1.0D,
                    center.z() + watchRadius
            );
        }

        private boolean isInsideWatch(Vec3 position) {
            double centerX = this.centerPos.getX() + 0.5D;
            double centerZ = this.centerPos.getZ() + 0.5D;
            double dx = position.x() - centerX;
            double dz = position.z() - centerZ;
            return isInsideCylinderHeight(position)
                    && dx * dx + dz * dz <= (this.radius + WATCH_PADDING) * (this.radius + WATCH_PADDING);
        }

        private Side sideOf(Vec3 position) {
            double centerX = this.centerPos.getX() + 0.5D;
            double centerZ = this.centerPos.getZ() + 0.5D;
            double dx = position.x() - centerX;
            double dz = position.z() - centerZ;
            boolean insideHorizontalRadius = dx * dx + dz * dz <= this.radius * this.radius;
            return insideHorizontalRadius && isInsideCylinderHeight(position) ? Side.INSIDE : Side.OUTSIDE;
        }

        private boolean crossesBoundary(Vec3 previousPosition, Vec3 currentPosition) {
            return (isInsideWatch(previousPosition) || isInsideWatch(currentPosition))
                    && sideOf(previousPosition) != sideOf(currentPosition);
        }

        private boolean isInsideCylinderHeight(Vec3 position) {
            return position.y() >= this.centerPos.getY() + BARRIER_FLOOR_Y_OFFSET
                    && position.y() <= this.centerPos.getY() + BARRIER_ROOF_Y_OFFSET + 1.0D;
        }

        private void moveToSide(Entity entity, Side side) {
            Vec3 center = center();
            double dx = entity.getX() - center.x();
            double dz = entity.getZ() - center.z();
            double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
            if (horizontalDistance < 0.001D) {
                dx = 1.0D;
                dz = 0.0D;
                horizontalDistance = 1.0D;
            }

            double targetHorizontalDistance = side == Side.INSIDE
                    ? Math.max(0.0D, this.radius - BARRIER_PADDING)
                    : this.radius + BARRIER_PADDING;
            double targetX = center.x() + dx / horizontalDistance * targetHorizontalDistance;
            double targetZ = center.z() + dz / horizontalDistance * targetHorizontalDistance;
            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();
            }
            entity.setDeltaMovement(Vec3.ZERO);
            entity.teleportTo(targetX, entity.getY(), targetZ);
        }

        private void spawnBoundaryParticles(ServerLevel level) {
            Vec3 center = center();
            int particleCount = boundaryParticleCount();
            for (int i = 0; i < particleCount; i++) {
                double angle = level.random.nextDouble() * Math.PI * 2.0D;
                double x = Math.cos(angle);
                double z = Math.sin(angle);
                double particleX = center.x() + x * this.radius;
                double particleZ = center.z() + z * this.radius;
                double cylinderY = this.centerPos.getY() + level.random.nextInt(BARRIER_CYLINDER_HEIGHT_BLOCKS);
                double blockTopY = cylinderY + 1.02D;
                double particleY = Math.min(blockTopY, cylinderY + BOUNDARY_PARTICLE_MAX_FALL_DISTANCE);
                level.sendParticles(
                        ParticleTypes.PORTAL,
                        particleX,
                        particleY,
                        particleZ,
                        1,
                        0.004D,
                        0.004D,
                        0.004D,
                        0.001D
                );
            }
        }

        private int boundaryParticleCount() {
            if (this.radius >= LARGE_RADIUS) {
                return (int) Math.round(BOUNDARY_PARTICLES_PER_PULSE * 2.5D);
            }
            if (this.radius >= MEDIUM_RADIUS) {
                return (int) Math.round(BOUNDARY_PARTICLES_PER_PULSE * 1.75D);
            }
            return BOUNDARY_PARTICLES_PER_PULSE;
        }

        private Vec3 center() {
            return Vec3.atCenterOf(this.centerPos);
        }

        private void placeBarrierBlocks(ServerLevel level) {
            for (BlockPos barrierPos : this.intendedBarrierPositions) {
                placeBarrierBlockIfAir(level, barrierPos);
            }
        }

        private void removeBarrierBlocks(ServerLevel level) {
            for (BlockPos barrierPos : List.copyOf(this.placedBarrierPositions)) {
                BarrierBlockKey key = new BarrierBlockKey(level.dimension(), barrierPos);
                if (BARRIER_BLOCKS.get(key) != this) {
                    continue;
                }
                BARRIER_BLOCKS.remove(key);
                if (level.getBlockState(barrierPos).is(ModBlocks.RITUAL_BARRIER.get())) {
                    level.setBlock(barrierPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            this.placedBarrierPositions.clear();
            this.pendingBarrierRepairs.clear();
        }

        private boolean isIntendedBarrierPos(BlockPos pos) {
            return this.intendedBarrierPositions.contains(pos);
        }

        private void markForRepair(BlockPos pos) {
            this.pendingBarrierRepairs.add(pos.immutable());
        }

        private void repairBarrierBlocks(ServerLevel level) {
            for (BlockPos pendingPos : List.copyOf(this.pendingBarrierRepairs)) {
                if (placeBarrierBlockIfAir(level, pendingPos)) {
                    this.pendingBarrierRepairs.remove(pendingPos);
                } else if (!level.getBlockState(pendingPos).isAir()) {
                    this.pendingBarrierRepairs.remove(pendingPos);
                }
            }

            if (this.intendedBarrierPositions.isEmpty()) {
                return;
            }
            List<BlockPos> positions = List.copyOf(this.intendedBarrierPositions);
            for (int i = 0; i < BARRIER_BLOCK_REPAIR_COUNT; i++) {
                BlockPos pos = positions.get(this.nextRepairIndex);
                this.nextRepairIndex = (this.nextRepairIndex + 1) % positions.size();
                placeBarrierBlockIfAir(level, pos);
            }
        }

        private boolean placeBarrierBlockIfAir(ServerLevel level, BlockPos pos) {
            if (!level.getBlockState(pos).isAir()) {
                return false;
            }
            level.setBlock(pos, ModBlocks.RITUAL_BARRIER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            BlockPos immutablePos = pos.immutable();
            this.placedBarrierPositions.add(immutablePos);
            BARRIER_BLOCKS.put(new BarrierBlockKey(level.dimension(), immutablePos), this);
            return true;
        }

        private static Set<BlockPos> createBarrierShellPositions(BlockPos centerPos, double radius) {
            Set<BlockPos> positions = new HashSet<>();
            int range = (int) Math.ceil(radius + BARRIER_BLOCK_SHELL_THICKNESS);
            Vec3 center = Vec3.atCenterOf(centerPos);
            for (BlockPos pos : BlockPos.betweenClosed(centerPos.offset(-range, BARRIER_FLOOR_Y_OFFSET, -range), centerPos.offset(range, BARRIER_ROOF_Y_OFFSET, range))) {
                Vec3 blockCenter = Vec3.atCenterOf(pos);
                double dx = blockCenter.x() - center.x();
                double dz = blockCenter.z() - center.z();
                double distance = Math.sqrt(dx * dx + dz * dz);
                boolean wall = pos.getY() >= centerPos.getY()
                        && pos.getY() < centerPos.getY() + BARRIER_CYLINDER_HEIGHT_BLOCKS
                        && Math.abs(distance - radius) <= BARRIER_BLOCK_SHELL_THICKNESS;
                boolean cap = (pos.getY() == centerPos.getY() + BARRIER_FLOOR_Y_OFFSET || pos.getY() == centerPos.getY() + BARRIER_ROOF_Y_OFFSET)
                        && distance <= radius + BARRIER_BLOCK_SHELL_THICKNESS;
                if (wall || cap) {
                    positions.add(pos.immutable());
                }
            }
            return Set.copyOf(positions);
        }
    }
}
