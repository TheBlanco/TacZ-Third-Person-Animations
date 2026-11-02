package com.skyplayer.tacztpanim.network;

import com.skyplayer.tacztpanim.TaczTpAnim;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public record AnimTriggerC2SPayload(String input) implements CustomPacketPayload {
    public static final Type<AnimTriggerC2SPayload> TYPE = new Type<>(TaczTpAnim.id("anim_trigger_c2s"));
    public static final StreamCodec<ByteBuf, AnimTriggerC2SPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.stringUtf8(32), AnimTriggerC2SPayload::input, AnimTriggerC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
