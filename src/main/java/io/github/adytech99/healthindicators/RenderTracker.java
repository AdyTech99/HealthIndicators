package io.github.adytech99.healthindicators;

import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class RenderTracker {
    private static final CopyOnWriteArrayList<UUID> UUIDS = new CopyOnWriteArrayList<>();

    public static boolean isInUUIDS(LivingEntity livingEntity){
        return UUIDS.contains(livingEntity.getUuid());
    }

    public static void tick(MinecraftClient client){
        if(client.player == null || client.world == null) return;
        for(Entity entity : client.world.getEntities()){
            if(entity instanceof  LivingEntity){
                LivingEntity livingEntity = ((LivingEntity) entity);
                if(doRender(client.player, livingEntity)){
                    addToUUIDS(livingEntity);
                    trimEntities(client.world);

                }
                else {
                    UUIDS.remove(livingEntity.getUuid());
                }
            }
        }
    }

    public static void onDamage(DamageSource damageSource, LivingEntity livingEntity) {
        if(damageSource.getAttacker() instanceof PlayerEntity){
            assert MinecraftClient.getInstance().world != null;

            if (ModConfig.HANDLER.instance().on_hit
                    && livingEntity instanceof LivingEntity
                    && RenderTracker.isAllowed(livingEntity, MinecraftClient.getInstance().player)) {
                addToUUIDS(livingEntity);
            }
        }
    }


    public static void removeFromUUIDS(Entity entity){
        UUIDS.remove(entity.getUuid());
    }

    public static void addToUUIDS(LivingEntity livingEntity){
        if(!UUIDS.contains(livingEntity.getUuid())) UUIDS.add(livingEntity.getUuid());
    }

    public static void trimEntities(ClientWorld world) {
        int over = UUIDS.size() - 64;
        if (over > 0) {
            UUIDS.subList(0, over).clear();
        }

        Iterator<UUID> iterator = UUIDS.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = getEntityFromUUID(uuid, world);
            if (isInvalid(entity)) {
                UUIDS.remove(uuid);
            }
        }
    }

    public static boolean overridePlayers(LivingEntity livingEntity){
        return (ModConfig.HANDLER.instance().override_players && livingEntity instanceof PlayerEntity) || livingEntity == MinecraftClient.getInstance().player;
    }

    public static boolean isAllowed(LivingEntity livingEntity, PlayerEntity self){
        if(!ModConfig.HANDLER.instance().passive_mobs && livingEntity instanceof PassiveEntity) return false;
        if(!ModConfig.HANDLER.instance().hostile_mobs && livingEntity instanceof HostileEntity) return false;
        if(!ModConfig.HANDLER.instance().players && livingEntity instanceof PlayerEntity) return false;
        if(!ModConfig.HANDLER.instance().self && livingEntity == self) return false;
        return true;
    }

    public static boolean doRender(ClientPlayerEntity player, LivingEntity livingEntity){
        if(!RenderTracker.isAllowed(livingEntity, player)) return false; //Entity Types
        if(!RenderTracker.isInUUIDS(livingEntity) && ModConfig.HANDLER.instance().on_hit && livingEntity != player) return false; //Damaged by Player
        if(livingEntity.getHealth() == livingEntity.getMaxHealth() && ModConfig.HANDLER.instance().damaged_only && livingEntity.getAbsorptionAmount() <= 0 && livingEntity != player) return false; //Damaged by Any Reason
        if((!isTargeted(player, livingEntity) && ModConfig.HANDLER.instance().on_crosshair) && !RenderTracker.overridePlayers(livingEntity)) return false; //Targeted with Crosshair

        return player != null
                && Config.getRenderingEnabled()
                && player.getVehicle() != livingEntity
                && !livingEntity.isInvisibleTo(player);
    }

    private static boolean isTargeted(ClientPlayerEntity player, LivingEntity livingEntity){
        Entity camera = MinecraftClient.getInstance().cameraEntity;
        double d = ModConfig.HANDLER.instance().reach;
        double e = MathHelper.square(d);
        Vec3d vec3d = camera.getCameraPosVec(0);
        HitResult hitResult = camera.raycast(d, 0, false);
        double f = hitResult.getPos().squaredDistanceTo(vec3d);
        if (hitResult.getType() != HitResult.Type.MISS) {
            e = f;
            d = Math.sqrt(e);
        }
        Vec3d vec3d2 = camera.getRotationVec(0);
        Vec3d vec3d3 = vec3d.add(vec3d2.x * d, vec3d2.y * d, vec3d2.z * d);
        float g = 1.0f;
        Box box = camera.getBoundingBox().stretch(vec3d2.multiply(d)).expand(1.0, 1.0, 1.0);
        assert MinecraftClient.getInstance().cameraEntity != null;
        EntityHitResult entityHitResult = ProjectileUtil.raycast(MinecraftClient.getInstance().cameraEntity, vec3d, vec3d3, box, entity -> !entity.isSpectator() && entity.canHit(), e);

        if (entityHitResult != null && entityHitResult.getEntity() instanceof LivingEntity livingEntity1){
            return livingEntity1 == livingEntity;
        }
        return false;
    }


    private static boolean isInvalid(Entity entity){
        return (entity == null || !entity.isAlive() || !entity.isLiving() || entity.isRegionUnloaded() || !(entity instanceof LivingEntity));
    }
    private static Entity getEntityFromUUID(UUID uuid, ClientWorld world) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUuid().equals(uuid)) {
                return entity;
            }
        }
        return null;
    }
}
