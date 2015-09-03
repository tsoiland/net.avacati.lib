package net.avacati.lib.serializingdatastore;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads classes like a normal classloader, except uses ASM to manipulate the byte code before the class is loaded.
 * The only thing that is manipulated is the class name, which is substituted according config given in {@link #mapClassNameFromTo(String, String)}.
 * <p>
 * The purpose of this is to enable tests of the migration-framework to take an old version of a class and serialize it in a way that makes it look
 * like it was done while the old class (e.g. TestDboV1) had the current name (e.g. TestDbo).
 */
class ClassNameSubstitutingClassLoader extends ClassLoader {
    private Map<String, String> mapFromClassNameToClassName = new HashMap<>();

    public ClassNameSubstitutingClassLoader() {
        super(null);
    }

    void mapClassNameFromTo(String from, String to) {
        this.mapFromClassNameToClassName.put(from, to);
    }

    @Override
    public Class<?> loadClass(String realName) throws ClassNotFoundException {
        // Only handle those classes that has been registered for modification.
        if(!this.mapFromClassNameToClassName.containsKey(realName)) {
            return super.loadClass(realName);
        }

        // Read class from resources and change name.
        byte[] remappedBytecode = readIntoClassWithFakeName(realName);

        // The class in the byte array has been modified, so the bytecode in there now refers to the new name.
        String newName = this.mapFromClassNameToClassName.get(realName);

        // Converts an array of bytes into an instance of class Class.
        final Class<?> aClass = defineClass(newName, remappedBytecode, 0, remappedBytecode.length);
        return aClass;
    }

    private byte[] readIntoClassWithFakeName(String realClassName) {
        try {
            // Reads the original class using getSystemResourceAsStream().
            ClassReader classReader = new ClassReader(realClassName);

            // Writes the modified class from the reader to a byte array (see return line).
            ClassWriter classWriter = new ClassWriter(classReader, 0);

            // Intercepts and modifies name of class as it passes from reader to writer.
            Remapper classNameRemapper = new ClassNameRemapper();
            classReader.accept(new RemappingClassAdapter(classWriter, classNameRemapper), ClassReader.EXPAND_FRAMES);

            // Write the modified class to a byte array.
            return classWriter.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Could not rewrite class " + realClassName, e);
        }
    }

    private class ClassNameRemapper extends Remapper {
        @Override
        public String map(String typeName) {
            final String replace = typeName.replace('/', '.');
            if(ClassNameSubstitutingClassLoader.this.mapFromClassNameToClassName.containsKey(replace)) {
                return ClassNameSubstitutingClassLoader.this.mapFromClassNameToClassName.get(replace).replace('.', '/');
            }
            return typeName;
        }
    }
}
