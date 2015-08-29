package net.avacati.lib.serializingdatastore.domainmigrations;

import net.avacati.lib.serializingdatastore.migration.Migrator;

public class TestDboMigrations extends Migrator {
    public TestDboMigrations() {
        super.add(this::migrate, TestDboV1.class, TestDbo.class);
    }

    TestDbo migrate(TestDboV1 v1) {
        return new TestDbo(v1.id.toString(), v1.string);
    }
}