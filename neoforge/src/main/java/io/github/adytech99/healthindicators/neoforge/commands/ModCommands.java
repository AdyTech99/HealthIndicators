package io.github.adytech99.healthindicators.neoforge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.adytech99.healthindicators.HealthIndicatorsCommon;
import io.github.adytech99.healthindicators.RenderTracker;
import io.github.adytech99.healthindicators.enums.HealthDisplayTypeEnum;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.util.ConfigUtils;
import io.github.adytech99.healthindicators.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import java.util.Objects;

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
                                    ConfigUtils.sendMessage(MinecraftClient.getInstance().player, Text.literal("Set heart offset to " + Util.truncate(ModConfig.HANDLER.instance().display_offset, 2)));
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

                .then(CommandManager.literal("monitor")
                        .then(CommandManager.argument("entity_name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                            for(Entity entity : MinecraftClient.getInstance().world.getEntities()){
                                if(entity.hasCustomName()) builder.suggest(Objects.requireNonNull(entity.getCustomName()).getString());
                                if(entity.isPlayer()) builder.suggest(Objects.requireNonNull(entity.getDisplayName()).getString());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            assert MinecraftClient.getInstance().world != null;
                            if(Util.getEntityFromName(MinecraftClient.getInstance().world, StringArgumentType.getString(context, "entity_name")) != null) {
                                ConfigUtils.sendMessage(MinecraftClient.getInstance().player, (Text.literal("Now monitoring " + StringArgumentType.getString(context, "entity_name"))));
                                RenderTracker.setTrackedEntity((LivingEntity) Util.getEntityFromName(MinecraftClient.getInstance().world, StringArgumentType.getString(context, "entity_name")));
                            }
                            else ConfigUtils.sendMessage(MinecraftClient.getInstance().player, (Text.literal("There is no entity named " + StringArgumentType.getString(context, "entity_name") + " in the world. It may have died or gone out of render distance.")));
                            return 0;
                        })))

                .then(CommandManager.literal("stop-monitoring")
                        .executes(context -> {
                            RenderTracker.setTrackedEntity(null);
                            ConfigUtils.sendMessage(MinecraftClient.getInstance().player, (Text.literal("Stopped monitoring ")));
                            return 0;
                        }))
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