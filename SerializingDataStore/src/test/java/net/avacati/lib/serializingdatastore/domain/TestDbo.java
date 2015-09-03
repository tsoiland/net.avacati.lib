package net.avacati.lib.serializingdatastore.domain;

import java.io.Serializable;
import java.util.Collection;

public class TestDbo implements Serializable {
    private static final long serialVersionUID = 6;
    public String id;
    public String foobar;
    public Collection<SubTestDbo> subTestDbos;
}
