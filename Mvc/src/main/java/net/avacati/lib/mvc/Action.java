package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;

import java.util.Map;

public class Action<C> extends AbstractAction {
    private ActionReference<C> handle;
    private Class<C> controllerClass;

    public Action(String url, ActionReference<C> handle, Class<C> controllerClass) {
        super(url);
        this.handle = handle;
        this.controllerClass = controllerClass;
    }

    public Action(String url, ActionReference<C> handle, String menu, Class<C> controllerClass) {
        super(url, menu);
        this.handle = handle;
        this.controllerClass = controllerClass;
    }

    public ActionResult performAction(Map<String, String> postData, ControllerFactory controllerFactory) throws Throwable {
        C controller = controllerFactory.createController(this.controllerClass);
        return this.handle.some(controller);
    }

    @FunctionalInterface
    public interface ActionReference<C> {
        ActionResult some(C controller);
    }
}
