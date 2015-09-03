package net.avacati.lib.serializingdatastore.domain;

import java.io.Serializable;

public class TestDboV4 implements Serializable {
    private static final long serialVersionUID = 4;
    public String id;
    public String foobar;
    public SubTestDboV2 subTestDbo;
}
