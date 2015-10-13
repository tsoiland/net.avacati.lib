package net.avacati.lib.sqlmapper;

class TypeNotSupportedException extends RuntimeException{
    public TypeNotSupportedException(Class<?> type) {
        super("No type mapping for: " + type.toString());
    }
}
