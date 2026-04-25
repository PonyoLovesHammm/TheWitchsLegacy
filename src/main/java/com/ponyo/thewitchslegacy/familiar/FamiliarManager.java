package com.ponyo.thewitchslegacy.familiar;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.Optional;

public final class FamiliarManager {
    private FamiliarManager() {
    }

    public static boolean playerHasFamiliar(ServerLevel level, ServerPlayer player) {
        return FamiliarSavedData.get(level).hasFamiliar(level, player.getUUID());
    }

    public static Optional<FamiliarSavedData.FamiliarRecord> getFamiliar(ServerLevel level, ServerPlayer player) {
        return FamiliarSavedData.get(level).getFamiliar(level.getServer(), player.getUUID());
    }

    public static void bindCatFamiliar(ServerPlayer player, Cat cat) {
        FamiliarSavedData.get((ServerLevel) player.level()).bindFamiliar(player.getUUID(), cat);
    }

    public static boolean isFamiliar(ServerLevel level, Cat cat) {
        return FamiliarSavedData.get(level).isRegisteredFamiliar(cat.getUUID());
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel) || !(entity instanceof Cat cat)) {
            return;
        }

        FamiliarSavedData.get(serverLevel).clearFamiliarByEntity(cat.getUUID());
    }
}
