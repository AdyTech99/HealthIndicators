package io.github.adytech99.healthindicators.fabric.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.adytech99.healthindicators.config.ModConfig;


public class ModMenuAPIImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModConfig::createScreen;
    }
}
