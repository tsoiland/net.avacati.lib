package net.avacati.lib.mvc;

public interface ControllerFactory {
    <C> C createController(Class<C> controllerClass);
}
