package net.avacati.lib.mvc;

import javax.servlet.http.HttpServlet;
import java.util.List;
import java.util.Optional;

public class MvcServletFactory {
    public static HttpServlet createMvcServlet(
            List<AbstractAction> actions,
            ControllerFactory controllerFactory,
            Optional<ErrorAction> defaultErrorAction) {
        return new MvcServlet(new Route(actions, controllerFactory, defaultErrorAction));
    }

    public static HttpServlet createMvcServlet(List<AbstractAction> actions, ControllerFactory controllerFactory) {
        return createMvcServlet(actions, controllerFactory, Optional.empty());
    }
}
