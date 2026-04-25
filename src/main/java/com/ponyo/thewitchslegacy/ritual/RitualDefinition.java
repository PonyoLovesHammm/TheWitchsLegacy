package com.ponyo.thewitchslegacy.ritual;

import net.minecraft.network.chat.Component;

import java.util.List;

public record RitualDefinition(String id, String displayNameKey, List<RitualRingRequirement> ringRequirements,
                               List<RitualItemRequirement> itemRequirements, int altarPowerCost,
                               RitualEffect effect, RitualRingMatcher ringMatcher, RitualStartValidator startValidator) {
    public RitualDefinition(String id, List<RitualRingRequirement> ringRequirements,
                            List<RitualItemRequirement> itemRequirements, int altarPowerCost,
                            RitualEffect effect) {
        this(id, "ritual.thewitchslegacy." + id, ringRequirements, itemRequirements, altarPowerCost, effect,
                RitualRingMatcher.allRequired(ringRequirements), RitualStartValidator.ALWAYS_ALLOW);
    }

    public RitualDefinition(String id, String displayNameKey, List<RitualRingRequirement> ringRequirements,
                            List<RitualItemRequirement> itemRequirements, int altarPowerCost,
                            RitualEffect effect) {
        this(id, displayNameKey, ringRequirements, itemRequirements, altarPowerCost, effect,
                RitualRingMatcher.allRequired(ringRequirements), RitualStartValidator.ALWAYS_ALLOW);
    }

    public RitualDefinition(String id, String displayNameKey, List<RitualRingRequirement> ringRequirements,
                            List<RitualItemRequirement> itemRequirements, int altarPowerCost,
                            RitualEffect effect, RitualRingMatcher ringMatcher) {
        this(id, displayNameKey, ringRequirements, itemRequirements, altarPowerCost, effect,
                ringMatcher, RitualStartValidator.ALWAYS_ALLOW);
    }

    public Component displayName() {
        return Component.translatable(displayNameKey);
    }
}
