package net.avacati.lib.serializingdatastore;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        //noinspection unchecked
        return (D) SerializationHelpers.deserializeObject(bytes);
    }

    public List<D> getAll() {
        final List<byte[]> bytes = this.byteDataStore.getAll();
        return bytes
                .stream()
                .map(SerializationHelpers::deserializeObject)
                .map(o -> (D) o)
                .collect(Collectors.toList());
    }
}