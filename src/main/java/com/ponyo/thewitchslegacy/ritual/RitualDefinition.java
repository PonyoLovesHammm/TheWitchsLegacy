package com.ponyo.thewitchslegacy.ritual;

import java.util.List;

public record RitualDefinition(String id, List<RitualRingRequirement> ringRequirements,
                               List<RitualItemRequirement> itemRequirements, int altarPowerCost,
                               RitualEffect effect) {
}
