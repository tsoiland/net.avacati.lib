package net.avacati.lib.serializingdatastore.domainmigrations;

import java.io.Serializable;
import java.util.UUID;

public class TestDboV1 implements Serializable {
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
