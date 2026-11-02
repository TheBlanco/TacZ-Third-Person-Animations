package com.skyplayer.tacztpanim.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.skyplayer.tacztpanim.client.ArmPoser;
import com.skyplayer.tacztpanim.client.ThirdPersonAnimator;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandLayer.class})
public class ItemInHandLayerMixin {
    private static final String RENDER = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V";
    private static final String RENDER_ARM_WITH_ITEM = "renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Inject(
        method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"},
        at = {@At("HEAD")}
    )
    private void tacztpanim$beginLayer(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        LivingEntity entity,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        CallbackInfo ci
    ) {
        ThirdPersonAnimator.setRenderingEntity(entity);
    }

    @Inject(
        method = {"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"},
        at = {@At("RETURN")}
    )
    private void tacztpanim$endLayer(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        LivingEntity entity,
        float limbSwing,
        float limbSwingAmount,
        float partialTicks,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        CallbackInfo ci
    ) {
        ThirdPersonAnimator.setRenderingEntity(null);
        ArmPoser.clearArmFrame();
    }

    @WrapOperation(
        method = {"renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
        )}
    )
    private void tacztpanim$holdWithOriginalArm(ArmedModel model, HumanoidArm arm, PoseStack poseStack, Operation<Void> original) {
        ArmPoser.translateToHandWithOriginalArms(model, arm, poseStack, () -> original.call(new Object[]{model, arm, poseStack}));
    }

    @Inject(
        method = {"renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"},
        at = {@At("RETURN")}
    )
    private void tacztpanim$endArm(
        LivingEntity entity,
        ItemStack stack,
        ItemDisplayContext displayContext,
        HumanoidArm arm,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        ArmPoser.clearArmFrame();
    }
}
