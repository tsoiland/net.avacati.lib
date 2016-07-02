package net.avacati.lib.sqlmapper.schema;

import net.avacati.lib.sqlmapper.util.JdbcHelper;

public class TableCreator {
    private IndirectTableCreator indirectTableCreator;
    private JdbcHelper jdbcHelper;

    public TableCreator(IndirectTableCreator indirectTableCreator, JdbcHelper jdbcHelper) {
        this.indirectTableCreator = indirectTableCreator;
        this.jdbcHelper = jdbcHelper;
    }

    public void createTableFor(Class dboClass) {
        this.indirectTableCreator
                .createCreateTableSqlsForClass(dboClass)
                .stream()
//                .peek(System.out::println)
                .forEach(this.jdbcHelper::execute);
    }
}
