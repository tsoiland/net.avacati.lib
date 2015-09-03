package net.avacati.lib.serializingdatastore.domain;

import java.io.Serializable;
import java.util.UUID;

public class TestDboV1 implements Serializable {
    private static final long serialVersionUID = 1;
    public UUID id;
    public String foobar;
}
