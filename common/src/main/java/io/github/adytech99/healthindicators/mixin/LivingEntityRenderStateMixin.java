package io.github.adytech99.healthindicators.mixin;

import io.github.adytech99.healthindicators.LivingEntityRenderStateAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
@Environment(EnvType.CLIENT)
public class LivingEntityRenderStateMixin implements LivingEntityRenderStateAccess {
    @Unique
    private LivingEntity livingEntity;

    @Override
    public LivingEntity healthindicators$getLivingEntity() {
        return livingEntity;
    }

    @Override
    public void healthindicators$setLivingEntity(LivingEntity livingEntity1) {
        livingEntity = livingEntity1;
    }
}
