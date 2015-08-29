package net.avacati.lib.serializingdatastore;

import java.sql.Connection;

public class SerializingDataStoreFactory {
    public static <T> SerializingDataStore<T> createDataStore(Connection connection, String tableName, SerializationProvider serializationProvider) {
        return new SerializingDataStore<>(new SqlByteDataStore(tableName, connection), serializationProvider);
    }

    public static <T> SerializingDataStore<T> createDataStore(Connection connection, String tableName) {
        return createDataStore(connection, tableName, new PlainSerializationProvider());
    }
}
