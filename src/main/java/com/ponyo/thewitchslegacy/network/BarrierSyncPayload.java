package com.ponyo.thewitchslegacy.network;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.ritual.definitions.barrier.RiteOfProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record BarrierSyncPayload(List<RiteOfProtection.SyncedBarrier> barriers) implements CustomPacketPayload {
    public static final Type<BarrierSyncPayload> TYPE = new Type<>(TheWitchsLegacy.id("barrier_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BarrierSyncPayload> STREAM_CODEC =
            CustomPacketPayload.codec(BarrierSyncPayload::write, BarrierSyncPayload::read);

    private static BarrierSyncPayload read(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        List<RiteOfProtection.SyncedBarrier> barriers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Identifier dimensionId = buffer.readIdentifier();
            BlockPos centerPos = buffer.readBlockPos();
            double radius = buffer.readDouble();
            int movementRuleId = buffer.readVarInt();
            UUID casterId = buffer.readUUID();
            barriers.add(new RiteOfProtection.SyncedBarrier(dimensionId, centerPos, radius, movementRuleId, casterId));
        }
        return new BarrierSyncPayload(List.copyOf(barriers));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.barriers.size());
        for (RiteOfProtection.SyncedBarrier barrier : this.barriers) {
            buffer.writeIdentifier(barrier.dimensionId());
            buffer.writeBlockPos(barrier.centerPos());
            buffer.writeDouble(barrier.radius());
            buffer.writeVarInt(barrier.movementRuleId());
            buffer.writeUUID(barrier.casterId());
        }
    }

    public static void handle(BarrierSyncPayload payload, IPayloadContext context) {
        RiteOfProtection.replaceClientSyncedBarriers(payload.barriers());
    }

    @Override
    public Type<BarrierSyncPayload> type() {
        return TYPE;
    }
}
