package net.avacati.lib.serializingdatastore;

import net.avacati.lib.aggregaterepository.DataStore;
import net.avacati.lib.serializingdatastore.domain.*;
import net.avacati.lib.serializingdatastore.migration.MigratingSerializingDataStoreFactory;
import net.avacati.lib.serializingdatastore.migration.Migrator;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.UUID;

public class MigrationTests {
    private DataStore<TestDbo> serializingDataStore;
    private Connection connection;
    private UUID uuid = UUID.fromString("0f050434-cee2-4d6e-8241-9ec4d2984207");

    @Before
    public void setUp() throws SQLException {
        // Setup H2
        connection = createConnection();

        // Setup domain migrations
        final Migrator migrator = new TestDboMigrations();

        // Setup SUT
        this.serializingDataStore = MigratingSerializingDataStoreFactory.createDataStore(connection, "testtable", migrator);
    }

    /**
     * Create a simple H2 inmem connection.
     */
    private Connection createConnection() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:");
        return dataSource.getConnection();
    }

    /**
     * Because {@link SerializingDataStore} is typed, we can't save the V1 object we've created, even if we technically have hacked the
     * classname to be correct. {@link SerializingDataStore#insert(UUID, Object)} is very simple, so we recreate it here and just insert
     * into the same db connection.
     */
    private void insertWithoutTypeCheck(UUID uuid, Object o) {
        final byte[] bytes = new PlainSerializationProvider().serializeObject(o);
        new SqlByteDataStore("testtable", this.connection).insert(uuid, bytes);
    }

    @Test
    public void insertV1GetLatest() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        // Arrange test dbo
        ClassNameSubstitutingClassLoader classNameSubstitutingClassLoader = new ClassNameSubstitutingClassLoader();
        classNameSubstitutingClassLoader.mapClassNameFromTo(TestDboV1.class.getName(), TestDbo.class.getName());
        final Class<?> aClass = classNameSubstitutingClassLoader.loadClass(TestDboV1.class.getName());
        final Object testDboV1 = aClass.newInstance();

        // Create TestDboV1
        testDboV1.getClass().getField("id").set(testDboV1, uuid);
        testDboV1.getClass().getField("foobar").set(testDboV1, "foobar value");

        // Insert TestDboV1,
        insertWithoutTypeCheck(uuid, testDboV1);

        // Get it as TestDbo. This is in the new version of the software, the datastore is now supposed to migrate before returning to us.
        final TestDbo testDbo = this.serializingDataStore.get(uuid);

        // Assert
        assertTTT(testDbo);
    }

    private void assertTTT(TestDbo testDbo) {
        Assert.assertEquals(uuid.toString(), testDbo.id);
        Assert.assertEquals("foobar value", testDbo.foobar);
        Assert.assertEquals(2, testDbo.subTestDbos.size());

        final Iterator<SubTestDbo> iterator = testDbo.subTestDbos.iterator();

        final SubTestDbo sub1 = iterator.next();
        Assert.assertEquals(40, sub1.id);
        Assert.assertEquals("fabricated substringsub1", sub1.substring);
        Assert.assertEquals("new value", sub1.newField);

        final SubTestDbo sub2 = iterator.next();
        Assert.assertEquals(40, sub2.id);
        Assert.assertEquals("fabricated substringsub2", sub2.substring);
        Assert.assertEquals("new value", sub2.newField);
    }


    @Test
    public void insertV3GetLatest() throws NoSuchFieldException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        // Set up classloader
        ClassNameSubstitutingClassLoader classNameSubstitutingClassLoader = new ClassNameSubstitutingClassLoader();

        classNameSubstitutingClassLoader.mapClassNameFromTo(TestDboV3.class.getName(), TestDbo.class.getName());
        classNameSubstitutingClassLoader.mapClassNameFromTo(SubTestDboV1.class.getName(), SubTestDbo.class.getName());

        final Class<?> testDboClass = classNameSubstitutingClassLoader.loadClass(TestDboV3.class.getName());
        final Class<?> subDboClass = classNameSubstitutingClassLoader.loadClass(SubTestDboV1.class.getName());

        // Create TestDbo
        final Object testDboV3 = testDboClass.newInstance();
        testDboV3.getClass().getField("id").set(testDboV3, uuid.toString());
        testDboV3.getClass().getField("foobar").set(testDboV3, "foobar value");

        // Create SubTestDbo
        Object subTestDbo = subDboClass.newInstance();
        subTestDbo.getClass().getField("id").set(subTestDbo, uuid);
        subTestDbo.getClass().getField("substring").set(subTestDbo, "fabricated substring");
        testDboV3.getClass().getField("subTestDbo").set(testDboV3, subTestDbo);

        // Insert TestDboV1,
        insertWithoutTypeCheck(uuid, testDboV3);

        // Get it as TestDbo. This is in the new version of the software, the datastore is now supposed to migrate before returning to us.
        final TestDbo testDbo = this.serializingDataStore.get(uuid);

        // Assert
        assertTTT(testDbo);
    }
}
