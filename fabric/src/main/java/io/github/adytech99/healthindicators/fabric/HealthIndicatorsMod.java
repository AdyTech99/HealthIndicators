package io.github.adytech99.healthindicators.fabric;

import io.github.adytech99.healthindicators.HealthIndicatorsCommon;
import io.github.adytech99.healthindicators.RenderTracker;
import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.fabric.commands.ModCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

@Environment(EnvType.CLIENT)
public class HealthIndicatorsMod implements ClientModInitializer {
    public static final String MOD_ID = HealthIndicatorsCommon.MOD_ID;

    public static final KeyBinding HEARTS_RENDERING_ENABLED = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".renderingEnabled",
            InputUtil.GLFW_KEY_LEFT,
            "key.categories." + MOD_ID
    ));

    public static final KeyBinding ARMOR_RENDERING_ENABLED = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".armorRenderingEnabled",
            InputUtil.GLFW_KEY_RIGHT_SHIFT,
            "key.categories." + MOD_ID
    ));

    public static final KeyBinding OVERRIDE_ALL_FILTERS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".overrideAllFilters",
            InputUtil.GLFW_KEY_RIGHT,
            "key.categories." + MOD_ID
    ));
    public static final KeyBinding INCREASE_HEART_OFFSET = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".increaseHeartOffset",
            InputUtil.GLFW_KEY_UP,
            "key.categories." + MOD_ID
    ));
    public static final KeyBinding DECREASE_HEART_OFFSET = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".decreaseHeartOffset",
            InputUtil.GLFW_KEY_DOWN,
            "key.categories." + MOD_ID
    ));

    public static final KeyBinding OPEN_CONFIG_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".openModMenuConfig",
            InputUtil.GLFW_KEY_I,
            "key.categories." + MOD_ID
    ));

    @Override
    public void onInitializeClient() {
        HealthIndicatorsCommon.init();
        if(ModConfig.HANDLER.instance().enable_commands) ModCommands.registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HealthIndicatorsCommon.tick();

            while (HEARTS_RENDERING_ENABLED.wasPressed()) {
                HealthIndicatorsCommon.enableHeartsRendering();
            }

            while (ARMOR_RENDERING_ENABLED.wasPressed()) {
                HealthIndicatorsCommon.enableArmorRendering();
            }

            while (INCREASE_HEART_OFFSET.wasPressed()) {
                HealthIndicatorsCommon.increaseOffset();
            }

            while (DECREASE_HEART_OFFSET.wasPressed()) {
                HealthIndicatorsCommon.decreaseOffset();
            }
            if (OVERRIDE_ALL_FILTERS.wasPressed()) {
                HealthIndicatorsCommon.overrideFilters();
            }
            else if(Config.getOverrideAllFiltersEnabled()) {
                HealthIndicatorsCommon.disableOverrideFilters();
            }

            if(OPEN_CONFIG_SCREEN.wasPressed()) HealthIndicatorsCommon.openConfigScreen();
        });

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            RenderTracker.removeFromUUIDS(entity);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ModConfig.HANDLER.save();
        });
    }
}
