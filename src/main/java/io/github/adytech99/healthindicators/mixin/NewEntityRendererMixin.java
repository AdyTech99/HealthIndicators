package io.github.adytech99.healthindicators.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.adytech99.healthindicators.Config;
import io.github.adytech99.healthindicators.HeartType;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.util.FilterConfig;
import io.github.adytech99.healthindicators.util.HitTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class NewEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    protected NewEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    public void renderHealth(T livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (shouldRenderHearts(player, livingEntity)) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder vertexConsumer = tessellator.getBuffer();

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


            int heartsPerRow = ModConfig.HANDLER.instance().hearts_per_row;

            int pixelsTotal = Math.min(heartsTotal, heartsPerRow) * 8 + 1;
            float maxX = pixelsTotal / 2.0f;

            double heartDensity = 50F - (Math.max(4F - Math.ceil((double) heartsTotal / heartsPerRow), -3F) * 5F);
            double h = 0;

            for (int isDrawingEmpty = 0; isDrawingEmpty < 2; isDrawingEmpty++) {
                for (int heart = 0; heart < heartsTotal; heart++) {
                    if (heart % heartsPerRow == 0) {
                        h = heart / heartDensity;
                    }

                    matrixStack.push();
                    vertexConsumer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

                    matrixStack.translate(0, livingEntity.getHeight() + 0.5f + h, 0);
                    if (this.hasLabel(livingEntity) && d <= 4096.0) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                        if (d < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(0) != null) {
                            matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
                        }
                    }

                    matrixStack.multiply(this.dispatcher.getRotation());

                    float pixelSize = 0.025F;
                    matrixStack.scale(pixelSize, pixelSize, pixelSize);
                    matrixStack.translate(0, ModConfig.HANDLER.instance().heart_offset, 0);

                    Matrix4f model = matrixStack.peek().getPositionMatrix();


                    float x = maxX - (heart % heartsPerRow) * 8;

                    if (isDrawingEmpty == 0) {
                        drawHeart(model, vertexConsumer, x, HeartType.EMPTY);
                    } else {
                        HeartType type;
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
                        if (type != HeartType.EMPTY) {
                            drawHeart(model, vertexConsumer, x, type);
                        }
                    }
                    tessellator.draw();
                    matrixStack.pop();
                }
            }
        }
    }

    @Unique
    private static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, HeartType type) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, type.icon);
        RenderSystem.enableDepthTest();

        float minU = 0F;
        float maxU = 1F;
        float minV = 0F;
        float maxV = 1F;

        float heartSize = 9F;

        drawVertex(model, vertexConsumer, x, 0F - heartSize, minU, maxV);
        drawVertex(model, vertexConsumer, x - heartSize, 0F - heartSize, maxU, maxV);
        drawVertex(model, vertexConsumer, x - heartSize, 0F, maxU, minV);
        drawVertex(model, vertexConsumer, x, 0F, minU, minV);
    }

    @Unique
    private static void drawVertex(Matrix4f model, VertexConsumer vertices, float x, float y, float u, float v) {
        vertices.vertex(model, x, y, 0.0F).texture(u, v).next();
    }

    @Unique
    private boolean shouldRenderHearts(ClientPlayerEntity player, LivingEntity livingEntity){
        if(!FilterConfig.isAllowed(livingEntity, player)) return false;
        if(!HitTracker.isInDamagedEntities(livingEntity) && ModConfig.HANDLER.instance().on_hit && livingEntity != player) return false;
        if(livingEntity.getHealth() != livingEntity.getMaxHealth() && ModConfig.HANDLER.instance().damaged_only && livingEntity.getAbsorptionAmount() <= 0 && livingEntity != player) return false;
        return player != null
                && Config.getRenderingEnabled()
                && player.getVehicle() != livingEntity
                && !livingEntity.isInvisibleTo(player);
    }
}
