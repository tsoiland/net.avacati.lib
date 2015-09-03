package net.avacati.lib.async;

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.util.UUID;

public class AsyncTest {
    @Test
    public void testAsync() throws InterruptedException {
        // Setup the actual class
        AsyncTestSpy asyncTestSpy = new AsyncTestSpy();

        // Setup framework
        final AsyncTestInterface proxy = AsyncProxyFactory.wrapInAsync(asyncTestSpy);

        // Act - Make the invocation
        TestParameters param = new TestParameters();
        param.uuid = UUID.randomUUID();
        param.string = "foobar";
        proxy.domainActivityAsync(param);

        // Wait for spy.
        synchronized (asyncTestSpy) {
            asyncTestSpy.wait(10000);
        }
        // If the spy notified us, we already know this has happened, but we assert anyway because it's sterile and I like the taste.
        Assert.assertTrue(asyncTestSpy.hasBeenCalled);
    }

    public interface AsyncTestInterface {
        void domainActivityAsync(TestParameters testParameters);
    }

    public static class TestParameters implements Serializable {
        public UUID uuid;
        public String string;
    }

    public class AsyncTestSpy implements AsyncTestInterface {
        private boolean hasBeenCalled;

        @Override
        public synchronized void domainActivityAsync(TestParameters testParameters) {
            this.hasBeenCalled = true;
            this.notify();
        }
    }
}
