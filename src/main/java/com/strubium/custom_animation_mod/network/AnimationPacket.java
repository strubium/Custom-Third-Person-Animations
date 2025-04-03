package com.strubium.custom_animation_mod.network;

import com.strubium.custom_animation_mod.CustomAnimationMod;
import com.strubium.custom_animation_mod.CustomModelPlayer;
import com.strubium.custom_animation_mod.AnimationEventHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import java.nio.charset.StandardCharsets;

public class AnimationPacket implements IMessage {
    private String animationName;
    private int entityId;

    public AnimationPacket() {
    }

    public AnimationPacket(int entityId, String animationName) {
        this.entityId = entityId;
        this.animationName = animationName;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        if (animationName != null) {
            byte[] nameBytes = animationName.getBytes(StandardCharsets.UTF_8);
            buf.writeInt(nameBytes.length);
            buf.writeBytes(nameBytes);
        } else {
            buf.writeInt(-1); // Special flag for null
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        int len = buf.readInt();
        if (len == -1) {
            animationName = null;  // Handle null case explicitly
        } else if (len >= 0) {
            animationName = buf.readCharSequence(len, StandardCharsets.UTF_8).toString();
        }
    }

    public static class Handler implements IMessageHandler<AnimationPacket, IMessage> {
        @Override
        public IMessage onMessage(AnimationPacket message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                // Handle on client
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    AbstractClientPlayer player = (AbstractClientPlayer) Minecraft.getMinecraft().world.getEntityByID(message.entityId);
                    if (player != null) {
                        RenderLivingBase<?> render = (RenderLivingBase<?>) Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(player);
                        if (render != null) {
                            if (render.getMainModel() instanceof CustomModelPlayer) {
                                CustomModelPlayer model = (CustomModelPlayer) render.getMainModel();
                                model.setActiveAnimation(message.animationName);
                            } else {
                                CustomAnimationMod.LOGGER.error("The main model is not an instance of CustomModelPlayer.");
                            }
                        } else {
                            CustomAnimationMod.LOGGER.error("Render object is null for player " + player);
                        }
                    } else {
                        CustomAnimationMod.LOGGER.warn("Player not found with entityId " + message.entityId);
                    }
                });
            } else {
                // Handle on server
                ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    AnimationEventHandlerServer.setAnimation(player, message.animationName);
                });
            }
            return null;
        }
    }
}
