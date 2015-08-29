package net.avacati.lib.serializingdatastore;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SerializingDataStore<D> implements DataStore<D> {
    private DataStore<byte[]> byteDataStore;
    private SerializationProvider serializationProvider;

    SerializingDataStore(SqlByteDataStore sqlByteDataStore, SerializationProvider serializationProvider) {
        this.byteDataStore = sqlByteDataStore;
        this.serializationProvider = serializationProvider;
    }

    public void insert(UUID id, D dbo) {
        final byte[] bytes = this.serializationProvider.serializeObject(dbo);
        this.byteDataStore.insert(id, bytes);
    }

    public void update(UUID id, D newDbo, D oldDbo) {
        final byte[] newBytes = this.serializationProvider.serializeObject(newDbo);
        final byte[] oldBytes = this.serializationProvider.serializeObject(oldDbo);
        this.byteDataStore.update(id, newBytes, oldBytes);
    }

    public D get(UUID id) {
        final byte[] bytes = this.byteDataStore.get(id);
        // 
        //noinspection unchecked 
        return (D) this.serializationProvider.deserializeObject(bytes);
    }

    public List<D> getAll() {
        final List<byte[]> bytes = this.byteDataStore.getAll();
        return bytes
                .stream()
                .map(this.serializationProvider::deserializeObject)
                .map(o -> (D) o)
                .collect(Collectors.toList());
    }
}