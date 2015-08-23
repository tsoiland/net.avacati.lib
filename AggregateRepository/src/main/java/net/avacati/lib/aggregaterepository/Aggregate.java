package net.avacati.lib.aggregaterepository;

import java.util.UUID;

/**
 * Represents an entity class that the unit of work can handle.
 * @param <Dbo> the dbo class of the aggregate entity
 */
public interface Aggregate<Dbo> {
    UUID getId();
    Dbo getDbo();

    interface AggregateFactory<A extends Aggregate<Dbo>, Dbo> {
        A createFromDbo(Dbo dbo);
    }
}
