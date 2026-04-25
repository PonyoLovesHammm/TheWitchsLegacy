package com.ponyo.thewitchslegacy.item.custom;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.custom.Glyph;
import com.ponyo.thewitchslegacy.ritual.RitualPatterns;
import com.ponyo.thewitchslegacy.ritual.RitualRingSize;
import com.ponyo.thewitchslegacy.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CircleTalisman extends Item {
    private static final String TALISMAN_RINGS_TAG = "CircleTalismanRings";
    private static final String SIZE_TAG = "Size";
    private static final String COLOR_TAG = "Color";
    private static final Comparator<RingData> RING_ORDER = Comparator.comparingInt(ring -> ring.size().ordinal());
    private static final int RING_TIER_COUNT = RitualRingSize.values().length;

    public CircleTalisman(Properties properties) {
        super(properties);
    }

    public static ItemStack createWithSingleRing(Item item, RitualRingSize size, RingColor color) {
        return createWithRings(item, List.of(new RingData(size, color)));
    }

    public static ItemStack createWithRings(Item item, List<RingData> rings) {
        ItemStack stack = new ItemStack(item);
        setStoredRings(stack, rings);
        return stack;
    }

    public static boolean isEmpty(ItemStack stack) {
        return stack.getItem() instanceof CircleTalisman && getStoredRings(stack).isEmpty();
    }

    public static List<RingData> storedRings(ItemStack stack) {
        return getStoredRings(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        List<RingData> rings = getStoredRings(stack);
        if (rings.isEmpty()) {
            return super.getName(stack);
        }

        MutableComponent ringList = Component.empty();
        for (int i = 0; i < rings.size(); i++) {
            if (i > 0) {
                ringList.append(Component.literal(", "));
            }
            ringList.append(rings.get(i).displayName());
        }

        return Component.translatable("item.thewitchslegacy.circle_talisman.filled", ringList);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        List<RingData> rings = getStoredRings(stack);

        if (rings.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }

        BlockPos centerPos = context.getClickedPos();
        if (!canPlaceStoredRings(level, centerPos, rings)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            placeStoredRings(level, centerPos, rings);
            clearStoredRings(stack);
            level.playSound(null, centerPos.above(), ModSounds.CHALK_DRAW.get(), SoundSource.BLOCKS);
        }

        return InteractionResult.SUCCESS;
    }

    private static boolean canPlaceStoredRings(Level level, BlockPos centerPos, List<RingData> rings) {
        BlockPos centerGlyphPos = centerPos.above();
        BlockState centerSupportState = level.getBlockState(centerPos);
        BlockState centerGlyphState = level.getBlockState(centerGlyphPos);

        if (!centerSupportState.isFaceSturdy(level, centerPos, Direction.UP)) {
            return false;
        }
        if (!centerGlyphState.canBeReplaced()) {
            return false;
        }

        for (RingData ring : rings) {
            for (BlockPos offset : RitualPatterns.positionsFor(ring.size())) {
                BlockPos supportPos = centerPos.offset(offset);
                BlockPos glyphPos = supportPos.above();
                BlockState supportState = level.getBlockState(supportPos);
                BlockState glyphState = level.getBlockState(glyphPos);

                if (!supportState.isFaceSturdy(level, supportPos, Direction.UP)) {
                    return false;
                }
                if (!glyphState.canBeReplaced()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static void placeStoredRings(Level level, BlockPos centerPos, List<RingData> rings) {
        level.setBlock(centerPos.above(), ModBlocks.GOLDEN_GLYPH.get().defaultBlockState().setValue(Glyph.VARIANT, 0), 3);

        for (RingData ring : rings) {
            Block glyphBlock = ring.color().glyphBlock();
            for (BlockPos offset : RitualPatterns.positionsFor(ring.size())) {
                BlockPos glyphPos = centerPos.offset(offset).above();
                int variant = level.getRandom().nextInt(12);
                level.setBlock(glyphPos, glyphBlock.defaultBlockState().setValue(Glyph.VARIANT, variant), 3);
            }
        }
    }

    private static List<RingData> getStoredRings(ItemStack stack) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!customData.contains(TALISMAN_RINGS_TAG)) {
            return List.of();
        }

        CompoundTag tag = customData.copyTag();
        ListTag ringTags = tag.getList(TALISMAN_RINGS_TAG).orElseGet(ListTag::new);
        List<RingData> rings = new ArrayList<>(ringTags.size());
        for (Tag ringTag : ringTags) {
            if (!(ringTag instanceof CompoundTag ringDataTag)) {
                continue;
            }

            RitualRingSize size = parseRingSize(ringDataTag.getString(SIZE_TAG).orElse(""));
            RingColor color = RingColor.fromSerializedName(ringDataTag.getString(COLOR_TAG).orElse(""));
            if (size == null || color == null) {
                continue;
            }

            rings.add(new RingData(size, color));
        }

        rings.sort(RING_ORDER);
        if (rings.size() > 3) {
            return List.copyOf(rings.subList(0, 3));
        }
        return List.copyOf(rings);
    }

    private static void setStoredRings(ItemStack stack, List<RingData> rings) {
        List<RingData> normalizedRings = new ArrayList<>(rings);
        normalizedRings.sort(RING_ORDER);

        if (normalizedRings.isEmpty()) {
            clearStoredRings(stack);
            return;
        }

        ListTag ringTags = new ListTag();
        for (RingData ring : normalizedRings.stream().limit(3).toList()) {
            CompoundTag ringTag = new CompoundTag();
            ringTag.putString(SIZE_TAG, serializeRingSize(ring.size()));
            ringTag.putString(COLOR_TAG, ring.color().serializedName());
            ringTags.add(ringTag);
        }

        CompoundTag tag = new CompoundTag();
        tag.put(TALISMAN_RINGS_TAG, ringTags);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                List.of(),
                List.of(),
                createModelSelectors(normalizedRings),
                List.of()
        ));
    }

    private static void clearStoredRings(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
        stack.remove(DataComponents.CUSTOM_MODEL_DATA);
    }

    private static List<String> createModelSelectors(List<RingData> rings) {
        List<String> selectors = new ArrayList<>(RING_TIER_COUNT);
        for (int i = 0; i < RING_TIER_COUNT; i++) {
            selectors.add("");
        }

        for (RingData ring : rings.stream().limit(RING_TIER_COUNT).toList()) {
            selectors.set(ring.size().ordinal(), ring.color().serializedName());
        }

        return List.copyOf(selectors);
    }

    private static String serializeRingSize(RitualRingSize size) {
        return size.name().toLowerCase(Locale.ROOT);
    }

    private static RitualRingSize parseRingSize(String name) {
        return switch (name) {
            case "small" -> RitualRingSize.SMALL;
            case "medium" -> RitualRingSize.MEDIUM;
            case "large" -> RitualRingSize.LARGE;
            default -> null;
        };
    }

    public record RingData(RitualRingSize size, RingColor color) {
        public Component displayName() {
            return Component.translatable("item.thewitchslegacy.circle_talisman.ring",
                    Component.translatable("item.thewitchslegacy.circle_talisman.size." + serializeRingSize(size)),
                    Component.translatable("item.thewitchslegacy.circle_talisman.color." + color.serializedName()));
        }
    }

    public enum RingColor {
        WHITE("white") {
            @Override
            public Block glyphBlock() {
                return ModBlocks.WHITE_GLYPH.get();
            }
        },
        FIERY("fiery") {
            @Override
            public Block glyphBlock() {
                return ModBlocks.FIERY_GLYPH.get();
            }
        },
        OTHERWHERE("otherwhere") {
            @Override
            public Block glyphBlock() {
                return ModBlocks.OTHERWHERE_GLYPH.get();
            }
        };

        private final String serializedName;

        RingColor(String serializedName) {
            this.serializedName = serializedName;
        }

        public abstract Block glyphBlock();

        public String serializedName() {
            return serializedName;
        }

        public static RingColor fromSerializedName(String name) {
            for (RingColor color : values()) {
                if (color.serializedName.equals(name)) {
                    return color;
                }
            }
            return null;
        }
    }
}
