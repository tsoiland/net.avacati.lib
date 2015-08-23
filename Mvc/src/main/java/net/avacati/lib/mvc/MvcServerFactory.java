package net.avacati.lib.mvc;

import javax.servlet.Servlet;
import java.util.ArrayList;
import net.avacati.lib.jetty.ServerAb;

public class MvcServerFactory {

    public ServerAb createMvcServer(ArrayList<AbstractAction> actions, ControllerFactory controllerFactory) {
        Servlet mvcServlet = new MvcServlet(new Route(actions, controllerFactory));
        return new ServerAb(mvcServlet, 8080);
    }
}
