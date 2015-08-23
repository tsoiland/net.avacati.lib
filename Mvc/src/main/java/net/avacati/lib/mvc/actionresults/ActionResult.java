package net.avacati.lib.mvc.actionresults;

import net.avacati.lib.mvc.Route;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ActionResult {
    void createResult(Route route, HttpServletResponse response) throws IOException;
}
