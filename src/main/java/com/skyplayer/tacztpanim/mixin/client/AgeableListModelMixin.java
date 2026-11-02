package com.skyplayer.tacztpanim.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.skyplayer.tacztpanim.client.ArmPoser;
import net.minecraft.client.model.AgeableListModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AgeableListModel.class})
public class AgeableListModelMixin {
    @Inject(
        method = {"renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"},
        at = {@At("TAIL")}
    )
    private void tacztpanim$renderBentArms(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color, CallbackInfo ci) {
        ArmPoser.renderBentArms(this, poseStack, consumer, light, overlay, color);
    }
}
