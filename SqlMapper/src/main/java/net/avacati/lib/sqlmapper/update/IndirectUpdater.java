package net.avacati.lib.sqlmapper.update;

import net.avacati.lib.sqlmapper.util.DbField;
import net.avacati.lib.sqlmapper.util.TypeMap;
import net.avacati.lib.sqlmapper.util.TypeMapConfig;
import net.avacati.lib.sqlmapper.util.TypeNotSupportedException;
import net.avacati.lib.sqlmapper.insert.IndirectInserter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IndirectUpdater {
    private TypeMap typeMap;
    private DirectUpdater directUpdater;
    private IndirectInserter indirectInserter;

    public IndirectUpdater(TypeMap typeMap, DirectUpdater directUpdater, IndirectInserter indirectInserter) {
        this.typeMap = typeMap;
        this.directUpdater = directUpdater;
        this.indirectInserter = indirectInserter;
    }

    /**
     * Go through an object tree of dbos and map it to sql tables.
     *
     * @return potentially multiple sql insert statements.
     */
    public List<String> createUpdateSqlsForObjectTree(Object newDbo, Object oldDbo) {
        // This method will return several sql insert statements.
        List<String> sql = new ArrayList<>();

        // Map the root directly to table
        this.directUpdater.createUpdateSqlForDirectlyMappableColumns(newDbo, oldDbo)
                          .ifPresent(sql::add);

        // Go through all of the fields and see if any of them should be indirectly mapped.
        sql.addAll(
                Arrays
                        .stream(newDbo.getClass().getFields())
                        .map(field -> this.createUpdateSqlForSubDbo(field, newDbo, oldDbo))
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
     * @param newParentDbo the parent dbo, one of which's fields contains the sub dbo to be mapped.
     * @param oldParentDbo
     * @return
     */
    private Optional<List<String>> createUpdateSqlForSubDbo(Field field, Object newParentDbo, Object oldParentDbo) {
        // What type are we trying to map?
        Class<?> type = field.getType();

        // Do we even support it?
        if (!this.typeMap.containsKey(type)) {
            throw new TypeNotSupportedException(type);
        }

        // Get the map config for this type.
        TypeMapConfig typeMapConfig = this.typeMap.get(type);

        // The value of our field can be processed in three ways:
        // - as a reference to a single object that should be processed as a dbo
        // - as a list of objects that should be processed as dbos.
        // - as neither (e.g. it is a String, int, Instant or UUID and has already been mapped by the
        //   direct mapper).
        if (typeMapConfig.isDboThatMapsToItsOwnTable()) {
            // Get the raw value from field.
            Object newSubDbo = this.getFieldValueFromDbo(field, newParentDbo);
            Object oldSubDbo = this.getFieldValueFromDbo(field, oldParentDbo);

            // Map it to it's own table.
            return this.createUpdateSqlForSubDbo(newSubDbo, oldSubDbo, typeMapConfig);

        } else if (typeMapConfig.shouldTreatAsList()) {
            // Get the raw value from field.
            Object newList = this.getFieldValueFromDbo(field, newParentDbo);
            Object oldList = this.getFieldValueFromDbo(field, oldParentDbo);

            if(newList == null) {
                throw new CannotSqlMapNullListException("Field: " + field.getName() + " is null on new dbo, and should be mapped as list. Unsupported.");
            }
            if(oldList == null) {
                throw new CannotSqlMapNullListException("Field: " + field.getName() + " is null on old dbo, and should be mapped as list. Unsupported.");
            }

            // Determine the type of objects the list contains
            TypeMapConfig typeMapConfigForParent = this.typeMap.get(newParentDbo.getClass());
            Class erasedTypeOfList = typeMapConfigForParent.getErasedType(field.getName());


            // Create extra field for foreign key. (Only needed when inserting into lists (not when updating items in list)
            DbField extraForeignKeyDbField = new DbField();
            extraForeignKeyDbField.columnName = newParentDbo.getClass().getSimpleName() + "_" + field.getName();
            extraForeignKeyDbField.value = typeMapConfigForParent.getPrimaryKey(newParentDbo);

            // Map the list to it's own table
            return this.createInsertUpdateOrDeleteSqlForEachObjectInList(newList, oldList, erasedTypeOfList, extraForeignKeyDbField);
        }

        return Optional.empty();
    }

    private Optional<List<String>> createUpdateSqlForSubDbo(Object newSubDbo, Object oldSubDbo, TypeMapConfig typeMapConfig) {
        if (oldSubDbo == null) {
            if (newSubDbo == null) {
                // If it was null before and is null now, no need to touch the subdbo table.
                return Optional.empty();

            } else {
                // If it was null, but has value now, we need to insert a row.
                // RECURSE to INSERT util.
                final List<String> insertSqlsForObjectTree = this.indirectInserter.createInsertSqlsForObjectTree(newSubDbo);
                return Optional.of(insertSqlsForObjectTree);
            }
        } else {
            if (newSubDbo == null) {
                // If it had a value before, but is null now, we need to recursively delete the sub-dbo-tree.
                // TODO
                // The direct mapper will set the foreign key to null, so the only consequence of not deleting is an orphan in the db. So we don't
                // _need_ to throw.
                //throw new RuntimeException("Delete not implemented");
                return Optional.empty();

            } else {
                // If it had a value before, and still has a value, either
                // - if it's the same entity, simply keep recursing for updates.
                // - otherwise, delete the old and insert the new.

                boolean isSameEntity = this.directUpdater.specialEquals(newSubDbo, oldSubDbo, typeMapConfig);
                if (isSameEntity) {
                    // Sub dbo is the same as before, but fields might have been changed. Check it.
                    // RECURSIVE call to process sub dbo as if it was a root. No need for foreign key here because the parent
                    // dbo will handle it as one of it's columns.
                    List<String> insertSqlForSubDbo = this.createUpdateSqlsForObjectTree(newSubDbo, oldSubDbo);
                    return Optional.of(insertSqlForSubDbo);

                } else {
                    // The current sub dbo is not the same as before. We prefer to delete the old and insert the new over updating the id.

                    // Insert the new
                    List<String> insertSqlsForObjectTree = this.indirectInserter.createInsertSqlsForObjectTree(newSubDbo);

                    // Delete the old
                    // TODO
                    // Lack of implementation only causes orphans, not strictly necessary to fail.
                    //throw new RuntimeException("Delete not implemented");

                    return Optional.of(insertSqlsForObjectTree);
                }
            }
        }
    }

    private Optional<List<String>> createInsertUpdateOrDeleteSqlForEachObjectInList(Object newList, Object oldList, Class erasedTypeOfList, DbField extraForeignKeyDbField) {
        // Crash early if the config told us to treat a non-collection class as list.
        if(!Collection.class.isAssignableFrom(newList.getClass())){
            throw new ClassCastException("TypeMapConfig said to treat as list, but new cannot be cast to collection.");
        }
        if(!Collection.class.isAssignableFrom(oldList.getClass())){
            throw new ClassCastException("TypeMapConfig said to treat as list, but old cannot be cast to collection.");
        }

        // Find out which items are new, which have been removed, and which need to be checked for updates.
        Collection<?> newCollection = (Collection) newList;
        Collection<?> oldCollection = (Collection) oldList;
        TypeMapConfig typeMapConfig = this.typeMap.get(erasedTypeOfList);
        ListModificationStatements listModificationStatements = this.fullOuterJoin(
                newCollection,
                oldCollection,
                (o, o2) -> this.directUpdater.specialEquals(o, o2, typeMapConfig));

        // Handle new items
        Stream<String> insertSqlsForAllItemsInList =
                listModificationStatements.inserts
                .stream()
                .map((dbo) -> this.indirectInserter.createInsertSqlsForObjectTree(dbo, extraForeignKeyDbField))
                .flatMap(Collection::stream);

        // Handle removed items
        if (!listModificationStatements.deletes.isEmpty()) {
            // TODO
            throw new RuntimeException("Not implemented");
        }

        // Handle existing items. These MIGHT have been updated.
        Stream<String> updateSqlsForAllItemsInList =
                listModificationStatements.needToBeCheckedForUpdates
                .stream()
                // For each item in the list, RECURSIVELY process each sub dbo as if it was a root.
                // Note that this doesn't need the foreign key db field.
                .map(subDbo -> this.createUpdateSqlsForObjectTree(subDbo.newDbo, subDbo.oldDbo))
                .flatMap(Collection::stream);

        // Collect sql from INSERT, DELETE and UPDATE.
        List<String> sql = Stream.concat(insertSqlsForAllItemsInList, updateSqlsForAllItemsInList).collect(Collectors.toList());
        return Optional.of(sql);
    }

    private ListModificationStatements fullOuterJoin(Collection<?> newList, Collection<?> oldList, BiFunction<Object, Object, Boolean> equalIdentityChecker) {
        // This will be returned
        ListModificationStatements result = new ListModificationStatements();

        // In 'the list we are NOT looping through', check off objects as they are matched with the first list.
        // This way we can know at the end which items are only in the old list.
        ArrayList<Object> yetUnmatchedItemsInOldList = new ArrayList<>(oldList);

        // Loop through one of the lists
        for (Object newObject : newList) {
            // Look up the item in the other list
            Optional<NewOld> inOld = this.find(newObject, oldList, equalIdentityChecker)
                                         .map(found -> new NewOld(newObject, found));

            if (inOld.isPresent()) {
                // If a row was present in both old and new we need to do an update.
                result.needToBeCheckedForUpdates.add(inOld.get());

                // ... and since it was present in the new list, we don't need to delete the row.
                yetUnmatchedItemsInOldList.remove(inOld.get().oldDbo);
            } else {
                // If it is not present in the old list, we should insert it.
                result.inserts.add(newObject);
            }
        }

        // The items left in this list are the ones which was only in the old list. And we should delete them.
        result.deletes = yetUnmatchedItemsInOldList;

        return result;
    }

    private Optional<Object> find(Object needle, Iterable hayStack, BiFunction<Object, Object, Boolean> equalIdentityChecker) {
        // Search for needle in haystack
        for (Object hay : hayStack) {
            if (equalIdentityChecker.apply(needle, hay)) {
                return Optional.of(hay);
            }
        }

        return Optional.empty();
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

    private static class ListModificationStatements {
        List<Object> inserts = new ArrayList<>();
        List<NewOld> needToBeCheckedForUpdates = new ArrayList<>();
        List<Object> deletes = new ArrayList<>();
    }

    private static class NewOld {
        final Object newDbo;
        final Object oldDbo;

        private NewOld(Object newDbo, Object oldDbo) {
            this.newDbo = newDbo;
            this.oldDbo = oldDbo;
        }
    }
}
