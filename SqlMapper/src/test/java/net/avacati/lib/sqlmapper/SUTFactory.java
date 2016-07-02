package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.typeconfig.TypeMap;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class SUTFactory {
    public static SqlDataStore createSqlDataStore(TypeMap typemap) {
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
        sqlDataStoreFactory.createSchema(connection);
        return sqlDataStoreFactory.createSqlDataStore(connection);
    }
}
