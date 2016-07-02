package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.TestDbo.ListItemTestDbo;
import net.avacati.lib.sqlmapper.util.TypeMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class UpdateSqlDataStoreTest {

    private TestDbo originalTestDbo;
    private TestDbo modifiedTestDbo;
    private SqlDataStore sqlDataStore;

    @Before
    public void setUp() {
        // Arrange type map
        TypeMap typemap = TestDboFactory.createTypeMap();

        // Arrange SUT
        this.sqlDataStore = SUTFactory.createSqlDataStore(typemap, TestDbo.class);

        // Arrange data
        this.originalTestDbo = TestDboFactory.createTestDbo();

        // Insert and get back another copy from the db
        this.sqlDataStore.insert(this.originalTestDbo);
        this.modifiedTestDbo = this.sqlDataStore.get(TestDbo.class, this.originalTestDbo.uuidColumn);
    }

    private TestDbo runUpdate() {
        this.sqlDataStore.update(this.modifiedTestDbo, this.originalTestDbo);
        return this.sqlDataStore.get(TestDbo.class, this.originalTestDbo.uuidColumn);
    }

    @Test
    public void simpleFieldModifications() {
        // Modify the copy
        this.modifiedTestDbo.data += "foo";
        this.modifiedTestDbo.subTestDbo.subData += "bar";
        this.modifiedTestDbo.subTestDbo.subSubTestDbo.a += "baz";

        // Save and fetch a third copy
        TestDbo finalAssertionTestDbo = this.runUpdate();

        // Assert
        Assert.assertEquals(this.originalTestDbo.uuidColumn, finalAssertionTestDbo.uuidColumn);
        Assert.assertEquals(this.originalTestDbo.data + "foo", finalAssertionTestDbo.data);
        Assert.assertEquals(this.originalTestDbo.subTestDbo.subData + "bar", finalAssertionTestDbo.subTestDbo.subData);
        Assert.assertEquals(this.originalTestDbo.subTestDbo.subSubTestDbo.a + "baz", finalAssertionTestDbo.subTestDbo.subSubTestDbo.a);
    }

    @Test
    public void listItemFieldModification() {
        // Modify the copy
        this.modifiedTestDbo.subTestDboList.get(0).listData += "foo";

        // Save and fetch a third copy
        TestDbo finalAssertionTestDbo = this.runUpdate();

        // Assert
        Assert.assertEquals(this.originalTestDbo.uuidColumn, finalAssertionTestDbo.uuidColumn);
        Assert.assertEquals(this.originalTestDbo.subTestDboList.get(0).listData + "foo", finalAssertionTestDbo.subTestDboList.get(0).listData);
    }

    @Test
    public void replaceSubSubDbo() {
        // Modify the copy
        this.modifiedTestDbo.subTestDbo.subSubTestDbo = new TestDbo.SubSubTestDbo();
        this.modifiedTestDbo.subTestDbo.subSubTestDbo.primaryKeyColumn = UUID.randomUUID();
        this.modifiedTestDbo.subTestDbo.subSubTestDbo.a = "completely new";

        // Save and fetch a third copy
        TestDbo finalAssertionTestDbo = this.runUpdate();

        // Assert
        Assert.assertEquals(this.modifiedTestDbo.subTestDbo.subSubTestDbo.primaryKeyColumn, finalAssertionTestDbo.subTestDbo.subSubTestDbo.primaryKeyColumn);
        Assert.assertEquals(this.modifiedTestDbo.subTestDbo.subSubTestDbo.a, finalAssertionTestDbo.subTestDbo.subSubTestDbo.a);
    }

    @Test
    public void setSubSubDboToNull() {
        // Modify the copy
        this.modifiedTestDbo.subTestDbo.subSubTestDbo = null;

        // Save and fetch a third copy
        TestDbo finalAssertionTestDbo = this.runUpdate();

        // Assert
        Assert.assertNull(finalAssertionTestDbo.subTestDbo.subSubTestDbo);
    }

    @Test
    public void modifyAndAddToList() {
        // Modify the copy
        this.modifiedTestDbo.subTestDboList.get(0).listData += "fooobar"; // Modify something in the list
        this.modifiedTestDbo.subTestDboList.add(new TestDbo.ListItemTestDbo("abc")); // Add to the list

        // Save and fetch a third copy
        TestDbo finalAssertionTestDbo = this.runUpdate();

        // Assert
        List<ListItemTestDbo> list = finalAssertionTestDbo.subTestDboList;
        Assert.assertEquals(2, list.size());

        // Modified item
        Assert.assertEquals(this.modifiedTestDbo.subTestDboList.get(0).primaryKeyColumn, list.get(1).primaryKeyColumn);
        Assert.assertEquals(this.modifiedTestDbo.subTestDboList.get(0).listData, list.get(1).listData);

        // Added item
        Assert.assertEquals(this.modifiedTestDbo.subTestDboList.get(1).primaryKeyColumn, list.get(0).primaryKeyColumn);
        Assert.assertEquals("abc", list.get(0).listData);
    }
}
