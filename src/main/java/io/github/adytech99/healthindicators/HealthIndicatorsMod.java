package io.github.adytech99.healthindicators;

import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.util.Maths;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class HealthIndicatorsMod implements ClientModInitializer {
    public static final String MOD_ID = "healthindicators";
    public static final String CONFIG_FILE = "healthindicators.json";

    public static final KeyBinding RENDERING_ENABLED_KEY_BINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".renderingEnabled",
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.categories." + MOD_ID
    ));
    public static final KeyBinding OVERRIDE_ALL_FILTERS = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".overrideAllFilters",
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.categories." + MOD_ID
    ));
    public static final KeyBinding INCREASE_HEART_OFFSET_KEY_BINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".increaseHeartOffset",
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.categories." + MOD_ID
    ));
    public static final KeyBinding DECREASE_HEART_OFFSET_KEY_BINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".decreaseHeartOffset",
            InputUtil.UNKNOWN_KEY.getCode(),
            "key.categories." + MOD_ID
    ));


    private boolean changed = false;
    private boolean clearedActionbar = false;

    @Override
    public void onInitializeClient() {
        ModConfig.HANDLER.load();
        Config.load();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (RENDERING_ENABLED_KEY_BINDING.wasPressed()) {
                Config.setRenderingEnabled(!Config.getRenderingEnabled());
                if (client.player != null) {
                    client.player.sendMessage(Text.literal((Config.getRenderingEnabled() ? "Enabled" : "Disabled") + " Health Indicators"), true);
                }
            }

            if (OVERRIDE_ALL_FILTERS.isPressed()) {
                Config.setOverrideAllFiltersEnabled(true);
                if (client.player != null) {
                    client.player.sendMessage(Text.literal( " Config Filters " + (Config.getOverrideAllFiltersEnabled() ? "Temporarily overridden" : "Re-implemented")), true);
                }
            }
            else if(Config.getOverrideAllFiltersEnabled()) {
                Config.setOverrideAllFiltersEnabled(false);
                client.inGameHud.setOverlayMessage(Text.literal(""), false);
            }

            while (INCREASE_HEART_OFFSET_KEY_BINDING.wasPressed()) {
                ModConfig.HANDLER.instance().heart_offset = (ModConfig.HANDLER.instance().heart_offset + ModConfig.HANDLER.instance().offset_step_size);
                changed = true;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().heart_offset,2)), true);
                }
            }

            while (DECREASE_HEART_OFFSET_KEY_BINDING.wasPressed()) {
                ModConfig.HANDLER.instance().heart_offset = (ModConfig.HANDLER.instance().heart_offset - ModConfig.HANDLER.instance().offset_step_size);
                changed = true;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().heart_offset,2)), true);
                }
            }
        });

        ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            RenderTracker.removeFromUUIDS(entity);
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if(client.world == null) return;
            if(changed && client.world.getTime() % 200 == 0){
                saveModConfig();
                changed = false;
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            saveModConfig();
        });

        ClientTickEvents.END_CLIENT_TICK.register(RenderTracker::tick);

    }

    public void saveModConfig(){
        ModConfig.HANDLER.save();
    }
}
