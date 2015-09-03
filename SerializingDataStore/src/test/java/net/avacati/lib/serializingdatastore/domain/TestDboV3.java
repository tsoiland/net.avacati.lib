package net.avacati.lib.serializingdatastore.domain;

import java.io.Serializable;

public class TestDboV3 implements Serializable {
    private static final long serialVersionUID = 3;
    public String id;
    public String foobar;
    public SubTestDboV1 subTestDbo;
}
