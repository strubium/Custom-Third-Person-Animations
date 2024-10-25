package com.example.modid;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class CustomModelPlayer extends ModelPlayer {
    private List<CustomAnimationLoader.Keyframe> keyframes; // List of keyframes for the animation
    private int duration; // Duration of the animation in ticks

    // Updated constructor to accept animation data
    public CustomModelPlayer(float modelSize, boolean smallArms, CustomAnimationLoader.AnimationData animationData) {
        super(modelSize, smallArms);
        if (animationData != null) {
            this.keyframes = animationData.keyframes;
            this.duration = animationData.duration;
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        // Custom animation logic using keyframes
        if (keyframes != null && !keyframes.isEmpty()) {
            // Determine the current keyframe based on ageInTicks
            float normalizedTime = ageInTicks % duration; // Loop back after duration
            CustomAnimationLoader.Keyframe currentKeyframe = null;
            CustomAnimationLoader.Keyframe nextKeyframe = null;

            // Find the current and next keyframes
            for (int i = 0; i < keyframes.size(); i++) {
                CustomAnimationLoader.Keyframe kf = keyframes.get(i);
                if (kf.time <= normalizedTime) {
                    currentKeyframe = kf;
                }
                if (i < keyframes.size() - 1 && keyframes.get(i + 1).time > normalizedTime) {
                    nextKeyframe = keyframes.get(i + 1);
                    break;
                }
            }

            if (currentKeyframe != null && nextKeyframe != null) {
                // Interpolate between the current and next keyframes
                float alpha = (normalizedTime - currentKeyframe.time) / (nextKeyframe.time - currentKeyframe.time);
                this.bipedRightArm.rotateAngleX = interpolate(currentKeyframe.rotation.rightArm.x, nextKeyframe.rotation.rightArm.x, alpha);
                this.bipedLeftArm.rotateAngleX = interpolate(currentKeyframe.rotation.leftArm.x, nextKeyframe.rotation.leftArm.x, alpha);
            }
        }
    }

    // Interpolation function
    private float interpolate(float start, float end, float alpha) {
        return start + (end - start) * alpha;
    }
}
