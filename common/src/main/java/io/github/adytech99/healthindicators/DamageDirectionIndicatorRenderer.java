package io.github.adytech99.healthindicators;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.platform.Mod;
import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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

    public static void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
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


            // Draw directional wedge
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(indicatorX, indicatorY, 0);

            // Apply rotation to face the attacker
            drawContext.getMatrices().multiply(new Quaternionf().rotationZ(angle));

            // Define wedge shape as a triangle (tip at 0,-4; base at -3,4 and 3,4)
            Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

            // Set up rendering for transparent color
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

            float scale = ModConfig.HANDLER.instance().damage_direction_indicators_scale;
            // Tip of the wedge (top center)
            //buffer.vertex(matrix, 0 * scale, -4 * scale, 0).color(255, 0, 0, alpha);
            buffer.vertex(matrix, 0 * scale, -4 * scale, 0).color(ModConfig.HANDLER.instance().damage_direction_indicators_color.getRed(), ModConfig.HANDLER.instance().damage_direction_indicators_color.getGreen(), ModConfig.HANDLER.instance().damage_direction_indicators_color.getBlue(), alpha);
            // Left base point
            buffer.vertex(matrix, -3 * scale, 4 * scale, 0).color(ModConfig.HANDLER.instance().damage_direction_indicators_color.getRed(), ModConfig.HANDLER.instance().damage_direction_indicators_color.getGreen(), ModConfig.HANDLER.instance().damage_direction_indicators_color.getBlue(), alpha);
            // Right base point
            buffer.vertex(matrix, 3 * scale, 4 * scale, 0).color(ModConfig.HANDLER.instance().damage_direction_indicators_color.getRed(), ModConfig.HANDLER.instance().damage_direction_indicators_color.getGreen(), ModConfig.HANDLER.instance().damage_direction_indicators_color.getBlue(), alpha);

            BuiltBuffer builtBuffer;
            try {
                builtBuffer = buffer.endNullable();
                if(builtBuffer != null){
                    BufferRenderer.drawWithGlobalProgram(builtBuffer);
                    builtBuffer.close();
                }
            }
            catch (Exception e){
                // F off
            }
            RenderSystem.disableBlend();
            drawContext.getMatrices().pop();
        }
    }
}