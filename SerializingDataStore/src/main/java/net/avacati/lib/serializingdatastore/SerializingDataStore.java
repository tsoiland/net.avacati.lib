package net.avacati.lib.serializingdatastore;

import java.util.UUID;

public class SerializingDataStore<D> implements DataStore<D> {
    private DataStore<byte[]> byteDataStore;

    public SerializingDataStore(SqlByteDataStore sqlByteDataStore) {
        this.byteDataStore = sqlByteDataStore;
    }

    public void insert(UUID id, D dbo) {
        final byte[] bytes = SerializationHelpers.serializeObject(dbo);
        this.byteDataStore.insert(id, bytes);
    }

    public void update(UUID id, D newDbo, D oldDbo) {
        final byte[] newBytes = SerializationHelpers.serializeObject(newDbo);
        final byte[] oldBytes = SerializationHelpers.serializeObject(oldDbo);
        this.byteDataStore.update(id, newBytes, oldBytes);
    }

    public D get(UUID id) {
        final byte[] bytes = this.byteDataStore.get(id);
        return (D) SerializationHelpers.deserializeObject(bytes);
    }
}