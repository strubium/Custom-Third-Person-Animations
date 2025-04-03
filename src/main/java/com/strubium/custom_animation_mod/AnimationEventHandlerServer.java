package com.strubium.custom_animation_mod;

import com.strubium.custom_animation_mod.network.AnimationNetworkHandler;
import com.strubium.custom_animation_mod.network.AnimationPacket;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.Map;

public class AnimationEventHandlerServer {
    private static final Map<EntityPlayerMP, String> activeAnimations = new HashMap<>();

    public static void setAnimation(EntityPlayerMP player, String animation) {
        if (animation == null) {
            activeAnimations.remove(player); // ↓ Update the animation we are playing if we remove it
            AnimationNetworkHandler.sendToClient(new AnimationPacket(player.getEntityId(), null), player);
        } else {
            activeAnimations.put(player, animation);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        for (Map.Entry<EntityPlayerMP, String> entry : activeAnimations.entrySet()) {

            EntityPlayerMP player = entry.getKey();
            String animation = entry.getValue();

            // Send periodic updates to clients
            AnimationNetworkHandler.sendToClient(new AnimationPacket(player.getEntityId(), animation), player);
        }
    }
}
