package net.avacati.lib.mvc;

import net.avacati.lib.mvc.helpers.ServletTestHelper;
import net.avacati.lib.mvc.helpers.TestRequest;
import net.avacati.lib.mvc.helpers.TestResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class FileResultTest {
    @Test
    public void getStyleCss() {
        // Act
        TestResponse httpResponse = ServletTestHelper.requestUrlFromServletWithActions(
                Collections.singletonList(FileController.style_css), new TestRequest("style.css", "GET"));

        // Assert
        Assert.assertEquals("css content", new String(httpResponse.spyResponseStreamContent()));
    }

    public static class FileController {
        public static AbstractAction style_css = new FileAction("style.css", "content/style.css");
    }
}

