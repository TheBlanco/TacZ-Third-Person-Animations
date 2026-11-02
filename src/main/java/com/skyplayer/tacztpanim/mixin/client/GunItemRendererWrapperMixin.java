package com.skyplayer.tacztpanim.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.skyplayer.tacztpanim.client.ThirdPersonAnimator;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    value = {GunItemRendererWrapper.class},
    remap = false
)
public class GunItemRendererWrapperMixin {
    private static final String RENDER_BY_ITEM = "renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V";

    @Inject(
        method = {"renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"},
        at = {@At("HEAD")}
    )
    private void tacztpanim$beginRender(
        ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, CallbackInfo ci
    ) {
        ThirdPersonAnimator.beginRender(stack, displayContext);
    }

    @Inject(
        method = {"renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"},
        at = {@At("RETURN")}
    )
    private void tacztpanim$endRender(
        ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, CallbackInfo ci
    ) {
        ThirdPersonAnimator.endRender();
    }
}
