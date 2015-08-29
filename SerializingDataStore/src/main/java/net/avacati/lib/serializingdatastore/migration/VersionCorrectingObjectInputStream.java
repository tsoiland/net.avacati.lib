package net.avacati.lib.serializingdatastore.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;

class VersionCorrectingObjectInputStream extends ObjectInputStream {
    private final Class latestClass;

    VersionCorrectingObjectInputStream(InputStream in, Class latestClass) throws IOException {
        super(in);
        this.latestClass = latestClass;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        // If it's not our class, let super handle it. This will be all the types our class references, like String, UUID, etc.
        if(!desc.getName().equals(latestClass.getName())){
            return super.resolveClass(desc);
        }

        // If it is the latest version, no need to modify, just let super handle it.
        if(desc.getSerialVersionUID()  == ObjectStreamClass.lookup(this.latestClass).getSerialVersionUID()) {
            return super.resolveClass(desc);
        }

        // By convention the old version gets renamed to ClassnameV# where # is the serialVersionUID
        String name = desc.getName() + "V" + desc.getSerialVersionUID();

        // Load the old version of the class
        final Class<?> aClass = Class.forName(name, false, ClassLoader.getSystemClassLoader());

        // This is a hack that modifies the internals of the ObjectStreamClass parameter we were given. There is a check
        // in there that matches names, which trips up our whole thing with loading as different class names. So, we
        // reach in there and modify the _its_ record of the classname as well.
        //   This seems to work fine, but no extensive check for side effects have been done.
        try {
            final Field field = desc.getClass().getDeclaredField("name");
            field.setAccessible(true);
            field.set(desc, name);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        // Return the class we've loaded.
        return aClass;
    }
}
