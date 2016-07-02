package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.util.TypeMap;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class SUTFactory {
    public static SqlDataStore createSqlDataStore(TypeMap typemap, Class<?> dboType) {
        // H2 datasource
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:");

        // Connection
        final Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // SqlDataStore
        SqlDataStoreFactory sqlDataStoreFactory = new SqlDataStoreFactory(typemap);
        sqlDataStoreFactory.createSchema(dboType, connection);
        return sqlDataStoreFactory.createSqlDataStore(connection);
    }
}
