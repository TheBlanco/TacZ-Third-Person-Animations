package com.skyplayer.tacztpanim.network;

import com.tacz.guns.api.item.IGun;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private static final String VERSION = "1";
    public static final Set<String> RELAYED_INPUTS = Set.of(
        "reload", "cancel_reload", "shoot", "inspect", "blot", "fire_select", "bayonet_muzzle", "bayonet_stock", "bayonet_push"
    );

    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        boolean client = FMLEnvironment.dist == Dist.CLIENT;
        IPayloadHandler<SlingInventoryPayload> slingHandler = client ? ClientPayloadHandlers::handleSling : (payload, context) -> {};
        IPayloadHandler<AnimTriggerS2CPayload> triggerHandler = client ? ClientPayloadHandlers::handleTrigger : (payload, context) -> {};
        registrar.playToClient(SlingInventoryPayload.TYPE, SlingInventoryPayload.STREAM_CODEC, slingHandler);
        registrar.playToClient(AnimTriggerS2CPayload.TYPE, AnimTriggerS2CPayload.STREAM_CODEC, triggerHandler);
        registrar.playToServer(AnimTriggerC2SPayload.TYPE, AnimTriggerC2SPayload.STREAM_CODEC, ModNetwork::handleTriggerOnServer);
    }

    private static void handleTriggerOnServer(AnimTriggerC2SPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (RELAYED_INPUTS.contains(payload.input()) && IGun.mainHandHoldGun(player)) {
                    if (TriggerRateLimiter.tryAcquire(player)) {
                        SafeSend.sendToPlayersTrackingEntity(player, new AnimTriggerS2CPayload(player.getId(), payload.input()));
                    }
                }
            }
        });
    }
}
