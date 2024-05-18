package io.github.adytech99.healthindicators;

import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.util.HitTracker;
import io.github.adytech99.healthindicators.util.Maths;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
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
    public static final KeyBinding HEART_STACKING_ENABLED_KEY_BINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".heartStackingEnabled",
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

            while (HEART_STACKING_ENABLED_KEY_BINDING.wasPressed()) {
                Config.setHeartStackingEnabled(!Config.getHeartStackingEnabled());
                if (client.player != null) {
                    client.player.sendMessage(Text.literal((Config.getHeartStackingEnabled() ? "Enabled" : "Disabled") + " Heart Stacking"), true);
                }
            }

            while (INCREASE_HEART_OFFSET_KEY_BINDING.wasPressed()) {
                ModConfig.HANDLER.instance().heart_offset = (ModConfig.HANDLER.instance().heart_offset + ModConfig.HANDLER.instance().offset_step_size);
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().heart_offset,2)), true);
                }
            }

            while (DECREASE_HEART_OFFSET_KEY_BINDING.wasPressed()) {
                ModConfig.HANDLER.instance().heart_offset = (ModConfig.HANDLER.instance().heart_offset - ModConfig.HANDLER.instance().offset_step_size);
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().heart_offset,2)), true);
                }
            }
        });

        //AttackEntityCallback.EVENT.register(HitTracker::attackHandler);

    }
}
