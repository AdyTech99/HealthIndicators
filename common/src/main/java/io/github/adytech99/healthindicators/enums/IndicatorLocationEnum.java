package io.github.adytech99.healthindicators.enums;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.text.Text;

public enum IndicatorLocationEnum implements NameableEnum {
    WORLD("World"),
    GUI("GUI"),
    BOTH("Both");

    private final String displayName;
    IndicatorLocationEnum(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public Text getDisplayName() {
        return Text.of(displayName);
    }
}
