package net.avacati.lib.sqlmapper;

import java.sql.ResultSet;

interface SqlDoer {
    void doSql(String sql);
    ResultSet doSql2(String sql);
}
