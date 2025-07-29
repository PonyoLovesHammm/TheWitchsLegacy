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
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.WHITE_CHALK.get()))
                    .title(Component.translatable("creativetab.thewitchslegacy.thewitchslegacy_items"))
                    .displayItems((parameters, output) -> {
                      output.accept(ModItems.WHITE_CHALK);
                      output.accept(ModBlocks.ROWAN_LOG);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
