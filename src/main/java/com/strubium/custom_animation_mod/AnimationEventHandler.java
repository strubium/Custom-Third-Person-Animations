package com.strubium.custom_animation_mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class AnimationEventHandler {

    private CustomModelPlayer customModel;
    private boolean inventoryOpen = false;
    private int resetDelay = 0;

    private static final Map<String, String> itemAnimationMap = new HashMap<>();

    public static void addAnimation(Item item, String animation){
        itemAnimationMap.put(item.getRegistryName().toString(), animation);
    }

    public static void addAnimation(String itemRegName, String animation){
        itemAnimationMap.put(itemRegName, animation);
    }


    public AnimationEventHandler() {
        try {
            Map<String, CustomAnimationLoader.AnimationData> animations = CustomAnimationLoader.loadAnimations(new ResourceLocation(Tags.MOD_ID, "animations/animation.json"));
            customModel = new CustomModelPlayer(0.0F, false, animations);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Define animations for specific items
//        itemAnimationMap.put("minecraft:diamond_sword", "sword_idle_animation");
//        itemAnimationMap.put("minecraft:bow", "bow_hold_animation");
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (event.getRenderer() instanceof RenderLivingBase) {
            ((RenderLivingBase<?>) event.getRenderer()).mainModel = customModel;
        }

        EntityPlayer player = event.getEntityPlayer();
        ItemStack heldItem = player.getHeldItemMainhand();

        if (!heldItem.isEmpty() && heldItem.getItem().getRegistryName() != null) {
            String animation = itemAnimationMap.get(heldItem.getItem().getRegistryName().toString());
            if (animation != null) {
                customModel.setActiveAnimation(animation);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null) {
            return;
        }

        boolean isInventoryOpen = mc.currentScreen instanceof GuiInventory;

        if (isInventoryOpen && !inventoryOpen) {
            customModel.setActiveAnimation("open_inventory");
        } else if (isInventoryOpen) {
            customModel.setActiveAnimation("check_inventory");
        } else if (!isInventoryOpen && inventoryOpen) {
            resetDelay = 2;
        }

        if (resetDelay > 0) {
            resetDelay--;
            if (resetDelay == 0) {
                customModel.setActiveAnimation(null);
            }
        }

        inventoryOpen = isInventoryOpen;
    }
}

