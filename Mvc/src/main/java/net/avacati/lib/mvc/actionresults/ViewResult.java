package net.avacati.lib.mvc.actionresults;

import net.avacati.lib.mvc.Route;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class ViewResult implements ActionResult {
    @Override
    public void createResult(Route route, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write(this.wrapRender(route));
    }

    /**
     * Used to wrap layout around the render method.
     *
     * @return unless overridden, the same as render.
     */
    public String wrapRender(Route route) {
        return this.render();
    }

    public abstract String render();
}

