package com.skyplayer.tacztpanim.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.skyplayer.tacztpanim.network.AnimTriggerC2SPayload;
import com.skyplayer.tacztpanim.network.ModNetwork;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.GunDisplayInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public final class ThirdPersonAnimator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Integer, ThirdPersonAnimator.PlayerAnimation> ANIMATIONS = new HashMap<>();
    private static final Set<GunDisplayInstance> BROKEN_DISPLAYS = Collections.newSetFromMap(new WeakHashMap<>());
    private static LivingEntity renderingEntity;
    private static BedrockGunModel activeModel;
    private static GunDisplayInstance activeDisplay;
    private static final Quaternionf savedCameraRotation = new Quaternionf();
    private static final String LEFT_HAND_NODE = "lefthand_pos";
    private static final String RIGHT_HAND_NODE = "righthand_pos";
    private static final Vector3f LEFT_GRIP = new Vector3f(0.375F, 0.625F, 0.0F);
    private static final Vector3f RIGHT_GRIP = new Vector3f(-0.375F, 0.625F, 0.0F);

    private ThirdPersonAnimator() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ThirdPersonAnimator::onClientTick);
        NeoForge.EVENT_BUS.addListener(event -> ANIMATIONS.clear());
    }

    private static void onClientTick(Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            if (!ClientConfig.ANIMATE_OTHERS.get()) {
                ANIMATIONS.clear();
            } else {
                double maxDistanceSqr = Math.pow(ClientConfig.MAX_DISTANCE.get().intValue(), 2.0);
                Set<Integer> seen = new HashSet<>();

                for (Player player : minecraft.level.players()) {
                    if (player != minecraft.player && !(player.distanceToSqr(minecraft.player) > maxDistanceSqr)) {
                        ThirdPersonAnimator.PlayerAnimation animation = tickPlayer(player);
                        if (animation != null) {
                            seen.add(player.getId());
                        }
                    }
                }

                ANIMATIONS.keySet().retainAll(seen);
            }
        } else {
            ANIMATIONS.clear();
        }
    }

    private static ThirdPersonAnimator.PlayerAnimation tickPlayer(Player player) {
        ItemStack stack = player.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(stack);
        GunDisplayInstance display = iGun == null ? null : TimelessAPI.getGunDisplay(stack).orElse(null);
        if (display == null) {
            return null;
        } else {
            int slot = player.getInventory().selected;
            ResourceLocation gunId = iGun.getGunId(stack);
            ThirdPersonAnimator.PlayerAnimation animation = ANIMATIONS.get(player.getId());
            if (animation == null || animation.display != display || animation.slot != slot || !animation.gunId.equals(gunId)) {
                animation = createAnimation(player, stack, display, slot, gunId);
                if (animation == null) {
                    ANIMATIONS.remove(player.getId());
                    return null;
                }

                ANIMATIONS.put(player.getId(), animation);
            }

            animation.context.setCurrentGunItem(stack);
            LuaAnimationStateMachine<GunAnimationStateContext> machine = animation.machine;
            double dx = player.getX() - player.xo;
            double dz = player.getZ() - player.zo;
            boolean moving = dx * dx + dz * dz > 1.0E-4;
            boolean slow = player.isCrouching() || player.isVisuallyCrawling();
            if (!slow && player.isSprinting()) {
                machine.trigger("run");
            } else if (!slow && moving) {
                machine.trigger("walk");
            } else {
                machine.trigger("idle");
            }

            return animation;
        }
    }

    private static ThirdPersonAnimator.PlayerAnimation createAnimation(
        Player player, ItemStack stack, GunDisplayInstance display, int slot, ResourceLocation gunId
    ) {
        if (BROKEN_DISPLAYS.contains(display)) {
            return null;
        } else {
            ThirdPersonAnimator.PlayerAnimation old = ANIMATIONS.get(player.getId());
            if (old != null && old.machine.isInitialized()) {
                try {
                    old.machine.exit();
                } catch (RuntimeException var9) {
                }
            }

            try {
                LuaAnimationStateMachine<GunAnimationStateContext> machine = StateMachineCloner.clone(display);
                if (machine == null) {
                    return null;
                } else {
                    RemoteGunContext context = new RemoteGunContext(player);
                    context.setCurrentGunItem(stack);
                    context.setPartialTicks(0.0F);
                    machine.setContext(context);
                    machine.initialize();
                    machine.trigger("draw");
                    return new ThirdPersonAnimator.PlayerAnimation(display, slot, gunId, machine, context);
                }
            } catch (LinkageError | Exception var8) {
                BROKEN_DISPLAYS.add(display);
                LOGGER.warn("Could not set up third-person animations for gun {}", gunId, var8);
                return null;
            }
        }
    }

    public static void onRemoteTrigger(int entityId, String input) {
        if (ModNetwork.RELAYED_INPUTS.contains(input)) {
            ThirdPersonAnimator.PlayerAnimation animation = ANIMATIONS.get(entityId);
            if (animation != null) {
                if ("shoot".equals(input)) {
                    animation.context.lastShootTimestamp = System.currentTimeMillis();
                }

                try {
                    animation.machine.trigger(input);
                } catch (RuntimeException var4) {
                    LOGGER.debug("Animation trigger {} failed for entity {}", new Object[]{input, entityId, var4});
                }
            }
        }
    }

    public static void onStateMachineTrigger(AnimationStateMachine<?> machine, String input) {
        if (ModNetwork.RELAYED_INPUTS.contains(input) && ClientConfig.SEND_MY_ANIMATIONS.get()) {
            LocalPlayer player = Minecraft.getInstance().player;
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (player != null && connection != null && connection.hasChannel(AnimTriggerC2SPayload.TYPE)) {
                GunDisplayInstance display = TimelessAPI.getGunDisplay(player.getMainHandItem()).orElse(null);
                if (display != null && display.getAnimationStateMachine() == machine) {
                    ClientSafeSend.sendToServer(new AnimTriggerC2SPayload(input));
                }
            }
        }
    }

    public static void setRenderingEntity(LivingEntity entity) {
        renderingEntity = entity;
    }

    @Nullable
    public static LivingEntity getRenderingEntity() {
        return renderingEntity;
    }

    public static boolean isAnimatingRender() {
        return activeModel != null;
    }

    public static void beginRender(ItemStack stack, ItemDisplayContext displayContext) {
        if (activeModel == null && renderingEntity instanceof Player player) {
            if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                if (stack == player.getMainHandItem()) {
                    poseModel(player, stack);
                }
            }
        }
    }

    public static void onGunModelRender(BedrockGunModel model, PoseStack poseStack) {
        if (activeModel == model && activeDisplay != null) {
            ArmPoser.captureGunFrame(activeDisplay, poseStack.last().pose());
        }
    }

    public static void endRender() {
        if (activeModel != null) {
            activeModel.cleanAnimationTransform();
            activeModel.getCameraAnimationObject().rotationQuaternion = new Quaternionf(savedCameraRotation);
            activeModel = null;
            activeDisplay = null;
        }
    }

    @Nullable
    static ThirdPersonAnimator.HandTargets sampleHands(Player player, ItemStack stack) {
        if (activeModel != null) {
            return null;
        } else {
            GunDisplayInstance display = poseModel(player, stack);
            if (display == null) {
                return null;
            } else {
                ThirdPersonAnimator.HandTargets var4;
                try {
                    BedrockGunModel model = activeModel;
                    var4 = new ThirdPersonAnimator.HandTargets(
                        display, handPoint(model.getNode("lefthand_pos"), LEFT_GRIP), handPoint(model.getNode("righthand_pos"), RIGHT_GRIP)
                    );
                } finally {
                    endRender();
                }

                return var4;
            }
        }
    }

    @Nullable
    private static Vector3f handPoint(@Nullable BedrockPart node, Vector3f grip) {
        if (node == null) {
            return null;
        } else {
            List<BedrockPart> path = new ArrayList<>();

            for (BedrockPart part = node; part != null; part = part.getParent()) {
                path.add(part);
            }

            Collections.reverse(path);
            PoseStack poseStack = new PoseStack();

            for (BedrockPart part : path) {
                part.translateAndRotateAndScale(poseStack);
            }

            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            return poseStack.last().pose().transformPosition(grip, new Vector3f());
        }
    }

    @Nullable
    private static GunDisplayInstance poseModel(Player player, ItemStack stack) {
        GunDisplayInstance display = TimelessAPI.getGunDisplay(stack).orElse(null);
        if (display == null) {
            return null;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(false);
            LuaAnimationStateMachine<GunAnimationStateContext> machine;
            if (player == minecraft.player) {
                if (!ClientConfig.ANIMATE_SELF.get() || minecraft.options.getCameraType().isFirstPerson()) {
                    return null;
                }

                machine = display.getAnimationStateMachine();
                if (machine == null || !machine.isInitialized()) {
                    return null;
                }

                machine.processContextIfExist(context -> {
                    context.setPartialTicks(partialTick);
                    context.setCurrentGunItem(stack);
                });
            } else {
                ThirdPersonAnimator.PlayerAnimation animation = ANIMATIONS.get(player.getId());
                if (animation == null || animation.display != display) {
                    return null;
                }

                machine = animation.machine;
                animation.context.setPartialTicks(partialTick);
            }

            BedrockGunModel model = display.getGunModel();
            if (model == null) {
                return null;
            } else {
                savedCameraRotation.set(model.getCameraAnimationObject().rotationQuaternion);
                activeModel = model;
                activeDisplay = display;

                try {
                    machine.update();
                } catch (RuntimeException var8) {
                    LOGGER.debug("Third-person animation update failed", var8);
                }

                scaleRootMotion(model.getRootNode(), ClientConfig.ROOT_MOTION_SCALE.get().floatValue());
                return display;
            }
        }
    }

    private static void scaleRootMotion(BedrockPart root, float scale) {
        if (root != null && !(scale >= 1.0F)) {
            root.offsetX *= scale;
            root.offsetY *= scale;
            root.offsetZ *= scale;
            root.additionalQuaternion.set(new Quaternionf().slerp(root.additionalQuaternion, scale));
        }
    }

    record HandTargets(GunDisplayInstance display, @Nullable Vector3f left, @Nullable Vector3f right) {
    }

    private static final class PlayerAnimation {
        final GunDisplayInstance display;
        final int slot;
        final ResourceLocation gunId;
        final LuaAnimationStateMachine<GunAnimationStateContext> machine;
        final RemoteGunContext context;

        PlayerAnimation(
            GunDisplayInstance display, int slot, ResourceLocation gunId, LuaAnimationStateMachine<GunAnimationStateContext> machine, RemoteGunContext context
        ) {
            this.display = display;
            this.slot = slot;
            this.gunId = gunId;
            this.machine = machine;
            this.context = context;
        }
    }
}
