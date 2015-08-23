package net.avacati.lib.aggregaterepository;

import java.util.UUID;

public abstract class EntityDbo {
    public UUID id;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityDbo && ((EntityDbo)obj).id.equals(this.id);
    }
}
