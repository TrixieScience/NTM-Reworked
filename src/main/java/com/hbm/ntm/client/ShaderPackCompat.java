package com.hbm.ntm.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/** Optional Iris handshake. No Iris, no problem. */
public final class ShaderPackCompat {
    private static final Method IRIS_INSTANCE;
    private static final Method IRIS_SHADERS_ENABLED;

    static {
        Method instance = null;
        Method shadersEnabled = null;
        try {
            Class<?> irisApi = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            instance = irisApi.getMethod("getInstance");
            shadersEnabled = irisApi.getMethod("isShaderPackInUse");
        } catch (ClassNotFoundException | NoSuchMethodException | LinkageError ignored) {
            // Iris is optional. Steve's smoke machine still works without it.
        }
        IRIS_INSTANCE = instance;
        IRIS_SHADERS_ENABLED = shadersEnabled;
    }

    private ShaderPackCompat() { }

    public static boolean shadersEnabled() {
        if (IRIS_INSTANCE == null || IRIS_SHADERS_ENABLED == null) return false;
        try {
            Object iris = IRIS_INSTANCE.invoke(null);
            return Boolean.TRUE.equals(IRIS_SHADERS_ENABLED.invoke(iris));
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }
}
