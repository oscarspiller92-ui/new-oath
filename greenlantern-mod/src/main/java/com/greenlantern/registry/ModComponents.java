package com.greenlantern.registry;

import com.greenlantern.GreenLanternMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ModComponents {
    /** Remaining ring energy. */
    public static final DataComponentType<Integer> CHARGE = reg("charge",
            DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    /** Selected ring mode (ordinal of PowerRingItem.Mode). */
    public static final DataComponentType<Integer> MODE = reg("mode",
            DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    private static <T> DataComponentType<T> reg(String name, DataComponentType.Builder<T> builder) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, GreenLanternMod.id(name), builder.build());
    }

    public static void register() {}
    private ModComponents() {}
}
