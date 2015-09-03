package net.avacati.lib.async;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.stream.Stream;

public class InvocationRepositoryInmemImpl implements InvocationRepository {
    private Queue<InvokeRow> inv = new ArrayDeque<>();

    @Override
    public synchronized void addSerializedMethodInvocations(byte[] serialization) {
        this.inv.add(new InvokeRow(serialization));
        this.notifyAll();
    }

    @Override
    public synchronized byte[] getNextInvocationBlocking() {
        if(this.inv.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return this.inv.poll().serialization;
    }

    @Override
    public void setDone(byte[] serialization) {
        getInvokeRowStream(serialization).forEach(InvokeRow::setDone);
    }

    @Override
    public void setDLQ(byte[] serialization, Throwable t) {
        getInvokeRowStream(serialization).forEach(invokeRow -> invokeRow.setError(t));
    }

    private Stream<InvokeRow> getInvokeRowStream(byte[] serialization) {
        return inv.stream().filter(invokeRow -> Arrays.equals(invokeRow.serialization, serialization));
    }

    private static class InvokeRow {
        private byte[] serialization;
        private RowStatus status;
        private Throwable t;

        public InvokeRow(byte[] serialization) {
            this.serialization = serialization;
            this.status = RowStatus.PENDING;
        }

        public void setDone() {
            this.status = RowStatus.DONE;
        }

        public void setError(Throwable t) {
            this.t = t;
            this.status = RowStatus.ERROR;
        }

        public RowStatus getStatus() {
            return status;
        }

        public Throwable getT() {
            return t;
        }

        private enum RowStatus {
            PENDING, DONE, ERROR
        }
    }
}
