package net.avacati.lib.aggregaterepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
}
