package net.avacati.lib.mvc;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Route {
    private ArrayList<AbstractAction> actions;
    private ControllerFactory controllerFactory;

    public Route(ArrayList<AbstractAction> actions, ControllerFactory controllerFactory) {
        this.actions = actions;
        this.controllerFactory = controllerFactory;
    }

    public void route(String url, Map<String, String> postdata, HttpServletResponse response) throws IOException {
        try {
            this.actions
                .stream()
                .filter(action -> action.url.equals(url))
                .findAny()
                .get()
                .performAction(postdata, controllerFactory)
                .createResult(this, response);

        } catch (Throwable throwable) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, throwable.getMessage());
        }
    }

    public List<AbstractAction> getMenuActions() {
        return this.actions
                .stream()
                .filter(a -> a.isMenuItem())
                .collect(Collectors.toList());
    }
}
