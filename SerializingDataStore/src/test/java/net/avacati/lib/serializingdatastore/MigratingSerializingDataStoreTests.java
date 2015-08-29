package net.avacati.lib.serializingdatastore;

import net.avacati.lib.serializingdatastore.domainmigrations.TestDbo;
import net.avacati.lib.serializingdatastore.domainmigrations.TestDboV1;
import net.avacati.lib.serializingdatastore.domainmigrations.TestDboMigrations;
import net.avacati.lib.serializingdatastore.migration.MigratingSerializingDataStoreFactory;
import net.avacati.lib.serializingdatastore.migration.Migrator;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class MigratingSerializingDataStoreTests {
    private DataStore<TestDbo> serializingDataStore;
    private UUID uuid = UUID.fromString("0f050434-cee2-4d6e-8241-9ec4d2984207");
    private Connection connection;

    @Before
    public void setUp() throws SQLException {
        // Setup H2
        connection = createConnection();

        // Setup domain migrations
        final Migrator migrator = new TestDboMigrations();

        // Setup SUT
        this.serializingDataStore = MigratingSerializingDataStoreFactory.createDataStore(connection, "testtable", migrator, TestDbo.class);
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

    /**
     * Create a simple H2 inmem connection.
     */
    private Connection createConnection() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:");
        return dataSource.getConnection();
    }

    @Test
    public void insertAndGet() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        // Load TestDboV1 through renaming classloader, so that it's name will be TestDbo.
        // That way it will look like it was serialized back when TestDboV1 actually _was_ TestDbo.
        final ClassNameSubstitutingClassLoader classNameSubstitutingClassLoader = new ClassNameSubstitutingClassLoader();
        classNameSubstitutingClassLoader.mapClassNameFromTo(TestDboV1.class.getName(), TestDbo.class.getName());
        final Class<?> aClass = classNameSubstitutingClassLoader.loadClass(TestDboV1.class.getName());

        // Create TestDboV1
        final Object o = aClass.newInstance();
        o.getClass().getField("id").set(o, uuid);
        o.getClass().getField("string").set(o, "foobar");

        // Insert TestDboV1,
        insertWithoutTypeCheck(uuid, o);

        // Get it as TestDbo. This is in the new version of the software, the datastore is now supposed to migrate before returning to us.
        final TestDbo testDbo = this.serializingDataStore.get(uuid);

        // Assert
        Assert.assertEquals(uuid.toString(), testDbo.id);
        Assert.assertEquals("foobar", testDbo.string);
    }

    @Test
    public void shouldNotMistakinglyAddVersionToCurrentDbo() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        TestDbo testDbo = new TestDbo("idfoo", "foobar");

        // Act
        this.serializingDataStore.insert(uuid, testDbo);
        final TestDbo testDbo1 = this.serializingDataStore.get(uuid);

        // Assert
        Assert.assertEquals(testDbo.id, testDbo1.id);
        Assert.assertEquals(testDbo.string, testDbo1.string);
    }
}
