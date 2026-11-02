package com.skyplayer.tacztpanim.client;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public final class ClientConfig {
    public static final ModConfigSpec SPEC;
    public static final BooleanValue ANIMATE_SELF;
    public static final BooleanValue ANIMATE_OTHERS;
    public static final BooleanValue SEND_MY_ANIMATIONS;
    public static final DoubleValue ROOT_MOTION_SCALE;
    public static final BooleanValue ANIMATE_ARMS;
    public static final DoubleValue ARM_FOLLOW_STRENGTH;
    public static final IntValue MAX_DISTANCE;

    private ClientConfig() {
    }

    static {
        Builder builder = new Builder();
        ANIMATE_SELF = builder.comment("Play your own gun animations on the third-person (F5) gun model.").define("animateSelf", true);
        ANIMATE_OTHERS = builder.comment("Play other players' gun animations on their gun model.").define("animateOthers", true);
        SEND_MY_ANIMATIONS = builder.comment("Tell the server when you reload / inspect / shoot... so other players can see the animation.")
            .define("sendMyAnimations", true);
        ROOT_MOTION_SCALE = builder.comment(
                "How much of the first-person whole-gun movement (sway, tilt during reload, draw...) is kept in third person.",
                "1.0 = exactly like first person, 0.0 = only moving parts (magazine, bolt...) animate."
            )
            .defineInRange("rootMotionScale", 0.5, 0.0, 1.0);
        ANIMATE_ARMS = builder.comment("Move the player's arms to where the first-person hands are (reloading, inspecting, working the bolt...).")
            .define("animateArms", true);
        ARM_FOLLOW_STRENGTH = builder.comment("How strongly the arms follow the first-person hands. 1.0 = fully, 0.0 = keep TACZ's normal third-person pose.")
            .defineInRange("armFollowStrength", 1.0, 0.0, 1.0);
        MAX_DISTANCE = builder.comment("Players further away than this (in blocks) are not animated.").defineInRange("maxDistance", 64, 4, 256);
        SPEC = builder.build();
    }
}
