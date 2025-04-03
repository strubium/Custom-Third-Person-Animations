package com.strubium.custom_animation_mod;

import com.strubium.custom_animation_mod.network.AnimationNetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class CustomAnimationMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        AnimationNetworkHandler.registerPackets();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
            MinecraftForge.EVENT_BUS.register(new AnimationEventHandlerClient());
            MinecraftForge.EVENT_BUS.register(new AnimationEventHandlerServer());
    }
}
