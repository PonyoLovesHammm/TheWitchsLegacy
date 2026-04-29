package com.ponyo.thewitchslegacy.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class PoppetShelfSavedData extends SavedData {
    private static final Codec<List<ItemStack>> SHELF_ITEMS_CODEC = ItemStack.OPTIONAL_CODEC.listOf()
            .xmap(PoppetShelfSavedData::normalizeItems, PoppetShelfSavedData::normalizeItems);

    private static final Codec<ShelfRecord> SHELF_RECORD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("dimension_id").forGetter(ShelfRecord::dimensionId),
            Codec.INT.fieldOf("x").forGetter(ShelfRecord::x),
            Codec.INT.fieldOf("y").forGetter(ShelfRecord::y),
            Codec.INT.fieldOf("z").forGetter(ShelfRecord::z),
            SHELF_ITEMS_CODEC.fieldOf("items").forGetter(ShelfRecord::items)
    ).apply(instance, ShelfRecord::new));

    private static final Codec<PoppetShelfSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, SHELF_RECORD_CODEC)
                    .optionalFieldOf("shelves", Map.of())
                    .forGetter(data -> data.shelves)
    ).apply(instance, PoppetShelfSavedData::new));

    public static final SavedDataType<PoppetShelfSavedData> TYPE = new SavedDataType<>(
            "thewitchslegacy_poppet_shelves",
            level -> new PoppetShelfSavedData(),
            level -> CODEC
    );

    private final Map<String, ShelfRecord> shelves;

    public PoppetShelfSavedData() {
        this(new HashMap<>());
    }

    private PoppetShelfSavedData(Map<String, ShelfRecord> shelves) {
        this.shelves = new HashMap<>(shelves);
    }

    public static PoppetShelfSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public void updateShelf(ServerLevel level, BlockPos pos, NonNullList<ItemStack> items) {
        this.shelves.put(shelfKey(level, pos), new ShelfRecord(
                level.dimension().identifier().toString(),
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                copyItems(items)
        ));
        this.setDirty();
    }

    public Optional<NonNullList<ItemStack>> getShelfItems(ServerLevel level, BlockPos pos) {
        ShelfRecord record = this.shelves.get(shelfKey(level, pos));
        if (record == null) {
            return Optional.empty();
        }

        NonNullList<ItemStack> items = NonNullList.withSize(PoppetShelfBlockEntity.SLOT_COUNT, ItemStack.EMPTY);
        for (int slot = 0; slot < PoppetShelfBlockEntity.SLOT_COUNT; slot++) {
            ItemStack stack = slot < record.items().size() ? record.items().get(slot) : ItemStack.EMPTY;
            items.set(slot, stack.copy());
        }
        return Optional.of(items);
    }

    public void removeShelf(ServerLevel level, BlockPos pos) {
        if (this.shelves.remove(shelfKey(level, pos)) != null) {
            this.setDirty();
        }
    }

    public Optional<ShelfPoppet> findFirst(ServerPlayer player, Predicate<ItemStack> predicate) {
        for (Map.Entry<String, ShelfRecord> entry : this.shelves.entrySet()) {
            ShelfRecord record = entry.getValue();
            for (int slot = 0; slot < record.items().size(); slot++) {
                ItemStack stack = record.items().get(slot);
                if (!stack.isEmpty() && predicate.test(stack)) {
                    return Optional.of(new ShelfPoppet(this, entry.getKey(), slot, stack));
                }
            }
        }

        return Optional.empty();
    }

    public void setStack(ServerPlayer player, String key, int slot, ItemStack stack) {
        ShelfRecord record = this.shelves.get(key);
        if (record == null || slot < 0 || slot >= record.items().size()) {
            return;
        }

        List<ItemStack> items = copyItems(record.items());
        items.set(slot, stack.copy());
        ShelfRecord updated = new ShelfRecord(record.dimensionId(), record.x(), record.y(), record.z(), items);
        this.shelves.put(key, updated);
        this.setDirty();
        syncLoadedBlockEntity(player, updated, slot, stack);
    }

    private static void syncLoadedBlockEntity(ServerPlayer player, ShelfRecord record, int slot, ItemStack stack) {
        Identifier dimensionId = Identifier.tryParse(record.dimensionId());
        if (dimensionId == null) {
            return;
        }

        ServerLevel level = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
        if (level == null) {
            return;
        }

        BlockPos pos = new BlockPos(record.x(), record.y(), record.z());
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity blockEntity) {
            blockEntity.setItemFromSavedData(slot, stack);
        }
    }

    private static String shelfKey(ServerLevel level, BlockPos pos) {
        return shelfKey(level.dimension().identifier().toString(), pos);
    }

    private static String shelfKey(String dimensionId, BlockPos pos) {
        return dimensionId + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
    }

    private static List<ItemStack> normalizeItems(List<ItemStack> items) {
        List<ItemStack> normalized = new ArrayList<>(PoppetShelfBlockEntity.SLOT_COUNT);
        for (int slot = 0; slot < PoppetShelfBlockEntity.SLOT_COUNT; slot++) {
            ItemStack stack = slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
            normalized.add(stack.copy());
        }
        return normalized;
    }

    private static List<ItemStack> copyItems(List<ItemStack> items) {
        List<ItemStack> copy = new ArrayList<>(PoppetShelfBlockEntity.SLOT_COUNT);
        for (int slot = 0; slot < PoppetShelfBlockEntity.SLOT_COUNT; slot++) {
            ItemStack stack = slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
            copy.add(stack.copy());
        }
        return copy;
    }

    private static List<ItemStack> copyItems(NonNullList<ItemStack> items) {
        return copyItems((List<ItemStack>) items);
    }

    private record ShelfRecord(String dimensionId, int x, int y, int z, List<ItemStack> items) {
    }

    public record ShelfPoppet(PoppetShelfSavedData data, String shelfKey, int slot, ItemStack stack) {
        public void setChanged(ServerPlayer player) {
            this.data.setStack(player, this.shelfKey, this.slot, this.stack);
        }
    }
}
