package io.github.adytech99.healthindicators;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
        if(timeSinceLastDamage != Integer.MAX_VALUE) timeSinceLastDamage++;
        if (attacker == null) timeSinceLastDamage = Integer.MAX_VALUE;
    }

    public static void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (player == null) return;

        if (timeSinceLastDamage <= 80 && attacker != null) {
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

            // Corrected calculation of indicator position:
            // We add on the sin for X so that a positive angle moves the indicator to the right.
            float indicatorX = centerX + radius * (float) Math.sin(angle);
            float indicatorY = centerY - radius * (float) Math.cos(angle);

            // Calculate fade-out alpha (0-255)
            int timeSinceHit = timeSinceLastDamage;
            int alpha = 255 - (int) (255 * (timeSinceHit / 80.0f));

            // Draw directional chevron using transformed coordinates
            drawContext.getMatrices().push();
            drawContext.getMatrices().translate(indicatorX, indicatorY, 0);

            // Correctly create and apply rotation matrix
            Matrix4f rotationMatrix = new Matrix4f().rotationZ(angle);
            drawContext.getMatrices().multiply(new Quaternionf().rotationZ(angle));

            // Chevron shape coordinates (rotated to face outward)
            int[] xPoints = {0, -3, 3};
            int[] yPoints = {-4, 4, 4};

            // Draw filled chevron with fading red color
            for (int i = 0; i < xPoints.length - 1; i++) {
                drawContext.fill(RenderLayer.getGui(),
                        xPoints[i], yPoints[i],
                        xPoints[i + 1], yPoints[i + 1],
                        (alpha << 24) | 0x00FF0000); // ARGB red with alpha
            }

            drawContext.getMatrices().pop();
        }

    }
}