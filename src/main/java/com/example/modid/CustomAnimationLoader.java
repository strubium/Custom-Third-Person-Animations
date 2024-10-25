package com.example.modid;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class CustomAnimationLoader {

    public static Map<String, AnimationData> loadAnimations(String resourcePath) throws IOException {
        Gson gson = new Gson();

        // Load from the resource pack
        InputStream inputStream = Minecraft.getMinecraft().getResourceManager()
                .getResource(new ResourceLocation("modid", resourcePath)).getInputStream();

        AnimationContainer animations = gson.fromJson(new InputStreamReader(inputStream), AnimationContainer.class);
        inputStream.close();
        return animations.getAnimations();
    }

    static class AnimationContainer {
        private Map<String, AnimationData> animations;

        public Map<String, AnimationData> getAnimations() {
            return animations;
        }
    }

    static class AnimationData {
        public List<Keyframe> keyframes; // List of keyframes for complex animations
        public int duration; // Duration of the animation in ticks
    }

    static class Keyframe {
        public float time; // Time at which this keyframe occurs
        public Rotation rotation; // Rotation values for this keyframe
    }

    static class Rotation {
        public Axis rightArm; // Rotation for the right arm
        public Axis leftArm; // Rotation for the left arm
    }

    static class Axis {
        public float x; // Rotation around the X-axis
        public float y; // Rotation around the Y-axis
        public float z; // Rotation around the Z-axis
    }
}
