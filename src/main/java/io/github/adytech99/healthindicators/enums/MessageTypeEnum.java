package io.github.adytech99.healthindicators.enums;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.text.Text;

public enum MessageTypeEnum implements NameableEnum {
    ACTIONBAR("Actionbar"),
    CHAT("Chat");

    private final String displayName;
    MessageTypeEnum(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public Text getDisplayName() {
        return Text.of(displayName);
    }
}
