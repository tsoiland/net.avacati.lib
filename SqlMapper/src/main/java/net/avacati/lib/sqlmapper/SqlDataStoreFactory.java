package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.insert.DirectInserter;
import net.avacati.lib.sqlmapper.insert.IndirectInserter;
import net.avacati.lib.sqlmapper.schema.DirectTableCreator;
import net.avacati.lib.sqlmapper.schema.IndirectTableCreator;
import net.avacati.lib.sqlmapper.schema.TableCreator;
import net.avacati.lib.sqlmapper.select.DirectSelecter;
import net.avacati.lib.sqlmapper.update.DirectUpdater;
import net.avacati.lib.sqlmapper.update.IndirectUpdater;
import net.avacati.lib.sqlmapper.util.JdbcHelper;
import net.avacati.lib.sqlmapper.typeconfig.TypeMap;

import java.sql.Connection;

public class SqlDataStoreFactory {
    private TypeMap typeConfigMap;

    public SqlDataStoreFactory(TypeMap typeMap) {
        this.typeConfigMap = typeMap;
    }

    public SqlDataStore createSqlDataStore(Connection connection) {
        // JdbcHelper
        final JdbcHelper jdbcHelper = new JdbcHelper(connection);

        // Selecter
        final DirectSelecter directSelecter = new DirectSelecter(this.typeConfigMap, jdbcHelper);

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
                jdbcHelper,
                directSelecter,
                indirectUpdater);
    }

    public void createSchema(Connection connection) {
        // Table Creator
        final TableCreator tableCreator = new TableCreator(
                new IndirectTableCreator(this.typeConfigMap,
                        new DirectTableCreator(this.typeConfigMap)),
                new JdbcHelper(connection));

        // Create schema for all Root entites (creator will recurse)
        this.typeConfigMap.getRootDboClasses()
                          .forEach(tableCreator::createTableFor);
    }
}
