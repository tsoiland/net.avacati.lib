package net.avacati.lib.mvc.actionresults;

import net.avacati.lib.mvc.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Redirect implements ActionResult {
    private AbstractAction action;

    public Redirect(AbstractAction action) {
        this.action = action;
    }

    @Override
    public void createResult(Route route, HttpServletResponse response) throws IOException {
        response.sendRedirect(this.action.url);
    }
}
