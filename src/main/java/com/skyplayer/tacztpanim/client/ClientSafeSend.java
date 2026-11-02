package com.skyplayer.tacztpanim.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client-side counterpart of SafeSend: skips sends when the server doesn't have the mod. */
public final class ClientSafeSend {
    private ClientSafeSend() {
    }

    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;
        if (connection.hasChannel(payload)) PacketDistributor.sendToServer(payload);
        for (CustomPacketPayload extra : payloads) {
            if (connection.hasChannel(extra)) PacketDistributor.sendToServer(extra);
        }
    }
}
