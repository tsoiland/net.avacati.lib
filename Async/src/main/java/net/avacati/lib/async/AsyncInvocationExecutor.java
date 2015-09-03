package net.avacati.lib.async;

public class AsyncInvocationExecutor {
    private InvocationRepository invocationRepository;
    private Adapter adapter;

    public AsyncInvocationExecutor(InvocationRepository invocationRepository, Adapter adapter) {
        this.invocationRepository = invocationRepository;
        this.adapter = adapter;
    }

    public void run() {
        while(true) {
            final byte[] nextInvocation = this.invocationRepository.getNextInvocationBlocking();
            if (nextInvocation == null) {
                continue;
            }
            try {
                this.adapter.invoke(nextInvocation);
                this.invocationRepository.setDone(nextInvocation);
            } catch (Throwable t) {
                t.printStackTrace();
                this.invocationRepository.setDLQ(nextInvocation, t);
            }
        }
    }
}
