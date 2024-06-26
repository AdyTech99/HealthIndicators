package io.github.adytech99.healthindicators;

import com.terraformersmc.modmenu.ModMenu;
import io.github.adytech99.healthindicators.commands.ModCommands;
import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.enums.MessageTypeEnum;
import io.github.adytech99.healthindicators.util.ConfigUtils;
import io.github.adytech99.healthindicators.util.Maths;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class HealthIndicatorsMod implements ClientModInitializer {
    public static final String MOD_ID = "healthindicators";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String CONFIG_FILE = "healthindicators.json";

    public static final KeyBinding RENDERING_ENABLED = KeyBindingHelper.registerKeyBinding(new KeyBinding(
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

    public static final KeyBinding OPEN_MOD_MENU_CONFIG = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".openModMenuConfig",
            InputUtil.GLFW_KEY_I,
            "key.categories." + MOD_ID
    ));


    private boolean changed = false;
    private static boolean openConfig = false;

    public static void openConfig(MinecraftClient client){
        openConfig = client.world != null;
    }

    @Override
    public void onInitializeClient() {
        ModConfig.HANDLER.load();
        Config.load();
        if(ModConfig.HANDLER.instance().enable_commands) ModCommands.registerCommands();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(openConfig){
                Screen configScreen = ModMenu.getConfigScreen(HealthIndicatorsMod.MOD_ID, client.currentScreen);
                client.setScreen(configScreen);
                openConfig = false;
            }
            boolean overlay = ModConfig.HANDLER.instance().message_type == MessageTypeEnum.ACTIONBAR;
            while (RENDERING_ENABLED.wasPressed()) {
                Config.setRenderingEnabled(!Config.getHeartsRenderingEnabled());
                if (client.player != null) {
                    Formatting formatting;
                    if(ModConfig.HANDLER.instance().colored_messages) formatting = Config.getHeartsRenderingEnabled() ? Formatting.GREEN : Formatting.RED;
                    else formatting = Formatting.WHITE;
                    ConfigUtils.sendMessage(client.player, Text.literal((Config.getHeartsRenderingEnabled() ? "Enabled" : "Disabled") + " Health Indicators").formatted(formatting));
                }
            }

            while (ARMOR_RENDERING_ENABLED.wasPressed()) {
                Config.setArmorRenderingEnabled(!Config.getArmorRenderingEnabled());
                if (client.player != null) {
                    Formatting formatting;
                    if(ModConfig.HANDLER.instance().colored_messages) formatting = Config.getArmorRenderingEnabled() ? Formatting.GREEN : Formatting.RED;
                    else formatting = Formatting.WHITE;
                    ConfigUtils.sendMessage(client.player, Text.literal((Config.getArmorRenderingEnabled() ? "Enabled" : "Disabled") + " Armor Indicators").formatted(formatting));
                }
            }

            while (INCREASE_HEART_OFFSET.wasPressed()) {
                ModConfig.HANDLER.instance().display_offset = (ModConfig.HANDLER.instance().display_offset + ModConfig.HANDLER.instance().offset_step_size);
                changed = true;
                if (client.player != null) {
                    ConfigUtils.sendMessage(client.player, Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().display_offset,2)));
                }
            }

            while (DECREASE_HEART_OFFSET.wasPressed()) {
                ModConfig.HANDLER.instance().display_offset = (ModConfig.HANDLER.instance().display_offset - ModConfig.HANDLER.instance().offset_step_size);
                changed = true;
                if (client.player != null) {
                    ConfigUtils.sendMessage(client.player, Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().display_offset,2)));
                }
            }
            if (OVERRIDE_ALL_FILTERS.isPressed()) {
                Config.setOverrideAllFiltersEnabled(true);
                if (client.player != null) {
                    ConfigUtils.sendOverlayMessage(client.player, Text.literal( " Config Criteria " + (Config.getOverrideAllFiltersEnabled() ? "Temporarily Overridden" : "Re-implemented")));
                }
            }
            else if(Config.getOverrideAllFiltersEnabled()) {
                Config.setOverrideAllFiltersEnabled(false);
                client.inGameHud.setOverlayMessage(Text.literal(""), false);
            }
            if(OPEN_MOD_MENU_CONFIG.isPressed()){
                openConfig(client);
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
        LOGGER.info("Never be heartless!");
    }

    public void saveModConfig(){
        ModConfig.HANDLER.save();
    }
}
