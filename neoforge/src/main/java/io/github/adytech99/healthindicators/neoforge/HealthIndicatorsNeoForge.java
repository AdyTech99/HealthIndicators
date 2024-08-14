package io.github.adytech99.healthindicators.neoforge;

import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.neoforge.commands.ModCommands;
import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
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
import org.jetbrains.annotations.NotNull;

@Mod(HealthIndicatorsCommon.MOD_ID)
@EventBusSubscriber(value = Dist.CLIENT, modid = HealthIndicatorsCommon.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class HealthIndicatorsNeoForge {

    public static MinecraftClient client = MinecraftClient.getInstance();

    public static final Lazy<KeyBinding> HEARTS_RENDERING_ENABLED = Lazy.of(() -> new KeyBinding(
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



    public HealthIndicatorsNeoForge() {
        HealthIndicatorsCommon.init();
        if(ModConfig.HANDLER.instance().enable_commands) NeoForge.EVENT_BUS.addListener(ModCommands::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);

    }

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event){
        event.register(HEARTS_RENDERING_ENABLED.get());
        event.register(INCREASE_HEART_OFFSET.get());
        event.register(DECREASE_HEART_OFFSET.get());
        event.register(OVERRIDE_ALL_FILTERS.get());
        event.register(ARMOR_RENDERING_ENABLED.get());
        event.register(OPEN_CONFIG_SCREEN.get());
    }


    public void onClientTick(ClientTickEvent.Post event){
        HealthIndicatorsCommon.tick();

        while (HEARTS_RENDERING_ENABLED.get().wasPressed()) {
            HealthIndicatorsCommon.enableHeartsRendering();
        }

        while (ARMOR_RENDERING_ENABLED.get().wasPressed()) {
            HealthIndicatorsCommon.enableArmorRendering();
        }

        while (INCREASE_HEART_OFFSET.get().wasPressed()) {
            HealthIndicatorsCommon.increaseOffset();
        }

        while (DECREASE_HEART_OFFSET.get().wasPressed()) {
            HealthIndicatorsCommon.decreaseOffset();
        }
        if (OVERRIDE_ALL_FILTERS.get().wasPressed()) {
            HealthIndicatorsCommon.overrideFilters();
        }
        else if(Config.getOverrideAllFiltersEnabled()) {
            HealthIndicatorsCommon.disableOverrideFilters();
        }

        if(OPEN_CONFIG_SCREEN.get().wasPressed()) HealthIndicatorsCommon.openConfigScreen();
    }

    @SubscribeEvent
    public static void constructMod(FMLConstructModEvent event){
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> new IConfigScreenFactory() {
            @Override
            public @NotNull Screen createScreen(@NotNull ModContainer arg, @NotNull Screen arg2) {
                return ModConfig.createScreen(arg2);
            }
        });
    }
}