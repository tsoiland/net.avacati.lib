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

public class ViewWithParameters {
    @Test
    public void getWithParameters() {
        this.runTest("GET");
    }

    @Test
    public void postWithParameters() {
        this.runTest("POST");
    }

    private void runTest(String method) {
        // Arrange get parameters
        Map<String, String[]> params = new HashMap<>();
        params.put("foo", new String[] {"foovalue"});
        params.put("bar", new String[] {"barvalue"});

        // Act
        TestResponse httpResponse = ServletTestHelper.requestUrlFromServletWithActions(
                Collections.singletonList(TestController.simpleAction),
                new TestRequest(params, "/test/simple", method));

        // Assert
        Assert.assertEquals("foovalue barvalue", httpResponse.spyResponseWriterContent());
    }

    public static class TestController {
        public static AbstractAction simpleAction = new ParameterAction<>("/test/simple", TestController::simpleAction, TestController.class);
        public ActionResult simpleAction(Map<String,String> params) {
            return new ViewResult() {
                @Override
                public String render() {
                    return params.get("foo") + " " +  params.get("bar");
                }
            };
        }
    }
}
