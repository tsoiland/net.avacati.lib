package net.avacati.lib.mvc;

import net.avacati.lib.mvc.helpers.ServletTestHelper;
import net.avacati.lib.mvc.helpers.TestRequest;
import net.avacati.lib.mvc.helpers.TestResponse;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

public class NotFoundTest {
    @Test
    public void notFoundTest() {
        // Act
        TestResponse httpResponse = ServletTestHelper.requestUrlFromServletWithActions(Collections.emptyList(), new TestRequest("index.html", "GET"));

        // Assert
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, httpResponse.spyServerCode());
        Assert.assertTrue(httpResponse.spyErrorMsg().contains("index.html"));
    }
}

