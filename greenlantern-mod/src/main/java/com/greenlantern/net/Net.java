package com.greenlantern.net;

import com.greenlantern.item.PowerRingItem;
import com.greenlantern.registry.ModComponents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public final class Net {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(SelectModePayload.TYPE, SelectModePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SelectModePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            PowerRingItem.Mode[] modes = PowerRingItem.Mode.values();
            int i = payload.mode();
            if (i < 0 || i >= modes.length) return;
            ItemStack ring = PowerRingItem.find(player);
            if (ring.isEmpty()) return;

            ring.set(ModComponents.MODE, i);
            player.displayClientMessage(Component.translatable("message.greenlantern.mode", modes[i].displayName())
                    .withStyle(ChatFormatting.GREEN), true);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 1.0f, 1.6f);
        });
    }

    private Net() {}
}
