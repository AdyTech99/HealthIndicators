package io.github.adytech99.healthindicators.util;

import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.enums.ArmorTypeEnum;
import io.github.adytech99.healthindicators.enums.HeartTypeEnum;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;

import static io.github.adytech99.healthindicators.enums.HeartTypeEnum.addHardcoreIcon;
import static io.github.adytech99.healthindicators.enums.HeartTypeEnum.addStatusIcon;

public class RenderUtils {
    public static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, HeartTypeEnum type, LivingEntity livingEntity) {
        // The vertexConsumer is already bound to the appropriate texture
        // We just need to draw the heart quad with the full texture coordinates
        
        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;

        float heartSize = 9F;
        
        // Draw the heart as a quad (two triangles)
        vertexConsumer.vertex(model, x, 0F - heartSize, 0.0F).texture(minU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, 1.0F);
        vertexConsumer.vertex(model, x - heartSize, 0F - heartSize, 0.0F).texture(maxU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, 1.0F);
        vertexConsumer.vertex(model, x - heartSize, 0F, 0.0F).texture(maxU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, 1.0F);
        vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawArmor(Matrix4f model, VertexConsumer vertexConsumer, float x, ArmorTypeEnum type) {
        // The vertexConsumer is already bound to the appropriate texture
        // We just need to draw the armor quad with the full texture coordinates
        
        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;

        float armorSize = 9F;
        
        // Draw the armor icon as a quad (two triangles)
        vertexConsumer.vertex(model, x, 0F - armorSize, 0.0F).texture(minU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, 1.0F);
        vertexConsumer.vertex(model, x - armorSize, 0F - armorSize, 0.0F).texture(maxU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, 1.0F);
        vertexConsumer.vertex(model, x - armorSize, 0F, 0.0F).texture(maxU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, 1.0F);
        vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static String getHealthText(LivingEntity livingEntity) {
        int decimalPlaces = ModConfig.HANDLER.instance().decimal_places;
        float health = livingEntity.getHealth();
        float maxHealth = livingEntity.getMaxHealth();
        float absorption = livingEntity.getAbsorptionAmount();

        // Rounding off to the specified number of decimal places
        String healthStr = formatToDecimalPlaces(health + absorption, decimalPlaces);
        String maxHealthStr = formatToDecimalPlaces(maxHealth, decimalPlaces);

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