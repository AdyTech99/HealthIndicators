package io.github.adytech99.healthindicators.config;

import com.google.gson.Gson;
import io.github.adytech99.healthindicators.HealthIndicatorsMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Config {
    private static final Gson GSON = new Gson();

    private static Config INSTANCE = new Config();

    private boolean renderingEnabled = true;
    private boolean armorRenderingEnabled = true;
    private boolean overrideAllFiltersEnabled = false;

    public static boolean getRenderingEnabled() {
        return INSTANCE.renderingEnabled;
    }

    public static void setRenderingEnabled(boolean renderingEnabled) {
        INSTANCE.renderingEnabled = renderingEnabled;
        save();
    }

    public static boolean getArmorRenderingEnabled() {
        return INSTANCE.armorRenderingEnabled;
    }
    public static void setArmorRenderingEnabled(boolean armorRenderingEnabled) {
        INSTANCE.armorRenderingEnabled = armorRenderingEnabled;
        save();
    }

    public static boolean getOverrideAllFiltersEnabled() {
        return INSTANCE.overrideAllFiltersEnabled;
    }

    public static void setOverrideAllFiltersEnabled(boolean overrideAllFiltersEnabled) {
        INSTANCE.overrideAllFiltersEnabled = overrideAllFiltersEnabled;
        save();
    }


    public static void load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FabricLoader.getInstance().getConfigDir().resolve(HealthIndicatorsMod.CONFIG_FILE).toFile()))) {
            Config config = GSON.fromJson(reader, Config.class);
            if (config != null) {
                INSTANCE = config;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FabricLoader.getInstance().getConfigDir().resolve(HealthIndicatorsMod.CONFIG_FILE).toFile()))) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
