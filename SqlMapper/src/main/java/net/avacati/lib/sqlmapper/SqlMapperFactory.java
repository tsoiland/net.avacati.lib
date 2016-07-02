package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.insert.DirectInserter;
import net.avacati.lib.sqlmapper.insert.IndirectInserter;
import net.avacati.lib.sqlmapper.schema.DirectTableCreator;
import net.avacati.lib.sqlmapper.schema.IndirectTableCreator;
import net.avacati.lib.sqlmapper.schema.TableCreator;
import net.avacati.lib.sqlmapper.select.DirectSelecter;
import net.avacati.lib.sqlmapper.update.DirectUpdater;
import net.avacati.lib.sqlmapper.update.IndirectUpdater;
import net.avacati.lib.sqlmapper.util.SqlDoerH2;
import net.avacati.lib.sqlmapper.util.TypeMapConfig;
import org.h2.jdbcx.JdbcDataSource;

import java.util.Map;

public class SqlMapperFactory<T> {
    private final TableCreator tableCreator;
    private final SqlDataStore<T> sqlDataStore;
    private Class<T> dboType;

    public SqlMapperFactory(Class<T> dboType, Map<Class, TypeMapConfig> typemap, JdbcDataSource dataSource) {
        SqlDoerH2 sqlDoerH2 = SqlDoerH2.create(dataSource);
        this.tableCreator = new TableCreator(new IndirectTableCreator(typemap, new DirectTableCreator(typemap)), sqlDoerH2);
        final IndirectInserter indirectInserter = new IndirectInserter(typemap, new DirectInserter(typemap));
        this.sqlDataStore = new SqlDataStore<>(
                indirectInserter,
                sqlDoerH2,
                new DirectSelecter(typemap, sqlDoerH2),
                dboType,
                new IndirectUpdater(typemap, new DirectUpdater(typemap), indirectInserter));
        this.dboType = dboType;
    }

    public void createSchema() {
        this.tableCreator.createTableFor(this.dboType);
    }

    public SqlDataStore<T> getSqlDataStore() {
        return this.sqlDataStore;
    }
}
