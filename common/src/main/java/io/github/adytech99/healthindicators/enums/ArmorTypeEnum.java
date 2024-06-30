package io.github.adytech99.healthindicators.enums;

import net.minecraft.util.Identifier;

public enum ArmorTypeEnum {
    FULL("armor_full"),
    HALF("armor_half"),
    EMPTY("armor_empty");

    public final Identifier icon;
    public final Identifier vanillaIcon;

    ArmorTypeEnum(String armorIcon) {
        icon = Identifier.of("minecraft", "textures/gui/sprites/hud/" + armorIcon + ".png");
        vanillaIcon = Identifier.of("healthindicators", "textures/gui/armor/" + armorIcon + ".png");
    }
}