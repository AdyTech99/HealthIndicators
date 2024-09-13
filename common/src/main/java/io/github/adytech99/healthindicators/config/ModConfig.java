package io.github.adytech99.healthindicators.config;

import com.google.common.collect.Lists;
import dev.architectury.platform.Platform;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.Boolean;
import dev.isxander.yacl3.config.v2.api.autogen.Label;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import io.github.adytech99.healthindicators.enums.HealthDisplayTypeEnum;
import io.github.adytech99.healthindicators.enums.IndicatorLocationEnum;
import io.github.adytech99.healthindicators.enums.MessageTypeEnum;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;

public class ModConfig {
    public static final Path CONFIG_PATH = Platform.getConfigFolder().resolve("health_indicators_config.json");

    public static final ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(Identifier.of("health-indicators", "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(CONFIG_PATH)
                    .build())
            .build();

    @Label
    @AutoGen(category = "filters")
    private final Text filtersProTip = Text.literal("Pro Tip: You can temporarily override the below criteria and force health display for all living entities by holding the Right-Arrow key (customizable)").formatted(Formatting.GOLD);

    @Label
    //@AutoGen(category = "filters", group = "entity_type")
    private final Text filtersTypeLabel = Text.literal("Enable health display based on entity type").formatted(Formatting.BOLD, Formatting.AQUA);

    @SerialEntry
    @AutoGen(category = "filters", group = "entity_type")
    @TickBox
    public boolean passive_mobs = true;

    @SerialEntry
    @AutoGen(category = "filters", group = "entity_type")
    @TickBox
    public boolean hostile_mobs = true;

    @SerialEntry
    @AutoGen(category = "filters", group = "entity_type")
    @MasterTickBox(value = "override_players")
    public boolean players = true;

    @SerialEntry
    @AutoGen(category = "filters", group = "entity_type")
    @TickBox
    public boolean self = false;

    @SerialEntry
    @AutoGen(category = "filters")
    @Boolean(formatter = Boolean.Formatter.CUSTOM)
    public boolean blacklistOrWhitelist = true;

    @SerialEntry
    @AutoGen(category = "filters")
    @ListGroup(valueFactory = EntitiesListGroup.class, controllerFactory = EntitiesListGroup.class)
    public List<String> list = Lists.newArrayList("minecraft:armor_stand");


    @Label
    @AutoGen(category = "filters", group = "advanced")
    private final Text filtersAdvancedLabel = Text.literal("Show for...").formatted(Formatting.AQUA);

    /*@Label
    @AutoGen(category = "filters", group = "advanced")
    //private final Text after_attack_label = Text.literal("Settings for the 'Show on attack' criteria:").formatted(Formatting.ITALIC);
    private final Text after_attack_label = Text.literal(" ").formatted(Formatting.ITALIC);*/

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
    //private final Text damaged_only_label = Text.literal("Settings for the 'damaged entity' criteria:").formatted(Formatting.ITALIC);
    private final Text damaged_only_label = Text.literal(" ").formatted(Formatting.ITALIC);

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @MasterTickBox(value = {"override_players", "max_health_percentage"})
    public boolean damaged_only = false;

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @IntSlider(min = 0, max = 100, step = 1)
    public int max_health_percentage = 100;

    @Label
    @AutoGen(category = "filters", group = "advanced")
    //private final Text on_crosshair_label = Text.literal("Settings for the 'looking at entity' criteria:").formatted(Formatting.ITALIC);
    private final Text looking_at_label = Text.literal(" ").formatted(Formatting.ITALIC);

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
    private final Text distance_label = Text.literal(" ").formatted(Formatting.ITALIC);

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @MasterTickBox(value = {"override_players", "distance"})
    public boolean within_distance = false;

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @IntSlider(min = 0, max = 512, step = 4)
    public int distance = 64;


    @Label
    @AutoGen(category = "filters", group = "advanced")
    private final Text override_players_label = Text.literal("Overrides").formatted(Formatting.ITALIC);

    @SerialEntry
    @AutoGen(category = "filters", group = "advanced")
    @TickBox
    public boolean override_players = false;


    //APPEARANCE


    @SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @EnumCycler
    public HealthDisplayTypeEnum indicator_type = HealthDisplayTypeEnum.HEARTS;

    @AutoGen(category = "appearance", group = "indicator_type")
    @Label
    private final Text heart_type_settings_label = Text.literal("Settings for the heart-type indicator");

    @SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @IntField
    public int icons_per_row = 10;

    @SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @Boolean
    public boolean use_vanilla_textures = false;

    @SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @Boolean
    public boolean show_heart_effects = true;

    @AutoGen(category = "appearance", group = "indicator_type")
    @Label
    private final Text number_type_settings_label = Text.literal("Settings for the number-type indicator");

    @SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @ColorField
    public Color number_color = Color.RED;

    @SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @Boolean
    public boolean render_number_display_shadow = false;

    /*@SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @MasterTickBox(value = {"number_display_background_color"})*/
    public boolean render_number_display_background_color = false;

    /*@SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @ColorField*/
    public Color number_display_background_color = Color.BLACK;

    @AutoGen(category = "appearance", group = "indicator_type")
    @Label
    private final Text common_type_settings_label = Text.literal("Common settings for all indicator types");

    @SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @MasterTickBox(value = {"max_health"})
    public boolean percentage_based_health = false;

    @SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @IntSlider(min = 1, max = 100, step = 1)
    public int max_health = 20;

    @SerialEntry
    @AutoGen(category = "appearance", group = "indicator_type")
    @FloatSlider(min = 0.0f, max = 0.1f, step = 0.005f, format = "%.3f")
    public float size = 0.025f;




    @SerialEntry
    @AutoGen(category = "appearance", group = "offset")
    @DoubleField
    public double display_offset = 0;

    @SerialEntry
    @AutoGen(category = "appearance", group = "offset")
    @DoubleSlider(min = 0.0, max = 10.0, step = 0.5)
    public double offset_step_size = 1;

    @SerialEntry
    @AutoGen(category = "appearance", group = "offset")
    @Boolean
    public boolean force_higher_offset_for_players = false;


    //MESSAGES & COMMANDS


    @SerialEntry
    @AutoGen(category = "messages", group = "messages_appearance")
    @EnumCycler
    public MessageTypeEnum message_type = MessageTypeEnum.ACTIONBAR;

    @SerialEntry
    @AutoGen(category = "messages", group = "messages_appearance")
    @Boolean(colored = true)
    public boolean colored_messages = true;

    @Label
    @AutoGen(category = "messages", group = "commands")
    private final Text commandsRestartWarning = Text.literal("For this section, a restart is required to apply any modifications").formatted(Formatting.RED);

    @SerialEntry
    @AutoGen(category = "messages", group = "commands")
    @Boolean(formatter = Boolean.Formatter.YES_NO)
    public boolean enable_commands = true;

    /*@SerialEntry
    @AutoGen(category = "appearance", group = "heart_offset")
    @Boolean
    public boolean hearts_clipping = true;*/



    public static Screen createScreen(@Nullable Screen parent) {
        return HANDLER.generateGui().generateScreen(parent);
    }
    public Screen createConfigScreen(Screen parent) {
        if (Platform.isModLoaded("yet_another_config_lib_v3")) {
            return createScreen(parent);
        }
        return null;
    }

}
