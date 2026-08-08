package com.chaevsfe.valence.core.net;

import com.chaevsfe.valence.core.ModConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SortPayload (int containerId, boolean playerSide) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<SortPayload> TYPE =
        new CustomPacketPayload.Type<>(ModConstants.loc("sort"));

    public static final StreamCodec<ByteBuf, SortPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, SortPayload::containerId,
        ByteBufCodecs.BOOL, SortPayload::playerSide,
        SortPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type () {
        return TYPE;
    }
}
