package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class PrintFullExceptionView implements ActionResult {
    private Throwable throwable;

    public PrintFullExceptionView(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public void createResult(Route route, HttpServletResponse response) throws IOException {
        String content = extractStackTrace(this.throwable);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, content);
    }

    private static String extractStackTrace(Throwable throwable1) {
        // Set up writer
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        // Print
        throwable1.printStackTrace(printWriter);
        printWriter.flush();

        // Return
        return writer.toString();
    }
}
