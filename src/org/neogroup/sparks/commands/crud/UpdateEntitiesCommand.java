
package org.neogroup.sparks.commands.crud;

import org.neogroup.sparks.model.Entity;

import java.util.List;

public class UpdateEntitiesCommand<E extends Entity> extends ModifyEntitiesCommand<E> {

    public UpdateEntitiesCommand(Class<? extends E> resourceClass, E resource) {
        super(resourceClass, resource);
    }

    public UpdateEntitiesCommand(Class<? extends E> resourceClass, List<E> resources) {
        super(resourceClass, resources);
    }
}