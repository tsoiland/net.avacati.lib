package net.avacati.lib.sqlmapper.typeconfig;

public class TypeNotSupportedException extends RuntimeException{
    TypeNotSupportedException(Class<?> type) {
        super("No type mapping for: " + type.toString());
    }
}
