package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;
import net.avacati.lib.mvc.actionresults.ViewResult;
import net.avacati.lib.mvc.helpers.ServletTestHelper;
import net.avacati.lib.mvc.helpers.TestRequest;
import net.avacati.lib.mvc.helpers.TestResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ErrorViewTest {

    @Test
    public void exceptionGetsHandledByGivenErrorAction() {
        // Arrange
        TestController.willThrowThis = new RuntimeException();

        // Act
        Map<String, String[]> parametermap = new HashMap<>();
        parametermap.put("fookey", new String[] {"foovalue"});
        TestResponse httpResponse = ServletTestHelper.requestUrlFromServletWithActions(
                Collections.singletonList(TestController.simpleAction),
                new TestRequest(parametermap, "/test/simple", "GET"),
                ErrorController.errorAction);

        // Assert
        Assert.assertEquals("Handling error foo", httpResponse.spyResponseWriterContent());
        Assert.assertEquals(TestController.willThrowThis, ErrorController.spyThrowable);
        Assert.assertEquals("/test/simple", ErrorController.spyUrl);
        Assert.assertEquals("foovalue", ErrorController.spyPostData.get("fookey"));
    }

    /**
     * This is the controller that HANDLES the throw.
     */
    public static class ErrorController {
        private static Throwable spyThrowable;
        private static String spyUrl;
        private static Map<String, String> spyPostData;

        public static ErrorAction errorAction = new ErrorAction<>(ErrorController::action, ErrorController.class);
        private ActionResult action(Throwable throwable, String url, Map<String, String> postData) {
            // Record spy stuff
            spyThrowable = throwable;
            spyUrl = url;
            spyPostData = postData;

            // Return a view with recognizable content
            return new ViewResult() {
                @Override
                public String render() {
                    return "Handling error foo";
                }
            };
        }
    }

    /**
     * This is the controller that THROWS
     */
    public static class TestController {
        private static RuntimeException willThrowThis;

        public static AbstractAction simpleAction = new Action<>("/test/simple", TestController::simpleAction, TestController.class);
        public ActionResult simpleAction() {
            throw willThrowThis;
        }
    }
}

