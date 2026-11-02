package com.skyplayer.tacztpanim.sync;

import com.skyplayer.tacztpanim.network.SafeSend;
import com.skyplayer.tacztpanim.network.SlingInventoryPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.tick.PlayerTickEvent.Post;

public final class HotbarSnapshotSync {
    private static final int CHECK_INTERVAL_TICKS = 5;
    private static final Map<UUID, SlingInventoryPayload> LAST_SENT = new WeakHashMap<>();

    private HotbarSnapshotSync() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(HotbarSnapshotSync::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(HotbarSnapshotSync::onStartTracking);
        NeoForge.EVENT_BUS.addListener(HotbarSnapshotSync::onLogout);
    }

    private static void onPlayerTick(Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 5 == 0) {
            SlingInventoryPayload snapshot = snapshot(player);
            SlingInventoryPayload last = LAST_SENT.get(player.getUUID());
            if (last == null || !sameSnapshot(last, snapshot)) {
                LAST_SENT.put(player.getUUID(), snapshot);
                SafeSend.sendToPlayersTrackingEntity(player, snapshot);
            }
        }
    }

    private static void onStartTracking(StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer target && event.getEntity() instanceof ServerPlayer viewer) {
            SafeSend.sendToPlayer(viewer, snapshot(target));
        }
    }

    private static void onLogout(PlayerLoggedOutEvent event) {
        LAST_SENT.remove(event.getEntity().getUUID());
    }

    private static SlingInventoryPayload snapshot(ServerPlayer player) {
        List<ItemStack> hotbar = new ArrayList<>(9);

        for (int slot = 0; slot < 9; slot++) {
            hotbar.add(player.getInventory().getItem(slot).copy());
        }

        return new SlingInventoryPayload(player.getId(), player.getInventory().selected, hotbar);
    }

    private static boolean sameSnapshot(SlingInventoryPayload a, SlingInventoryPayload b) {
        if (a.selected() != b.selected()) {
            return false;
        } else {
            for (int slot = 0; slot < 9; slot++) {
                if (!ItemStack.matches(a.hotbar().get(slot), b.hotbar().get(slot))) {
                    return false;
                }
            }

            return true;
        }
    }
}
