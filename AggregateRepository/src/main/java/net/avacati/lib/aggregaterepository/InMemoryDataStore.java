package net.avacati.lib.aggregaterepository;

import java.util.*;

public class InMemoryDataStore<D> implements DataStore<D> {
    private Map<UUID, D> dboList;

    public InMemoryDataStore() {
        this.dboList = new HashMap<>();
    }

    public void insert(UUID id, D dbo) {
        this.dboList.put(id, dbo);
    }

    public void update(UUID id, D dbo, D oldDbo) {
        this.dboList.remove(id, oldDbo);
        this.insert(id, dbo);
    }

    public D get(UUID id) {
        return this.dboList.get(id);
    }

    @Override
    public Collection<D> getAll() {
        return dboList.values();
    }
}
