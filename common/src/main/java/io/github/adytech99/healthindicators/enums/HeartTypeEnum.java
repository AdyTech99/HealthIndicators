package io.github.adytech99.healthindicators.enums;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;

public enum HeartTypeEnum {
    EMPTY("container"),
    RED_FULL("full"),
    RED_HALF("half"),
    YELLOW_FULL("absorbing_full"),
    YELLOW_HALF("absorbing_half");

    public final Identifier icon;
    public final Identifier vanillaIcon;

    HeartTypeEnum(String heartIcon) {
        icon = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + addStatusIcon() + addHardcoreIcon() + heartIcon + ".png");
        vanillaIcon = Identifier.of("healthindicators", "textures/gui/heart/" + addStatusIcon() + addHardcoreIcon() + heartIcon + ".png");
    }

    private static String addStatusIcon(){
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        assert clientPlayer != null;
        if (clientPlayer.hasStatusEffect(StatusEffects.POISON)) return "poisoned_";
        if (clientPlayer.hasStatusEffect(StatusEffects.WITHER)) return "withered_";
        if (clientPlayer.isFrozen()) return "frozen_";
        else return "";
    }


    private static String addHardcoreIcon(){
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        assert clientPlayer != null;
        if (clientPlayer.clientWorld.getLevelProperties().isHardcore()) return "hardcore_";
        else return "";
    }
}