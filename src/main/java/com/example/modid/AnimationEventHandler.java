package com.example.modid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class AnimationEventHandler {

    private CustomModelPlayer customModel;
    private static Field modelField;
    private boolean inventoryOpen = false;
    private int resetDelay = 0;

    private final Map<String, String> itemAnimationMap = new HashMap<>();

    static {
        try {
            modelField = RenderLivingBase.class.getDeclaredField("mainModel");
            modelField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            ExampleMod.LOGGER.error("Error loading mainModel");
        }
    }

    public AnimationEventHandler() {
        try {
            Map<String, CustomAnimationLoader.AnimationData> animations = CustomAnimationLoader.loadAnimations("animations/animation.json");
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
        try {
            if (modelField != null) {
                modelField.set(event.getRenderer(), customModel);
            }

            EntityPlayer player = event.getEntityPlayer();
            ItemStack heldItem = player.getHeldItemMainhand();

            if (heldItem != null && heldItem.getItem().getRegistryName() != null) {
                String animation = itemAnimationMap.get(heldItem.getItem().getRegistryName().toString());
                if (animation != null) {
                    customModel.setActiveAnimation(animation);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
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

