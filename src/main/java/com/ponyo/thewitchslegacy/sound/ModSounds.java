package com.ponyo.thewitchslegacy.sound;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, TheWitchsLegacy.MODID);

    //This will be a different setup for custom block sounds (a little more complicated)
    public static final Supplier<SoundEvent> CHALK_DRAW = registerSoundEvent("chalk_draw");


    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(TheWitchsLegacy.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) { SOUND_EVENTS.register(eventBus); }
}
