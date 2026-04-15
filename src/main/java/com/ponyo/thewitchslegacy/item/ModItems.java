package com.ponyo.thewitchslegacy.item;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.item.custom.Chalk;
import com.ponyo.thewitchslegacy.item.custom.Mutandis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheWitchsLegacy.MODID);

    //Default Items!
    public static final DeferredItem<Item> INFUSED_STONE = registerItem("infused_stone", Item::new);
    public static final DeferredItem<Item> SOFT_CLAY_JAR = registerItem("soft_clay_jar", Item::new);
    public static final DeferredItem<Item> CLAY_JAR = registerItem("clay_jar", Item::new);
    public static final DeferredItem<Item> WOOD_ASH = registerItem("wood_ash", Item::new);
    public static final DeferredItem<Item> BONE_NEEDLE = registerItem("bone_needle", Item::new);
    public static final DeferredItem<Item> WITCHS_CLAIM = registerItem("witchs_claim", Item::new);
    public static final DeferredItem<Item> WITCHS_CLAIM_FILLED = registerItem("witchs_claim_filled", Item::new);

    //Special Items
    public static final DeferredItem<Item> WHITE_CHALK = registerItem("white_chalk", Chalk::new);
    public static final DeferredItem<Item> GOLDEN_CHALK = registerItem("golden_chalk", Chalk::new);
    public static final DeferredItem<Item> FIERY_CHALK = registerItem("fiery_chalk", Chalk::new);
    public static final DeferredItem<Item> OTHERWHERE_CHALK = registerItem("otherwhere_chalk", Chalk::new);
    public static final DeferredItem<Item> MUTANDIS = registerItem("mutandis", Mutandis::new);
    public static final DeferredItem<Item> MUTANDIS_EXTREMIS = registerItem("mutandis_extremis", Mutandis::new);

    //Food Items!
    public static final DeferredItem<Item> ROWAN_BERRY_PIE = registerItem("rowan_berry_pie",
            properties -> new Item(properties.food(ModFoodProperties.ROWAN_BERRY_PIE)));
    public static final DeferredItem<Item> ROWAN_BERRIES = registerItem("rowan_berries",
            properties -> new Item(properties.food(ModFoodProperties.ROWAN_BERRIES)));
    public static final DeferredItem<Item> GARLIC = registerItem("garlic",
            properties -> new Item(properties.food(ModFoodProperties.GARLIC)));

    private static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, name));
        return ITEMS.register(name, () -> factory.apply(new Item.Properties().setId(key)));
    }

    public static void register(IEventBus eventBus) { ITEMS.register(eventBus); }
}
