package io.github.adytech99.healthindicators.mixin;

import io.github.adytech99.healthindicators.LivingEntityAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
@Environment(EnvType.CLIENT)
public class LivingEntityMixin implements LivingEntityAccess {
    public int healthindicators$healthDisplayTimer;

    @Override
    public int getHealthindicators$healthDisplayTimer() {
        return healthindicators$healthDisplayTimer;
    }

    @Override
    public void setHealthindicators$healthDisplayTimer(int v) {
        healthindicators$healthDisplayTimer = v;
    }
}
