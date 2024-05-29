package io.github.adytech99.healthindicators.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

public class ModConfig {
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("health_indicators_config.json");

    public static final ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(new Identifier("health-indicators", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(CONFIG_PATH)
                    .build())
            .build();

    @Label
    @AutoGen(category = "filters", group = "type")
    private final Text filtersTypeLabel = Text.literal("Enable/disable hearts display based on entity type").formatted(Formatting.BOLD, Formatting.AQUA);

    @SerialEntry
    @AutoGen(category = "filters", group = "type")
    @TickBox
    public boolean passive_mobs = true;

    @SerialEntry
    @AutoGen(category = "filters", group = "type")
    @TickBox
    public boolean hostile_mobs = true;

    @SerialEntry
    @AutoGen(category = "filters", group = "type")
    @MasterTickBox(value = "override_players")
    public boolean players = true;

    @SerialEntry
    @AutoGen(category = "filters", group = "type")
    @TickBox
    public boolean self = false;

    @Label
    @AutoGen(category = "filters", group = "advanced")
    private final Text filtersAdvancedLabel = Text.literal("Enable/disable hearts display based on additional misc. criteria").formatted(Formatting.BOLD, Formatting.AQUA);

    @Label
    @AutoGen(category = "filters", group = "advanced")
    private final Text after_attack_label = Text.literal("Settings for the 'Show on attack' criteria:").formatted(Formatting.ITALIC);

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @MasterTickBox(value = {"override_players", "time_after_hit"})
    public boolean after_attack = false;

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @IntSlider(min = 0, max = 120, step = 1)
    public int time_after_hit = 60;

    @Label
    @AutoGen(category = "filters", group = "advanced")
    private final Text damaged_only_label = Text.literal("Settings for the 'damaged entity' criteria:").formatted(Formatting.ITALIC);

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @MasterTickBox(value = "override_players")
    public boolean damaged_only = false;

    @Label
    @AutoGen(category = "filters", group = "advanced")
    private final Text on_crosshair_label = Text.literal("Settings for the 'looking at entity' criteria:").formatted(Formatting.ITALIC);

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @MasterTickBox(value = {"override_players", "reach"})
    public boolean looking_at = false;

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @IntField(min = 0, max = 1024)
    public int reach = 3;

    @Label
    @AutoGen(category = "filters", group = "advanced")
    private final Text override_players_label = Text.literal("Overrides").formatted(Formatting.ITALIC);

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @TickBox
    public boolean override_players = true;



    @SerialEntry
    @AutoGen(category = "appearance", group = "heart_offset")
    @DoubleField
    public double heart_offset = 0;

    @SerialEntry
    @AutoGen(category = "appearance", group = "heart_offset")
    @DoubleSlider(min = 0.0, max = 10.0, step = 0.5)
    public double offset_step_size = 1;

    @SerialEntry
    @AutoGen(category = "appearance", group = "heart_offset")
    @Boolean
    public boolean force_higher_offset_for_players = false;

    /*@SerialEntry
    @AutoGen(category = "appearance", group = "heart_offset")
    @Boolean
    public boolean hearts_clipping = true;*/

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
