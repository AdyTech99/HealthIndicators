package io.github.adytech99.healthindicators.neoforge.mixin;

import io.github.adytech99.healthindicators.neoforge.RenderTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LivingEntity.class)
public class EntityDamageMixin {
    @Inject(method = "onDamaged", at = @At("HEAD"))
    private void onEntityDamage(DamageSource damageSource, CallbackInfo callbackInfo) {
        RenderTracker.onDamage(damageSource, ((LivingEntity) (Object) this));
    }
}
