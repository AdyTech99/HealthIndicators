package io.github.adytech99.healthindicators;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.adytech99.healthindicators.config.ModConfig;
import io.github.adytech99.healthindicators.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.awt.*;


public class HudRenderer {

    //public static DrawContext drawContext;
    //public static RenderTickCounter renderTickCounter;
    private static final Random random = Random.create();

    /*public static void onHudRender(LivingEntity livingEntity, double squaredDistanceToCamera, boolean hasLabel, Quaternionf rotation) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexConsumer;
        MatrixStack matrixStack = drawContext.getMatrices();

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

        for (int isDrawingEmpty = 0; isDrawingEmpty < 2; isDrawingEmpty++) {
            for (int heart = 0; heart < heartsTotal; heart++) {
                matrixStack.push();
                float scale = ModConfig.HANDLER.instance().size;
                vertexConsumer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

                //matrixStack.translate(0, livingEntity.getHeight() + 0.5f + h, 0);

                if (livingEntity.hasStatusEffect(StatusEffects.REGENERATION) && ModConfig.HANDLER.instance().show_heart_effects) {
                    if(HeartJumpData.getWhichHeartJumping(livingEntity) == heart){
                        //matrixStack.translate(0.0D, 1.15F * scale, 0.0D);
                    }
                }

                if ((hasLabel
                        || (ModConfig.HANDLER.instance().force_higher_offset_for_players && livingEntity instanceof PlayerEntity && livingEntity != MinecraftClient.getInstance().player))
                        && squaredDistanceToCamera <= 4096.0) {
                    //matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    if (squaredDistanceToCamera < 100.0 && livingEntity instanceof PlayerEntity && livingEntity.getEntityWorld().getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME) != null) {
                        //matrixStack.translate(0.0D, 9.0F * 1.15F * scale, 0.0D);
                    }
                }

                //matrixStack.multiply(rotation);
                if(MinecraftClient.getInstance().player != null) matrixStack.multiply(MinecraftClient.getInstance().player.getFacing().getRotationQuaternion());
                matrixStack.scale(-scale, scale, scale);
                //matrixStack.translate(0, ModConfig.HANDLER.instance().display_offset, 0);
                Matrix4f model = matrixStack.peek().getPositionMatrix();

                float x = maxX - (heart % heartsPerRow) * 8;

                if (isDrawingEmpty == 0) {
                    drawHeart(model, vertexConsumer, x, HeartTypeEnum.EMPTY, livingEntity);
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
                        drawHeart(model, vertexConsumer, x, type, livingEntity);
                    }
                }

                BuiltBuffer builtBuffer;
                try {
                    builtBuffer = vertexConsumer.endNullable();
                    if(builtBuffer != null){
                        BufferRenderer.drawWithGlobalProgram(builtBuffer);
                        builtBuffer.close();
                    }
                }
                catch (Exception e){
                    // F off
                }
                matrixStack.pop();
            }
        }
    }*/


