package net.avacati.lib.mvc;

import net.avacati.lib.mvc.actionresults.ActionResult;
import net.avacati.lib.mvc.actionresults.ViewResult;
import net.avacati.lib.mvc.helpers.ServletTestHelper;
import net.avacati.lib.mvc.helpers.TestRequest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ControllerFactoryTest {
    private Class<?> controllerFactoryWasAskedForClass;

    @Test
    public void controllerFactoryShouldBeInvoked() {
        ServletTestHelper.requestUrlFromServletWithActions(Arrays.asList(TestController.action), new TestRequest("/test/test", "GET"),
            new ControllerFactory() {
                @Override
                public <C> C createController(Class<C> controllerClass) {
                    ControllerFactoryTest.this.controllerFactoryWasAskedForClass = controllerClass;
                    //noinspection unchecked
                    return (C) new TestController();
                }
            });

        Assert.assertEquals(TestController.class, this.controllerFactoryWasAskedForClass);
    }

    public static class TestController {
        public static AbstractAction action = new Action<>("/test/test", TestController::action, TestController.class);
        private ActionResult action() {
            return new ViewResult() {
                @Override
                public String render() {
                    return "";
                }
            };
        }
    }
}
