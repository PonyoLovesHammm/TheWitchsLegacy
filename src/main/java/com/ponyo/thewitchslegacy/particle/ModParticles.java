package com.ponyo.thewitchslegacy.particle;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, TheWitchsLegacy.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CAULDRON_BUBBLE =
            PARTICLE_TYPES.register("cauldron_bubble", () -> new SimpleParticleType(false));

    private ModParticles() {
    }

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
