package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;

import java.util.Map;

public class ParameterAction<C> extends AbstractAction {
    private PostActionReference<C> handle;
    private Class<C> controllerClass;

    public ParameterAction(String url, PostActionReference<C> handle, Class<C> controllerClass) {
        super(url);
        this.handle = handle;
        this.controllerClass = controllerClass;
    }

    public ParameterAction(String url, PostActionReference<C> handle, String menu, Class<C> controllerClass) {
        super(url, menu);
        this.handle = handle;
        this.controllerClass = controllerClass;
    }

    public ActionResult performAction(Map<String, String> postData, ControllerFactory controllerFactory) throws Throwable {
        C controller = controllerFactory.createController(this.controllerClass);
        return this.handle.some(controller, postData);
    }


    @FunctionalInterface
    public interface PostActionReference<C> {
        ActionResult some(C controller, Map<String, String> postData) throws Throwable;
    }
}
