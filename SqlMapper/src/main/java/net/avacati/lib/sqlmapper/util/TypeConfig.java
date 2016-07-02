package net.avacati.lib.sqlmapper.util;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public class TypeConfig {
    private boolean shouldMapDirectlyToColumn;
    private boolean shouldRecurse;

    private Function<Object, String> mappingFunction;

    private String directRowTableName;
    private boolean shouldTreatAsList;
    private Function<Object, String> primaryKeyMapFunction;
    private Function<String, Object> reverseFunction;
    private String primaryKeyFieldName;
    private ErasedTypes erasedTypes;
    private boolean shouldEqualDirectly;
    private boolean shouldEqualUsingPK;

    private TypeConfig() {

    }

    public String map(Object fieldValue) {
        if (fieldValue == null) {
            return null;
        }

        return this.mappingFunction.apply(fieldValue);
    }

    public String getTableNameForDbo() {
        return this.directRowTableName;
    }


    public static TypeConfig directToString(Function<String, Object> reverseFunction) {
        return direct(Object::toString, reverseFunction);
    }

    public static <T> TypeConfig direct(Function<T, String> mappingFunction, Function<String, T> reverseFunction) {
        TypeConfig typeConfig = new TypeConfig();
        typeConfig.mappingFunction = (Function<Object, String>) mappingFunction;
        typeConfig.reverseFunction = (Function<String, Object>) reverseFunction;
        typeConfig.shouldMapDirectlyToColumn = true;
        typeConfig.shouldRecurse = false;
        typeConfig.shouldTreatAsList = false;
        typeConfig.shouldEqualDirectly = true;
        typeConfig.shouldEqualUsingPK = false;
        return typeConfig;
    }

    public static TypeConfig asDboToTable(String tableName, Function<Object, String> primayKeyMapFunction, String primaryKeyFieldName) {
        return asDboToTable(tableName, primayKeyMapFunction, primaryKeyFieldName, new ErasedTypes());
    }

    public static TypeConfig asDboToTable(String tableName, Function<?, String> primayKeyMapFunction, String primaryKeyFieldName, ErasedTypes erasedTypes) {
        TypeConfig typeConfig = new TypeConfig();
        typeConfig.primaryKeyMapFunction = (Function<Object, String>) primayKeyMapFunction;
        typeConfig.directRowTableName = tableName;
        typeConfig.primaryKeyFieldName = primaryKeyFieldName;
        typeConfig.shouldMapDirectlyToColumn = false;
        typeConfig.shouldRecurse = false;
        typeConfig.shouldTreatAsList = false;
        typeConfig.erasedTypes = erasedTypes;
        typeConfig.shouldEqualDirectly = false;
        typeConfig.shouldEqualUsingPK = true;
        return typeConfig;
    }

    public boolean shouldMapDirectlyToColumn() {
        return this.shouldMapDirectlyToColumn;
    }

    public static TypeConfig asSubDbo(String tableName, Function<Object, String> foreignKeyFunction, String primaryKeyFieldName) {
        return asSubDbo(tableName, foreignKeyFunction, primaryKeyFieldName, new ErasedTypes());
    }

    public static TypeConfig asSubDbo(String tableName, Function<?, String> primaryKeyFunction, String primaryKeyFieldName, ErasedTypes erasedTypes) {
        TypeConfig typeConfig = new TypeConfig();
        typeConfig.directRowTableName = tableName;
        typeConfig.mappingFunction = (Function<Object, String>) primaryKeyFunction;
        typeConfig.primaryKeyMapFunction = (Function<Object, String>) primaryKeyFunction;
        typeConfig.primaryKeyFieldName = primaryKeyFieldName;
        typeConfig.shouldMapDirectlyToColumn = true;
        typeConfig.shouldRecurse = true;
        typeConfig.shouldTreatAsList = false;
        typeConfig.erasedTypes = erasedTypes;
        typeConfig.shouldEqualDirectly = false;
        typeConfig.shouldEqualUsingPK = true;
        return typeConfig;
    }

    public static TypeConfig asList() {
        TypeConfig typeConfig = new TypeConfig();
        typeConfig.mappingFunction = o -> "foobar";
        typeConfig.directRowTableName = null;
        typeConfig.shouldMapDirectlyToColumn = false;
        typeConfig.shouldRecurse = false;
        typeConfig.shouldTreatAsList = true;
        return typeConfig;
    }

    public boolean isDboThatMapsToItsOwnTable() {
        return this.shouldRecurse;
    }

    public boolean shouldTreatAsList() {
        return this.shouldTreatAsList;
    }

    public String getPrimaryKey(Object dbo) {
        return this.primaryKeyMapFunction.apply(dbo);
    }

    public Object reverseMap(String value) {
        return this.reverseFunction.apply(value);
    }

    public String getPrimaryKeyFieldName() {
        return this.primaryKeyFieldName;
    }

    public static void putStandardTypeConfigs(Map<Class, TypeConfig> map) {
        map.put(String.class,   TypeConfig.directToString(s -> s));
        map.put(UUID.class,     TypeConfig.directToString(UUID::fromString));
        map.put(int.class,      TypeConfig.direct(o -> Integer.toString((int) o), Integer::parseInt));
        map.put(Instant.class,  TypeConfig.direct(o -> Long.toString(((Instant) o).toEpochMilli()), s -> Instant.ofEpochMilli(Long.valueOf(s))));
        map.put(List.class,     TypeConfig.asList());
    }

    public Class getErasedType(String name) {
        return this.erasedTypes.get(name);
    }

    public boolean shouldEqualDirectly() {
        return this.shouldEqualDirectly;
    }

    public boolean shouldEqualUsingPK() {
        return this.shouldEqualUsingPK;
    }

    public static class ErasedTypes {
        private final Map<String,Class> erasedTypes = new HashMap<>();

        public ErasedTypes put(String fieldName, Class<?> erasedType) {
            this.erasedTypes.put(fieldName, erasedType);
            return this;
        }

        public Class<?> get(String fieldName) {
            return this.erasedTypes.get(fieldName);
        }
    }
}
