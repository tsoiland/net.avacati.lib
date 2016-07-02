package net.avacati.lib.sqlmapper.schema;

import net.avacati.lib.sqlmapper.schema.DirectTableCreator.DbField2;
import net.avacati.lib.sqlmapper.schema.DirectTableCreator.Table;
import net.avacati.lib.sqlmapper.typeconfig.TypeConfig;
import net.avacati.lib.sqlmapper.typeconfig.TypeMap;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class IndirectTableCreator {
    private TypeMap typeMap;
    private DirectTableCreator directTableCreator;

    public IndirectTableCreator(TypeMap typeMap, DirectTableCreator directTableCreator) {
        this.typeMap = typeMap;
        this.directTableCreator = directTableCreator;
    }

    public List<String> createCreateTableSqlsForClass(Class dboClass) {
        final List<Table> tablesToCreate = this.createCreateTableSqlsForClass2(dboClass);

        return tablesToCreate
                .stream()
                .collect(Collectors.groupingBy(table -> table.dboClass))
                .values() // Grouping above returns map where value is list of Table.
                .stream()
                .map(this::unionTables)
                .sorted((o1, o2) -> o1.dboClass.getName().compareTo(o2.dboClass.getName()))
                .map(this::renderCreateTableStatement)
                .collect(Collectors.toList());
    }

    private String renderCreateTableStatement(Table table) {
        String columnClause = table.columns
                .stream()
                .map(f -> f.columnName + " " + f.type)
                .collect(Collectors.joining(",\n\t"));

        return "CREATE TABLE " + table.tableName +
                "(\n\t" + columnClause + ")\n";
    }

    private Table unionTables(List<Table> tablesForSameClass) {
        // Gather all fields from all table definitions and join the sets.
        Set<DbField2> unionOfAllFields =
                tablesForSameClass
                    .stream()
                    .flatMap(table -> table.columns.stream())
                    .sorted((o1, o2) -> o1.type.compareTo(o2.type))
                    .collect(Collectors.toSet());

        // Pick a table who's values will go on. (They should all be equal anyway)
        Table table = tablesForSameClass.get(0);

        // Construct the new Table with the union of the original tables fields.
        return new Table(table.dboClass, table.tableName, new ArrayList<>(unionOfAllFields));
    }

    public List<Table> createCreateTableSqlsForClass2(Class dboClass) {
        return this.createCreateTableSqlsForClass(dboClass, new DbField2[0]);
    }

    /**
     * Go through an object tree of dbos and map it to sql tables.
     *
     * @return potentially multiple sql insert statements.
     */
    private List<Table> createCreateTableSqlsForClass(Class dboClass, DbField2... extraFields) {
        // This method will return several sql insert statements.
        List<Table> sql = new ArrayList<>();

        // Map the root directly to table
        sql.add(this.directTableCreator.createCreateTableScript(dboClass, extraFields));

        // Go through all of the fields and see if any of them should be indirectly mapped.
        sql.addAll(
                Arrays
                        .stream(dboClass.getFields())
                        .map(field -> this.createCreateTableSqlForSubDbo(field, dboClass))
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
     * @param parentDboClass the parent dbo, one of which's fields contains the sub dbo to be mapped.
     * @return
     */
    private Optional<List<Table>> createCreateTableSqlForSubDbo(Field field, Class parentDboClass) {
        // What type are we trying to map?
        Class<?> type = field.getType();

        // Get the map config for this type.
        TypeConfig typeConfig = this.typeMap.get(type);

        // The value of our field can be processed in three ways:
        // - as a reference to a single object that should be processed as a dbo
        // - as a list of objects that should be processed as dbos.
        // - as neither (e.g. it is a String, int, Instant or UUID and has already been mapped by the
        //   direct mapper).
        if (typeConfig.isDboThatMapsToItsOwnTable()) {
            // RECURSIVE call to process sub dbo as if it was a root. No need for foreign key here because the parent
            // dbo will handle it as one of it's columns.
            List<Table> createTableSqlForSubDbo = this.createCreateTableSqlsForClass2(field.getType());

            return Optional.of(createTableSqlForSubDbo);

        } else if (typeConfig.shouldTreatAsList()) {
            // Create extra field for foreign key. (Only needed for lists)
            DbField2 extraForeignKeyDbField = new DbField2();
            extraForeignKeyDbField.columnName = parentDboClass.getSimpleName() + "_" + field.getName();
            extraForeignKeyDbField.type = "varchar(max)";

            // Map the list to it's own table
            Class erasedTypeOfList = this.typeMap.get(parentDboClass).getErasedType(field.getName());
            // Helps intelliJ not trip over itself in trying to analyze the generics of the below stream.
            List<Table> insertSqlsForAllItemsInList = this.createCreateTableSqlsForClass(erasedTypeOfList, extraForeignKeyDbField);

            return Optional.of(insertSqlsForAllItemsInList);
        }

        return Optional.empty();
    }
}
