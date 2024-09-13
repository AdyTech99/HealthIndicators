package io.github.adytech99.healthindicators.util;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

import java.util.Objects;

public class Util {
    public static double truncate(double number, int places) {
        return Math.floor(number * Math.pow(10, places)) / Math.pow(10, places);
    }

    public static Entity getEntityFromName(ClientWorld world, String entity_name){
        for(Entity entity : world.getEntities()){
            if(entity.hasCustomName()) if(Objects.equals(entity.getCustomName().getLiteralString(), entity_name)) return entity;
            if(entity.isPlayer()) if(Objects.equals(entity.getDisplayName().getString(), entity_name)) return entity;
        }
        return null;
    }
}
