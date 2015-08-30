package net.avacati.lib.serializingdatastore.migration;

import java.util.*;
import java.util.function.Function;

public class Migrator {
    private List<Migration> list = new ArrayList<>();

    public <V1, V2> void add(Function<V1, V2> function, Class<V1> v1Class, Class<V2> v2Class) {
        final Migration migration = new Migration(function, v1Class, v2Class);
        this.list.add(migration);
    }

    Object migrate(Object v1Object, Class v2Class) {
        MigrationPath migrationPath = new MigrationPath(v1Object.getClass(), v2Class);

        do {
            Migration nextStep = findMigrationFromStepToAnyWhere(migrationPath.latestStep());
            migrationPath.addMigrationStep(nextStep);
        } while (!migrationPath.isComplete());


        return migrationPath.runThroughEntirePath(v1Object);
    }

    private class MigrationPath {
        private Deque<Migration> migrationPath = new ArrayDeque<>();
        private Class fromClass;
        private Class toClass;

        private MigrationPath(Class fromClass, Class toClass) {
            this.fromClass = fromClass;
            this.toClass = toClass;
        }

        public void addMigrationStep(Migration migration) {
            if(!migration.v1Class.isAssignableFrom(this.latestStep())) {
                throw new RuntimeException("Migration cannot be added to path because it is not linked with tip of the current path.");
            }

            this.migrationPath.add(migration);
        }

        public Class latestStep() {
            return migrationPath.isEmpty() ? fromClass : migrationPath.peekLast().v2Class;
        }

        public boolean isComplete() {
            return this.migrationPath.peekLast().v2Class.isAssignableFrom(this.toClass);
        }

        public Object runThroughEntirePath(Object firstVersion) {
            Object currentVersion = firstVersion;
            for(Migration migration : this.migrationPath) {
                currentVersion = migration.migrate(currentVersion);
            }

            if(!currentVersion.getClass().isAssignableFrom(this.toClass)) {
                throw new RuntimeException("Ran the whole migration path, but current object is still not target version.");
            }

            return currentVersion;
        }
    }

    private Migration findMigrationFromStepToAnyWhere(Class value) {
        return list
                .stream()
                .filter(migration -> migration.canMigrateFrom(value))
                .findFirst()
                .get();
    }

    private static class Migration {
        private Function function;
        private Class<?> v1Class;
        private Class<?> v2Class;

        private Migration(Function function, Class v1Class, Class v2Class) {
            this.function = function;
            this.v1Class = v1Class;
            this.v2Class = v2Class;
        }

        private Object migrate(Object v1) {
            return function.apply(v1);
        }

        public boolean canMigrateFrom(Class v1) {
            return this.v1Class.isAssignableFrom(v1);
        }
    }
}
