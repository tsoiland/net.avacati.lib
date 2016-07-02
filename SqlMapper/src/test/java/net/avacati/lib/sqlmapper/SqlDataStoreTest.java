package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.util.TypeMap;
import org.junit.Assert;
import org.junit.Test;

public class SqlDataStoreTest {
    @Test
    public void insertAndGet() {
        // Arrange type map
        TypeMap typeMap = TestDboFactory.createTypeMap();

        // Arrange SUT
        SqlDataStore sqlDataStore = SUTFactory.createSqlDataStore(typeMap, TestDbo.class);

        // Arrange data
        // Arrange testDbo
        TestDbo testDbo = TestDboFactory.createTestDbo();

        // Act
        sqlDataStore.insert(testDbo);
        TestDbo resultTestDbo = sqlDataStore.get(TestDbo.class, testDbo.uuidColumn);

        // Assert
        Assert.assertNotNull(resultTestDbo);
        Assert.assertEquals(testDbo.uuidColumn, resultTestDbo.uuidColumn);
        Assert.assertEquals(testDbo.data, resultTestDbo.data);
    }
}
