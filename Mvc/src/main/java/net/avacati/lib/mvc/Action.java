package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;

import java.util.Map;

public class Action<C> extends AbstractAction {
    private ActionReference<C> actionLambda;
    private Class<C> controllerClass;

    public Action(String url, ActionReference<C> actionLambda, Class<C> controllerClass) {
        super(url);
        this.actionLambda = actionLambda;
        this.controllerClass = controllerClass;
    }

    public Action(String url, ActionReference<C> actionLambda, String menu, Class<C> controllerClass) {
        super(url, menu);
        this.actionLambda = actionLambda;
        this.controllerClass = controllerClass;
    }

    public ActionResult performAction(Map<String, String> postData, ControllerFactory controllerFactory) throws Throwable {
        C controller = controllerFactory.createController(this.controllerClass);
        return this.actionLambda.invokeOn(controller);
    }

    @FunctionalInterface
    public interface ActionReference<C> {
        ActionResult invokeOn(C controller);
    }
}
