package com.ponyo.thewitchslegacy.ritual;

import com.ponyo.thewitchslegacy.ritual.definitions.rites_of_binding.RiteOfBinding;
import com.ponyo.thewitchslegacy.ritual.definitions.RiteOfCharging;
import com.ponyo.thewitchslegacy.ritual.definitions.rites_of_binding.FamiliarBinding;

import java.util.List;

public final class ModRituals {
    public static final RitualDefinition TALISMAN_BINDING_1 = RiteOfBinding.createTalismanBinding1();
    public static final RitualDefinition TALISMAN_BINDING_2 = RiteOfBinding.createTalismanBinding2();
    public static final RitualDefinition WAYSTONE_BINDING_1 = RiteOfBinding.createWaystoneBinding1();
    public static final RitualDefinition WAYSTONE_BINDING_2 = RiteOfBinding.createWaystoneBinding2();
    public static final RitualDefinition BLOODED_WAYSTONE_1 = RiteOfBinding.createBloodedWaystone1();
    public static final RitualDefinition BLOODED_WAYSTONE_2 = RiteOfBinding.createBloodedWaystone2();
    public static final RitualDefinition FAMILIAR_BINDING = FamiliarBinding.create();
    public static final RitualDefinition RITE_OF_CHARGING = RiteOfCharging.create();

    public static final List<RitualDefinition> ALL = List.of(
            TALISMAN_BINDING_1,
            TALISMAN_BINDING_2,
            WAYSTONE_BINDING_1,
            WAYSTONE_BINDING_2,
            BLOODED_WAYSTONE_1,
            BLOODED_WAYSTONE_2,
            FAMILIAR_BINDING,
            RITE_OF_CHARGING
    );

    private ModRituals() {
    }
}
