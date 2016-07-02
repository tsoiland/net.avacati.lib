package net.avacati.lib.sqlmapper.schema;

import net.avacati.lib.sqlmapper.util.SqlDoer;

public class TableCreator {
    private IndirectTableCreator indirectTableCreator;
    private SqlDoer sqlDoer;

    public TableCreator(IndirectTableCreator indirectTableCreator, SqlDoer sqlDoer) {
        this.indirectTableCreator = indirectTableCreator;
        this.sqlDoer = sqlDoer;
    }

    public void createTableFor(Class dboClass) {
        this.indirectTableCreator
                .createCreateTableSqlsForClass(dboClass)
                .stream()
//                .peek(System.out::println)
                .forEach(this.sqlDoer::execute);
    }
}
