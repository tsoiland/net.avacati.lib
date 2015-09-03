package net.avacati.lib.async;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Proxy implements InvocationHandler {
    private Consumer<byte[]> newInvocationConsumer;

    Proxy(Consumer<byte[]> newInvocationConsumer) {
        this.newInvocationConsumer = newInvocationConsumer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // If there are no parameter in the method the args array will be null. Make it an empty array so it will become an empty string.
        if(args == null)
            args = new Object[0];

        // Serialize the arguments
        String serializedArguments = Arrays.stream(args)
                .map(SerializationHelpers::serializeToBase64String)
                .collect(Collectors.joining(":"));

        // Serialize parameter types
        final String serializedParameterTypes = SerializationHelpers.serializeToBase64String(method.getParameterTypes());

        // Assemble invocation and encode as UTF-8
        final String invocation = method.getName() + "#" + serializedParameterTypes + "#" + serializedArguments + "#";
        final byte[] serializedInvocation = invocation.getBytes(Charset.forName("UTF-8"));

        // Invoke newInvocationConsumer
        this.newInvocationConsumer.accept(serializedInvocation);

        // Return void
        return null;
    }
}
