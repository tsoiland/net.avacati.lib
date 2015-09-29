package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;
import net.avacati.lib.mvc.actionresults.PlaintextResult;
import net.avacati.lib.mvc.helpers.ServletTestHelper;
import net.avacati.lib.mvc.helpers.TestRequest;
import net.avacati.lib.mvc.helpers.TestResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class PlaintextResultTest {
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
                Collections.singletonList(TestController.plaintextAction),
                new TestRequest("/test/plaintext", method));

        // Assert
        Assert.assertEquals("foobar", httpResponse.spyResponseWriterContent());
    }

    public static class TestController {
        public static AbstractAction plaintextAction = new Action<>("/test/plaintext", TestController::plaintextAction, TestController.class);
        public ActionResult plaintextAction() {
            return new PlaintextResult("foobar");
        }
    }
}
