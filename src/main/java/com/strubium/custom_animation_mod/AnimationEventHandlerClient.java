package com.strubium.custom_animation_mod;

import com.strubium.custom_animation_mod.network.AnimationPacket;
import com.strubium.custom_animation_mod.network.AnimationNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class AnimationEventHandlerClient {

    private GuiScreen lastScreen = null;

    private static final Map<String, String> itemAnimationMap = new HashMap<>();
    private static final Map<Class<? extends GuiScreen>, String> screenOpenAnimationMap = new HashMap<>();
    private static final Map<Class<? extends GuiScreen>, String> screenLoopAnimationMap = new HashMap<>();

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

    public AnimationEventHandlerClient() {
        // Default screen animations
        addScreenAnimation(GuiInventory.class, "open_inventory", "check_inventory");
    }

    @SubscribeEvent
    public void onRenderPlayerPreNumberTwo(RenderPlayerEvent.Pre event) {
        if (!(event.getRenderer().getMainModel() instanceof CustomModelPlayer)) {
            event.getRenderer().mainModel = new CustomModelPlayer();
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack heldItem = player.getHeldItemMainhand();

        if (!heldItem.isEmpty() && heldItem.getItem().getRegistryName() != null) {
            String animation = itemAnimationMap.get(heldItem.getItem().getRegistryName().toString());
            sendAnimationRequestToServer(player.getEntityId(), animation);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        GuiScreen newScreen = event.getGui();

        // Check if the screen is being closed (newScreen == null)
        if (newScreen == null) {
            // Send the "null" animation to the server to stop any active animation
            int playerUUID = Minecraft.getMinecraft().player.getEntityId();
            sendAnimationRequestToServer(playerUUID, null); // Stop the loop animation
        } else {
            // If a new screen is opened, send the associated open animation
            String openAnimation = screenOpenAnimationMap.get(newScreen.getClass());
            if (openAnimation != null) {
                int playerUUID = Minecraft.getMinecraft().player.getEntityId();
                sendAnimationRequestToServer(playerUUID, openAnimation);
            }
        }

        // Update the lastScreen
        lastScreen = newScreen;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        int playerID = mc.player.getEntityId();
        if (mc.currentScreen != null) {
            // Send loop animation for the current screen
            String loopAnimation = screenLoopAnimationMap.get(mc.currentScreen.getClass());
            if (loopAnimation != null) {
                sendAnimationRequestToServer(playerID, loopAnimation);
            }
        } else if (lastScreen != null) {
            // If the screen is closed and no new screen is opened, send null to stop the animation
            sendAnimationRequestToServer(playerID, null);
            lastScreen = null;  // Reset the lastScreen to null after sending the stop animation request
        }
    }

    private void sendAnimationRequestToServer(int playerID, String animation) {
        // Send packet to server to set animation for the correct player
        AnimationNetworkHandler.sendToServer(new AnimationPacket(playerID, animation));
    }

}
