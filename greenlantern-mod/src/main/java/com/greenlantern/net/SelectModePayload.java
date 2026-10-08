package com.greenlantern.net;

import com.greenlantern.GreenLanternMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: "switch my ring to this mode". */
public record SelectModePayload(int mode) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SelectModePayload> TYPE =
            new CustomPacketPayload.Type<>(GreenLanternMod.id("select_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectModePayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SelectModePayload::mode, SelectModePayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
