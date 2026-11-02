package com.skyplayer.tacztpanim.mixin.client;

import com.skyplayer.tacztpanim.client.ThirdPersonAnimator;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {AnimationStateMachine.class},
    remap = false
)
public class AnimationStateMachineMixin {
    @Inject(
        method = {"trigger"},
        at = {@At("HEAD")}
    )
    private void tacztpanim$onTrigger(String condition, CallbackInfo ci) {
        ThirdPersonAnimator.onStateMachineTrigger((AnimationStateMachine<?>) (Object) this, condition);
    }
}
