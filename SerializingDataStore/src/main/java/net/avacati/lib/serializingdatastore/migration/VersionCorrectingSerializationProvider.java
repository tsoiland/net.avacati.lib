package net.avacati.lib.serializingdatastore.migration;

import net.avacati.lib.serializingdatastore.SerializationProvider;

import java.io.*;
import java.util.Map;

class VersionCorrectingSerializationProvider implements SerializationProvider {
    private Map<String, Class> latestVersionOfAllClassesToMigrate;

    VersionCorrectingSerializationProvider(Map<String, Class> latestVersionOfAllClassesToMigrate) {

        this.latestVersionOfAllClassesToMigrate = latestVersionOfAllClassesToMigrate;
    }

    @Override
    public byte[] serializeObject(Object object) {
        try {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutput out = new ObjectOutputStream(bos)) {
                out.writeObject(object);
                return bos.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object deserializeObject(byte[] bytes) {
        try {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInput in = new VersionCorrectingObjectInputStream(bis, latestVersionOfAllClassesToMigrate)) {
                return in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
