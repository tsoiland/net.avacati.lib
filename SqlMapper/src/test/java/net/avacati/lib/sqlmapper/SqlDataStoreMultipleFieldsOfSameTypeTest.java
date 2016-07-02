package net.avacati.lib.sqlmapper;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class SqlDataStoreMultipleFieldsOfSameTypeTest {
    @Test
    public void insertAndGet() {
        // Arrange type mapping
        Map<Class, TypeMapConfig> typemap = new HashMap<>();

        // - standards
        TypeMapConfig.putStandardTypeConfigs(typemap);

        // - TestDbo
        Map<String, Class> erasedTypesFromTestDbo = new HashMap<>();
        erasedTypesFromTestDbo.put("listSubZ", SubTestDbo.class);
        erasedTypesFromTestDbo.put("listSubW", SubTestDbo.class);
        typemap.put(TestDbo.class, TypeMapConfig.asDboToTable("test_table", o -> ((TestDbo) o).uuidColumn.toString(), "uuidColumn", erasedTypesFromTestDbo));

        // - SubTestDbo
        typemap.put(SubTestDbo.class, TypeMapConfig.asSubDbo("sub_test_table", o -> ((SubTestDbo) o).primaryKeyColumn.toString(), "primaryKeyColumn"));

        // Arrange db schema
//        SqlDoerH2 sqlDoerH2 = SqlDoerH2.create();
//        sqlDoerH2.doSql("CREATE TABLE test_table (uuidColumn varchar(36), data varchar(max), singleSubX varchar(36), singleSubY varchar(36))");
//        sqlDoerH2.doSql("CREATE TABLE sub_test_table (primaryKeyColumn varchar(36), data varchar(max), testDbo_listsubz varchar(36), testDbo_listsubw varchar(36))");

        // Arrange schema creator
        SqlDoerH2 sqlDoerH2 = SqlDoerH2.create();
        final TableCreator tableCreator = new TableCreator(new IndirectTableCreator(typemap, new DirectTableCreator(typemap)), sqlDoerH2);
        tableCreator.createTableFor(TestDbo.class);

        // Arrange SUT
        IndirectMapper indirectMapper = new IndirectMapper(typemap, new DirectMapper(typemap));
        SqlDataStore<TestDbo> sqlDataStore = new SqlDataStore<>(
                indirectMapper,
                sqlDoerH2,
                new DirectSelecter(typemap, sqlDoerH2),
                TestDbo.class,
                new IndirectUpdater(typemap, new DirectUpdater(typemap), indirectMapper));

        // Arrange data
        // Arrange testDbo
        TestDbo testDbo = new TestDbo();
        testDbo.uuidColumn = UUID.fromString("6b2e6530-358f-42ef-95f6-9818c9992e2a");
        testDbo.data = "foobar";
        testDbo.listSubZ = new ArrayList<>();
        testDbo.listSubW= new ArrayList<>();

        // Arrange X
        final SubTestDbo x = new SubTestDbo();
        x.primaryKeyColumn = UUID.fromString("8b563c1c-6102-4627-9e4a-0f9006918fd4");
        x.data = "X data";
        testDbo.singleSubX = x;

        // Arrange Y
        final SubTestDbo y = new SubTestDbo();
        y.primaryKeyColumn = UUID.fromString("8b563c1c-0000-4627-9e4a-0f9006918fd4");
        y.data = "Y data";
        testDbo.singleSubY = y;

        // Arrange Z1
        final SubTestDbo z1 = new SubTestDbo();
        z1.primaryKeyColumn = UUID.fromString("8b563c1c-6102-0000-9e4a-0f9006918fd4");
        z1.data = "Z1 data";
        testDbo.listSubZ.add(z1);

        // Arrange Z2
        final SubTestDbo z2 = new SubTestDbo();
        z2.primaryKeyColumn = UUID.fromString("8b563c1c-6102-1111-9e4a-0f9006918fd4");
        z2.data = "Z2 data";
        testDbo.listSubZ.add(z2);

        // Arrange W1
        final SubTestDbo w1 = new SubTestDbo();
        w1.primaryKeyColumn = UUID.fromString("8b563c1c-6102-0000-0000-0f9006918fd4");
        w1.data = "W1 data";
        testDbo.listSubW.add(w1);

        // Arrange W2
        final SubTestDbo w2 = new SubTestDbo();
        w2.primaryKeyColumn = UUID.fromString("8b563c1c-6102-1111-1111-0f9006918fd4");
        w2.data = "W2 data";
        testDbo.listSubW.add(w2);

        // Act
        sqlDataStore.insert(testDbo.uuidColumn, testDbo);
        TestDbo resultTestDbo = sqlDataStore.get(testDbo.uuidColumn);

        // Assert
        Assert.assertNotNull(resultTestDbo);
        Assert.assertEquals(testDbo.uuidColumn, resultTestDbo.uuidColumn);
        Assert.assertEquals(testDbo.data, resultTestDbo.data);

        // Assert X
        final SubTestDbo singleSubX = resultTestDbo.singleSubX;
        Assert.assertNotNull(singleSubX);
        Assert.assertEquals(x.primaryKeyColumn, singleSubX.primaryKeyColumn);
        Assert.assertEquals(x.data, singleSubX.data);

        // Assert X
        final SubTestDbo singleSubY = resultTestDbo.singleSubY;
        Assert.assertNotNull(singleSubY);
        Assert.assertEquals(y.primaryKeyColumn, singleSubY.primaryKeyColumn);
        Assert.assertEquals(y.data, singleSubY.data);

        // Assert Z1
        final SubTestDbo singleSubZ1 = resultTestDbo.listSubZ.get(0);
        Assert.assertNotNull(singleSubZ1);
        Assert.assertEquals(z1.primaryKeyColumn, singleSubZ1.primaryKeyColumn);
        Assert.assertEquals(z1.data, singleSubZ1.data);

        // Assert Z2
        final SubTestDbo singleSubZ2 = resultTestDbo.listSubZ.get(1);
        Assert.assertNotNull(singleSubZ2);
        Assert.assertEquals(z2.primaryKeyColumn, singleSubZ2.primaryKeyColumn);
        Assert.assertEquals(z2.data, singleSubZ2.data);

        // Assert W1
        final SubTestDbo singleSubW1 = resultTestDbo.listSubW.get(0);
        Assert.assertNotNull(singleSubW1);
        Assert.assertEquals(w1.primaryKeyColumn, singleSubW1.primaryKeyColumn);
        Assert.assertEquals(w1.data, singleSubW1.data);

        // Assert W2
        final SubTestDbo singleSubW2 = resultTestDbo.listSubW.get(1);
        Assert.assertNotNull(singleSubW2);
        Assert.assertEquals(w2.primaryKeyColumn, singleSubW2.primaryKeyColumn);
        Assert.assertEquals(w2.data, singleSubW2.data);
    }

    public static class TestDbo {
        public UUID uuidColumn;
        public String data;

        public SubTestDbo singleSubX;
        public SubTestDbo singleSubY;
        public List<SubTestDbo> listSubZ;
        public List<SubTestDbo> listSubW;
    }

    public static class SubTestDbo {
        public UUID primaryKeyColumn;
        public String data;
    }
}