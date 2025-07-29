package com.ponyo.thewitchslegacy.item;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TheWitchsLegacy.MODID);

    public static final DeferredItem<Item> WHITE_CHALK = ITEMS.register("white_chalk",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
