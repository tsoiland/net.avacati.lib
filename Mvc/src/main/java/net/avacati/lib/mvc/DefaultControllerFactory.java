package net.avacati.lib.mvc;

import java.util.HashMap;
import java.util.Map;

public class DefaultControllerFactory implements ControllerFactory {
    private final Map<Class<?>,Object> map = new HashMap<>();

    public <T> void add(Class<T> clazz, T value) {
        this.map.put(clazz, value);
    }

    @Override
    public <C> C createController(Class<C> controllerClass) {
        if(this.map.containsKey(controllerClass))
            return (C) this.map.get(controllerClass);

        throw new RuntimeException("Controllerfactory cannot instantiate " + controllerClass);
    }
}

