package com.skyplayer.tacztpanim.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skyplayer.tacztpanim.client.ThirdPersonAnimator;
import com.tacz.guns.util.RenderDistance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    value = {RenderDistance.class},
    remap = false
)
public class RenderDistanceMixin {
    @Inject(
        method = {"inRenderHighPolyModelDistance"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private static void tacztpanim$keepHighPoly(PoseStack poseStack, CallbackInfoReturnable<Boolean> cir) {
        if (ThirdPersonAnimator.isAnimatingRender()) {
            cir.setReturnValue(true);
        }
    }
}
