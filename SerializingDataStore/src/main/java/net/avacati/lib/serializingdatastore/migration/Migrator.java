package net.avacati.lib.serializingdatastore.migration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A holder and applier of migrations between objects. It works with one destination class.
 */
public class Migrator {
    private List<Migration> list = new ArrayList<>();
    private Class<?> destinationClass;
    private List<Class> latestVersionOfAllClassesToMigrate;

    /**
     * Create a holder and applier for migrations between objects
     *
     * @param destinationClass                   the newest version of the aggregate/top-level domain entity. The one we'll be migrating to in all
     *                                           cases.
     * @param latestVersionOfAllClassesToMigrate a complete list (only newest versions) of the domain entities involved in the aggregate that should
     *                                           be migrated.
     */
    public Migrator(Class<?> destinationClass, List<Class> latestVersionOfAllClassesToMigrate) {
        this.destinationClass = destinationClass;
        this.latestVersionOfAllClassesToMigrate = latestVersionOfAllClassesToMigrate;
    }

    /**
     * Convenience constructor for {@link #Migrator(Class, List)} if there is only one class (including sub entities) that should be migrated.
     */
    public Migrator(Class<?> destinationClass) {
        this.destinationClass = destinationClass;
        this.latestVersionOfAllClassesToMigrate = Arrays.asList(destinationClass);
    }

    /**
     * Register a function and explicitly state which classes it migrates from and to.
     */
    public <V1, V2> void add(Function<V1, V2> function, Class<V1> v1Class, Class<V2> v2Class) {
        final Migration migration = new Migration<>(function, v1Class, v2Class);
        this.list.add(migration);
    }

    /**
     * Use migrations registered through {@link #add(Function, Class, Class)} to construct a chain of migrations that will result in an object of the
     * type that was specified in {@link #Migrator(Class, List)}.
     */
    Object migrate(Object v1Object) {
        // If it's already the correct version, just return it.
        if (v1Object.getClass().isAssignableFrom(this.destinationClass)) {
            return v1Object;
        }

        // Start a new migration path. From the class of our parameter, to the class given in constructor.
        MigrationPath migrationPath = new MigrationPath(v1Object.getClass(), this.destinationClass);

        // Keep looking up steps and adding them until path is complete (it can migrate it's way from our parameter to the desired class.
        do {
            Migration nextStep = findFirstMigrationFromStepToAnyWhere(migrationPath.latestStep());
            migrationPath.addMigrationStep(nextStep);
        } while (!migrationPath.isComplete());

        // Apply the assembled migration path to the object at hand.
        return migrationPath.runThroughEntirePath(v1Object);
    }

    Map<String, Class> getLatestVersionOfAllClassesToMigrate() {
        return this.latestVersionOfAllClassesToMigrate.stream().collect(Collectors.toMap(Class::getName, aClass -> aClass));
    }

    private Migration findFirstMigrationFromStepToAnyWhere(Class value) {
        return list
                .stream()
                .filter(migration -> migration.canMigrateFrom(value))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No migration from class: " + value.getName()));
    }

    /**
     * A simple holder of one migration function and the from and to classes it applies to.
     */
    private static class Migration<From, To> {
        private Function<From, To> function;
        private Class<From> fromClass;
        private Class<To> toClass;

        private Migration(Function<From, To> function, Class<From> fromClass, Class<To> toClass) {
            this.function = function;
            this.fromClass = fromClass;
            this.toClass = toClass;
        }

        private To migrate(From from) {
            return function.apply(from);
        }

        public boolean canMigrateFrom(Class v1) {
            return this.fromClass.isAssignableFrom(v1);
        }
    }

    /**
     * Represents a chain of migrations from one class to another. It is not possible to add migrations that does not fit into the chain, and {@link
     * #isComplete()} will not return until the last migration in the chain will produce the "to"-class.
     */
    private static class MigrationPath {
        private Deque<Migration> migrationPath = new ArrayDeque<>();
        private Class fromClass;
        private Class toClass;

        private MigrationPath(Class fromClass, Class toClass) {
            this.fromClass = fromClass;
            this.toClass = toClass;
        }

        public void addMigrationStep(Migration migration) {
            if (!migration.fromClass.isAssignableFrom(this.latestStep())) {
                throw new RuntimeException("Migration cannot be added to path because it is not linked with tip of the current path.");
            }

            this.migrationPath.add(migration);
        }

        public Class latestStep() {
            return migrationPath.isEmpty() ? fromClass : migrationPath.peekLast().toClass;
        }

        public boolean isComplete() {
            return this.migrationPath.peekLast().toClass.isAssignableFrom(this.toClass);
        }

        /**
         * Run the added migrations in order, giving the result from the first as input to the next until all have been run and we have an object of
         * the desired class.
         */
        public Object runThroughEntirePath(Object firstVersion) {
            Object currentVersion = firstVersion;
            for (Migration migration : this.migrationPath) {
                currentVersion = migration.migrate(currentVersion);
            }

            if (!currentVersion.getClass().isAssignableFrom(this.toClass)) {
                throw new RuntimeException("Ran the whole migration path, but current object is still not target version.");
            }

            return currentVersion;
        }
    }
}
