package com.ponyo.thewitchslegacy.ritual;

import com.ponyo.thewitchslegacy.ritual.definitions.general.FamiliarBinding;
import com.ponyo.thewitchslegacy.ritual.definitions.general.GlyphicTransformation;
import com.ponyo.thewitchslegacy.ritual.definitions.general.RiteOfCharging;
import com.ponyo.thewitchslegacy.ritual.definitions.general.RitesOfBinding;
import com.ponyo.thewitchslegacy.ritual.definitions.summoning.DemonSummon;
import com.ponyo.thewitchslegacy.ritual.definitions.summoning.ImpSummon;
import com.ponyo.thewitchslegacy.ritual.definitions.summoning.WitherSummon;
import com.ponyo.thewitchslegacy.ritual.definitions.summoning.WitchSummon;
import com.ponyo.thewitchslegacy.ritual.definitions.barrier.RiteOfImprisonment;
import com.ponyo.thewitchslegacy.ritual.definitions.barrier.RiteOfProtection;
import com.ponyo.thewitchslegacy.ritual.definitions.barrier.RiteOfSanctity;
import com.ponyo.thewitchslegacy.ritual.definitions.transposition.CreatureTeleportation;
import com.ponyo.thewitchslegacy.ritual.definitions.transposition.IronTransposition;
import com.ponyo.thewitchslegacy.ritual.definitions.transposition.RiteOfBeastialCall;
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
    public static final RitualDefinition CREATURE_TELEPORTATION = CreatureTeleportation.create();
    public static final RitualDefinition IRON_TRANSPOSITION = IronTransposition.create();
    public static final RitualDefinition RITE_OF_BEASTIAL_CALL = RiteOfBeastialCall.create();
    public static final RitualDefinition BLOODED_WAYSTONE_1 = RitesOfBinding.createBloodedWaystone1();
    public static final RitualDefinition BLOODED_WAYSTONE_2 = RitesOfBinding.createBloodedWaystone2();
    public static final RitualDefinition FAMILIAR_BINDING = FamiliarBinding.create();
    public static final RitualDefinition RITE_OF_CHARGING = RiteOfCharging.create();
    public static final RitualDefinition DEMON_SUMMON_1 = DemonSummon.create();
    public static final RitualDefinition DEMON_SUMMON_2 = DemonSummon.createInfusedStone();
    public static final RitualDefinition IMP_SUMMON = ImpSummon.create();
    public static final RitualDefinition WITHER_SUMMON_1 = WitherSummon.createSacrifice();
    public static final RitualDefinition WITHER_SUMMON_2 = WitherSummon.createInfusedStone();
    public static final RitualDefinition WITCH_SUMMON = WitchSummon.create();
    private static final List<RitualDefinition> RITE_OF_IMPRISONMENT = RiteOfImprisonment.createAll();
    private static final List<RitualDefinition> RITE_OF_PROTECTION = RiteOfProtection.createAll();
    private static final List<RitualDefinition> RITE_OF_SANCTITY = RiteOfSanctity.createAll();
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
        rituals.add(CREATURE_TELEPORTATION);
        rituals.add(IRON_TRANSPOSITION);
        rituals.add(RITE_OF_BEASTIAL_CALL);
        rituals.addAll(GLYPHIC_TRANSFORMATIONS);
        rituals.add(BLOODED_WAYSTONE_1);
        rituals.add(BLOODED_WAYSTONE_2);
        rituals.add(FAMILIAR_BINDING);
        rituals.add(RITE_OF_CHARGING);
        rituals.add(DEMON_SUMMON_1);
        rituals.add(DEMON_SUMMON_2);
        rituals.add(IMP_SUMMON);
        rituals.add(WITHER_SUMMON_1);
        rituals.add(WITHER_SUMMON_2);
        rituals.add(WITCH_SUMMON);
        rituals.addAll(RITE_OF_IMPRISONMENT);
        rituals.addAll(RITE_OF_PROTECTION);
        rituals.addAll(RITE_OF_SANCTITY);
        return List.copyOf(rituals);
    }
}
