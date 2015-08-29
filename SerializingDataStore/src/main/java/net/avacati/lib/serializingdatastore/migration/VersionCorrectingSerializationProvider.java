package net.avacati.lib.serializingdatastore.migration;

import net.avacati.lib.serializingdatastore.SerializationProvider;

import java.io.*;

class VersionCorrectingSerializationProvider implements SerializationProvider {
    private Class latestClass;

    VersionCorrectingSerializationProvider(Class latestClass) {

        this.latestClass = latestClass;
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
                 ObjectInput in = new VersionCorrectingObjectInputStream(bis, latestClass)) {
                return in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
