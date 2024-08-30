package io.github.adytech99.healthindicators;

import io.github.adytech99.healthindicators.config.Config;
import io.github.adytech99.healthindicators.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RenderTracker {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static final ConcurrentHashMap<UUID, Integer> UUIDS = new ConcurrentHashMap<>();

    public static boolean after_attack = ModConfig.HANDLER.instance().after_attack;

    public static void tick(MinecraftClient client){
        if(client.player == null || client.world == null) return;
        if(Config.getRenderingEnabled()) {
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof LivingEntity livingEntity && satisfiesAdvancedCriteria(client.player, livingEntity) && satisfiesList(client.player, livingEntity)) {
                    addToUUIDS(livingEntity);
                } else removeFromUUIDS(entity.getUuid());
            }
        }
        trimEntities(client.world);
        if(ModConfig.HANDLER.instance().after_attack != after_attack) {
            UUIDS.clear();
            after_attack = ModConfig.HANDLER.instance().after_attack;
        }
    }

    public static void onDamage(DamageSource damageSource, LivingEntity livingEntity) {
        if(damageSource.getAttacker() instanceof PlayerEntity){
            assert client.world != null;
            if (ModConfig.HANDLER.instance().after_attack
                    && livingEntity instanceof LivingEntity
                    && RenderTracker.isEntityTypeAllowed(livingEntity, client.player)
                    && satisfiesList(client.player, livingEntity)) {

                if(!addToUUIDS(livingEntity)){
                    UUIDS.replace(livingEntity.getUuid(), (ModConfig.HANDLER.instance().time_after_hit * 20));
                }
            }
        }
    }


    public static void trimEntities(ClientWorld world) {
        // Check if there's a need to trim entries
        Iterator<Map.Entry<UUID, Integer>> iterator = UUIDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            entry.setValue(entry.getValue() - 1);
            if (entry.getValue() <= 0) {
                iterator.remove(); // Safe removal during iteration
            }
        }

        // Remove invalid entities
        UUIDS.entrySet().removeIf(entry -> isInvalid(getEntityFromUUID(entry.getKey(), world))|| !Config.getRenderingEnabled() );
        if(UUIDS.size() >= 1536) UUIDS.clear();
    }


    public static void removeFromUUIDS(Entity entity){
        UUIDS.remove(entity.getUuid());
    }
    public static void removeFromUUIDS(UUID uuid){
        UUIDS.remove(uuid);
    }

    public static boolean addToUUIDS(LivingEntity livingEntity){
        if(!UUIDS.containsKey(livingEntity.getUuid())){
            UUIDS.put(livingEntity.getUuid(), ModConfig.HANDLER.instance().after_attack ? (ModConfig.HANDLER.instance().time_after_hit * 20) : 2400);
            return true;
        }
        else return false;
    }

    public static boolean isInUUIDS(LivingEntity livingEntity){
        return UUIDS.containsKey(livingEntity.getUuid());
    }

    public static boolean overridePlayers(ClientPlayerEntity playerEntity, LivingEntity livingEntity){
        return (ModConfig.HANDLER.instance().override_players && livingEntity instanceof PlayerEntity && livingEntity != client.player)
                || (livingEntity == client.player && ModConfig.HANDLER.instance().self);
    }

    public static boolean isEntityTypeAllowed(LivingEntity livingEntity, PlayerEntity self){
        if(!ModConfig.HANDLER.instance().passive_mobs && livingEntity instanceof PassiveEntity) return false;
        if(!ModConfig.HANDLER.instance().hostile_mobs && livingEntity instanceof HostileEntity) return false;
        if(!ModConfig.HANDLER.instance().players && livingEntity instanceof PlayerEntity) return false;
        if(!ModConfig.HANDLER.instance().self && livingEntity == self) return false;
        return true;
    }

    public static boolean satisfiesAdvancedCriteria(ClientPlayerEntity player, LivingEntity livingEntity){
        if(overridePlayers(player, livingEntity)) return true;

        if(!isEntityTypeAllowed(livingEntity, player)) return false; //Entity Types
        if(ModConfig.HANDLER.instance().after_attack && !UUIDS.containsKey(livingEntity.getUuid())) return false; //Damaged by Player, key should have been added by separate means. Necessary because removal check is done by this method.
        if(ModConfig.HANDLER.instance().damaged_only && (livingEntity.getHealth() == livingEntity.getMaxHealth() || livingEntity.getHealth() > livingEntity.getMaxHealth()*((float) ModConfig.HANDLER.instance().max_health_percentage / 100)) && livingEntity.getAbsorptionAmount() <= 0) return false; //Damaged by Any Reason
        if(ModConfig.HANDLER.instance().looking_at && !isTargeted(livingEntity)) return false;
        if(ModConfig.HANDLER.instance().within_distance && livingEntity.distanceTo(player) > ModConfig.HANDLER.instance().distance) return false;

        return !isInvalid(livingEntity);
    }

    public static boolean satisfiesList(ClientPlayerEntity player, LivingEntity livingEntity){
        if(!ModConfig.HANDLER.instance().blacklistOrWhitelist){
            if(ModConfig.HANDLER.instance().list.isEmpty()) return true;
            if(ModConfig.HANDLER.instance().list.contains("minecraft:player") && livingEntity instanceof PlayerEntity) return true;
        }

        String[] blacklist1 = new String[ModConfig.HANDLER.instance().list.size()];
        for(int i = 0; i < ModConfig.HANDLER.instance().list.size(); i++){
            blacklist1[i] = ModConfig.HANDLER.instance().list.get(i);
        }

        if(ModConfig.HANDLER.instance().blacklistOrWhitelist) return Arrays.stream(blacklist1).noneMatch(s -> {
            if(livingEntity instanceof PlayerEntity) return Text.of(s).equals(Objects.requireNonNull(livingEntity.getName()));
            else return s.equals(EntityType.getId(livingEntity.getType()).toString());
        });
        else return Arrays.stream(blacklist1).anyMatch(s -> {
            if(livingEntity instanceof PlayerEntity) return Text.of(s).equals(Objects.requireNonNull(livingEntity.getName()));
            else return s.equals(EntityType.getId(livingEntity.getType()).toString());
        });
    }

    public static boolean isTargeted(LivingEntity livingEntity){
        Entity camera = client.cameraEntity;
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
        assert client.cameraEntity != null;
        EntityHitResult entityHitResult = ProjectileUtil.raycast(client.cameraEntity, vec3d, vec3d3, box, entity -> !entity.isSpectator() && entity.canHit(), e);

        if (entityHitResult != null && entityHitResult.getEntity() instanceof LivingEntity livingEntity1){
            return livingEntity1 == livingEntity;
        }
        return false;
    }


    public static boolean isInvalid(Entity entity){
        return (entity == null
                || !entity.isAlive()
                || !entity.isLiving()
                || entity.isRegionUnloaded()
                || !(entity instanceof LivingEntity)
                || client.player == null
                || client.player.getVehicle() == entity
                || entity.isInvisibleTo(client.player));
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

