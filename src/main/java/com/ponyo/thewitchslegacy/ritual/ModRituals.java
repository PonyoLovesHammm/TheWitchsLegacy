package com.ponyo.thewitchslegacy.ritual;

import com.ponyo.thewitchslegacy.ritual.definitions.RiteOfCharging;
import com.ponyo.thewitchslegacy.ritual.definitions.RiteOfBinding;

import java.util.List;

public final class ModRituals {
    public static final RitualDefinition RITE_OF_BINDING = RiteOfBinding.create();
    public static final RitualDefinition CHARGED_INFUSED_STONE = RiteOfCharging.create();

    public static final List<RitualDefinition> ALL = List.of(RITE_OF_BINDING, CHARGED_INFUSED_STONE);

    private ModRituals() {
    }
}
