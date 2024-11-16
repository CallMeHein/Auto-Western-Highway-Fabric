package hein.auto_western_highway.common;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Arrays;


public class InvokeVersionSpecific {
    public static <T> T invokeVersionSpecific(String className, String methodName, Object... args) {
        Class<?>[] argTypes = Arrays.stream(args).map(Object::getClass).toArray(Class<?>[]::new);


        String version = FabricLoader.getInstance().getModContainer("minecraft")
                .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
                .orElseThrow();

        String targetClassName = String.format("hein.auto_western_highway._%s.", version.replace(".","_"));
        try {
            targetClassName += className;
            return (T) Class.forName(targetClassName).getMethod(methodName, argTypes).invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeVersionSpecific(String className, Object... args){
        // if no methodName is given, assume it's just the className with a lowercase first letter
        return invokeVersionSpecific(className, className.substring(0, 1).toLowerCase() + className.substring(1),args);
    }
}
