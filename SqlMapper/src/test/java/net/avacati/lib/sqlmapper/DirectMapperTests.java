package net.avacati.lib.sqlmapper;

import net.avacati.lib.sqlmapper.DirectMapperTests.DirectTestDbo.TestEnum;
import net.avacati.lib.sqlmapper.insert.DirectInserter;
import net.avacati.lib.sqlmapper.util.DbField;
import net.avacati.lib.sqlmapper.util.TypeMap;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

public class DirectMapperTests {
    @Test
    public void AllTypesDbo() {
        // Setup type mapping
        TypeMap map = new TypeMap();
        map.putStandardTypeConfigs();
        map.asDboToTable(DirectTestDbo.class, "test_table", null, null);
        map.directToString(TestEnum.class, TestEnum::valueOf);
        map.asSubDbo(DirectSubTestDbo.class, "sub_test_table", o -> ((DirectSubTestDbo) o).primaryKeyColumn.toString(), "not used");

        // Arrange SUT
        DirectInserter directInserter = new DirectInserter(map);

        // Arrange test values
        DirectTestDbo directTestDbo = new DirectTestDbo();
        directTestDbo.stringColumn = "testing string";
        directTestDbo.uuidColumn = UUID.fromString("6b2e6530-358f-42ef-95f6-9818c9992e2a");
        directTestDbo.intColumn = 47;
        directTestDbo.instantColumn = Instant.ofEpochMilli(123456789);
        directTestDbo.enumColumn = TestEnum.TestB;

        directTestDbo.directSubTestDbo = new DirectSubTestDbo();
        directTestDbo.directSubTestDbo.primaryKeyColumn = UUID.fromString("580720c8-6b09-47cd-b0c1-c1f17e4bb8d0");

        // Act
        DbField extraFields = new DbField();
        extraFields.columnName = "extraColumnName";
        extraFields.value = "extraValue";
        String sql = directInserter.createInsertSqlForDirectlyMappableColumns(directTestDbo, extraFields);

        // Assert
        String expected =
            "INSERT INTO test_table(\n" +
                    "\tstringColumn,\n" +
                    "\tuuidColumn,\n" +
                    "\tintColumn,\n" +
                    "\tinstantColumn,\n" +
                    "\tenumColumn,\n" +
                    "\tdirectSubTestDbo,\n" +
                    "\textraColumnName)\n" +
                    "VALUES (\n" +
                    "\t'testing string',\n" +
                    "\t'6b2e6530-358f-42ef-95f6-9818c9992e2a',\n" +
                    "\t'47',\n" +
                    "\t'123456789',\n" +
                    "\t'TestB',\n" +
                    "\t'580720c8-6b09-47cd-b0c1-c1f17e4bb8d0',\n" +
                    "\t'extraValue')\n";
        Assert.assertEquals(expected, sql);
    }

    public static class DirectTestDbo {
        public String stringColumn;
        public UUID uuidColumn;
        public int intColumn;
        public Instant instantColumn;
        public TestEnum enumColumn;
        public DirectSubTestDbo directSubTestDbo;

        public enum TestEnum {
            TestA, TestB
        }
    }

    public static class DirectSubTestDbo {
        public UUID primaryKeyColumn;
    }
}
