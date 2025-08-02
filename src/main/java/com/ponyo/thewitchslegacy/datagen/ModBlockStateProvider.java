/*
package com.ponyo.thewitchslegacy.datagen;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, TheWitchsLegacy.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWitchItem(ModBlocks.ROWAN_LOG);
        //blockWitchItem(ModBlocks.WHITE_GLYPH);
        //blockWitchItem(ModBlocks.OTHERWHERE_GLYPH);
        //blockWitchItem(ModBlocks.FIERY_GLYPH);
        //blockWitchItem(ModBlocks.GOLDEN_GLYPH);
        blockWitchItem(ModBlocks.SPANISH_MOSS);
    }

    private void blockWitchItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }
}


 */