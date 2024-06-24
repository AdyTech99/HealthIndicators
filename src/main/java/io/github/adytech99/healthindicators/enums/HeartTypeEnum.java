package io.github.adytech99.healthindicators.enums;

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
        icon = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + heartIcon + ".png");
        vanillaIcon = Identifier.of("healthindicators", "textures/gui/heart/" + heartIcon + ".png");
    }
}