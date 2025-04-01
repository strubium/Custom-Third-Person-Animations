package com.strubium.custom_animation_mod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnimationEventHandler {

    private final CustomModelPlayer customModel;
    private GuiScreen lastScreen = null;

    private static final Map<String, String> itemAnimationMap = new HashMap<>();
    private static final Map<Class<? extends GuiScreen>, String> screenOpenAnimationMap = new HashMap<>();
    private static final Map<Class<? extends GuiScreen>, String> screenLoopAnimationMap = new HashMap<>();

    public static void addAnimation(Item item, String animation) {
        itemAnimationMap.put(item.getRegistryName().toString(), animation);
    }

    public static void addAnimation(String itemRegName, String animation) {
        itemAnimationMap.put(itemRegName, animation);
    }

    public static void addScreenAnimation(Class<? extends GuiScreen> screenClass, String openAnimation, String loopAnimation) {
        if (openAnimation != null) {
            screenOpenAnimationMap.put(screenClass, openAnimation);
        }
        if (loopAnimation != null) {
            screenLoopAnimationMap.put(screenClass, loopAnimation);
        }
    }

    public AnimationEventHandler() {
        try {
            Map<String, CustomAnimationLoader.AnimationData> animations = CustomAnimationLoader.loadAnimations(new ResourceLocation(Tags.MOD_ID, "animations/animation.json"));
            customModel = new CustomModelPlayer(0.0F, false, animations);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load animations!", e);
        }

        // Default screen animations
        addScreenAnimation(GuiInventory.class, "open_inventory", "check_inventory");
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
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen newScreen = event.getGui();

        if (newScreen == null) {
            // If GUI closes, reset animation
            if (lastScreen != null) {
                customModel.setActiveAnimation(null);
            }
        } else {
            // Play one-time opening animation
            String openAnimation = screenOpenAnimationMap.get(newScreen.getClass());
            if (openAnimation != null) {
                customModel.setActiveAnimation(openAnimation);
            }
        }

        lastScreen = newScreen;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        // Handle loop animation while GUI is open
        if (mc.currentScreen != null) {
            String loopAnimation = screenLoopAnimationMap.get(mc.currentScreen.getClass());
            if (loopAnimation != null) {
                customModel.setActiveAnimation(loopAnimation);
            }
        } else if (lastScreen != null) {
            // Reset animation when screen is closed
            customModel.setActiveAnimation(null);
            lastScreen = null;
        }
    }
}
