package net.avacati.lib.serializingdatastore.migration.domain;

import java.io.Serializable;

public class SubTestDboV3 implements Serializable {
    private static final long serialVersionUID = 3;
    public long id;
    public String substring;
    public String newField;
}
