package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.util.TypeMapConfig;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class SqlDataStoreTest {
    @Test
    public void insertAndGet() {
        // Arrange type map
        Map<Class, TypeMapConfig> typemap = new HashMap<>();
        TypeMapConfig.putStandardTypeConfigs(typemap);

        Map<String, Class> erasedTypesFromTestDbo = new HashMap<>();
        erasedTypesFromTestDbo.put("subTestDboList", ListItemTestDbo.class);

        typemap.put(TestDbo.class, TypeMapConfig.asDboToTable("test_table", o -> ((TestDbo) o).uuidColumn.toString(), "uuidColumn", erasedTypesFromTestDbo));


        Map<String, Class> erasedTypesFromSubTestDbo = new HashMap<>();
        erasedTypesFromSubTestDbo.put("subListItemTestDboList", SubListItemTestDbo.class);

        typemap.put(SubTestDbo.class, TypeMapConfig.asSubDbo("sub_test_table", o -> ((SubTestDbo) o).primaryKeyColumn.toString(), "primaryKeyColumn", erasedTypesFromSubTestDbo));
        typemap.put(SubSubTestDbo.class, TypeMapConfig.asSubDbo("sub_sub_test_table", o -> ((SubSubTestDbo) o).primaryKeyColumn.toString(), "primaryKeyColumn"));
        typemap.put(ListItemTestDbo.class, TypeMapConfig.asSubDbo("list_item_test_table", o -> ((ListItemTestDbo) o).primaryKeyColumn.toString(), "primaryKeyColumn"));
        typemap.put(SubListItemTestDbo.class, TypeMapConfig.asSubDbo("sub_list_item_test_table", o -> ((SubListItemTestDbo) o).primaryKeyColumn.toString(), "primaryKeyColumn"));

        // Arrange schema creator

        // Arrange SUT
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:");
        final SqlMapperFactory<TestDbo> sqlMapperFactory = new SqlMapperFactory<>(TestDbo.class, typemap, dataSource);
        sqlMapperFactory.createSchema();
        SqlDataStore<TestDbo> sqlDataStore = sqlMapperFactory.getSqlDataStore();

        // Arrange data
        // Arrange testDbo
        TestDbo testDbo = new TestDbo();
        testDbo.data = "foobar";
        testDbo.uuidColumn = UUID.fromString("6b2e6530-358f-42ef-95f6-9818c9992e2a");

        testDbo.subTestDboList = new ArrayList<>();
        testDbo.subTestDboList.add(new ListItemTestDbo());
        testDbo.subTestDboList.get(0).primaryKeyColumn = UUID.fromString("8b563c1c-6102-4627-9e4a-0f9006918fd4");

        // Arrange subTestDbo
        testDbo.subTestDbo = new SubTestDbo();
        testDbo.subTestDbo.primaryKeyColumn = UUID.fromString("580720c8-6b09-47cd-b0c1-c1f17e4bb8d0");

        testDbo.subTestDbo.subListItemTestDboList = new ArrayList<>();
        testDbo.subTestDbo.subListItemTestDboList.add(new SubListItemTestDbo());
        testDbo.subTestDbo.subListItemTestDboList.get(0).primaryKeyColumn = UUID.fromString("0000000c-6102-4627-9e4a-0f9006918fd4");

        // Arrange subSubTestDbo
        testDbo.subTestDbo.subSubTestDbo = new SubSubTestDbo();
        testDbo.subTestDbo.subSubTestDbo.primaryKeyColumn = UUID.fromString("1204445f-6fd1-4aea-9098-f8db55871a9b");

        // Act
        sqlDataStore.insert(testDbo.uuidColumn, testDbo);
        TestDbo resultTestDbo = sqlDataStore.get(testDbo.uuidColumn);

        // Assert
        Assert.assertNotNull(resultTestDbo);
        Assert.assertEquals(testDbo.uuidColumn, resultTestDbo.uuidColumn);
        Assert.assertEquals(testDbo.data, resultTestDbo.data);
    }

    public static class TestDbo {
        public UUID uuidColumn;
        public String data;
        public SubTestDbo subTestDbo;
        public List<ListItemTestDbo> subTestDboList;
    }

    public static class SubTestDbo {
        public UUID primaryKeyColumn;
        public SubSubTestDbo subSubTestDbo;
        public List<SubListItemTestDbo> subListItemTestDboList;
    }

    public static class SubSubTestDbo {
        public UUID primaryKeyColumn;
    }

    public static class ListItemTestDbo {
        public UUID primaryKeyColumn;
    }

    public static class SubListItemTestDbo {
        public UUID primaryKeyColumn;
    }
}
