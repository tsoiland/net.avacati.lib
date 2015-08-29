package net.avacati.lib.serializingdatastore;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqlByteDataStore implements DataStore<byte[]> {
    private String tableName = "test_table";
    private java.sql.Connection connection;

    public SqlByteDataStore(String tableName, Connection connection) {
        this.tableName = tableName;
        this.connection = connection;
        this.doSql("CREATE TABLE IF NOT EXISTS " + tableName + " (id varchar(50), bytes BINARY)");
    }

    @Override
    public void insert(UUID id, byte[] bytes) {
        String sql = "INSERT INTO " + tableName + " (id, bytes) VALUES (?, ?)";
        try {
            final PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, id.toString());
            preparedStatement.setBytes(2, bytes);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(UUID id, byte[] newBytes, byte[] oldBytes) {
        String sql = "UPDATE " + tableName + " SET bytes = ? WHERE id = ?";
        try {
            final PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setBytes(1, newBytes);
            preparedStatement.setString(2, id.toString());
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] get(UUID id) {
        String sql = "SELECT bytes FROM " + tableName + " WHERE id = ?";
        try {
            final PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setString(1, id.toString());
            final ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getBytes("bytes");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<byte[]> getAll() {
        String sql = "SELECT bytes FROM " + tableName;
        try {
            final PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            final ResultSet resultSet = preparedStatement.executeQuery();

            List<byte[]> bytes = new ArrayList<>();
            while(resultSet.next()) {
                bytes.add(resultSet.getBytes("bytes"));
            }

            return bytes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void doSql(String sql) {
        try {
            Statement statement = this.connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("sql exception", e);
        }
    }
}