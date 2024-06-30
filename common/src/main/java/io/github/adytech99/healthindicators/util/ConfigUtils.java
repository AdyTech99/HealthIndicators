package io.github.adytech99.healthindicators.util;

import io.github.adytech99.healthindicators.enums.MessageTypeEnum;
import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ConfigUtils {
    public static void sendMessage(ClientPlayerEntity player, Text text) {
        if (isSendMessage()) {
            boolean overlay = ModConfig.HANDLER.instance().message_type == MessageTypeEnum.ACTIONBAR;
            player.sendMessage(text, overlay);
        }
    }

    public static void sendMessage(ServerPlayerEntity player, Text text) {
        if (isSendMessage()) {
            boolean overlay = ModConfig.HANDLER.instance().message_type == MessageTypeEnum.ACTIONBAR;
            player.sendMessage(text, overlay);
        }
    }

    public static void sendOverlayMessage(ClientPlayerEntity player, Text text) {
        if (isSendMessage()) {
            boolean overlay = ModConfig.HANDLER.instance().message_type == MessageTypeEnum.ACTIONBAR;
            player.sendMessage(text, true);
        }
    }

    public static boolean isSendMessage() {
        return ModConfig.HANDLER.instance().message_type != MessageTypeEnum.NONE;
    }

}
