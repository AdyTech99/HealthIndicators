package io.github.adytech99.healthindicators.neoforge;

import io.github.adytech99.healthindicators.RenderTracker;
import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.neoforge.commands.ModCommands;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.util.ConfigUtils;
import io.github.adytech99.healthindicators.util.Maths;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;

import io.github.adytech99.healthindicators.HealthIndicatorsCommon;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.jarjar.nio.util.Lazy;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

@Mod(HealthIndicatorsCommon.MOD_ID)
@EventBusSubscriber(value = Dist.CLIENT, modid = HealthIndicatorsCommon.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class HealthIndicatorsMod {

    public static MinecraftClient client = MinecraftClient.getInstance();
    private static boolean changed = false;
    private static boolean openConfig = false;

    public static void openConfig(){
        openConfig = client.world != null;
    }

    public static final KeyBinding EXAMPLE_MAPPING =
        new KeyBinding(
            "key.examplemod.example1", // Will be localized using this translation key
            InputUtil.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_P, // Default key is P
            "key.categories.misc" // Mapping will be in the misc category
        );

    public static final Lazy<KeyBinding> RENDERING_ENABLED = Lazy.of(() -> new KeyBinding(
            "key." + HealthIndicatorsCommon.MOD_ID + ".renderingEnabled",
            InputUtil.GLFW_KEY_LEFT,
            "key.categories." + HealthIndicatorsCommon.MOD_ID
    ));

    public static final Lazy<KeyBinding> ARMOR_RENDERING_ENABLED = Lazy.of(() -> new KeyBinding(
            "key." + HealthIndicatorsCommon.MOD_ID + ".armorRenderingEnabled",
            InputUtil.GLFW_KEY_RIGHT_SHIFT,
            "key.categories." + HealthIndicatorsCommon.MOD_ID
    ));

    public static final Lazy<KeyBinding> OVERRIDE_ALL_FILTERS = Lazy.of(() -> new KeyBinding(
            "key." + HealthIndicatorsCommon.MOD_ID + ".overrideAllFilters",
            InputUtil.GLFW_KEY_RIGHT,
            "key.categories." + HealthIndicatorsCommon.MOD_ID
    ));
    public static final Lazy<KeyBinding> INCREASE_HEART_OFFSET = Lazy.of(() -> new KeyBinding(
            "key." + HealthIndicatorsCommon.MOD_ID + ".increaseHeartOffset",
            InputUtil.GLFW_KEY_UP,
            "key.categories." + HealthIndicatorsCommon.MOD_ID
    ));

    public static final Lazy<KeyBinding> DECREASE_HEART_OFFSET = Lazy.of(() -> new KeyBinding(
            "key." + HealthIndicatorsCommon.MOD_ID + ".decreaseHeartOffset",
            InputUtil.GLFW_KEY_DOWN,
            "key.categories." + HealthIndicatorsCommon.MOD_ID));

    public static final Lazy<KeyBinding> OPEN_CONFIG_SCREEN = Lazy.of(() -> new KeyBinding(
            "key." + HealthIndicatorsCommon.MOD_ID + ".openModMenuConfig",
            InputUtil.GLFW_KEY_I,
            "key.categories." + HealthIndicatorsCommon.MOD_ID));

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event){
        event.register(RENDERING_ENABLED.get());
        event.register(INCREASE_HEART_OFFSET.get());
        event.register(DECREASE_HEART_OFFSET.get());
        event.register(OVERRIDE_ALL_FILTERS.get());
        event.register(ARMOR_RENDERING_ENABLED.get());
        event.register(OPEN_CONFIG_SCREEN.get());
    }


    public void onClientTick(ClientTickEvent.Post event){
        if(openConfig){
            Screen configScreen = ModConfig.createScreen(client.currentScreen);
            client.setScreen(configScreen);
            openConfig = false;
        }

        while (RENDERING_ENABLED.get().wasPressed()) {
            Config.setHeartsRenderingEnabled(!Config.getHeartsRenderingEnabled());
            if (client.player != null) {
                Formatting formatting;
                if(ModConfig.HANDLER.instance().colored_messages) formatting = Config.getHeartsRenderingEnabled() ? Formatting.GREEN : Formatting.RED;
                else formatting = Formatting.WHITE;
                ConfigUtils.sendMessage(client.player, Text.literal((Config.getHeartsRenderingEnabled() ? "Enabled" : "Disabled") + " Armor Indicators").formatted(formatting));
            }
        }

        while (ARMOR_RENDERING_ENABLED.get().wasPressed()) {
            Config.setArmorRenderingEnabled(!Config.getArmorRenderingEnabled());
            if (client.player != null) {
                Formatting formatting;
                if(ModConfig.HANDLER.instance().colored_messages) formatting = Config.getArmorRenderingEnabled() ? Formatting.GREEN : Formatting.RED;
                else formatting = Formatting.WHITE;
                ConfigUtils.sendMessage(client.player, Text.literal((Config.getArmorRenderingEnabled() ? "Enabled" : "Disabled") + " Armor Indicators").formatted(formatting));
            }
        }

        while (INCREASE_HEART_OFFSET.get().wasPressed()) {
            ModConfig.HANDLER.instance().display_offset = (ModConfig.HANDLER.instance().display_offset + ModConfig.HANDLER.instance().offset_step_size);
            changed = true;
            if (client.player != null) {
                ConfigUtils.sendMessage(client.player, Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().display_offset,2)));
            }
        }

        while (DECREASE_HEART_OFFSET.get().wasPressed()) {
            ModConfig.HANDLER.instance().display_offset = (ModConfig.HANDLER.instance().display_offset - ModConfig.HANDLER.instance().offset_step_size);
            changed = true;
            if (client.player != null) {
                ConfigUtils.sendMessage(client.player, Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().display_offset,2)));
            }
        }
        if (OVERRIDE_ALL_FILTERS.get().wasPressed()) {
            Config.setOverrideAllFiltersEnabled(true);
            if (client.player != null) {
                ConfigUtils.sendOverlayMessage(client.player, Text.literal( " Config Criteria " + (Config.getOverrideAllFiltersEnabled() ? "Temporarily Overridden" : "Re-implemented")));
            }
        }
        else if(Config.getOverrideAllFiltersEnabled()) {
            Config.setOverrideAllFiltersEnabled(false);
            client.inGameHud.setOverlayMessage(Text.literal(""), false);
        }

        if(OPEN_CONFIG_SCREEN.get().wasPressed()) openConfig();

        if(client.world == null) return;
        if(changed && client.world.getTime() % 200 == 0){
            ModConfig.HANDLER.save();
            changed = false;
        }

        RenderTracker.tick(client);
    }

    @SubscribeEvent
    public static void constructMod(FMLConstructModEvent event){
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> new IConfigScreenFactory() {
            @Override
            public Screen createScreen(MinecraftClient arg, Screen arg2) {
                return ModConfig.createScreen(arg2);
            }
        });
    }

    public HealthIndicatorsMod() {
        ModConfig.HANDLER.load();
        ModConfig.HANDLER.save();
        HealthIndicatorsCommon.init();
        NeoForge.EVENT_BUS.addListener(ModCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
    }
}