package net.avacati.lib.sqlmapper;

import net.avacati.lib.aggregaterepository.DataStore;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

class SqlDataStore<T> implements DataStore<T> {
    private IndirectMapper indirectMapper;
    private SqlDoer sqlDoer;
    private DirectSelecter directSelecter;
    private Class<T> dboType;
    private IndirectUpdater indirectUpdater;

    public SqlDataStore(IndirectMapper indirectMapper, SqlDoer sqlDoer, DirectSelecter directSelecter, Class<T> dboType, IndirectUpdater indirectUpdater) {
        this.indirectMapper = indirectMapper;
        this.sqlDoer = sqlDoer;
        this.directSelecter = directSelecter;
        this.dboType = dboType;
        this.indirectUpdater = indirectUpdater;
    }

    @Override
    public void insert(UUID id, T r) {
        List<String> sqls = this.indirectMapper.createInsertSqlsForObjectTree(r);
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
