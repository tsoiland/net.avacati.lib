package net.avacati.lib.sqlmapper.typeconfig;

import net.avacati.lib.sqlmapper.typeconfig.TypeConfig.ErasedTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TypeMap {
    private final Map<Class, TypeConfig> map = new HashMap<>();

    public TypeMap putStandardTypeConfigs() {
        this.directToString(String.class, s -> s);
        this.directToString(UUID.class, UUID::fromString);
        this.direct(int.class, o -> Integer.toString((int) o), Integer::parseInt);
        this.direct(Instant.class, o -> Long.toString(((Instant) o).toEpochMilli()), s -> Instant.ofEpochMilli(Long.valueOf(s)));
        this.asList(List.class);
        return this;
    }

    public TypeConfig get(Class<?> dboClass) {
        // Do we even support it?
        if (!this.map.containsKey(dboClass)) {
            throw new TypeNotSupportedException(dboClass);
        }

        return this.map.get(dboClass);
    }

    public <T> TypeMap directToString(Class<T> mappedType, Function<String, T> reverseFunction) {
        this.map.put(mappedType, TypeConfig.direct(Object::toString, reverseFunction));
        return this;
    }

    public <T> TypeMap direct(Class<T> mappedType, Function<T, String> mappingFunction, Function<String, T> reverseFunction) {
        this.map.put(mappedType, TypeConfig.direct(mappingFunction, reverseFunction));
        return this;
    }

    public <T> TypeMap asDboToTable(Class<T> mappedType, String tableName, Function<T, String> primayKeyMapFunction, String primaryKeyFieldName) {
        this.asDboToTable(mappedType, tableName, primayKeyMapFunction, primaryKeyFieldName, new ErasedTypes());
        return this;
    }

    public <T> TypeMap asDboToTable(Class<T> mappedType, String tableName, Function<T, String> primayKeyMapFunction, String primaryKeyFieldName, ErasedTypes erasedTypes) {
        this.map.put(mappedType, TypeConfig.asDboToTable(tableName, primayKeyMapFunction, primaryKeyFieldName, erasedTypes));
        return this;
    }

    public <T> TypeMap asSubDbo(Class<T> mappedType, String tableName, Function<T, String> foreignKeyFunction, String primaryKeyFieldName) {
        this.asSubDbo(mappedType, tableName, foreignKeyFunction, primaryKeyFieldName, new ErasedTypes());
        return this;
    }

    public <T> TypeMap asSubDbo(Class<T> mappedType, String tableName, Function<T, String> primaryKeyFunction, String primaryKeyFieldName, ErasedTypes erasedTypes) {
        this.map.put(mappedType, TypeConfig.asSubDbo(tableName, primaryKeyFunction, primaryKeyFieldName, erasedTypes));
        return this;

    }

    public TypeMap asList(Class mappedType) {
        this.map.put(mappedType, TypeConfig.asList());
        return this;
    }

    public <Dbo> boolean containsKey(Class<Dbo> type) {
        return this.map.containsKey(type);
    }

    public List<Class> getRootDboClasses() {
        return this.map.entrySet()
                .stream()
                .filter(classTypeConfigEntry -> classTypeConfigEntry.getValue().isRootDbo())
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }
}
