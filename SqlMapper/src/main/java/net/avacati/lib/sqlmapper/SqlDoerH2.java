package net.avacati.lib.sqlmapper;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class SqlDoerH2 implements SqlDoer {
    private Connection connection;

    public SqlDoerH2(Connection connection) {
        this.connection = connection;
    }

    public static SqlDoerH2 create() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:");
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new SqlDoerH2(connection);
    }
    @Override
    public void doSql(String sql) {
        try {
            Statement statement = this.connection.createStatement();
                statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("sql exception", e);
        }
    }

    @Override
    public ResultSet doSql2(String sql) {
        try {
            Statement statement = this.connection.createStatement();
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException("sql exception", e);
        }
    }
}
