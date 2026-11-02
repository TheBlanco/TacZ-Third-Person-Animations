package com.skyplayer.tacztpanim.network;

import com.skyplayer.tacztpanim.TaczTpAnim;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public record AnimTriggerS2CPayload(int entityId, String input) implements CustomPacketPayload {
    public static final Type<AnimTriggerS2CPayload> TYPE = new Type<>(TaczTpAnim.id("anim_trigger_s2c"));
    public static final StreamCodec<ByteBuf, AnimTriggerS2CPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, AnimTriggerS2CPayload::entityId, ByteBufCodecs.stringUtf8(32), AnimTriggerS2CPayload::input, AnimTriggerS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
