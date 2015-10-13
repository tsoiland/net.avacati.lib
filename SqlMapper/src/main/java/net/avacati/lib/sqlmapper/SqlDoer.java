package net.avacati.lib.sqlmapper;

import java.sql.ResultSet;

public interface SqlDoer {
    void doSql(String sql);
    ResultSet doSql2(String sql);
}
