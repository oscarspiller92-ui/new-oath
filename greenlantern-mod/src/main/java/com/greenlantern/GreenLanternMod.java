package com.greenlantern;

import com.greenlantern.ability.Oath;
import com.greenlantern.ability.RingFlight;
import com.greenlantern.net.Net;
import com.greenlantern.registry.ModBlocks;
import com.greenlantern.registry.ModComponents;
import com.greenlantern.registry.ModItems;
import com.greenlantern.registry.ModTabs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.ResourceLocation;

public class GreenLanternMod implements ModInitializer {
    public static final String MOD_ID = "greenlantern";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModComponents.register();
        ModBlocks.register();
        ModItems.register();
        ModTabs.register();
        Net.register();

        ServerTickEvents.END_SERVER_TICK.register(RingFlight::tick);
        ServerTickEvents.END_SERVER_TICK.register(Oath::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> RingFlight.stop(handler.player, false));
    }
}
