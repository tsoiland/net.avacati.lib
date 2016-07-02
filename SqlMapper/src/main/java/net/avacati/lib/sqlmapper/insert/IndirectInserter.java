package net.avacati.lib.sqlmapper.insert;

import net.avacati.lib.sqlmapper.util.DbField;
import net.avacati.lib.sqlmapper.util.TypeMapConfig;
import net.avacati.lib.sqlmapper.util.TypeNotSupportedException;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class IndirectInserter {
    private Map<Class, TypeMapConfig> map;
    private DirectInserter directInserter;

    public IndirectInserter(Map<Class, TypeMapConfig> map, DirectInserter directInserter) {
        this.map = map;
        this.directInserter = directInserter;
    }

    public List<String> createInsertSqlsForObjectTree(Object dbo) {
        return this.createInsertSqlsForObjectTree(dbo, new DbField[0]);
    }

    /**
     * Go through an object tree of dbos and map it to sql tables.
     *
     * @return potentially multiple sql insert statements.
     */
    public List<String> createInsertSqlsForObjectTree(Object dbo, DbField... extraFields) {
        // This method will return several sql insert statements.
        List<String> sql = new ArrayList<>();

        // Map the root directly to table
        sql.add(this.directInserter.createInsertSqlForDirectlyMappableColumns(dbo, extraFields));

        // Go through all of the fields and see if any of them should be indirectly mapped.
        sql.addAll(
                Arrays
                        .stream(dbo.getClass().getFields())
                        .map(field -> this.createInsertSqlForSubDbo(field, dbo))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()));

        return sql;
    }

    /**
     * Map one of the fields of a parent dbo as a sub dbo.
     *
     * @param field specifies which field of the parentDbo should be mapped
     * @param parentDbo the parent dbo, one of which's fields contains the sub dbo to be mapped.
     * @return
     */
    private Optional<List<String>> createInsertSqlForSubDbo(Field field, Object parentDbo) {
        // What type are we trying to map?
        Class<?> type = field.getType();

        // Do we even support it?
        if (!this.map.containsKey(type)) {
            throw new TypeNotSupportedException(type);
        }

        // Get the map config for this type.
        TypeMapConfig typeMapConfig = this.map.get(type);

        // The value of our field can be processed in three ways:
        // - as a reference to a single object that should be processed as a dbo
        // - as a list of objects that should be processed as dbos.
        // - as neither (e.g. it is a String, int, Instant or UUID and has already been mapped by the
        //   direct mapper).
        if (typeMapConfig.isDboThatMapsToItsOwnTable()) {
            // Get the raw value from field.
            Object subDbo = this.getFieldValueFromDbo(field, parentDbo);

            // Map it to it's own table.
            return this.createInsertSqlForSubDbo(subDbo);

        } else if (typeMapConfig.shouldTreatAsList()) {
            // Get the raw value from field.
            Object list = this.getFieldValueFromDbo(field, parentDbo);

            if(list == null) {
                throw new CannotSqlMapNullListException("Field: " + field.getName() + " is null, and should be mapped as list. Unsupported.");
            }
            // Create extra field for foreign key. (Only needed for lists)
            DbField extraForeignKeyDbField = new DbField();
            extraForeignKeyDbField.columnName = parentDbo.getClass().getSimpleName() + "_" + field.getName();
            extraForeignKeyDbField.value = this.map.get(parentDbo.getClass()).getPrimaryKey(parentDbo);

            // Map the list to it's own table
            return this.createInsertSqlForEachObjectInList(list, extraForeignKeyDbField);
        }

        return Optional.empty();
    }

    private Optional<List<String>> createInsertSqlForSubDbo(Object subDbo) {
        // If the value of the subDbo is null we simply don't insert a row.
        if(subDbo == null){
            return Optional.empty();
        }

        // RECURSIVE call to process sub dbo as if it was a root. No need for foreign key here because the parent
        // dbo will handle it as one of it's columns.
        List<String> insertSqlForSubDbo = this.createInsertSqlsForObjectTree(subDbo);

        return Optional.of(insertSqlForSubDbo);
    }

    private Optional<List<String>> createInsertSqlForEachObjectInList(Object list, DbField extraForeignKeyDbField) {
        // Crash early if the config told us to treat a non-collection class as list.
        if(!Collection.class.isAssignableFrom(list.getClass())){
            throw new ClassCastException("TypeMapConfig said to treat as list, but cannot be cast to collection.");
        }

        // Helps intelliJ not trip over itself in trying to analyze the generics of the below stream.
        Collection<?> collection = (Collection) list;

        // For each item in the list, RECURSIVELY process each sub dbo as if it was a root. Except we add foreign key
        // as extra fields.
        List<String> insertSqlsForAllItemsInList = collection
                .stream()
                .map(subDbo -> this.createInsertSqlsForObjectTree(subDbo, extraForeignKeyDbField))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        return Optional.of(insertSqlsForAllItemsInList);
    }

    private Object getFieldValueFromDbo(Field field, Object dbo) {
        try {
            return field.get(dbo);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private class CannotSqlMapNullListException extends RuntimeException{
        public CannotSqlMapNullListException(String s) {
            super(s);
        }
    }
}
