package com.ponyo.thewitchslegacy.ritual;

import com.ponyo.thewitchslegacy.ritual.definitions.general.FamiliarBinding;
import com.ponyo.thewitchslegacy.ritual.definitions.general.GlyphicTransformation;
import com.ponyo.thewitchslegacy.ritual.definitions.general.RiteOfCharging;
import com.ponyo.thewitchslegacy.ritual.definitions.general.RitesOfBinding;
import com.ponyo.thewitchslegacy.ritual.definitions.transposition.WaystoneTransposition;

import java.util.ArrayList;
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
    private static final List<RitualDefinition> GLYPHIC_TRANSFORMATIONS = GlyphicTransformation.createAll();

    public static final List<RitualDefinition> ALL = createAll();

    private ModRituals() {
    }

    private static List<RitualDefinition> createAll() {
        List<RitualDefinition> rituals = new ArrayList<>();
        rituals.add(TALISMAN_BINDING_1);
        rituals.add(TALISMAN_BINDING_2);
        rituals.add(WAYSTONE_BINDING_1);
        rituals.add(WAYSTONE_BINDING_2);
        rituals.add(WAYSTONE_BINDING_3);
        rituals.add(WAYSTONE_BINDING_4);
        rituals.add(WAYSTONE_TRANSPOSITION);
        rituals.addAll(GLYPHIC_TRANSFORMATIONS);
        rituals.add(BLOODED_WAYSTONE_1);
        rituals.add(BLOODED_WAYSTONE_2);
        rituals.add(FAMILIAR_BINDING);
        rituals.add(RITE_OF_CHARGING);
        return List.copyOf(rituals);
    }
}
