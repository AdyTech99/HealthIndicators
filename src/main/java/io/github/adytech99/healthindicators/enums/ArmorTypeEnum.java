package io.github.adytech99.healthindicators.enums;

import net.minecraft.util.Identifier;

public enum ArmorTypeEnum {
    FULL("armor_full"),
    HALF("armor_half"),
    EMPTY("armor_empty");

    public final Identifier icon;

    ArmorTypeEnum(String heartIcon) {
        icon = Identifier.of("minecraft", "textures/gui/sprites/hud/" + heartIcon + ".png");
    }
}