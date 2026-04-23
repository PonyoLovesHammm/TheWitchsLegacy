package com.ponyo.thewitchslegacy.menu;

import com.ponyo.thewitchslegacy.TheWitchsLegacy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TheWitchsLegacy.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<WitchOvenMenu>> WITCH_OVEN =
            MENUS.register("witch_oven", () -> IMenuTypeExtension.create(WitchOvenMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AltarMenu>> ALTAR =
            MENUS.register("altar", () -> IMenuTypeExtension.create(AltarMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DistilleryMenu>> DISTILLERY =
            MENUS.register("distillery", () -> IMenuTypeExtension.create(DistilleryMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
