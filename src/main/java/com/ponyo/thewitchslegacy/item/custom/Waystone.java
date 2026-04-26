package com.ponyo.thewitchslegacy.item.custom;

import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class Waystone extends Item {
    private static final String LOCATION_TAG = "WaystoneLocation";
    private static final String DIMENSION_TAG = "Dimension";
    private static final String X_TAG = "X";
    private static final String Y_TAG = "Y";
    private static final String Z_TAG = "Z";
    private static final String BLOOD_TARGET_TAG = "BloodTarget";
    private static final String ENTITY_UUID_TAG = "EntityUuid";
    private static final String ENTITY_NAME_TAG = "EntityName";

    public Waystone(Properties properties) {
        super(properties);
    }

    public static ItemStack createBoundWaystone(Item item, Level level, BlockPos glyphPos) {
        ItemStack stack = new ItemStack(item);
        bindToLocation(stack, level, glyphPos);
        return stack;
    }

    public static ItemStack createBloodedWaystone(Item item, ServerPlayer player) {
        ItemStack stack = new ItemStack(item);
        bindToPlayer(stack, player);
        return stack;
    }

    public static void bindToLocation(ItemStack stack, Level level, BlockPos glyphPos) {
        CompoundTag root = getRootTag(stack);
        CompoundTag locationTag = new CompoundTag();
        writeLocation(locationTag, level.dimension().identifier().toString(), glyphPos.getX(), glyphPos.getY(), glyphPos.getZ());
        root.put(LOCATION_TAG, locationTag);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
    }

    public static void bindToPlayer(ItemStack stack, ServerPlayer player) {
        bindToPlayer(stack, player.getUUID(), player.getName().getString());
    }

    public static void bindToPlayer(ItemStack stack, UUID playerUuid, String playerName) {
        CompoundTag root = getRootTag(stack);
        root.remove(LOCATION_TAG);

        CompoundTag bloodTargetTag = new CompoundTag();
        bloodTargetTag.putString(ENTITY_UUID_TAG, playerUuid.toString());
        bloodTargetTag.putString(ENTITY_NAME_TAG, playerName);
        root.put(BLOOD_TARGET_TAG, bloodTargetTag);

        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
    }

    public static Optional<StoredLocation> getStoredLocation(ItemStack stack) {
        CompoundTag root = getRootTag(stack);
        if (!root.contains(LOCATION_TAG)) {
            return Optional.empty();
        }

        CompoundTag locationTag = root.getCompoundOrEmpty(LOCATION_TAG);
        return parseLocation(locationTag);
    }

    public static Optional<BloodTarget> getBloodTarget(ItemStack stack) {
        CompoundTag root = getRootTag(stack);
        if (!root.contains(BLOOD_TARGET_TAG)) {
            return Optional.empty();
        }

        CompoundTag targetTag = root.getCompoundOrEmpty(BLOOD_TARGET_TAG);
        Optional<String> entityUuid = targetTag.getString(ENTITY_UUID_TAG);
        if (entityUuid.isEmpty()) {
            return Optional.empty();
        }

        UUID parsedUuid;
        try {
            parsedUuid = UUID.fromString(entityUuid.get());
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        return Optional.of(new BloodTarget(
                parsedUuid,
                targetTag.getString(ENTITY_NAME_TAG).orElse("")
        ));
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.is(ModItems.BOUND_WAYSTONE.get()) && getStoredLocation(stack).isPresent()) {
            StoredLocation location = getStoredLocation(stack).orElseThrow();
            return Component.translatable(
                    "item.thewitchslegacy.bound_waystone.filled",
                    formatCoordinate(location.x()),
                    formatCoordinate(location.y()),
                    formatCoordinate(location.z())
            );
        }
        if (stack.is(ModItems.BLOODED_WAYSTONE.get()) && getBloodTarget(stack).isPresent()) {
            BloodTarget target = getBloodTarget(stack).orElseThrow();
            return Component.translatable("item.thewitchslegacy.blooded_waystone.filled", target.entityName());
        }

        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        getStoredLocation(stack).ifPresent(location -> tooltipAdder.accept(
                Component.translatable(
                                "tooltip.thewitchslegacy.waystone.location",
                                formatCoordinate(location.x()),
                                formatCoordinate(location.y()),
                                formatCoordinate(location.z())
                        )
                        .withStyle(ChatFormatting.GRAY)
        ));

        getStoredLocation(stack).ifPresent(location -> tooltipAdder.accept(
                Component.translatable("tooltip.thewitchslegacy.waystone.dimension", location.dimensionId())
                        .withStyle(ChatFormatting.DARK_GRAY)
        ));

        getBloodTarget(stack).ifPresent(target -> tooltipAdder.accept(
                Component.translatable("tooltip.thewitchslegacy.waystone.blood_target", target.entityName())
                        .withStyle(ChatFormatting.DARK_RED)
        ));
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag();
    }

    private static void writeLocation(CompoundTag tag, String dimensionId, double x, double y, double z) {
        tag.putString(DIMENSION_TAG, dimensionId);
        tag.putDouble(X_TAG, x);
        tag.putDouble(Y_TAG, y);
        tag.putDouble(Z_TAG, z);
    }

    private static Optional<StoredLocation> parseLocation(CompoundTag tag) {
        Optional<String> dimensionName = tag.getString(DIMENSION_TAG);
        Optional<Double> x = tag.getDouble(X_TAG);
        Optional<Double> y = tag.getDouble(Y_TAG);
        Optional<Double> z = tag.getDouble(Z_TAG);
        if (dimensionName.isEmpty() || x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new StoredLocation(dimensionName.get(), x.get(), y.get(), z.get()));
    }

    private static String formatCoordinate(double value) {
        if (Math.rint(value) == value) {
            return Integer.toString((int) value);
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    public record StoredLocation(String dimensionId, double x, double y, double z) {
        public BlockPos blockPos() {
            return BlockPos.containing(x, y, z);
        }
    }

    public record BloodTarget(UUID entityUuid, String entityName) {
    }
}
