package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;
import net.avacati.lib.mvc.actionresults.ViewResult;
import net.avacati.lib.mvc.helpers.ServletTestHelper;
import net.avacati.lib.mvc.helpers.TestRequest;
import net.avacati.lib.mvc.helpers.TestResponse;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

public class ExceptionTest {
    @Test
    public void throwFromAction1() {
        runTest("Exception message from view", "/test/throwfromview");
    }

    @Test
    public void throwFromAction2() {
        runTest("Exception message from action", "/test/throwfromaction");
    }

    public void runTest(String expectedExceptionMessage, String requestUri) {
        // Act
        TestResponse httpResponse = ServletTestHelper.requestUrlFromServletWithActions(
                Arrays.asList(TestController.throwFromAction, TestController.throwFromView),
                new TestRequest(requestUri, "GET"));

        // Assert
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, httpResponse.spyServerCode());
        Assert.assertTrue("Not contained in: " + httpResponse.spyErrorMsg(), httpResponse.spyErrorMsg().contains(expectedExceptionMessage));
        Assert.assertTrue(httpResponse.spyErrorMsg().contains("TestException"));
        Assert.assertTrue(httpResponse.spyErrorMsg().contains("TestController"));
    }

    public static class TestController {
        public static AbstractAction throwFromView = new Action<>("/test/throwfromview", TestController::throwFromView, TestController.class);
        public ActionResult throwFromView() {
            return new ViewResult() {
                @Override
                public String render() {
                    throw new TestException("Exception message from view");
                }
            };
        }

        public static AbstractAction throwFromAction = new Action<>("/test/throwfromaction", TestController::throwFromAction, TestController.class);
        public ActionResult throwFromAction() {
            throw new TestException("Exception message from action");
        }
    }

    private static class TestException extends RuntimeException {
        public TestException(String s) {
            super(s);
        }
    }
}
