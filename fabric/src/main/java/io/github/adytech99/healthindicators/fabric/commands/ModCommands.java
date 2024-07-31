package io.github.adytech99.healthindicators.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.adytech99.healthindicators.HealthIndicatorsCommon;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.enums.HealthDisplayTypeEnum;
import io.github.adytech99.healthindicators.fabric.HealthIndicatorsMod;
import io.github.adytech99.healthindicators.util.ConfigUtils;
import io.github.adytech99.healthindicators.util.Maths;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntitySubPredicateTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ModCommands {
    @Environment(EnvType.CLIENT)
    public static void registerCommands(){
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::configCommands);
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::openModMenuCommand);
        //ClientCommandRegistrationCallback.EVENT.register(ModCommands::IndicatorTypeCommand);
    }

    public static final SuggestionProvider<FabricClientCommandSource> CLIENT_SUMMONABLE_ENTITIES = SuggestionProviders.register(Identifier.of("healthindicators","summonable_entities"), (context, builder) -> CommandSource.suggestFromIdentifier(Registries.ENTITY_TYPE.stream().filter(entityType -> entityType.isEnabled(((CommandSource)context.getSource()).getEnabledFeatures()) && entityType.isSummonable()), builder, EntityType::getId, entityType -> Text.translatable(Util.createTranslationKey("entity", EntityType.getId(entityType)))));

    private static void configCommands(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {
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

    private static void openModMenuCommand(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {
        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("healthindicators")
            .executes(context -> {
                HealthIndicatorsCommon.openConfig();
                return 1;
        }));
    }
}
