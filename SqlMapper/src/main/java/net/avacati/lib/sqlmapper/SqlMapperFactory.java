package net.avacati.lib.sqlmapper;

import org.h2.jdbcx.JdbcDataSource;

import java.util.Map;

public class SqlMapperFactory<T> {
    private final TableCreator tableCreator;
    private final SqlDataStore<T> sqlDataStore;
    private Class<T> dboType;

    public SqlMapperFactory(Class<T> dboType, Map<Class, TypeMapConfig> typemap, JdbcDataSource dataSource) {
        SqlDoerH2 sqlDoerH2 = SqlDoerH2.create(dataSource);
        this.tableCreator = new TableCreator(new IndirectTableCreator(typemap, new DirectTableCreator(typemap)), sqlDoerH2);
        this.sqlDataStore = new SqlDataStore<>(
                new IndirectMapper(typemap, new DirectMapper(typemap)),
                sqlDoerH2,
                new DirectSelecter(typemap, sqlDoerH2),
                dboType);
        this.dboType = dboType;
    }

    public void createSchema() {
        tableCreator.createTableFor(dboType);
    }

    public SqlDataStore<T> getSqlDataStore() {
        return sqlDataStore;
    }
}
