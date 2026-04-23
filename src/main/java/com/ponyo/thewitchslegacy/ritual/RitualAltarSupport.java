package com.ponyo.thewitchslegacy.ritual;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.custom.Altar;
import com.ponyo.thewitchslegacy.block.entity.AltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class RitualAltarSupport {
    private static final int MAX_ALTAR_RANGE = 48;
    private static final int MAX_ALTAR_RANGE_SQUARED = MAX_ALTAR_RANGE * MAX_ALTAR_RANGE;

    private RitualAltarSupport() {
    }

    static AltarBlockEntity findBestSupportingAltar(ServerLevel level, BlockPos centerPos, int requiredPower) {
        List<AltarBlockEntity> candidates = new ArrayList<>();

        for (BlockPos scanPos : BlockPos.betweenClosed(
                centerPos.offset(-MAX_ALTAR_RANGE, -1, -MAX_ALTAR_RANGE),
                centerPos.offset(MAX_ALTAR_RANGE, 1, MAX_ALTAR_RANGE))) {
            if (scanPos.distSqr(centerPos) > MAX_ALTAR_RANGE_SQUARED) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(scanPos);
            if (!(blockEntity instanceof AltarBlockEntity altar)) {
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

            if (altar.getCurrentPower() >= requiredPower) {
                candidates.add(altar);
            }
        }

        return candidates.stream()
                .max(Comparator.comparingInt(AltarBlockEntity::getCurrentPower))
                .orElse(null);
    }
}
