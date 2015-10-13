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

    public SqlDataStore(IndirectMapper indirectMapper, SqlDoer sqlDoer, DirectSelecter directSelecter, Class<T> dboType) {
        this.indirectMapper = indirectMapper;
        this.sqlDoer = sqlDoer;
        this.directSelecter = directSelecter;
        this.dboType = dboType;
    }

    @Override
    public void insert(UUID id, T r) {
        List<String> sqls = this.indirectMapper.createInsertSqlsForObjectTree(r);
        sqls.stream().forEach(sqlDoer::doSql);
    }

    @Override
    public void update(UUID id, T newDbo, T oldDbo) {

    }

    @Override
    public T get(UUID id) {
        return this.directSelecter.getDbo(dboType, id.toString());
    }

    @Override
    public Collection<T> getAll() {
        return null;
    }
}
