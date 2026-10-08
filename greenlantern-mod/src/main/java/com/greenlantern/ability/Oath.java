package com.greenlantern.ability;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Speaks the (original) oath in chat, one line at a time, when a ring is charged at a Power Battery. */
public final class Oath {
    private static final int LINES = 4;
    private static final int TICKS_BETWEEN_LINES = 35;
    private static final double HEARING_RANGE = 48.0;

    private record Pending(UUID id, long due, int line) {}

    private static final List<Pending> QUEUE = new ArrayList<>();

    public static void speak(ServerPlayer player) {
        for (Pending p : QUEUE) {
            if (p.id().equals(player.getUUID())) return; // already reciting
        }
        long now = player.server.getTickCount();
        for (int i = 0; i < LINES; i++) {
            QUEUE.add(new Pending(player.getUUID(), now + (long) i * TICKS_BETWEEN_LINES, i + 1));
        }
    }

    public static void tick(MinecraftServer server) {
        if (QUEUE.isEmpty()) return;
        long now = server.getTickCount();
        Iterator<Pending> it = QUEUE.iterator();
        while (it.hasNext()) {
            Pending p = it.next();
            if (p.due() > now) continue;
            it.remove();

            ServerPlayer speaker = server.getPlayerList().getPlayer(p.id());
            if (speaker == null) continue;

            Component line = Component.translatable("oath.greenlantern." + p.line())
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC);
            Component msg = Component.literal("<").append(speaker.getDisplayName()).append("> ").append(line);
            for (ServerPlayer other : speaker.serverLevel().players()) {
                if (other.distanceToSqr(speaker) <= HEARING_RANGE * HEARING_RANGE) {
                    other.sendSystemMessage(msg);
                }
            }

            Fx.burst(speaker.serverLevel(), speaker.position().add(0, 1.0, 0), p.line() == LINES ? 40 : 10);
            speaker.serverLevel().playSound(null, speaker.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.8f, 0.8f + p.line() * 0.2f);
        }
    }

    private Oath() {}
}
