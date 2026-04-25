package com.ponyo.thewitchslegacy.ritual;

import com.ponyo.thewitchslegacy.ritual.definitions.general.FamiliarBinding;
import com.ponyo.thewitchslegacy.ritual.definitions.general.RiteOfCharging;
import com.ponyo.thewitchslegacy.ritual.definitions.general.RitesOfBinding;
import com.ponyo.thewitchslegacy.ritual.definitions.transposition.WaystoneTransposition;

import java.util.List;

public final class ModRituals {
    public static final RitualDefinition TALISMAN_BINDING_1 = RitesOfBinding.createTalismanBinding1();
    public static final RitualDefinition TALISMAN_BINDING_2 = RitesOfBinding.createTalismanBinding2();
    public static final RitualDefinition WAYSTONE_BINDING_1 = RitesOfBinding.createWaystoneBinding1();
    public static final RitualDefinition WAYSTONE_BINDING_2 = RitesOfBinding.createWaystoneBinding2();
    public static final RitualDefinition WAYSTONE_BINDING_3 = RitesOfBinding.createWaystoneBinding3();
    public static final RitualDefinition WAYSTONE_BINDING_4 = RitesOfBinding.createWaystoneBinding4();
    public static final RitualDefinition WAYSTONE_TRANSPOSITION = WaystoneTransposition.create();
    public static final RitualDefinition BLOODED_WAYSTONE_1 = RitesOfBinding.createBloodedWaystone1();
    public static final RitualDefinition BLOODED_WAYSTONE_2 = RitesOfBinding.createBloodedWaystone2();
    public static final RitualDefinition FAMILIAR_BINDING = FamiliarBinding.create();
    public static final RitualDefinition RITE_OF_CHARGING = RiteOfCharging.create();

    public static final List<RitualDefinition> ALL = List.of(
            TALISMAN_BINDING_1,
            TALISMAN_BINDING_2,
            WAYSTONE_BINDING_1,
            WAYSTONE_BINDING_2,
            WAYSTONE_BINDING_3,
            WAYSTONE_BINDING_4,
            WAYSTONE_TRANSPOSITION,
            BLOODED_WAYSTONE_1,
            BLOODED_WAYSTONE_2,
            FAMILIAR_BINDING,
            RITE_OF_CHARGING
    );

    private ModRituals() {
    }
}
