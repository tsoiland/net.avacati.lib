package net.avacati.lib.async;

import java.lang.reflect.InvocationHandler;
import java.util.function.Consumer;

public class AsyncProxyFactory {
    public static <T> T wrapInAsync(T impl) {
        InvocationRepository invocationRepository = new InvocationRepositoryInmemImpl();
        Adapter adapter = new Adapter(impl);
        AsyncInvocationExecutor asyncInvocationExecutor = new AsyncInvocationExecutor(invocationRepository, adapter);
        new Thread(asyncInvocationExecutor::run).start();
        final Class<T> aClass = (Class<T>) impl.getClass();
        return AsyncProxyFactory.createProxy(aClass, invocationRepository::addSerializedMethodInvocations);
    }

    private static <T> T createProxy(Class<T> clazz, Consumer<byte[]> next) {
        InvocationHandler invocationHandler = new Proxy(next);

        //noinspection unchecked
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                invocationHandler);
    }
}
