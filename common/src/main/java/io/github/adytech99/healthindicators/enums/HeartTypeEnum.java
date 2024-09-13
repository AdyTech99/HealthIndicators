package io.github.adytech99.healthindicators.enums;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;

public enum HeartTypeEnum {
    EMPTY("container"),
    RED_FULL("full"),
    RED_HALF("half"),
    YELLOW_FULL("absorbing_full"),
    YELLOW_HALF("absorbing_half");

    public final String icon;

    HeartTypeEnum(String heartIcon) {
        icon = heartIcon;
    }

    public static String addStatusIcon(LivingEntity livingEntity){
        if (livingEntity.hasStatusEffect(StatusEffects.WITHER)) return "withered_";
        if (livingEntity.hasStatusEffect(StatusEffects.POISON)) return "poisoned_";
        if (livingEntity.isFrozen()) return "frozen_";
        else return "";
    }


    public static String addHardcoreIcon(LivingEntity livingEntity){
        if (livingEntity.getWorld().getLevelProperties().isHardcore()) return "hardcore_";
        else return "";
    }
}