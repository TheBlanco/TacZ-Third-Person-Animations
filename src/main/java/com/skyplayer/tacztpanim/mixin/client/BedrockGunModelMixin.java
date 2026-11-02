package com.skyplayer.tacztpanim.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skyplayer.tacztpanim.client.ThirdPersonAnimator;
import com.tacz.guns.client.model.BedrockGunModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {BedrockGunModel.class},
    remap = false
)
public class BedrockGunModelMixin {
    @Inject(
        method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/client/renderer/RenderType;II)V"},
        at = {@At("HEAD")}
    )
    private void tacztpanim$captureFrame(
        PoseStack poseStack, ItemStack gunItem, ItemDisplayContext transformType, RenderType renderType, int light, int overlay, CallbackInfo ci
    ) {
        ThirdPersonAnimator.onGunModelRender((BedrockGunModel) (Object) this, poseStack);
    }
}
