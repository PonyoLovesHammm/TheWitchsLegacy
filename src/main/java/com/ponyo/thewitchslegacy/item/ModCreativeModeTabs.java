package com.ponyo.thewitchslegacy.item;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TheWitchsLegacy.MODID);

    public static final Supplier<CreativeModeTab> THE_WITCHS_LEGACY_TAB = CREATIVE_MODE_TAB.register("thewitchslegacy_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.WHITE_CHALK.get()))
                    .title(Component.translatable("creativetab.thewitchslegacy.thewitchslegacy_items"))
                    .displayItems((parameters, output) -> {
                        //General Items
                        output.accept(ModItems.INFUSED_STONE);
                        output.accept(ModItems.SOFT_CLAY_JAR);
                        output.accept(ModItems.CLAY_JAR);
                        output.accept(ModItems.WOOD_ASH);
                        output.accept(ModItems.BONE_NEEDLE);
                        output.accept(ModItems.WITCHS_CLAIM);
                        output.accept(ModItems.WITCHS_CLAIM_FILLED);
                        //Special Items
                        output.accept(ModItems.WHITE_CHALK);
                        output.accept(ModItems.GOLDEN_CHALK);
                        output.accept(ModItems.FIERY_CHALK);
                        output.accept(ModItems.OTHERWHERE_CHALK);
                        output.accept(ModItems.MUTANDIS);
                        output.accept(ModItems.MUTANDIS_EXTREMIS);
                        //Food Items
                        output.accept(ModItems.ROWAN_BERRY_PIE);
                        output.accept(ModItems.GARLIC);
                        output.accept(ModItems.ROWAN_BERRIES);
                        //General Blocks
                        output.accept(ModBlocks.ROWAN_LOG);
                        output.accept(ModBlocks.ROWAN_PLANKS);
                        output.accept(ModBlocks.ROWAN_STAIRS);
                        output.accept(ModBlocks.ROWAN_SLAB);
                        output.accept(ModBlocks.ROWAN_PRESSURE_PLATE);
                        output.accept(ModBlocks.ROWAN_BUTTON);
                        output.accept(ModBlocks.ROWAN_FENCE);
                        output.accept(ModBlocks.ROWAN_FENCE_GATE);
                        output.accept(ModBlocks.ROWAN_DOOR);
                        output.accept(ModBlocks.ROWAN_TRAPDOOR);
                        output.accept(ModBlocks.HAWTHORN_LOG);
                        output.accept(ModBlocks.HAWTHORN_PLANKS);
                        output.accept(ModBlocks.HAWTHORN_STAIRS);
                        output.accept(ModBlocks.HAWTHORN_SLAB);
                        output.accept(ModBlocks.HAWTHORN_PRESSURE_PLATE);
                        output.accept(ModBlocks.HAWTHORN_BUTTON);
                        output.accept(ModBlocks.HAWTHORN_FENCE);
                        output.accept(ModBlocks.HAWTHORN_FENCE_GATE);
                        output.accept(ModBlocks.HAWTHORN_DOOR);
                        output.accept(ModBlocks.HAWTHORN_TRAPDOOR);
                        output.accept(ModBlocks.WILLOW_LOG);
                        output.accept(ModBlocks.WILLOW_PLANKS);
                        output.accept(ModBlocks.WILLOW_STAIRS);
                        output.accept(ModBlocks.WILLOW_SLAB);
                        output.accept(ModBlocks.WILLOW_PRESSURE_PLATE);
                        output.accept(ModBlocks.WILLOW_BUTTON);
                        output.accept(ModBlocks.WILLOW_FENCE);
                        output.accept(ModBlocks.WILLOW_FENCE_GATE);
                        output.accept(ModBlocks.WILLOW_DOOR);
                        output.accept(ModBlocks.WILLOW_TRAPDOOR);
                        //Special Blocks
                        output.accept(ModBlocks.SPANISH_MOSS);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
