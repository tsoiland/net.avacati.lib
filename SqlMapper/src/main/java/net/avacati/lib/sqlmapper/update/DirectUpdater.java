package net.avacati.lib.sqlmapper.update;

import net.avacati.lib.sqlmapper.util.DbField;
import net.avacati.lib.sqlmapper.util.TypeMapConfig;
import net.avacati.lib.sqlmapper.util.TypeNotSupportedException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class DirectUpdater {
    private Map<Class, TypeMapConfig> map;

    public DirectUpdater(Map<Class, TypeMapConfig> map) {
        this.map = map;
    }

    public Optional<String> createUpdateSqlForDirectlyMappableColumns(Object newDbo, Object oldDbo) {
        // New and old should be same class
        if (!newDbo.getClass().equals(oldDbo.getClass())) {
            throw new IllegalStateException();
        }

        // Find column names and values for all directly mappable fields.
        List<DbField> dbFields =
            Arrays
                .stream(newDbo.getClass().getFields())
                .map(field -> this.findColumnNameAndValueForFieldOnDbo(field, newDbo, oldDbo))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // Render sql
        String collect = dbFields.stream()
                .map(f -> f.columnName + "='" + f.value + "'")
                .collect(Collectors.joining(",\n\t"));

        // If there were no changed columns, don't try to render the statement, just return empty.
        if (collect.isEmpty()) {
            return Optional.empty();
        }

        // Get typemapconfig
        TypeMapConfig typeMapConfig = this.map.get(newDbo.getClass());

        // Find the primary key value for the WHERE clause in the update.
        String id = typeMapConfig.getPrimaryKey(newDbo);
        String id2 = typeMapConfig.getPrimaryKey(oldDbo);
        if (!id.equals(id2)) {
            throw new IllegalStateException();
        }

        // Render the sql
        String sql = String.format(
                "UPDATE %s \nSET \n\t%s \nWHERE %s = '%s'",
                typeMapConfig.getTableNameForDbo(),
                collect,
                typeMapConfig.getPrimaryKeyFieldName(),
                id);
        return Optional.of(sql);
    }

    private Optional<DbField> findColumnNameAndValueForFieldOnDbo(Field field, Object newDbo, Object oldDbo) {
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
            // Get the raw values from field.
            Object newFieldValue = this.getFieldValueFromDbo(field, newDbo);
            Object oldFieldValue = this.getFieldValueFromDbo(field, oldDbo);

            // Has the field changed?
            if (!this.specialEquals(newFieldValue, oldFieldValue, typeMapConfig)) {
                // Start a new field
                DbField dbField = new DbField();

                // Assign column name
                dbField.columnName = field.getName();

                // Assign column value based on mapping function.
                dbField.value = typeMapConfig.map(newFieldValue);

                return Optional.of(dbField);
            }
        }

        // If else
        return Optional.empty();
    }

    /**
     * An equals method that checks the typemapconfig to see if equality is decided by the dbos id'field and not regular equals method.
     */
    public boolean specialEquals(Object newFieldValue, Object oldFieldValue, TypeMapConfig typeMapConfig) {
        if (typeMapConfig.shouldEqualDirectly()) {
            // Do a regular equals
            return Objects.equals(newFieldValue, oldFieldValue);

        } else if (typeMapConfig.shouldEqualUsingPK()) {
            // Compare the primary key (identity) of the objects to determine equality (used for subdbos)
            try {
                // If they are both null, or both the same instance, they are definitely equal.
                if (newFieldValue == oldFieldValue) {
                    return true;
                }

                // If one is null, but not the other, there is no way they are equal.
                if (newFieldValue == null ^ oldFieldValue == null) {
                    return false;
                }

                // Now we know that they are both non-null

                // Get the field for Id/PK
                Class<?> dboClass = newFieldValue.getClass();
                Field pkfield = dboClass.getField(typeMapConfig.getPrimaryKeyFieldName());

                // Get Id from new and old
                Object newId = pkfield.get(newFieldValue);
                Object oldId = pkfield.get(oldFieldValue);

                // Simply compare ids.
                return Objects.equals(newId, oldId);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new AssertionError("No equals technique defined for " + newFieldValue.getClass().getName());
        }
    }

    private Object getFieldValueFromDbo(Field field, Object dbo) {
        try {
            return field.get(dbo);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
