package net.avacati.lib.serializingdatastore;

public interface SerializationProvider {
    byte[] serializeObject(Object object);
    Object deserializeObject(byte[] bytes);
}
