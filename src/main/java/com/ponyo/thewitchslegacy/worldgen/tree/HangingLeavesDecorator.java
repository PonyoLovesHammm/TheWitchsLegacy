package com.ponyo.thewitchslegacy.worldgen.tree;

import com.mojang.serialization.MapCodec;
import com.ponyo.thewitchslegacy.block.ModBlocks;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class HangingLeavesDecorator extends TreeDecorator {
    public static final HangingLeavesDecorator INSTANCE = new HangingLeavesDecorator();
    public static final MapCodec<HangingLeavesDecorator> CODEC = MapCodec.unit(INSTANCE);

    private static final record Column(int x, int z) {
    }

    private HangingLeavesDecorator() {
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeGrowers.WILLOW_HANGING_LEAVES.get();
    }

    @Override
    public void place(Context context) {
        Object2IntMap<Column> lowestLeafByColumn = new Object2IntOpenHashMap<>();
        lowestLeafByColumn.defaultReturnValue(Integer.MAX_VALUE);
        Set<Column> leafColumns = new HashSet<>();
        int lowestLeafY = Integer.MAX_VALUE;

        for (BlockPos pos : context.leaves()) {
            Column column = new Column(pos.getX(), pos.getZ());
            leafColumns.add(column);
            if (pos.getY() < lowestLeafByColumn.getInt(column)) {
                lowestLeafByColumn.put(column, pos.getY());
            }
            if (pos.getY() < lowestLeafY) {
                lowestLeafY = pos.getY();
            }
        }

        BlockState hangingLeaves = ModBlocks.WILLOW_LEAVES.get().defaultBlockState()
                .setValue(LeavesBlock.DISTANCE, 7)
                .setValue(LeavesBlock.PERSISTENT, false)
                .setValue(LeavesBlock.WATERLOGGED, false);

        lowestLeafByColumn.object2IntEntrySet().forEach(entry -> {
            Column column = entry.getKey();
            if (!isOuterColumn(column, leafColumns)) {
                return;
            }

            int remainingLeafDistance = maxRemainingLeafDistance(column, entry.getIntValue(), context);
            if (remainingLeafDistance <= 0) {
                return;
            }

            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(column.x(), entry.getIntValue() - 1, column.z());
            int placedLeaves = 0;

            while (context.isAir(pos) && placedLeaves < remainingLeafDistance) {
                context.setBlock(pos, hangingLeaves);
                pos.move(0, -1, 0);
                placedLeaves++;
            }
        });
    }

    private static boolean isOuterColumn(Column column, Set<Column> leafColumns) {
        return !leafColumns.contains(new Column(column.x(), column.z() - 1))
                || !leafColumns.contains(new Column(column.x(), column.z() + 1))
                || !leafColumns.contains(new Column(column.x() - 1, column.z()))
                || !leafColumns.contains(new Column(column.x() + 1, column.z()));
    }

    private static int maxRemainingLeafDistance(Column column, int leafY, Context context) {
        BlockPos leafPos = new BlockPos(column.x(), leafY, column.z());
        int nearestLogDistance = Integer.MAX_VALUE;

        for (BlockPos logPos : context.logs()) {
            int distance = leafPos.distManhattan(logPos);
            if (distance < nearestLogDistance) {
                nearestLogDistance = distance;
            }
        }

        return Math.max(0, 6 - nearestLogDistance);
    }
}
