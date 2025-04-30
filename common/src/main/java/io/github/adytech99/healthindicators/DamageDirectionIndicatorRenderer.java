package io.github.adytech99.healthindicators;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.*;

public class DamageDirectionIndicatorRenderer {
    private static PlayerEntity player = HealthIndicatorsCommon.client.player;
    private static int timeSinceLastDamage = Integer.MAX_VALUE;
    private static LivingEntity attacker;

    public static void markDamageToPlayer(LivingEntity livingEntity){
        timeSinceLastDamage = 0;
        attacker = livingEntity;
    }

    public static void tick(){
        player = HealthIndicatorsCommon.client.player;
        if(timeSinceLastDamage != Integer.MAX_VALUE) timeSinceLastDamage++;
        if (attacker == null || attacker.isDead() || attacker.isRemoved()){
            timeSinceLastDamage = Integer.MAX_VALUE;
            attacker = null;
        }
        if(timeSinceLastDamage == Integer.MAX_VALUE) attacker = null;
    }

    public static void render(DrawContext drawContext, float tickDelta) {
        if (player == null) return;
        if (timeSinceLastDamage <= ModConfig.HANDLER.instance().damage_direction_indicators_visibility_time * 20 && attacker != null) {
            // Get positions and calculate direction
            Vec3d playerPos = player.getPos();
            Vec3d attackerPos = attacker.getPos();
            double deltaX = attackerPos.x - playerPos.x;
            double deltaZ = attackerPos.z - playerPos.z;

            // Calculate yaw to attacker and delta from player's current view
            float yawToAttacker = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
            yawToAttacker = MathHelper.wrapDegrees(yawToAttacker);
            float deltaYaw = MathHelper.wrapDegrees(yawToAttacker - player.getYaw());

            // Get screen center and setup parameters
            int centerX = drawContext.getScaledWindowWidth() / 2;
            int centerY = drawContext.getScaledWindowHeight() / 2;
            float radius = 24.0f;
            float angle = (float) Math.toRadians(deltaYaw);

            // Calculate indicator position
            float indicatorX = centerX + radius * (float) Math.sin(angle);
            float indicatorY = centerY - radius * (float) Math.cos(angle);

            // Calculate fade-out alpha (0-255)
            int alpha = 255;
            if (ModConfig.HANDLER.instance().damage_direction_indicators_fade_out) {
                int fadeDelay = (ModConfig.HANDLER.instance().damage_direction_indicators_visibility_time - ModConfig.HANDLER.instance().damage_direction_indicators_fade_out_time) * 20;
                if (timeSinceLastDamage >= fadeDelay) {
                    float progress = Math.min((timeSinceLastDamage - fadeDelay) / (float) (ModConfig.HANDLER.instance().damage_direction_indicators_fade_out_time * 20), 1.0f);
                    alpha = 255 - (int) (255 * progress);
                }
            }

            float scale = ModConfig.HANDLER.instance().damage_direction_indicators_scale;
            Color color = ModConfig.HANDLER.instance().damage_direction_indicators_color;
            int argb = (alpha << 24) | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();
            
            // Draw a simple triangle directly using DrawContext
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(indicatorX, indicatorY, 0);
            drawContext.getMatrices().multiply(new Quaternionf().rotationZ(angle));
            
            // Draw triangle using line by line 
            int x1 = (int)(-3 * scale);
            int y1 = (int)(4 * scale);
            int x2 = (int)(3 * scale);
            int y2 = (int)(4 * scale); 
            int x3 = (int)(0);
            int y3 = (int)(-4 * scale);
            
            // Fill the triangle by drawing horizontal lines
            for (int y = y3; y <= y1; y++) {
                // Calculate x range at this y coordinate
                float progress = (float)(y - y3) / (y1 - y3);
                int leftX = Math.round(x3 + progress * (x1 - x3));
                int rightX = Math.round(x3 + progress * (x2 - x3));
                
                // Draw horizontal line
                drawContext.fill(leftX, y, rightX + 1, y + 1, argb);
            }
            
            drawContext.getMatrices().pop();
        }
    }
}