package net.avacati.lib.serializingdatastore.migration.domain;

import java.io.Serializable;
import java.util.List;

public class TestDboV5 implements Serializable {
    private static final long serialVersionUID = 5;
    public String id;
    public String foobar;
    public List<SubTestDboV2> subTestDbos;
}
