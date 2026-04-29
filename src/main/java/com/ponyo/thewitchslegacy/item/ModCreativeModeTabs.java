package com.ponyo.thewitchslegacy.item;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.item.custom.CircleTalisman;
import com.ponyo.thewitchslegacy.ritual.RitualRingSize;
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
                        output.accept(ModItems.INFUSED_STONE_CHARGED);
                        output.accept(ModItems.SOFT_CLAY_JAR);
                        output.accept(ModItems.CLAY_JAR);
                        output.accept(ModItems.WOOD_ASH);
                        output.accept(ModItems.QUICKLIME);
                        output.accept(ModItems.SELENITE_SHARD);
                        output.accept(ModItems.REFINED_EVIL);
                        output.accept(ModItems.DEMON_HEART);
                        output.accept(ModItems.BONE_NEEDLE);
                        output.accept(ModItems.POPPET);
                        output.accept(ModItems.VOODOO_POPPET);
                        output.accept(ModItems.VOODOO_PROTECTION_POPPET);
                        output.accept(ModItems.ARMOR_PROTECTION_POPPET);
                        output.accept(ModItems.DEATH_PROTECTION_POPPET);
                        output.accept(ModItems.EARTH_PROTECTION_POPPET);
                        output.accept(ModItems.FIRE_PROTECTION_POPPET);
                        output.accept(ModItems.HUNGER_PROTECTION_POPPET);
                        output.accept(ModItems.TOOL_PROTECTION_POPPET);
                        output.accept(ModItems.WATER_PROTECTION_POPPET);
                        output.accept(ModItems.VAMPIRIC_POPPET);
                        output.accept(ModItems.WAYSTONE);
                        output.accept(ModItems.WITCHS_CLAIM);
                        output.accept(ModItems.BARK_OF_THE_ANCIENT);
                        output.accept(ModItems.BREATH_OF_THE_GODDESS);
                        output.accept(ModItems.TEAR_OF_THE_GODDESS);
                        output.accept(ModItems.DIAMOND_VAPOR);
                        output.accept(ModItems.ODOR_OF_PURITY);
                        output.accept(ModItems.REEK_OF_MISFORTUNE);
                        output.accept(ModItems.DROP_OF_LUCK);
                        output.accept(ModItems.DEMONIC_BLOOD);
                        output.accept(ModItems.ENDER_DEW);
                        output.accept(ModItems.OIL_OF_VITRIOL);
                        output.accept(ModItems.ECHOES_OF_THE_LOST);
                        output.accept(ModItems.GUST_OF_THE_ARID_WINDS);
                        output.accept(ModItems.HEART_OF_THE_WILD);
                        output.accept(ModItems.PRICKLE_THROUGH_THE_VEIL);
                        output.accept(ModItems.ROOT_OF_REMEMBRANCE);
                        output.accept(ModItems.SCENT_OF_SERENITY);
                        output.accept(ModItems.TOUCH_OF_REGROWTH);
                        output.accept(ModItems.WHIFF_OF_MAGIC);
                        //Plant Items
                        output.accept(ModItems.BELLADONA);
                        output.accept(ModItems.BELLADONA_SEEDS);
                        output.accept(ModItems.SNOWBELL_SEEDS);
                        output.accept(ModItems.ICY_NEEDLE);
                        output.accept(ModItems.MANDRAKE_SEEDS);
                        output.accept(ModItems.MANDRAKE_ROOT);
                        output.accept(ModItems.WATER_ARTICHOKE);
                        output.accept(ModItems.WATER_ARTICHOKE_SEEDS);
                        output.accept(ModItems.GARLIC);
                        output.accept(ModBlocks.SPANISH_MOSS);
                        output.accept(ModBlocks.GLINT_WEED);
                        output.accept(ModBlocks.EMBER_MOSS);
                        //Special Items
                        output.accept(ModItems.WHITE_CHALK);
                        output.accept(ModItems.GOLDEN_CHALK);
                        output.accept(ModItems.FIERY_CHALK);
                        output.accept(ModItems.OTHERWHERE_CHALK);
                        output.accept(ModItems.CIRCLE_TALISMAN);
                        output.accept(CircleTalisman.createWithSingleRing(ModItems.CIRCLE_TALISMAN.get(),
                                RitualRingSize.SMALL, CircleTalisman.RingColor.WHITE));
                        output.accept(CircleTalisman.createWithSingleRing(ModItems.CIRCLE_TALISMAN.get(),
                                RitualRingSize.SMALL, CircleTalisman.RingColor.FIERY));
                        output.accept(CircleTalisman.createWithSingleRing(ModItems.CIRCLE_TALISMAN.get(),
                                RitualRingSize.SMALL, CircleTalisman.RingColor.OTHERWHERE));
                        output.accept(CircleTalisman.createWithSingleRing(ModItems.CIRCLE_TALISMAN.get(),
                                RitualRingSize.MEDIUM, CircleTalisman.RingColor.WHITE));
                        output.accept(CircleTalisman.createWithSingleRing(ModItems.CIRCLE_TALISMAN.get(),
                                RitualRingSize.MEDIUM, CircleTalisman.RingColor.FIERY));
                        output.accept(CircleTalisman.createWithSingleRing(ModItems.CIRCLE_TALISMAN.get(),
                                RitualRingSize.MEDIUM, CircleTalisman.RingColor.OTHERWHERE));
                        output.accept(CircleTalisman.createWithSingleRing(ModItems.CIRCLE_TALISMAN.get(),
                                RitualRingSize.LARGE, CircleTalisman.RingColor.WHITE));
                        output.accept(CircleTalisman.createWithSingleRing(ModItems.CIRCLE_TALISMAN.get(),
                                RitualRingSize.LARGE, CircleTalisman.RingColor.FIERY));
                        output.accept(CircleTalisman.createWithSingleRing(ModItems.CIRCLE_TALISMAN.get(),
                                RitualRingSize.LARGE, CircleTalisman.RingColor.OTHERWHERE));
                        output.accept(ModItems.MUTANDIS);
                        output.accept(ModItems.MUTANDIS_EXTREMIS);
                        //Food Items
                        output.accept(ModItems.ROWAN_BERRY_PIE);
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
                        output.accept(ModBlocks.ROWAN_SAPLING);
                        output.accept(ModBlocks.ROWAN_LEAVES);
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
                        output.accept(ModBlocks.HAWTHORN_SAPLING);
                        output.accept(ModBlocks.HAWTHORN_LEAVES);
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
                        output.accept(ModBlocks.WILLOW_SAPLING);
                        output.accept(ModBlocks.WILLOW_LEAVES);
                        output.accept(ModBlocks.WILLOW_CANOPY_LEAVES);
                        //Special Blocks
                        output.accept(ModBlocks.CANDELABRA);
                        output.accept(ModBlocks.WHITE_CANDELABRA);
                        output.accept(ModBlocks.LIGHT_GRAY_CANDELABRA);
                        output.accept(ModBlocks.GRAY_CANDELABRA);
                        output.accept(ModBlocks.BLACK_CANDELABRA);
                        output.accept(ModBlocks.BROWN_CANDELABRA);
                        output.accept(ModBlocks.RED_CANDELABRA);
                        output.accept(ModBlocks.ORANGE_CANDELABRA);
                        output.accept(ModBlocks.YELLOW_CANDELABRA);
                        output.accept(ModBlocks.LIME_CANDELABRA);
                        output.accept(ModBlocks.GREEN_CANDELABRA);
                        output.accept(ModBlocks.CYAN_CANDELABRA);
                        output.accept(ModBlocks.LIGHT_BLUE_CANDELABRA);
                        output.accept(ModBlocks.BLUE_CANDELABRA);
                        output.accept(ModBlocks.PURPLE_CANDELABRA);
                        output.accept(ModBlocks.MAGENTA_CANDELABRA);
                        output.accept(ModBlocks.PINK_CANDELABRA);
                        output.accept(ModBlocks.WITCH_CAULDRON);
                        output.accept(ModBlocks.WITCH_OVEN);
                        output.accept(ModBlocks.DISTILLERY);
                        output.accept(ModBlocks.SPINNING_WHEEL);
                        output.accept(ModBlocks.POPPET_SHELF);
                        output.accept(ModBlocks.KETTLE);
                        output.accept(ModBlocks.CHALICE);
                        output.accept(ModBlocks.FILLED_CHALICE);
                        output.accept(ModBlocks.INFINITY_EGG);
                        output.accept(ModBlocks.MAGIC_MIRROR);
                        output.accept(ModBlocks.ALTAR);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
