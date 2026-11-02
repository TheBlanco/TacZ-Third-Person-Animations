package com.skyplayer.tacztpanim;

import com.skyplayer.tacztpanim.client.ClientConfig;
import com.skyplayer.tacztpanim.client.ThirdPersonAnimator;
import com.skyplayer.tacztpanim.network.ModNetwork;
import com.skyplayer.tacztpanim.network.SafeSend;
import com.skyplayer.tacztpanim.sync.HotbarSnapshotSync;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod("tacz_tp_anim")
public final class TaczTpAnim {
    public static final String MOD_ID = "tacz_tp_anim";

    public TaczTpAnim(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(ModNetwork::register);
        SafeSend.register();
        HotbarSnapshotSync.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            container.registerConfig(Type.CLIENT, ClientConfig.SPEC);
            ThirdPersonAnimator.register();
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("tacz_tp_anim", path);
    }
}
