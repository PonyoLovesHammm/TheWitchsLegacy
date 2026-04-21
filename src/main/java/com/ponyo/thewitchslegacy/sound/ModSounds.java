package com.ponyo.thewitchslegacy.sound;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, TheWitchsLegacy.MODID);

    public static final String CHALK_DRAW_ID = "chalk_draw";
    public static final String MANDRAKE_SCREAM_1_ID = "mandrake_scream_1";
    public static final String MANDRAKE_SCREAM_2_ID = "mandrake_scream_2";
    public static final String MANDRAKE_SCREAM_3_ID = "mandrake_scream_3";
    public static final String MANDRAKE_SCREAM_ON_PLANT_BREAK_ID = "mandrake_scream_on_plant_break";

    public static final Supplier<SoundEvent> CHALK_DRAW = registerSoundEvent(CHALK_DRAW_ID);
    public static final Supplier<SoundEvent> MANDRAKE_SCREAM_1 = registerSoundEvent(MANDRAKE_SCREAM_1_ID);
    public static final Supplier<SoundEvent> MANDRAKE_SCREAM_2 = registerSoundEvent(MANDRAKE_SCREAM_2_ID);
    public static final Supplier<SoundEvent> MANDRAKE_SCREAM_3 = registerSoundEvent(MANDRAKE_SCREAM_3_ID);
    public static final Supplier<SoundEvent> MANDRAKE_SCREAM_ON_PLANT_BREAK = registerSoundEvent(MANDRAKE_SCREAM_ON_PLANT_BREAK_ID);

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(TheWitchsLegacy.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
