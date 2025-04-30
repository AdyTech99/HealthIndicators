package io.github.adytech99.healthindicators.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.adytech99.healthindicators.HealthIndicatorsCommon;
import io.github.adytech99.healthindicators.Renderer;
import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.enums.ArmorTypeEnum;
import io.github.adytech99.healthindicators.enums.HealthDisplayTypeEnum;
import io.github.adytech99.healthindicators.enums.HeartTypeEnum;
import io.github.adytech99.healthindicators.RenderTracker;
import io.github.adytech99.healthindicators.util.HeartJumpData;
import io.github.adytech99.healthindicators.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.adytech99.healthindicators.util.RenderUtils.drawArmor;
import static io.github.adytech99.healthindicators.util.RenderUtils.drawHeart;
import static io.github.adytech99.healthindicators.enums.HeartTypeEnum.addHardcoreIcon;
import static io.github.adytech99.healthindicators.enums.HeartTypeEnum.addStatusIcon;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends EntityRenderer<T, S>
        implements FeatureRendererContext<S, M> {

    @Unique private LivingEntity mainLivingEntityThing;
    @Unique private final MinecraftClient client = MinecraftClient.getInstance();
    @Unique private static final Identifier ICONS_TEXTURE = Identifier.of("minecraft", "textures/gui/icons.png");
    
    protected EntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void updateRenderState(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci){
        mainLivingEntityThing = livingEntity;
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
    public void render(S livingEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (mainLivingEntityThing != null && (RenderTracker.isInUUIDS(mainLivingEntityThing) || (Config.getOverrideAllFiltersEnabled() && !RenderTracker.isInvalid(mainLivingEntityThing)))) {
            if(Config.getHeartsRenderingEnabled() || Config.getOverrideAllFiltersEnabled()) {
                if (ModConfig.HANDLER.instance().indicator_type == HealthDisplayTypeEnum.HEARTS)
                    renderHearts(mainLivingEntityThing, livingEntityRenderState.bodyYaw, 0, matrixStack, vertexConsumerProvider, i);
                else if (ModConfig.HANDLER.instance().indicator_type == HealthDisplayTypeEnum.NUMBER)
                    renderNumber(mainLivingEntityThing, livingEntityRenderState.bodyYaw, 0, matrixStack, vertexConsumerProvider, i);
                else if (ModConfig.HANDLER.instance().indicator_type == HealthDisplayTypeEnum.DYNAMIC) {
                    if (mainLivingEntityThing.getMaxHealth() > 100)
                        renderNumber(mainLivingEntityThing, livingEntityRenderState.bodyYaw, 0, matrixStack, vertexConsumerProvider, i);
                    else renderHearts(mainLivingEntityThing, livingEntityRenderState.bodyYaw, 0, matrixStack, vertexConsumerProvider, i);
                }
            }
            if(Config.getArmorRenderingEnabled() || Config.getOverrideAllFiltersEnabled()) renderArmorPoints(mainLivingEntityThing, livingEntityRenderState.bodyYaw, 0, matrixStack, vertexConsumerProvider, i);
        }
    }

    @Unique private void renderHearts(LivingEntity livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light){
        double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);

        int healthRed = MathHelper.ceil(livingEntity.getHealth());
        int maxHealth = MathHelper.ceil(livingEntity.getMaxHealth());
        int healthYellow = MathHelper.ceil(livingEntity.getAbsorptionAmount());

        if(ModConfig.HANDLER.instance().percentage_based_health) {
            healthRed = MathHelper.ceil(((float) healthRed /maxHealth) * ModConfig.HANDLER.instance().max_health);
            maxHealth = MathHelper.ceil(ModConfig.HANDLER.instance().max_health);
            healthYellow = MathHelper.ceil(livingEntity.getAbsorptionAmount());
        }

        int heartsRed = MathHelper.ceil(healthRed / 2.0F);
        boolean lastRedHalf = (healthRed & 1) == 1;
        int heartsNormal = MathHelper.ceil(maxHealth / 2.0F);
        int heartsYellow = MathHelper.ceil(healthYellow / 2.0F);
        boolean lastYellowHalf = (healthYellow & 1) == 1;
        int heartsTotal = heartsNormal + heartsYellow;

        int heartsPerRow = ModConfig.HANDLER.instance().icons_per_row;
        int pixelsTotal = Math.min(heartsTotal, heartsPerRow) * 8 + 1;
        float maxX = pixelsTotal / 2.0f;

        float scale = ModConfig.HANDLER.instance().size;

        double heartDensity = 50F - (Math.max(4F - Math.ceil((double) heartsTotal / heartsPerRow), -3F) * 5F);
        double h = 0;

        for (int isDrawingEmpty = 0; isDrawingEmpty < 2; isDrawingEmpty++) {
            for (int heart = 0; heart < heartsTotal; heart++) {
                if (heart % heartsPerRow == 0) {
                    h = heart / heartDensity;
                }

                matrixStack.push();
                matrixStack.translate(0, livingEntity.getHeight() + 0.5f + h, 0);

                if (livingEntity.hasStatusEffect(StatusEffects.REGENERATION) && ModConfig.HANDLER.instance().show_heart_effects) {
                    if(HeartJumpData.getWhichHeartJumping(livingEntity) == heart){
                        matrixStack.translate(0.0D, 1.15F * scale, 0.0D);
                    }
                }

                if ((this.hasLabel((T) livingEntity, d)
                        || (ModConfig.HANDLER.instance().force_higher_offset_for_players && livingEntity instanceof PlayerEntity && livingEntity != client.player))
                        && d <= 4096.0) {
                    matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    if (d < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(net.minecraft.scoreboard.ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    }
                }

                matrixStack.multiply(this.dispatcher.getRotation());
                matrixStack.scale(-scale, scale, scale);
                matrixStack.translate(0, ModConfig.HANDLER.instance().display_offset, 0);
                Matrix4f model = matrixStack.peek().getPositionMatrix();

                float x = maxX - (heart % heartsPerRow) * 8;

                if (isDrawingEmpty == 0) {
                    // Create heart texture identifier
                    String additionalIconEffects = "";
                    HeartTypeEnum type = HeartTypeEnum.EMPTY;
                    Identifier heartTextureId = ModConfig.HANDLER.instance().use_vanilla_textures ? 
                        Identifier.of("healthindicators", "textures/gui/heart/" + additionalIconEffects + type.icon + ".png") :
                        Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + additionalIconEffects + type.icon + ".png");
                        
                    // Get vertex consumer for this specific texture
                    VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
                        RenderLayer.getText(heartTextureId)
                    );
                    drawHeart(model, vertexConsumer, x, type, livingEntity);
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
                        // Create heart texture identifier with effects
                        String additionalIconEffects = "";
                        if(type != HeartTypeEnum.YELLOW_FULL && type != HeartTypeEnum.YELLOW_HALF && type != HeartTypeEnum.EMPTY && ModConfig.HANDLER.instance().show_heart_effects) {
                            additionalIconEffects = (addStatusIcon(livingEntity) + addHardcoreIcon(livingEntity));
                        }
                        
                        Identifier heartTextureId = ModConfig.HANDLER.instance().use_vanilla_textures ? 
                            Identifier.of("healthindicators", "textures/gui/heart/" + additionalIconEffects + type.icon + ".png") :
                            Identifier.of("minecraft", "textures/gui/sprites/hud/heart/" + additionalIconEffects + type.icon + ".png");
                            
                        // Get vertex consumer for this specific texture
                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
                            RenderLayer.getText(heartTextureId)
                        );
                        drawHeart(model, vertexConsumer, x, type, livingEntity);
                    }
                }

                matrixStack.pop();
            }
        }
    }

    @Unique
    private void renderNumber(LivingEntity livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light){
        double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);
        String healthText = RenderUtils.getHealthText(livingEntity);

        matrixStack.push();
        float scale = ModConfig.HANDLER.instance().size;

        matrixStack.translate(0, livingEntity.getHeight() + 0.5f, 0);
        if ((this.hasLabel((T) livingEntity, d)
                || (ModConfig.HANDLER.instance().force_higher_offset_for_players && livingEntity instanceof PlayerEntity && livingEntity != client.player))
                && d <= 4096.0) {
            matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
            if (d < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(net.minecraft.scoreboard.ScoreboardDisplaySlot.BELOW_NAME) != null) {
                matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
            }
        }

        matrixStack.multiply(this.dispatcher.getRotation());
        matrixStack.scale(scale, -scale, scale);
        matrixStack.translate(0, -ModConfig.HANDLER.instance().display_offset, 0);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float x = -textRenderer.getWidth(healthText) / 2.0f;
        Matrix4f model = matrixStack.peek().getPositionMatrix();

        textRenderer.draw(healthText, x, 0, ModConfig.HANDLER.instance().number_color.getRGB(), ModConfig.HANDLER.instance().render_number_display_shadow, model, vertexConsumerProvider, TextRenderer.TextLayerType.NORMAL, ModConfig.HANDLER.instance().render_number_display_background_color ? ModConfig.HANDLER.instance().number_display_background_color.getRGB() : 0, light);
        matrixStack.pop();
    }


    @Unique private void renderArmorPoints(LivingEntity livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light){
        double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);

        int armor = MathHelper.ceil(livingEntity.getArmor());
        int maxArmor = MathHelper.ceil(livingEntity.getArmor());

        if(maxArmor == 0) return;

        int armorPoints = MathHelper.ceil(armor / 2.0F);
        boolean lastPointHalf = (armor & 1) == 1;
        int pointsNormal = MathHelper.ceil(maxArmor / 2.0F);
        int pointsTotal = 10;

        int pointsPerRow = ModConfig.HANDLER.instance().icons_per_row;
        int pixelsTotal = Math.min(pointsTotal, pointsPerRow) * 8 + 1;
        float maxX = pixelsTotal / 2.0f;
        float scale = ModConfig.HANDLER.instance().size;


        double pointDensity = 50F - (Math.max(4F - Math.ceil((double) pointsTotal / pointsPerRow), -3F) * 5F);
        double h = 0;

        for (int isDrawingEmpty = 0; isDrawingEmpty < 2; isDrawingEmpty++) {
            for (int pointCount = 0; pointCount < pointsTotal; pointCount++) {
                if (pointCount % pointsPerRow == 0) {
                    h = (scale*10)*((pointCount/2 + pointsPerRow - 1) / pointsPerRow);
                }

                matrixStack.push();
                int extraHeight = (int) (((livingEntity.getMaxHealth() + livingEntity.getAbsorptionAmount())/2 + pointsPerRow - 1) / pointsPerRow);
                matrixStack.translate(0, livingEntity.getHeight() + 0.75f + (scale*10)*(extraHeight-1) + h, 0);
                if ((this.hasLabel((T) livingEntity, d)
                        || (ModConfig.HANDLER.instance().force_higher_offset_for_players && livingEntity instanceof PlayerEntity && livingEntity != client.player))
                        && d <= 4096.0) {
                    matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    if (d < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(net.minecraft.scoreboard.ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    }
                }

                matrixStack.multiply(this.dispatcher.getRotation());
                matrixStack.scale(-scale, scale, scale);
                matrixStack.translate(0, ModConfig.HANDLER.instance().display_offset, 0);
                Matrix4f model = matrixStack.peek().getPositionMatrix();

                float x = maxX - (pointCount % pointsPerRow) * 8;

                if (isDrawingEmpty == 0) {
                    // Get the correct armor texture identifier
                    ArmorTypeEnum type = ArmorTypeEnum.EMPTY;
                    Identifier armorIcon = ModConfig.HANDLER.instance().use_vanilla_textures ? type.vanillaIcon : type.icon;
                    
                    // Get vertex consumer for this specific texture
                    VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
                        RenderLayer.getText(armorIcon)
                    );
                    drawArmor(model, vertexConsumer, x, type);
                } else {
                    ArmorTypeEnum type = null;
                    if (pointCount < armorPoints) {
                        type = ArmorTypeEnum.FULL;
                        if (pointCount == armorPoints - 1 && lastPointHalf) {
                            type = ArmorTypeEnum.HALF;
                        }
                    } else if (pointCount < pointsNormal) {
                        type = ArmorTypeEnum.EMPTY;
                    }
                    if(type != null) {
                        // Get the correct armor texture identifier
                        Identifier armorIcon = ModConfig.HANDLER.instance().use_vanilla_textures ? type.vanillaIcon : type.icon;
                        
                        // Get vertex consumer for this specific texture
                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
                            RenderLayer.getText(armorIcon)
                        );
                        drawArmor(model, vertexConsumer, x, type);
                    }
                }

                matrixStack.pop();
            }
        }
    }
}
