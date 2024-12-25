package io.github.adytech99.healthindicators.util;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.enums.ArmorTypeEnum;
import io.github.adytech99.healthindicators.enums.HeartTypeEnum;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import static io.github.adytech99.healthindicators.enums.HeartTypeEnum.addHardcoreIcon;
import static io.github.adytech99.healthindicators.enums.HeartTypeEnum.addStatusIcon;

public class RenderUtils {
    public static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, HeartTypeEnum type, LivingEntity livingEntity) {
        String additionalIconEffects = "";
        if(type != HeartTypeEnum.YELLOW_FULL && type != HeartTypeEnum.YELLOW_HALF && type != HeartTypeEnum.EMPTY && ModConfig.HANDLER.instance().show_heart_effects) additionalIconEffects = (addStatusIcon(livingEntity) + addHardcoreIcon(livingEntity));
        Identifier heartIcon = Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + additionalIconEffects + type.icon + ".png");
        Identifier vanillaHeartIcon = Identifier.of("healthindicators", "textures/gui/heart/" + additionalIconEffects + type.icon + ".png");

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        RenderSystem.setShaderTexture(0, ModConfig.HANDLER.instance().use_vanilla_textures ? vanillaHeartIcon : heartIcon);
        RenderSystem.enableDepthTest();

        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;

        float heartSize = 9F;

        vertexConsumer.vertex(model, x, 0F - heartSize, 0.0F).texture(minU, maxV);
        vertexConsumer.vertex(model, x - heartSize, 0F - heartSize, 0.0F).texture(maxU, maxV);
        vertexConsumer.vertex(model, x - heartSize, 0F, 0.0F).texture(maxU, minV);
        vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV);
    }


    public static void drawArmor(Matrix4f model, VertexConsumer vertexConsumer, float x, ArmorTypeEnum type) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        Identifier armorIcon = ModConfig.HANDLER.instance().use_vanilla_textures ? type.vanillaIcon : type.icon;
        RenderSystem.setShaderTexture(0, armorIcon);
        RenderSystem.enableDepthTest();

        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;

        float heartSize = 9F;

        vertexConsumer.vertex(model, x, 0F - heartSize, 0.0F).texture(minU, maxV);
        vertexConsumer.vertex(model, x - heartSize, 0F - heartSize, 0.0F).texture(maxU, maxV);
        vertexConsumer.vertex(model, x - heartSize, 0F, 0.0F).texture(maxU, minV);
        vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV);
    }

    public static String getHealthText(LivingEntity livingEntity) {
        int decimalPlaces = ModConfig.HANDLER.instance().decimal_places;
        float health = livingEntity.getHealth();
        float maxHealth = livingEntity.getMaxHealth();
        float absorption = livingEntity.getAbsorptionAmount();

        // Rounding off to the specified number of decimal places
        String healthStr = formatToDecimalPlaces(health + absorption, decimalPlaces);
        String maxHealthStr = formatToDecimalPlaces(maxHealth, decimalPlaces);
        //String absorptionStr = formatToDecimalPlaces(absorption, decimalPlaces);

        if (ModConfig.HANDLER.instance().percentage_based_health) {
            float percentage = ((health + absorption) / maxHealth) * 100;
            // Rounding off percentage to the specified number of decimal places
            String percentageStr = formatToDecimalPlaces(percentage, decimalPlaces);
            return percentageStr + " %";
        } else {
            return healthStr + " / " + maxHealthStr;
        }
    }

    private static String formatToDecimalPlaces(float value, int decimalPlaces) {
        // Convert the value to a string with the specified number of decimal places
        if (decimalPlaces == 0) {
            return String.format("%.0f", value);
        } else {
            String decimalPlaces2 = String.valueOf(decimalPlaces);
            return String.format("%." + decimalPlaces2 + "f", value);
        }
    }
}