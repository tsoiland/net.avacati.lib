package net.avacati.lib.serializingdatastore;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class SerializingDataStoreTests {
    private SerializingDataStore<TestDbo> serializingDataStore;

    @Before
    public void setUp() throws SQLException {
        Connection connection = createH2Connection();
        this.serializingDataStore = SerializingDataStoreFactory.createDataStore(connection, "test_table");
    }

    private Connection createH2Connection() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:");
        return dataSource.getConnection();
    }

    @Test
    public void insertAndGet() throws SQLException {
        // Arrange
        UUID uuid = UUID.randomUUID();
        TestDbo testDbo = new TestDbo(uuid, "foobar");

        // Act
        serializingDataStore.insert(uuid, testDbo);
        final TestDbo testDbo1 = this.serializingDataStore.get(uuid);

        // Assert
        Assert.assertEquals(testDbo.id, testDbo1.id);
        Assert.assertEquals(testDbo.string, testDbo1.string);
    }

    @Test
    public void update() throws SQLException {
        // Arrange
        UUID id = UUID.randomUUID();
        TestDbo insertDbo = new TestDbo(id, "foobar");
        this.serializingDataStore.insert(id, insertDbo);

        // Act
        final TestDbo updateDbo = new TestDbo(id, "bazqux");
        this.serializingDataStore.update(id, updateDbo, insertDbo);

        // Assert
        final TestDbo getDbo = this.serializingDataStore.get(id);
        Assert.assertEquals(updateDbo.id, getDbo.id);
        Assert.assertEquals(updateDbo.string, getDbo.string);
    }

    public static class TestDbo implements Serializable {
        private final UUID uuid;
        UUID id;
        String string;

        public TestDbo(UUID uuid, String string) {
            this.uuid = uuid;
            this.string = string;
        }
    }
}
