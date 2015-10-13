package net.avacati.lib.sqlmapper;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

class DirectMapper {
    private Map<Class, TypeMapConfig> map;

    public DirectMapper(Map<Class, TypeMapConfig> map) {
        this.map = map;
    }

    public String createInsertSqlForDirectlyMappableColumns(Object dbo) {
        return createInsertSqlForDirectlyMappableColumns(dbo, new DbField[0]);
    }

    public String createInsertSqlForDirectlyMappableColumns(Object dbo, DbField... extraFields) {
        // Find column names and values for all directly mappable fields.
        List<DbField> dbFields =
            Arrays
                .stream(dbo.getClass().getFields())
                .map(field -> findColumnNameAndValueForFieldOnDbo(field, dbo))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Collections.addAll(dbFields, extraFields);

        // Render sql
        String valuesClause = dbFields
                .stream()
                .map(f -> "'" + f.value + "'")
                .collect(Collectors.joining(",\n\t"));

        String columnClause = dbFields
                .stream()
                .map(f -> f.columnName)
                .collect(Collectors.joining(",\n\t"));

        String tableName = this.map.get(dbo.getClass()).getTableNameForDbo();
        String sql =
                "INSERT INTO " + tableName +
                    "(\n\t" + columnClause + ")\n" +
                    "VALUES (\n\t" + valuesClause + ")\n";

        return sql;
    }

    private Optional<DbField> findColumnNameAndValueForFieldOnDbo(Field field, Object dbo) {
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
            DbField dbField = new DbField();

            // Assign column name
            dbField.columnName = field.getName();

            // Get the raw value from field.
            Object fieldValue = getFieldValueFromDbo(field, dbo);

            // Assign column value based on mapping function.
            dbField.value = typeMapConfig.map(fieldValue);

            return Optional.of(dbField);
        }

        // If else
        return Optional.empty();
    }

    private Object getFieldValueFromDbo(Field field, Object dbo) {
        try {
            return field.get(dbo);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
