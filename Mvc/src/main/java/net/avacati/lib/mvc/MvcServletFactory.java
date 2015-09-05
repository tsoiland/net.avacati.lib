package net.avacati.lib.mvc;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.Optional;

public class MvcServletFactory {
    public static Servlet createMvcServlet(
            ArrayList<AbstractAction> actions,
            ControllerFactory controllerFactory,
            Optional<ErrorAction>
            defaultErrorAction) {
        return new MvcServlet(new Route(actions, controllerFactory, defaultErrorAction));
    }

    public static Servlet createMvcServlet(ArrayList<AbstractAction> actions, ControllerFactory controllerFactory) {
        return createMvcServlet(actions, controllerFactory, Optional.empty());
    }
}
