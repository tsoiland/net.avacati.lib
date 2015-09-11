package net.avacati.lib.aggregaterepository;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;

public class UnitOfWork<D> {
    private DataStore<D> dataStore;
    private List<PieceOfWork<D>> insertsDirty;
    private List<PieceOfWork<D>> maybeDirty;

    UnitOfWork(DataStore<D> dataStore) {
        this.dataStore = dataStore;
        this.insertsDirty = new ArrayList<>();
        this.maybeDirty = new ArrayList<>();
    }

    void insert(UUID id, Supplier<D> dboSupplier) {
        this.insertsDirty.add(new PieceOfWork<>(id, dboSupplier));
    }

    void maybeUpdate(UUID id, Supplier<D> dboSupplier) {
        this.maybeDirty.add(new PieceOfWork<>(id, dboSupplier));
    }

    public void save() {
        // Perform update on all entities that might have been changed.
        this.maybeDirty.forEach(
                entity -> {
                    D oldDbo = this.dataStore.get(entity.getId());
                    this.dataStore.update(entity.getId(), entity.getDbo(), oldDbo);
            });

        // Perform all inserts.
        this.insertsDirty.forEach(entity -> this.dataStore.insert(entity.getId(), entity.getDbo()));

        // The inserted entities can still be modified, but they should _never_ be _inserted_ again.
        this.maybeDirty.addAll(this.insertsDirty);
        this.insertsDirty.clear();
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

