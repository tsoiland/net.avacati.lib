package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.util.TypeMap;
import net.avacati.lib.sqlmapper.util.TypeConfig.ErasedTypes;

import java.util.ArrayList;
import java.util.UUID;

public class TestDboFactory {
    public static TestDbo createTestDbo() {
        TestDbo testDbo = new TestDbo();
        testDbo.data = "1";
        testDbo.uuidColumn = UUID.fromString("6b2e6530-358f-42ef-95f6-9818c9992e2a");

        testDbo.subTestDboList = new ArrayList<>();
        testDbo.subTestDboList.add(new TestDbo.ListItemTestDbo());
        testDbo.subTestDboList.get(0).primaryKeyColumn = UUID.fromString("8b563c1c-6102-4627-9e4a-0f9006918fd4");
        testDbo.subTestDboList.get(0).listData = "yr";

        // Arrange subTestDbo
        testDbo.subTestDbo = new TestDbo.SubTestDbo();
        testDbo.subTestDbo.subData = "2";
        testDbo.subTestDbo.primaryKeyColumn = UUID.fromString("580720c8-6b09-47cd-b0c1-c1f17e4bb8d0");

        testDbo.subTestDbo.subListItemTestDboList = new ArrayList<>();
        testDbo.subTestDbo.subListItemTestDboList.add(new TestDbo.SubListItemTestDbo());
        testDbo.subTestDbo.subListItemTestDboList.get(0).primaryKeyColumn = UUID.fromString("0000000c-6102-4627-9e4a-0f9006918fd4");

        // Arrange subSubTestDbo
        testDbo.subTestDbo.subSubTestDbo = new TestDbo.SubSubTestDbo();
        testDbo.subTestDbo.subSubTestDbo.a = "3";
        testDbo.subTestDbo.subSubTestDbo.primaryKeyColumn = UUID.fromString("1204445f-6fd1-4aea-9098-f8db55871a9b");
        return testDbo;
    }

    public static TypeMap createTypeMap() {
        TypeMap typeMap = new TypeMap();
        typeMap.putStandardTypeConfigs();

        typeMap.asDboToTable(
                TestDbo.class,
                "test_table",
                o -> o.uuidColumn.toString(),
                "uuidColumn",
                new ErasedTypes().put("subTestDboList", TestDbo.ListItemTestDbo.class));

        typeMap.asSubDbo(
                TestDbo.SubTestDbo.class,
                "sub_test_table",
                o -> o.primaryKeyColumn.toString(),
                "primaryKeyColumn",
                new ErasedTypes().put("subListItemTestDboList", TestDbo.SubListItemTestDbo.class));
        typeMap.asSubDbo(
                TestDbo.SubSubTestDbo.class,
                "sub_sub_test_table",
                o -> o.primaryKeyColumn.toString(),
                "primaryKeyColumn");
        typeMap.asSubDbo(
                TestDbo.ListItemTestDbo.class,
                "list_item_test_table",
                o -> o.primaryKeyColumn.toString(),
                "primaryKeyColumn");
        typeMap.asSubDbo(
                TestDbo.SubListItemTestDbo.class,
                "sub_list_item_test_table",
                o -> o.primaryKeyColumn.toString(),
                "primaryKeyColumn");
        return typeMap;
    }
}
