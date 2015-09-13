package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Route {
    private List<AbstractAction> actions;
    private ControllerFactory controllerFactory;
    private Optional<ErrorAction> defaultErrorAction;

    public Route(List<AbstractAction> actions, ControllerFactory controllerFactory, Optional<ErrorAction> defaultErrorAction) {
        this.actions = actions;
        this.controllerFactory = controllerFactory;
        this.defaultErrorAction = defaultErrorAction;
    }

    public void route(String url, Map<String, String> postdata, HttpServletResponse response) throws IOException {
        try {
            this.actions
                .stream()
                .filter(action -> action.url.equals(url))
                .findAny()
                .orElseThrow(() -> new UrlNotMappedToActionException(url))
                .performAction(postdata, controllerFactory)
                .createResult(this, response);
        } catch (Exception e) {
            try {
                if(defaultErrorAction.isPresent()){
                    final ActionResult actionResult = defaultErrorAction.get().performAction(e, url, postdata, controllerFactory);
                    actionResult.createResult(this, response);
                } else {
                    // Default to printing the full exception
                    new PrintFullExceptionView(e).createResult(this, response);
                }
            } catch (Exception e2) {
                // If we can't print it to the response, then at least print it to std.err
                e2.printStackTrace();
                throw new RuntimeException(e2);
            }
        }
    }

    public List<AbstractAction> getMenuActions() {
        return this.actions
                .stream()
                .filter(AbstractAction::isMenuItem)
                .collect(Collectors.toList());
    }
}
