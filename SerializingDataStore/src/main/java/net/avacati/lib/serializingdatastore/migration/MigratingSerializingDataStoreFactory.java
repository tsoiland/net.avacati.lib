package net.avacati.lib.serializingdatastore.migration;

import net.avacati.lib.aggregaterepository.DataStore;
import net.avacati.lib.serializingdatastore.SerializingDataStoreFactory;

import java.sql.Connection;

public class MigratingSerializingDataStoreFactory {
    public static <DboType> DataStore<DboType> createDataStore(Connection connection, String tableName, Migrator migrator) {
        // Create migrating serialization provider
        final MigratingSerializationProvider serializationProvider = new MigratingSerializationProvider(
                migrator,
                new VersionCorrectingSerializationProvider(migrator.getLatestVersionOfAllClassesToMigrate()));

        // Create data store
        return SerializingDataStoreFactory.createDataStore(connection, tableName, serializationProvider);
    }
}
