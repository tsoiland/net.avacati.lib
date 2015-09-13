package net.avacati.lib.mvc;

public class NoParamDefaultControllerFactory implements ControllerFactory {
    @Override
    public <C> C createController(Class<C> controllerClass) {
        try {
            return controllerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
