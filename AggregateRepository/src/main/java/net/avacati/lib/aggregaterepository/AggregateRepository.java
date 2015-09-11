package net.avacati.lib.aggregaterepository;

import java.util.function.Function;

public class AggregateRepository<Aggregate, Dbo> {
    private final UnitOfWork<Dbo> unitOfWork;
    private final Repository<Aggregate, Dbo> innerRepo;

    private AggregateRepository(
            DataStore<Dbo> aggregateRepositoryDataStore,
            AggregateFactory<Aggregate, Dbo> factory,
            Function<Aggregate, Dbo>
            dboFromAggregateEntity) {
        this.unitOfWork = new UnitOfWork<>(aggregateRepositoryDataStore);
        this.innerRepo = new Repository<>(aggregateRepositoryDataStore, unitOfWork, factory, dboFromAggregateEntity);
    }

    public static <Aggregate, Dbo> AggregateRepository<Aggregate, Dbo> create(
            DataStore<Dbo> aggregateRepositoryDataStore,
            AggregateFactory<Aggregate, Dbo> factory,
            Function<Aggregate, Dbo> dboFromAggregateEntity) {
        return new AggregateRepository<>(aggregateRepositoryDataStore, factory, dboFromAggregateEntity);
    }

    public UnitOfWork<Dbo> getUnitOfWork() {
        return unitOfWork;
    }

    public Repository<Aggregate, Dbo> getInnerRepo() {
        return innerRepo;
    }
}
