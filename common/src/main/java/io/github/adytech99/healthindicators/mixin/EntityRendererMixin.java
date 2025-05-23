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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

    /**
     * Checks if there's a block between the player's camera and the entity
     * @param entity The entity to check
     * @return true if a block is blocking the view, false otherwise
     */
    @Unique
    private boolean isEntityObstructedByBlocks(LivingEntity entity) {
        if (client.player == null || client.world == null) return false;
        
        // Get the camera and entity positions
        Vec3d cameraPos = client.player.getCameraPosVec(1.0f);
        // Target the entity's position plus some height offset to aim at the health indicator position
        Vec3d entityPos = entity.getPos().add(0, entity.getHeight() + 0.5f, 0); 
        
        // Perform raycast from player to entity
        HitResult hitResult = client.player.raycast(200.0, 1.0f, false);
        
        // If we hit a block before reaching the entity's position, view is obstructed
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            double distanceToHit = hitResult.getPos().squaredDistanceTo(cameraPos);
            double distanceToEntity = entityPos.squaredDistanceTo(cameraPos);
            return distanceToHit < distanceToEntity;
        }
        
        return false;
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

        // Check if entity is obstructed by blocks
        boolean isObstructed = ModConfig.HANDLER.instance().show_through_walls; // && isEntityObstructedByBlocks(livingEntity);

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
                        
                    // Get vertex consumer for this specific texture with appropriate render layer
                    RenderLayer renderLayer;
                    if (isObstructed) {
                        // Use a render layer that ignores depth testing when show_through_walls is enabled and view is obstructed
                        renderLayer = RenderLayer.getTextSeeThrough(heartTextureId);
                    } else {
                        // Use normal text render layer
                        renderLayer = RenderLayer.getText(heartTextureId);
                    }
                    
                    VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
                    
                    // Apply correct opacity
                    float opacity = ModConfig.HANDLER.instance().health_bar_opacity / 100.0F;
                    float minU = 0F;
                    float maxU = 1F;
                    float minV = 0F;
                    float maxV = 1F;
                    float heartSize = 9F;
                    
                    vertexConsumer.vertex(model, x, 0F - heartSize, 0.0F).texture(minU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                    vertexConsumer.vertex(model, x - heartSize, 0F - heartSize, 0.0F).texture(maxU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                    vertexConsumer.vertex(model, x - heartSize, 0F, 0.0F).texture(maxU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                    vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                
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
                            
                        // Get vertex consumer for this specific texture with appropriate render layer
                        RenderLayer renderLayer;
                        if (isObstructed) {
                            // Use a render layer that ignores depth testing when view is obstructed
                            renderLayer = RenderLayer.getTextSeeThrough(heartTextureId);
                        } else {
                            // Use normal text render layer
                            renderLayer = RenderLayer.getText(heartTextureId);
                        }
                        
                        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
                        
                        // Apply correct opacity
                        float opacity = ModConfig.HANDLER.instance().health_bar_opacity / 100.0F;
                        float minU = 0F;
                        float maxU = 1F;
                        float minV = 0F;
                        float maxV = 1F;
                        float heartSize = 9F;
                        
                        vertexConsumer.vertex(model, x, 0F - heartSize, 0.0F).texture(minU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                        vertexConsumer.vertex(model, x - heartSize, 0F - heartSize, 0.0F).texture(maxU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                        vertexConsumer.vertex(model, x - heartSize, 0F, 0.0F).texture(maxU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                        vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
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

        // Check if entity is obstructed by blocks
        boolean isObstructed = ModConfig.HANDLER.instance().show_through_walls;

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

        // Use a different text layer type when show_through_walls is enabled and entity is obstructed
        TextRenderer.TextLayerType textLayerType = isObstructed ? 
            TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL;
            
        int backgroundColor = ModConfig.HANDLER.instance().render_number_display_background_color ? 
            ModConfig.HANDLER.instance().number_display_background_color.getRGB() : 0;
            
        // Apply opacity based on health_bar_opacity
        int textColor = ModConfig.HANDLER.instance().number_color.getRGB();

        int opacity = ModConfig.HANDLER.instance().health_bar_opacity;
        textColor = (textColor & 0x00FFFFFF) | ((opacity * 255 / 100) << 24);

        
        textRenderer.draw(healthText, x, 0, textColor, 
            ModConfig.HANDLER.instance().render_number_display_shadow, model, 
            vertexConsumerProvider, textLayerType, backgroundColor, light);
            
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

        // Check if entity is obstructed by blocks
        boolean isObstructed = ModConfig.HANDLER.instance().show_through_walls;

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

                ArmorTypeEnum type = null;
                if (isDrawingEmpty == 0) {
                    type = ArmorTypeEnum.EMPTY;
                } else if (pointCount < armorPoints) {
                    type = ArmorTypeEnum.FULL;
                    if (pointCount == armorPoints - 1 && lastPointHalf) {
                        type = ArmorTypeEnum.HALF;
                    }
                }
                
                // Only proceed with rendering if we have a valid type
                if (type != null) {
                    Identifier armorTextureId = type.icon;
    
                    // Get vertex consumer for this specific texture with appropriate render layer
                    RenderLayer renderLayer;
                    if (isObstructed) {
                        // Use a render layer that ignores depth testing when show_through_walls is enabled and view is obstructed
                        renderLayer = RenderLayer.getTextSeeThrough(armorTextureId);
                    } else {
                        // Use normal text render layer
                        renderLayer = RenderLayer.getText(armorTextureId);
                    }
                    
                    VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
                    
                    // Apply correct opacity
                    float opacity = ModConfig.HANDLER.instance().health_bar_opacity / 100.0F;
                    float minU = 0F;
                    float maxU = 1F;
                    float minV = 0F;
                    float maxV = 1F;
                    float armorSize = 9F;
                    
                    vertexConsumer.vertex(model, x, 0F - armorSize, 0.0F).texture(minU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                    vertexConsumer.vertex(model, x - armorSize, 0F - armorSize, 0.0F).texture(maxU, maxV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                    vertexConsumer.vertex(model, x - armorSize, 0F, 0.0F).texture(maxU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);
                    vertexConsumer.vertex(model, x, 0F, 0.0F).texture(minU, minV).light(15728880).color(1.0F, 1.0F, 1.0F, opacity);

                }

                matrixStack.pop();
            }
        }
    }
}
