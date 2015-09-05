package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;

import java.util.Map;

public class ParameterAction<C> extends AbstractAction {
    private PostActionReference<C> actionLambda;
    private Class<C> controllerClass;

    public ParameterAction(String url, PostActionReference<C> actionLambda, Class<C> controllerClass) {
        super(url);
        this.actionLambda = actionLambda;
        this.controllerClass = controllerClass;
    }

    public ParameterAction(String url, PostActionReference<C> actionLambda, String menu, Class<C> controllerClass) {
        super(url, menu);
        this.actionLambda = actionLambda;
        this.controllerClass = controllerClass;
    }

    public ActionResult performAction(Map<String, String> postData, ControllerFactory controllerFactory) throws Exception {
        C controller = controllerFactory.createController(this.controllerClass);
        return this.actionLambda.invokeOn(controller, postData);
    }


    @FunctionalInterface
    public interface PostActionReference<C> {
        ActionResult invokeOn(C controller, Map<String, String> postData) throws Exception;
    }
}
