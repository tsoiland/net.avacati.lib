package net.avacati.lib.aggregaterepository;

import java.util.UUID;

public class NoSuchEntityException extends RuntimeException {
    private UUID id;

    public NoSuchEntityException(UUID id) {
        super("Underlying datastore returned null for id: " + id + ". Cannot create aggregate without dbo.");
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
