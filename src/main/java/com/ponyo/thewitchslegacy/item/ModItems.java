package com.ponyo.thewitchslegacy.item;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.custom.Chalk;
import com.ponyo.thewitchslegacy.item.custom.CropSeeds;
import com.ponyo.thewitchslegacy.item.custom.Mutandis;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheWitchsLegacy.MODID);

    //Default Items!
    public static final DeferredItem<Item> INFUSED_STONE = registerItem("infused_stone", Item::new);
    public static final DeferredItem<Item> INFUSED_STONE_CHARGED = registerItem("infused_stone_charged",
            properties -> new Item(properties) {
                @Override
                public boolean isFoil(ItemStack stack) {
                    return true;
                }
            });
    public static final DeferredItem<Item> SOFT_CLAY_JAR = registerItem("soft_clay_jar", Item::new);
    public static final DeferredItem<Item> CLAY_JAR = registerItem("clay_jar", Item::new);
    public static final DeferredItem<Item> WOOD_ASH = registerItem("wood_ash", Item::new);
    public static final DeferredItem<Item> BONE_NEEDLE = registerItem("bone_needle", Item::new);
    public static final DeferredItem<Item> WITCHS_CLAIM = registerItem("witchs_claim", Item::new);
    public static final DeferredItem<Item> WITCHS_CLAIM_FILLED = registerItem("witchs_claim_filled", Item::new);
    public static final DeferredItem<Item> BARK_OF_THE_ANCIENT = registerItem("bark_of_the_ancient", Item::new);
    public static final DeferredItem<Item> BREATH_OF_THE_GODDESS = registerItem("breath_of_the_goddess", Item::new);
    public static final DeferredItem<Item> ENDER_DEW = registerItem("ender_dew", Item::new);
    public static final DeferredItem<Item> ECHOES_OF_THE_LOST = registerItem("echoes_of_the_lost", Item::new);
    public static final DeferredItem<Item> GUST_OF_THE_ARID_WINDS = registerItem("gust_of_the_arid_winds", Item::new);
    public static final DeferredItem<Item> HEART_OF_THE_WILD = registerItem("heart_of_the_wild", Item::new);
    public static final DeferredItem<Item> PRICKLE_THROUGH_THE_VEIL = registerItem("prickle_through_the_veil", Item::new);
    public static final DeferredItem<Item> ROOT_OF_REMEMBRANCE = registerItem("root_of_remembrance", Item::new);
    public static final DeferredItem<Item> SCENT_OF_SERENITY = registerItem("scent_of_serenity", Item::new);
    public static final DeferredItem<Item> TOUCH_OF_REGROWTH = registerItem("touch_of_regrowth", Item::new);
    public static final DeferredItem<Item> WHIFF_OF_MAGIC = registerItem("whiff_of_magic", Item::new);

    //Plant Items!
    public static final DeferredItem<Item> BELLADONA = registerItem("belladona", Item::new);
    public static final DeferredItem<Item> BELLADONA_SEEDS = registerItem("belladona_seeds",
            properties -> new CropSeeds(ModBlocks.BELLADONA_CROP::get, properties));
    public static final DeferredItem<Item> SNOWBELL_SEEDS = registerItem("snowbell_seeds",
            properties -> new CropSeeds(ModBlocks.SNOWBELL_CROP::get, properties));
    public static final DeferredItem<Item> ICY_NEEDLE = registerItem("icy_needle", Item::new);
    public static final DeferredItem<Item> MANDRAKE_SEEDS = registerItem("mandrake_seeds",
            properties -> new CropSeeds(ModBlocks.MANDRAKE_CROP::get, properties));
    public static final DeferredItem<Item> MANDRAKE_ROOT = registerItem("mandrake_root", Item::new);
    public static final DeferredItem<Item> WATER_ARTICHOKE = registerItem("water_artichoke", Item::new);
    public static final DeferredItem<Item> WATER_ARTICHOKE_SEEDS = registerItem("water_artichoke_seeds",
            properties -> new CropSeeds(ModBlocks.WATER_ARTICHOKE_CROP::get, properties, true));

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
            properties -> new Item(properties.food(ModFoodProperties.ROWAN_BERRIES)
                    .component(
                            DataComponents.CONSUMABLE,
                            Consumables.defaultFood()
                                    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 50, 1), 0.8F))
                                    .build()
                    )));
    public static final DeferredItem<Item> GARLIC = registerItem("garlic",
            properties -> new BlockItem(ModBlocks.GARLIC_CROP.get(), properties.food(ModFoodProperties.GARLIC)
                    .component(
                            DataComponents.CONSUMABLE,
                            Consumables.defaultFood()
                                    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 1), 1.0F))
                                    .build()
                    )
                    .useItemDescriptionPrefix()));

    private static <T extends Item> DeferredItem<T> registerItem(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, name));
        return ITEMS.register(name, () -> factory.apply(new Item.Properties().setId(key)));
    }

    public static void register(IEventBus eventBus) { ITEMS.register(eventBus); }
}
