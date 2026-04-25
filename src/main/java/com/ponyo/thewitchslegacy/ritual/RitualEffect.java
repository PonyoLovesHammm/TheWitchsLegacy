package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@FunctionalInterface
public interface RitualEffect {
    Component execute(ServerLevel level, BlockPos centerPos, ServerPlayer player, List<ItemStack> consumedItems);
}
