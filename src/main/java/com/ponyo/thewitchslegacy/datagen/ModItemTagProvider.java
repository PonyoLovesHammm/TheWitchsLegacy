/*
package com.ponyo.thewitchslegacy.datagen;

import com.ponyo.thewitchslegacy.ModTags;
import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, TheWitchsLegacy.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModTags.Items.TESTING_ITEMS)
                .add(ModItems.ROWAN_BERRIES.get())
                .add(Items.STICK)
                .add(ModItems.GARLIC.get());

    }
}


 */