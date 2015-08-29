package net.avacati.lib.serializingdatastore.migration;

import net.avacati.lib.serializingdatastore.SerializationProvider;

/**
 * Pass the deserialized object through a {@link Migrator} before returning.
 */
class MigratingSerializationProvider implements SerializationProvider {
    private Migrator migrator;
    private Class latestClass;
    private VersionCorrectingSerializationProvider inner;

    MigratingSerializationProvider(Migrator migrator, Class latestClass, VersionCorrectingSerializationProvider inner) {
        this.migrator = migrator;
        this.latestClass = latestClass;
        this.inner = inner;
    }

    @Override
    public byte[] serializeObject(Object object) {
        return this.inner.serializeObject(object);
    }

    @Override
    public Object deserializeObject(byte[] bytes) {
        // Deserialize to the accurate version (the old version)
        Object object = this.inner.deserializeObject(bytes);

        // Check if it is the latest version
        if (object.getClass().isAssignableFrom(this.latestClass)) {
            return object;
        }

        // If not migrate to latest version
        return this.migrator.migrate(object, latestClass);
    }
}
