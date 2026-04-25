package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface RitualEffect {
    void execute(ServerLevel level, BlockPos centerPos, ServerPlayer player);
}
