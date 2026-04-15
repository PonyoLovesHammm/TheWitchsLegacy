package com.ponyo.thewitchslegacy.block;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.custom.Glyph;
import com.ponyo.thewitchslegacy.block.custom.SpanishMoss;
import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheWitchsLegacy.MODID);

    //Default Blocks!
    public static final DeferredBlock<Block> ROWAN_LOG = registerBlock(
            "rowan_log",
            () -> new RotatedPillarBlock(
                    blockProperties("rowan_log", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> ROWAN_PLANKS = registerBlock(
            "rowan_planks",
            () -> new Block(
                    blockProperties("rowan_planks", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<StairBlock> ROWAN_STAIRS = registerBlock(
            "rowan_stairs",
            () -> new StairBlock(ModBlocks.ROWAN_PLANKS.get().defaultBlockState(),
                    blockProperties("rowan_stairs", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<SlabBlock> ROWAN_SLAB = registerBlock(
            "rowan_slab",
            () -> new SlabBlock(blockProperties("rowan_slab", BlockBehaviour.Properties.of())
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));

    public static final DeferredBlock<PressurePlateBlock> ROWAN_PRESSURE_PLATE = registerBlock(
            "rowan_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.OAK,
                    blockProperties("rowan_pressure_plate", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<ButtonBlock> ROWAN_BUTTON = registerBlock(
            "rowan_button",
            () -> new ButtonBlock(BlockSetType.OAK, 20,
                    blockProperties("rowan_button", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noCollision()));

    public static final DeferredBlock<FenceBlock> ROWAN_FENCE = registerBlock(
            "rowan_fence",
            () -> new FenceBlock(
                    blockProperties("rowan_fence", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<FenceGateBlock> ROWAN_FENCE_GATE = registerBlock(
            "rowan_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK,
                    blockProperties("rowan_fence_gate", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<DoorBlock> ROWAN_DOOR = registerBlock(
            "rowan_door",
            () -> new DoorBlock(BlockSetType.OAK,
                    blockProperties("rowan_door", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    public static final DeferredBlock<TrapDoorBlock> ROWAN_TRAPDOOR = registerBlock(
            "rowan_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK,
                    blockProperties("rowan_trapdoor", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    public static final DeferredBlock<Block> HAWTHORN_LOG = registerBlock(
            "hawthorn_log",
            () -> new RotatedPillarBlock(
                    blockProperties("hawthorn_log", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> HAWTHORN_PLANKS = registerBlock(
            "hawthorn_planks",
            () -> new Block(
                    blockProperties("hawthorn_planks", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<StairBlock> HAWTHORN_STAIRS = registerBlock(
            "hawthorn_stairs",
            () -> new StairBlock(ModBlocks.HAWTHORN_PLANKS.get().defaultBlockState(),
                    blockProperties("hawthorn_stairs", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<SlabBlock> HAWTHORN_SLAB = registerBlock(
            "hawthorn_slab",
            () -> new SlabBlock(blockProperties("hawthorn_slab", BlockBehaviour.Properties.of())
                    .strength(2.0F)
                    .sound(SoundType.WOOD)));

    public static final DeferredBlock<PressurePlateBlock> HAWTHORN_PRESSURE_PLATE = registerBlock(
            "hawthorn_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.OAK,
                    blockProperties("hawthorn_pressure_plate", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<ButtonBlock> HAWTHORN_BUTTON = registerBlock(
            "hawthorn_button",
            () -> new ButtonBlock(BlockSetType.OAK, 20,
                    blockProperties("hawthorn_button", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noCollision()));

    public static final DeferredBlock<FenceBlock> HAWTHORN_FENCE = registerBlock(
            "hawthorn_fence",
            () -> new FenceBlock(
                    blockProperties("hawthorn_fence", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<FenceGateBlock> HAWTHORN_FENCE_GATE = registerBlock(
            "hawthorn_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK,
                    blockProperties("hawthorn_fence_gate", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<DoorBlock> HAWTHORN_DOOR = registerBlock(
            "hawthorn_door",
            () -> new DoorBlock(BlockSetType.OAK,
                    blockProperties("hawthorn_door", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    public static final DeferredBlock<TrapDoorBlock> HAWTHORN_TRAPDOOR = registerBlock(
            "hawthorn_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK,
                    blockProperties("hawthorn_trapdoor", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    public static final DeferredBlock<Block> WILLOW_LOG = registerBlock(
            "willow_log",
            () -> new RotatedPillarBlock(
                    blockProperties("willow_log", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> WILLOW_PLANKS = registerBlock(
            "willow_planks",
            () -> new Block(
                    blockProperties("willow_planks", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<StairBlock> WILLOW_STAIRS = registerBlock(
            "willow_stairs",
            () -> new StairBlock(ModBlocks.WILLOW_PLANKS.get().defaultBlockState(),
                    blockProperties("willow_stairs", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<SlabBlock> WILLOW_SLAB = registerBlock(
            "willow_slab",
            () -> new SlabBlock(blockProperties("willow_slab", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<PressurePlateBlock> WILLOW_PRESSURE_PLATE = registerBlock(
            "willow_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.OAK,
                    blockProperties("willow_pressure_plate", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<ButtonBlock> WILLOW_BUTTON = registerBlock(
            "willow_button",
            () -> new ButtonBlock(BlockSetType.OAK, 20,
                    blockProperties("willow_button", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noCollision()));

    public static final DeferredBlock<FenceBlock> WILLOW_FENCE = registerBlock(
            "willow_fence",
            () -> new FenceBlock(
                    blockProperties("willow_fence", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<FenceGateBlock> WILLOW_FENCE_GATE = registerBlock(
            "willow_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK,
                    blockProperties("willow_fence_gate", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<DoorBlock> WILLOW_DOOR = registerBlock(
            "willow_door",
            () -> new DoorBlock(BlockSetType.OAK,
                    blockProperties("willow_door", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    public static final DeferredBlock<TrapDoorBlock> WILLOW_TRAPDOOR = registerBlock(
            "willow_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK,
                    blockProperties("willow_trapdoor", BlockBehaviour.Properties.of())
                            .strength(2.0F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()));

    //Special Blocks!
    public static final DeferredBlock<Block> SPANISH_MOSS = registerBlock(
            "spanish_moss",
            () -> new SpanishMoss(
                    blockProperties("spanish_moss", BlockBehaviour.Properties.ofFullCopy(Blocks.VINE))));

    public static final DeferredBlock<Block> GOLDEN_GLYPH = registerBlock(
            "golden_glyph",
            () -> new Glyph(
                    blockProperties("golden_glyph", BlockBehaviour.Properties.of())
                            .strength(.1F)));

    public static final DeferredBlock<Block> FIERY_GLYPH = registerBlock(
            "fiery_glyph",
            () -> new Glyph(
                    blockProperties("fiery_glyph", BlockBehaviour.Properties.of())
                            .strength(.1F)));

    public static final DeferredBlock<Block> WHITE_GLYPH = registerBlock(
            "white_glyph",
            () -> new Glyph(
                    blockProperties("white_glyph", BlockBehaviour.Properties.of())
                            .strength(.1F)));

    public static final DeferredBlock<Block> OTHERWHERE_GLYPH = registerBlock(
            "otherwhere_glyph",
            () -> new Glyph(
                    blockProperties("otherwhere_glyph", BlockBehaviour.Properties.of())
                            .strength(.1F)));

    public static final DeferredBlock<Block> WITCH_CAULDRON = registerBlock(
            "witch_cauldron",
            () -> new Block(
                    blockProperties("witch_cauldron", BlockBehaviour.Properties.of())
                            .strength(2.5F)
                            .sound(SoundType.METAL)
                            .noOcclusion()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, name));
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().setId(key)));
    }

    private static BlockBehaviour.Properties blockProperties(String name, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, name));
        return properties.setId(key);
    }

    public static void register(IEventBus eventBus) { BLOCKS.register(eventBus); }
}
