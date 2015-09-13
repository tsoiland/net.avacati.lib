package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;
import net.avacati.lib.mvc.actionresults.ViewResult;
import net.avacati.lib.mvc.helpers.ServletTestHelper;
import net.avacati.lib.mvc.helpers.TestRequest;
import net.avacati.lib.mvc.helpers.TestResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class ViewWithoutParameters {
    @Test
    public void get() {
        this.returnSimpleViewResult("GET");
    }

    @Test
    public void post() {
        this.returnSimpleViewResult("POST");
    }

    public void returnSimpleViewResult(String method) {
        // Act
        TestResponse httpResponse = ServletTestHelper.requestUrlFromServletWithActions(
                Collections.singletonList(TestController.simpleAction),
                new TestRequest("/test/simple", method));

        // Assert
        Assert.assertEquals("foobar", httpResponse.spyResponseWriterContent());
    }

    public static class TestController {
        public static AbstractAction simpleAction = new Action<>("/test/simple", TestController::simpleAction, TestController.class);
        public ActionResult simpleAction() {
            return new ViewResult() {
                @Override
                public String render() {
                    return "foobar";
                }
            };
        }
    }
}
