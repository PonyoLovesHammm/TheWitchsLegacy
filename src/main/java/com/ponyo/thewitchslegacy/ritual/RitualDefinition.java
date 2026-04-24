package com.ponyo.thewitchslegacy.ritual;

import java.util.List;

public record RitualDefinition(String id, List<RitualRingRequirement> ringRequirements,
                               List<RitualItemRequirement> itemRequirements, int altarPowerCost,
                               RitualEffect effect, RitualRingMatcher ringMatcher) {
    public RitualDefinition(String id, List<RitualRingRequirement> ringRequirements,
                            List<RitualItemRequirement> itemRequirements, int altarPowerCost,
                            RitualEffect effect) {
        this(id, ringRequirements, itemRequirements, altarPowerCost, effect, RitualRingMatcher.allRequired(ringRequirements));
    }
}
