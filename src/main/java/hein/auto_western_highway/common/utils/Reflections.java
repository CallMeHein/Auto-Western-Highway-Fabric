package hein.auto_western_highway.common.utils;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;
import java.util.Arrays;


public class Reflections {
    public static <T> T invokeVersionSpecific(String className, String methodName, Object... args) {
        Class<?>[] argTypes = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);


        String version = FabricLoader.getInstance().getModContainer("minecraft")
                .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
                .orElseThrow();

        String targetClassName = String.format("_%s.", version.replace(".", "_"));
        try {
            targetClassName += className;
            return (T) getMethod(targetClassName, methodName, argTypes).invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(String className, String methodName, Class<?>... parameterTypes) {
        try {
            return Class.forName("hein.auto_western_highway." + className).getMethod(methodName, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }
}
