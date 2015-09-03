package net.avacati.lib.serializingdatastore.domain;

import net.avacati.lib.serializingdatastore.migration.AutoMigrator;
import net.avacati.lib.serializingdatastore.migration.Migrator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestDboMigrations extends Migrator {
    public TestDboMigrations() {
        super(TestDbo.class, Arrays.asList(TestDbo.class, SubTestDbo.class));

        this.add(this::v1ToV2, TestDboV1.class, TestDboV2.class);
        this.add(this::v2ToV3, TestDboV2.class, TestDboV3.class);
        this.add(this::v3ToV4, TestDboV3.class, TestDboV4.class);
        this.add(this::v4ToV5, TestDboV4.class, TestDboV5.class);
        this.add(this::v5ToV6, TestDboV5.class, TestDboV6.class);

        // Auto migrate last version
        final AutoMigrator autoMigrator = new AutoMigrator<>(TestDbo.class);
        autoMigrator.addSubDboTypeMap(SubTestDboV3.class, SubTestDbo.class);
        this.add(autoMigrator.createAutoMigration(), TestDboV6.class, TestDbo.class);
    }

    private TestDboV2 v1ToV2(TestDboV1 testDboV1) {
        final TestDboV2 testDboV2 = new TestDboV2();
        testDboV2.id = testDboV1.id.toString();
        testDboV2.foobar = testDboV1.foobar;
        return testDboV2;
    }

    private TestDboV3 v2ToV3(TestDboV2 testDboV2) {
        final TestDboV3 testDboV3 = new TestDboV3();
        testDboV3.id = testDboV2.id;
        testDboV3.foobar = testDboV2.foobar;
        testDboV3.subTestDbo = new SubTestDboV1();
        testDboV3.subTestDbo.id = UUID.fromString(testDboV2.id.replace('0', '1'));
        testDboV3.subTestDbo.substring = "fabricated substring";
        return testDboV3;
    }

    private TestDboV4 v3ToV4(TestDboV3 testDboV3) {
        final TestDboV4 testDboV4 = new TestDboV4();
        testDboV4.id = testDboV3.id;
        testDboV4.foobar = testDboV3.foobar;
        testDboV4.subTestDbo = new SubTestDboV2();
        testDboV4.subTestDbo.id = testDboV3.subTestDbo.id.toString();
        testDboV4.subTestDbo.substring = testDboV3.subTestDbo.substring;
        return testDboV4;
    }

    private TestDboV5 v4ToV5(TestDboV4 testDboV4) {
        final TestDboV5 testDboV5 = new TestDboV5();
        testDboV5.id = testDboV4.id;
        testDboV5.foobar = testDboV4.foobar;
        testDboV5.subTestDbos = new ArrayList<>();
        testDboV5.subTestDbos.add(new SubTestDboV2());
        testDboV5.subTestDbos.get(0).id = testDboV4.subTestDbo.id + "sub1";
        testDboV5.subTestDbos.get(0).substring = testDboV4.subTestDbo.substring + "sub1";
        testDboV5.subTestDbos.add(new SubTestDboV2());
        testDboV5.subTestDbos.get(1).id = testDboV4.subTestDbo.id + "sub2";
        testDboV5.subTestDbos.get(1).substring = testDboV4.subTestDbo.substring + "sub2";
        return testDboV5;
    }

    private TestDboV6 v5ToV6(TestDboV5 testDboV5) {
        final TestDboV6 testDboV6 = new TestDboV6();
        testDboV6.id = testDboV5.id;
        testDboV6.foobar = testDboV5.foobar;
        testDboV6.subTestDbos = testDboV5.subTestDbos
                .stream()
                .map(subTestDboV2 -> {
                    final SubTestDboV3 subTestDboV3 = new SubTestDboV3();
                    subTestDboV3.id = subTestDboV2.id.length();
                    subTestDboV3.substring = subTestDboV2.substring;
                    subTestDboV3.newField = "new value";
                    return subTestDboV3;
                })
                .collect(Collectors.<SubTestDboV3>toList());
        return testDboV6;
    }
}
