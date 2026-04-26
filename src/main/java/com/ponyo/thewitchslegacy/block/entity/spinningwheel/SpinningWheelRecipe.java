package com.ponyo.thewitchslegacy.block.entity.spinningwheel;

import net.minecraft.resources.Identifier;

public record SpinningWheelRecipe(Identifier id, int craftTimeTicks, int altarPowerPerSecond) {
    public SpinningWheelRecipe {
        if (craftTimeTicks <= 0) {
            throw new IllegalArgumentException("craftTimeTicks must be positive");
        }
        if (altarPowerPerSecond < 0) {
            throw new IllegalArgumentException("altarPowerPerSecond must be non-negative");
        }
    }
}
