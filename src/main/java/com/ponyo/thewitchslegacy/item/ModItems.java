package com.ponyo.thewitchslegacy.item;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.item.custom.Chalk;
import com.ponyo.thewitchslegacy.item.custom.Mutandis;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheWitchsLegacy.MODID);

    //Default Items!
    public static final DeferredItem<Item> INFUSED_STONE = ITEMS.register("infused_stone",
            () -> new Item(new Item.Properties()));

    //Special Items
    public static final DeferredItem<Item> WHITE_CHALK = ITEMS.register("white_chalk",
            () -> new Chalk(new Item.Properties()));
    public static final DeferredItem<Item> GOLDEN_CHALK = ITEMS.register("golden_chalk",
            () -> new Chalk(new Item.Properties()));
    public static final DeferredItem<Item> FIERY_CHALK = ITEMS.register("fiery_chalk",
            () -> new Chalk(new Item.Properties()));
    public static final DeferredItem<Item> OTHERWHERE_CHALK = ITEMS.register("otherwhere_chalk",
            () -> new Chalk(new Item.Properties()));
    public static final DeferredItem<Item> MUTANDIS = ITEMS.register("mutandis",
            () -> new Mutandis(new Item.Properties()));
    public static final DeferredItem<Item> MUTANDIS_EXTREMIS = ITEMS.register("mutandis_extremis",
            () -> new Mutandis(new Item.Properties()));

    //Food Items!
    public static final DeferredItem<Item> ROWAN_BERRY_PIE = ITEMS.register("rowan_berry_pie",
            () -> new Item(new Item.Properties().food(ModFoodProperties.ROWAN_BERRY_PIE)));
    public static final DeferredItem<Item> GARLIC = ITEMS.register("garlic",
            () -> new Item(new Item.Properties().food(ModFoodProperties.GARLIC)));
    public static final DeferredItem<Item> ROWAN_BERRIES = ITEMS.register("rowan_berries",
            () -> new Item(new Item.Properties().food(ModFoodProperties.ROWAN_BERRIES)));


    public static void register(IEventBus eventBus) { ITEMS.register(eventBus); }
}
