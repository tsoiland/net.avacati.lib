package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.insert.DirectInserter;
import net.avacati.lib.sqlmapper.insert.IndirectInserter;
import net.avacati.lib.sqlmapper.util.TypeMapConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class IndirectMapperTests {
    @Test
    public void AllTypesDbo() {
        // Setup type mapping
        Map<Class, TypeMapConfig> map = new HashMap<>();
        map.put(UUID.class, TypeMapConfig.directToString(UUID::fromString));

        map.put(List.class, TypeMapConfig.asList());

        map.put(TestDbo.class, TypeMapConfig.asDboToTable("test_table", o -> ((TestDbo) o).uuidColumn.toString(), "uuidColumn"));
        map.put(SubTestDbo.class, TypeMapConfig.asSubDbo("sub_test_table", o -> ((SubTestDbo) o).primaryKeyColumn.toString(), "not used"));
        map.put(SubSubTestDbo.class, TypeMapConfig.asSubDbo("sub_sub_test_table", o -> ((SubSubTestDbo) o).primaryKeyColumn.toString(), "not used"));
        map.put(ListItemTestDbo.class, TypeMapConfig.asSubDbo("list_item_test_table", o -> ((ListItemTestDbo) o).primaryKeyColumn.toString(), "not used"));

        // Arrange SUT
        IndirectInserter directMapper = new IndirectInserter(map, new DirectInserter(map));

        // Arrange test values
        TestDbo testDbo = new TestDbo();
        testDbo.uuidColumn = UUID.fromString("6b2e6530-358f-42ef-95f6-9818c9992e2a");

        testDbo.subTestDboList = new ArrayList<>();
        testDbo.subTestDboList.add(new ListItemTestDbo());
        testDbo.subTestDboList.get(0).primaryKeyColumn = UUID.fromString("8b563c1c-6102-4627-9e4a-0f9006918fd4");

        testDbo.subTestDbo = new SubTestDbo();
        testDbo.subTestDbo.primaryKeyColumn = UUID.fromString("580720c8-6b09-47cd-b0c1-c1f17e4bb8d0");

        testDbo.subTestDbo.subSubTestDbo = new SubSubTestDbo();
        testDbo.subTestDbo.subSubTestDbo.primaryKeyColumn = UUID.fromString("1204445f-6fd1-4aea-9098-f8db55871a9b");

        // Act
        List<String> sql = directMapper.createInsertSqlsForObjectTree(testDbo);

        // Assert
        Assert.assertEquals(4, sql.size());
        String expected =
                "INSERT INTO test_table(\n" +
                        "\tuuidColumn,\n" +
                        "\tsubTestDbo)\n" +
                        "VALUES (\n" +
                        "\t'6b2e6530-358f-42ef-95f6-9818c9992e2a',\n" +
                        "\t'580720c8-6b09-47cd-b0c1-c1f17e4bb8d0')\n";
        Assert.assertEquals(expected, sql.get(0));

        String expected2 =
                "INSERT INTO sub_test_table(\n" +
                        "\tprimaryKeyColumn,\n" +
                        "\tsubSubTestDbo)\n" +
                        "VALUES (\n" +
                        "\t'580720c8-6b09-47cd-b0c1-c1f17e4bb8d0',\n" +
                        "\t'1204445f-6fd1-4aea-9098-f8db55871a9b')\n";
        Assert.assertEquals(expected2, sql.get(1));

        String expected3 =
                "INSERT INTO sub_sub_test_table(\n" +
                        "\tprimaryKeyColumn)\n" +
                        "VALUES (\n" +
                        "\t'1204445f-6fd1-4aea-9098-f8db55871a9b')\n";
        Assert.assertEquals(expected3, sql.get(2));

        String expected4 =
                "INSERT INTO list_item_test_table(\n" +
                        "\tprimaryKeyColumn,\n" +
                        "\tTestDbo_subTestDboList)\n" +
                        "VALUES (\n" +
                        "\t'8b563c1c-6102-4627-9e4a-0f9006918fd4',\n" +
                        "\t'6b2e6530-358f-42ef-95f6-9818c9992e2a')\n";
        Assert.assertEquals(expected4, sql.get(3));
    }

    public static class TestDbo {
        public UUID uuidColumn;
        public SubTestDbo subTestDbo;
        public List<ListItemTestDbo> subTestDboList;
    }

    public static class SubTestDbo {
        public UUID primaryKeyColumn;
        public SubSubTestDbo subSubTestDbo;
    }

    public static class SubSubTestDbo {
        public UUID primaryKeyColumn;
    }

    public static class ListItemTestDbo {
        public UUID primaryKeyColumn;
    }
}
