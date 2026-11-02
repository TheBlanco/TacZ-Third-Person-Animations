package com.skyplayer.tacztpanim.client;

import com.tacz.guns.api.client.animation.AnimationController;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.animation.statemachine.LuaStateMachineFactory;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.pojo.display.gun.GunDisplay;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

final class StateMachineCloner {
    private static final ResourceLocation DEFAULT_STATE_MACHINE = ResourceLocation.fromNamespaceAndPath("tacz", "default_state_machine");
    private static final Field PROTOTYPES = field(AnimationController.class, "prototypes");
    private static final Field SOUND_CHANNEL = field(ObjectAnimation.class, "soundChannel");
    private static final Field DISPLAY = field(GunDisplayInstance.class, "display");

    private StateMachineCloner() {
    }

    @Nullable
    static LuaAnimationStateMachine<GunAnimationStateContext> clone(GunDisplayInstance display) throws ReflectiveOperationException {
        LuaAnimationStateMachine<GunAnimationStateContext> original = display.getAnimationStateMachine();
        BedrockGunModel model = display.getGunModel();
        if (original != null && model != null) {
            Map<String, ObjectAnimation> prototypes = (Map<String, ObjectAnimation>)PROTOTYPES.get(original.getAnimationController());
            List<ObjectAnimation> silent = new ArrayList<>(prototypes.size());

            for (ObjectAnimation prototype : prototypes.values()) {
                ObjectAnimation copy = new ObjectAnimation(prototype);
                SOUND_CHANNEL.set(copy, null);
                silent.add(copy);
            }

            AnimationController controller = new AnimationController(silent, model);
            GunDisplay pojo = (GunDisplay)DISPLAY.get(display);
            ResourceLocation scriptId = pojo.getStateMachineLocation() != null ? pojo.getStateMachineLocation() : DEFAULT_STATE_MACHINE;
            LuaTable script = ClientAssetsManager.INSTANCE.getScript(scriptId);
            if (script == null) {
                return null;
            } else {
                LuaTable ownScript = (LuaTable)deepCopy(script, new IdentityHashMap<>());
                return new LuaStateMachineFactory<GunAnimationStateContext>().setController(controller).setLuaScripts(ownScript).build();
            }
        } else {
            return null;
        }
    }

    private static LuaValue deepCopy(LuaValue value, Map<LuaValue, LuaValue> seen) {
        if (!value.istable()) {
            return value;
        } else {
            LuaValue existing = seen.get(value);
            if (existing != null) {
                return existing;
            } else {
                LuaTable source = (LuaTable)value;
                LuaTable copy = new LuaTable();
                seen.put(value, copy);
                LuaValue key = LuaValue.NIL;

                while (true) {
                    Varargs next = source.next(key);
                    key = next.arg1();
                    if (key.isnil()) {
                        LuaValue metatable = source.getmetatable();
                        if (metatable != null && !metatable.isnil()) {
                            copy.setmetatable(deepCopy(metatable, seen));
                        }

                        return copy;
                    }

                    copy.rawset(deepCopy(key, seen), deepCopy(next.arg(2), seen));
                }
            }
        }
    }

    private static Field field(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException var3) {
            throw new IllegalStateException("Unsupported TACZ version: missing " + owner.getSimpleName() + "." + name, var3);
        }
    }
}
