package net.avacati.lib.aggregaterepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Generic repository for entities that
 * - uses dbo pattern,
 * - can implement Aggregate interface,
 * - and do not need queries other than getById.
 *
 * @param <A>   the entity type we're storing
 * @param <Dbo> the corresponding dbo type
 */
public class Repository<A, Dbo> {
    private DataStore<Dbo> innerRepo;
    private UnitOfWork<Dbo> unitOfWork;
    private AggregateFactory<A, Dbo> aggregateEntityFromDbo;
    private Map<UUID, A> identityMap;
    private Function<A, Dbo> dboFromAggregateEntity;

    public Repository(DataStore<Dbo> innerRepo, UnitOfWork<Dbo> unitOfWork, AggregateFactory<A, Dbo> aggregateEntityFromDbo, Function<A, Dbo>
            dboFromAggregateEntity) {
        this.innerRepo = innerRepo;
        this.unitOfWork = unitOfWork;
        this.aggregateEntityFromDbo = aggregateEntityFromDbo;
        this.dboFromAggregateEntity = dboFromAggregateEntity;
        this.identityMap = new HashMap<>();
    }

    public void add(UUID id, A aggregateEntity) {
        // Add it to the identity map.
        if(this.identityMap.containsKey(id)) {
            throw new DuplicateAddException(
                    "Duplicate key in Identity Map. Either added same id twice, or added id that was returned by get(). Id: " + id);
        }
        this.identityMap.put(id, aggregateEntity);
        
        this.unitOfWork.insert(id, () -> this.dboFromAggregateEntity.apply(aggregateEntity));
    }

    public A get(UUID id, Function<A, Dbo> getDbo) {
        // If it has already been loaded, return the one from the identity map.
        final A alreadyLoadedEntity = this.identityMap.get(id);
        if (alreadyLoadedEntity != null) {
            return alreadyLoadedEntity;
        }

        // If not, try to fetch the dbo.
        Dbo dbo = this.innerRepo.get(id);
        if (dbo == null) {
            throw new NoSuchEntityException(id);
        }

        // If there is a dbo, use it create an Aggregate entity.
        A aggregateEntity = this.aggregateEntityFromDbo.createFromDbo(dbo);

        // Add it to the identity map.
        this.identityMap.put(id, aggregateEntity);

        // Tell the unit of work that we are giving the entity to someone who might change it and cause it to need saving.
        this.unitOfWork.maybeUpdate(id, () -> getDbo.apply(aggregateEntity));

        // Return the aggregate entity.
        return aggregateEntity;
    }

    public Collection<Dbo> getAll() {
        return this.innerRepo.getAll();
    }
}

