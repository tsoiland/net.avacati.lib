package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.insert.IndirectInserter;
import net.avacati.lib.sqlmapper.select.DirectSelecter;
import net.avacati.lib.sqlmapper.update.IndirectUpdater;
import net.avacati.lib.sqlmapper.util.SqlDoer;

import java.util.List;
import java.util.UUID;

public class SqlDataStoreImpl implements SqlDataStore {
    private IndirectInserter indirectInserter;
    private SqlDoer sqlDoer;
    private DirectSelecter directSelecter;
    private IndirectUpdater indirectUpdater;

    SqlDataStoreImpl(IndirectInserter indirectInserter, SqlDoer sqlDoer, DirectSelecter directSelecter, IndirectUpdater indirectUpdater) {
        this.indirectInserter = indirectInserter;
        this.sqlDoer = sqlDoer;
        this.directSelecter = directSelecter;
        this.indirectUpdater = indirectUpdater;
    }

    @Override
    public void insert(Object dbo) {
        List<String> sqls = this.indirectInserter.createInsertSqlsForObjectTree(dbo);
        sqls.stream().forEach(this.sqlDoer::execute);
    }

    @Override
    public <T> void update(T newDbo, T oldDbo) {
        final List<String> updateSqlsForObjectTree = this.indirectUpdater.createUpdateSqlsForObjectTree(newDbo, oldDbo);
        updateSqlsForObjectTree.stream().forEach(this.sqlDoer::execute);
    }

    @Override
    public <T> T get(Class<T> dboType, UUID id) {
        return this.directSelecter.getDbo(dboType, id.toString()).get();
    }
}
