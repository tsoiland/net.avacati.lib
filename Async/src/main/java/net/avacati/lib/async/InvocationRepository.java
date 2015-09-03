package net.avacati.lib.async;

public interface InvocationRepository {
    void addSerializedMethodInvocations(byte[] serialization);
    byte[] getNextInvocationBlocking();
    void setDone(byte[] serialization);
    void setDLQ(byte[] serialization, Throwable t);
}

