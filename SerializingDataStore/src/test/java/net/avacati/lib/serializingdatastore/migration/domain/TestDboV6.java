package net.avacati.lib.serializingdatastore.migration.domain;

import java.io.Serializable;
import java.util.List;

public class TestDboV6 implements Serializable {
    private static final long serialVersionUID = 6;
    public String id;
    public String foobar;
    public List<SubTestDboV3> subTestDbos;
}
