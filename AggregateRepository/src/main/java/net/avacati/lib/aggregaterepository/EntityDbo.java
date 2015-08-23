package net.avacati.lib.aggregaterepository;

import java.io.Serializable;
import java.util.UUID;

public abstract class EntityDbo implements Serializable {
    public UUID id;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityDbo && ((EntityDbo)obj).id.equals(this.id);
    }
}
