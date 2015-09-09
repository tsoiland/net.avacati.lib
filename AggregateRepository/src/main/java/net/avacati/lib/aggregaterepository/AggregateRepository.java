package net.avacati.lib.aggregaterepository;

public class AggregateRepository<Aggregate, Dbo> {
    private final UnitOfWork<Dbo> unitOfWork;
    private final Repository<Aggregate, Dbo> innerRepo;

    private AggregateRepository(DataStore<Dbo> aggregateRepositoryDataStore, AggregateFactory<Aggregate, Dbo> factory) {
        this.unitOfWork = new UnitOfWork<>(aggregateRepositoryDataStore);
        this.innerRepo = new Repository<>(aggregateRepositoryDataStore, unitOfWork, factory);
    }

    public static <Aggregate, Dbo> AggregateRepository<Aggregate, Dbo> create(
            DataStore<Dbo> aggregateRepositoryDataStore,
            AggregateFactory<Aggregate, Dbo> factory) {
        return new AggregateRepository<>(aggregateRepositoryDataStore, factory);
    }

    public UnitOfWork<Dbo> getUnitOfWork() {
        return unitOfWork;
    }

    public Repository<Aggregate, Dbo> getInnerRepo() {
        return innerRepo;
    }
}
