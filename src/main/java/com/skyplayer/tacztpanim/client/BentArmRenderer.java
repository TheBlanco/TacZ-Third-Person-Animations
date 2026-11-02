package com.skyplayer.tacztpanim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.core.Direction;

final class BentArmRenderer {
    private static final Set<Direction> SIDES = EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    private static final Set<Direction> UPPER_FACES = EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN);
    private static final Set<Direction> HAND_FACE = EnumSet.of(Direction.UP);
    private static final float SLEEVE_GROW = 0.25F;
    private static final Cube[][][] CUBES = new Cube[2][2][];

    private BentArmRenderer() {
    }

    private static Cube[] build(boolean right, boolean slim) {
        int width = slim ? 3 : 4;
        float minX = right ? (slim ? -2.0F : -3.0F) : -1.0F;
        int u = right ? 40 : 32;
        int v = right ? 16 : 48;
        int sleeveU = right ? 40 : 48;
        int sleeveV = right ? 32 : 48;
        return new Cube[]{
            cube(u, v, minX, -2.0F, width, 6.0F, 0.0F, UPPER_FACES),
            cube(u, v + 5, minX, -1.0F, width, 7.0F, 0.0F, SIDES),
            cube(u, v, minX, -1.0F, width, 7.0F, 0.0F, HAND_FACE),
            cube(sleeveU, sleeveV, minX, -2.0F, width, 6.0F, 0.25F, UPPER_FACES),
            cube(sleeveU, sleeveV + 5, minX, -1.0F, width, 7.0F, 0.25F, SIDES),
            cube(sleeveU, sleeveV, minX, -1.0F, width, 7.0F, 0.25F, HAND_FACE)
        };
    }

    private static Cube cube(int u, int v, float minX, float minY, int width, float height, float grow, Set<Direction> faces) {
        return new Cube(u, v, minX, minY, -2.0F, width, height, 4.0F, grow, grow, grow, false, 64.0F, 64.0F, faces);
    }

    static void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color, ArmPoser.BentArm arm, boolean right, boolean slim) {
        Cube[] cubes = CUBES[right ? 1 : 0][slim ? 1 : 0];
        poseStack.pushPose();
        poseStack.translate(arm.shoulder.x, arm.shoulder.y, arm.shoulder.z);
        poseStack.mulPose(arm.upperRotation);
        cubes[0].compile(poseStack.last(), consumer, light, overlay, color);
        if (arm.sleeveVisible) {
            cubes[3].compile(poseStack.last(), consumer, light, overlay, color);
        }

        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(arm.elbow.x, arm.elbow.y, arm.elbow.z);
        poseStack.mulPose(arm.foreRotation);
        cubes[1].compile(poseStack.last(), consumer, light, overlay, color);
        cubes[2].compile(poseStack.last(), consumer, light, overlay, color);
        if (arm.sleeveVisible) {
            cubes[4].compile(poseStack.last(), consumer, light, overlay, color);
            cubes[5].compile(poseStack.last(), consumer, light, overlay, color);
        }

        poseStack.popPose();
    }

    static {
        for (int right = 0; right < 2; right++) {
            for (int slim = 0; slim < 2; slim++) {
                CUBES[right][slim] = build(right == 1, slim == 1);
            }
        }
    }
}
