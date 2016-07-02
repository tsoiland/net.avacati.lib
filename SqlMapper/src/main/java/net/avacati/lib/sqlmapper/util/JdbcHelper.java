package net.avacati.lib.sqlmapper.util;

import java.sql.*;
import java.util.List;

public class JdbcHelper {
    private Connection connection;

    public JdbcHelper(Connection connection) {
        this.connection = connection;
    }

    public void execute(String sql) {
        try (Statement statement = this.connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("sql exception", e);
        }
    }

    public <T> List<T> retrieveList(String sql, ResultSetMapper<T> resultSetMapper) {
        return this.retrieveList(sql, resultSetMapper, new Object[0]);
    }

    private <T> List<T> retrieveList(String sql, ResultSetMapper<T> resultSetMapper, Object... values) {
        try(PreparedStatement statement = this.connection.prepareStatement(sql)) {
            this.setParameters(statement, values);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSetMapper.map(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setParameters(PreparedStatement preparedStatement, Object[] values) throws SQLException {
        int columnIndex = 1;
        for (Object value : values) {
            if (value.getClass().isAssignableFrom(String.class)) {
                String typedValue = (String) value;
                preparedStatement.setString(columnIndex++, typedValue);
            }
        }
    }

    @SuppressWarnings("WeakerAccess") // actually used, intellij just doesn't realize it.
    @FunctionalInterface
    public interface ResultSetMapper<T> {
        List<T> map(ResultSet resultSet) throws SQLException;
    }
}
