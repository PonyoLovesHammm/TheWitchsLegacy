package com.ponyo.thewitchslegacy.block;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.custom.Glyph;
import com.ponyo.thewitchslegacy.block.custom.SpanishMoss;
import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> HAWTHORN_LOG = registerBlock(
            "hawthorn_log",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> WILLOW_LOG = registerBlock(
            "willow_log",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.0F)
                            .sound(SoundType.WOOD)));

    //Special Blocks!
    public static final DeferredBlock<Block> SPANISH_MOSS = registerBlock(
            "spanish_moss",
            () -> new SpanishMoss(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.VINE)));

    public static final DeferredBlock<Block> GOLDEN_GLYPH = registerBlock(
            "golden_glyph",
            () -> new Glyph(
                    BlockBehaviour.Properties.of()
                            .strength(.1F)));

    public static final DeferredBlock<Block> FIERY_GLYPH = registerBlock(
            "fiery_glyph",
            () -> new Glyph(
                    BlockBehaviour.Properties.of()
                            .strength(.1F)));

    public static final DeferredBlock<Block> WHITE_GLYPH = registerBlock(
            "white_glyph",
            () -> new Glyph(
                    BlockBehaviour.Properties.of()
                            .strength(.1F)));

    public static final DeferredBlock<Block> OTHERWHERE_GLYPH = registerBlock(
            "otherwhere_glyph",
            () -> new Glyph(
                    BlockBehaviour.Properties.of()
                            .strength(.1F)));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) { BLOCKS.register(eventBus); }
}
