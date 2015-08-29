package net.avacati.lib.serializingdatastore.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Migrator {
    private List<Migration> list = new ArrayList<>();

    public <V1, V2> void add(Function<V1, V2> function, Class<V1> v1Class, Class<V2> v2Class) {
        final Migration migration = new Migration(function, v1Class, v2Class);
        this.list.add(migration);
    }

    Object migrate(Object value, Class v2Class) {
        final Migration v1ToV2Migration = list
                .stream()
                .filter(migration -> migration.match(value.getClass(), v2Class))
                .findFirst()
                .get();

        return v1ToV2Migration.migrate(value);
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

        private boolean match(Class v1, Class v2) {
            return this.v1Class.isAssignableFrom(v1) && this.v2Class.isAssignableFrom(v2);
        }

        private Object migrate(Object v1) {
            return function.apply(v1);
        }
    }
}
