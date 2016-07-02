package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.insert.IndirectInserter;
import net.avacati.lib.sqlmapper.select.DirectSelecter;
import net.avacati.lib.sqlmapper.update.IndirectUpdater;
import net.avacati.lib.sqlmapper.util.JdbcHelper;

import java.util.List;
import java.util.UUID;

public class SqlDataStoreImpl implements SqlDataStore {
    private IndirectInserter indirectInserter;
    private JdbcHelper jdbcHelper;
    private DirectSelecter directSelecter;
    private IndirectUpdater indirectUpdater;

    SqlDataStoreImpl(IndirectInserter indirectInserter, JdbcHelper jdbcHelper, DirectSelecter directSelecter, IndirectUpdater indirectUpdater) {
        this.indirectInserter = indirectInserter;
        this.jdbcHelper = jdbcHelper;
        this.directSelecter = directSelecter;
        this.indirectUpdater = indirectUpdater;
    }

    @Override
    public void insert(Object dbo) {
        List<String> sqls = this.indirectInserter.createInsertSqlsForObjectTree(dbo);
        sqls.stream().forEach(this.jdbcHelper::execute);
    }

    @Override
    public <T> void update(T newDbo, T oldDbo) {
        final List<String> updateSqlsForObjectTree = this.indirectUpdater.createUpdateSqlsForObjectTree(newDbo, oldDbo);
        updateSqlsForObjectTree.stream().forEach(this.jdbcHelper::execute);
    }

    @Override
    public <T> T get(Class<T> dboType, UUID id) {
        return this.directSelecter.getDbo(dboType, id.toString()).get();
    }
}
