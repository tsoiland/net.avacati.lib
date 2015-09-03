package net.avacati.lib.serializingdatastore.migration;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Deserializes objects, but potentially into a different class than was originally used for serialization.
 * <li>Use the same class if the serialVersionUID matches.
 * <li>Delegate to {@link ObjectInputStream} if it's not one of the configured (in constructor) classes.
 * <li>If it's one of the configured classes, but not the most recent serialVersionUID, use convention to find the snapshot version of that class,
 * and use that class when loading. The classes named as snapshots should be exactly like that class <i>actually</i> was at that serialVersionUID.
 */
class VersionCorrectingObjectInputStream extends ObjectInputStream {
    private Map<String, Class> classesToCheckForMigration;

    VersionCorrectingObjectInputStream(InputStream in, Map<String, Class> classesToCheckForMigration) throws IOException {
        super(in);
        this.classesToCheckForMigration = classesToCheckForMigration;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        // If it's not our class, let super handle it. This will be all the types our class references, like String, UUID, etc.
        if (!this.classesToCheckForMigration.containsKey(desc.getName())) {
            return super.resolveClass(desc);
        }

        // If it is the latest version, no need to modify, just let super handle it.
        if (desc.getSerialVersionUID() == ObjectStreamClass.lookup(this.classesToCheckForMigration.get(desc.getName())).getSerialVersionUID()) {
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
