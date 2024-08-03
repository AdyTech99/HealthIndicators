package io.github.adytech99.healthindicators.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class HeartJumpData {
    private static final MinecraftClient client = MinecraftClient.getInstance();

    private static boolean isHeartJumping = false;
    private static int whichHeartJumping = -1;

    public static int getWhichHeartJumping(LivingEntity livingEntity) {
        return (int) (livingEntity.age % livingEntity.getMaxHealth());
    }

    public static boolean isHeartJumping() {
        return isHeartJumping;
    }

    public static void tick(MinecraftClient client){
        PlayerEntity player = client.player;
        if(player == null) return;
        if(whichHeartJumping != -1){
            whichHeartJumping++;
            isHeartJumping = true;
            if(whichHeartJumping > player.getMaxHealth()/2){
                whichHeartJumping = -1;
                isHeartJumping = false;
            }
        } else if (player.age % 16 == 0) {
            whichHeartJumping = 1;
            isHeartJumping = true;
        }
        else isHeartJumping = false;
        //client.player.sendMessage(Text.literal(String.valueOf(whichHeartJumping)), true);
    }
}
