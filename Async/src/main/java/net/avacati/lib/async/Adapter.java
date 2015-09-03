package net.avacati.lib.async;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Adapter {
    private Object realObject;

    public Adapter(Object realObject) {
        this.realObject = realObject;
    }

    public void invoke(byte[] serializedInvocation) {
        // Decode invocation as UTF-8
        final String invocation = new String(serializedInvocation, Charset.forName("UTF-8"));

        // Split into method name, parameter types and arguments
        final String[] split = invocation.split("#");
        final String methodName = split[0];
        final String serializedParameterTypes = split[1];

        // Deserialize parameter types
        Class[] parameterTypes = (Class[]) SerializationHelpers.deserializeFromBase64String(serializedParameterTypes);

        // Split arguments apart and deserialize individually
        Object[] arguments = new Object[0];
        if (split.length > 2) {
            final String serializedArguments = split[2];
            String[] base64EncodedSerializedArguments = serializedArguments.split(":");
            arguments = Arrays.stream(base64EncodedSerializedArguments)
                    .map(SerializationHelpers::deserializeFromBase64String)
                    .collect(Collectors.toList())
                    .toArray();
        }

        // Perform actual invocation.
        invoke(methodName, parameterTypes, arguments);
    }

    private Object invoke(String methodName, Class[] parameterTypes, Object[] arguments) {
        try {
            final Class<?> realClass = this.realObject.getClass();
            final Method method = realClass.getMethod(methodName, parameterTypes);
            return method.invoke(this.realObject, arguments);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
