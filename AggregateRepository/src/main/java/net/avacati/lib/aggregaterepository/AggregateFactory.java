package net.avacati.lib.aggregaterepository;

public interface AggregateFactory<A, Dbo> {
    A createFromDbo(Dbo dbo);
}
