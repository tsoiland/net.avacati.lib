package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.util.TypeMapConfig;
import org.h2.jdbcx.JdbcDataSource;

import java.util.Map;

public class SqlDataStoreFactory {
    public static SqlDataStore<TestDbo> createSqlDataStore(Map<Class, TypeMapConfig> typemap) {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:");
        final SqlMapperFactory<TestDbo> sqlMapperFactory = new SqlMapperFactory<>(TestDbo.class, typemap, dataSource);
        sqlMapperFactory.createSchema();
        return sqlMapperFactory.getSqlDataStore();
    }
}
