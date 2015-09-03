package net.avacati.lib.serializingdatastore.migration;

import net.avacati.lib.serializingdatastore.SerializationProvider;

/**
 * Pass the deserialized object through a {@link Migrator} before returning.
 */
class MigratingSerializationProvider implements SerializationProvider {
    private Migrator migrator;
    private VersionCorrectingSerializationProvider inner;

    MigratingSerializationProvider(Migrator migrator, VersionCorrectingSerializationProvider inner) {
        this.migrator = migrator;
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

        // If not migrate to latest version
        return this.migrator.migrate(object);
    }
}
