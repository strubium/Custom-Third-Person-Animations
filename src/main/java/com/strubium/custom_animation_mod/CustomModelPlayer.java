package com.strubium.custom_animation_mod;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class CustomModelPlayer extends ModelPlayer {
    private final Map<String, CustomAnimationLoader.AnimationData> animations = new HashMap<>();
    private String activeAnimation = null;
    private List<CustomAnimationLoader.Keyframe> keyframes;
    private int duration;
    private float animationProgress = 0;
    private boolean loopAnimation = false; // Flag to loop the animation

    // Constructor to accept multiple animations
    public CustomModelPlayer(float modelSize, boolean smallArms, Map<String, CustomAnimationLoader.AnimationData> animationDataMap) {
        super(modelSize, smallArms);
        if (animationDataMap != null) {
            this.animations.putAll(animationDataMap);
        }
    }

    public CustomModelPlayer() {
        super(0.0f, false);
        try {
            this.animations.putAll(CustomAnimationLoader.loadAnimations(new ResourceLocation(Tags.MOD_ID, "animations/animation.json")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // Set the current active animation, with support for stopping animations
    public void setActiveAnimation(String animationName) {
        if (animationName == null) {
            activeAnimation = null;
            keyframes = null;
            return;
        }

        if (!animationName.equals(activeAnimation)) {
            activeAnimation = animationName;
            CustomAnimationLoader.AnimationData animationData = animations.get(animationName);
            this.keyframes = animationData.keyframes;
            this.duration = animationData.duration;
            this.animationProgress = 0;
            this.loopAnimation = animationData.loop;
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        if (activeAnimation != null && keyframes != null && !keyframes.isEmpty()) {
            // Increment the animation progress
            animationProgress += 1;

            // Loop the animation or freeze it at the final frame
            if (loopAnimation) {
                if (animationProgress >= duration) {
                    animationProgress = 0; // Reset to start of animation
                }
            } else if (animationProgress >= duration) {
                animationProgress = duration - 1; // Hold at the last frame without resetting
            }

            float normalizedTime = animationProgress % duration;
            CustomAnimationLoader.Keyframe currentKeyframe = null;
            CustomAnimationLoader.Keyframe nextKeyframe = null;

            // Find the current and next keyframes based on the normalized time
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

            if (currentKeyframe != null) {
                if (nextKeyframe != null) {
                    // Interpolate between the current and next keyframe
                    float alpha = (normalizedTime - currentKeyframe.time) / (nextKeyframe.time - currentKeyframe.time);
                    updateRotation(currentKeyframe, nextKeyframe, alpha);
                } else {
                    // If no next keyframe, hold the last keyframe
                    updateRotation(currentKeyframe, currentKeyframe, 1.0f);
                }
            }
        }
    }

    // Helper method to update rotation angles using interpolation
    private void updateRotation(CustomAnimationLoader.Keyframe currentKeyframe, CustomAnimationLoader.Keyframe nextKeyframe, float alpha) {
        // Interpolate x, y, and z rotations for right and left arms
        float rightArmRotX = interpolate(currentKeyframe.rotation.rightArm.x, nextKeyframe.rotation.rightArm.x, alpha);
        float rightArmRotY = interpolate(currentKeyframe.rotation.rightArm.y, nextKeyframe.rotation.rightArm.y, alpha);
        float rightArmRotZ = interpolate(currentKeyframe.rotation.rightArm.z, nextKeyframe.rotation.rightArm.z, alpha);
        float leftArmRotX = interpolate(currentKeyframe.rotation.leftArm.x, nextKeyframe.rotation.leftArm.x, alpha);
        float leftArmRotY = interpolate(currentKeyframe.rotation.leftArm.y, nextKeyframe.rotation.leftArm.y, alpha);
        float leftArmRotZ = interpolate(currentKeyframe.rotation.leftArm.z, nextKeyframe.rotation.leftArm.z, alpha);

        // Apply rotations only if the values are non-zero
        if (rightArmRotX != 0 || rightArmRotY != 0 || rightArmRotZ != 0) {
            this.bipedRightArm.rotateAngleX = rightArmRotX;
            this.bipedRightArm.rotateAngleY = rightArmRotY;
            this.bipedRightArm.rotateAngleZ = rightArmRotZ;
            this.bipedRightArmwear.rotateAngleX = rightArmRotX;
            this.bipedRightArmwear.rotateAngleY = rightArmRotY;
            this.bipedRightArmwear.rotateAngleZ = rightArmRotZ;
        }

        if (leftArmRotX != 0 || leftArmRotY != 0 || leftArmRotZ != 0) {
            this.bipedLeftArm.rotateAngleX = leftArmRotX;
            this.bipedLeftArm.rotateAngleY = leftArmRotY;
            this.bipedLeftArm.rotateAngleZ = leftArmRotZ;
            this.bipedLeftArmwear.rotateAngleX = leftArmRotX;
            this.bipedLeftArmwear.rotateAngleY = leftArmRotY;
            this.bipedLeftArmwear.rotateAngleZ = leftArmRotZ;
        }
    }




    // Interpolation between two float values based on alpha
    private float interpolate(float start, float end, float alpha) {
        return start + (end - start) * alpha;
    }
}
