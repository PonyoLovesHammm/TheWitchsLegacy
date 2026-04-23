package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

@FunctionalInterface
public interface RitualEffect {
    void execute(ServerLevel level, BlockPos centerPos);
}
