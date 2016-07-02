package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.schema.DirectTableCreator;
import net.avacati.lib.sqlmapper.schema.IndirectTableCreator;
import net.avacati.lib.sqlmapper.util.TypeMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TableCreatorTests {
    @Test
    public void checkCreateTableStatements() {
        // Arrange type map
        TypeMap typemap = TestDboFactory.createTypeMap();

        // Act
        final List<String> createTableScript = new IndirectTableCreator(typemap, new DirectTableCreator(typemap)).createCreateTableSqlsForClass(TestDbo.class);

        // Assert
        Assert.assertEquals(5, createTableScript.size());

        Assert.assertEquals("CREATE TABLE list_item_test_table(\n" +
                "\tlistData varchar(max),\n" +
                "\tTestDbo_subTestDboList varchar(max),\n" +
                "\tprimaryKeyColumn varchar(max))\n", createTableScript.get(1));

        Assert.assertEquals("CREATE TABLE sub_list_item_test_table(\n" +
                "\tlistData varchar(max),\n" +
                "\tSubTestDbo_subListItemTestDboList varchar(max),\n" +
                "\tprimaryKeyColumn varchar(max))\n", createTableScript.get(2));

        Assert.assertEquals("CREATE TABLE sub_sub_test_table(\n" +
                "\ta varchar(max),\n" +
                "\tprimaryKeyColumn varchar(max))\n", createTableScript.get(3));

        Assert.assertEquals("CREATE TABLE sub_test_table(\n" +
                "\tsubData varchar(max),\n" +
                "\tprimaryKeyColumn varchar(max),\n" +
                "\tsubSubTestDbo varchar(max))\n", createTableScript.get(4));

        Assert.assertEquals("CREATE TABLE test_table(\n" +
                "\tuuidColumn varchar(max),\n" +
                "\tdata varchar(max),\n" +
                "\tsubTestDbo varchar(max))\n", createTableScript.get(0));
    }
}
