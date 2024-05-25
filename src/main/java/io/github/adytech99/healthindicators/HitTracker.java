package io.github.adytech99.healthindicators;

import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class HitTracker {
    private static final CopyOnWriteArrayList<UUID> damagedEntities = new CopyOnWriteArrayList<>();

    public static void onDamage(DamageSource damageSource, LivingEntity livingEntity) {
        if(damageSource.getAttacker() instanceof PlayerEntity){
            assert MinecraftClient.getInstance().world != null;
            attackHandler(MinecraftClient.getInstance().player, null, null, livingEntity, null);
        }
    }

    public static ActionResult attackHandler(PlayerEntity player, @Nullable World world, @Nullable Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
        if (ModConfig.HANDLER.instance().on_hit
                && entity instanceof LivingEntity
                && RenderTracker.isAllowed((LivingEntity) entity, player)) {
            addToDamagedEntities((LivingEntity) entity, MinecraftClient.getInstance().world);
        }
        return ActionResult.PASS;
    }

    public static void addToDamagedEntities(LivingEntity livingEntity, ClientWorld world) {
        if (!damagedEntities.contains(livingEntity.getUuid())) {
            damagedEntities.add(livingEntity.getUuid());
            trimDamagedEntities(world);
        }
    }

    public static void trimDamagedEntities(ClientWorld world) {
        int over = damagedEntities.size() - 32;
        if (over > 0) {
            damagedEntities.subList(0, over).clear();
        }

        Iterator<UUID> iterator = damagedEntities.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = getEntityFromUUID(uuid, world);
            if (isInvalid(entity)) {
                damagedEntities.remove(uuid);
            }
        }
    }

    public static void removeFromDamagedEntities(Entity entity){
        damagedEntities.remove(entity.getUuid());
    }

    public static boolean isInDamagedEntities(LivingEntity livingEntity) {
        return damagedEntities.contains(livingEntity.getUuid()) || RenderTracker.overridePlayers(livingEntity);
    }

    public static CopyOnWriteArrayList<UUID> getDamagedEntities() {
        return damagedEntities;
    }

    public static void setDamagedEntities(CopyOnWriteArrayList<UUID> damagedEntities) {
        synchronized (HitTracker.damagedEntities) {
            HitTracker.damagedEntities.clear();
            HitTracker.damagedEntities.addAll(damagedEntities);
        }
    }

    private static Entity getEntityFromUUID(UUID uuid, ClientWorld world) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUuid().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }

    private static boolean isInvalid(Entity entity){
        return (entity == null || !entity.isAlive() || !entity.isLiving() || entity.isRegionUnloaded());
    }
}