package com.strubium.custom_animation_mod.network;

import com.strubium.custom_animation_mod.CustomAnimationMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class AnimationNetworkHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("custom_anim");

    public static void registerPackets() {
        int packetId = 0;
        INSTANCE.registerMessage(AnimationPacket.Handler.class, AnimationPacket.class, packetId++, Side.CLIENT);
        INSTANCE.registerMessage(AnimationPacket.Handler.class, AnimationPacket.class, packetId++, Side.SERVER);
        CustomAnimationMod.LOGGER.info("Loaded Packets");
    }

    public static void sendToClient(IMessage message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }
    public static void sendToServer(IMessage message) {
        INSTANCE.sendToServer(message);
    }
}
