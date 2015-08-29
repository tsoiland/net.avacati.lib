package net.avacati.lib.serializingdatastore.domainmigrations;

import java.io.Serializable;

public class TestDbo implements Serializable {
    private static final long serialVersionUID = 2;
    public String id;
    public String string;

    public TestDbo(String id, String string) {
        this.id = id;
        this.string = string;
    }
}
