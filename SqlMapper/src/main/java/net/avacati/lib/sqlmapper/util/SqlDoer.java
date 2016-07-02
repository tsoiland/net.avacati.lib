package net.avacati.lib.sqlmapper.util;

import java.sql.ResultSet;

public interface SqlDoer {
    void doSql(String sql);
    ResultSet doSql2(String sql);
}
