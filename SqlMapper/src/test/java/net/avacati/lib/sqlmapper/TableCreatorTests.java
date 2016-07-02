package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.schema.DirectTableCreator;
import net.avacati.lib.sqlmapper.schema.IndirectTableCreator;
import net.avacati.lib.sqlmapper.util.TypeMapConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableCreatorTests {
    @Test
    public void checkCreateTableStatements() {
        // Arrange type map
        Map<Class, TypeMapConfig> typemap = new HashMap<>();
        TypeMapConfig.putStandardTypeConfigs(typemap);

        Map<String, Class> erasedTypesFromTestDbo = new HashMap<>();
        erasedTypesFromTestDbo.put("subTestDboList", TestDbo.ListItemTestDbo.class);

        typemap.put(TestDbo.class, TypeMapConfig.asDboToTable("test_table", o -> ((TestDbo) o).uuidColumn.toString(), "uuidColumn", erasedTypesFromTestDbo));


        Map<String, Class> erasedTypesFromSubTestDbo = new HashMap<>();
        erasedTypesFromSubTestDbo.put("subListItemTestDboList", TestDbo.SubListItemTestDbo.class);

        typemap.put(TestDbo.SubTestDbo.class, TypeMapConfig.asSubDbo("sub_test_table", o -> ((TestDbo.SubTestDbo) o).primaryKeyColumn.toString(), "primaryKeyColumn", erasedTypesFromSubTestDbo));
        typemap.put(TestDbo.SubSubTestDbo.class, TypeMapConfig.asSubDbo("sub_sub_test_table", o -> ((TestDbo.SubSubTestDbo) o).primaryKeyColumn.toString(), "primaryKeyColumn"));
        typemap.put(TestDbo.ListItemTestDbo.class, TypeMapConfig.asSubDbo("list_item_test_table", o -> ((TestDbo.ListItemTestDbo) o).primaryKeyColumn.toString(), "primaryKeyColumn"));
        typemap.put(TestDbo.SubListItemTestDbo.class, TypeMapConfig.asSubDbo("sub_list_item_test_table", o -> ((TestDbo.SubListItemTestDbo) o).primaryKeyColumn
                .toString(), "primaryKeyColumn"));

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
