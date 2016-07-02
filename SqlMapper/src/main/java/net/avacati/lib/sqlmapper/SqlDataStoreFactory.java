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

import java.sql.Connection;
import java.util.Map;

public class SqlDataStoreFactory {
    private Map<Class, TypeMapConfig> typeConfigMap;

    public SqlDataStoreFactory(Map<Class, TypeMapConfig> typeConfigMap) {
        this.typeConfigMap = typeConfigMap;
    }

    public SqlDataStore createSqlDataStore(Connection connection) {
        // SqlDoer
        final SqlDoerH2 sqlDoer = new SqlDoerH2(connection);

        // Selecter
        final DirectSelecter directSelecter = new DirectSelecter(this.typeConfigMap, sqlDoer);

        // Inserter
        final IndirectInserter indirectInserter = new IndirectInserter(
                this.typeConfigMap,
                new DirectInserter(this.typeConfigMap));

        // Updater
        final IndirectUpdater indirectUpdater = new IndirectUpdater(
                this.typeConfigMap,
                new DirectUpdater(this.typeConfigMap),
                indirectInserter);

        // SqlDataStore
        return new SqlDataStoreImpl(
                indirectInserter,
                sqlDoer,
                directSelecter,
                indirectUpdater);
    }

    public void createSchema(Class<?> dboType, Connection connection) {
        final IndirectTableCreator indirectTableCreator = new IndirectTableCreator(
                this.typeConfigMap,
                new DirectTableCreator(this.typeConfigMap));
        final TableCreator tableCreator = new TableCreator(indirectTableCreator, new SqlDoerH2(connection));
        tableCreator.createTableFor(dboType);
    }
}
