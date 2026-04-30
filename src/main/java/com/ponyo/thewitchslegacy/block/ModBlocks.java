package com.ponyo.thewitchslegacy.block;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.custom.Glyph;
import com.ponyo.thewitchslegacy.block.custom.GlintWeed;
import com.ponyo.thewitchslegacy.block.custom.EmberMoss;
import com.ponyo.thewitchslegacy.block.custom.Altar;
import com.ponyo.thewitchslegacy.block.custom.Candelabra;
import com.ponyo.thewitchslegacy.block.custom.Chalice;
import com.ponyo.thewitchslegacy.block.custom.Distillery;
import com.ponyo.thewitchslegacy.block.custom.Kettle;
import com.ponyo.thewitchslegacy.block.custom.MagicMirror;
import com.ponyo.thewitchslegacy.block.custom.PoppetShelf;
import com.ponyo.thewitchslegacy.block.custom.WitchCrop;
import com.ponyo.thewitchslegacy.block.custom.SpanishMoss;
import com.ponyo.thewitchslegacy.block.custom.SpinningWheel;
import com.ponyo.thewitchslegacy.block.custom.WalkthroughLeaves;
import com.ponyo.thewitchslegacy.block.custom.WitchCauldron;
import com.ponyo.thewitchslegacy.block.custom.WitchOven;
import com.ponyo.thewitchslegacy.entity.MandrakeEntity;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.worldgen.tree.ModTreeGrowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

    public static final DeferredBlock<SaplingBlock> ROWAN_SAPLING = registerBlock(
            "rowan_sapling",
            () -> new SaplingBlock(ModTreeGrowers.ROWAN,
                    blockProperties("rowan_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING))));

    public static final DeferredBlock<TintedParticleLeavesBlock> ROWAN_LEAVES = registerBlock(
            "rowan_leaves",
            () -> new TintedParticleLeavesBlock(0.01F,
                    blockProperties("rowan_leaves", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES))));

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

    public static final DeferredBlock<SaplingBlock> HAWTHORN_SAPLING = registerBlock(
            "hawthorn_sapling",
            () -> new SaplingBlock(ModTreeGrowers.HAWTHORN,
                    blockProperties("hawthorn_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING))));

    public static final DeferredBlock<TintedParticleLeavesBlock> HAWTHORN_LEAVES = registerBlock(
            "hawthorn_leaves",
            () -> new TintedParticleLeavesBlock(0.01F,
                    blockProperties("hawthorn_leaves", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES))) {
                @Override
                public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
                    ItemStack tool = player.getMainHandItem();
                    return tool.is(Items.SHEARS);
                }
            });

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

    public static final DeferredBlock<SaplingBlock> WILLOW_SAPLING = registerBlock(
            "willow_sapling",
            () -> new SaplingBlock(ModTreeGrowers.WILLOW,
                    blockProperties("willow_sapling", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING))));

    public static final DeferredBlock<TintedParticleLeavesBlock> WILLOW_CANOPY_LEAVES = registerBlock(
            "willow_canopy_leaves",
            () -> new TintedParticleLeavesBlock(0.01F,
                    blockProperties("willow_canopy_leaves", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES))));

    public static final DeferredBlock<WalkthroughLeaves> WILLOW_LEAVES = registerBlock(
            "willow_leaves",
            () -> new WalkthroughLeaves(0.01F,
                    blockProperties("willow_leaves", BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES))));

    //Plant Blocks!
    public static final DeferredBlock<Block> SPANISH_MOSS = registerBlock(
            "spanish_moss",
            () -> new SpanishMoss(
                    blockProperties("spanish_moss", BlockBehaviour.Properties.ofFullCopy(Blocks.VINE))));

    public static final DeferredBlock<Block> GLINT_WEED = registerBlock(
            "glint_weed",
            () -> new GlintWeed(
                    decorativeProps("glint_weed", SoundType.GRASS, 0.2F)
                            .instabreak()
                            .lightLevel(state -> 7)
                            .replaceable()
                            .randomTicks()
                            .offsetType(BlockBehaviour.OffsetType.XZ)));

    public static final DeferredBlock<Block> EMBER_MOSS = registerBlock(
            "ember_moss",
            () -> new EmberMoss(
                    decorativeProps("ember_moss", SoundType.GRASS, 0.2F)
                            .instabreak()
                            .replaceable()
                            .noCollision()
                            .randomTicks()
                            .offsetType(BlockBehaviour.OffsetType.XZ)));

    public static final DeferredBlock<Block> BELLADONA_PLANT = registerBlock(
            "belladona_plant",
            () -> new TallFlowerBlock(
                    decorativeProps("belladona_plant", SoundType.GRASS, 0.0F)
                            .instabreak()
                            .replaceable()
                            .noCollision()
                            .offsetType(BlockBehaviour.OffsetType.XZ)));

    public static final DeferredBlock<Block> SNOWBELL_CROP = registerBlockWithoutItem(
            "snowbell_crop",
            () -> new WitchCrop(
                    blockProperties("snowbell_crop", BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)),
                    ModItems.SNOWBELL_SEEDS::get,
                    WitchCrop.createShapes(2.0D, 5.0D, 8.0D, 11.0D, 14.0D, 16.0D)));

    public static final DeferredBlock<Block> MANDRAKE_CROP = registerBlockWithoutItem(
            "mandrake_crop",
            () -> new WitchCrop(
                    blockProperties("mandrake_crop", BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)),
                    ModItems.MANDRAKE_SEEDS::get,
                    WitchCrop.createShapes(2.0D, 6.0D, 10.0D, 14.0D),
                    WitchCrop.SupportType.FARMLAND,
                    ModBlocks::handleMandrakeMatureBreak));

    public static final DeferredBlock<Block> WATER_ARTICHOKE_CROP = registerBlockWithoutItem(
            "water_artichoke_crop",
            () -> new WitchCrop(
                    blockProperties("water_artichoke_crop", BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)),
                    ModItems.WATER_ARTICHOKE_SEEDS::get,
                    WitchCrop.createShapes(2.0D, 5.0D, 8.0D, 11.0D, 14.0D),
                    WitchCrop.SupportType.WATER_SOURCE));

    public static final DeferredBlock<Block> GARLIC_CROP = registerBlockWithoutItem(
            "garlic_crop",
            () -> new WitchCrop(
                    blockProperties("garlic_crop", BlockBehaviour.Properties.ofFullCopy(Blocks.CARROTS)),
                    ModItems.GARLIC::get,
                    WitchCrop.createShapes(2.0D, 5.0D, 8.0D, 11.0D, 14.0D)));

    //Special Blocks!
    public static final DeferredBlock<Block> GOLDEN_GLYPH = registerBlockWithoutItem(
            "golden_glyph",
            () -> new Glyph(
                    blockProperties("golden_glyph", BlockBehaviour.Properties.of())
                            .strength(.1F)));

    public static final DeferredBlock<Block> FIERY_GLYPH = registerBlockWithoutItem(
            "fiery_glyph",
            () -> new Glyph(
                    blockProperties("fiery_glyph", BlockBehaviour.Properties.of())
                            .strength(.1F)));

    public static final DeferredBlock<Block> WHITE_GLYPH = registerBlockWithoutItem(
            "white_glyph",
            () -> new Glyph(
                    blockProperties("white_glyph", BlockBehaviour.Properties.of())
                            .strength(.1F)));

    public static final DeferredBlock<Block> OTHERWHERE_GLYPH = registerBlockWithoutItem(
            "otherwhere_glyph",
            () -> new Glyph(
                    blockProperties("otherwhere_glyph", BlockBehaviour.Properties.of())
                            .strength(.1F)));

    public static final DeferredBlock<Block> CANDELABRA = registerBlock(
            "candelabra",
            () -> new Candelabra(
                    decorativeProps("candelabra", SoundType.METAL, 2.0F)
                            .lightLevel(state -> 15)));
    public static final DeferredBlock<Block> WHITE_CANDELABRA = registerCandelabra("white_candelabra");
    public static final DeferredBlock<Block> ORANGE_CANDELABRA = registerCandelabra("orange_candelabra");
    public static final DeferredBlock<Block> MAGENTA_CANDELABRA = registerCandelabra("magenta_candelabra");
    public static final DeferredBlock<Block> LIGHT_BLUE_CANDELABRA = registerCandelabra("light_blue_candelabra");
    public static final DeferredBlock<Block> YELLOW_CANDELABRA = registerCandelabra("yellow_candelabra");
    public static final DeferredBlock<Block> LIME_CANDELABRA = registerCandelabra("lime_candelabra");
    public static final DeferredBlock<Block> PINK_CANDELABRA = registerCandelabra("pink_candelabra");
    public static final DeferredBlock<Block> GRAY_CANDELABRA = registerCandelabra("gray_candelabra");
    public static final DeferredBlock<Block> LIGHT_GRAY_CANDELABRA = registerCandelabra("light_gray_candelabra");
    public static final DeferredBlock<Block> CYAN_CANDELABRA = registerCandelabra("cyan_candelabra");
    public static final DeferredBlock<Block> PURPLE_CANDELABRA = registerCandelabra("purple_candelabra");
    public static final DeferredBlock<Block> BLUE_CANDELABRA = registerCandelabra("blue_candelabra");
    public static final DeferredBlock<Block> BROWN_CANDELABRA = registerCandelabra("brown_candelabra");
    public static final DeferredBlock<Block> GREEN_CANDELABRA = registerCandelabra("green_candelabra");
    public static final DeferredBlock<Block> RED_CANDELABRA = registerCandelabra("red_candelabra");
    public static final DeferredBlock<Block> BLACK_CANDELABRA = registerCandelabra("black_candelabra");

    public static final DeferredBlock<Block> WITCH_OVEN = registerBlock(
            "witch_oven",
            () -> new WitchOven(
                    decorativeProps("witch_oven", SoundType.STONE, 3.5F)
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 13 : 0)));

    public static final DeferredBlock<Block> WITCH_CAULDRON = registerBlock(
            "witch_cauldron",
            () -> new WitchCauldron(
                    decorativeProps("witch_cauldron", SoundType.METAL, 2.5F)));

    public static final DeferredBlock<Block> DISTILLERY = registerBlock(
            "distillery",
            () -> new Distillery(
                    decorativeProps("distillery", SoundType.METAL, 3.0F)));

    public static final DeferredBlock<Block> SPINNING_WHEEL = registerBlock(
            "spinning_wheel",
            () -> new SpinningWheel(
                    decorativeProps("spinning_wheel", SoundType.WOOD, 2.5F)));

    public static final DeferredBlock<Block> POPPET_SHELF = registerBlock(
            "poppet_shelf",
            () -> new PoppetShelf(
                    decorativeProps("poppet_shelf", SoundType.WOOD, 2.0F)));

    public static final DeferredBlock<Block> KETTLE = registerBlock(
            "kettle",
            () -> new Kettle(
                    decorativeProps("kettle", SoundType.METAL, 2.0F)));

    public static final DeferredBlock<Block> CHALICE = registerBlock(
            "chalice",
            () -> new Chalice(
                    decorativeProps("chalice", SoundType.METAL, 1.0F)));

    public static final DeferredBlock<Block> FILLED_CHALICE = registerBlock(
            "filled_chalice",
            () -> new Chalice(
                    decorativeProps("filled_chalice", SoundType.METAL, 1.0F)));

    public static final DeferredBlock<Block> INFINITY_EGG = registerBlock(
            "infinity_egg",
            () -> new Block(
                    decorativeProps("infinity_egg", SoundType.METAL, 3.0F)));

    public static final DeferredBlock<Block> MAGIC_MIRROR = registerBlock(
            "magic_mirror",
            () -> new MagicMirror(
                    decorativeProps("magic_mirror", SoundType.GLASS, 2.0F)));

    public static final DeferredBlock<Block> ALTAR = registerBlock(
            "altar",
            () -> new Altar(
                    decorativeProps("altar", SoundType.STONE, 3.0F)));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static DeferredBlock<Block> registerCandelabra(String name) {
        return registerBlock(name, () -> new Candelabra(
                decorativeProps(name, SoundType.METAL, 2.0F)
                        .lightLevel(state -> 15)
        ));
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, name));
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().setId(key)));
    }

    private static boolean handleMandrakeMatureBreak(Level level, @javax.annotation.Nullable Player player, BlockPos pos, BlockState state,
                                                     BlockEntity blockEntity, ItemStack tool, WitchCrop cropBlock) {
        long timeOfDay = level.getDayTime() % 24000L;
        boolean isDaytime = timeOfDay >= 0L && timeOfDay < 12000L;

        if (level.isClientSide() || !isDaytime) {
            return false;
        }

        if (player != null) {
            player.awardStat(Stats.BLOCK_MINED.get(cropBlock));
            player.causeFoodExhaustion(0.005F);
        }
        MandrakeEntity.spawnFromHarvest(level, pos, player);
        return true;
    }

    private static BlockBehaviour.Properties blockProperties(String name, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, name));
        return properties.setId(key);
    }

    private static BlockBehaviour.Properties decorativeProps(String name, SoundType sound, float strength) {
        return blockProperties(name, BlockBehaviour.Properties.of())
                .strength(strength)
                .sound(sound)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .dynamicShape()
                .noOcclusion();
    }

    public static void register(IEventBus eventBus) { BLOCKS.register(eventBus); }
}
