
package org.neogroup.sparks.model;

import java.util.ArrayList;
import java.util.List;

public class EntityFilterGroup extends EntityFilter {

    private EntityFilterGroupConnector connector;
    private List<EntityFilter> filters;

    public EntityFilterGroup() {
        filters = new ArrayList<>();
        connector = EntityFilterGroupConnector.AND;
    }

    public EntityFilterGroupConnector getConnector() {
        return connector;
    }

    public void setConnector(EntityFilterGroupConnector connector) {
        this.connector = connector;
    }

    public List<EntityFilter> getFilters() {
        return filters;
    }

    public int getSize() {
        return filters.size();
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }

    public void setFilters(List<EntityFilter> filters) {
        this.filters = filters;
    }

    public void addFilter (EntityFilter filter) {
        filters.add(filter);
    }

    public void removeFilter (EntityFilter filter) {
        filters.remove(filter);
    }
}
