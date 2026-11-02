package com.skyplayer.tacztpanim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.resource.GunDisplayInstance;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ArmPoser {
    static final float UPPER_LENGTH = 0.25F;
    static final float FOREARM_LENGTH = 0.3125F;
    private static final Map<GunDisplayInstance, Matrix4f[]> GUN_FRAMES = new WeakHashMap<>();
    private static PlayerModel<?> posedModel;
    private static LivingEntity posedEntity;
    private static final float[] ORIGINAL_RIGHT = new float[3];
    private static final float[] ORIGINAL_LEFT = new float[3];
    private static final ArmPoser.BentArm BENT_RIGHT = new ArmPoser.BentArm();
    private static final ArmPoser.BentArm BENT_LEFT = new ArmPoser.BentArm();
    private static Matrix4f pendingArmFrame;
    private static HumanoidArm pendingArm;

    private ArmPoser() {
    }

    public static void afterSetupAnim(PlayerModel<?> model, LivingEntity entity, float ageInTicks) {
        if (posedModel == model) {
            posedModel = null;
            posedEntity = null;
        }

        if (ageInTicks != 0.0F && ClientConfig.ANIMATE_ARMS.get() && entity instanceof Player player) {
            if (!player.isSleeping() && !player.isSwimming() && !player.isFallFlying() && !player.onClimbable()) {
                ItemStack stack = player.getMainHandItem();
                ThirdPersonAnimator.HandTargets hands = ThirdPersonAnimator.sampleHands(player, stack);
                if (hands != null) {
                    HumanoidArm gunArm = player.getMainArm();
                    Matrix4f[] frames = GUN_FRAMES.get(hands.display());
                    Matrix4f armToGun = frames == null ? null : frames[gunArm == HumanoidArm.RIGHT ? 0 : 1];
                    if (armToGun != null) {
                        save(model.rightArm, ORIGINAL_RIGHT);
                        save(model.leftArm, ORIGINAL_LEFT);
                        PoseStack poseStack = new PoseStack();
                        model.translateToHand(gunArm, poseStack);
                        Matrix4f gunFrame = new Matrix4f(poseStack.last().pose()).mul(armToGun);
                        float strength = ClientConfig.ARM_FOLLOW_STRENGTH.get().floatValue();
                        if (!isCustomPlayerModelPart(model.leftArm) && !isCustomPlayerModelPart(model.rightArm)) {
                            BENT_LEFT.active = hands.left() != null
                                && solve(model.leftArm, gunFrame.transformPosition(hands.left(), new Vector3f()), strength, 1.0F, BENT_LEFT);
                            BENT_RIGHT.active = hands.right() != null
                                && solve(model.rightArm, gunFrame.transformPosition(hands.right(), new Vector3f()), strength, -1.0F, BENT_RIGHT);
                            if (BENT_LEFT.active || BENT_RIGHT.active) {
                                model.leftSleeve.copyFrom(model.leftArm);
                                model.rightSleeve.copyFrom(model.rightArm);
                                hideIfBent(model.leftArm, model.leftSleeve, BENT_LEFT);
                                hideIfBent(model.rightArm, model.rightSleeve, BENT_RIGHT);
                                posedModel = model;
                                posedEntity = entity;
                            }
                        } else {
                            BENT_LEFT.active = false;
                            BENT_RIGHT.active = false;
                            if (hands.left() != null) {
                                aimStraight(model.leftArm, gunFrame.transformPosition(hands.left(), new Vector3f()), strength);
                            }

                            if (hands.right() != null) {
                                aimStraight(model.rightArm, gunFrame.transformPosition(hands.right(), new Vector3f()), strength);
                            }

                            model.leftSleeve.copyFrom(model.leftArm);
                            model.rightSleeve.copyFrom(model.rightArm);
                            posedModel = model;
                            posedEntity = entity;
                        }
                    }
                }
            }
        }
    }

    private static boolean isCustomPlayerModelPart(ModelPart part) {
        return part.getClass().getName().startsWith("com.tom.cpm.");
    }

    private static void aimStraight(ModelPart arm, Vector3f target, float strength) {
        if (arm.visible) {
            Vector3f shoulder = new Vector3f(arm.x / 16.0F, arm.y / 16.0F, arm.z / 16.0F);
            Vector3f direction = new Vector3f(target).sub(shoulder);
            if (!(direction.lengthSquared() < 1.0E-8F)) {
                direction.normalize();
                if (strength < 1.0F) {
                    direction = direction(arm).lerp(direction, strength).normalize();
                }

                Vector3f euler = new Quaternionf().rotationTo(0.0F, 1.0F, 0.0F, direction.x, direction.y, direction.z).getEulerAnglesZYX(new Vector3f());
                arm.xRot = euler.x;
                arm.yRot = euler.y;
                arm.zRot = euler.z;
            }
        }
    }

    private static void hideIfBent(ModelPart arm, ModelPart sleeve, ArmPoser.BentArm bent) {
        if (bent.active) {
            bent.sleeveVisible = sleeve.visible;
            arm.visible = false;
            sleeve.visible = false;
        }
    }

    private static boolean solve(ModelPart arm, Vector3f target, float strength, float side, ArmPoser.BentArm out) {
        if (!arm.visible) {
            return false;
        } else {
            Vector3f shoulder = new Vector3f(arm.x / 16.0F, arm.y / 16.0F, arm.z / 16.0F);
            if (strength < 1.0F) {
                Vector3f original = direction(arm).mul(0.5625F).add(shoulder);
                target = original.lerp(target, strength);
            }

            Vector3f toTarget = new Vector3f(target).sub(shoulder);
            float distance = toTarget.length();
            if (distance < 1.0E-4F) {
                return false;
            } else {
                Vector3f axis = new Vector3f(toTarget).div(distance);
                float reach = Mth.clamp(distance, Math.abs(-0.0625F) + 0.001F, 0.5624F);
                float along = (-0.03515625F + reach * reach) / (2.0F * reach);
                float height = (float)Math.sqrt(Math.max(0.0F, 0.0625F - along * along));
                Vector3f pole = new Vector3f(side * 0.8F, 1.0F, 0.3F);
                pole.sub(new Vector3f(axis).mul(pole.dot(axis)));
                if (pole.lengthSquared() < 1.0E-6F) {
                    pole.set(side, 0.0F, 0.0F);
                }

                pole.normalize();
                Vector3f elbow = new Vector3f(axis).mul(along).add(new Vector3f(pole).mul(height)).add(shoulder);
                Vector3f hand = new Vector3f(axis).mul(Math.min(distance, 0.5625F)).add(shoulder);
                Vector3f upperDir = new Vector3f(elbow).sub(shoulder).normalize();
                Vector3f foreDir = new Vector3f(hand).sub(elbow);
                if (foreDir.lengthSquared() < 1.0E-8F) {
                    foreDir.set(upperDir);
                }

                foreDir.normalize();
                out.shoulder.set(shoulder);
                out.elbow.set(elbow);
                pointAlong(upperDir, out.upperRotation);
                out.foreRotation.rotationTo(upperDir, foreDir).mul(out.upperRotation);
                Vector3f euler = out.upperRotation.getEulerAnglesZYX(new Vector3f());
                arm.xRot = euler.x;
                arm.yRot = euler.y;
                arm.zRot = euler.z;
                return true;
            }
        }
    }

    private static void pointAlong(Vector3f direction, Quaternionf into) {
        into.rotationTo(0.0F, 1.0F, 0.0F, direction.x, direction.y, direction.z);
    }

    private static Vector3f direction(ModelPart arm) {
        return new Quaternionf().rotationZYX(arm.zRot, arm.yRot, arm.xRot).transform(new Vector3f(0.0F, 1.0F, 0.0F));
    }

    public static void renderBentArms(Object model, PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
        if (model == posedModel && posedModel != null) {
            boolean slim = ((SlimArmsAccess)posedModel).tacztpanim$isSlim();
            if (BENT_LEFT.active) {
                BentArmRenderer.render(poseStack, consumer, light, overlay, color, BENT_LEFT, false, slim);
            }

            if (BENT_RIGHT.active) {
                BentArmRenderer.render(poseStack, consumer, light, overlay, color, BENT_RIGHT, true, slim);
            }
        }
    }

    public static void translateToHandWithOriginalArms(Object model, HumanoidArm arm, PoseStack poseStack, Runnable translateToHand) {
        boolean swap = model == posedModel && posedModel != null && ThirdPersonAnimator.getRenderingEntity() == posedEntity;
        float[] right = new float[3];
        float[] left = new float[3];
        if (swap) {
            save(posedModel.rightArm, right);
            save(posedModel.leftArm, left);
            load(posedModel.rightArm, ORIGINAL_RIGHT);
            load(posedModel.leftArm, ORIGINAL_LEFT);
        }

        try {
            translateToHand.run();
        } finally {
            if (swap) {
                load(posedModel.rightArm, right);
                load(posedModel.leftArm, left);
            }
        }

        pendingArmFrame = new Matrix4f(poseStack.last().pose());
        pendingArm = arm;
    }

    public static void clearArmFrame() {
        pendingArmFrame = null;
        pendingArm = null;
    }

    static void captureGunFrame(GunDisplayInstance display, Matrix4f gunPose) {
        if (pendingArmFrame != null && pendingArm != null) {
            Matrix4f armToGun = new Matrix4f(pendingArmFrame).invert().mul(gunPose);
            if (armToGun.isFinite()) {
                GUN_FRAMES.computeIfAbsent(display, ignored -> new Matrix4f[2])[pendingArm == HumanoidArm.RIGHT ? 0 : 1] = armToGun;
            }
        }
    }

    private static void save(ModelPart part, float[] into) {
        into[0] = part.xRot;
        into[1] = part.yRot;
        into[2] = part.zRot;
    }

    private static void load(ModelPart part, float[] from) {
        part.xRot = from[0];
        part.yRot = from[1];
        part.zRot = from[2];
    }

    static final class BentArm {
        boolean active;
        boolean sleeveVisible;
        final Vector3f shoulder = new Vector3f();
        final Vector3f elbow = new Vector3f();
        final Quaternionf upperRotation = new Quaternionf();
        final Quaternionf foreRotation = new Quaternionf();
    }
}
