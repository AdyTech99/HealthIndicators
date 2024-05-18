package io.github.adytech99.healthindicators.util;

import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;

public class FilterConfig {
    public static boolean isAllowed(LivingEntity livingEntity, PlayerEntity self){
        if(!ModConfig.HANDLER.instance().passive_mobs && livingEntity instanceof PassiveEntity) return false;
        if(!ModConfig.HANDLER.instance().hostile_mobs && livingEntity instanceof HostileEntity) return false;
        if(!ModConfig.HANDLER.instance().players && livingEntity instanceof PlayerEntity) return false;
        if(!ModConfig.HANDLER.instance().self && livingEntity == self) return false;
        return true;
    }
}
