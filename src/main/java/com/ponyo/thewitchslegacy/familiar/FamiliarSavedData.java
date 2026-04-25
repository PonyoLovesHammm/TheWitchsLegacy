package com.ponyo.thewitchslegacy.familiar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FamiliarSavedData extends SavedData {
    private static final Codec<FamiliarRecord> FAMILIAR_RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("familiar_id").forGetter(FamiliarRecord::familiarId),
            Codec.STRING.fieldOf("familiar_name").forGetter(FamiliarRecord::familiarName)
    ).apply(instance, FamiliarRecord::new));

    private static final Codec<FamiliarSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, FAMILIAR_RECORD_CODEC)
                    .optionalFieldOf("owner_familiars", Map.of())
                    .forGetter(data -> data.ownerFamiliars)
    ).apply(instance, FamiliarSavedData::new));

    public static final SavedDataType<FamiliarSavedData> TYPE = new SavedDataType<>(
            "thewitchslegacy_familiars",
            level -> new FamiliarSavedData(),
            level -> CODEC
    );

    private final Map<String, FamiliarRecord> ownerFamiliars;

    public FamiliarSavedData() {
        this(new HashMap<>());
    }

    private FamiliarSavedData(Map<String, FamiliarRecord> ownerFamiliars) {
        this.ownerFamiliars = new HashMap<>(ownerFamiliars);
    }

    public static FamiliarSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean hasFamiliar(ServerLevel level, UUID ownerId) {
        return getFamiliar(level.getServer(), ownerId).isPresent();
    }

    public Optional<FamiliarRecord> getFamiliar(MinecraftServer server, UUID ownerId) {
        FamiliarRecord record = this.ownerFamiliars.get(ownerId.toString());
        if (record == null) {
            return Optional.empty();
        }

        UUID familiarId;
        try {
            familiarId = UUID.fromString(record.familiarId());
        } catch (IllegalArgumentException exception) {
            this.ownerFamiliars.remove(ownerId.toString());
            this.setDirty();
            return Optional.empty();
        }

        Entity familiarEntity = findEntity(server, familiarId);
        if (familiarEntity instanceof Cat cat && cat.isAlive()) {
            String liveName = cat.getName().getString();
            if (!liveName.equals(record.familiarName())) {
                FamiliarRecord updated = new FamiliarRecord(record.familiarId(), liveName);
                this.ownerFamiliars.put(ownerId.toString(), updated);
                this.setDirty();
                return Optional.of(updated);
            }
        }

        return Optional.of(record);
    }

    public boolean isRegisteredFamiliar(UUID familiarId) {
        String familiarIdString = familiarId.toString();
        return this.ownerFamiliars.values().stream().anyMatch(record -> record.familiarId().equals(familiarIdString));
    }

    public void bindFamiliar(UUID ownerId, Cat cat) {
        this.ownerFamiliars.put(ownerId.toString(), new FamiliarRecord(cat.getUUID().toString(), cat.getName().getString()));
        this.setDirty();
    }

    public void clearFamiliar(UUID ownerId) {
        if (this.ownerFamiliars.remove(ownerId.toString()) != null) {
            this.setDirty();
        }
    }

    public void clearFamiliarByEntity(UUID familiarId) {
        String familiarIdString = familiarId.toString();
        String ownerKey = this.ownerFamiliars.entrySet().stream()
                .filter(entry -> entry.getValue().familiarId().equals(familiarIdString))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (ownerKey != null) {
            this.ownerFamiliars.remove(ownerKey);
            this.setDirty();
        }
    }

    private static Entity findEntity(MinecraftServer server, UUID entityId) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityId);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    public record FamiliarRecord(String familiarId, String familiarName) {
    }
}
