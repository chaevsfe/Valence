package com.chaevsfe.valence.core.net;

import com.chaevsfe.valence.core.ModConstants;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HelloPayload (int protocol, List<String> enabled) implements CustomPacketPayload
{
    public static final int PROTOCOL = 1;

    public static final CustomPacketPayload.Type<HelloPayload> TYPE =
        new CustomPacketPayload.Type<>(ModConstants.loc("hello"));

    public static final StreamCodec<ByteBuf, HelloPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, HelloPayload::protocol,
        ByteBufCodecs.stringUtf8(64).apply(ByteBufCodecs.list()), HelloPayload::enabled,
        HelloPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type () {
        return TYPE;
    }
}
