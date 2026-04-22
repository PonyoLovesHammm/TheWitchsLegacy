package com.ponyo.thewitchslegacy;

import com.ponyo.thewitchslegacy.block.ModBlocks;
import com.ponyo.thewitchslegacy.block.entity.ModBlockEntities;
import com.ponyo.thewitchslegacy.block.entity.client.WitchCauldronRenderer;
import com.ponyo.thewitchslegacy.client.gui.WitchOvenScreen;
import com.ponyo.thewitchslegacy.entity.ModEntities;
import com.ponyo.thewitchslegacy.entity.client.MandrakeModel;
import com.ponyo.thewitchslegacy.entity.client.MandrakeRenderer;
import com.ponyo.thewitchslegacy.menu.ModMenuTypes;
import com.ponyo.thewitchslegacy.particle.ModParticles;
import com.ponyo.thewitchslegacy.particle.client.CauldronBubbleParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = TheWitchsLegacy.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = TheWitchsLegacy.MODID, value = Dist.CLIENT)
public class TheWitchsLegacyClient {
    public TheWitchsLegacyClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        TheWitchsLegacy.LOGGER.info("HELLO FROM CLIENT SETUP");
        TheWitchsLegacy.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MANDRAKE.get(), MandrakeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WITCH_CAULDRON.get(), WitchCauldronRenderer::new);
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MandrakeModel.LAYER_LOCATION, MandrakeModel::createBodyLayer);
    }

    @SubscribeEvent
    static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.WITCH_OVEN.get(), WitchOvenScreen::new);
    }

    @SubscribeEvent
    static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.CAULDRON_BUBBLE.get(), CauldronBubbleParticle.Provider::new);
    }

    @SubscribeEvent
    static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : FoliageColor.FOLIAGE_DEFAULT,
                ModBlocks.ROWAN_LEAVES.get(),
                ModBlocks.WILLOW_CANOPY_LEAVES.get(),
                ModBlocks.WILLOW_LEAVES.get()
        );

    }
}
