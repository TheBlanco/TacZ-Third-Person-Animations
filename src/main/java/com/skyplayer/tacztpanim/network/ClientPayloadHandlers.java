package com.skyplayer.tacztpanim.network;

import com.skyplayer.tacztpanim.client.ThirdPersonAnimator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

final class ClientPayloadHandlers {
    private ClientPayloadHandlers() {
    }

    static void handleSling(SlingInventoryPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                if (minecraft.level.getEntity(payload.entityId()) instanceof Player player && !player.isLocalPlayer()) {
                    for (int slot = 0; slot < 9; slot++) {
                        player.getInventory().setItem(slot, payload.hotbar().get(slot).copy());
                    }

                    player.getInventory().selected = payload.selected();
                }
            }
        });
    }

    static void handleTrigger(AnimTriggerS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ThirdPersonAnimator.onRemoteTrigger(payload.entityId(), payload.input()));
    }
}
