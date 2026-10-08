package com.greenlantern.ability;

import com.greenlantern.item.PowerRingItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Server-side ring flight: flight while a charged ring is in the inventory, draining energy. */
public final class RingFlight {
    private static final Set<UUID> ACTIVE = new HashSet<>();
    private static final float RING_FLY_SPEED = 0.12f;
    private static final float VANILLA_FLY_SPEED = 0.05f;

    public static boolean isActive(ServerPlayer p) {
        return ACTIVE.contains(p.getUUID());
    }

    public static void start(ServerPlayer p) {
        ACTIVE.add(p.getUUID());
        Abilities a = p.getAbilities();
        a.mayfly = true;
        a.flying = true;
        a.setFlyingSpeed(RING_FLY_SPEED);
        p.onUpdateAbilities();
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.5f);
        p.displayClientMessage(Component.translatable("message.greenlantern.flight_on").withStyle(ChatFormatting.GREEN), true);
    }

    public static void stop(ServerPlayer p, boolean cushionFall) {
        if (!ACTIVE.remove(p.getUUID())) return;
        Abilities a = p.getAbilities();
        a.setFlyingSpeed(VANILLA_FLY_SPEED);
        if (!p.isCreative() && !p.isSpectator()) {
            a.mayfly = false;
            a.flying = false;
        }
        p.onUpdateAbilities();
        if (cushionFall && !p.onGround()) {
            p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, false));
        }
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    public static void toggle(ServerPlayer p) {
        if (isActive(p)) {
            stop(p, true);
            p.displayClientMessage(Component.translatable("message.greenlantern.flight_off").withStyle(ChatFormatting.GRAY), true);
        } else {
            start(p);
        }
    }

    /** First ring in the inventory (optionally requiring energy). */
    public static ItemStack findRing(ServerPlayer p, boolean needCharge) {
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.getItem() instanceof PowerRingItem && (!needCharge || p.isCreative() || PowerRingItem.getCharge(s) > 0)) {
                return s;
            }
        }
        return null;
    }

    public static void tick(MinecraftServer server) {
        int t = server.getTickCount();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            // Slow passive regeneration: willpower returns over time.
            if (t % 100 == 0) {
                ItemStack r = findRing(p, false);
                if (r != null && PowerRingItem.getCharge(r) < PowerRingItem.MAX_CHARGE) {
                    PowerRingItem.setCharge(r, PowerRingItem.getCharge(r) + 1);
                }
            }

            Fx.handGlow(p, t);

            if (!isActive(p)) continue;

            ItemStack ring = findRing(p, true);
            if (ring == null) {
                stop(p, true);
                p.displayClientMessage(Component.translatable("message.greenlantern.out_of_power").withStyle(ChatFormatting.RED), true);
                continue;
            }

            Abilities a = p.getAbilities();
            if (!a.mayfly) {
                a.mayfly = true;
                p.onUpdateAbilities();
            }
            if (a.flying) {
                if (t % 20 == 0 && !p.isCreative()) {
                    PowerRingItem.setCharge(ring, PowerRingItem.getCharge(ring) - 1);
                }
                if (t % 2 == 0) Fx.flightTrail(p, t);
            }
        }
    }

    private RingFlight() {}
}
