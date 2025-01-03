package io.github.adytech99.healthindicators.fabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.adytech99.healthindicators.HealthIndicatorsCommon;
import io.github.adytech99.healthindicators.RenderTracker;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.enums.HealthDisplayTypeEnum;
import io.github.adytech99.healthindicators.util.ConfigUtils;
import io.github.adytech99.healthindicators.util.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

import java.util.Objects;

public class ModCommands {
    @Environment(EnvType.CLIENT)
    public static void registerCommands(){
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::configCommands);
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::openModMenuCommand);
    }


    private static void configCommands(CommandDispatcher<FabricClientCommandSource> fabricClientCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess) {
        fabricClientCommandSourceCommandDispatcher.register(ClientCommandManager.literal("healthindicators")
            .then(ClientCommandManager.literal("offset")
                .then(ClientCommandManager.argument("offset", DoubleArgumentType.doubleArg())
                    .executes(context -> {
                        ModConfig.HANDLER.instance().display_offset = DoubleArgumentType.getDouble(context, "offset");
                        ModConfig.HANDLER.save();
                        ConfigUtils.sendMessage(context.getSource().getPlayer(), Text.literal("Set heart offset to " + Util.truncate(ModConfig.HANDLER.instance().display_offset,2)));
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


            .then(ClientCommandManager.literal("monitor")
                .then(ClientCommandManager.argument("entity_name", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            for(Entity entity : context.getSource().getWorld().getEntities()){
                                if(entity.hasCustomName()) builder.suggest(Objects.requireNonNull(entity.getCustomName()).getString());
                                if(entity.isPlayer()) builder.suggest(Objects.requireNonNull(entity.getDisplayName()).getString());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if(Util.getEntityFromName(context.getSource().getWorld(), StringArgumentType.getString(context, "entity_name")) != null) {
                                ConfigUtils.sendMessage(context.getSource().getPlayer(), (Text.literal("Now monitoring " + StringArgumentType.getString(context, "entity_name"))));
                                RenderTracker.setTrackedEntity((LivingEntity) Util.getEntityFromName(context.getSource().getWorld(), StringArgumentType.getString(context, "entity_name")));
                            }
                            else ConfigUtils.sendMessage(context.getSource().getPlayer(), (Text.literal("There is no entity named " + StringArgumentType.getString(context, "entity_name") + " in the world. It may have died or gone out of render distance.")));
                            return 0;
                        })))

            .then(ClientCommandManager.literal("stop-monitoring")
                .executes(context -> {
                    RenderTracker.setTrackedEntity(null);
                    ConfigUtils.sendMessage(context.getSource().getPlayer(), (Text.literal("Stopped monitoring ")));
                    return 0;
                }))
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
