package io.github.adytech99.healthindicators.enums;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.text.Text;

public enum DamageReasonEnum implements NameableEnum {
    ANY("ANY"),
    SELF("SELF"),
    PLAYER("ANY PLAYER"),
    MOB("ANY MOB"),
    NATURE("NATURAL");

    private final String displayName;
    DamageReasonEnum(String displayName) {
        this.displayName = displayName;
    }
    @Override
    public Text getDisplayName() {
        return Text.of(displayName);
    }
}
