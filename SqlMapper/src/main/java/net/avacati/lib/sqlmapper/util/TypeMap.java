package net.avacati.lib.sqlmapper.util;

import net.avacati.lib.sqlmapper.util.TypeMapConfig.ErasedTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class TypeMap {
    private final Map<Class, TypeMapConfig> map = new HashMap<>();

    public void putStandardTypeConfigs() {
        this.directToString(String.class, s -> s);
        this.directToString(UUID.class, UUID::fromString);
        this.direct(int.class, o -> Integer.toString((int) o), Integer::parseInt);
        this.direct(Instant.class, o -> Long.toString(((Instant) o).toEpochMilli()), s -> Instant.ofEpochMilli(Long.valueOf(s)));
        this.asList(List.class);
    }

    public TypeMapConfig get(Class<?> dboClass) {
        return this.map.get(dboClass);
    }

    public void directToString(Class mappedType, Function<String, Object> reverseFunction) {
        this.map.put(mappedType, TypeMapConfig.direct(Object::toString, reverseFunction));
    }

    public void direct(Class mappedType, Function<Object, String> mappingFunction, Function<String, Object> reverseFunction) {
        this.map.put(mappedType, TypeMapConfig.direct(mappingFunction, reverseFunction));
    }

    public void asDboToTable(Class mappedType, String tableName, Function<Object, String> primayKeyMapFunction, String primaryKeyFieldName) {
        this.asDboToTable(mappedType, tableName, primayKeyMapFunction, primaryKeyFieldName, new ErasedTypes());
    }

    public void asDboToTable(Class mappedType, String tableName, Function<Object, String> primayKeyMapFunction, String primaryKeyFieldName, ErasedTypes erasedTypes) {
        this.map.put(mappedType, TypeMapConfig.asDboToTable(tableName, primayKeyMapFunction, primaryKeyFieldName, erasedTypes));
    }

    public void asSubDbo(Class mappedType, String tableName, Function<Object, String> foreignKeyFunction, String primaryKeyFieldName) {
        this.asSubDbo(mappedType, tableName, foreignKeyFunction, primaryKeyFieldName, new ErasedTypes());
    }

    public void asSubDbo(Class mappedType, String tableName, Function<Object, String> primaryKeyFunction, String primaryKeyFieldName, ErasedTypes erasedTypes) {
        this.map.put(mappedType, TypeMapConfig.asSubDbo(tableName, primaryKeyFunction, primaryKeyFieldName, erasedTypes));

    }

    public void asList(Class mappedType) {
        this.map.put(mappedType, TypeMapConfig.asList());
    }

    public <Dbo> boolean containsKey(Class<Dbo> type) {
        return this.map.containsKey(type);
    }
}
