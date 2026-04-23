package com.ponyo.thewitchslegacy.ritual;

import com.ponyo.thewitchslegacy.ritual.definitions.RiteOfCharging;

import java.util.List;

public final class ModRituals {
    public static final RitualDefinition CHARGED_INFUSED_STONE = RiteOfCharging.create();

    public static final List<RitualDefinition> ALL = List.of(CHARGED_INFUSED_STONE);

    private ModRituals() {
    }
}
