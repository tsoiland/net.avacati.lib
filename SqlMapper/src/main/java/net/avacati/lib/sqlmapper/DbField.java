package net.avacati.lib.sqlmapper;

class DbField {
    public String columnName;
    public String value;

    public DbField(String columnName, String value) {
        this.columnName = columnName;
        this.value = value;
    }

    public DbField() {
    }
}
