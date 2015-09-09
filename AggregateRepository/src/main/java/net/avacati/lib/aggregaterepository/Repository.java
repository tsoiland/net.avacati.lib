package net.avacati.lib.aggregaterepository;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

/**
 * Generic repository for entities that
 *  - uses dbo pattern,
 *  - can implement Aggregate interface,
 *  - and do not need queries other than getById.
 *
 * @param <A> the entity type we're storing
 * @param <Dbo> the corresponding dbo type
 */
public class Repository<A , Dbo>{
    private DataStore<Dbo> innerRepo;
    private UnitOfWork<Dbo> unitOfWork;
    private AggregateFactory<A, Dbo> factory;

    public Repository(DataStore<Dbo> innerRepo, UnitOfWork<Dbo> unitOfWork, AggregateFactory<A, Dbo> factory) {
        this.innerRepo = innerRepo;
        this.unitOfWork = unitOfWork;
        this.factory = factory;
    }

    public void add(UUID id, Dbo dbo) {
        this.unitOfWork.insert(id, dbo);
    }

    public A get(UUID id, Function<A,Dbo> getDbo) {
        Dbo dbo = this.innerRepo.get(id);
        A order = this.factory.createFromDbo(dbo);
        this.unitOfWork.maybeUpdate(id, () -> getDbo.apply(order));
        return order;
    }

    public Collection<Dbo> getAll() {
        return this.innerRepo.getAll();
    }
}

