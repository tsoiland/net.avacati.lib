package net.avacati.lib.serializingdatastore;

import java.io.*;

public class PlainSerializationProvider implements SerializationProvider {
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
                 ObjectInput in = new ObjectInputStream(bis)) {
                return in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}