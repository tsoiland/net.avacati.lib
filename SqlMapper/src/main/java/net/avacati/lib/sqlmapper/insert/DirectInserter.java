package net.avacati.lib.sqlmapper.insert;

import net.avacati.lib.sqlmapper.util.DbField;
import net.avacati.lib.sqlmapper.typeconfig.TypeConfig;
import net.avacati.lib.sqlmapper.typeconfig.TypeMap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DirectInserter {
    private TypeMap typeMap;

    public DirectInserter(TypeMap typeMap) {
        this.typeMap = typeMap;
    }

    public String createInsertSqlForDirectlyMappableColumns(Object dbo) {
        return this.createInsertSqlForDirectlyMappableColumns(dbo, new DbField[0]);
    }

    public String createInsertSqlForDirectlyMappableColumns(Object dbo, DbField... extraFields) {
        // Find column names and values for all directly mappable fields.
        List<DbField> dbFields =
            Arrays
                .stream(dbo.getClass().getFields())
                .map(field -> this.findColumnNameAndValueForFieldOnDbo(field, dbo))
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

        String tableName = this.typeMap.get(dbo.getClass()).getTableNameForDbo();
        String sql =
                "INSERT INTO " + tableName +
                    "(\n\t" + columnClause + ")\n" +
                    "VALUES (\n\t" + valuesClause + ")\n";

        return sql;
    }

    private Optional<DbField> findColumnNameAndValueForFieldOnDbo(Field field, Object dbo) {
        // What type are we trying to map?
        Class<?> type = field.getType();

        // Get the map config for this type.
        TypeConfig typeConfig = this.typeMap.get(type);

        // Should we map it directly?
        if (typeConfig.shouldMapDirectlyToColumn()) {
            // Start a new field
            DbField dbField = new DbField();

            // Assign column name
            dbField.columnName = field.getName();

            // Get the raw value from field.
            Object fieldValue = this.getFieldValueFromDbo(field, dbo);

            // Assign column value based on mapping function.
            dbField.value = typeConfig.map(fieldValue);

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
