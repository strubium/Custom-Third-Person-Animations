package com.example.modid;

import com.example.modid.CustomModelPlayer;
import com.example.modid.CustomAnimationLoader; // Your custom loader for animations
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.io.IOException;
import java.util.Map;

public class EventHandler {

    private CustomModelPlayer customModel; // Custom model with JSON-based animation
    private static Field modelField;

    // Static block to initialize reflection
    static {
        try {
            // Use RenderLivingBase class instead of RenderPlayer
            modelField = RenderLivingBase.class.getDeclaredField("mainModel");
            modelField.setAccessible(true); // Allow access to the private field
        } catch (NoSuchFieldException e) {
            e.printStackTrace(); // Log the error if the field is not found
        }
    }

    // Constructor for EventHandler - load the JSON and create the custom model
    public EventHandler() {
        try {
            // Load the animation data from the JSON file in the resources
            Map<String, CustomAnimationLoader.AnimationData> animations = CustomAnimationLoader.loadAnimations("animations/animation.json");
            CustomAnimationLoader.AnimationData armSwingAnimation = animations.get("hold_gun");
            customModel = new CustomModelPlayer(0.0F, false, armSwingAnimation);
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception if JSON fails to load
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        try {
            if (modelField != null) {
                // Replace the main model with the custom model with JSON animation
                modelField.set(event.getRenderer(), customModel);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // Handle the IllegalAccessException
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        try {
            if (modelField != null) {
                // Reset to the default model after rendering (optional)
                modelField.set(event.getRenderer(), customModel);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // Handle the IllegalAccessException
        }
    }
}
