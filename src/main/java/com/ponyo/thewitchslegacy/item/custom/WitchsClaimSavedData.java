package com.ponyo.thewitchslegacy.item.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WitchsClaimSavedData extends SavedData {
    private static final Codec<PlayerBedRecord> PLAYER_BED_RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("owner_id").forGetter(PlayerBedRecord::ownerId),
            Codec.STRING.fieldOf("owner_name").forGetter(PlayerBedRecord::ownerName),
            Codec.STRING.fieldOf("dimension_id").forGetter(PlayerBedRecord::dimensionId),
            Codec.INT.fieldOf("x").forGetter(PlayerBedRecord::x),
            Codec.INT.fieldOf("y").forGetter(PlayerBedRecord::y),
            Codec.INT.fieldOf("z").forGetter(PlayerBedRecord::z)
    ).apply(instance, PlayerBedRecord::new));

    private static final Codec<BedOwnerRecord> BED_OWNER_RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("owner_id").forGetter(BedOwnerRecord::ownerId),
            Codec.STRING.fieldOf("owner_name").forGetter(BedOwnerRecord::ownerName)
    ).apply(instance, BedOwnerRecord::new));

    private static final Codec<WitchsClaimSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, PLAYER_BED_RECORD_CODEC)
                    .optionalFieldOf("player_beds", Map.of())
                    .forGetter(data -> data.playerBeds),
            Codec.unboundedMap(Codec.STRING, BED_OWNER_RECORD_CODEC)
                    .optionalFieldOf("bed_owners", Map.of())
                    .forGetter(data -> data.bedOwners),
            Codec.unboundedMap(Codec.STRING, Codec.LONG)
                    .optionalFieldOf("bed_claim_days", Map.of())
                    .forGetter(data -> data.bedClaimDays)
    ).apply(instance, WitchsClaimSavedData::new));

    public static final SavedDataType<WitchsClaimSavedData> TYPE = new SavedDataType<>(
            "thewitchslegacy_witchs_claims",
            level -> new WitchsClaimSavedData(),
            level -> CODEC
    );

    private final Map<String, PlayerBedRecord> playerBeds;
    private final Map<String, BedOwnerRecord> bedOwners;
    private final Map<String, Long> bedClaimDays;

    public WitchsClaimSavedData() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    private WitchsClaimSavedData(Map<String, PlayerBedRecord> playerBeds, Map<String, BedOwnerRecord> bedOwners,
                                 Map<String, Long> bedClaimDays) {
        this.playerBeds = new HashMap<>(playerBeds);
        this.bedOwners = new HashMap<>(bedOwners);
        this.bedClaimDays = new HashMap<>(bedClaimDays);
    }

    public static WitchsClaimSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Optional<BedOwnerRecord> getBedOwner(Level level, BlockPos bedPos) {
        return Optional.ofNullable(this.bedOwners.get(bedKey(level.dimension().identifier().toString(), bedPos)));
    }

    public boolean canClaimBed(Level level, BlockPos bedPos) {
        String key = bedKey(level.dimension().identifier().toString(), bedPos);
        long currentDay = level.getDayTime() / 24000L;
        return this.bedClaimDays.getOrDefault(key, -1L) != currentDay;
    }

    public void markBedClaimed(Level level, BlockPos bedPos) {
        String key = bedKey(level.dimension().identifier().toString(), bedPos);
        long currentDay = level.getDayTime() / 24000L;
        this.bedClaimDays.put(key, currentDay);
        this.setDirty();
    }

    public void bindBed(ServerLevel level, UUID ownerId, String ownerName, BlockPos bedPos) {
        clearOwnerBed(ownerId);

        String ownerKey = ownerId.toString();
        String dimensionId = level.dimension().identifier().toString();
        PlayerBedRecord playerBedRecord = new PlayerBedRecord(ownerKey, ownerName, dimensionId, bedPos.getX(), bedPos.getY(), bedPos.getZ());
        this.playerBeds.put(ownerKey, playerBedRecord);
        this.bedOwners.put(bedKey(dimensionId, bedPos), new BedOwnerRecord(ownerKey, ownerName));
        this.setDirty();
    }

    public void clearOwnerBed(UUID ownerId) {
        String ownerKey = ownerId.toString();
        PlayerBedRecord removed = this.playerBeds.remove(ownerKey);
        if (removed != null) {
            this.bedOwners.remove(bedKey(removed.dimensionId(), new BlockPos(removed.x(), removed.y(), removed.z())));
            this.setDirty();
        }
    }

    public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel spawnLevel = player.level().getServer() != null ? player.level().getServer().getLevel(event.getSpawnLevel()) : null;
        WitchsClaimSavedData data = get((ServerLevel) player.level());
        if (event.getNewSpawn() == null || spawnLevel == null) {
            data.clearOwnerBed(player.getUUID());
            return;
        }

        BlockPos normalizedBedPos = normalizeBedPos(spawnLevel, event.getNewSpawn());
        if (!isBed(spawnLevel, normalizedBedPos)) {
            data.clearOwnerBed(player.getUUID());
            return;
        }

        data.bindBed(spawnLevel, player.getUUID(), player.getName().getString(), normalizedBedPos);
    }

    public static boolean isBed(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof BedBlock;
    }

    public static BlockPos normalizeBedPos(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock) || !state.hasProperty(BedBlock.PART) || !state.hasProperty(BedBlock.FACING)) {
            return pos;
        }

        return state.getValue(BedBlock.PART) == BedPart.HEAD ? pos : pos.relative(state.getValue(BedBlock.FACING));
    }

    private static String bedKey(String dimensionId, BlockPos pos) {
        return dimensionId + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
    }

    public record PlayerBedRecord(String ownerId, String ownerName, String dimensionId, int x, int y, int z) {
    }

    public record BedOwnerRecord(String ownerId, String ownerName) {
    }
}
