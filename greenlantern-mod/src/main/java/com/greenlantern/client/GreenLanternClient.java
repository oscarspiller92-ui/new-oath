package com.greenlantern.client;

import com.greenlantern.item.PowerRingItem;
import com.greenlantern.registry.ModBlocks;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.glfw.GLFW;

public class GreenLanternClient implements ClientModInitializer {
    public static final KeyMapping WHEEL_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.greenlantern.wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.greenlantern"));

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.ENERGY_CONSTRUCT, RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.POWER_BATTERY, RenderType.cutout());

        HudRenderCallback.EVENT.register(RingHud::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (WHEEL_KEY.consumeClick()) {
                if (client.player != null && client.screen == null && !PowerRingItem.find(client.player).isEmpty()) {
                    client.setScreen(new ConstructWheelScreen());
                }
            }
        });
    }
}
