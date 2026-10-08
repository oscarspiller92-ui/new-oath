package com.greenlantern.ability;

import com.greenlantern.item.PowerRingItem;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** All the green-light visuals in one place. */
public final class Fx {
    /** Simple emerald dust (also usable client-side). */
    public static final DustParticleOptions GREEN_DUST = new DustParticleOptions(new Vector3f(0.15f, 1.0f, 0.35f), 1.4f);
    /** Bright, nearly white-green core of beams. */
    public static final DustParticleOptions CORE = new DustParticleOptions(new Vector3f(0.75f, 1.0f, 0.82f), 2.2f);
    /** Pale lime that fades into deep emerald: the "burning willpower" look. */
    public static final DustColorTransitionOptions GLOW = new DustColorTransitionOptions(
            new Vector3f(0.6f, 1.0f, 0.7f), new Vector3f(0.04f, 0.75f, 0.25f), 1.6f);

    public static void burst(ServerLevel level, Vec3 at, int count) {
        level.sendParticles(GLOW, at.x, at.y, at.z, count, 0.6, 0.6, 0.6, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, Math.max(1, count / 4), 0.5, 0.5, 0.5, 0.05);
    }

    private static Vec3[] basis(Vec3 dir) {
        Vec3 ref = Math.abs(dir.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = dir.cross(ref).normalize();
        Vec3 up = right.cross(dir).normalize();
        return new Vec3[]{right, up};
    }

    /** A thick energy beam: bright core with a double helix spiralling around it. */
    public static void beam(ServerLevel level, Vec3 origin, Vec3 dir, double length) {
        Vec3[] b = basis(dir);
        int i = 0;
        for (double d = 0; d < length; d += 0.35, i++) {
            Vec3 c = origin.add(dir.scale(d));
            double r = 0.30 * Math.min(1.0, d / 2.0 + 0.3);
            level.sendParticles(CORE, c.x, c.y, c.z, 1, 0, 0, 0, 0);
            for (int k = 0; k < 2; k++) {
                double a = d * 2.4 + k * Math.PI;
                Vec3 p = c.add(b[0].scale(Math.cos(a) * r)).add(b[1].scale(Math.sin(a) * r));
                level.sendParticles(GLOW, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
            if (i % 4 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, c.x, c.y, c.z, 1, 0.06, 0.06, 0.06, 0.01);
            }
        }
    }

    /** Expanding concentric rings plus sparks, facing along `normal`. */
    public static void shockwave(ServerLevel level, Vec3 at, Vec3 normal, double maxRadius) {
        Vec3[] b = basis(normal);
        for (double r = 0.5; r <= maxRadius; r += 0.55) {
            int n = 10 + (int) (r * 9);
            for (int j = 0; j < n; j++) {
                double a = Math.PI * 2 * j / n;
                Vec3 p = at.add(b[0].scale(Math.cos(a) * r)).add(b[1].scale(Math.sin(a) * r));
                level.sendParticles(GLOW, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
        }
        level.sendParticles(CORE, at.x, at.y, at.z, 4, 0.15, 0.15, 0.15, 0.0);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 24, 0.3, 0.3, 0.3, 0.35);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 14, 0.2, 0.2, 0.2, 0.18);
    }

    /** Evenly spaced points on a sphere (shield activation ripple). */
    public static void sphere(ServerLevel level, Vec3 center, double radius, int points) {
        double golden = Math.PI * (3 - Math.sqrt(5));
        for (int i = 0; i < points; i++) {
            double y = 1 - (i / (double) (points - 1)) * 2;
            double rr = Math.sqrt(1 - y * y);
            double th = golden * i;
            level.sendParticles(GLOW, center.x + Math.cos(th) * rr * radius, center.y + y * radius,
                    center.z + Math.sin(th) * rr * radius, 1, 0, 0, 0, 0);
        }
    }

    /** The ring glowing on your hand while you hold it. */
    public static void handGlow(ServerPlayer p, int tick) {
        if (tick % 3 != 0) return;
        if (!(p.getMainHandItem().getItem() instanceof PowerRingItem)
                && !(p.getOffhandItem().getItem() instanceof PowerRingItem)) return;
        Vec3 look = p.getLookAngle();
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 pos = p.getEyePosition().add(look.scale(0.55)).add(right.scale(0.38)).add(0, -0.45, 0);
        ServerLevel level = p.serverLevel();
        level.sendParticles(GLOW, pos.x, pos.y, pos.z, 1, 0.04, 0.04, 0.04, 0.0);
        if (tick % 9 == 0) level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0.06, 0.06, 0.06, 0.01);
    }

    /** Spiral aura plus a glowing wake while flying. */
    public static void flightTrail(ServerPlayer p, int tick) {
        ServerLevel level = p.serverLevel();
        double h = (tick % 24) / 24.0 * 1.6;
        for (int k = 0; k < 2; k++) {
            double a = tick * 0.55 + k * Math.PI;
            level.sendParticles(GLOW, p.getX() + Math.cos(a) * 0.6, p.getY() + 0.1 + h, p.getZ() + Math.sin(a) * 0.6, 1, 0, 0, 0, 0);
        }
        level.sendParticles(CORE, p.getX(), p.getY() + 0.1, p.getZ(), 1, 0.25, 0.05, 0.25, 0.0);
        if (tick % 4 == 0) level.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 0.3, p.getZ(), 1, 0.3, 0.2, 0.3, 0.02);
    }

    private Fx() {}
}
