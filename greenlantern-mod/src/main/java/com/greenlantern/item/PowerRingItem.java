package com.greenlantern.item;

import com.greenlantern.ability.Constructs;
import com.greenlantern.ability.Fx;
import com.greenlantern.ability.RingFlight;
import com.greenlantern.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;

public class PowerRingItem extends Item {
    public static final int MAX_CHARGE = 200;

    public enum Mode {
        BLAST(5, 8),
        FIST(20, 25),
        BRIDGE(15, 30),
        SHIELD(30, 60),
        FLIGHT(0, 10);

        public final int cost;
        public final int cooldown;

        Mode(int cost, int cooldown) {
            this.cost = cost;
            this.cooldown = cooldown;
        }

        public Mode next() {
            Mode[] v = values();
            return v[(ordinal() + 1) % v.length];
        }

        public Component displayName() {
            return Component.translatable("mode.greenlantern." + name().toLowerCase(Locale.ROOT));
        }
    }

    public PowerRingItem(Properties properties) {
        super(properties);
    }

    // ---- state helpers ----
    public static int getCharge(ItemStack s) {
        return s.getOrDefault(ModComponents.CHARGE, MAX_CHARGE);
    }

    public static void setCharge(ItemStack s, int v) {
        s.set(ModComponents.CHARGE, Mth.clamp(v, 0, MAX_CHARGE));
    }

    public static Mode getMode(ItemStack s) {
        Mode[] v = Mode.values();
        return v[Mth.clamp(s.getOrDefault(ModComponents.MODE, 0), 0, v.length - 1)];
    }

    /** The ring this player is using: main hand, then off hand, then anywhere in the inventory. */
    public static ItemStack find(Player p) {
        if (p.getMainHandItem().getItem() instanceof PowerRingItem) return p.getMainHandItem();
        if (p.getOffhandItem().getItem() instanceof PowerRingItem) return p.getOffhandItem();
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.getItem() instanceof PowerRingItem) return s;
        }
        return ItemStack.EMPTY;
    }

    // ---- use ----
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Sneak + use: cycle mode
        if (player.isShiftKeyDown()) {
            Mode next = getMode(stack).next();
            stack.set(ModComponents.MODE, next.ordinal());
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.greenlantern.mode", next.displayName())
                        .withStyle(ChatFormatting.GREEN), true);
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.6f);
            }
            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.success(stack);
        }

        Mode mode = getMode(stack);
        boolean free = player.getAbilities().instabuild;

        if (mode == Mode.FLIGHT) {
            if (!RingFlight.isActive(sp) && !free && getCharge(stack) <= 0) {
                return fizzle(player, stack);
            }
            RingFlight.toggle(sp);
            player.getCooldowns().addCooldown(this, mode.cooldown);
            return InteractionResultHolder.success(stack);
        }

        if (!free && getCharge(stack) < mode.cost) {
            return fizzle(player, stack);
        }

        switch (mode) {
            case BLAST -> blast(serverLevel, sp);
            case BRIDGE -> {
                Constructs.bridge(serverLevel, sp);
                serverLevel.playSound(null, sp.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f, 0.8f);
            }
            case SHIELD -> {
                Constructs.shield(serverLevel, sp);
                serverLevel.playSound(null, sp.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 1.0f, 1.4f);
            }
            case FIST -> Constructs.fist(serverLevel, sp);
            default -> {}
        }

        if (!free) setCharge(stack, getCharge(stack) - mode.cost);
        player.getCooldowns().addCooldown(this, mode.cooldown);
        return InteractionResultHolder.success(stack);
    }

    private InteractionResultHolder<ItemStack> fizzle(Player player, ItemStack stack) {
        player.displayClientMessage(Component.translatable("message.greenlantern.low_power").withStyle(ChatFormatting.RED), true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8f, 1.2f);
        return InteractionResultHolder.fail(stack);
    }

    private void blast(ServerLevel level, ServerPlayer player) {
        double range = 48.0;
        Vec3 start = player.getEyePosition();
        Vec3 dir = player.getLookAngle();
        Vec3 end = start.add(dir.scale(range));

        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDist = blockHit.getType() == HitResult.Type.MISS ? range : blockHit.getLocation().distanceTo(start);
        Vec3 beamEnd = start.add(dir.scale(maxDist));

        AABB box = player.getBoundingBox().expandTowards(dir.scale(maxDist)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player, start, beamEnd, box, e -> !e.isSpectator() && e.isPickable(), maxDist * maxDist);

        if (entityHit != null) {
            beamEnd = entityHit.getLocation();
            maxDist = beamEnd.distanceTo(start);
            if (entityHit.getEntity() instanceof LivingEntity target) {
                target.hurt(level.damageSources().playerAttack(player), 10.0f);
                target.push(dir.x * 1.2, 0.35, dir.z * 1.2);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));
            }
        }

        // Visuals: muzzle flash, spiralling beam, impact shockwave
        Vec3 origin = start.add(dir.scale(0.8)).add(0, -0.25, 0);
        Fx.burst(level, origin, 10);
        Fx.beam(level, origin, dir, Math.max(0.0, maxDist - 0.8));
        Fx.shockwave(level, beamEnd, dir, 1.7);
        level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.2f, 1.6f);
        level.playSound(null, BlockPos.containing(beamEnd), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1.0f, 0.6f);
    }

    // ---- visuals ----
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getCharge(stack) / MAX_CHARGE);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x2BFF5A;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.translatable("tooltip.greenlantern.charge", getCharge(stack), MAX_CHARGE).withStyle(ChatFormatting.GREEN));
        tip.add(Component.translatable("tooltip.greenlantern.mode", getMode(stack).displayName()).withStyle(ChatFormatting.DARK_GREEN));
        tip.add(Component.translatable("tooltip.greenlantern.hint1").withStyle(ChatFormatting.GRAY));
        tip.add(Component.translatable("tooltip.greenlantern.hint2").withStyle(ChatFormatting.GRAY));
    }
}
