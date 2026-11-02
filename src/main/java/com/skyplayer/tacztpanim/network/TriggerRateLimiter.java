package com.skyplayer.tacztpanim.network;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;

final class TriggerRateLimiter {
    private static final int MAX_PER_SECOND = 40;
    private static final Map<UUID, long[]> WINDOWS = new WeakHashMap<>();

    private TriggerRateLimiter() {
    }

    static boolean tryAcquire(ServerPlayer player) {
        long second = player.server.getTickCount() / 20;
        long[] window = WINDOWS.computeIfAbsent(player.getUUID(), uuid -> new long[]{second, 0L});
        if (window[0] != second) {
            window[0] = second;
            window[1] = 0L;
        }

        return ++window[1] <= 40L;
    }
}
