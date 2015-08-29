package net.avacati.lib.mvc.actionresults;

import net.avacati.lib.mvc.AbstractAction;
import net.avacati.lib.mvc.Route;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public abstract class ViewResult implements ActionResult {
    @Override
    public void createResult(Route route, HttpServletResponse response) throws IOException {
        final String renderedOutput = this.wrapRender(route.getMenuActions());
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(renderedOutput);
    }

    /**
     * Used to wrap layout around the render method.
     *
     * @return unless overridden, the same as render.
     */
    protected String wrapRender(List<AbstractAction> menuActions) {
        return this.render();
    }

    public abstract String render();
}

