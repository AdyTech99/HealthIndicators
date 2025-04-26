package io.github.adytech99.healthindicators;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.texture.AbstractTexture;

public abstract class Renderer {
    public static class AbstractRenderLayerTexture extends RenderPhase.TextureBase {
        public AbstractRenderLayerTexture(AbstractTexture texture) {
            super(() -> RenderSystem.setShaderTexture(0, texture.getGlTexture()), () -> {});
        }
    }
}