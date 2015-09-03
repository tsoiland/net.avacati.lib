package net.avacati.lib.async;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Base64;

class SerializationHelpers {
    static String serializeToBase64String(Object object) {
        final byte[] serializedParameterTypes = serializeObject(object);
        final byte[] b64ParameterTypes = Base64.getEncoder().encode(serializedParameterTypes);
        return new String(b64ParameterTypes, Charset.forName("UTF-8"));
    }

    static Object deserializeFromBase64String(String base64String) {
        final byte[] base64Bytes = base64String.getBytes(Charset.forName("UTF-8"));
        final byte[] plainBytes = Base64.getDecoder().decode(base64Bytes);
        return SerializationHelpers.deserializeObject(plainBytes);
    }

    static byte[] serializeObject(Object object) {
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

    static Object deserializeObject(byte[] bytes) {
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