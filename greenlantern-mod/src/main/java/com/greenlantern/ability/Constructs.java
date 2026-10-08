package com.greenlantern.ability;

import com.greenlantern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Builders for the ring's temporary hard-light constructs. */
public final class Constructs {
    public static final int BRIDGE_LIFETIME = 240;
    public static final int SHIELD_LIFETIME = 160;

    private static boolean place(ServerLevel level, BlockPos pos, int life) {
        if (!level.getBlockState(pos).canBeReplaced()) return false;
        level.setBlock(pos, ModBlocks.ENERGY_CONSTRUCT.defaultBlockState(), 3);
        level.scheduleTick(pos, ModBlocks.ENERGY_CONSTRUCT, life);
        level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.25, 0.25, 0.25, 0.02);
        return true;
    }

    /** A 3-wide walkway that materialises outward, 16 blocks long. */
    public static void bridge(ServerLevel level, Player player) {
        Direction facing = player.getDirection();
        Direction side = facing.getClockWise();
        BlockPos origin = player.blockPosition().below();
        for (int step = 0; step < 16; step++) {
            for (int w = -1; w <= 1; w++) {
                BlockPos p = origin.relative(facing, step).relative(side, w);
                if (place(level, p, BRIDGE_LIFETIME + step * 3) && w == 0) {
                    level.sendParticles(Fx.GLOW, p.getX() + 0.5, p.getY() + 1.1, p.getZ() + 0.5, 3, 0.3, 0.05, 0.3, 0.0);
                }
            }
        }
    }

    /** A hollow sphere of hard light around the player, with an activation ripple. */
    public static void shield(ServerLevel level, Player player) {
        BlockPos c = player.blockPosition().above();
        double outer = 3.6, inner = 2.6;
        for (int dx = -4; dx <= 4; dx++)
            for (int dy = -4; dy <= 4; dy++)
                for (int dz = -4; dz <= 4; dz++) {
                    double d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 <= outer * outer && d2 >= inner * inner) {
                        place(level, c.offset(dx, dy, dz), SHIELD_LIFETIME);
                    }
                }
        Vec3 center = Vec3.atCenterOf(c);
        Fx.sphere(level, center, 3.2, 90);
        Fx.sphere(level, center, 1.8, 40);
    }

    /** A giant hard-light fist (with forearm) that slams whatever is in front of you. */
    public static void fist(ServerLevel level, Player player) {
        Vec3 dir = player.getLookAngle();
        Vec3 eye = player.getEyePosition();
        Vec3 center = eye.add(dir.scale(4.5));

        // The punch itself
        AABB box = new AABB(center, center).inflate(1.9);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, t -> t != player && t.isAlive())) {
            e.hurt(level.damageSources().playerAttack(player), 14.0f);
            e.setDeltaMovement(dir.x * 2.2, 0.55, dir.z * 2.2);
            e.hurtMarked = true;
        }

        // Forearm: a thin line of light from your hand to the fist
        for (double d = 1.8; d < 3.3; d += 0.5) {
            place(level, BlockPos.containing(eye.add(dir.scale(d)).add(0, -0.3, 0)), 12);
        }
        // Fist: 3x3x3 cube
        BlockPos c = BlockPos.containing(center);
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                for (int dz = -1; dz <= 1; dz++) {
                    place(level, c.offset(dx, dy, dz), 12);
                }

        Fx.shockwave(level, center, dir, 3.0);
        level.playSound(null, BlockPos.containing(center), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.PLAYERS, 1.2f, 0.6f);
    }

    private Constructs() {}
}
