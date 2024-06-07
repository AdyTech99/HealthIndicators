package io.github.adytech99.healthindicators.enums;

import net.minecraft.util.Identifier;

public enum HeartTypeEnum {
    EMPTY("container"),
    RED_FULL("full"),
    RED_HALF("half"),
    YELLOW_FULL("absorbing_full"),
    YELLOW_HALF("absorbing_half");

    public final Identifier icon;

    HeartTypeEnum(String heartIcon) {
        icon = new Identifier("minecraft", "textures/gui/sprites/hud/heart/" + heartIcon + ".png");
    }
}