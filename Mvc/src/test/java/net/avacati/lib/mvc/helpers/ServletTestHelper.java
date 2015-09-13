package net.avacati.lib.mvc.helpers;

import net.avacati.lib.mvc.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ServletTestHelper {
    public static TestResponse requestUrlFromServletWithActions(List<AbstractAction> actions, TestRequest request, ErrorAction errorAction) {
        final ControllerFactory controllerFactory = new NoParamDefaultControllerFactory();
        return requestUrlFromServletWithActions(actions, request, controllerFactory, Optional.of(errorAction));
    }

    public static TestResponse requestUrlFromServletWithActions(List<AbstractAction> actions, TestRequest request, ControllerFactory controllerFactory) {
        return requestUrlFromServletWithActions(actions, request, controllerFactory, Optional.empty());
    }

    public static TestResponse requestUrlFromServletWithActions(List<AbstractAction> actions, TestRequest request) {
        final ControllerFactory controllerFactory = new NoParamDefaultControllerFactory();
        return requestUrlFromServletWithActions(actions, request, controllerFactory, Optional.empty());
    }

    private static TestResponse requestUrlFromServletWithActions(
            List<AbstractAction> actions,
            TestRequest request,
            ControllerFactory controllerFactory,
            Optional<ErrorAction> errorAction) {
        // Arrange Servlet
        final HttpServlet mvcServlet = MvcServletFactory.createMvcServlet(actions, controllerFactory, errorAction);

        // Act on Servlet
        return new ServletInvokeHelper(mvcServlet).getTestResponse(request);
    }

    private static class ServletInvokeHelper {
        private HttpServlet mvcServlet;

        public ServletInvokeHelper(HttpServlet mvcServlet) {
            this.mvcServlet = mvcServlet;
        }

        public TestResponse getTestResponse(TestRequest request) {
            final TestResponse httpResponse = new TestResponse();
            try {
                this.mvcServlet.service(request, httpResponse);
            } catch (ServletException | IOException e) {
                throw new RuntimeException(e);
            }
            return httpResponse;
        }
    }
}
