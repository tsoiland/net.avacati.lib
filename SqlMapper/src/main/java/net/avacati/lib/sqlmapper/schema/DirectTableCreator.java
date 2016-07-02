package net.avacati.lib.sqlmapper.schema;

import net.avacati.lib.sqlmapper.util.TypeMapConfig;
import net.avacati.lib.sqlmapper.util.TypeNotSupportedException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class DirectTableCreator {
    private Map<Class, TypeMapConfig> map;

    public DirectTableCreator(Map<Class, TypeMapConfig> map) {
        this.map = map;
    }

    public Table createCreateTableScript(Class dboClass, DbField2... extraFields) {
        // Find column names and values for all directly mappable fields.
        List<DbField2> dbFields =
                Arrays
                        .stream(dboClass.getFields())
                        .map(field -> findColumnNameAndDataTypeForField(field))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        Collections.addAll(dbFields, extraFields);

        // Render sql
        String tableName = this.map.get(dboClass).getTableNameForDbo();

        return new Table(dboClass, tableName, dbFields);
    }

    private Optional<DbField2> findColumnNameAndDataTypeForField(Field field) {
        // What type are we trying to map?
        Class<?> type = field.getType();

        // Do we even support it?
        if (!this.map.containsKey(type)) {
            throw new TypeNotSupportedException(type);
        }

        // Get the map config for this type.
        TypeMapConfig typeMapConfig = this.map.get(type);

        // Should we map it directly?
        if (typeMapConfig.shouldMapDirectlyToColumn()) {
            // Start a new field
            DbField2 dbField = new DbField2();
            dbField.columnName = field.getName();
            dbField.type = "varchar(max)";

            return Optional.of(dbField);
        }

        // If else
        return Optional.empty();
    }

    public static class Table {
        public Class dboClass;
        public String tableName;
        public List<DbField2> columns;

        public Table(Class dboClass, String tableName, List<DbField2> columns) {
            this.dboClass = dboClass;
            this.tableName = tableName;
            this.columns = columns;
        }
    }
    public static class DbField2 {
        public String columnName;
        public String type;

        @Override
        public int hashCode() {
            return this.columnName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            final boolean b = obj instanceof DbField2 && ((DbField2) obj).columnName.equals(this.columnName);
            return b;
        }
    }
}
