package me.eldodebug.soar.injection.mixin.mixins.entity;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.world.World;

@Mixin(EntityList.class)
public class MixinEntityList {

    @Unique
    private static final Map<Class<?>, Constructor<?>> CLASS_TO_CONSTRUCTOR = new HashMap<Class<?>, Constructor<?>>();

    @Redirect(method={"createEntityByName"}, at=@At(value="INVOKE", target="Ljava/lang/Class;getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", remap=false))
    private static Constructor<?> createEntityByName$cacheConstructor(Class<? extends Entity> instance, Class<?>[] parameterTypes) {
        return MixinEntityList.getConstructor(instance);
    }

    @Redirect(method={"createEntityFromNBT"}, at=@At(value="INVOKE", target="Ljava/lang/Class;getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", remap=false))
    private static Constructor<?> createEntityFromNBT$cacheConstructor(Class<? extends Entity> instance, Class<?>[] parameterTypes) {
        return MixinEntityList.getConstructor(instance);
    }

    @Redirect(method={"createEntityByID"}, at=@At(value="INVOKE", target="Ljava/lang/Class;getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;", remap=false))
    private static Constructor<?> createEntityByID$cacheConstructor(Class<? extends Entity> instance, Class<?>[] parameterTypes) {
        return MixinEntityList.getConstructor(instance);
    }

    @SuppressWarnings("unchecked")
	private static Constructor<?> getConstructor(Class<? extends Entity> entityClass) {
        Constructor<? extends Entity> constructor = (Constructor<? extends Entity>) CLASS_TO_CONSTRUCTOR.get(entityClass);
        if (constructor == null) {
            try {
                constructor = entityClass.getConstructor(World.class);
                CLASS_TO_CONSTRUCTOR.put(entityClass, constructor);
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return constructor;
    }
}
