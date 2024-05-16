package io.github.adytech99.healthindicators.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.image.ImageRendererFactory;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ModConfig {
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("health_indicators_config.json");

    public static final ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(new Identifier("health-indicators", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(CONFIG_PATH)
                    .build())
            .build();

    @SerialEntry
    @AutoGen(category = "toggles")
    @TickBox
    public boolean passive_mobs = true;

    @SerialEntry
    @AutoGen(category = "toggles")
    @TickBox
    public boolean hostile_mobs = true;

    @SerialEntry
    @AutoGen(category = "toggles")
    @MasterTickBox(value = "self")
    public boolean players = true;

    @SerialEntry
    @AutoGen(category = "toggles")
    @TickBox
    public boolean self = false;

    @SerialEntry
    @AutoGen(category = "appearance", group = "heart_offset")
    @DoubleField
    public double heart_offset = 0;

    @SerialEntry
    @AutoGen(category = "appearance", group = "heart_offset")
    @DoubleSlider(min = 0.0, max = 10.0, step = 0.5)
    public double offset_step_size = 1;

    @SerialEntry
    @AutoGen(category = "appearance")
    @IntField
    public int hearts_per_row = 10;

    public static Screen createScreen(@Nullable Screen parent) {
        return HANDLER.generateGui().generateScreen(parent);
    }
    public Screen createConfigScreen(Screen parent) {
        if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
            return createScreen(parent);
        }
        return null;
    }

}
