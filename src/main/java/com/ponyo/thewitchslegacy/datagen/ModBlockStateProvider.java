/*
package com.ponyo.thewitchslegacy.datagen;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TheWitchsLegacy.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //blockWitchItem(ModBlocks.WHITE_GLYPH);
        //blockWitchItem(ModBlocks.OTHERWHERE_GLYPH);
        //blockWitchItem(ModBlocks.FIERY_GLYPH);
        //blockWitchItem(ModBlocks.GOLDEN_GLYPH);
        //blockWitchItem(ModBlocks.SPANISH_MOSS);
        stairsBlock(ModBlocks.WILLOW_STAIRS.get(), blockTexture(ModBlocks.WILLOW_STAIRS.get()));

        blockItem(ModBlocks.WILLOW_STAIRS);
        blockItem(ModBlocks.WILLOW_SLAB);
        blockItem(ModBlocks.WILLOW_PRESSURE_PLATE);
        blockItem(ModBlocks.WILLOW_FENCE_GATE);
        blockItem(ModBlocks.WILLOW_TRAPDOOR);
    }

    private void blockWitchItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("thewitchslegacy:block/" + deferredBlock.getId().getPath()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("thewitchslegacy:block/" + deferredBlock.getId().getPath() + appendix));
    }
}

 */