package net.avacati.lib.aggregaterepository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class UnitOfWork<D> {
    private DataStore<D> dataStore;
    private List<PieceOfWork<D>> insertsDirty;
    private List<PieceOfWork<D>> maybeDirty;

    public UnitOfWork(DataStore<D> dataStore) {
        this.dataStore = dataStore;
        this.insertsDirty = new ArrayList<>();
        this.maybeDirty = new ArrayList<>();
    }

    public void insert(UUID id, D dbo) {
        this.insertsDirty.add(new PieceOfWork<>(id, () -> dbo));
    }

    public void maybeUpdate(UUID id, Supplier<D> dbo) {
        this.maybeDirty.add(new PieceOfWork<>(id, dbo));
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

    /**
     * One single entity that should be changed.
     */
    private static class PieceOfWork<E> implements Serializable {
        private UUID id;
        private Supplier<E> dbo;

        private PieceOfWork(UUID id, Supplier<E> dbo) {
            this.id = id;
            this.dbo = dbo;
        }

        public UUID getId() {
            return id;
        }

        public E getDbo() {
            return dbo.get();
        }
    }
}

