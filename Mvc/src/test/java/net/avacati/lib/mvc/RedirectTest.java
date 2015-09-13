package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;
import net.avacati.lib.mvc.actionresults.Redirect;
import net.avacati.lib.mvc.helpers.ServletTestHelper;
import net.avacati.lib.mvc.helpers.TestRequest;
import net.avacati.lib.mvc.helpers.TestResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class RedirectTest {
    @Test
    public void redirectToPath() {
        // Act
        TestResponse httpResponse = ServletTestHelper.requestUrlFromServletWithActions(
                Collections.singletonList(RedirectTestController.redirectFromTestAction), new TestRequest("redirect/from", "GET"));

        // Assert
        Assert.assertEquals("redirect/to", httpResponse.spyRedirectLocation());
    }

    public static class RedirectTestController {
        public static AbstractAction redirectFromTestAction = new Action<>("redirect/from", RedirectTestController::redirectFromTestAction, RedirectTestController.class);
        public ActionResult redirectFromTestAction() {
            return new Redirect(redirectToTestAction);
        }

        public static AbstractAction redirectToTestAction = new Action<>("redirect/to", RedirectTestController::redirectToTestAction, RedirectTestController.class);
        public ActionResult redirectToTestAction() {
            // No implementation because we're not really going to invoke it.
            return null;
        }
    }
}

