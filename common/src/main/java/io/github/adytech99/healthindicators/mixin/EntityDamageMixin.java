package io.github.adytech99.healthindicators.mixin;

import io.github.adytech99.healthindicators.DamageDirectionIndicatorRenderer;
import io.github.adytech99.healthindicators.HealthIndicatorsCommon;
import io.github.adytech99.healthindicators.RenderTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class EntityDamageMixin {
    @Inject(method = "onDamaged(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("TAIL"))
    private void onEntityDamage(DamageSource damageSource, CallbackInfo callbackInfo) {
        RenderTracker.onDamage(damageSource, ((LivingEntity) (Object) this));

        if(damageSource.getAttacker() instanceof LivingEntity attacker && attacker != null && (Object) this == HealthIndicatorsCommon.client.player){
            DamageDirectionIndicatorRenderer.markDamageToPlayer(attacker);
        }
    }
}
