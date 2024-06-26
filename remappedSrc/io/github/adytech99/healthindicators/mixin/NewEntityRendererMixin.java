package io.github.adytech99.healthindicators.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.adytech99.healthindicators.enums.HeartTypeEnum;
import io.github.adytech99.healthindicators.RenderTracker;
import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.enums.HealthDisplayTypeEnum;
import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntityRenderer.class)
public abstract class NewEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {

    @Unique
    private final MinecraftClient client = MinecraftClient.getInstance();
    protected NewEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    public void render(T livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {

        if (RenderTracker.isInUUIDS(livingEntity) || (Config.getOverrideAllFiltersEnabled() && !RenderTracker.isInvalid(livingEntity))) {
            if((ModConfig.HANDLER.instance().looking_at && !RenderTracker.isTargeted(livingEntity))
                    && (!Config.getOverrideAllFiltersEnabled() && !(livingEntity instanceof PlayerEntity && ModConfig.HANDLER.instance().override_players))
                    && livingEntity != client.player) return;

            if(ModConfig.HANDLER.instance().indicator_type == HealthDisplayTypeEnum.HEARTS) renderHearts(livingEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);
            else if(ModConfig.HANDLER.instance().indicator_type == HealthDisplayTypeEnum.NUMBER) renderNumber(livingEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);;

        }
    }

    @Unique private void renderHearts(T livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light){
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer;

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
                vertexConsumer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                float pixelSize = 0.025F;

                matrixStack.translate(0, livingEntity.getHeight() + 0.5f + h, 0);
                if ((this.hasLabel(livingEntity)
                        || (ModConfig.HANDLER.instance().force_higher_offset_for_players && livingEntity instanceof PlayerEntity && livingEntity != client.player))
                        && d <= 4096.0) {
                    matrixStack.translate(0.0D, 9.0F * 1.15F * pixelSize, 0.0D);
                    if (d < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * pixelSize, 0.0D);
                    }
                }

                matrixStack.multiply(this.dispatcher.getRotation());
                matrixStack.scale(pixelSize, pixelSize, pixelSize);
                matrixStack.translate(0, ModConfig.HANDLER.instance().display_offset, 0);
                Matrix4f model = matrixStack.peek().getPositionMatrix();

                float x = maxX - (heart % heartsPerRow) * 8;

                if (isDrawingEmpty == 0) {
                    drawHeart(model, vertexConsumer, x, HeartTypeEnum.EMPTY);
                } else {
                    HeartTypeEnum type;
                    if (heart < heartsRed) {
                        type = HeartTypeEnum.RED_FULL;
                        if (heart == heartsRed - 1 && lastRedHalf) {
                            type = HeartTypeEnum.RED_HALF;
                        }
                    } else if (heart < heartsNormal) {
                        type = HeartTypeEnum.EMPTY;
                    } else {
                        type = HeartTypeEnum.YELLOW_FULL;
                        if (heart == heartsTotal - 1 && lastYellowHalf) {
                            type = HeartTypeEnum.YELLOW_HALF;
                        }
                    }
                    if (type != HeartTypeEnum.EMPTY) {
                        drawHeart(model, vertexConsumer, x, type);
                    }
                }
                //tessellator.draw();
                BufferRenderer.drawWithGlobalProgram(vertexConsumer.end());
                matrixStack.pop();
            }
        }
    }


    @Unique
    private void renderNumber(T livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light){
        double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);
        int health = MathHelper.ceil(livingEntity.getHealth());
        int maxHealth = MathHelper.ceil(livingEntity.getMaxHealth());
        int absorption = MathHelper.ceil(livingEntity.getAbsorptionAmount());

        String healthText = health+absorption + " / " + maxHealth;

        matrixStack.push();
        float scale = 0.025F;

        matrixStack.translate(0, livingEntity.getHeight() + 0.5f, 0);
        if ((this.hasLabel(livingEntity)
                || (ModConfig.HANDLER.instance().force_higher_offset_for_players && livingEntity instanceof PlayerEntity && livingEntity != client.player))
                && d <= 4096.0) {
            matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
            if (d < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
            }
        }

        matrixStack.multiply(this.dispatcher.getRotation());
        matrixStack.scale(-scale, -scale, scale);
        matrixStack.translate(0, -ModConfig.HANDLER.instance().display_offset, 0);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float x = -textRenderer.getWidth(healthText) / 2.0f;
        Matrix4f model = matrixStack.peek().getPositionMatrix();

        textRenderer.draw(healthText, x, 0, ModConfig.HANDLER.instance().number_color.getRGB(), ModConfig.HANDLER.instance().render_number_display_shadow, model, vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, ModConfig.HANDLER.instance().render_number_display_background_color ? ModConfig.HANDLER.instance().number_display_background_color.getRGB() : 0, light);
        matrixStack.pop();
    }

    @Unique
    private static void drawHeart(Matrix4f model, VertexConsumer vertexConsumer, float x, HeartTypeEnum type) {
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
        vertices.vertex(model, x, y, 0.0F).texture(u, v);
        //vertices.vertex(model, x, y, 0.0F).texture(u, v).next();
    }
}
