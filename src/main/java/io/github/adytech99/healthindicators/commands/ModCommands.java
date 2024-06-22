package io.github.adytech99.healthindicators.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.adytech99.healthindicators.HealthIndicatorsMod;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.enums.HealthDisplayTypeEnum;
import io.github.adytech99.healthindicators.enums.MessageTypeEnum;
import io.github.adytech99.healthindicators.util.ConfigUtils;
import io.github.adytech99.healthindicators.util.Maths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

public class ModCommands {
    @Environment(EnvType.CLIENT)
    public static void registerCommands(){
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::OffsetCommand);
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::OpenModMenuConfigCommand);
        //ClientCommandRegistrationCallback.EVENT.register(ModCommands::IndicatorTypeCommand);
    }

    private static void OffsetCommand(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {
        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("healthindicators")
            .then(ClientCommandManager.literal("offset")
                .then(ClientCommandManager.argument("offset", DoubleArgumentType.doubleArg())
                    .executes(context -> {
                        ModConfig.HANDLER.instance().display_offset = DoubleArgumentType.getDouble(context, "offset");
                        ModConfig.HANDLER.save();
                        ConfigUtils.sendMessage(context.getSource().getPlayer(), Text.literal("Set heart offset to " + Maths.truncate(ModConfig.HANDLER.instance().display_offset,2)));
                        return 1;
                    })))

            .then(ClientCommandManager.literal("indicator-type")
                .then(ClientCommandManager.argument("indicator_type", StringArgumentType.string())
                    .suggests((context, builder) -> {
                        builder.suggest("heart");
                        builder.suggest("number");
                        return builder.buildFuture();
                    })
                        .executes(context -> {
                            HealthDisplayTypeEnum displayTypeEnum;
                            if (StringArgumentType.getString(context, "indicator_type").equals("heart")) {
                                displayTypeEnum = HealthDisplayTypeEnum.HEARTS;
                            } else if (StringArgumentType.getString(context, "indicator_type").equals("number")) {
                                displayTypeEnum = HealthDisplayTypeEnum.NUMBER;
                            } else {
                                ConfigUtils.sendMessage(context.getSource().getPlayer(), Text.literal("Unknown argument, please try again."));
                                return 1;
                            }

                            ModConfig.HANDLER.instance().indicator_type = displayTypeEnum;
                            ModConfig.HANDLER.save();
                            ConfigUtils.sendMessage(context.getSource().getPlayer(), Text.literal("Set display type to " + ModConfig.HANDLER.instance().indicator_type));
                            return 1;
                        })))
        );
    }

    private static void IndicatorTypeCommand(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {
        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("healthindicators")
                .then(ClientCommandManager.argument("operation", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            builder.suggest("indicator_type");
                            return builder.buildFuture();
                        })
                            .then(ClientCommandManager.argument("indicator_type", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    builder.suggest("heart");
                                    builder.suggest("number");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    HealthDisplayTypeEnum displayTypeEnum;
                                    if(StringArgumentType.getString(context, "indicator_type").equals("heart")){
                                        displayTypeEnum = HealthDisplayTypeEnum.HEARTS;
                                    } else if (StringArgumentType.getString(context, "indicator_type").equals("number")) {
                                        displayTypeEnum = HealthDisplayTypeEnum.NUMBER;
                                    }
                                    else {
                                        ConfigUtils.sendMessage(context.getSource().getPlayer(), Text.literal("Unknown argument, please try again."));
                                        return 1;
                                    }

                                    ModConfig.HANDLER.instance().indicator_type = displayTypeEnum;
                                    ModConfig.HANDLER.save();
                                    ConfigUtils.sendMessage(context.getSource().getPlayer(), Text.literal("Set display type to " + ModConfig.HANDLER.instance().indicator_type));
                                    return 1;
                                }))));
    }

    private static void OpenModMenuConfigCommand(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {
        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("healthindicators")
            .executes(context -> {
                HealthIndicatorsMod.openConfig(context.getSource().getClient());
                //client.player.sendMessage(Text.of(configScreen.toString()));
                return 1;
        }));
    }
}
