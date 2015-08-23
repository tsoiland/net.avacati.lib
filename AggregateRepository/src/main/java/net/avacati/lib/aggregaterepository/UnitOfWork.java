package net.avacati.lib.aggregaterepository;

import java.util.ArrayList;
import java.util.List;

public class UnitOfWork<E extends Aggregate<D>, D> {
    private DataStore<D> dataStore;
    private List<E> insertsDirty;
    private List<E> maybeDirty;

    public UnitOfWork(DataStore<D> dataStore) {
        this.dataStore = dataStore;
        this.insertsDirty = new ArrayList<>();
        this.maybeDirty = new ArrayList<>();
    }

    public void insert(E entity) {
        this.insertsDirty.add(entity);
    }

    public void maybeUpdate(E entity) {
        this.maybeDirty.add(entity);
    }

    public void save() {
        // Perform all inserts.
        this.insertsDirty.forEach(entity -> this.dataStore.insert(entity.getId(), entity.getDbo()));
        this.insertsDirty.clear();

        // Perform update on all entities that might have been changed.
        this.maybeDirty.forEach(
                entity -> {
                    D oldDbo = this.dataStore.get(entity.getId());
                    this.dataStore.update(entity.getId(), entity.getDbo(), oldDbo);
            });
        this.maybeDirty.clear();
    }

}

