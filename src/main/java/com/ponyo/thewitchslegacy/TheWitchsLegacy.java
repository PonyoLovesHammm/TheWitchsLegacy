package com.ponyo.thewitchslegacy;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.entity.ModBlockEntities;
import com.ponyo.thewitchslegacy.block.custom.PoppetShelf;
import com.ponyo.thewitchslegacy.entity.ModEntities;
import com.ponyo.thewitchslegacy.familiar.FamiliarManager;
import com.ponyo.thewitchslegacy.item.ModCreativeModeTabs;
import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.Poppet;
import com.ponyo.thewitchslegacy.item.custom.WitchsClaimSavedData;
import com.ponyo.thewitchslegacy.menu.ModMenuTypes;
import com.ponyo.thewitchslegacy.particle.ModParticles;
import com.ponyo.thewitchslegacy.recipe.ModRecipeSerializers;
import com.ponyo.thewitchslegacy.ritual.RitualManager;
import com.ponyo.thewitchslegacy.sound.ModSounds;
import com.ponyo.thewitchslegacy.worldgen.tree.ModTreeGrowers;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TheWitchsLegacy.MODID)
public class TheWitchsLegacy {
    public static final String MODID = "thewitchslegacy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public TheWitchsLegacy(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (TheWitchsLegacy) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(RitualManager::onServerTick);
        NeoForge.EVENT_BUS.addListener(FamiliarManager::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(WitchsClaimSavedData::onPlayerSetSpawn);
        NeoForge.EVENT_BUS.addListener(Poppet::onItemCrafted);
        NeoForge.EVENT_BUS.addListener(Poppet::onLivingDamagePre);
        NeoForge.EVENT_BUS.addListener(Poppet::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(Poppet::onArmorHurt);
        NeoForge.EVENT_BUS.addListener(Poppet::onPlayerDestroyItem);
        NeoForge.EVENT_BUS.addListener(Poppet::onPlayerTickPre);
        NeoForge.EVENT_BUS.addListener(Poppet::onPlayerTickPost);
        NeoForge.EVENT_BUS.addListener(PoppetShelf::onRightClickBlock);

        ModCreativeModeTabs.register(modEventBus);

        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModParticles.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);

        ModSounds.register(modEventBus);
        ModTreeGrowers.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}
