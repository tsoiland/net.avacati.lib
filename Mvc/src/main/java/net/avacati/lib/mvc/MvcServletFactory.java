package net.avacati.lib.mvc;

import javax.servlet.Servlet;
import java.util.ArrayList;

public class MvcServletFactory {
    public static Servlet createMvcServlet(
            ArrayList<AbstractAction> actions,
            ControllerFactory controllerFactory) {
        return new MvcServlet(new Route(actions, controllerFactory));
    }
}