    public static void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter){
        drawNumberHealthGUIIndicator(RenderTracker.getTrackedEntity(), ModConfig.HANDLER.instance().number_color, 20, 20, ModConfig.HANDLER.instance().render_number_display_shadow, drawContext);
    }


    /*public static void renderHealthBar(LivingEntity livingEntity, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking) {
        HudHeartType heartType = HudHeartType.fromEntityState(livingEntity);
        boolean bl = livingEntity.getWorld().getLevelProperties().isHardcore();
        int i = MathHelper.ceil((double)maxHealth / 2.0);
        int j = MathHelper.ceil((double)absorption / 2.0);
        int k = i * 2;

        for(int l = i + j - 1; l >= 0; --l) {
            int m = l / 10;
            int n = l % 10;

            int x = drawContext.getScaledWindowWidth() / 2 - 91;
            int y = drawContext.getScaledWindowHeight() - 39;

            int o = x + n * 8;
            int p = y - m * lines;
            //int o = 10;
            //int p = -m * lines;
            //int p = 10;
            if (lastHealth + absorption <= 4) {
                p += random.nextInt(2);
            }

            if (l < i && l == regeneratingHeartIndex) {
                p -= 2;
            }

            drawHeart(drawContext, HudHeartType.CONTAINER, o, p, bl, blinking, false);
            int q = l * 2;
            boolean bl2 = l >= i;
            if (bl2) {
                int r = q - k;
                if (r < absorption) {
                    boolean bl3 = r + 1 == absorption;
                    drawHeart(drawContext, heartType == HudHeartType.WITHERED ? heartType : HudHeartType.ABSORBING, o, p, bl, false, bl3);
                }
            }

            boolean bl4;
            if (blinking && q < health) {
                bl4 = q + 1 == health;
                drawHeart(drawContext, heartType, o, p, bl, true, bl4);
            }
            if (q < lastHealth) {
                bl4 = q + 1 == lastHealth;
                drawHeart(drawContext, heartType, o, p, bl, false, bl4);
            }
        }

    }*/

    private static void drawHeart(DrawContext context, HudHeartType type, int x, int y, boolean hardcore, boolean blinking, boolean half) {
        RenderSystem.enableBlend();
        context.drawGuiTexture(type.getTexture(hardcore, half, blinking), 50, 50, 9, 9);
        RenderSystem.disableBlend();
    }

    public static void drawNumberHealthGUIIndicator(LivingEntity livingEntity, Color textColor, int x, int y, boolean shadow, DrawContext drawContext){
        String name = String.valueOf(livingEntity.getCustomName() != null ? livingEntity.getCustomName().getLiteralString() : livingEntity.getDisplayName().getString());
        drawContext.drawText(MinecraftClient.getInstance().textRenderer, name, x, y, textColor.getRGB(), shadow);
        drawContext.drawText(MinecraftClient.getInstance().textRenderer, RenderUtils.getHealthText(livingEntity), x, y+10, textColor.getRGB(), shadow);
    }

    enum HudHeartType {
        CONTAINER(Identifier.ofVanilla("hud/heart/container"), Identifier.ofVanilla("hud/heart/container_blinking"), Identifier.ofVanilla("hud/heart/container"), Identifier.ofVanilla("hud/heart/container_blinking"), Identifier.ofVanilla("hud/heart/container_hardcore"), Identifier.ofVanilla("hud/heart/container_hardcore_blinking"), Identifier.ofVanilla("hud/heart/container_hardcore"), Identifier.ofVanilla("hud/heart/container_hardcore_blinking")),
        NORMAL(Identifier.ofVanilla("hud/heart/full"), Identifier.ofVanilla("hud/heart/full_blinking"), Identifier.ofVanilla("hud/heart/half"), Identifier.ofVanilla("hud/heart/half_blinking"), Identifier.ofVanilla("hud/heart/hardcore_full"), Identifier.ofVanilla("hud/heart/hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/hardcore_half"), Identifier.ofVanilla("hud/heart/hardcore_half_blinking")),
        POISONED(Identifier.ofVanilla("hud/heart/poisoned_full"), Identifier.ofVanilla("hud/heart/poisoned_full_blinking"), Identifier.ofVanilla("hud/heart/poisoned_half"), Identifier.ofVanilla("hud/heart/poisoned_half_blinking"), Identifier.ofVanilla("hud/heart/poisoned_hardcore_full"), Identifier.ofVanilla("hud/heart/poisoned_hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/poisoned_hardcore_half"), Identifier.ofVanilla("hud/heart/poisoned_hardcore_half_blinking")),
        WITHERED(Identifier.ofVanilla("hud/heart/withered_full"), Identifier.ofVanilla("hud/heart/withered_full_blinking"), Identifier.ofVanilla("hud/heart/withered_half"), Identifier.ofVanilla("hud/heart/withered_half_blinking"), Identifier.ofVanilla("hud/heart/withered_hardcore_full"), Identifier.ofVanilla("hud/heart/withered_hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/withered_hardcore_half"), Identifier.ofVanilla("hud/heart/withered_hardcore_half_blinking")),
        ABSORBING(Identifier.ofVanilla("hud/heart/absorbing_full"), Identifier.ofVanilla("hud/heart/absorbing_full_blinking"), Identifier.ofVanilla("hud/heart/absorbing_half"), Identifier.ofVanilla("hud/heart/absorbing_half_blinking"), Identifier.ofVanilla("hud/heart/absorbing_hardcore_full"), Identifier.ofVanilla("hud/heart/absorbing_hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/absorbing_hardcore_half"), Identifier.ofVanilla("hud/heart/absorbing_hardcore_half_blinking")),
        FROZEN(Identifier.ofVanilla("hud/heart/frozen_full"), Identifier.ofVanilla("hud/heart/frozen_full_blinking"), Identifier.ofVanilla("hud/heart/frozen_half"), Identifier.ofVanilla("hud/heart/frozen_half_blinking"), Identifier.ofVanilla("hud/heart/frozen_hardcore_full"), Identifier.ofVanilla("hud/heart/frozen_hardcore_full_blinking"), Identifier.ofVanilla("hud/heart/frozen_hardcore_half"), Identifier.ofVanilla("hud/heart/frozen_hardcore_half_blinking"));

        private final Identifier fullTexture;
        private final Identifier fullBlinkingTexture;
        private final Identifier halfTexture;
        private final Identifier halfBlinkingTexture;
        private final Identifier hardcoreFullTexture;
        private final Identifier hardcoreFullBlinkingTexture;
        private final Identifier hardcoreHalfTexture;
        private final Identifier hardcoreHalfBlinkingTexture;

        HudHeartType(final Identifier fullTexture, final Identifier fullBlinkingTexture, final Identifier halfTexture, final Identifier halfBlinkingTexture, final Identifier hardcoreFullTexture, final Identifier hardcoreFullBlinkingTexture, final Identifier hardcoreHalfTexture, final Identifier hardcoreHalfBlinkingTexture) {
            this.fullTexture = fullTexture;
            this.fullBlinkingTexture = fullBlinkingTexture;
            this.halfTexture = halfTexture;
            this.halfBlinkingTexture = halfBlinkingTexture;
            this.hardcoreFullTexture = hardcoreFullTexture;
            this.hardcoreFullBlinkingTexture = hardcoreFullBlinkingTexture;
            this.hardcoreHalfTexture = hardcoreHalfTexture;
            this.hardcoreHalfBlinkingTexture = hardcoreHalfBlinkingTexture;
        }

        public Identifier getTexture(boolean hardcore, boolean half, boolean blinking) {
            if (!hardcore) {
                if (half) {
                    return blinking ? this.halfBlinkingTexture : this.halfTexture;
                } else {
                    return blinking ? this.fullBlinkingTexture : this.fullTexture;
                }
            } else if (half) {
                return blinking ? this.hardcoreHalfBlinkingTexture : this.hardcoreHalfTexture;
            } else {
                return blinking ? this.hardcoreFullBlinkingTexture : this.hardcoreFullTexture;
            }
        }

        static HudHeartType fromEntityState(LivingEntity livingEntity) {
            HudHeartType heartType;
            if (livingEntity.hasStatusEffect(StatusEffects.POISON)) {
                heartType = POISONED;
            } else if (livingEntity.hasStatusEffect(StatusEffects.WITHER)) {
                heartType = WITHERED;
            } else if (livingEntity.isFrozen()) {
                heartType = FROZEN;
            } else {
                heartType = NORMAL;
            }

            return heartType;
        }
    }

}
