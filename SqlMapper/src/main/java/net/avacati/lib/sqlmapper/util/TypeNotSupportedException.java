package net.avacati.lib.sqlmapper.util;

public class TypeNotSupportedException extends RuntimeException{
    public TypeNotSupportedException(Class<?> type) {
        super("No type mapping for: " + type.toString());
    }
}
