package net.avacati.lib.aggregaterepository;

import java.util.UUID;

/**
 * Generic repository for entities that
 *  - uses dbo pattern,
 *  - can implement Aggregate interface,
 *  - and do not need queries other than getById.
 *
 * @param <A> the entity type we're storing
 * @param <Dbo> the corresponding dbo type
 */
public class Repository<A extends Aggregate<Dbo>, Dbo>{
    private DataStore<Dbo> innerRepo;
    private UnitOfWork<A, Dbo> unitOfWork;
    private Aggregate.AggregateFactory<A, Dbo> factory;

    public Repository(DataStore<Dbo> innerRepo, UnitOfWork<A, Dbo> unitOfWork, Aggregate.AggregateFactory<A, Dbo> factory) {
        this.innerRepo = innerRepo;
        this.unitOfWork = unitOfWork;
        this.factory = factory;
    }

    public void add(A order) {
        this.unitOfWork.insert(order);
    }

    public A get(UUID id) {
        Dbo dbo = this.innerRepo.get(id);
        A order = this.factory.createFromDbo(dbo);
        this.unitOfWork.maybeUpdate(order);
        return order;
    }
}

