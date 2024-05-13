package io.github.adytech99.healthindicators.mixin;

import io.github.adytech99.healthindicators.HeartType;
import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class PlayerEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {


    @Shadow protected M model;

    public PlayerEntityRendererMixin(EntityRendererFactory.Context ctx, PlayerEntityModel<AbstractClientPlayerEntity> model, float shadowRadius) {
        super(ctx);
    }

    @Unique
    BufferBuilder bufferBuilder = null;

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    public void renderHealth(T livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {

        if(!ModConfig.HANDLER.instance().passive_mobs && livingEntity instanceof PassiveEntity) return;
        if(!ModConfig.HANDLER.instance().hostile_mobs && livingEntity instanceof HostileEntity) return;
        if(!ModConfig.HANDLER.instance().players && livingEntity instanceof PlayerEntity) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null && player.getVehicle() != livingEntity && livingEntity != player && !livingEntity.isInvisibleTo(player) && this.model != null) {
            RenderLayer renderLayerEmpty = this.model.getLayer(HeartType.EMPTY.icon);
            RenderLayer renderLayerRedFull = this.model.getLayer(HeartType.RED_FULL.icon);
            RenderLayer renderLayerRedHalf = this.model.getLayer(HeartType.RED_HALF.icon);
            RenderLayer renderLayerYellowFull = this.model.getLayer(HeartType.YELLOW_FULL.icon);
            RenderLayer renderLayerYellowHalf = this.model.getLayer(HeartType.YELLOW_HALF.icon);


            RenderLayer renderLayer = renderLayerEmpty;
            bufferBuilder = (BufferBuilder) vertexConsumerProvider.getBuffer(renderLayer);
            renderLayer.startDrawing();

            double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);

            int healthRed = MathHelper.ceil(livingEntity.getHealth());
            int maxHealth = MathHelper.ceil(livingEntity.getMaxHealth());
            int healthYellow = MathHelper.ceil(livingEntity.getAbsorptionAmount());

            int heartsRed = MathHelper.ceil(healthRed / 2.0F);
            boolean lastRedHalf = (healthRed & 1) == 1;
            int heartsNormal = MathHelper.ceil(maxHealth / 2.0F);
            int heartsYellow = MathHelper.ceil(healthYellow / 2.0F);
            boolean lastYellowHalf = (healthYellow & 1) == 1;
            int heartsTotal = heartsNormal + heartsYellow;


            int heartsPerRow = 10;

            int pixelsTotal = Math.min(heartsTotal, heartsPerRow) * 8 + 1;
            float maxX = pixelsTotal / 2.0f;

            double heartDensity = 50F - (Math.max(4F - Math.ceil(heartsTotal / 10F), -3F) * 5F);

            Matrix4f model = null;
            double h = 0;
            HeartType lastType = null;
            for (int isDrawingEmpty = 0; isDrawingEmpty < 2; isDrawingEmpty++) {
                for (int heart = 0; heart < heartsTotal; heart++) {

                    HeartType type = HeartType.EMPTY;
                    if(isDrawingEmpty != 0) {
                        if (heart < heartsRed) {
                            type = HeartType.RED_FULL;
                            if (heart == heartsRed - 1 && lastRedHalf) {
                                type = HeartType.RED_HALF;
                            }
                        } else if (heart < heartsNormal) {
                            type = HeartType.EMPTY;
                        } else {
                            type = HeartType.YELLOW_FULL;
                            if (heart == heartsTotal - 1 && lastYellowHalf) {
                                type = HeartType.YELLOW_HALF;
                            }
                        }
                    }

                    if (heart % heartsPerRow == 0 || (lastType != type && lastType != null)) {
                        if(heart != 0) {
                            renderLayer.endDrawing();
                            matrixStack.pop();
                        }

                        if(type.equals(HeartType.EMPTY)) {
                            renderLayer = renderLayerEmpty;
                        } else if(type.equals(HeartType.RED_FULL)) {
                            renderLayer = renderLayerRedFull;
                        } else if(type.equals(HeartType.RED_HALF)) {
                            renderLayer = renderLayerRedHalf;
                        } else if(type.equals(HeartType.YELLOW_FULL)) {
                            renderLayer = renderLayerYellowFull;
                        } else if(type.equals(HeartType.YELLOW_HALF)) {
                            renderLayer = renderLayerYellowHalf;
                        }
                        bufferBuilder = (BufferBuilder) vertexConsumerProvider.getBuffer(renderLayer);

                        matrixStack.push();
                        renderLayer.startDrawing();

                        if(heart % heartsPerRow == 0) h = (heart / heartDensity);

                        matrixStack.translate(0, livingEntity.getHeight() + 0.5f + h + (double) ModConfig.HANDLER.instance().heart_offset / 10, 0);
                        if (this.hasLabel(livingEntity) && d <= 4096.0) {
                            matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                            if (d < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                                matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                            }
                        }

                        matrixStack.multiply(this.dispatcher.getRotation());

                        float pixelSize = 0.025F;
                        matrixStack.scale(pixelSize, pixelSize, pixelSize);

                        model = matrixStack.peek().getPositionMatrix();
                    }

                    float x = maxX - (heart % 10) * 8;
                    lastType = type;

                    if(isDrawingEmpty == 0) {
                        drawHeart(model, bufferBuilder, x, light);
                    } else {
                        if (type != HeartType.EMPTY) {
                            drawHeart(model, bufferBuilder, x, light);
                        }
                    }
                }
                renderLayer.endDrawing();
                matrixStack.pop();
            }
        }
    }

    @Unique
    private static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, int light) {
        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;

        float heartSize = 9F;

        drawVertex(model, vertexConsumer, x, 0F - heartSize, minU, maxV, light);
        drawVertex(model, vertexConsumer, x - heartSize, 0F - heartSize, maxU, maxV, light);
        drawVertex(model, vertexConsumer, x - heartSize, 0F, maxU, minV, light);
        drawVertex(model, vertexConsumer, x, 0F, minU, minV, light);
    }

    @Unique
    private static void drawVertex(Matrix4f model, VertexConsumer vertices, float x, float y, float u, float v, int light) {
        vertices.vertex(model, x, y, 0.0F).color(1F, 1F, 1F, 1F).texture(u, v).overlay(0, 10).light(light).normal(x, y, 0.0F).next();
    }
}