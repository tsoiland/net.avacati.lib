package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;

import java.util.Map;

public class ErrorAction <C> {
    private ErrorActionReference<C> actionLambda;
    private Class<C> controllerClass;

    public ErrorAction(ErrorActionReference<C> actionLambda, Class<C> controllerClass) {
        this.actionLambda = actionLambda;
        this.controllerClass = controllerClass;
    }

    public ActionResult performAction(Throwable throwable, String url, Map<String, String> postData, ControllerFactory controllerFactory) throws Exception {
        C controller = controllerFactory.createController(this.controllerClass);
        return this.actionLambda.invokeOn(controller, throwable, url, postData);
    }

    @FunctionalInterface
    public interface ErrorActionReference<C> {
        ActionResult invokeOn(C controller, Throwable throwable, String url, Map<String, String> postData) throws Exception;
    }
}
