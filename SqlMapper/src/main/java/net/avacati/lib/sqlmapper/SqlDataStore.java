package net.avacati.lib.sqlmapper;

import net.avacati.lib.aggregaterepository.DataStore;
import net.avacati.lib.sqlmapper.insert.IndirectInserter;
import net.avacati.lib.sqlmapper.select.DirectSelecter;
import net.avacati.lib.sqlmapper.update.IndirectUpdater;
import net.avacati.lib.sqlmapper.util.SqlDoer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

class SqlDataStore<T> implements DataStore<T> {
    private IndirectInserter indirectInserter;
    private SqlDoer sqlDoer;
    private DirectSelecter directSelecter;
    private Class<T> dboType;
    private IndirectUpdater indirectUpdater;

    public SqlDataStore(IndirectInserter indirectInserter, SqlDoer sqlDoer, DirectSelecter directSelecter, Class<T> dboType, IndirectUpdater indirectUpdater) {
        this.indirectInserter = indirectInserter;
        this.sqlDoer = sqlDoer;
        this.directSelecter = directSelecter;
        this.dboType = dboType;
        this.indirectUpdater = indirectUpdater;
    }

    @Override
    public void insert(UUID id, T r) {
        List<String> sqls = this.indirectInserter.createInsertSqlsForObjectTree(r);
        sqls.stream().forEach(this.sqlDoer::doSql);
    }

    @Override
    public void update(UUID id, T newDbo, T oldDbo) {
        final List<String> updateSqlsForObjectTree = this.indirectUpdater.createUpdateSqlsForObjectTree(newDbo, oldDbo);
        updateSqlsForObjectTree.stream().forEach(this.sqlDoer::doSql);
    }

    @Override
    public T get(UUID id) {
        return this.directSelecter.getDbo(this.dboType, id.toString()).get();
    }

    @Override
    public Collection<T> getAll() {
        return null;
    }
}
