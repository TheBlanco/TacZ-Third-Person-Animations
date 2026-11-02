package com.skyplayer.tacztpanim.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Server-side sends that skip clients which never negotiated our channels.
 * The registrar is optional, so players without the mod (or with a mismatched
 * build) can join; NeoForge throws if we send them one of our payloads.
 */
public final class SafeSend {
    // Viewers of each entity, mirrored from Start/StopTracking so tracking broadcasts can be filtered per player.
    private static final Map<Entity, Set<ServerPlayer>> TRACKERS = new WeakHashMap<>();
    private static boolean registered;

    private SafeSend() {
    }

    public static void register() {
        if (registered) return;
        registered = true;
        NeoForge.EVENT_BUS.addListener(SafeSend::onStartTracking);
        NeoForge.EVENT_BUS.addListener(SafeSend::onStopTracking);
        NeoForge.EVENT_BUS.addListener(SafeSend::onLogout);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        if (player.connection == null) return;
        if (player.connection.hasChannel(payload)) PacketDistributor.sendToPlayer(player, payload);
        for (CustomPacketPayload extra : payloads) {
            if (player.connection.hasChannel(extra)) PacketDistributor.sendToPlayer(player, extra);
        }
    }

    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        Set<ServerPlayer> viewers = TRACKERS.get(entity);
        if (viewers == null || viewers.isEmpty()) return;
        for (ServerPlayer viewer : new ArrayList<>(viewers)) {
            sendToPlayer(viewer, payload, payloads);
        }
    }

    private static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer viewer)) return;
        TRACKERS.computeIfAbsent(event.getTarget(), e -> Collections.newSetFromMap(new IdentityHashMap<>())).add(viewer);
    }

    private static void onStopTracking(PlayerEvent.StopTracking event) {
        Set<ServerPlayer> viewers = TRACKERS.get(event.getTarget());
        if (viewers == null) return;
        viewers.remove(event.getEntity());
        if (viewers.isEmpty()) TRACKERS.remove(event.getTarget());
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Entity player = event.getEntity();
        TRACKERS.remove(player);
        TRACKERS.values().removeIf(viewers -> viewers.remove(player) && viewers.isEmpty());
    }
}
