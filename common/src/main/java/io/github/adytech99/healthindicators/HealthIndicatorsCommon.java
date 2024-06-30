package io.github.adytech99.healthindicators;

import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.util.ConfigUtils;
import io.github.adytech99.healthindicators.util.Maths;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class HealthIndicatorsCommon {
    public static final String MOD_ID = "healthindicators";
    public static final MinecraftClient client = MinecraftClient.getInstance();

    private static boolean changed = false;
    private static boolean openConfig = false;

    public static void init() {
        ModConfig.HANDLER.load();
        Config.load();
    }

    public static void tick(){
        if(openConfig){
            Screen configScreen = ModConfig.createScreen(client.currentScreen);
            client.setScreen(configScreen);
            openConfig = false;
        }

        if(client.world == null) return;
        if(changed && client.world.getTime() % 200 == 0){
            ModConfig.HANDLER.save();
            changed = false;
        }

        RenderTracker.tick(client);
    }

    public static void openConfig(){
        openConfig = client.world != null;
    }

    public static void enableHeartsRendering(){
        Config.setHeartsRenderingEnabled(!Config.getHeartsRenderingEnabled());
        if (client.player != null) {
            Formatting formatting;
            if(ModConfig.HANDLER.instance().colored_messages) formatting = Config.getHeartsRenderingEnabled() ? Formatting.GREEN : Formatting.RED;
            else formatting = Formatting.WHITE;
            ConfigUtils.sendMessage(client.player, Text.literal((Config.getHeartsRenderingEnabled() ? "Enabled" : "Disabled") + " Armor Indicators").formatted(formatting));
        }
    }

    public static void enableArmorRendering(){
        Config.setArmorRenderingEnabled(!Config.getArmorRenderingEnabled());
        if (client.player != null) {
            Formatting formatting;
            if(ModConfig.HANDLER.instance().colored_messages) formatting = Config.getArmorRenderingEnabled() ? Formatting.GREEN : Formatting.RED;
            else formatting = Formatting.WHITE;
            ConfigUtils.sendMessage(client.player, Text.literal((Config.getArmorRenderingEnabled() ? "Enabled" : "Disabled") + " Armor Indicators").formatted(formatting));
        }
    }

    public static void increaseOffset(){
        ModConfig.HANDLER.instance().display_offset = (ModConfig.HANDLER.instance().display_offset + ModConfig.HANDLER.instance().offset_step_size);
        changed = true;
        if (client.player != null) {
            ConfigUtils.sendMessage(client.player, Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().display_offset,2)));
        }
    }
    public static void decreaseOffset(){
        ModConfig.HANDLER.instance().display_offset = (ModConfig.HANDLER.instance().display_offset - ModConfig.HANDLER.instance().offset_step_size);
        changed = true;
        if (client.player != null) {
            ConfigUtils.sendMessage(client.player, Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().display_offset,2)));
        }
    }

    public static void overrideFilters(){
        Config.setOverrideAllFiltersEnabled(true);
        if (client.player != null) {
            ConfigUtils.sendOverlayMessage(client.player, Text.literal( " Config Criteria " + (Config.getOverrideAllFiltersEnabled() ? "Temporarily Overridden" : "Re-implemented")));
        }
    }

    public static void disableOverrideFilters(){
        Config.setOverrideAllFiltersEnabled(false);
        client.inGameHud.setOverlayMessage(Text.literal(""), false);
    }

    public static void openConfigScreen(){
        openConfig();
    }
}
