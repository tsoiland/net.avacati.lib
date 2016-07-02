package net.avacati.lib.sqlmapper;

import java.util.UUID;

public interface SqlDataStore {
    void insert(Object dbo);

    <T> void update(T newDbo, T oldDbo);

    <T> T get(Class<T> dboType, UUID id);
}
