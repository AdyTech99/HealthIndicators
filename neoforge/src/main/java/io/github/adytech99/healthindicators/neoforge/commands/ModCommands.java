package io.github.adytech99.healthindicators.neoforge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.adytech99.healthindicators.HealthIndicatorsCommon;
import io.github.adytech99.healthindicators.enums.HealthDisplayTypeEnum;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.util.ConfigUtils;
import io.github.adytech99.healthindicators.util.Maths;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public class ModCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        registerConfigCommands(event.getDispatcher());
        registerOpenConfigCommand(event.getDispatcher());
    }
    public static void registerConfigCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("healthindicators")
                .then(CommandManager.literal("offset")
                        .then(CommandManager.argument("offset", DoubleArgumentType.doubleArg())
                                .executes(context -> {
                                    ModConfig.HANDLER.instance().display_offset = DoubleArgumentType.getDouble(context, "offset");
                                    ModConfig.HANDLER.save();
                                    ConfigUtils.sendMessage(MinecraftClient.getInstance().player, Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().display_offset, 2)));
                                    return 1;
                                })))

                .then(CommandManager.literal("indicator-type")
                        .then(CommandManager.argument("indicator_type", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    builder.suggest("heart");
                                    builder.suggest("number");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    HealthDisplayTypeEnum displayTypeEnum;
                                    String type = StringArgumentType.getString(context, "indicator_type");
                                    if ("heart".equals(type)) {
                                        displayTypeEnum = HealthDisplayTypeEnum.HEARTS;
                                    } else if ("number".equals(type)) {
                                        displayTypeEnum = HealthDisplayTypeEnum.NUMBER;
                                    } else {
                                        ConfigUtils.sendMessage(MinecraftClient.getInstance().player, Text.literal("Unknown argument, please try again."));
                                        return 1;
                                    }
                                    ModConfig.HANDLER.instance().indicator_type = displayTypeEnum;
                                    ModConfig.HANDLER.save();
                                    ConfigUtils.sendMessage(MinecraftClient.getInstance().player, Text.literal("Set display type to " + ModConfig.HANDLER.instance().indicator_type));
                                    return 1;
                                })))
        );
    }

    public static void registerOpenConfigCommand(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(CommandManager.literal("healthindicators")
                .executes(context -> {
                    HealthIndicatorsCommon.openConfig();
                    return 1;
                })
        );
    }
}