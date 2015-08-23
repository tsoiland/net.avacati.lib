package net.avacati.lib.aggregaterepository;

import java.util.UUID;

public interface DataStore<D> {
    void insert(UUID id, D r);
    void update(UUID id, D newDbo, D oldDbo);
    D get(UUID id);
}
