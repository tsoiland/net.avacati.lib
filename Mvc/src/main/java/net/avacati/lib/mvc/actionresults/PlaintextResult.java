package net.avacati.lib.mvc.actionresults;

import net.avacati.lib.mvc.Route;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PlaintextResult implements ActionResult {
    private String plaintextOutput;

    public PlaintextResult(String plaintextOutput) {
        this.plaintextOutput = plaintextOutput;
    }

    @Override
    public void createResult(Route route, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain;charset=utf-8");
        response.getWriter().write(plaintextOutput);

    }
}
