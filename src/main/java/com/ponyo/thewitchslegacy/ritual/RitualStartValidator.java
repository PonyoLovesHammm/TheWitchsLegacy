package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface RitualStartValidator {
    RitualStartValidator ALWAYS_ALLOW = (level, centerPos, player) -> null;

    Component validate(ServerLevel level, BlockPos centerPos, ServerPlayer player);
}
