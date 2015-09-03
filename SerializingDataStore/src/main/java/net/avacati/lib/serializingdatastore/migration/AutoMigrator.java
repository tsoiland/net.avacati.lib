package net.avacati.lib.serializingdatastore.migration;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AutoMigrator <V2> {
    private Map<Class, Class> mapFromTo = new HashMap<>();
    private Class<V2> v2Class;

    public AutoMigrator(Class<V2> v2Class) {
        this.v2Class = v2Class;
    }

    public void addSubDboTypeMap(Class from, Class to) {
        this.mapFromTo.put(from, to);
    }

    public  <V1> Function<V1,V2> createAutoMigration() {
        return (V1 v1) -> autoMigrateObjectToClass(v1, this.v2Class);
    }

    private <V1, V2> V2 autoMigrateObjectToClass(V1 v1, Class<V2> v2Class) {
        try {
            // Instantiate
            final V2 v2 = v2Class.newInstance();

            // Copy over all values
            Arrays.stream(v1.getClass().getFields())
                    .forEach(fieldOnV1 -> {
                        try {
                            final Field fieldOnV2 = v2.getClass().getField(fieldOnV1.getName());
                            final Object v2Value = mapFieldObjectFromTypeToType(fieldOnV1.get(v1), fieldOnV1.getType(), fieldOnV2.getType());
                            fieldOnV2.set(v2, v2Value);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    });

            // Return
            return v2;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Object mapFieldObjectFromTypeToType(Object v1FieldValue, Class<?> v1FieldType, Class<?> v2FieldType) {
        // If it's a collection recursively auto migrate each item
        if (v2FieldType.isAssignableFrom(Collection.class)){
            final Collection<Object> fieldValueOnV11 = (Collection<Object>) v1FieldValue;
            return fieldValueOnV11
                    .stream()
                    .map(o -> autoMigrateObjectToClass(o, this.mapFromTo.get(o.getClass())))
                    .collect(Collectors.toList());
        }
        // If it's the same type, then simply use the v1 value
        else if (v2FieldType.isAssignableFrom(v1FieldType)) {
            return v1FieldValue;
        }
        // If it's not the same type try to recursively auto map it.
        else {
            return autoMigrateObjectToClass(v1FieldValue, v2FieldType);
        }
    }
}
