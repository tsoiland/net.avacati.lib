package net.avacati.lib.sqlmapper;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public class TypeMapConfig {
    private boolean shouldMapDirectlyToColumn;
    private boolean shouldRecurse;

    private Function<Object, String> mappingFunction;

    private String directRowTableName;
    private boolean shouldTreatAsList;
    private Function<Object, String> primaryKeyMapFunction;
    private Function<String, Object> reverseFunction;
    private String primaryKeyFieldName;
    private Map<String,Class> erasedTypes;
    private boolean shouldEqualDirectly;
    private boolean shouldEqualUsingPK;

    private TypeMapConfig() {

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


    public static TypeMapConfig directToString(Function<String, Object> reverseFunction) {
        return direct(Object::toString, reverseFunction);
    }

    public static TypeMapConfig direct(Function<Object, String> mappingFunction, Function<String, Object> reverseFunction) {
        TypeMapConfig typeMapConfig = new TypeMapConfig();
        typeMapConfig.mappingFunction = mappingFunction;
        typeMapConfig.reverseFunction = reverseFunction;
        typeMapConfig.shouldMapDirectlyToColumn = true;
        typeMapConfig.shouldRecurse = false;
        typeMapConfig.shouldTreatAsList = false;
        typeMapConfig.shouldEqualDirectly = true;
        typeMapConfig.shouldEqualUsingPK = false;
        return typeMapConfig;
    }

    public static TypeMapConfig asDboToTable(String tableName, Function<Object, String> primayKeyMapFunction, String primaryKeyFieldName) {
        return asDboToTable(tableName, primayKeyMapFunction, primaryKeyFieldName, new HashMap<>());
    }

    public static TypeMapConfig asDboToTable(String tableName, Function<Object, String> primayKeyMapFunction, String primaryKeyFieldName, Map<String,Class> erasedTypes) {
        TypeMapConfig typeMapConfig = new TypeMapConfig();
        typeMapConfig.primaryKeyMapFunction = primayKeyMapFunction;
        typeMapConfig.directRowTableName = tableName;
        typeMapConfig.primaryKeyFieldName = primaryKeyFieldName;
        typeMapConfig.shouldMapDirectlyToColumn = false;
        typeMapConfig.shouldRecurse = false;
        typeMapConfig.shouldTreatAsList = false;
        typeMapConfig.erasedTypes = erasedTypes;
        typeMapConfig.shouldEqualDirectly = false;
        typeMapConfig.shouldEqualUsingPK = true;
        return typeMapConfig;
    }

    public boolean shouldMapDirectlyToColumn() {
        return this.shouldMapDirectlyToColumn;
    }

    public static TypeMapConfig asSubDbo(String tableName, Function<Object, String> foreignKeyFunction, String primaryKeyFieldName) {
        return asSubDbo(tableName, foreignKeyFunction, primaryKeyFieldName, new HashMap<>());
    }

    public static TypeMapConfig asSubDbo(String tableName, Function<Object, String> primaryKeyFunction, String primaryKeyFieldName, Map<String,Class> erasedTypes) {
        TypeMapConfig typeMapConfig = new TypeMapConfig();
        typeMapConfig.directRowTableName = tableName;
        typeMapConfig.mappingFunction = primaryKeyFunction;
        typeMapConfig.primaryKeyMapFunction = primaryKeyFunction;
        typeMapConfig.primaryKeyFieldName = primaryKeyFieldName;
        typeMapConfig.shouldMapDirectlyToColumn = true;
        typeMapConfig.shouldRecurse = true;
        typeMapConfig.shouldTreatAsList = false;
        typeMapConfig.erasedTypes = erasedTypes;
        typeMapConfig.shouldEqualDirectly = false;
        typeMapConfig.shouldEqualUsingPK = true;
        return typeMapConfig;
    }

    public static TypeMapConfig asList() {
        TypeMapConfig typeMapConfig = new TypeMapConfig();
        typeMapConfig.mappingFunction = o -> "foobar";
        typeMapConfig.directRowTableName = null;
        typeMapConfig.shouldMapDirectlyToColumn = false;
        typeMapConfig.shouldRecurse = false;
        typeMapConfig.shouldTreatAsList = true;
        return typeMapConfig;
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

    public static void putStandardTypeConfigs(Map<Class, TypeMapConfig> map) {
        map.put(String.class,   TypeMapConfig.directToString(s -> s));
        map.put(UUID.class,     TypeMapConfig.directToString(UUID::fromString));
        map.put(int.class,      TypeMapConfig.direct(o -> Integer.toString((int) o), Integer::parseInt));
        map.put(Instant.class,  TypeMapConfig.direct(o -> Long.toString(((Instant) o).toEpochMilli()), s -> Instant.ofEpochMilli(Long.valueOf(s))));
        map.put(List.class,     TypeMapConfig.asList());
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
}
