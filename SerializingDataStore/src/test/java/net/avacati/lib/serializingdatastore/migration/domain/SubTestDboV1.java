package net.avacati.lib.serializingdatastore.migration.domain;

import java.io.Serializable;
import java.util.UUID;

public class SubTestDboV1 implements Serializable {
    private static final long serialVersionUID = 1;
    public UUID id;
    public String substring;
}
