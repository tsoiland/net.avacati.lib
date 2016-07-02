package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.util.TypeMapConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    public static Map<Class, TypeMapConfig> createTypeMap() {
        Map<Class, TypeMapConfig> typemap = new HashMap<>();
        TypeMapConfig.putStandardTypeConfigs(typemap);

        Map<String, Class> erasedTypesFromTestDbo = new HashMap<>();
        erasedTypesFromTestDbo.put("subTestDboList", TestDbo.ListItemTestDbo.class);

        typemap.put(TestDbo.class, TypeMapConfig.asDboToTable("test_table", o -> ((TestDbo) o).uuidColumn.toString(), "uuidColumn", erasedTypesFromTestDbo));


        Map<String, Class> erasedTypesFromSubTestDbo = new HashMap<>();
        erasedTypesFromSubTestDbo.put("subListItemTestDboList", TestDbo.SubListItemTestDbo.class);

        typemap.put(TestDbo.SubTestDbo.class, TypeMapConfig.asSubDbo(
                                                    "sub_test_table",
                                                    o -> ((TestDbo.SubTestDbo) o).primaryKeyColumn.toString(),
                                                    "primaryKeyColumn",
                                                    erasedTypesFromSubTestDbo));
        typemap.put(TestDbo.SubSubTestDbo.class, TypeMapConfig.asSubDbo(
                                                    "sub_sub_test_table",
                                                    o -> ((TestDbo.SubSubTestDbo) o).primaryKeyColumn.toString(),
                                                    "primaryKeyColumn"));
        typemap.put(TestDbo.ListItemTestDbo.class, TypeMapConfig.asSubDbo(
                                                    "list_item_test_table",
                                                    o -> ((TestDbo.ListItemTestDbo) o).primaryKeyColumn.toString(),
                                                    "primaryKeyColumn"));
        typemap.put(TestDbo.SubListItemTestDbo.class, TypeMapConfig.asSubDbo(
                                                    "sub_list_item_test_table",
                                                    o -> ((TestDbo.SubListItemTestDbo) o).primaryKeyColumn.toString(),
                                                    "primaryKeyColumn"));
        return typemap;
    }
}
