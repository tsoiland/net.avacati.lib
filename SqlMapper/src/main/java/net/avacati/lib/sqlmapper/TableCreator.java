package net.avacati.lib.sqlmapper;

class TableCreator {
    private IndirectTableCreator indirectTableCreator;
    private SqlDoerH2 sqlDoer;

    public TableCreator(IndirectTableCreator indirectTableCreator, SqlDoerH2 sqlDoer) {
        this.indirectTableCreator = indirectTableCreator;
        this.sqlDoer = sqlDoer;
    }

    public void createTableFor(Class dboClass) {
        this.indirectTableCreator
                .createCreateTableSqlsForClass(dboClass)
                .stream()
                .forEach(this.sqlDoer::doSql);
    }
}
