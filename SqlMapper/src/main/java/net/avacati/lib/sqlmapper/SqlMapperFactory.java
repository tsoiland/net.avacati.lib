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
        final IndirectMapper indirectMapper = new IndirectMapper(typemap, new DirectMapper(typemap));
        this.sqlDataStore = new SqlDataStore<>(
                indirectMapper,
                sqlDoerH2,
                new DirectSelecter(typemap, sqlDoerH2),
                dboType,
                new IndirectUpdater(typemap, new DirectUpdater(typemap), indirectMapper));
        this.dboType = dboType;
    }

    public void createSchema() {
        this.tableCreator.createTableFor(this.dboType);
    }

    public SqlDataStore<T> getSqlDataStore() {
        return this.sqlDataStore;
    }
}
