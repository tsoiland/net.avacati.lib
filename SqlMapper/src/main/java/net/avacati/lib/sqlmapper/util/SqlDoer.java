package net.avacati.lib.sqlmapper.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlDoer {
    private Connection connection;

    public SqlDoer(Connection connection) {
        this.connection = connection;
    }

    public void execute(String sql) {
        try {
            Statement statement = this.connection.createStatement();
                statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("sql exception", e);
        }
    }

    public ResultSet query(String sql) {
        try {
            Statement statement = this.connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException("sql exception", e);
        }
    }
}
