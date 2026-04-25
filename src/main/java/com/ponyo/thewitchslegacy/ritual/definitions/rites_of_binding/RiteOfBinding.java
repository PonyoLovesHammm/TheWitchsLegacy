package com.ponyo.thewitchslegacy.ritual.definitions.rites_of_binding;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.CircleTalisman;
import com.ponyo.thewitchslegacy.item.custom.WaystoneItem;
import com.ponyo.thewitchslegacy.ritual.RitualDefinition;
import com.ponyo.thewitchslegacy.ritual.RitualEffects;
import com.ponyo.thewitchslegacy.ritual.RitualItemRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualPatterns;
import com.ponyo.thewitchslegacy.ritual.RitualRingMatcher;
import com.ponyo.thewitchslegacy.ritual.RitualRingRequirement;
import com.ponyo.thewitchslegacy.ritual.RitualRingSize;
import com.ponyo.thewitchslegacy.ritual.RitualStartValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RiteOfBinding {
    private static final double SMALL_RING_PLAYER_HORIZONTAL_RADIUS = 3.5D;
    private static final double SMALL_RING_PLAYER_VERTICAL_RADIUS = 2.5D;

    private RiteOfBinding() {
    }

    public static RitualDefinition createTalismanBinding1() {
        return createTalismanBinding("talisman_binding_1", 1000, List.of(
                new RitualItemRequirement(ModItems.CIRCLE_TALISMAN.get(), 1, true, CircleTalisman::isEmpty),
                new RitualItemRequirement(Items.REDSTONE, 1, true)
        ));
    }

    public static RitualDefinition createTalismanBinding2() {
        return createTalismanBinding("talisman_binding_2", 0, List.of(
                new RitualItemRequirement(ModItems.CIRCLE_TALISMAN.get(), 1, true, CircleTalisman::isEmpty),
                new RitualItemRequirement(Items.REDSTONE, 1, true),
                new RitualItemRequirement(ModItems.INFUSED_STONE_CHARGED.get(), 1, true)
        ));
    }

    public static RitualDefinition createWaystoneBinding1() {
        return createWaystoneBinding("waystone_binding_1", 500, List.of(
                RitualRingRequirement.small(ModBlocks.WHITE_GLYPH.get())
        ), List.of(
                new RitualItemRequirement(ModItems.WAYSTONE.get(), 1, true),
                new RitualItemRequirement(ModItems.ENDER_DEW.get(), 1, true),
                new RitualItemRequirement(Items.GLOWSTONE_DUST, 1, true)
        ));
    }

    public static RitualDefinition createWaystoneBinding2() {
        return createWaystoneBinding("waystone_binding_2", 0, List.of(
                RitualRingRequirement.small(ModBlocks.WHITE_GLYPH.get())
        ), List.of(
                new RitualItemRequirement(ModItems.WAYSTONE.get(), 1, true),
                new RitualItemRequirement(ModItems.ENDER_DEW.get(), 1, true),
                new RitualItemRequirement(Items.GLOWSTONE_DUST, 1, true),
                new RitualItemRequirement(ModItems.INFUSED_STONE_CHARGED.get(), 1, true)
        ));
    }

    public static RitualDefinition createBloodedWaystone1() {
        return createBloodedWaystoneBinding("blooded_waystone_1", 500, List.of(
                RitualRingRequirement.small(ModBlocks.WHITE_GLYPH.get())
        ), List.of(
                new RitualItemRequirement(ModItems.WAYSTONE.get(), 1, true),
                new RitualItemRequirement(ModItems.ENDER_DEW.get(), 1, true),
                new RitualItemRequirement(Items.SLIME_BALL, 1, true),
                new RitualItemRequirement(ModItems.ICY_NEEDLE.get(), 1, true)
        ));
    }

    public static RitualDefinition createBloodedWaystone2() {
        return createBloodedWaystoneBinding("blooded_waystone_2", 0, List.of(
                RitualRingRequirement.small(ModBlocks.WHITE_GLYPH.get())
        ), List.of(
                new RitualItemRequirement(ModItems.WAYSTONE.get(), 1, true),
                new RitualItemRequirement(ModItems.ENDER_DEW.get(), 1, true),
                new RitualItemRequirement(Items.SLIME_BALL, 1, true),
                new RitualItemRequirement(ModItems.ICY_NEEDLE.get(), 1, true),
                new RitualItemRequirement(ModItems.INFUSED_STONE_CHARGED.get(), 1, true)
        ));
    }

    private static RitualDefinition createTalismanBinding(String id, int altarPowerCost, List<RitualItemRequirement> itemRequirements) {
        return new RitualDefinition(
                id,
                "ritual.thewitchslegacy." + id,
                List.of(),
                itemRequirements,
                altarPowerCost,
                (level, centerPos, player) -> {
                    List<CircleTalisman.RingData> rings = detectBindableRings(level, centerPos);
                    if (rings.isEmpty()) {
                        return;
                    }

                    clearMatchedRings(level, centerPos, rings);

                    BlockPos outputPos = centerPos.above(1);
                    RitualEffects.playCompletionEffects(level, outputPos);
                    RitualEffects.spawnOutputItem(level, outputPos, CircleTalisman.createWithRings(ModItems.CIRCLE_TALISMAN.get(), rings));
                    RitualEffects.spawnChargedStoneRemainderIfNeeded(level, outputPos, itemRequirements);
                },
                (level, centerPos) -> !detectBindableRings(level, centerPos).isEmpty()
        );
    }

    private static RitualDefinition createWaystoneBinding(String id, int altarPowerCost, List<RitualRingRequirement> ringRequirements,
                                                          List<RitualItemRequirement> itemRequirements) {
        return new RitualDefinition(
                id,
                "ritual.thewitchslegacy." + id,
                ringRequirements,
                itemRequirements,
                altarPowerCost,
                (level, centerPos, player) -> {
                    BlockPos outputPos = centerPos.above(1);
                    RitualEffects.playCompletionEffects(level, outputPos);
                    RitualEffects.spawnOutputItem(level, outputPos,
                            WaystoneItem.createBoundWaystone(ModItems.BOUND_WAYSTONE.get(), level, centerPos));
                    RitualEffects.spawnChargedStoneRemainderIfNeeded(level, outputPos, itemRequirements);
                }
        );
    }

    private static RitualDefinition createBloodedWaystoneBinding(String id, int altarPowerCost, List<RitualRingRequirement> ringRequirements,
                                                                 List<RitualItemRequirement> itemRequirements) {
        return new RitualDefinition(
                id,
                "ritual.thewitchslegacy." + id,
                ringRequirements,
                itemRequirements,
                altarPowerCost,
                (level, centerPos, player) -> {
                    ServerPlayer bloodTarget = findClosestPlayerInSmallCircle(level, centerPos);
                    if (bloodTarget == null) {
                        return;
                    }

                    BlockPos outputPos = centerPos.above(1);
                    RitualEffects.playCompletionEffects(level, outputPos);
                    RitualEffects.spawnOutputItem(level, outputPos,
                            WaystoneItem.createBloodedWaystone(ModItems.BLOODED_WAYSTONE.get(), bloodTarget));
                    RitualEffects.spawnChargedStoneRemainderIfNeeded(level, outputPos, itemRequirements);
                },
                RitualRingMatcher.allRequired(ringRequirements),
                createBloodedWaystoneStartValidator()
        );
    }

    private static RitualStartValidator createBloodedWaystoneStartValidator() {
        return (level, centerPos, player) -> {
            if (findClosestPlayerInSmallCircle(level, centerPos) == null) {
                return Component.translatable("message.thewitchslegacy.no_player_for_blooded_waystone");
            }
            return null;
        };
    }

    private static ServerPlayer findClosestPlayerInSmallCircle(net.minecraft.server.level.ServerLevel level, BlockPos centerPos) {
        return level.getEntitiesOfClass(
                        ServerPlayer.class,
                        new AABB(centerPos).inflate(
                                SMALL_RING_PLAYER_HORIZONTAL_RADIUS,
                                SMALL_RING_PLAYER_VERTICAL_RADIUS,
                                SMALL_RING_PLAYER_HORIZONTAL_RADIUS
                        ),
                        candidate -> candidate.isAlive() && isWithinSmallCircle(candidate, centerPos)
                ).stream()
                .min(Comparator.comparingDouble(candidate ->
                        horizontalDistanceToCenterSqr(candidate, centerPos)))
                .orElse(null);
    }

    private static boolean isWithinSmallCircle(ServerPlayer candidate, BlockPos centerPos) {
        double verticalDistance = Math.abs(candidate.getY() - (centerPos.getY() + 0.5D));
        return verticalDistance <= SMALL_RING_PLAYER_VERTICAL_RADIUS
                && horizontalDistanceToCenterSqr(candidate, centerPos)
                <= SMALL_RING_PLAYER_HORIZONTAL_RADIUS * SMALL_RING_PLAYER_HORIZONTAL_RADIUS;
    }

    private static double horizontalDistanceToCenterSqr(ServerPlayer candidate, BlockPos centerPos) {
        double dx = candidate.getX() - (centerPos.getX() + 0.5D);
        double dz = candidate.getZ() - (centerPos.getZ() + 0.5D);
        return dx * dx + dz * dz;
    }

    private static List<CircleTalisman.RingData> detectBindableRings(net.minecraft.server.level.ServerLevel level, BlockPos centerPos) {
        List<CircleTalisman.RingData> rings = new ArrayList<>();

        for (RitualRingSize size : RitualRingSize.values()) {
            for (CircleTalisman.RingColor color : CircleTalisman.RingColor.values()) {
                if (matchesRing(level, centerPos, size, color.glyphBlock())) {
                    rings.add(new CircleTalisman.RingData(size, color));
                    break;
                }
            }
        }

        return rings;
    }

    private static boolean matchesRing(net.minecraft.server.level.ServerLevel level, BlockPos centerPos, RitualRingSize size, Block glyphBlock) {
        for (BlockPos offset : RitualPatterns.positionsFor(size)) {
            if (!level.getBlockState(centerPos.offset(offset)).is(glyphBlock)) {
                return false;
            }
        }
        return true;
    }

    private static void clearMatchedRings(net.minecraft.server.level.ServerLevel level, BlockPos centerPos, List<CircleTalisman.RingData> rings) {
        if (level.getBlockState(centerPos).is(ModBlocks.GOLDEN_GLYPH.get())) {
            level.removeBlock(centerPos, false);
        }

        for (CircleTalisman.RingData ring : rings) {
            for (BlockPos offset : RitualPatterns.positionsFor(ring.size())) {
                BlockPos glyphPos = centerPos.offset(offset);
                level.removeBlock(glyphPos, false);
            }
        }
    }
}
