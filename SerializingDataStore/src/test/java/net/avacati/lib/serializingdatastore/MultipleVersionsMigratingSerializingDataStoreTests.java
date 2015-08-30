package net.avacati.lib.serializingdatastore;

import net.avacati.lib.serializingdatastore.migration.MigratingSerializingDataStoreFactory;
import net.avacati.lib.serializingdatastore.migration.Migrator;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class MultipleVersionsMigratingSerializingDataStoreTests {
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

    /**
     * Test migration from V1 to V3
     */
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
        Assert.assertEquals(36L, testDbo.id);
        Assert.assertEquals("foobarv3", testDbo.string);
    }

    public static class TestDboV1 implements Serializable {
        private static final long serialVersionUID = 1;
        public UUID id;
        public String string;

        public TestDboV1() {
        }

        public TestDboV1(UUID id, String string) {
            this.id = id;
            this.string = string;
        }
    }

    public static class TestDboV2 implements Serializable {
        private static final long serialVersionUID = 2;
        public String id;
        public String string;

        public TestDboV2() {
        }

        public TestDboV2(String id, String string) {
            this.id = id;
            this.string = string;
        }
    }

    public static class TestDbo implements Serializable {
        private static final long serialVersionUID = 3;
        public long id;
        public String string;

        public TestDbo() {
        }

        public TestDbo(long id, String string) {
            this.id = id;
            this.string = string;
        }
    }

    public static class TestDboMigrations extends Migrator {
        public TestDboMigrations() {
            super.add(this::migrate, TestDboV1.class, TestDboV2.class);
            super.add(this::migrate, TestDboV2.class, TestDbo.class);
        }

        TestDboV2 migrate(TestDboV1 v1) {
            return new TestDboV2(v1.id.toString(), v1.string);
        }

        TestDbo migrate(TestDboV2 v2) {
            return new TestDbo(v2.id.length(), v2.string + "v3");
        }
    }
}
