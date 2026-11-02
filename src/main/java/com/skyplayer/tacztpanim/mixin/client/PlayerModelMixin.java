package com.skyplayer.tacztpanim.mixin.client;

import com.skyplayer.tacztpanim.client.ArmPoser;
import com.skyplayer.tacztpanim.client.SlimArmsAccess;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {PlayerModel.class},
    priority = 1500
)
public class PlayerModelMixin implements SlimArmsAccess {
    @Shadow
    @Final
    private boolean slim;

    @Inject(
        method = {"setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"},
        at = {@At("TAIL")}
    )
    private void tacztpanim$followFirstPersonHands(
        LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci
    ) {
        ArmPoser.afterSetupAnim((PlayerModel<?>) (Object) this, entity, ageInTicks);
    }

    @Override
    public boolean tacztpanim$isSlim() {
        return this.slim;
    }
}
