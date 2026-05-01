package com.ponyo.thewitchslegacy.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private static final String VERSION = "1";

    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToClient(BarrierSyncPayload.TYPE, BarrierSyncPayload.STREAM_CODEC, BarrierSyncPayload::handle);
    }
}
